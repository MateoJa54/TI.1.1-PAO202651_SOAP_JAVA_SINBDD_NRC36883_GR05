package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.modelo.RespuestaAutenticacion;
import ec.edu.monster.modelo.RespuestaCatalogo;
import ec.edu.monster.modelo.RespuestaConversion;
import ec.edu.monster.modelo.RespuestaUnidades;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.modelo.SolicitudConversion;
import ec.edu.monster.util.NumeroUtil;
import ec.edu.monster.util.RespuestaUtil;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Batería amplia de casos sobre las utilidades y el modelo compartidos. Cada
 * caso es independiente y no necesita el servidor encendido.
 */
class CasosMasivosTest {

    // ---------- lectura de números ----------

    @ParameterizedTest
    @CsvSource({
        "12,12", "-3.5,-3.5", "0,0", "+7,7", "42,42", "-0,0",
        "0.0,0.0", "1000000,1000000", "1.5,1.5", "-100,-100",
        "3.14159,3.14159", "2.5e2,250", "5,5", "99,99", "0.125,0.125"
    })
    void aceptaNumerosConComa(String texto, double esperado) {
        assertEquals(esperado, NumeroUtil.interpretarDecimal(texto.replace('.', ',')).orElseThrow(), 1e-9);
    }

    @ParameterizedTest
    @CsvSource({
        "12,12", "-3.5,-3.5", "0,0", "+7,7", "42,42", "-0,0",
        "0.0,0.0", "1000000,1000000", "1.5,1.5", "-100,-100",
        "3.14159,3.14159", "2.5e2,250", "5,5", "99,99", "0.125,0.125"
    })
    void aceptaNumerosConPunto(String texto, double esperado) {
        assertEquals(esperado, NumeroUtil.interpretarDecimal(texto).orElseThrow(), 1e-9);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "abc", "1a", "", "  ", ".", ",", "1e", "--1", "1.2.3",
        "+", "e3", "1,2,3", "1 2", "0x10", "NaN", "Infinity", "3..5"
    })
    void rechazaTextosQueNoSonNumeros(String texto) {
        assertTrue(NumeroUtil.interpretarDecimal(texto).isEmpty());
    }

    @Test
    void nullNoEsNumero() {
        assertTrue(NumeroUtil.interpretarDecimal(null).isEmpty());
    }

    // ---------- presentación de números ----------

    @ParameterizedTest
    @CsvSource({
        "0,0", "1,1", "100,100", "25,25", "-2.5,-2.5",
        "0.5,0.5", "1609.344,1609.344", "0.3048,0.3048", "1000000,1000000"
    })
    void formateaSinCerosSobrantes(double numero, String esperado) {
        assertEquals(esperado, NumeroUtil.formatear(numero));
    }

    @Test
    void formateaNegativoCeroComoCero() {
        assertEquals("0", NumeroUtil.formatear(-0.0));
    }

    @Test
    void losNumerosEnormesNoSonNulos() {
        assertNotNull(NumeroUtil.formatear(1e20));
    }

    @Test
    void losNumerosDiminutosNoSonNulos() {
        assertNotNull(NumeroUtil.formatear(1e-9));
    }

    // ---------- respuesta: lectura de estado ----------

    @ParameterizedTest
    @ValueSource(strings = {"OK"})
    void elEstadoExitosoEsOk(String estado) {
        RespuestaAutenticacion r = new RespuestaAutenticacion();
        r.setEstado(estado);
        assertTrue(RespuestaUtil.esExitosa(r));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ok", "Ok", "oK", "ERROR", "KO", ""})
    void otrosEstadosNoSonExitosos(String estado) {
        RespuestaAutenticacion r = new RespuestaAutenticacion();
        r.setEstado(estado);
        assertFalse(RespuestaUtil.esExitosa(r));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ERROR", "ERROR_AUTH", "FALLO", "NO", "401"})
    void losEstadosDistintosDeOkNoSonExitosos(String estado) {
        RespuestaAutenticacion r = new RespuestaAutenticacion();
        r.setEstado(estado);
        assertFalse(RespuestaUtil.esExitosa(r));
    }

    @Test
    void unaRespuestaNulaNoEsExitosa() {
        assertFalse(RespuestaUtil.esExitosa(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"AUTH-001", "AUTH-002"})
    void ciertosCodigosExigenVolverAAutenticarse(String codigo) {
        RespuestaAutenticacion r = new RespuestaAutenticacion();
        r.setEstado("ERROR");
        r.setCodigo(codigo);
        assertTrue(RespuestaUtil.requiereAutenticarse(r));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GEN-001", "VAL-002", "SERV-003", "AUTH-003"})
    void otrosCodigosNoPidenAutenticarse(String codigo) {
        RespuestaAutenticacion r = new RespuestaAutenticacion();
        r.setEstado("ERROR");
        r.setCodigo(codigo);
        assertFalse(RespuestaUtil.requiereAutenticarse(r));
    }

    // ---------- modelo ----------

    @Test
    void sesionNuevaVacia() {
        SesionModelo s = new SesionModelo();
        assertNotNull(s);
        assertEquals(null, s.getToken());
    }

    @Test
    void sesionGuardaToken() {
        SesionModelo s = new SesionModelo();
        s.setToken("token-123");
        assertEquals("token-123", s.getToken());
    }

    @Test
    void sesionGuardaExpiracion() {
        SesionModelo s = new SesionModelo();
        s.setExpiracion("2026-12-31T23:59:59Z");
        assertEquals("2026-12-31T23:59:59Z", s.getExpiracion());
    }

    @Test
    void solicitudConservaDatos() {
        SolicitudConversion s = new SolicitudConversion("LONGITUD", "metros", "pies", 3.0);
        assertEquals("LONGITUD", s.getCategoria());
        assertEquals("metros", s.getUnidadOrigen());
        assertEquals("pies", s.getUnidadDestino());
        assertEquals(3.0, s.getValor(), 1e-9);
    }

    @Test
    void solicitudVaciaSePuedeRellenar() {
        SolicitudConversion s = new SolicitudConversion();
        s.setCategoria("MASA");
        s.setUnidadOrigen("gramos");
        s.setUnidadDestino("kilogramos");
        s.setValor(500.0);
        assertEquals("MASA", s.getCategoria());
        assertEquals(500.0, s.getValor(), 1e-9);
    }

    @Test
    void respuestaAutenticacionGuardaToken() {
        RespuestaAutenticacion r = new RespuestaAutenticacion();
        r.setToken("abc");
        r.setExpiracion("2026-12-31T23:59:59Z");
        assertEquals("abc", r.getToken());
        assertEquals("2026-12-31T23:59:59Z", r.getExpiracion());
    }

    @Test
    void catalogoGuardaCategorias() {
        RespuestaCatalogo r = new RespuestaCatalogo();
        r.setCategorias(List.of("MASA", "LONGITUD", "TEMPERATURA"));
        assertEquals(3, r.getCategorias().size());
    }

    @Test
    void unidadesGuardaLista() {
        RespuestaUnidades r = new RespuestaUnidades();
        r.setUnidades(List.of("gramos", "kilogramos", "libras"));
        assertEquals(3, r.getUnidades().size());
    }

    @Test
    void conversionGuardaResultado() {
        RespuestaConversion r = new RespuestaConversion();
        r.setUnidadOrigen("kilometros");
        r.setUnidadDestino("millas");
        r.setResultado(6.21371);
        assertEquals("kilometros", r.getUnidadOrigen());
        assertEquals("millas", r.getUnidadDestino());
        assertEquals(6.21371, r.getResultado(), 1e-9);
    }

    @Test
    void conversionOkEsExitosa() {
        RespuestaConversion r = new RespuestaConversion();
        r.setEstado("OK");
        assertTrue(RespuestaUtil.esExitosa(r));
    }

    @Test
    void conversionErrorNoEsExitosa() {
        RespuestaConversion r = new RespuestaConversion();
        r.setEstado("ERROR");
        r.setCodigo("SERV-001");
        assertFalse(RespuestaUtil.esExitosa(r));
    }
}
