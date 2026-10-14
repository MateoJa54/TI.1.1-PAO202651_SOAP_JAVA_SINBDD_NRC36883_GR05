package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import java.net.ServerSocket;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Prueba el adaptador con SOAP de verdad: publica, dentro de la misma
 * prueba, un servicio web mínimo con el mismo contrato que el servidor y lo
 * consume por HTTP. No necesita Payara.
 */
class ClienteWSConversorTest {

    private static final String ESPACIO = "http://ws.monster.edu.ec/";

    private static Endpoint servicio;
    private static String url;

    /** Servicio de prueba: mismo contrato que el servidor, respuestas fijas. */
    @WebService(serviceName = "WSConversorUnidades", targetNamespace = ESPACIO)
    public static class ServicioDePrueba {

        @WebMethod(operationName = "autenticar")
        public RespuestaAutenticacion autenticar(
                @WebParam(name = "usuario") String usuario,
                @WebParam(name = "password") String password) {
            RespuestaAutenticacion r = new RespuestaAutenticacion();
            if ("monster".equals(usuario) && "contraseña ñ".equals(password)) {
                r.setEstado("OK");
                r.setCodigo("200");
                r.setMensaje("Operación realizada correctamente");
                r.setToken("TOKEN-SOAP");
                r.setExpiracion("2026-10-14T12:15:00Z");
            } else {
                r.setEstado("ERROR");
                r.setCodigo("AUTH-003");
                r.setMensaje("Usuario o contraseña incorrectos");
            }
            return r;
        }

        @WebMethod(operationName = "obtenerCategorias")
        public RespuestaCatalogo obtenerCategorias(@WebParam(name = "token") String token) {
            RespuestaCatalogo r = new RespuestaCatalogo();
            if (token == null) {
                r.setEstado("ERROR");
                r.setCodigo("AUTH-001");
                r.setMensaje("Token inválido");
                return r;
            }
            r.setEstado("OK");
            r.setCodigo("200");
            r.setMensaje("Operación realizada correctamente");
            r.setCategorias(List.of("LONGITUD", "MASA", "TEMPERATURA"));
            return r;
        }

        @WebMethod(operationName = "obtenerUnidades")
        public RespuestaUnidades obtenerUnidades(
                @WebParam(name = "token") String token,
                @WebParam(name = "categoria") String categoria) {
            RespuestaUnidades r = new RespuestaUnidades();
            r.setEstado("OK");
            r.setCodigo("200");
            r.setMensaje("Operación realizada correctamente");
            r.setCategoria(categoria);
            r.setUnidades(List.of("centímetros", "metros"));
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
            if (valor == null) {
                r.setEstado("ERROR");
                r.setCodigo("DATA-001");
                r.setMensaje("Datos de la solicitud inconsistentes");
                return r;
            }
            r.setEstado("OK");
            r.setCodigo("200");
            // El mensaje devuelve lo recibido, para comprobar que cada parámetro llegó en su lugar.
            r.setMensaje(token + "|" + categoria);
            r.setResultado(valor * 100);
            r.setUnidadOrigen(unidadOrigen);
            r.setUnidadDestino(unidadDestino);
            return r;
        }
    }

    @BeforeAll
    static void publicarServicio() throws IOException {
        url = "http://localhost:" + puertoLibre() + "/WSCOMSERVIDOR/WSConversorUnidades";
        servicio = Endpoint.publish(url, new ServicioDePrueba());
    }

    @AfterAll
    static void detenerServicio() {
        servicio.stop();
    }

    private static int puertoLibre() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    @Test
    void autenticarRecibeElTokenYLaExpiracion() {
        RespuestaAutenticacion r = new ClienteWSConversor(url).autenticar("monster", "contraseña ñ");

        assertEquals("OK", r.getEstado());
        assertEquals("200", r.getCodigo());
        assertEquals("TOKEN-SOAP", r.getToken());
        assertEquals("2026-10-14T12:15:00Z", r.getExpiracion());
    }

    @Test
    void unaRespuestaDeErrorLlegaComoDatosYNoComoExcepcion() {
        RespuestaAutenticacion r = new ClienteWSConversor(url).autenticar("monster", "mala");

        assertEquals("ERROR", r.getEstado());
        assertEquals("AUTH-003", r.getCodigo());
        assertEquals("Usuario o contraseña incorrectos", r.getMensaje());
        assertNull(r.getToken());
    }

