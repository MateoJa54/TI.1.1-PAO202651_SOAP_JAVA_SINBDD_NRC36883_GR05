package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.modelos.CodigoRespuesta;
import ec.edu.monster.modelos.SolicitudConversion;
import ec.edu.monster.servicios.CatalogoServicio;
import ec.edu.monster.servicios.ConversionServicio;
import ec.edu.monster.utilidades.Textos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Casos borde de Textos y de la búsqueda en el catálogo con caracteres
 * Unicode que un cliente web o móvil puede enviar sin querer.
 */
class TextosYCatalogoBordeTest {

    private final CatalogoServicio catalogo = new CatalogoServicio();
    private final ConversionServicio servicio = new ConversionServicio(catalogo);

    private CodigoRespuesta validar(String categoria, String origen, String destino) {
        return servicio.validar(new SolicitudConversion(categoria, origen, destino, 1.0));
    }

    @Test
    void tabuladoresYSaltosDeLineaAlrededorSeIgnoran() {
        assertEquals(CodigoRespuesta.OK_200, validar("\tLONGITUD\n", "\r\nmetros\t", " pies "));
    }

    @Test
    void laMayusculaTurcaConPuntoNoRompeLaBusqueda() {
        // "İ" en NFD es I + punto combinado; el punto se descarta como tilde.
        assertEquals("i", Textos.normalizar("İ"));
    }

    @Test
    void unaTildeSueltaNoEsUnaUnidad() {
        assertEquals(CodigoRespuesta.UNIT_001, validar("LONGITUD", "́", "pies"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"metro", "metross", "m etros", "°kelvin", "kelvins"})
    void variantesCercanasNoSeAceptanComoUnidad(String unidad) {
        CodigoRespuesta codigo = validar("LONGITUD", unidad, "pies");

        assertEquals(CodigoRespuesta.UNIT_001, codigo, unidad);
    }

    @Test
    void unSimboloDeOtraCategoriaEsUnidadNoSoportadaYNoDatoInconsistente() {
        // Solo se buscan nombres: "g" no existe en ninguna categoría, por eso es UNIT y no DATA.
        assertEquals(CodigoRespuesta.UNIT_001, validar("LONGITUD", "g", "pies"));
    }

    @Test
    void unNombreConTildeDistintaTambienSeAcepta() {
        assertEquals(CodigoRespuesta.OK_200, validar("TEMPERATURA", "Réaumur", "celsius"));
    }

    @Test
    void unaCategoriaEnLetrasDeAnchoCompletoNoExiste() {
        assertEquals(CodigoRespuesta.DATA_001, validar("ＬＯＮＧＩＴＵＤ", "metros", "pies"));
    }

    @Test
    void unaUnidadNulaConCategoriaValidaEsData001() {
        assertEquals(CodigoRespuesta.DATA_001, validar("LONGITUD", null, "pies"));
    }

    @Test
    void estaVacioConsideraVacioElEspacioDeAnchoCompletoYNoElCaracterInvisible() {
        // isBlank reconoce U+3000 como espacio; el espacio de ancho cero no lo es.
        assertTrue(Textos.estaVacio("　"));
        assertFalse(Textos.estaVacio("​"));
    }

    @Test
    void elEspacioDuroAlrededorDeUnaUnidadSeIgnora() {
        // U+00A0: el espacio duro que llega al copiar y pegar desde una página web.
        assertEquals(CodigoRespuesta.OK_200, validar("LONGITUD", "metros ", "pies"));
        assertEquals(CodigoRespuesta.OK_200, validar(" LONGITUD ", "metros", " pies"));
    }

}
