package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.modelos.CodigoRespuesta;
import ec.edu.monster.modelos.RespuestaConversion;
import ec.edu.monster.modelos.SolicitudConversion;
import ec.edu.monster.servicios.CatalogoServicio;
import ec.edu.monster.servicios.ConversionServicio;
import ec.edu.monster.servicios.conversores.EscalaTemperatura;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Casos borde de precisión y de física de ConversorLineal, ConversorTemperatura
 * y EscalaTemperatura. Las pruebas existentes comparan con tolerancia; aquí se
 * compara exacto donde el usuario espera exactitud.
 *
 * La regla del cero absoluto vive en la estrategia de temperatura (IConversor.admite).
 */
class ConversoresPrecisionBordeTest {

    private final CatalogoServicio catalogo = new CatalogoServicio();
    private final ConversionServicio servicio = new ConversionServicio(catalogo);

    private RespuestaConversion convertir(String categoria, String origen, String destino, double valor) {
        return servicio.convertir(new SolicitudConversion(categoria, origen, destino, valor));
    }

    // ---------- conversiones lineales exactas ----------

    @ParameterizedTest
    @CsvSource({
        "LONGITUD, metros, centímetros, 1, 100",
        "LONGITUD, millas, metros, 1, 1609.344",
        "LONGITUD, yardas, pies, 1, 3",
        "MASA, kilogramos, gramos, 1, 1000",
        "MASA, quintales, libras, 1, 100",
        "MASA, libras, onzas, 1, 16"
    })
    void lasEquivalenciasDefinidasPorLeySonExactasOCasiExactas(
            String categoria, String origen, String destino, double valor, double esperado) {
        RespuestaConversion respuesta = convertir(categoria, origen, destino, valor);

        assertEquals("200", respuesta.getCodigo());
        // Un ulp de diferencia como máximo: el factor ya está definido exacto en el catálogo.
        assertEquals(esperado, respuesta.getResultado(), Math.ulp(esperado));
    }

    @Test
    void elValorSubnormalMasPequenoSeConvierteSinError() {
        RespuestaConversion respuesta = convertir("LONGITUD", "metros", "millas", Double.MIN_VALUE);

        // El resultado se redondea a cero (subdesbordamiento); no es un error de datos.
        assertEquals("200", respuesta.getCodigo());
        assertEquals(0.0, respuesta.getResultado());
    }

    @Test
    void ceroNegativoSeDevuelveConSigno() {
        // Comportamiento actual: -0.0 viaja como "-0.0". Se fija aquí para detectar cambios.
        assertEquals(-0.0, convertir("LONGITUD", "metros", "pies", -0.0).getResultado());
    }

    // ---------- temperatura ----------

    @ParameterizedTest
    @CsvSource({
        "celsius, fahrenheit, -40, -40",
        "celsius, kelvin, -273.15, 0",
        "fahrenheit, celsius, 212, 100",
        "reaumur, celsius, 80, 100",
        "newton, celsius, 33, 100"
    })
    void losPuntosFijosDeTemperaturaSonExactos(String origen, String destino, double valor, double esperado) {
        assertEquals(esperado, convertir("TEMPERATURA", origen, destino, valor).getResultado(), 1e-12);
    }

    @ParameterizedTest
    @EnumSource(EscalaTemperatura.class)
    void cadaEscalaConvierteElCeroAbsolutoAlCeroAbsoluto(EscalaTemperatura escala) {
        double ceroAbsolutoEnEscala = escala.desdeCelsius(-273.15);

        assertEquals(0.0, EscalaTemperatura.KELVIN.desdeCelsius(escala.aCelsius(ceroAbsolutoEnEscala)), 1e-9);
    }

    @Test
    void unaTemperaturaBajoElCeroAbsolutoEsData001() {
        RespuestaConversion respuesta = convertir("TEMPERATURA", "kelvin", "celsius", -5.0);

        assertEquals(CodigoRespuesta.DATA_001.getCodigo(), respuesta.getCodigo());
        assertEquals(CodigoRespuesta.DATA_001.getMensaje(), respuesta.getMensaje());
    }

    @Test
    void unaTemperaturaMuyBajoElCeroAbsolutoEnCelsiusEsData001() {
        assertEquals(CodigoRespuesta.DATA_001.getCodigo(),
                convertir("TEMPERATURA", "celsius", "kelvin", -500.0).getCodigo());
    }

    @ParameterizedTest
    @EnumSource(EscalaTemperatura.class)
    void elCeroAbsolutoExactoSeAceptaEnCadaEscala(EscalaTemperatura escala) {
        double cero = escala.desdeCelsius(-273.15);
        String unidad = nombreDeUnidad(escala);

        RespuestaConversion respuesta = convertir("TEMPERATURA", unidad, "kelvin", cero);

        assertEquals("200", respuesta.getCodigo(), unidad + " " + cero);
        assertEquals(0.0, respuesta.getResultado(), 1e-9);
    }

    @ParameterizedTest
    @EnumSource(EscalaTemperatura.class)
    void unPocoBajoElCeroAbsolutoSeRechazaEnCadaEscala(EscalaTemperatura escala) {
        double bajoElCero = escala.desdeCelsius(-273.15 - 0.001);

        assertEquals(CodigoRespuesta.DATA_001.getCodigo(),
                convertir("TEMPERATURA", nombreDeUnidad(escala), "kelvin", bajoElCero).getCodigo(),
                escala.name());
    }

    @Test
    void laRegionDelCeroAbsolutoSoloAplicaALaUnidadDeOrigen() {
        // El destino nunca puede quedar bajo el cero absoluto si el origen no lo está.
        assertEquals("200", convertir("TEMPERATURA", "kelvin", "celsius", 0.0).getCodigo());
    }

    @Test
    void unaUnidadInexistenteSeRechazaAntesQueElCeroAbsoluto() {
        // El orden de reglas es: datos, categoría, unidades y al final el valor posible.
        assertEquals(CodigoRespuesta.UNIT_001.getCodigo(),
                convertir("TEMPERATURA", "rankine", "kelvin", -500.0).getCodigo());
    }

    @Test
    void validarYConvertirDanElMismoCodigoBajoElCeroAbsoluto() {
        SolicitudConversion solicitud = new SolicitudConversion("TEMPERATURA", "kelvin", "celsius", -1.0);

        assertEquals(CodigoRespuesta.DATA_001, servicio.validar(solicitud));
        assertEquals(CodigoRespuesta.DATA_001.getCodigo(), servicio.convertir(solicitud).getCodigo());
    }

    @Test
    void laLongitudYLaMasaNegativasSeAceptan() {
        // Decisión de diseño: solo la temperatura tiene un límite físico (el cero absoluto).
        RespuestaConversion respuesta = convertir("MASA", "kilogramos", "gramos", -3.0);

        assertEquals("200", respuesta.getCodigo());
        assertTrue(respuesta.getResultado() < 0);
    }

    private static String nombreDeUnidad(EscalaTemperatura escala) {
        return escala.name().toLowerCase();
    }
}
