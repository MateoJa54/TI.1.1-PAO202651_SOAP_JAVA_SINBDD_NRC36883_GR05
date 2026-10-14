package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.modelos.Unidad;
import ec.edu.monster.servicios.conversores.ConversorLineal;
import ec.edu.monster.servicios.conversores.ConversorTemperatura;
import ec.edu.monster.servicios.conversores.EscalaTemperatura;
import ec.edu.monster.servicios.conversores.IConversor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Prueba las estrategias de conversión aisladas: sin catálogo, sin
 * validación y sin servicio web.
 */
class ConversoresTest {

    private static final double TOLERANCIA = 1e-9;

    private final IConversor lineal = new ConversorLineal();
    private final IConversor temperatura = new ConversorTemperatura();

    private static Unidad unidad(String nombre, double factor) {
        return new Unidad(nombre, nombre, factor);
    }

    private static Unidad escala(String nombre) {
        return new Unidad(nombre, nombre, 1.0);
    }

    // ---------- ConversorLineal ----------

    @Test
    void linealMultiplicaPorElFactorDeOrigenYDividePorElDeDestino() {
        assertEquals(2500.0, lineal.convertir(2.5, unidad("kg", 1.0), unidad("g", 0.001)), TOLERANCIA);
    }

    @Test
    void linealConLaMismaUnidadDevuelveElMismoValor() {
        Unidad pies = unidad("pies", 0.3048);

        assertEquals(7.25, lineal.convertir(7.25, pies, pies), TOLERANCIA);
    }

    @Test
    void linealConCeroDaCero() {
        assertEquals(0.0, lineal.convertir(0.0, unidad("mi", 1609.344), unidad("cm", 0.01)), TOLERANCIA);
    }

    @Test
    void linealConservaElSignoDeUnValorNegativo() {
        assertEquals(-200.0, lineal.convertir(-2.0, unidad("m", 1.0), unidad("cm", 0.01)), TOLERANCIA);
    }

    @Test
    void linealConValoresMuyPequenosNoPierdeElResultado() {
        assertEquals(1e-12, lineal.convertir(1e-9, unidad("g", 0.001), unidad("kg", 1.0)), 1e-21);
    }

    @Test
    void linealDesbordaAInfinitoYElServicioEsQuienLoRechaza() {
        // La estrategia solo calcula; ConversionServicio convierte el infinito en DATA-001.
        double resultado = lineal.convertir(Double.MAX_VALUE, unidad("mi", 1609.344), unidad("cm", 0.01));

        assertTrue(Double.isInfinite(resultado));
    }

    // ---------- ConversorTemperatura ----------

    @ParameterizedTest(name = "{0} {1} = {3} {2}")
    @CsvSource({
        // puntos fijos conocidos
        "-40, celsius, fahrenheit, -40",
        "0, celsius, fahrenheit, 32",
        "100, celsius, fahrenheit, 212",
        "37, celsius, fahrenheit, 98.6",
        // cero absoluto
        "0, kelvin, celsius, -273.15",
        "0, kelvin, fahrenheit, -459.67",
        "-273.15, celsius, kelvin, 0",
        // escalas poco comunes
        "33, newton, celsius, 100",
        "80, reaumur, celsius, 100",
        "0, newton, kelvin, 273.15",
        "0, reaumur, fahrenheit, 32",
        "33, newton, reaumur, 80",
        // valores negativos y decimales
        "-10.5, celsius, kelvin, 262.65",
        "-459.67, fahrenheit, kelvin, 0"
    })
    void temperaturaConvierteLosPuntosConocidos(
            double valor, String origen, String destino, double esperado) {
        assertEquals(esperado, temperatura.convertir(valor, escala(origen), escala(destino)), TOLERANCIA);
    }

    @ParameterizedTest
    @EnumSource(EscalaTemperatura.class)
    void temperaturaConLaMismaEscalaDevuelveElMismoValor(EscalaTemperatura escala) {
        Unidad unidad = escala(escala.name());

        assertEquals(25.5, temperatura.convertir(25.5, unidad, unidad), TOLERANCIA);
    }

    @ParameterizedTest
    @EnumSource(EscalaTemperatura.class)
    void cadaEscalaVaYVuelveDeCelsiusSinPerderElValor(EscalaTemperatura escala) {
        for (double celsius : new double[] {-273.15, -40, 0, 36.6, 100, 1_000_000}) {
            assertEquals(celsius, escala.aCelsius(escala.desdeCelsius(celsius)), 1e-6);
        }
    }

    @Test
    void temperaturaAceptaElNombreConMayusculasYTildes() {
        assertEquals(100.0,
                temperatura.convertir(80, escala("RÉAUMUR"), escala(" Celsius ")), TOLERANCIA);
    }

    @Test
    void temperaturaConUnaUnidadSinFormulaFallaConUnMensajeClaro() {
        // No puede ocurrir en producción (el servicio valida antes); protege de un catálogo mal armado.
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> temperatura.convertir(1, escala("rankine"), escala("celsius")));

        assertTrue(error.getMessage().contains("rankine"));
    }

    // ---------- EscalaTemperatura ----------

    @Test
    void deNombreNoEncuentraNulosVaciosNiDesconocidos() {
        assertTrue(EscalaTemperatura.deNombre(null).isEmpty());
        assertTrue(EscalaTemperatura.deNombre("").isEmpty());
        assertTrue(EscalaTemperatura.deNombre("   ").isEmpty());
        assertTrue(EscalaTemperatura.deNombre("rankine").isEmpty());
        assertTrue(EscalaTemperatura.deNombre("celsiu").isEmpty());
    }

    @Test
    void deNombreEncuentraLasCincoEscalasDelErs() {
        assertEquals(EscalaTemperatura.CELSIUS, EscalaTemperatura.deNombre("celsius").orElseThrow());
        assertEquals(EscalaTemperatura.FAHRENHEIT, EscalaTemperatura.deNombre("Fahrenheit").orElseThrow());
        assertEquals(EscalaTemperatura.KELVIN, EscalaTemperatura.deNombre("KELVIN").orElseThrow());
        assertEquals(EscalaTemperatura.NEWTON, EscalaTemperatura.deNombre(" newton").orElseThrow());
        assertEquals(EscalaTemperatura.REAUMUR, EscalaTemperatura.deNombre("réaumur").orElseThrow());
    }
}
