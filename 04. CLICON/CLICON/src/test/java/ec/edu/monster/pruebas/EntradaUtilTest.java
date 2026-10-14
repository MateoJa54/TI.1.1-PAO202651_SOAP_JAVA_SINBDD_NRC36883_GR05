package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.util.EntradaUtil;
import ec.edu.monster.util.FinDeEntradaException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class EntradaUtilTest {

    private final ByteArrayOutputStream pantalla = new ByteArrayOutputStream();

    /** Crea una entrada donde cada elemento es una línea que "escribe" el usuario. */
    private EntradaUtil conTeclado(String... lineas) {
        String texto = lineas.length == 0 ? "" : String.join("\n", lineas) + "\n";
        return new EntradaUtil(
                new ByteArrayInputStream(texto.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(pantalla, true, StandardCharsets.UTF_8));
    }

    private String pantalla() {
        return pantalla.toString(StandardCharsets.UTF_8);
    }

    // ---------- interpretarDecimal ----------

    @ParameterizedTest(name = "[{0}] = {1}")
    @CsvSource(delimiter = '|', value = {
        "12 | 12", "12.5 | 12.5", "12,5 | 12.5", "-3.25 | -3.25", "+7 | 7", "0 | 0", "-0 | 0",
        ".5 | 0.5", ",5 | 0.5", "5. | 5", "1e3 | 1000", "2.5E-2 | 0.025", "007 | 7",
        "'  42  ' | 42", "0.000001 | 0.000001", "123456789.123 | 123456789.123"})
    void aceptaNumerosConPuntoOComa(String texto, double esperado) {
        assertEquals(esperado, EntradaUtil.interpretarDecimal(texto).orElseThrow(), 1e-12);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        " ", "abc", "12abc", "1.2.3", "1,2,3", "1 2", "--5", "+-5", ".", ",", "-", "e5",
        "NaN", "Infinity", "-Infinity", "0x1F", "1e", "1e999", "12f", "12d", "1_000", "$5", "5%", "١٢"})
    void rechazaLoQueNoEsUnNumeroFinito(String texto) {
        assertEquals(Optional.empty(), EntradaUtil.interpretarDecimal(texto));
    }

    // ---------- formatear ----------

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
        "1609.344, 1609.344", "100.0, 100", "0.0, 0", "-0.0, 0", "0.3048, 0.3048",
        "0.30479999999999996, 0.3048", "-40.0, -40", "1.0E10, 10000000000",
        "33.333333333333336, 33.3333333333", "0.000000001, 1E-9", "2.5, 2.5",
        "1.0E20, 1E+20", "-1.5E300, -1.5E+300", "123456789012.0, 123456789012", "0.000001, 0.000001",
        "999999999999.0, 999999999999", "999999999999999.0, 1000000000000000", "1.0E15, 1E+15", "4.9E-324, 4.94065645841E-324", "0.1, 0.1", "-0.5, -0.5"})
    void formateaSinCerosSobrantesYAbreviaLosExtremos(double numero, String esperado) {
        assertEquals(esperado, EntradaUtil.formatear(numero));
    }

    // ---------- lectura ----------

    @Test
    void leerTextoQuitaLosEspaciosDeLosExtremos() {
        assertEquals("monster", conTeclado("   monster  ").leerTexto("Usuario"));
        assertTrue(pantalla().contains("Usuario: "));
    }

    @Test
    void leerTextoAceptaUnaLineaVacia() {
        assertEquals("", conTeclado("").leerTexto("Usuario"));
    }

    @Test
    void leerTextoConservaTildesYEnie() {
        assertEquals("contraseña ñandú", conTeclado("contraseña ñandú").leerTexto("Clave"));
    }

    @Test
    void siLaEntradaSeCierraSeAvisaConUnaExcepcionPropia() {
        assertThrows(FinDeEntradaException.class, () -> conTeclado().leerTexto("Usuario"));
        assertThrows(FinDeEntradaException.class, () -> conTeclado("abc").leerDecimal("Valor"));
        assertThrows(FinDeEntradaException.class, () -> conTeclado("9").leerOpcion("Opción", 0, 3));
    }

    @Test
    void sinConsolaRealLaContrasenaSeLeeComoTexto() {
        assertEquals("clave", conTeclado("clave").leerPassword("Contraseña"));
    }

    @Test
    void leerDecimalInsisteHastaRecibirUnNumero() {
        EntradaUtil entrada = conTeclado("abc", "", "1.2.3", "12,5");

        assertEquals(12.5, entrada.leerDecimal("Valor"), 1e-12);
        assertEquals(3, pantalla().split("Valor no válido", -1).length - 1);
    }

    @Test
    void leerOpcionInsisteHastaRecibirUnaOpcionDelRango() {
        EntradaUtil entrada = conTeclado("5", "-1", "dos", "1.0", "", "99999999999999999999", "2");

        assertEquals(2, entrada.leerOpcion("Opción", 0, 3));
        assertEquals(6, pantalla().split("Opción no válida", -1).length - 1);
    }

    @Test
    void leerOpcionAceptaLosDosExtremosDelRango() {
        assertEquals(0, conTeclado("0").leerOpcion("Opción", 0, 3));
        assertEquals(3, conTeclado("3").leerOpcion("Opción", 0, 3));
    }

    // ---------- elegir ----------

    @Test
    void elegirDevuelveElElementoDeLaPosicionEscrita() {
        Optional<String> elegido = conTeclado("2").elegir("Categoría:", List.of("LONGITUD", "MASA"));

        assertEquals(Optional.of("MASA"), elegido);
        assertTrue(pantalla().contains("1. LONGITUD"));
        assertTrue(pantalla().contains("2. MASA"));
        assertTrue(pantalla().contains("0. Cancelar"));
    }

    @Test
    void elegirConCeroCancela() {
        assertEquals(Optional.empty(), conTeclado("0").elegir("Categoría:", List.of("LONGITUD")));
    }

    @Test
    void elegirNoAceptaUnaPosicionQueNoExiste() {
        assertEquals(Optional.of("LONGITUD"), conTeclado("2", "1").elegir("Categoría:", List.of("LONGITUD")));
    }

    @Test
    void elegirDeUnaListaVaciaSoloPermiteCancelar() {
        assertEquals(Optional.empty(), conTeclado("1", "0").elegir("Categoría:", List.of()));
    }
}
