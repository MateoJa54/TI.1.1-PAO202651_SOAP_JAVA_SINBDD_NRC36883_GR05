package ec.edu.monster.pruebas;

import ec.edu.monster.controlador.ConversorControlador;
import org.junit.Test;
import static org.junit.Assert.assertThrows;

public class ConversorControladorTest {
    @Test public void conversionRequiresAnAuthenticatedSession() {
        ConversorControlador controlador = new ConversorControlador("http://127.0.0.1:1/no-usado");
        assertThrows(IllegalStateException.class,
                () -> controlador.convertir("MASA", "kilogramos", "gramos", 2));
    }

    @Test public void closingSessionRemovesConversionAuthorization() {
        ConversorControlador controlador = new ConversorControlador("http://127.0.0.1:1/no-usado");
        controlador.cerrarSesion();
        assertThrows(IllegalStateException.class,
                () -> controlador.convertir("MASA", "kilogramos", "gramos", 2));
    }
}
