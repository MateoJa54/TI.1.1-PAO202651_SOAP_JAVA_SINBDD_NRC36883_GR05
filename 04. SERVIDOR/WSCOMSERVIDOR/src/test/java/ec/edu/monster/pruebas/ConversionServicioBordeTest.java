package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.modelos.CodigoRespuesta;
import ec.edu.monster.modelos.RespuestaConversion;
import ec.edu.monster.modelos.SolicitudConversion;
import ec.edu.monster.servicios.CatalogoServicio;
import ec.edu.monster.servicios.ConversionServicio;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Casos borde de la conversión: límites numéricos, textos mal escritos,
 * prioridad entre errores y propiedades que deben cumplirse para todas las
 * unidades del catálogo.
 */
class ConversionServicioBordeTest {

    private final CatalogoServicio catalogo = new CatalogoServicio();
    private final ConversionServicio servicio = new ConversionServicio(catalogo);

    private RespuestaConversion convertir(String categoria, String origen, String destino, Double valor) {
        return servicio.convertir(new SolicitudConversion(categoria, origen, destino, valor));
    }

    private CodigoRespuesta validar(String categoria, String origen, String destino, Double valor) {
        return servicio.validar(new SolicitudConversion(categoria, origen, destino, valor));
    }

    // ---------- límites numéricos ----------

    @Test
    void ceroEsUnValorValido() {
        RespuestaConversion r = convertir("LONGITUD", "millas", "metros", 0.0);

        assertEquals("OK", r.getEstado());
        assertEquals(0.0, r.getResultado(), 0.0);
    }

    @Test
    void ceroNegativoNoRompeNada() {
        RespuestaConversion r = convertir("MASA", "libras", "gramos", -0.0);

        assertEquals("OK", r.getEstado());
        assertEquals(0.0, Math.abs(r.getResultado()), 0.0);
    }

    @Test
    void losValoresNegativosSeConviertenConservandoElSigno() {
        // El ERS no restringe el signo: -5 metros es un desplazamiento válido.
        assertEquals(-500.0, convertir("LONGITUD", "metros", "centímetros", -5.0).getResultado(), 1e-9);
        assertEquals(-40.0, convertir("TEMPERATURA", "celsius", "fahrenheit", -40.0).getResultado(), 1e-9);
    }

    @Test
    void elValorMasPequenoPosibleNoEsUnError() {
        RespuestaConversion r = convertir("LONGITUD", "metros", "metros", Double.MIN_VALUE);

        assertEquals("OK", r.getEstado());
        assertEquals(Double.MIN_VALUE, r.getResultado(), 0.0);
    }

    @Test
    void elValorMasGrandeSeAceptaSiElResultadoCabe() {
        RespuestaConversion r = convertir("LONGITUD", "centímetros", "millas", Double.MAX_VALUE);

        assertEquals("OK", r.getEstado());
        assertTrue(Double.isFinite(r.getResultado()));
    }

    @Test
    void siElResultadoNoCabeEnUnDoubleEsData001() {
        RespuestaConversion positivo = convertir("LONGITUD", "millas", "centímetros", Double.MAX_VALUE);
        RespuestaConversion negativo = convertir("LONGITUD", "millas", "centímetros", -Double.MAX_VALUE);

        assertEquals("DATA-001", positivo.getCodigo());
        assertEquals("DATA-001", negativo.getCodigo());
        assertNull(positivo.getResultado());
    }

    @ParameterizedTest
    @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void losValoresNoFinitosSonData001EnTodasLasCategorias(double valor) {
        assertEquals(CodigoRespuesta.DATA_001, validar("LONGITUD", "metros", "pies", valor));
        assertEquals(CodigoRespuesta.DATA_001, validar("MASA", "gramos", "libras", valor));
        assertEquals(CodigoRespuesta.DATA_001, validar("TEMPERATURA", "celsius", "kelvin", valor));
    }

    @Test
    void losDecimalesSeConservan() {
        assertEquals(0.3048, convertir("LONGITUD", "pies", "metros", 1.0).getResultado(), 1e-12);
        assertEquals(453.59237, convertir("MASA", "libras", "gramos", 1.0).getResultado(), 1e-9);
    }

    // ---------- textos mal escritos ----------

