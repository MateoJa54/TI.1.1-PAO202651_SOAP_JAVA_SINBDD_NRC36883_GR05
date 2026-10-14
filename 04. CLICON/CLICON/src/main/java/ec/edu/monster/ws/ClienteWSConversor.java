package ec.edu.monster.ws;

import ec.edu.monster.modelo.Respuesta;
import ec.edu.monster.modelo.RespuestaAutenticacion;
import ec.edu.monster.modelo.RespuestaCatalogo;
import ec.edu.monster.modelo.RespuestaConversion;
import ec.edu.monster.modelo.RespuestaUnidades;
import ec.edu.monster.modelo.SolicitudConversion;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebServiceException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import javax.xml.namespace.QName;

/**
 * Adaptador SOAP: implementa {@link IClienteConversor} llamando al servicio
 * web con JAX-WS. Es la única clase del cliente que sabe que existe SOAP.
 *
 * <p>No descarga el WSDL del servidor: usa la copia del contrato guardada en
 * el proyecto ({@code src/main/resources/wsdl}) y solo cambia la dirección
 * a la que se envían los mensajes. Así arranca aunque el servidor esté
 * apagado y el error aparece, de forma controlada, en la primera llamada.
 */
public class ClienteWSConversor implements IClienteConversor {

    public static final String URL_POR_DEFECTO =
            "http://localhost:8080/WSCOMSERVIDOR/WSConversorUnidades";

    private static final QName SERVICIO =
            new QName(WSConversorUnidades.ESPACIO_DE_NOMBRES, "WSConversorUnidades");
    private static final String WSDL_LOCAL = "/wsdl/WSConversorUnidades.wsdl";
    private static final QName PUERTO =
            new QName(WSConversorUnidades.ESPACIO_DE_NOMBRES, "WSConversorUnidadesPort");

    // Sin estos límites, un servidor que no contesta dejaría la consola colgada.
    private static final int ESPERA_CONEXION_MS = 5_000;
    private static final int ESPERA_RESPUESTA_MS = 15_000;

    private final String url;
    private final WSConversorUnidades puerto;

    /**
     * @param url dirección del servicio, sin {@code ?wsdl}
     */
    public ClienteWSConversor(String url) {
        this(url, ESPERA_CONEXION_MS, ESPERA_RESPUESTA_MS);
    }

    /**
     * @param url               dirección del servicio, sin {@code ?wsdl}
     * @param esperaConexionMs  tiempo máximo para conectar, en milisegundos
     * @param esperaRespuestaMs tiempo máximo para recibir la respuesta, en milisegundos
     */
    public ClienteWSConversor(String url, int esperaConexionMs, int esperaRespuestaMs) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("La dirección del servicio es obligatoria");
        }
        this.url = url.trim();
        exigirDireccionHttp(this.url);
        URL contrato = Objects.requireNonNull(
                ClienteWSConversor.class.getResource(WSDL_LOCAL), "Falta el contrato " + WSDL_LOCAL);
        this.puerto = Service.create(contrato, SERVICIO).getPort(PUERTO, WSConversorUnidades.class);

        Map<String, Object> contexto = ((BindingProvider) puerto).getRequestContext();
        contexto.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, this.url);
        contexto.put("com.sun.xml.ws.connect.timeout", esperaConexionMs);
        contexto.put("com.sun.xml.ws.request.timeout", esperaRespuestaMs);
    }

    /** Rechaza al arrancar una dirección mal escrita, en lugar de fallar en la primera llamada. */
    private static void exigirDireccionHttp(String direccion) {
        try {
            URI uri = new URI(direccion);
            boolean esHttp = "http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme());
            if (!esHttp || uri.getHost() == null) {
                throw new IllegalArgumentException("La dirección del servicio no es válida: " + direccion);
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("La dirección del servicio no es válida: " + direccion, e);
        }
    }

    public String getUrl() {
        return url;
    }

    @Override
    public RespuestaAutenticacion autenticar(String usuario, String password) {
        return llamar(() -> puerto.autenticar(usuario, password));
    }

    @Override
    public RespuestaCatalogo obtenerCategorias(String token) {
        return llamar(() -> puerto.obtenerCategorias(token));
    }

    @Override
    public RespuestaUnidades obtenerUnidades(String token, String categoria) {
        return llamar(() -> puerto.obtenerUnidades(token, categoria));
    }

    @Override
    public RespuestaConversion convertir(String token, SolicitudConversion solicitud) {
        Objects.requireNonNull(solicitud, "La solicitud es obligatoria");
        return llamar(() -> puerto.convertir(
                token,
                solicitud.getCategoria(),
                solicitud.getUnidadOrigen(),
                solicitud.getUnidadDestino(),
                solicitud.getValor()));
    }

    /**
     * Ejecuta la llamada SOAP y traduce cualquier fallo de comunicación a una
     * sola excepción del cliente, para que el resto del programa no dependa
     * de las excepciones de JAX-WS.
     */
    private <R extends Respuesta> R llamar(Supplier<R> operacion) {
        R respuesta;
        try {
            respuesta = operacion.get();
        } catch (WebServiceException e) {
            throw new ServicioNoDisponibleException(
                    "No se pudo comunicar con el servidor en " + url, e);
        }
        if (respuesta == null || respuesta.getEstado() == null || respuesta.getCodigo() == null) {
            throw new ServicioNoDisponibleException(
                    "El servidor en " + url + " contestó algo que no es una respuesta del conversor", null);
        }
        return respuesta;
    }
}
