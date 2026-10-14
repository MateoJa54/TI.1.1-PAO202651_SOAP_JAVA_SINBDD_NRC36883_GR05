package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.modelos.CodigoRespuesta;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class CodigoRespuestaTest {

    @Test
    void soloElCodigo200EsExito() {
        for (CodigoRespuesta codigo : CodigoRespuesta.values()) {
            String esperado = codigo == CodigoRespuesta.OK_200 ? "OK" : "ERROR";
            assertEquals(esperado, codigo.getEstado(), codigo.name());
        }
    }

    @Test
    void losCodigosYMensajesCoincidenConElErsYElEcud() {
        assertEquals("200", CodigoRespuesta.OK_200.getCodigo());
        assertEquals("AUTH-001", CodigoRespuesta.AUTH_001.getCodigo());
        assertEquals("Token inválido", CodigoRespuesta.AUTH_001.getMensaje());
        assertEquals("Sesión expirada, vuelva a autenticarse", CodigoRespuesta.AUTH_002.getMensaje());
        assertEquals("Usuario o contraseña incorrectos", CodigoRespuesta.AUTH_003.getMensaje());
        assertEquals("Unidad de origen no soportada", CodigoRespuesta.UNIT_001.getMensaje());
        assertEquals("Unidad de destino no soportada", CodigoRespuesta.UNIT_002.getMensaje());
        assertEquals("Datos de la solicitud inconsistentes", CodigoRespuesta.DATA_001.getMensaje());
    }

    @Test
    void noHayCodigosRepetidos() {
        Set<String> codigos = Arrays.stream(CodigoRespuesta.values())
                .map(CodigoRespuesta::getCodigo)
                .collect(Collectors.toSet());
        assertTrue(codigos.size() == CodigoRespuesta.values().length);
    }
}
