package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.modelos.CodigoRespuesta;
import ec.edu.monster.seguridad.FiltroCors;
import ec.edu.monster.servicios.FabricaRespuestas;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Casos borde de FiltroCors (orígenes que intentan pasar por localhost) y de
 * FabricaRespuestas (cada código de error produce una respuesta coherente).
 */
class CorsYFabricaBordeTest {

    private final FiltroCors porDefecto = new FiltroCors();

    @ParameterizedTest
    @ValueSource(strings = {
        "null",
        "http://localhost:",
        "http://localhost:-1",
        "http://localhost:8080/",
        "http://localhost:8080.evil.com",
        "http://localhost.evil.com:8080",
        "https://localhost:8080",
        "http://LOCALHOST:8080",
        "http://localhost:8080 ",
        "file://"
    })
    void origenesQueImitanALocalhostSeRechazan(String origen) {
        assertFalse(porDefecto.esOrigenPermitido(origen), origen);
    }

    @ParameterizedTest
    @ValueSource(strings = {"http://localhost", "http://localhost:1", "http://127.0.0.1:65535"})
    void localhostConOSinPuertoSePermite(String origen) {
        assertTrue(porDefecto.esOrigenPermitido(origen), origen);
    }

    @Test
    void unaListaConfiguradaConEspaciosYComasSobrantesSeLimpia() {
        FiltroCors filtro = new FiltroCors(" , http://a.com ,, http://b.com:* , ");

        assertTrue(filtro.esOrigenPermitido("http://a.com"));
        assertTrue(filtro.esOrigenPermitido("http://b.com:9000"));
        assertFalse(filtro.esOrigenPermitido(""));
    }

    @Test
    void unaListaVaciaNoPermiteNingunOrigen() {
        assertFalse(new FiltroCors("").esOrigenPermitido("http://localhost:8080"));
    }

    @ParameterizedTest
    @EnumSource(value = CodigoRespuesta.class, names = "OK_200", mode = EnumSource.Mode.EXCLUDE)
    void cadaCodigoDeErrorProduceRespuestasSinDatos(CodigoRespuesta codigo) {
        assertEquals("ERROR", FabricaRespuestas.conversionFallida(codigo).getEstado());
        assertNull(FabricaRespuestas.conversionFallida(codigo).getResultado());
        assertNull(FabricaRespuestas.autenticacionFallida(codigo).getToken());
        assertTrue(FabricaRespuestas.catalogoFallido(codigo).getCategorias().isEmpty());
        assertTrue(FabricaRespuestas.unidadesFallidas(codigo).getUnidades().isEmpty());
        assertEquals(codigo.getCodigo(), FabricaRespuestas.unidadesFallidas(codigo).getCodigo());
    }

    @Test
    void unCodigoNuloNoProduceUnaRespuestaFalsa() {
        assertThrows(NullPointerException.class, () -> FabricaRespuestas.conversionFallida(null));
    }
}
