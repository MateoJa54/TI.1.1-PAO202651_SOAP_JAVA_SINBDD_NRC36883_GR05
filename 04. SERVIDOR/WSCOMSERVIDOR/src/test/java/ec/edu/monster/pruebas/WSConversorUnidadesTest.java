package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import ec.edu.monster.controladores.ws.WSConversorUnidades;
import ec.edu.monster.modelos.RespuestaAutenticacion;
import ec.edu.monster.modelos.RespuestaCatalogo;
import ec.edu.monster.modelos.RespuestaConversion;
import ec.edu.monster.modelos.RespuestaUnidades;
import ec.edu.monster.modelos.SolicitudConversion;
import ec.edu.monster.seguridad.AutenticacionServicio;
import ec.edu.monster.seguridad.TokenServicio;
import ec.edu.monster.servicios.CatalogoServicio;
import ec.edu.monster.servicios.ConversionServicio;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Prueba el controlador completo (token + servicios + respuestas) sin
 * levantar ningún servidor.
 */
class WSConversorUnidadesTest {

    private static final Instant INICIO = Instant.parse("2026-10-14T12:00:00Z");

    private RelojManual reloj;
    private WSConversorUnidades ws;

    @BeforeEach
    void preparar() {
        reloj = new RelojManual(INICIO);
        TokenServicio tokens = new TokenServicio(Duration.ofMinutes(15), reloj);
        CatalogoServicio catalogo = new CatalogoServicio();
        ws = new WSConversorUnidades(
                new AutenticacionServicio("usuarioPrueba", "clavePrueba", tokens),
                tokens,
                catalogo,
                new ConversionServicio(catalogo));
    }

    private String tokenValido() {
        return ws.autenticar("usuarioPrueba", "clavePrueba").getToken();
    }

    // ---------- autenticar ----------

    @Test
    void autenticarConCredencialCorrectaEntregaToken() {
        RespuestaAutenticacion r = ws.autenticar("usuarioPrueba", "clavePrueba");

        assertEquals("OK", r.getEstado());
        assertNotNull(r.getToken());
        assertEquals("2026-10-14T12:15:00Z", r.getExpiracion());
    }

    @Test
    void autenticarConCredencialIncorrectaEsAuth003() {
        assertEquals("AUTH-003", ws.autenticar("usuarioPrueba", "mala").getCodigo());
    }

    // ---------- operaciones protegidas: el token ----------

    @Test
    void sinTokenTodasLasOperacionesRespondenAuth001() {
        assertEquals("AUTH-001", ws.obtenerCategorias(null).getCodigo());
        assertEquals("AUTH-001", ws.obtenerUnidades("falso", "LONGITUD").getCodigo());
        assertEquals("AUTH-001", ws.convertir("", "LONGITUD", "metros", "pies", 1.0).getCodigo());
    }

    @Test
    void conTokenVencidoTodasLasOperacionesRespondenAuth002() {
        String token = tokenValido();
        reloj.avanzar(Duration.ofMinutes(16));

        assertEquals("AUTH-002", ws.obtenerCategorias(token).getCodigo());
    }

    @Test
    void unaRespuestaDeErrorNoTraeResultado() {
        RespuestaConversion r = ws.convertir("falso", "LONGITUD", "metros", "pies", 1.0);

        assertEquals("ERROR", r.getEstado());
        assertNull(r.getResultado());
    }

    // ---------- operaciones con token válido ----------

    @Test
    void obtenerCategoriasDevuelveLasTres() {
        RespuestaCatalogo r = ws.obtenerCategorias(tokenValido());

        assertEquals("OK", r.getEstado());
        assertEquals(List.of("LONGITUD", "MASA", "TEMPERATURA"), r.getCategorias());
    }

    @Test
    void obtenerUnidadesDevuelveLasDeLaCategoria() {
        RespuestaUnidades r = ws.obtenerUnidades(tokenValido(), "masa");

        assertEquals("OK", r.getEstado());
        assertEquals("MASA", r.getCategoria());
        assertEquals(5, r.getUnidades().size());
    }

    @Test
    void obtenerUnidadesDeCategoriaInexistenteEsData001() {
        RespuestaUnidades r = ws.obtenerUnidades(tokenValido(), "VOLUMEN");

        assertEquals("DATA-001", r.getCodigo());
        assertEquals(List.of(), r.getUnidades());
    }

    @Test
    void convertirDevuelveElResultado() {
        RespuestaConversion r = ws.convertir(tokenValido(), "LONGITUD", "millas", "metros", 2.0);

        assertEquals("OK", r.getEstado());
        assertEquals(3218.688, r.getResultado(), 1e-9);
    }

    @Test
    void convertirConUnidadInvalidaDevuelveElCodigoDeLaFicha() {
        String token = tokenValido();

        assertEquals("UNIT-001", ws.convertir(token, "LONGITUD", "xyz", "metros", 1.0).getCodigo());
        assertEquals("UNIT-002", ws.convertir(token, "LONGITUD", "metros", "xyz", 1.0).getCodigo());
        assertEquals("DATA-001", ws.convertir(token, "LONGITUD", "metros", "celsius", 1.0).getCodigo());
    }

    @Test
    void convertirSinValorEsData001() {
        RespuestaConversion r = ws.convertir(tokenValido(), "LONGITUD", "metros", "pies", null);

        assertEquals("DATA-001", r.getCodigo());
        assertNull(r.getResultado());
    }

    // ---------- el cliente siempre recibe respuesta ----------

    @Test
    void unaExcepcionInesperadaSeConvierteEnSys001() {
        TokenServicio tokens = new TokenServicio(Duration.ofMinutes(15), reloj);
        CatalogoServicio catalogo = new CatalogoServicio();
        ConversionServicio roto = new ConversionServicio(catalogo) {
            @Override
            public RespuestaConversion convertir(SolicitudConversion solicitud) {
                throw new IllegalStateException("fallo simulado");
            }
        };
        WSConversorUnidades wsRoto = new WSConversorUnidades(
                new AutenticacionServicio("u", "p", tokens), tokens, catalogo, roto);
        String token = wsRoto.autenticar("u", "p").getToken();

        RespuestaConversion r = wsRoto.convertir(token, "LONGITUD", "metros", "pies", 1.0);

        assertEquals("ERROR", r.getEstado());
        assertEquals("SYS-001", r.getCodigo());
        assertEquals("Error interno del servidor", r.getMensaje());
    }
}
