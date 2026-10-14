package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.seguridad.FiltroCors;
import org.junit.jupiter.api.Test;

class FiltroCorsTest {

    @Test
    void porDefectoPermiteCualquierPuertoDeLocalhost() {
        FiltroCors filtro = new FiltroCors();

        assertTrue(filtro.esOrigenPermitido("http://localhost:5173"));
        assertTrue(filtro.esOrigenPermitido("http://localhost:4200"));
        assertTrue(filtro.esOrigenPermitido("http://localhost"));
        assertTrue(filtro.esOrigenPermitido("http://127.0.0.1:3000"));
    }

    @Test
    void porDefectoRechazaOrigenesExternos() {
        FiltroCors filtro = new FiltroCors();

        assertFalse(filtro.esOrigenPermitido("http://sitio-externo.com"));
        assertFalse(filtro.esOrigenPermitido("https://localhost:5173"));
    }

    @Test
    void elComodinDePuertoNoSePuedeBurlarConUnDominioParecido() {
        FiltroCors filtro = new FiltroCors();

        assertFalse(filtro.esOrigenPermitido("http://localhost:80.malo.com"));
        assertFalse(filtro.esOrigenPermitido("http://localhost.malo.com"));
        assertFalse(filtro.esOrigenPermitido("http://localhost:"));
    }

    @Test
    void sinCabeceraOriginNoEsUnaPeticionDeNavegador() {
        FiltroCors filtro = new FiltroCors();

        assertFalse(filtro.esOrigenPermitido(null));
        assertFalse(filtro.esOrigenPermitido(" "));
    }

    @Test
    void unOrigenExactoSoloPermiteEse() {
        FiltroCors filtro = new FiltroCors("http://localhost:5173, https://conversor.monster.edu.ec");

        assertTrue(filtro.esOrigenPermitido("http://localhost:5173"));
        assertTrue(filtro.esOrigenPermitido("https://conversor.monster.edu.ec"));
        assertFalse(filtro.esOrigenPermitido("http://localhost:4200"));
    }

    @Test
    void elAsteriscoPermiteCualquierOrigen() {
        FiltroCors filtro = new FiltroCors("*");

        assertTrue(filtro.esOrigenPermitido("http://cualquiera.com"));
    }
}
