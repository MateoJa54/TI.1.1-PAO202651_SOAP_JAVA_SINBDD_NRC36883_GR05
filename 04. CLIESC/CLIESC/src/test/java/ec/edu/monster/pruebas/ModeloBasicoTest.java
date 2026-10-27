package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.modelo.RespuestaAutenticacion;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.util.NumeroUtil;
import ec.edu.monster.util.RespuestaUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ModeloBasicoTest {

    @Test
    void testSesionVacia() {
        SesionModelo s = new SesionModelo();
        assertNull(s.getToken());
        assertNull(s.getExpiracion());
        s.setToken("t");
        s.setExpiracion("e");
        assertEquals("t", s.getToken());
        assertEquals("e", s.getExpiracion());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "1", "10.5", "-5", "123456.789"})
    void numerosValidos(String v) {
        assertTrue(NumeroUtil.interpretarDecimal(v).isPresent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "1a", "", "  "})
    void numerosInvalidos(String v) {
        assertTrue(NumeroUtil.interpretarDecimal(v).isEmpty());
    }

    @Test
    void respuestaUtil() {
        RespuestaAutenticacion r = new RespuestaAutenticacion();
        r.setEstado("OK");
        assertTrue(RespuestaUtil.esExitosa(r));
        r.setEstado("ERROR");
        r.setCodigo("AUTH-001");
        assertTrue(RespuestaUtil.requiereAutenticarse(r));
        r.setCodigo("AUTH-002");
        assertTrue(RespuestaUtil.requiereAutenticarse(r));
        r.setCodigo("X");
        assertFalse(RespuestaUtil.requiereAutenticarse(r));
    }
}
