package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import ec.edu.monster.modelo.RespuestaAutenticacion;
import ec.edu.monster.modelo.RespuestaCatalogo;
import ec.edu.monster.modelo.RespuestaConversion;
import ec.edu.monster.modelo.RespuestaUnidades;
import ec.edu.monster.modelo.SolicitudConversion;
import ec.edu.monster.ws.ClienteWSConversor;
import ec.edu.monster.ws.ServicioNoDisponibleException;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import jakarta.xml.ws.Endpoint;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Casos borde del adaptador SOAP: textos difíciles de transportar en XML y
 * servidores que contestan mal, tarde o no contestan.
 */
class ClienteWSConversorBordeTest {

    private static final String ESPACIO = "http://ws.monster.edu.ec/";

    private static Endpoint servicio;
    private static HttpServer servidorHttp;
    private static String url;
    private static String urlHttp;

    /** Servicio de prueba que devuelve lo que recibe, o falla según el usuario pedido. */
    @WebService(serviceName = "WSConversorUnidades", targetNamespace = ESPACIO)
    public static class ServicioEco {

        @WebMethod(operationName = "autenticar")
        public RespuestaAutenticacion autenticar(
                @WebParam(name = "usuario") String usuario,
                @WebParam(name = "password") String password) {
            if ("excepcion".equals(usuario)) {
                throw new IllegalStateException("fallo no controlado en el servidor");
            }
            RespuestaAutenticacion r = new RespuestaAutenticacion();
            if ("vacia".equals(usuario)) {
                return r;
            }
            if ("nula".equals(usuario)) {
                return null;
            }
            r.setEstado("OK");
            r.setCodigo("200");
            r.setMensaje(usuario == null ? "<nulo>" : usuario);
            r.setToken(password == null ? "<nulo>" : password);
            return r;
        }

        @WebMethod(operationName = "obtenerCategorias")
        public RespuestaCatalogo obtenerCategorias(@WebParam(name = "token") String token) {
            RespuestaCatalogo r = new RespuestaCatalogo();
            r.setEstado("OK");
            r.setCodigo("200");
            r.setMensaje(token == null ? "<nulo>" : token);
            List<String> muchas = new ArrayList<>();
            for (int i = 0; i < 500; i++) {
                muchas.add("CATEGORIA-" + i);
            }
            r.setCategorias(muchas);
            return r;
        }

        @WebMethod(operationName = "obtenerUnidades")
        public RespuestaUnidades obtenerUnidades(
                @WebParam(name = "token") String token,
                @WebParam(name = "categoria") String categoria) {
            RespuestaUnidades r = new RespuestaUnidades();
            r.setEstado("OK");
            r.setCodigo("200");
            r.setMensaje("ok");
            r.setCategoria(categoria);
            r.setUnidades(List.of("°C", "°Ré", "a & b", "<x>", "comilla\"doble", "  con espacios  "));
            return r;
        }

        @WebMethod(operationName = "convertir")
        public RespuestaConversion convertir(
                @WebParam(name = "token") String token,
                @WebParam(name = "categoria") String categoria,
                @WebParam(name = "unidadOrigen") String unidadOrigen,
                @WebParam(name = "unidadDestino") String unidadDestino,
                @WebParam(name = "valor") Double valor) {
            RespuestaConversion r = new RespuestaConversion();
            r.setEstado("OK");
            r.setCodigo("200");
            r.setMensaje(categoria == null ? "<nulo>" : categoria);
            r.setUnidadOrigen(unidadOrigen);
            r.setUnidadDestino(unidadDestino);
            r.setResultado(valor);
            return r;
        }
    }

