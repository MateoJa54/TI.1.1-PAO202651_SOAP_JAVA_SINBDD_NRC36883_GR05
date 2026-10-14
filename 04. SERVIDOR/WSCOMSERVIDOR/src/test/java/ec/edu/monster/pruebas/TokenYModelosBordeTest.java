package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.modelos.CodigoRespuesta;
import ec.edu.monster.modelos.Respuesta;
import ec.edu.monster.modelos.RespuestaAutenticacion;
import ec.edu.monster.modelos.RespuestaCatalogo;
import ec.edu.monster.modelos.RespuestaConversion;
import ec.edu.monster.modelos.RespuestaUnidades;
import ec.edu.monster.modelos.SolicitudConversion;
import ec.edu.monster.seguridad.TokenServicio;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Casos borde de TokenServicio y requisitos que JAX-WS impone a los modelos
 * que viajan por el contrato (constructor vacío y get/set por cada campo).
 */
class TokenYModelosBordeTest {

    private final RelojManual reloj = new RelojManual(Instant.parse("2026-01-01T00:00:00Z"));
    private final TokenServicio tokens = new TokenServicio(Duration.ofMinutes(15), reloj);

    // ---------- token ----------

    @Test
    void elTokenNoSeAceptaConOtrasMayusculasNiConEspacios() {
        String token = tokens.generarToken();

        assertEquals(CodigoRespuesta.AUTH_001, tokens.validarToken(token.toUpperCase()));
        assertEquals(CodigoRespuesta.AUTH_001, tokens.validarToken(" " + token + " "));
        assertEquals(CodigoRespuesta.AUTH_001, tokens.validarToken(token + "\n"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "undefined", "Bearer x", "' OR '1'='1", "<token/>"})
    void textosQueUnClienteDefectuosoPuedeEnviarSonAuth001(String token) {
        assertEquals(CodigoRespuesta.AUTH_001, tokens.validarToken(token));
    }

    @Test
    void unTokenEnormeEsAuth001SinErrores() {
        assertEquals(CodigoRespuesta.AUTH_001, tokens.validarToken("a".repeat(1_000_000)));
    }

    @Test
    void laVigenciaMinimaDeUnNanosegundoFunciona() {
        TokenServicio corto = new TokenServicio(Duration.ofNanos(1), reloj);
        String token = corto.generarToken();

        assertEquals(CodigoRespuesta.OK_200, corto.validarToken(token));
        reloj.avanzar(Duration.ofNanos(1));
        assertEquals(CodigoRespuesta.AUTH_002, corto.validarToken(token));
    }

    @Test
    void trasInformarLaExpiracionElMismoTokenPasaASerDesconocido() {
        String token = tokens.generarToken();
        reloj.avanzar(Duration.ofMinutes(15));

        assertEquals(CodigoRespuesta.AUTH_002, tokens.validarToken(token));
        // Comportamiento actual: el segundo intento ya no dice "expirada" sino "inválido".
        assertEquals(CodigoRespuesta.AUTH_001, tokens.validarToken(token));
    }

    @Test
    void obtenerExpiracionDeUnTokenDesconocidoEstaVacia() {
        assertTrue(tokens.obtenerExpiracion("no-existe").isEmpty());
        assertTrue(tokens.obtenerExpiracion(null).isEmpty());
    }

    // ---------- modelos que viajan por el contrato ----------

    @Test
    void cadaModeloDelContratoTieneConstructorVacioPublico() throws Exception {
        for (Class<?> tipo : List.of(RespuestaAutenticacion.class, RespuestaCatalogo.class,
                RespuestaUnidades.class, RespuestaConversion.class, SolicitudConversion.class)) {
            assertNotNull(tipo.getConstructor(), tipo.getSimpleName());
            assertTrue(Modifier.isPublic(tipo.getConstructor().getModifiers()), tipo.getSimpleName());
        }
    }

    @Test
    void cadaCampoDeLosModelosDelContratoTieneGetYSet() throws Exception {
        for (Class<?> tipo : List.of(RespuestaAutenticacion.class, RespuestaCatalogo.class,
                RespuestaUnidades.class, RespuestaConversion.class, SolicitudConversion.class)) {
            for (PropertyDescriptor propiedad : Introspector.getBeanInfo(tipo, Object.class).getPropertyDescriptors()) {
                assertNotNull(propiedad.getReadMethod(), tipo.getSimpleName() + "." + propiedad.getName());
                assertNotNull(propiedad.getWriteMethod(), tipo.getSimpleName() + "." + propiedad.getName());
            }
        }
    }

    @Test
    void laRespuestaBaseEsAbstractaYNoSePuedeEnviarSola() {
        assertTrue(Modifier.isAbstract(Respuesta.class.getModifiers()));
    }

    @Test
    void unaSolicitudVaciaNoInventaValores() {
        SolicitudConversion solicitud = new SolicitudConversion();

        assertEquals(null, solicitud.getValor());
        assertEquals(null, solicitud.getCategoria());
    }
}
