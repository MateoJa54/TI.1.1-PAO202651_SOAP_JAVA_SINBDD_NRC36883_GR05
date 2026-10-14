package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.modelos.CodigoRespuesta;
import ec.edu.monster.modelos.RespuestaAutenticacion;
import ec.edu.monster.seguridad.AutenticacionServicio;
import ec.edu.monster.seguridad.TokenServicio;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Casos borde de la seguridad: el instante exacto de expiración, tokens
 * alterados, credenciales parecidas a la correcta y uso simultáneo.
 */
class SeguridadBordeTest {

    private static final Instant INICIO = Instant.parse("2026-10-14T12:00:00Z");
    private static final Duration VIGENCIA = Duration.ofMinutes(15);

    private RelojManual reloj;
    private TokenServicio tokens;
    private AutenticacionServicio autenticacion;

    @BeforeEach
    void preparar() {
        reloj = new RelojManual(INICIO);
        tokens = new TokenServicio(VIGENCIA, reloj);
        autenticacion = new AutenticacionServicio("usuarioPrueba", "Clave Prueba#1", tokens);
    }

    // ---------- TokenServicio: el límite de tiempo ----------

    @Test
    void unInstanteAntesDeExpirarElTokenTodaviaSirve() {
        String token = tokens.generarToken();
        reloj.avanzar(VIGENCIA.minusNanos(1));

        assertEquals(CodigoRespuesta.OK_200, tokens.validarToken(token));
    }

    @Test
    void enElInstanteExactoDeExpiracionElTokenYaNoSirve() {
        String token = tokens.generarToken();
        reloj.avanzar(VIGENCIA);

        assertEquals(CodigoRespuesta.AUTH_002, tokens.validarToken(token));
    }

    @Test
    void usarElTokenNoAlargaSuVigencia() {
        // La ficha habla de 15 minutos desde la autenticación, no desde el último uso.
        String token = tokens.generarToken();
        reloj.avanzar(Duration.ofMinutes(10));
        assertEquals(CodigoRespuesta.OK_200, tokens.validarToken(token));
        reloj.avanzar(Duration.ofMinutes(10));

        assertEquals(CodigoRespuesta.AUTH_002, tokens.validarToken(token));
    }

    @Test
    void elMismoTokenSePuedeUsarMuchasVecesMientrasEsteVigente() {
        String token = tokens.generarToken();

        for (int i = 0; i < 1000; i++) {
            assertEquals(CodigoRespuesta.OK_200, tokens.validarToken(token));
        }
    }

    @Test
    void cadaSesionVencePorSeparado() {
        String primero = tokens.generarToken();
        reloj.avanzar(Duration.ofMinutes(10));
        String segundo = tokens.generarToken();
        reloj.avanzar(Duration.ofMinutes(6));

        assertEquals(CodigoRespuesta.AUTH_002, tokens.validarToken(primero));
        assertEquals(CodigoRespuesta.OK_200, tokens.validarToken(segundo));
    }

    @Test
    void siElRelojDelServidorRetrocedeElTokenSigueSiendoValido() {
        String token = tokens.generarToken();
        reloj.avanzar(Duration.ofHours(-1));

        assertEquals(CodigoRespuesta.OK_200, tokens.validarToken(token));
    }

    // ---------- TokenServicio: tokens alterados ----------

