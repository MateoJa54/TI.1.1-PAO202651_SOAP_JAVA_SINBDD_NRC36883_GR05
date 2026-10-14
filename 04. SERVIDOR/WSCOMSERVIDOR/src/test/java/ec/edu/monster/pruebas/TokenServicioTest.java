package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.modelos.CodigoRespuesta;
import ec.edu.monster.seguridad.TokenServicio;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TokenServicioTest {

    private static final Instant INICIO = Instant.parse("2026-10-14T12:00:00Z");

    private RelojManual reloj;
    private TokenServicio servicio;

    @BeforeEach
    void preparar() {
        reloj = new RelojManual(INICIO);
        servicio = new TokenServicio(Duration.ofMinutes(15), reloj);
    }

    @Test
    void unTokenRecienGeneradoEsValido() {
        String token = servicio.generarToken();

        assertEquals(CodigoRespuesta.OK_200, servicio.validarToken(token));
    }

    @Test
    void cadaTokenEsDistinto() {
        assertNotEquals(servicio.generarToken(), servicio.generarToken());
    }

    @Test
    void tokenNuloVacioODesconocidoEsAuth001() {
        assertEquals(CodigoRespuesta.AUTH_001, servicio.validarToken(null));
        assertEquals(CodigoRespuesta.AUTH_001, servicio.validarToken("  "));
        assertEquals(CodigoRespuesta.AUTH_001, servicio.validarToken("no-existe"));
    }

    @Test
    void elTokenVencePasadosLos15Minutos() {
        String token = servicio.generarToken();

        reloj.avanzar(Duration.ofMinutes(14).plusSeconds(59));
        assertEquals(CodigoRespuesta.OK_200, servicio.validarToken(token));

        reloj.avanzar(Duration.ofSeconds(1));
        assertEquals(CodigoRespuesta.AUTH_002, servicio.validarToken(token));
    }

    @Test
    void unTokenVencidoSeEliminaYDespuesEsDesconocido() {
        String token = servicio.generarToken();
        reloj.avanzar(Duration.ofMinutes(16));

        assertEquals(CodigoRespuesta.AUTH_002, servicio.validarToken(token));
        assertEquals(CodigoRespuesta.AUTH_001, servicio.validarToken(token));
    }

    @Test
    void laExpiracionEsElInicioMasLaVigencia() {
        String token = servicio.generarToken();

        assertEquals(INICIO.plus(Duration.ofMinutes(15)), servicio.obtenerExpiracion(token).orElseThrow());
        assertTrue(servicio.obtenerExpiracion("no-existe").isEmpty());
        assertTrue(servicio.obtenerExpiracion(null).isEmpty());
    }

    @Test
    void generarTokenDescartaLosVencidos() {
        String viejo = servicio.generarToken();
        reloj.avanzar(Duration.ofMinutes(20));

        servicio.generarToken();

        assertTrue(servicio.obtenerExpiracion(viejo).isEmpty());
    }

    @Test
    void laVigenciaDebeSerPositiva() {
        assertThrows(IllegalArgumentException.class, () -> new TokenServicio(Duration.ZERO, reloj));
        assertThrows(IllegalArgumentException.class,
                () -> new TokenServicio(Duration.ofMinutes(-1), reloj));
    }
}