    @BeforeAll
    static void publicar() throws IOException {
        url = "http://localhost:" + puertoLibre() + "/WSCOMSERVIDOR/WSConversorUnidades";
        servicio = Endpoint.publish(url, new ServicioEco());

        // Un servidor web cualquiera, que NO es el conversor.
        servidorHttp = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        responder("/html", 200, "text/html", "<html><body>Hola</body></html>");
        responder("/vacio", 200, "text/xml", "");
        responder("/error500", 500, "text/plain", "Internal Server Error");
        responder("/noexiste", 404, "text/html", "<h1>404</h1>");
        responder("/xmlroto", 200, "text/xml", "<soap:Envelope><sin-cerrar>");
        responder("/otroxml", 200, "text/xml", "<?xml version=\"1.0\"?><otra><cosa/></otra>");
        servidorHttp.createContext("/lento", intercambio -> {
            try {
                Thread.sleep(3_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            intercambio.sendResponseHeaders(200, -1);
            intercambio.close();
        });
        servidorHttp.setExecutor(Executors.newCachedThreadPool());
        servidorHttp.start();
        urlHttp = "http://localhost:" + servidorHttp.getAddress().getPort();
    }

    private static void responder(String ruta, int estado, String tipo, String cuerpo) {
        servidorHttp.createContext(ruta, intercambio -> {
            byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);
            intercambio.getRequestBody().readAllBytes();
            intercambio.getResponseHeaders().set("Content-Type", tipo + "; charset=utf-8");
            intercambio.sendResponseHeaders(estado, bytes.length == 0 ? -1 : bytes.length);
            if (bytes.length > 0) {
                intercambio.getResponseBody().write(bytes);
            }
            intercambio.close();
        });
    }

    @AfterAll
    static void detener() {
        servicio.stop();
        servidorHttp.stop(0);
    }

    private static int puertoLibre() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static SolicitudConversion solicitud(String categoria, String origen, String destino, Double valor) {
        return new SolicitudConversion(categoria, origen, destino, valor);
    }

    // ---------- textos difíciles de transportar ----------

    @ParameterizedTest
    @ValueSource(strings = {
        "a & b", "<script>alert(1)</script>", "]]>", "comilla\"doble", "comilla'simple", "50% + 1",
        "ñandú áéíóú ÑÁÉÍÓÚ ü", "°C → °F", "日本語", "emoji 😀", "linea1\nlinea2", "tab\tulado",
        "<![CDATA[x]]>", "&amp;", "' OR '1'='1", "a\\b/c", "{json: true}"})
    void losCaracteresEspecialesLleganIntactosEnLosDosSentidos(String texto) {
        RespuestaAutenticacion r = new ClienteWSConversor(url).autenticar(texto, texto);

        assertEquals(texto, r.getMensaje());
        assertEquals(texto, r.getToken());
    }

    @Test
    void losEspaciosAlInicioYAlFinalSeConservan() {
        RespuestaAutenticacion r = new ClienteWSConversor(url).autenticar("  usuario  ", "  clave  ");

        assertEquals("  usuario  ", r.getMensaje());
        assertEquals("  clave  ", r.getToken());
    }

    @Test
    void unTextoVacioNoSeConvierteEnNulo() {
        assertEquals("", new ClienteWSConversor(url).autenticar("", "").getMensaje());
    }

    @Test
    void unParametroNuloLlegaNuloAlServidor() {
        ClienteWSConversor cliente = new ClienteWSConversor(url);

        assertEquals("<nulo>", cliente.autenticar(null, null).getMensaje());
        assertEquals("<nulo>", cliente.obtenerCategorias(null).getMensaje());
        assertEquals("<nulo>", cliente.convertir(null, solicitud(null, null, null, 1.0)).getMensaje());
    }

    @Test
    void unTextoMuyLargoViajaCompleto() {
        String largo = "x".repeat(200_000);

        assertEquals(largo, new ClienteWSConversor(url).autenticar(largo, "p").getMensaje());
    }

    @Test
    void unaListaGrandeLlegaCompletaYEnOrden() {
        List<String> categorias = new ClienteWSConversor(url).obtenerCategorias("t").getCategorias();

        assertEquals(500, categorias.size());
        assertEquals("CATEGORIA-0", categorias.get(0));
        assertEquals("CATEGORIA-499", categorias.get(499));
    }

    @Test
    void losElementosDeUnaListaConservanSimbolosYEspacios() {
        RespuestaUnidades r = new ClienteWSConversor(url).obtenerUnidades("t", "TEMPERATURA");

        assertEquals(List.of("°C", "°Ré", "a & b", "<x>", "comilla\"doble", "  con espacios  "), r.getUnidades());
    }

    // ---------- números ----------

    @ParameterizedTest
    @ValueSource(doubles = {
        0.0, -0.0, 1.0, -1.0, 0.1, 1.0 / 3, 1609.344, 1e-300, 1e300, 123456789.123456789,
        Double.MIN_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE})
    void cualquierNumeroFinitoVaYVuelveSinCambiarNiUnBit(double valor) {
        RespuestaConversion r = new ClienteWSConversor(url).convertir("t", solicitud("L", "a", "b", valor));

        assertEquals(Double.doubleToLongBits(valor), Double.doubleToLongBits(r.getResultado()));
    }

    @ParameterizedTest
    @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void losValoresNoFinitosSePuedenTransportarYElServidorEsQuienLosRechaza(double valor) {
        // La vista nunca los envía; si otro código lo hiciera, SOAP los lleva y el servidor responde DATA-001.
        RespuestaConversion r = new ClienteWSConversor(url).convertir("t", solicitud("L", "a", "b", valor));

        assertEquals(Double.doubleToLongBits(valor), Double.doubleToLongBits(r.getResultado()));
    }

    // ---------- el servidor contesta mal ----------

    @Test
    void siElServidorLanzaUnaExcepcionElClienteRecibeSuPropiaExcepcion() {
        ClienteWSConversor cliente = new ClienteWSConversor(url);

        ServicioNoDisponibleException error = assertThrows(
                ServicioNoDisponibleException.class, () -> cliente.autenticar("excepcion", "p"));

        assertNotNull(error.getCause());
        assertTrue(error.getMessage().contains(url));
    }

    @Test
    void unaRespuestaSinEstadoNiCodigoNoSeAceptaComoValida() {
        ClienteWSConversor cliente = new ClienteWSConversor(url);

        assertThrows(ServicioNoDisponibleException.class, () -> cliente.autenticar("vacia", "p"));
    }

    @Test
    void unaRespuestaNulaNoSeAceptaComoValida() {
        ClienteWSConversor cliente = new ClienteWSConversor(url);

        assertThrows(ServicioNoDisponibleException.class, () -> cliente.autenticar("nula", "p"));
    }

    @Test
    void despuesDeUnFalloElMismoClienteSigueSirviendo() {
        ClienteWSConversor cliente = new ClienteWSConversor(url);
        assertThrows(ServicioNoDisponibleException.class, () -> cliente.autenticar("excepcion", "p"));

        assertEquals("monster", cliente.autenticar("monster", "p").getMensaje());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/html", "/vacio", "/error500", "/noexiste", "/xmlroto", "/otroxml", "/ruta-sin-registrar"})
    void siLaDireccionEsDeOtroServidorWebSeLanzaLaExcepcionDelCliente(String ruta) {
        ClienteWSConversor cliente = new ClienteWSConversor(urlHttp + ruta);

        assertThrows(ServicioNoDisponibleException.class, () -> cliente.obtenerCategorias("t"));
        assertThrows(ServicioNoDisponibleException.class,
                () -> cliente.convertir("t", solicitud("L", "a", "b", 1.0)));
    }

    @Test
    void siElServidorTardaDemasiadoElClienteNoSeQuedaColgado() {
        ClienteWSConversor cliente = new ClienteWSConversor(urlHttp + "/lento", 1_000, 300);

        assertTimeout(Duration.ofSeconds(2),
                () -> assertThrows(ServicioNoDisponibleException.class, () -> cliente.obtenerCategorias("t")));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "esto no es una url", "ftp://localhost/servicio", "http://", "localhost:8080/WSCOMSERVIDOR",
        "//localhost:8080/ws", "http:///sin-equipo", "C:/carpeta/servicio","http://local host/ws"})
    void unaDireccionMalEscritaSeRechazaAlCrearElCliente(String direccion) {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class, () -> new ClienteWSConversor(direccion));

        assertTrue(error.getMessage().contains(direccion));
    }