    @Test
    void unTokenAlteradoEsAuth001() {
        String token = tokens.generarToken();

        assertEquals(CodigoRespuesta.AUTH_001, tokens.validarToken(token.toUpperCase()));
        assertEquals(CodigoRespuesta.AUTH_001, tokens.validarToken(" " + token));
        assertEquals(CodigoRespuesta.AUTH_001, tokens.validarToken(token + " "));
        assertEquals(CodigoRespuesta.AUTH_001, tokens.validarToken(token.substring(1)));
        assertEquals(CodigoRespuesta.AUTH_001, tokens.validarToken(token + "0"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "undefined", "0", "true", "' OR '1'='1", "<token/>", "\u0000"})
    void losValoresTipicosDeUnClienteDefectuosoSonAuth001(String basura) {
        assertEquals(CodigoRespuesta.AUTH_001, tokens.validarToken(basura));
    }

    @Test
    void unTokenDeOtroServidorNoSirveEnEste() {
        TokenServicio otroServidor = new TokenServicio(VIGENCIA, reloj);

        assertEquals(CodigoRespuesta.AUTH_001, tokens.validarToken(otroServidor.generarToken()));
    }

    @Test
    void elTokenTieneFormatoUuidYNoEsPredecible() {
        String token = tokens.generarToken();

        assertTrue(token.matches("[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"),
                "Se esperaba un UUID versión 4 (aleatorio): " + token);
    }

    @Test
    void elRelojYLaVigenciaSonObligatorios() {
        assertThrows(NullPointerException.class, () -> new TokenServicio(null, reloj));
        assertThrows(NullPointerException.class, () -> new TokenServicio(VIGENCIA, null));
    }

    @Test
    void elConstructorPorDefectoUsaElRelojRealY15Minutos() {
        TokenServicio real = new TokenServicio();
        Instant antes = Clock.systemUTC().instant();

        Instant expiracion = real.obtenerExpiracion(real.generarToken()).orElseThrow();

        Duration vigenciaReal = Duration.between(antes, expiracion);
        assertTrue(vigenciaReal.compareTo(Duration.ofMinutes(14)) > 0
                && vigenciaReal.compareTo(Duration.ofMinutes(16)) < 0);
    }

    // ---------- TokenServicio: varios clientes a la vez ----------

    @Test
    void muchosClientesALaVezRecibenTokensDistintosYValidos() throws Exception {
        int hilos = 16;
        int tokensPorHilo = 250;
        Set<String> generados = ConcurrentHashMap.newKeySet();
        ExecutorService ejecutor = Executors.newFixedThreadPool(hilos);
        try {
            List<Future<Boolean>> tareas = new ArrayList<>();
            for (int h = 0; h < hilos; h++) {
                Callable<Boolean> tarea = () -> {
                    boolean todosValidos = true;
                    for (int i = 0; i < tokensPorHilo; i++) {
                        String token = tokens.generarToken();
                        generados.add(token);
                        todosValidos &= tokens.validarToken(token) == CodigoRespuesta.OK_200;
                    }
                    return todosValidos;
                };
                tareas.add(ejecutor.submit(tarea));
            }
            for (Future<Boolean> tarea : tareas) {
                assertTrue(tarea.get(30, TimeUnit.SECONDS));
            }
        } finally {
            ejecutor.shutdownNow();
        }

        assertEquals(hilos * tokensPorHilo, generados.size());
    }

    // ---------- AutenticacionServicio: credenciales parecidas ----------

    @ParameterizedTest
    @ValueSource(strings = {
        "USUARIOPRUEBA", "usuarioprueba", " usuarioPrueba", "usuarioPrueba ", "usuarioPrueb", "usuarioPruebaa"})
    void elUsuarioDebeCoincidirExactamente(String usuarioParecido) {
        assertEquals("AUTH-003", autenticacion.autenticar(usuarioParecido, "Clave Prueba#1").getCodigo());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "clave prueba#1", "ClavePrueba#1", "Clave Prueba#1 ", " Clave Prueba#1", "Clave Prueba#", "Clave  Prueba#1"})
    void laContrasenaDebeCoincidirExactamente(String claveParecida) {
        assertEquals("AUTH-003", autenticacion.autenticar("usuarioPrueba", claveParecida).getCodigo());
    }

    @Test
    void laContrasenaPuedeTenerEspaciosYSimbolos() {
        assertEquals("OK", autenticacion.autenticar("usuarioPrueba", "Clave Prueba#1").getEstado());
    }

