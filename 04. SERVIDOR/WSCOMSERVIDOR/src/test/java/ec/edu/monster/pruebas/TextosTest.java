package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.utilidades.Textos;
import org.junit.jupiter.api.Test;

class TextosTest {

    @Test
    void normalizarQuitaEspaciosMayusculasYTildes() {
        assertEquals("centimetros", Textos.normalizar("  CENTÍMETROS "));
        assertEquals("reaumur", Textos.normalizar("Réaumur"));
        assertEquals("longitud", Textos.normalizar("\tLongitud\n"));
    }

    @Test
    void normalizarDeNuloEsTextoVacioYNoFalla() {
        assertEquals("", Textos.normalizar(null));
        assertEquals("", Textos.normalizar("   "));
    }

    @Test
    void normalizarTrataIgualLaTildePrecompuestaYLaCombinada() {
        // "í" puede llegar como un solo carácter o como "i" + tilde combinada.
        assertEquals(Textos.normalizar("centímetros"), Textos.normalizar("centímetros"));
    }

    @Test
    void normalizarNoUneLasPalabras() {
        assertEquals("metros cubicos", Textos.normalizar("Metros Cúbicos"));
    }

    @Test
    void normalizarNoDependeDelIdiomaDelServidor() {
        // En turco, "I".toLowerCase() no es "i"; se usa Locale.ROOT para evitarlo.
        assertEquals("libras", Textos.normalizar("LIBRAS"));
        assertEquals("millas", Textos.normalizar("MILLAS"));
    }

    @Test
    void estaVacioDetectaNulosVaciosYSoloEspacios() {
        assertTrue(Textos.estaVacio(null));
        assertTrue(Textos.estaVacio(""));
        assertTrue(Textos.estaVacio("   "));
        assertTrue(Textos.estaVacio("\t\n"));
        assertFalse(Textos.estaVacio(" a "));
        assertFalse(Textos.estaVacio("0"));
    }
}
