package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.controladores.ws.WSConversorUnidades;
import ec.edu.monster.modelos.Categoria;
import ec.edu.monster.modelos.RespuestaCatalogo;
import ec.edu.monster.modelos.RespuestaConversion;
import ec.edu.monster.modelos.RespuestaUnidades;
import ec.edu.monster.modelos.Unidad;
import ec.edu.monster.seguridad.AutenticacionServicio;
import ec.edu.monster.seguridad.TokenServicio;
import ec.edu.monster.servicios.CatalogoServicio;
import ec.edu.monster.servicios.ConversionServicio;
import ec.edu.monster.servicios.conversores.ConversorLineal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Casos borde del catálogo y del servicio web completo (token + servicios),
 * tal como lo vería un cliente, pero sin levantar el servidor.
 */
class CatalogoYServicioWebBordeTest {

    private static final Instant INICIO = Instant.parse("2026-10-14T12:00:00Z");

    private RelojManual reloj;
    private CatalogoServicio catalogo;
    private WSConversorUnidades ws;

    @BeforeEach
    void preparar() {
        reloj = new RelojManual(INICIO);
        catalogo = new CatalogoServicio();
        ws = armar(catalogo, new ConversionServicio(catalogo));
    }

    private WSConversorUnidades armar(CatalogoServicio catalogoUsado, ConversionServicio conversion) {
        TokenServicio tokens = new TokenServicio(Duration.ofMinutes(15), reloj);
        return new WSConversorUnidades(
                new AutenticacionServicio("usuarioPrueba", "clavePrueba", tokens),
                tokens, catalogoUsado, conversion);
    }

    private String token(WSConversorUnidades servicio) {
        return servicio.autenticar("usuarioPrueba", "clavePrueba").getToken();
    }

