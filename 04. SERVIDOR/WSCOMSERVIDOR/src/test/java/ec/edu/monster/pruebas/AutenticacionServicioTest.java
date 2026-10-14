package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ec.edu.monster.modelos.CodigoRespuesta;
import ec.edu.monster.modelos.RespuestaAutenticacion;
import ec.edu.monster.seguridad.AutenticacionServicio;
import ec.edu.monster.seguridad.TokenServicio;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AutenticacionServicioTest {

    private static final Instant INICIO = Instant.parse("2026-10-14T12:00:00Z");

    private TokenServicio tokenServicio;
    private AutenticacionServicio servicio;

    @BeforeEach
    void preparar() {
        tokenServicio = new TokenServicio(Duration.ofMinutes(15), new RelojManual(INICIO));
        servicio = new AutenticacionServicio("usuarioPrueba", "clavePrueba", tokenServicio);
    }

    @Test
    void credencialCorrectaEntregaTokenValidoYExpiracion() {
        RespuestaAutenticacion r = servicio.autenticar("usuarioPrueba", "clavePrueba");

        assertEquals("OK", r.getEstado());
        assertEquals("200", r.getCodigo());
        assertNotNull(r.getToken());
        assertEquals("2026-10-14T12:15:00Z", r.getExpiracion());
        assertEquals(CodigoRespuesta.OK_200, tokenServicio.validarToken(r.getToken()));
    }

    @Test
    void contrasenaOUsuarioIncorrectoEsAuth003SinToken() {
        RespuestaAutenticacion malaClave = servicio.autenticar("usuarioPrueba", "otra");
        RespuestaAutenticacion malUsuario = servicio.autenticar("otro", "clavePrueba");

        assertEquals("AUTH-003", malaClave.getCodigo());
        assertEquals("AUTH-003", malUsuario.getCodigo());
        assertNull(malaClave.getToken());
    }

    @Test
    void camposVaciosONulosEsData001() {
        assertEquals("DATA-001", servicio.autenticar("", "clavePrueba").getCodigo());
        assertEquals("DATA-001", servicio.autenticar("usuarioPrueba", "  ").getCodigo());
        assertEquals("DATA-001", servicio.autenticar(null, null).getCodigo());
    }

    @Test
    void laCredencialConfiguradaNoPuedeEstarVacia() {
        assertThrows(IllegalArgumentException.class,
                () -> new AutenticacionServicio("", "clave", tokenServicio));
        assertThrows(IllegalArgumentException.class,
                () -> new AutenticacionServicio("usuario", " ", tokenServicio));
    }
}
