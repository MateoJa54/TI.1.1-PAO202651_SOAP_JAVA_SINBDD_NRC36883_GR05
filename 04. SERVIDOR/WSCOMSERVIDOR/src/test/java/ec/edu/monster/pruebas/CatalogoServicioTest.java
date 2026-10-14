package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.modelos.Unidad;
import ec.edu.monster.servicios.CatalogoServicio;
import java.util.List;
import org.junit.jupiter.api.Test;

class CatalogoServicioTest {

    private final CatalogoServicio catalogo = new CatalogoServicio();

    @Test
    void tieneLasTresCategoriasDelErs() {
        assertEquals(List.of("LONGITUD", "MASA", "TEMPERATURA"), catalogo.obtenerCategorias());
    }

    @Test
    void cadaCategoriaTieneLasCincoUnidadesDelErs() {
        assertEquals(
                List.of("centímetros", "metros", "pies", "yardas", "millas"),
                catalogo.obtenerUnidades("LONGITUD"));
        assertEquals(
                List.of("gramos", "kilogramos", "onzas", "libras", "quintales"),
                catalogo.obtenerUnidades("MASA"));
        assertEquals(
                List.of("celsius", "fahrenheit", "kelvin", "newton", "reaumur"),
                catalogo.obtenerUnidades("TEMPERATURA"));
    }

    @Test
    void lasCategoriasSeBuscanSinDistinguirMayusculas() {
        assertTrue(catalogo.existeCategoria("longitud"));
        assertTrue(catalogo.existeCategoria("  Masa "));
        assertFalse(catalogo.existeCategoria("VOLUMEN"));
        assertFalse(catalogo.existeCategoria(null));
    }

    @Test
    void unaCategoriaInexistenteNoTieneUnidades() {
        assertTrue(catalogo.obtenerUnidades("VOLUMEN").isEmpty());
    }

    @Test
    void lasUnidadesSeBuscanSinDistinguirMayusculasNiTildes() {
        Unidad encontrada = catalogo.buscarUnidad("LONGITUD", "CENTIMETROS").orElseThrow();

        assertEquals("centímetros", encontrada.getNombre());
        assertEquals(0.01, encontrada.getFactorBase(), 1e-12);
    }

    @Test
    void unaUnidadSoloSeEncuentraEnSuCategoria() {
        assertTrue(catalogo.buscarUnidad("MASA", "metros").isEmpty());
        assertTrue(catalogo.buscarUnidad("LONGITUD", "xyz").isEmpty());
        assertTrue(catalogo.buscarUnidad("VOLUMEN", "metros").isEmpty());
    }

    @Test
    void sabeEnQueCategoriaEstaUnaUnidad() {
        assertEquals("TEMPERATURA", catalogo.buscarCategoriaDeUnidad("Celsius").orElseThrow().getNombre());
        assertEquals("MASA", catalogo.buscarCategoriaDeUnidad("libras").orElseThrow().getNombre());
        assertTrue(catalogo.buscarCategoriaDeUnidad("xyz").isEmpty());
        assertTrue(catalogo.buscarCategoriaDeUnidad("").isEmpty());
    }
}
