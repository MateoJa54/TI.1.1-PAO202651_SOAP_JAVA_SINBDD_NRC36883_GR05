package ec.edu.monster.pruebas;

import ec.edu.monster.modelo.Conversion;
import ec.edu.monster.modelo.Sesion;
import org.junit.Test;
import static org.junit.Assert.*;

public class ModeloConversorTest {
    @Test public void conversionPreservesItsCategoryUnitsAndValue() {
        Conversion conversion = new Conversion("LONGITUD", "metros", "yardas", 69.5);
        assertEquals("LONGITUD", conversion.getCategoria());
        assertEquals("metros", conversion.getUnidadOrigen());
        assertEquals("yardas", conversion.getUnidadDestino());
        assertEquals(69.5, conversion.getValor(), 0.000001);
    }

    @Test public void sessionPreservesTheServiceToken() {
        assertEquals("token-123", new Sesion("token-123").getToken());
    }
}