    @Test
    void laCredencialPuedeTenerTildesYEnie() {
        AutenticacionServicio conTildes = new AutenticacionServicio("muñoz", "contraseña-ñandú", tokens);

        assertEquals("OK", conTildes.autenticar("muñoz", "contraseña-ñandú").getEstado());
        assertEquals("AUTH-003", conTildes.autenticar("munoz", "contraseña-ñandú").getCodigo());
    }

    @Test
    void usuarioYContrasenaIntercambiadosNoAutentican() {
        assertEquals("AUTH-003", autenticacion.autenticar("Clave Prueba#1", "usuarioPrueba").getCodigo());
    }

    @Test
    void siFaltaUnSoloCampoEsData001YNoAuth003() {
        assertEquals("DATA-001", autenticacion.autenticar("usuarioPrueba", null).getCodigo());
        assertEquals("DATA-001", autenticacion.autenticar(null, "Clave Prueba#1").getCodigo());
        assertEquals("DATA-001", autenticacion.autenticar("usuarioPrueba", "").getCodigo());
    }

    @Test
    void unaEntradaEnormeOMaliciosaSoloEsUnaCredencialIncorrecta() {
        assertEquals("AUTH-003", autenticacion.autenticar("x".repeat(100_000), "y".repeat(100_000)).getCodigo());
        assertEquals("AUTH-003", autenticacion.autenticar("admin' --", "' OR '1'='1").getCodigo());
        assertEquals("AUTH-003", autenticacion.autenticar("<usuario>", "]]>").getCodigo());
    }

    @Test
    void elErrorNoRevelaSiFalloElUsuarioOLaContrasena() {
        RespuestaAutenticacion malUsuario = autenticacion.autenticar("otro", "Clave Prueba#1");
        RespuestaAutenticacion malaClave = autenticacion.autenticar("usuarioPrueba", "otra");

        assertEquals(malUsuario.getCodigo(), malaClave.getCodigo());
        assertEquals(malUsuario.getMensaje(), malaClave.getMensaje());
    }

    @Test
    void unIntentoFallidoNoEntregaTokenNiExpiracion() {
        RespuestaAutenticacion r = autenticacion.autenticar("usuarioPrueba", "otra");

        assertEquals("ERROR", r.getEstado());
        assertNull(r.getToken());
        assertNull(r.getExpiracion());
    }

    @Test
    void cadaAutenticacionAbreUnaSesionNuevaSinCerrarLaAnterior() {
        String primero = autenticacion.autenticar("usuarioPrueba", "Clave Prueba#1").getToken();
        String segundo = autenticacion.autenticar("usuarioPrueba", "Clave Prueba#1").getToken();

        assertNotEquals(primero, segundo);
        assertEquals(CodigoRespuesta.OK_200, tokens.validarToken(primero));
        assertEquals(CodigoRespuesta.OK_200, tokens.validarToken(segundo));
    }

    @Test
    void despuesDeVariosFallosLaCredencialCorrectaSigueFuncionando() {
        for (int i = 0; i < 50; i++) {
            autenticacion.autenticar("usuarioPrueba", "intento" + i);
        }

        assertEquals("OK", autenticacion.autenticar("usuarioPrueba", "Clave Prueba#1").getEstado());
    }

    @Test
    void laExpiracionSeEntregaEnUtcYSinFraccionesDeSegundo() {
        reloj.avanzar(Duration.ofMillis(789));

        RespuestaAutenticacion r = autenticacion.autenticar("usuarioPrueba", "Clave Prueba#1");

        assertEquals("2026-10-14T12:15:00Z", r.getExpiracion());
    }

    @Test
    void elServicioDeTokensEsObligatorio() {
        assertThrows(NullPointerException.class, () -> new AutenticacionServicio("u", "p", null));
        assertThrows(IllegalArgumentException.class, () -> new AutenticacionServicio(null, "p", tokens));
    }
}
