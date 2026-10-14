package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ec.edu.monster.modelos.Categoria;
import ec.edu.monster.modelos.CodigoRespuesta;
import ec.edu.monster.modelos.RespuestaConversion;
import ec.edu.monster.modelos.SolicitudConversion;
import ec.edu.monster.modelos.Unidad;
import ec.edu.monster.servicios.CatalogoServicio;
import ec.edu.monster.servicios.ConversionServicio;
import ec.edu.monster.servicios.conversores.IConversor;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ConversionServicioTest {

    private final CatalogoServicio catalogo = new CatalogoServicio();
    private final ConversionServicio servicio = new ConversionServicio(catalogo);

    private static SolicitudConversion solicitud(
            String categoria, String origen, String destino, double valor) {
        return new SolicitudConversion(categoria, origen, destino, valor);
    }

    // ---------- cálculo (valores de las fórmulas del ERS RQF-005) ----------

    @ParameterizedTest(name = "{3} {1} -> {2} = {4}")
    @CsvSource({
        "LONGITUD,millas,metros,1,1609.344",
        "LONGITUD,pies,centímetros,1,30.48",
        "LONGITUD,yardas,pies,1,3.0",
        "LONGITUD,metros,metros,5,5.0",
        "MASA,libras,onzas,1,16.0",
        "MASA,quintales,libras,1,100.0",
        "MASA,kilogramos,gramos,2.5,2500.0",
        "TEMPERATURA,celsius,fahrenheit,100,212.0",
        "TEMPERATURA,celsius,newton,100,33.0",
        "TEMPERATURA,celsius,reaumur,100,80.0",
        "TEMPERATURA,celsius,kelvin,0,273.15",
        "TEMPERATURA,fahrenheit,kelvin,212,373.15",
        "TEMPERATURA,kelvin,celsius,273.15,0.0",
        "TEMPERATURA,reaumur,fahrenheit,80,212.0"
    })
    void convierteSegunLasFormulasDelErs(
            String categoria, String origen, String destino, double valor, double esperado) {
        RespuestaConversion r = servicio.convertir(solicitud(categoria, origen, destino, valor));

        assertEquals("OK", r.getEstado());
        assertEquals("200", r.getCodigo());
        assertEquals(esperado, r.getResultado(), 1e-9);
        assertEquals(origen, r.getUnidadOrigen());
        assertEquals(destino, r.getUnidadDestino());
    }

    @Test
    void laRespuestaUsaLosNombresDelCatalogoAunqueElUsuarioEscribaSinTilde() {
        RespuestaConversion r = servicio.convertir(solicitud("longitud", "PIES", "CENTIMETROS", 1));

        assertEquals("OK", r.getEstado());
        assertEquals("pies", r.getUnidadOrigen());
        assertEquals("centímetros", r.getUnidadDestino());
    }

    // ---------- validación: orden y códigos de las fichas ECUD ----------

    @Test
    void camposNulosOVaciosEsData001() {
        assertEquals(CodigoRespuesta.DATA_001, servicio.validar(null));
        assertEquals(CodigoRespuesta.DATA_001, servicio.validar(solicitud("", "metros", "pies", 1)));
        assertEquals(CodigoRespuesta.DATA_001, servicio.validar(solicitud("LONGITUD", null, "pies", 1)));
        assertEquals(CodigoRespuesta.DATA_001, servicio.validar(solicitud("LONGITUD", "metros", " ", 1)));
    }

    @Test
    void valorAusenteEsData001() {
        // Un cliente que omite <valor> o envía texto no numérico llega aquí como null.
        assertEquals(CodigoRespuesta.DATA_001,
                servicio.validar(new SolicitudConversion("LONGITUD", "metros", "pies", null)));
    }

    @Test
    void valorNoFinitoEsData001() {
        assertEquals(CodigoRespuesta.DATA_001,
                servicio.validar(solicitud("LONGITUD", "metros", "pies", Double.NaN)));
        assertEquals(CodigoRespuesta.DATA_001,
                servicio.validar(solicitud("LONGITUD", "metros", "pies", Double.POSITIVE_INFINITY)));
    }

    @Test
    void categoriaInexistenteEsData001() {
        assertEquals(CodigoRespuesta.DATA_001,
                servicio.validar(solicitud("VOLUMEN", "metros", "pies", 1)));
    }

    @Test
    void unidadDeOrigenInexistenteEsUnit001() {
        assertEquals(CodigoRespuesta.UNIT_001,
                servicio.validar(solicitud("LONGITUD", "xyz", "metros", 1)));
    }

    @Test
    void unidadDeDestinoInexistenteEsUnit002() {
        assertEquals(CodigoRespuesta.UNIT_002,
                servicio.validar(solicitud("LONGITUD", "metros", "xyz", 1)));
    }

    @Test
    void siFallanAmbasUnidadesSeInformaLaDeOrigen() {
        assertEquals(CodigoRespuesta.UNIT_001,
                servicio.validar(solicitud("LONGITUD", "xyz", "abc", 1)));
    }

    @Test
    void unaUnidadDeOtraCategoriaEsData001() {
        // "celsius" existe en el sistema, pero no es de LONGITUD (ficha CU-06, FA-04).
        assertEquals(CodigoRespuesta.DATA_001,
                servicio.validar(solicitud("LONGITUD", "metros", "celsius", 1)));
        assertEquals(CodigoRespuesta.DATA_001,
                servicio.validar(solicitud("MASA", "metros", "gramos", 1)));
    }

    @Test
    void unaSolicitudValidaDaOk200() {
        assertEquals(CodigoRespuesta.OK_200,
                servicio.validar(solicitud("MASA", "libras", "kilogramos", 10)));
    }

    // ---------- convertir() ante errores ----------

    @Test
    void siLaValidacionFallaNoSeCalculaNada() {
        RespuestaConversion r = servicio.convertir(solicitud("LONGITUD", "metros", "celsius", 1));

        assertEquals("ERROR", r.getEstado());
        assertEquals("DATA-001", r.getCodigo());
        assertNull(r.getResultado());
    }

    @Test
    void unDesbordamientoNoSeEnviaComoInfinito() {
        RespuestaConversion r = servicio.convertir(solicitud("LONGITUD", "millas", "centímetros", 1e308));

        assertEquals("ERROR", r.getEstado());
        assertEquals("DATA-001", r.getCodigo());
        assertNull(r.getResultado());
    }

    // ---------- configuración ----------

    @Test
    void siFaltaUnConversorParaUnaCategoriaFallaAlArrancar() {
        assertThrows(IllegalStateException.class,
                () -> new ConversionServicio(catalogo, Map.of(CatalogoServicio.LONGITUD, conversorFalso())));
    }

    @Test
    void sePuedeAgregarUnaCategoriaNuevaSinTocarElResto() {
        // La "fase 2" del ERS: una categoría nueva es datos + una estrategia.
        Categoria volumen = new Categoria("VOLUMEN", List.of(
                new Unidad("litros", "l", 1.0), new Unidad("mililitros", "ml", 0.001)));
        CatalogoServicio ampliado = new CatalogoServicio(List.of(volumen));
        ConversionServicio conVolumen = new ConversionServicio(
                ampliado, Map.of("VOLUMEN", new ec.edu.monster.servicios.conversores.ConversorLineal()));

        RespuestaConversion r = conVolumen.convertir(solicitud("VOLUMEN", "litros", "mililitros", 2));

        assertEquals(2000.0, r.getResultado(), 1e-9);
    }

    private static IConversor conversorFalso() {
        return (valor, origen, destino) -> valor;
    }
}