    // ---------- CatalogoServicio ----------

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "VOLUMEN", "LONGITUDES", "LONG", "LONGITUD;MASA"})
    void consultarUnidadesDeUnaCategoriaInvalidaEsData001ConListaVacia(String categoria) {
        RespuestaUnidades r = catalogo.consultarUnidades(categoria);

        assertEquals("DATA-001", r.getCodigo());
        assertNull(r.getCategoria());
        assertEquals(List.of(), r.getUnidades());
    }

    @Test
    void consultarUnidadesDevuelveElNombreOficialDeLaCategoria() {
        assertEquals("TEMPERATURA", catalogo.consultarUnidades("  temperatura ").getCategoria());
    }

    @Test
    void lasBusquedasConNulosNoFallan() {
        assertTrue(catalogo.buscarCategoria(null).isEmpty());
        assertTrue(catalogo.buscarUnidad(null, null).isEmpty());
        assertTrue(catalogo.buscarUnidad("LONGITUD", null).isEmpty());
        assertTrue(catalogo.buscarUnidad(null, "metros").isEmpty());
        assertTrue(catalogo.buscarCategoriaDeUnidad(null).isEmpty());
        assertTrue(catalogo.obtenerUnidades(null).isEmpty());
    }

    @Test
    void nadiePuedeModificarElCatalogoDesdeAfuera() {
        assertThrows(UnsupportedOperationException.class,
                () -> catalogo.obtenerCategorias().add("VOLUMEN"));
        assertThrows(UnsupportedOperationException.class,
                () -> catalogo.obtenerUnidades("LONGITUD").add("leguas"));
        assertThrows(UnsupportedOperationException.class,
                () -> catalogo.buscarCategoria("MASA").orElseThrow().getUnidades().clear());
    }

    @Test
    void modificarLaListaOriginalNoCambiaLaCategoriaYaCreada() {
        List<Unidad> unidades = new ArrayList<>(List.of(new Unidad("litros", "l", 1.0)));
        Categoria volumen = new Categoria("VOLUMEN", unidades);

        unidades.add(new Unidad("galones", "gal", 3.785));

        assertEquals(1, volumen.getUnidades().size());
    }

    @Test
    void ningunaUnidadEstaRepetidaEntreCategorias() {
        // Si una unidad estuviera en dos categorías, "unidad de otra categoría" sería ambiguo.
        List<String> todas = new ArrayList<>();
        for (String categoria : catalogo.obtenerCategorias()) {
            todas.addAll(catalogo.obtenerUnidades(categoria));
        }

        assertEquals(15, todas.size());
        assertEquals(15, todas.stream().distinct().count());
    }

    @Test
    void losFactoresDeLongitudYMasaSonPositivos() {
        for (String categoria : List.of(CatalogoServicio.LONGITUD, CatalogoServicio.MASA)) {
            for (Unidad unidad : catalogo.buscarCategoria(categoria).orElseThrow().getUnidades()) {
                assertTrue(unidad.getFactorBase() > 0, unidad.getNombre());
                assertNotNull(unidad.getSimbolo());
            }
        }
    }

    @Test
    void unCatalogoVacioRespondeOkSinCategorias() {
        CatalogoServicio vacio = new CatalogoServicio(List.of());

        assertEquals("OK", vacio.consultarCategorias().getEstado());
        assertEquals(List.of(), vacio.consultarCategorias().getCategorias());
        assertEquals("DATA-001", vacio.consultarUnidades("LONGITUD").getCodigo());
    }

    // ---------- servicio web: el token manda ----------

    @Test
    void conTokenVencidoLasTresOperacionesProtegidasRespondenAuth002() {
        String vencido = token(ws);
        reloj.avanzar(Duration.ofMinutes(15));

        assertEquals("AUTH-002", ws.obtenerCategorias(vencido).getCodigo());
        // Tras informar la expiración el token se elimina: desde ahí es un token desconocido.
        assertEquals("AUTH-001", ws.obtenerUnidades(vencido, "LONGITUD").getCodigo());
        assertEquals("AUTH-001", ws.convertir(vencido, "LONGITUD", "metros", "pies", 1.0).getCodigo());
    }

    @Test
    void elTokenSeRevisaAntesQueLosDatos() {
        // Un cliente sin sesión no debe poder averiguar qué unidades existen.
        assertEquals("AUTH-001", ws.convertir("falso", "VOLUMEN", "xyz", "abc", null).getCodigo());
        assertEquals("AUTH-001", ws.obtenerUnidades(null, "VOLUMEN").getCodigo());
    }

    @Test
    void sinTokenNoSeEntregaNingunDatoDelCatalogo() {
        RespuestaCatalogo categorias = ws.obtenerCategorias("falso");
        RespuestaUnidades unidades = ws.obtenerUnidades("falso", "LONGITUD");

        assertEquals(List.of(), categorias.getCategorias());
        assertEquals(List.of(), unidades.getUnidades());
        assertNull(unidades.getCategoria());
    }

    @Test
    void unMismoTokenSirveParaTodasLasOperaciones() {
        String token = token(ws);

        assertEquals("OK", ws.obtenerCategorias(token).getEstado());
        assertEquals("OK", ws.obtenerUnidades(token, "MASA").getEstado());
        assertEquals("OK", ws.convertir(token, "MASA", "libras", "onzas", 1.0).getEstado());
        assertEquals("OK", ws.convertir(token, "MASA", "onzas", "libras", 16.0).getEstado());
    }

    @Test
    void unErrorDeDatosNoCierraLaSesion() {
        String token = token(ws);

        assertEquals("UNIT-001", ws.convertir(token, "LONGITUD", "xyz", "metros", 1.0).getCodigo());
        assertEquals("DATA-001", ws.convertir(token, "LONGITUD", "metros", "pies", null).getCodigo());
        assertEquals("OK", ws.convertir(token, "LONGITUD", "metros", "pies", 1.0).getEstado());
    }

    @Test
    void elTokenDeUnServidorNoSirveEnOtro() {
        WSConversorUnidades otro = armar(catalogo, new ConversionServicio(catalogo));

        assertEquals("AUTH-001", otro.obtenerCategorias(token(ws)).getCodigo());
    }

    @Test
    void volverAAutenticarseTrasLaExpiracionDevuelveElAcceso() {
        String vencido = token(ws);
        reloj.avanzar(Duration.ofMinutes(20));
        assertEquals("AUTH-002", ws.obtenerCategorias(vencido).getCodigo());

        String nuevo = token(ws);

        assertEquals("OK", ws.obtenerCategorias(nuevo).getEstado());
    }

    // ---------- servicio web: datos de entrada ----------

    @Test
    void autenticarConCamposNulosEsData001YNoUnaExcepcion() {
        assertEquals("DATA-001", ws.autenticar(null, null).getCodigo());
        assertEquals("DATA-001", ws.autenticar("usuarioPrueba", "").getCodigo());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "VOLUMEN"})
    void obtenerUnidadesConCategoriaInvalidaEsData001(String categoria) {
        assertEquals("DATA-001", ws.obtenerUnidades(token(ws), categoria).getCodigo());
    }

    @Test
    void convertirConTodosLosCamposNulosEsData001() {
        RespuestaConversion r = ws.convertir(token(ws), null, null, null, null);

        assertEquals("DATA-001", r.getCodigo());
        assertNull(r.getResultado());
    }

    @Test
    void todaRespuestaTraeEstadoCodigoYMensaje() {
        String token = token(ws);

        for (var r : List.of(
                ws.autenticar("x", "y"),
                ws.obtenerCategorias(token),
                ws.obtenerCategorias(null),
                ws.obtenerUnidades(token, "MASA"),
                ws.obtenerUnidades(token, "xyz"),
                ws.convertir(token, "MASA", "libras", "onzas", 1.0),
                ws.convertir(token, "MASA", "libras", "xyz", 1.0))) {
            assertTrue(r.getEstado().equals("OK") || r.getEstado().equals("ERROR"));
            assertNotNull(r.getCodigo());
            assertNotNull(r.getMensaje());
            assertEquals(r.getEstado().equals("OK"), r.getCodigo().equals("200"));
        }
    }

    // ---------- servicio web: el cliente siempre recibe respuesta ----------

    @Test
    void siElCatalogoFallaLasConsultasRespondenSys001() {
        CatalogoServicio roto = new CatalogoServicio() {
            @Override
            public RespuestaCatalogo consultarCategorias() {
                throw new IllegalStateException("fallo simulado");
            }

            @Override
            public RespuestaUnidades consultarUnidades(String categoria) {
                throw new IllegalStateException("fallo simulado");
            }
        };
        WSConversorUnidades wsRoto = armar(roto, new ConversionServicio(catalogo));
        String token = token(wsRoto);

        RespuestaCatalogo categorias = wsRoto.obtenerCategorias(token);
        RespuestaUnidades unidades = wsRoto.obtenerUnidades(token, "LONGITUD");

        assertEquals("SYS-001", categorias.getCodigo());
        assertEquals("SYS-001", unidades.getCodigo());
        assertEquals(List.of(), categorias.getCategorias());
    }

    @Test
    void elMensajeDeSys001NoRevelaElDetalleInterno() {
        ConversionServicio roto = new ConversionServicio(catalogo) {
            @Override
            public RespuestaConversion convertir(ec.edu.monster.modelos.SolicitudConversion solicitud) {
                throw new IllegalStateException("clave=secreta ruta=C:/interno");
            }
        };
        WSConversorUnidades wsRoto = armar(catalogo, roto);

        RespuestaConversion r = wsRoto.convertir(token(wsRoto), "LONGITUD", "metros", "pies", 1.0);

        assertEquals("Error interno del servidor", r.getMensaje());
    }

    @Test
    void unaCategoriaNuevaFuncionaDeExtremoAExtremo() {
        CatalogoServicio ampliado = new CatalogoServicio(List.of(new Categoria("VOLUMEN", List.of(
                new Unidad("litros", "l", 1.0), new Unidad("mililitros", "ml", 0.001)))));
        WSConversorUnidades wsAmpliado = armar(ampliado,
                new ConversionServicio(ampliado, Map.of("VOLUMEN", new ConversorLineal())));
        String token = token(wsAmpliado);

        assertEquals(List.of("VOLUMEN"), wsAmpliado.obtenerCategorias(token).getCategorias());
        assertEquals(500.0,
                wsAmpliado.convertir(token, "volumen", "mililitros", "litros", 500_000.0).getResultado(), 1e-9);
        assertEquals("DATA-001",
                wsAmpliado.convertir(token, "LONGITUD", "metros", "pies", 1.0).getCodigo());
    }
}
