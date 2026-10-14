package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.Main;
import ec.edu.monster.modelo.Respuesta;
import ec.edu.monster.modelo.RespuestaAutenticacion;
import ec.edu.monster.modelo.RespuestaCatalogo;
import ec.edu.monster.modelo.RespuestaConversion;
import ec.edu.monster.modelo.RespuestaUnidades;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.modelo.SolicitudConversion;
import ec.edu.monster.util.RespuestaUtil;
import ec.edu.monster.ws.ClienteWSConversor;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Modelo (solo datos), lectura de códigos de respuesta y configuración de
 * la dirección del servidor.
 */
class ModeloYUtilTest {

    private static RespuestaConversion respuesta(String estado, String codigo, String mensaje) {
        RespuestaConversion r = new RespuestaConversion();
        r.setEstado(estado);
        r.setCodigo(codigo);
        r.setMensaje(mensaje);
        return r;
    }

    @AfterEach
    void limpiarPropiedad() {
        System.clearProperty("ws.url");
    }

    // ---------- modelo ----------

    @Test
    void unaSesionNuevaNoTieneToken() {
        SesionModelo sesion = new SesionModelo();

        assertNull(sesion.getToken());
        assertNull(sesion.getExpiracion());
    }

    @Test
    void laSesionGuardaLoQueSeLeAsigna() {
        SesionModelo sesion = new SesionModelo();
        sesion.setToken("abc");
        sesion.setExpiracion("2026-10-14T12:15:00Z");

        assertEquals("abc", sesion.getToken());
        assertEquals("2026-10-14T12:15:00Z", sesion.getExpiracion());
    }

    @Test
    void laSolicitudConservaSusCuatroDatos() {
        SolicitudConversion s = new SolicitudConversion("MASA", "libras", "onzas", -2.5);

        assertEquals("MASA", s.getCategoria());
        assertEquals("libras", s.getUnidadOrigen());
        assertEquals("onzas", s.getUnidadDestino());
        assertEquals(-2.5, s.getValor(), 0.0);
    }

    @Test
    void laSolicitudVaciaAdmiteValorNuloSinConvertirloEnCero() {
        SolicitudConversion s = new SolicitudConversion();

        assertNull(s.getValor());
        s.setValor(0.0);
        assertEquals(0.0, s.getValor(), 0.0);
    }

    @Test
    void lasListasDeUnaRespuestaNuevaEstanVaciasYNoNulas() {
        assertNotNull(new RespuestaCatalogo().getCategorias());
        assertTrue(new RespuestaCatalogo().getCategorias().isEmpty());
        assertTrue(new RespuestaUnidades().getUnidades().isEmpty());
    }

    @Test
    void unaRespuestaNuevaNoTraeDatosInventados() {
        RespuestaConversion conversion = new RespuestaConversion();
        RespuestaAutenticacion autenticacion = new RespuestaAutenticacion();

        assertNull(conversion.getEstado());
        assertNull(conversion.getResultado());
        assertNull(autenticacion.getToken());
        assertNull(new RespuestaUnidades().getCategoria());
    }

    @Test
    void lasCuatroRespuestasCompartenEstadoCodigoYMensaje() {
        for (Respuesta r : List.of(new RespuestaAutenticacion(), new RespuestaCatalogo(),
                new RespuestaUnidades(), new RespuestaConversion())) {
            r.setEstado("ERROR");
            r.setCodigo("SYS-001");
            r.setMensaje("Error interno del servidor");

            assertEquals("ERROR", r.getEstado());
            assertEquals("SYS-001", r.getCodigo());
            assertEquals("Error interno del servidor", r.getMensaje());
        }
    }

    // ---------- RespuestaUtil ----------