    @ParameterizedTest(name = "[{0}] [{1}] [{2}]")
    @CsvSource(ignoreLeadingAndTrailingWhitespace = false, value = {
        "longitud,metros,pies",
        "LONGITUD,METROS,PIES",
        "Longitud,Metros,Pies",
        " LONGITUD , metros , pies ",
        "LONGITUD,centimetros,metros",
        "LONGITUD,CENTÍMETROS,metros"
    })
    void aceptaMayusculasEspaciosYFaltaDeTildes(String categoria, String origen, String destino) {
        assertEquals(CodigoRespuesta.OK_200, validar(categoria, origen, destino, 1.0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"metro", "metross", "m", "met ros", "metros;", "pies' OR '1'='1", "<metros>"})
    void unNombreParecidoPeroNoIgualNoEsUnaUnidad(String unidadMalEscrita) {
        assertEquals(CodigoRespuesta.UNIT_001, validar("LONGITUD", unidadMalEscrita, "pies", 1.0));
        assertEquals(CodigoRespuesta.UNIT_002, validar("LONGITUD", "pies", unidadMalEscrita, 1.0));
    }

    @Test
    void noSeAceptaElSimboloEnLugarDelNombre() {
        // El contrato trabaja con nombres ("metros"), no con símbolos ("m").
        assertEquals(CodigoRespuesta.UNIT_001, validar("MASA", "kg", "gramos", 1.0));
    }

    @Test
    void unTextoMuyLargoNoRompeLaValidacion() {
        String largo = "x".repeat(100_000);

        assertEquals(CodigoRespuesta.DATA_001, validar(largo, "metros", "pies", 1.0));
        assertEquals(CodigoRespuesta.UNIT_001, validar("LONGITUD", largo, "pies", 1.0));
    }

    @Test
    void laRespuestaExitosaSiempreUsaLosNombresOficiales() {
        RespuestaConversion r = convertir(" temperatura ", "RÉAUMUR", "Celsius", 80.0);

        assertEquals("reaumur", r.getUnidadOrigen());
        assertEquals("celsius", r.getUnidadDestino());
        assertEquals(100.0, r.getResultado(), 1e-9);
    }

    // ---------- prioridad entre errores (orden de las fichas ECUD) ----------

    @Test
    void losCamposVaciosSeInformanAntesQueLaCategoriaOLasUnidades() {
        assertEquals(CodigoRespuesta.DATA_001, validar("VOLUMEN", "xyz", "", 1.0));
        assertEquals(CodigoRespuesta.DATA_001, validar("LONGITUD", "xyz", "abc", null));
    }

    @Test
    void laCategoriaInexistenteSeInformaAntesQueLasUnidades() {
        assertEquals(CodigoRespuesta.DATA_001, validar("VOLUMEN", "xyz", "abc", 1.0));
    }

    @Test
    void laUnidadDeOrigenSeRevisaAntesQueLaDeDestino() {
        // Origen de otra categoría (DATA-001) gana a destino inexistente (UNIT-002).
        assertEquals(CodigoRespuesta.DATA_001, validar("LONGITUD", "celsius", "xyz", 1.0));
        // Origen inexistente (UNIT-001) gana a destino de otra categoría (DATA-001).
        assertEquals(CodigoRespuesta.UNIT_001, validar("LONGITUD", "xyz", "celsius", 1.0));
    }

    @Test
    void lasDosUnidadesDeOtraCategoriaEsData001() {
        assertEquals(CodigoRespuesta.DATA_001, validar("LONGITUD", "gramos", "libras", 1.0));
    }

    @Test
    void todoErrorDeValidacionLlegaSinResultadoNiUnidades() {
        for (RespuestaConversion r : new RespuestaConversion[] {
            convertir(null, null, null, null),
            convertir("VOLUMEN", "litros", "galones", 1.0),
            convertir("LONGITUD", "xyz", "metros", 1.0),
            convertir("LONGITUD", "metros", "xyz", 1.0),
            convertir("LONGITUD", "metros", "kelvin", 1.0)}) {
            assertEquals("ERROR", r.getEstado());
            assertNotNull(r.getMensaje());
            assertNull(r.getResultado());
            assertNull(r.getUnidadOrigen());
            assertNull(r.getUnidadDestino());
        }
    }

    // ---------- propiedades para todo el catálogo ----------

    @Test
    void todasLasCombinacionesDeUnidadesDeCadaCategoriaConvierten() {
        int combinaciones = 0;
        for (String categoria : catalogo.obtenerCategorias()) {
            for (String origen : catalogo.obtenerUnidades(categoria)) {
                for (String destino : catalogo.obtenerUnidades(categoria)) {
                    RespuestaConversion r = convertir(categoria, origen, destino, 12.5);
                    assertEquals("OK", r.getEstado(), categoria + ": " + origen + " -> " + destino);
                    assertTrue(Double.isFinite(r.getResultado()));
                    combinaciones++;
                }
            }
        }
        assertEquals(75, combinaciones);
    }

    @Test
    void convertirALaMismaUnidadDevuelveElMismoValor() {
        for (String categoria : catalogo.obtenerCategorias()) {
            for (String unidad : catalogo.obtenerUnidades(categoria)) {
                assertEquals(42.0, convertir(categoria, unidad, unidad, 42.0).getResultado(), 1e-9,
                        categoria + ": " + unidad);
            }
        }
    }

    @Test
    void irYVolverEntreDosUnidadesRecuperaElValorOriginal() {
        double original = 123.456;
        for (String categoria : catalogo.obtenerCategorias()) {
            for (String a : catalogo.obtenerUnidades(categoria)) {
                for (String b : catalogo.obtenerUnidades(categoria)) {
                    double ida = convertir(categoria, a, b, original).getResultado();
                    double vuelta = convertir(categoria, b, a, ida).getResultado();
                    assertEquals(original, vuelta, 1e-9, categoria + ": " + a + " <-> " + b);
                }
            }
        }
    }

    @Test
    void ningunaUnidadDeUnaCategoriaSirveEnOtra() {
        for (String categoria : catalogo.obtenerCategorias()) {
            for (String otra : catalogo.obtenerCategorias()) {
                if (categoria.equals(otra)) {
                    continue;
                }
                for (String unidadAjena : catalogo.obtenerUnidades(otra)) {
                    String propia = catalogo.obtenerUnidades(categoria).get(0);
                    assertEquals(CodigoRespuesta.DATA_001, validar(categoria, unidadAjena, propia, 1.0));
                    assertEquals(CodigoRespuesta.DATA_001, validar(categoria, propia, unidadAjena, 1.0));
                }
            }
        }
    }

    // ---------- configuración inválida ----------

    @Test
    void elCatalogoEsObligatorio() {
        assertThrows(NullPointerException.class, () -> new ConversionServicio(null));
    }

    @Test
    void unMapaDeConversoresVacioFallaAlArrancarYNoEnLaPrimeraPeticion() {
        assertThrows(IllegalStateException.class, () -> new ConversionServicio(catalogo, Map.of()));
    }

    @Test
    void elServicioNoGuardaEstadoEntreLlamadas() {
        convertir("LONGITUD", "xyz", "metros", 1.0);

        assertEquals("OK", convertir("LONGITUD", "pies", "metros", 1.0).getEstado());
    }
}
