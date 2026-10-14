package ec.edu.monster;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.modelo.RespuestaConversion;
import ec.edu.monster.util.EntradaUtil;
import ec.edu.monster.util.RespuestaUtil;
import ec.edu.monster.ws.ClienteWSConversor;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Casos borde de EntradaUtil (lo que escribe el usuario), de cómo se muestran
 * los resultados, de RespuestaUtil y de la dirección del servicio.
 */
class EntradaYFormatoBordeTest {

    // ---------- interpretarDecimal ----------

    @ParameterizedTest
    @ValueSource(strings = {
        "1.000,5", "1_000", "1 000", "0x1A", "1e", "e5", "+", "-", ".", ",", "--1",
        "١٢", "１２", "∞", "Infinity", "NaN", "1e400", "1.5.2", "1,5,2"
    })
    void textosQueParecenNumerosPeroNoLoSonSeRechazan(String texto) {
        assertEquals(Optional.empty(), EntradaUtil.interpretarDecimal(texto), texto);
    }

    @ParameterizedTest
    @CsvSource({
        "'  -.5 ', -0.5",
        "'1.', 1.0",
        "'+0', 0.0",
        "'00012', 12.0",
        "'4.9e-324', 4.9e-324",
        "'1E308', 1e308",
        "'1,5e2', 150.0"
    })
    void formasValidasMenosComunesSeAceptan(String texto, double esperado) {
        assertEquals(Optional.of(esperado), EntradaUtil.interpretarDecimal(texto));
    }

    @Test
    void unNumeroQueSeRedondeaACeroSeAceptaComoCeroNegativo() {
        // Comportamiento actual: "-1e-400" se convierte en -0.0 sin avisar al usuario.
        assertEquals(Optional.of(-0.0), EntradaUtil.interpretarDecimal("-1e-400"));
    }

    // ---------- formatear ----------

    @ParameterizedTest
    @CsvSource({
        "-0.0, 0",
        "0.30000000000000004, 0.3",
        "1.0E-6, 0.000001",
        "1.0E308, 1E+308"
    })
    void formatearCasosLimite(double numero, String esperado) {
        assertEquals(esperado, EntradaUtil.formatear(numero));
    }

    @Test
    void elNumeroMasPequenoSeMuestraEnNotacionCientifica() {
        assertTrue(EntradaUtil.formatear(Double.MIN_VALUE).contains("E-324"));
    }

    @Test
    void hallazgoActualUnNumeroGrandeSeMuestraRedondeadoComoSiFueraExacto() {
        // 12 cifras significativas en notación normal: los últimos dígitos se vuelven ceros.
        assertEquals("123456789012000", EntradaUtil.formatear(123456789012345.0));
    }

    @Test
    void unValorNoFinitoSeMuestraTalCualYNoRompeLaConsola() {
        // El contrato xsd:double permite NaN e infinitos aunque el servidor nunca los envíe.
        assertEquals("NaN", EntradaUtil.formatear(Double.NaN));
        assertEquals("Infinity", EntradaUtil.formatear(Double.POSITIVE_INFINITY));
        assertEquals("-Infinity", EntradaUtil.formatear(Double.NEGATIVE_INFINITY));
    }

    // ---------- RespuestaUtil ----------

    @Test
    void unEstadoOkEnMinusculasOConEspaciosNoEsExito() {
        RespuestaConversion respuesta = new RespuestaConversion();
        respuesta.setEstado("ok");
        assertFalse(RespuestaUtil.esExitosa(respuesta));
        respuesta.setEstado(" OK ");
        assertFalse(RespuestaUtil.esExitosa(respuesta));
    }

    @Test
    void unCodigoDeAutenticacionConOtrasMayusculasNoCierraLaSesion() {
        RespuestaConversion respuesta = new RespuestaConversion();
        respuesta.setCodigo("auth-001");

        assertFalse(RespuestaUtil.requiereAutenticarse(respuesta));
    }

    @Test
    void auth003NoCierraLaSesionPorqueSoloOcurreAlAutenticarse() {
        RespuestaConversion respuesta = new RespuestaConversion();
        respuesta.setCodigo("AUTH-003");

        assertFalse(RespuestaUtil.requiereAutenticarse(respuesta));
    }

    // ---------- dirección del servicio ----------

    @ParameterizedTest
    @ValueSource(strings = {"http://", "localhost:8080", "ftp://localhost/x", "//localhost:8080", "http:// espacio", "  "})
    void direccionesQueNoSonHttpSeRechazanAlCrearElCliente(String url) {
        assertThrows(IllegalArgumentException.class, () -> new ClienteWSConversor(url));
    }

    @Test
    void laDireccionSeGuardaSinEspaciosAlrededor() {
        assertEquals("http://localhost:8080/x", new ClienteWSConversor("  http://localhost:8080/x \t").getUrl());
    }

    @Test
    void lasMayusculasEnElEsquemaSeAceptan() {
        assertDoesNotThrow(() -> new ClienteWSConversor("HTTP://localhost:8080/x"));
    }
}