    @Test
    void lasListasLleganCompletasYConTildes() {
        ClienteWSConversor cliente = new ClienteWSConversor(url);

        assertEquals(List.of("LONGITUD", "MASA", "TEMPERATURA"),
                cliente.obtenerCategorias("TOKEN-SOAP").getCategorias());
        RespuestaUnidades unidades = cliente.obtenerUnidades("TOKEN-SOAP", "LONGITUD");
        assertEquals("LONGITUD", unidades.getCategoria());
        assertEquals(List.of("centímetros", "metros"), unidades.getUnidades());
    }

    @Test
    void unaListaAusenteEnLaRespuestaLlegaVaciaYNoNula() {
        RespuestaCatalogo r = new ClienteWSConversor(url).obtenerCategorias(null);

        assertEquals("AUTH-001", r.getCodigo());
        assertNotNull(r.getCategorias());
        assertTrue(r.getCategorias().isEmpty());
    }

    @Test
    void convertirEnviaCadaDatoEnSuParametro() {
        RespuestaConversion r = new ClienteWSConversor(url).convertir(
                "TOKEN-SOAP", new SolicitudConversion("LONGITUD", "metros", "centímetros", 2.5));

        assertEquals("TOKEN-SOAP|LONGITUD", r.getMensaje());
        assertEquals("metros", r.getUnidadOrigen());
        assertEquals("centímetros", r.getUnidadDestino());
        assertEquals(250.0, r.getResultado(), 1e-9);
    }

    @Test
    void losDecimalesYLosNegativosViajanSinPerderPrecision() {
        ClienteWSConversor cliente = new ClienteWSConversor(url);

        assertEquals(-12345.678901, cliente.convertir("t",
                new SolicitudConversion("LONGITUD", "a", "b", -123.45678901)).getResultado(), 1e-9);
        assertEquals(1e-7, cliente.convertir("t",
                new SolicitudConversion("LONGITUD", "a", "b", 1e-9)).getResultado(), 1e-18);
    }

    @Test
    void unValorNuloLlegaNuloAlServidorYNoComoCero() {
        RespuestaConversion r = new ClienteWSConversor(url).convertir(
                "TOKEN-SOAP", new SolicitudConversion("LONGITUD", "metros", "centímetros", null));

        assertEquals("DATA-001", r.getCodigo());
        assertNull(r.getResultado());
    }

    @Test
    void elMismoClienteSirveParaMuchasLlamadas() {
        ClienteWSConversor cliente = new ClienteWSConversor(url);

        for (int i = 1; i <= 20; i++) {
            assertEquals(i * 100.0, cliente.convertir("t",
                    new SolicitudConversion("LONGITUD", "a", "b", (double) i)).getResultado(), 1e-9);
        }
    }

    @Test
    void siNoHayServidorEnEsaDireccionSeLanzaLaExcepcionDelCliente() throws IOException {
        String sinServidor = "http://localhost:" + puertoLibre() + "/WSCOMSERVIDOR/WSConversorUnidades";
        ClienteWSConversor cliente = new ClienteWSConversor(sinServidor);

        ServicioNoDisponibleException error = assertThrows(
                ServicioNoDisponibleException.class, () -> cliente.obtenerCategorias("t"));

        assertTrue(error.getMessage().contains(sinServidor));
    }

    @Test
    void siLaRutaNoEsElServicioSeLanzaLaExcepcionDelCliente() {
        ClienteWSConversor cliente = new ClienteWSConversor(url.replace("WSConversorUnidades", "NoExiste"));

        assertThrows(ServicioNoDisponibleException.class, () -> cliente.autenticar("monster", "x"));
    }

    @Test
    void crearElClienteNoNecesitaQueElServidorEsteEncendido() {
        ClienteWSConversor cliente = new ClienteWSConversor("  http://localhost:1/noexiste  ");

        assertEquals("http://localhost:1/noexiste", cliente.getUrl());
    }

    @Test
    void laDireccionYLaSolicitudSonObligatorias() {
        assertThrows(IllegalArgumentException.class, () -> new ClienteWSConversor(null));
        assertThrows(IllegalArgumentException.class, () -> new ClienteWSConversor("  "));
        assertThrows(NullPointerException.class, () -> new ClienteWSConversor(url).convertir("t", null));
    }
}
