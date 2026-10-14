package ec.edu.monster.seguridad;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Rechaza con un SOAP Fault limpio las peticiones que el runtime de Payara no sabe despachar.
 *
 * <p>Una operación que no existe, un espacio de nombres incorrecto o un cuerpo vacío hacen que
 * Payara falle con un NullPointerException antes de llegar al servicio, y el cliente recibe
 * "java.lang.NullPointerException" (ERS RNF-009: no exponer detalles técnicos). Este filtro
 * revisa el elemento raíz del cuerpo y contesta con un mensaje genérico.
 *
 * <p>Los errores de negocio (token, datos, unidades) no pasan por aquí: siguen siendo
 * respuestas estructuradas del servicio. Esto solo cubre errores de protocolo.
 *
 * <p>Parámetros en web.xml: {@code soap.espacio.nombres} y {@code soap.operaciones}
 * (nombres separados por coma). No se leen del controlador para que la capa de seguridad no
 * dependa de la de controladores.
 */
public class FiltroPeticionSoap implements Filter {

    static final String PARAMETRO_ESPACIO = "soap.espacio.nombres";
    static final String PARAMETRO_OPERACIONES = "soap.operaciones";

    static final String ESPACIO_POR_DEFECTO = "http://ws.monster.edu.ec/";
    static final String OPERACIONES_POR_DEFECTO = "autenticar,obtenerCategorias,obtenerUnidades,convertir";

    /** Los mensajes más grandes se dejan pasar sin inspeccionar: el runtime ya sabe rechazarlos. */
    public static final int LIMITE_INSPECCION = 1_048_576;

    public static final String MENSAJE_OPERACION = "Operación no soportada";
    public static final String MENSAJE_FORMATO = "Mensaje SOAP mal formado";

    private static final String SOBRE_SOAP_11 = "http://schemas.xmlsoap.org/soap/envelope/";

    /** Resultado de revisar un mensaje. */
    public enum Veredicto {
        VALIDO,
        OPERACION_NO_SOPORTADA,
        MENSAJE_MAL_FORMADO
    }

    private String espacioNombres;
    private Set<String> operaciones;

    public FiltroPeticionSoap() {
        this(ESPACIO_POR_DEFECTO, OPERACIONES_POR_DEFECTO);
    }

    public FiltroPeticionSoap(String espacioNombres, String operaciones) {
        this.espacioNombres = espacioNombres;
        this.operaciones = separar(operaciones);
    }

    @Override
    public void init(FilterConfig configuracion) {
        String espacio = configuracion.getInitParameter(PARAMETRO_ESPACIO);
        if (espacio != null && !espacio.isBlank()) {
            this.espacioNombres = espacio.trim();
        }
        String lista = configuracion.getInitParameter(PARAMETRO_OPERACIONES);
        if (lista != null && !lista.isBlank()) {
            this.operaciones = separar(lista);
        }
    }

    @Override
    public void doFilter(ServletRequest peticion, ServletResponse respuesta, FilterChain cadena)
            throws IOException, ServletException {
        HttpServletRequest http = (HttpServletRequest) peticion;
        if (!esMensajeSoap(http)) {
            cadena.doFilter(peticion, respuesta);
            return;
        }

        byte[] leido = http.getInputStream().readNBytes(LIMITE_INSPECCION + 1);
        boolean completo = leido.length <= LIMITE_INSPECCION;
        Veredicto veredicto = completo ? inspeccionar(leido) : Veredicto.VALIDO;
        if (veredicto != Veredicto.VALIDO) {
            responderFault((HttpServletResponse) respuesta, mensajeDe(veredicto));
            return;
        }

        InputStream cuerpo = completo
                ? new ByteArrayInputStream(leido)
                : new SequenceInputStream(new ByteArrayInputStream(leido), http.getInputStream());
        cadena.doFilter(new PeticionConCuerpo(http, cuerpo), respuesta);
    }

