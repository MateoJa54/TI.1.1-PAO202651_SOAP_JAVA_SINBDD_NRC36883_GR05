package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.modelos.CodigoRespuesta;
import ec.edu.monster.modelos.RespuestaAutenticacion;
import ec.edu.monster.modelos.RespuestaCatalogo;
import ec.edu.monster.modelos.RespuestaConversion;
import ec.edu.monster.modelos.RespuestaUnidades;
import ec.edu.monster.servicios.FabricaRespuestas;
import java.util.List;
import org.junit.jupiter.api.Test;

class FabricaRespuestasTest {

    @Test
    void conversionExitosaLlevaElMensajeDeLaFichaCu04() {
        RespuestaConversion r = FabricaRespuestas.conversionExitosa(3218.688, "millas", "metros");

        assertEquals("OK", r.getEstado());
        assertEquals("200", r.getCodigo());
        assertEquals("Conversión realizada correctamente", r.getMensaje());
        assertEquals(3218.688, r.getResultado(), 1e-9);
        assertEquals("millas", r.getUnidadOrigen());
        assertEquals("metros", r.getUnidadDestino());
    }

    @Test
    void conversionFallidaNoLlevaResultadoNiUnidades() {
        RespuestaConversion r = FabricaRespuestas.conversionFallida(CodigoRespuesta.UNIT_001);

        assertEquals("ERROR", r.getEstado());
        assertEquals("UNIT-001", r.getCodigo());
        assertEquals("Unidad de origen no soportada", r.getMensaje());
        assertNull(r.getResultado());
        assertNull(r.getUnidadOrigen());
        assertNull(r.getUnidadDestino());
    }

    @Test
    void autenticacionExitosaLlevaTokenYExpiracion() {
        RespuestaAutenticacion r = FabricaRespuestas.autenticacionExitosa("abc", "2026-10-14T17:30:00Z");

        assertEquals("OK", r.getEstado());
        assertEquals("abc", r.getToken());
        assertEquals("2026-10-14T17:30:00Z", r.getExpiracion());
    }

    @Test
    void autenticacionFallidaNoLlevaToken() {
        RespuestaAutenticacion r = FabricaRespuestas.autenticacionFallida(CodigoRespuesta.AUTH_003);

        assertEquals("AUTH-003", r.getCodigo());
        assertNull(r.getToken());
    }

    @Test
    void catalogoYUnidadesExitosos() {
        RespuestaCatalogo catalogo = FabricaRespuestas.catalogoExitoso(List.of("LONGITUD", "MASA"));
        RespuestaUnidades unidades = FabricaRespuestas.unidadesExitosas("MASA", List.of("gramos"));

        assertEquals(List.of("LONGITUD", "MASA"), catalogo.getCategorias());
        assertEquals("MASA", unidades.getCategoria());
        assertEquals(List.of("gramos"), unidades.getUnidades());
    }

    @Test
    void catalogoYUnidadesFallidosTraenListasVaciasNoNulas() {
        assertTrue(FabricaRespuestas.catalogoFallido(CodigoRespuesta.AUTH_001).getCategorias().isEmpty());
        assertTrue(FabricaRespuestas.unidadesFallidas(CodigoRespuesta.DATA_001).getUnidades().isEmpty());
    }

    @Test
    void unaRespuestaFallidaNoAceptaElCodigoDeExito() {
        assertThrows(IllegalArgumentException.class,
                () -> FabricaRespuestas.conversionFallida(CodigoRespuesta.OK_200));
        assertThrows(NullPointerException.class,
                () -> FabricaRespuestas.autenticacionFallida(null));
    }
}