    @Test
    void soloElEstadoOkExactoEsExito() {
        assertTrue(RespuestaUtil.esExitosa(respuesta("OK", "200", "bien")));
        assertFalse(RespuestaUtil.esExitosa(respuesta("ERROR", "DATA-001", "mal")));
        assertFalse(RespuestaUtil.esExitosa(respuesta("ok", "200", "bien")));
        assertFalse(RespuestaUtil.esExitosa(respuesta(" OK ", "200", "bien")));
        assertFalse(RespuestaUtil.esExitosa(respuesta(null, "200", "bien")));
        assertFalse(RespuestaUtil.esExitosa(respuesta("", "200", "bien")));
        assertFalse(RespuestaUtil.esExitosa(null));
    }

    @Test
    void elExitoSeDecidePorElEstadoYNoPorElCodigo() {
        // Un 200 con estado ERROR sería una contradicción del servidor: no se trata como éxito.
        assertFalse(RespuestaUtil.esExitosa(respuesta("ERROR", "200", "raro")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"AUTH-001", "AUTH-002"})
    void tokenInvalidoYSesionExpiradaExigenAutenticarse(String codigo) {
        assertTrue(RespuestaUtil.requiereAutenticarse(respuesta("ERROR", codigo, "x")));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "200", "AUTH-003", "UNIT-001", "UNIT-002", "DATA-001", "SYS-001", "", "auth-001", "AUTH-0010", "XYZ-999"})
    void losDemasCodigosNoCierranLaSesion(String codigo) {
        assertFalse(RespuestaUtil.requiereAutenticarse(respuesta("ERROR", codigo, "x")));
    }

    @Test
    void requiereAutenticarseToleraNulos() {
        assertFalse(RespuestaUtil.requiereAutenticarse(null));
        assertFalse(RespuestaUtil.requiereAutenticarse(respuesta("ERROR", null, null)));
    }

    @Test
    void elErrorSeDescribeConCodigoYMensaje() {
        assertEquals("[UNIT-001] Unidad de origen no soportada",
                RespuestaUtil.describirError(respuesta("ERROR", "UNIT-001", "Unidad de origen no soportada")));
    }

    @Test
    void describirUnErrorSinMensajeNoFalla() {
        assertEquals("[SYS-001] null", RespuestaUtil.describirError(respuesta("ERROR", "SYS-001", null)));
    }

    // ---------- dirección del servidor ----------

    @Test
    void sinConfiguracionSeUsaElServidorLocal() {
        // Solo vale si el equipo no define WS_URL; en ese caso gana la variable.
        String esperado = System.getenv("WS_URL") == null || System.getenv("WS_URL").isBlank()
                ? ClienteWSConversor.URL_POR_DEFECTO
                : System.getenv("WS_URL").trim();

        assertEquals(esperado, Main.resolverUrl(new String[0]));
        assertEquals(esperado, Main.resolverUrl(null));
    }

    @Test
    void laDireccionPorDefectoApuntaAlServicioYNoAlWsdl() {
        assertEquals("http://localhost:8080/WSCOMSERVIDOR/WSConversorUnidades",
                ClienteWSConversor.URL_POR_DEFECTO);
    }

    @Test
    void elArgumentoDelProgramaTienePrioridad() {
        System.setProperty("ws.url", "http://propiedad:8080/ws");

        assertEquals("http://argumento:8080/ws", Main.resolverUrl(new String[] {"  http://argumento:8080/ws "}));
    }

    @Test
    void sinArgumentoSeUsaLaPropiedadDelSistema() {
        System.setProperty("ws.url", " http://propiedad:8080/ws ");

        assertEquals("http://propiedad:8080/ws", Main.resolverUrl(new String[0]));
    }

    @ParameterizedTest
    @CsvSource(value = {"''", "'   '"})
    void unArgumentoVacioSeIgnora(String argumento) {
        System.setProperty("ws.url", "http://propiedad:8080/ws");

        assertEquals("http://propiedad:8080/ws", Main.resolverUrl(new String[] {argumento}));
    }

    @Test
    void soloSeUsaElPrimerArgumento() {
        assertEquals("http://uno/ws", Main.resolverUrl(new String[] {"http://uno/ws", "http://dos/ws"}));
    }
}