    @Test
    void unEquipoQueNoExisteFallaConLaExcepcionDelClienteAlLlamar() {
        ClienteWSConversor cliente = new ClienteWSConversor(
                "http://equipo-que-no-existe.invalid:8080/WSCOMSERVIDOR/WSConversorUnidades", 1_000, 1_000);

        assertThrows(ServicioNoDisponibleException.class, () -> cliente.obtenerCategorias("t"));
    }

    @Test
    void seAceptanDireccionesHttpsYConDireccionIp() {
        assertEquals("https://conversor.monster.edu.ec/ws", new ClienteWSConversor("https://conversor.monster.edu.ec/ws").getUrl());
        assertEquals("http://192.168.1.50:8080/ws", new ClienteWSConversor("http://192.168.1.50:8080/ws").getUrl());
    }

    // ---------- uso simultáneo ----------

    @Test
    void variosClientesALaVezRecibenCadaUnoSuRespuesta() throws Exception {
        int hilos = 8;
        ExecutorService ejecutor = Executors.newFixedThreadPool(hilos);
        try {
            List<Future<Boolean>> tareas = new ArrayList<>();
            for (int h = 0; h < hilos; h++) {
                int numero = h;
                Callable<Boolean> tarea = () -> {
                    ClienteWSConversor cliente = new ClienteWSConversor(url);
                    boolean correcto = true;
                    for (int i = 0; i < 25; i++) {
                        double valor = numero * 1000 + i;
                        RespuestaConversion r = cliente.convertir(
                                "t", solicitud("CAT-" + numero, "a", "b", valor));
                        correcto &= r.getResultado() == valor && ("CAT-" + numero).equals(r.getMensaje());
                    }
                    return correcto;
                };
                tareas.add(ejecutor.submit(tarea));
            }
            for (Future<Boolean> tarea : tareas) {
                assertTrue(tarea.get(60, TimeUnit.SECONDS));
            }
        } finally {
            ejecutor.shutdownNow();
        }
    }
}