    /**
     * Revisa que el cuerpo sea un sobre SOAP 1.1 cuya primera operación exista.
     *
     * @param mensaje bytes del mensaje HTTP
     * @return si se puede despachar o por qué no
     */
    public Veredicto inspeccionar(byte[] mensaje) {
        try {
            XMLStreamReader lector = nuevaFabrica().createXMLStreamReader(new ByteArrayInputStream(mensaje));
            try {
                lector.nextTag();
                if (!esElemento(lector, SOBRE_SOAP_11, "Envelope")) {
                    return Veredicto.MENSAJE_MAL_FORMADO;
                }
                lector.nextTag();
                if (esElemento(lector, SOBRE_SOAP_11, "Header")) {
                    saltarElemento(lector);
                    lector.nextTag();
                }
                if (!esElemento(lector, SOBRE_SOAP_11, "Body")) {
                    return Veredicto.MENSAJE_MAL_FORMADO;
                }
                if (lector.nextTag() != XMLStreamConstants.START_ELEMENT) {
                    return Veredicto.OPERACION_NO_SOPORTADA; // <Body/> vacío
                }
                boolean existe = espacioNombres.equals(lector.getNamespaceURI())
                        && operaciones.contains(lector.getLocalName());
                if (!existe) {
                    return Veredicto.OPERACION_NO_SOPORTADA;
                }
                // Hasta el final: un mensaje cortado después de la operación también debe rechazarse.
                while (lector.hasNext()) {
                    lector.next();
                }
                return Veredicto.VALIDO;
            } finally {
                lector.close();
            }
        } catch (XMLStreamException | RuntimeException e) {
            return Veredicto.MENSAJE_MAL_FORMADO;
        }
    }

    static String mensajeDe(Veredicto veredicto) {
        return veredicto == Veredicto.OPERACION_NO_SOPORTADA ? MENSAJE_OPERACION : MENSAJE_FORMATO;
    }

    public static String construirFault(String mensaje) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<S:Envelope xmlns:S=\"" + SOBRE_SOAP_11 + "\"><S:Body><S:Fault>"
                + "<faultcode>S:Client</faultcode><faultstring>" + mensaje + "</faultstring>"
                + "</S:Fault></S:Body></S:Envelope>";
    }

    private static void responderFault(HttpServletResponse respuesta, String mensaje) throws IOException {
        byte[] cuerpo = construirFault(mensaje).getBytes(StandardCharsets.UTF_8);
        respuesta.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        respuesta.setContentType("text/xml; charset=utf-8");
        respuesta.setContentLength(cuerpo.length);
        respuesta.getOutputStream().write(cuerpo);
    }

    private static boolean esMensajeSoap(HttpServletRequest peticion) {
        String tipo = peticion.getContentType();
        return "POST".equalsIgnoreCase(peticion.getMethod())
                && tipo != null
                && tipo.toLowerCase(Locale.ROOT).startsWith("text/xml");
    }

    private static XMLInputFactory nuevaFabrica() {
        XMLInputFactory fabrica = XMLInputFactory.newFactory();
        // Sin DTD ni entidades externas: un mensaje con XXE se rechaza como mal formado.
        fabrica.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        fabrica.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        fabrica.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
        return fabrica;
    }

    private static boolean esElemento(XMLStreamReader lector, String espacio, String nombre) {
        return lector.getEventType() == XMLStreamConstants.START_ELEMENT
                && espacio.equals(lector.getNamespaceURI())
                && nombre.equals(lector.getLocalName());
    }

    /** Avanza hasta el cierre del elemento en el que está el lector. */
    private static void saltarElemento(XMLStreamReader lector) throws XMLStreamException {
        int profundidad = 1;
        while (profundidad > 0) {
            int evento = lector.next();
            if (evento == XMLStreamConstants.START_ELEMENT) {
                profundidad++;
            } else if (evento == XMLStreamConstants.END_ELEMENT) {
                profundidad--;
            }
        }
    }

    private static Set<String> separar(String lista) {
        return Arrays.stream(lista.split(","))
                .map(String::trim)
                .filter(nombre -> !nombre.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    /** La petición original con el cuerpo ya leído, para que el servicio lo reciba completo. */
    private static final class PeticionConCuerpo extends HttpServletRequestWrapper {

        private final InputStream flujo;

        PeticionConCuerpo(HttpServletRequest peticion, InputStream flujo) {
            super(peticion);
            this.flujo = flujo;
        }

        @Override
        public ServletInputStream getInputStream() {
            return new ServletInputStream() {
                private boolean terminado;

                @Override
                public int read() throws IOException {
                    int valor = flujo.read();
                    if (valor < 0) {
                        terminado = true;
                    }
                    return valor;
                }

                @Override
                public int read(byte[] destino, int desde, int largo) throws IOException {
                    int leidos = flujo.read(destino, desde, largo);
                    if (leidos < 0) {
                        terminado = true;
                    }
                    return leidos;
                }

                @Override
                public boolean isFinished() {
                    return terminado;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener oyente) {
                    throw new UnsupportedOperationException("Lectura asíncrona no soportada");
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            String codificacion = getCharacterEncoding();
            return new BufferedReader(new InputStreamReader(flujo,
                    codificacion == null ? StandardCharsets.UTF_8 : java.nio.charset.Charset.forName(codificacion)));
        }
    }
}
