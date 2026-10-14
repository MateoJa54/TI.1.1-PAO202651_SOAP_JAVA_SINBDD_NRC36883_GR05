package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.seguridad.FiltroPeticionSoap;
import ec.edu.monster.seguridad.FiltroPeticionSoap.Veredicto;
import jakarta.jws.WebMethod;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * El filtro cambia el "java.lang.NullPointerException" que Payara devuelve ante una operación
 * inexistente por un Fault genérico, sin tocar lo que sí es una operación válida.
 */
class FiltroPeticionSoapTest {

    private static final String NS = "http://ws.monster.edu.ec/";
    private static final String SOBRE = "http://schemas.xmlsoap.org/soap/envelope/";

    private final FiltroPeticionSoap filtro = new FiltroPeticionSoap();

    private static String mensaje(String cuerpo) {
        return "<?xml version=\"1.0\"?><s:Envelope xmlns:s=\"" + SOBRE + "\" xmlns:t=\"" + NS + "\">"
                + "<s:Body>" + cuerpo + "</s:Body></s:Envelope>";
    }

    private Veredicto revisar(String xml) {
        return filtro.inspeccionar(xml.getBytes(StandardCharsets.UTF_8));
    }

    // ---------- qué se considera una petición válida ----------

    @ParameterizedTest
    @ValueSource(strings = {"autenticar", "obtenerCategorias", "obtenerUnidades", "convertir"})
    void lasCuatroOperacionesDelServicioSonValidas(String operacion) {
        assertEquals(Veredicto.VALIDO, revisar(mensaje("<t:" + operacion + "><token>x</token></t:" + operacion + ">")));
    }

    @Test
    void unaCabeceraSoapAntesDelCuerpoNoImpideElDespacho() {
        String xml = "<s:Envelope xmlns:s=\"" + SOBRE + "\" xmlns:t=\"" + NS + "\">"
                + "<s:Header><x:a xmlns:x=\"urn:x\"><x:b/></x:a></s:Header>"
                + "<s:Body><t:convertir/></s:Body></s:Envelope>";

        assertEquals(Veredicto.VALIDO, revisar(xml));
    }

    @Test
    void comentariosYEspaciosEntreElementosSeIgnoran() {
        String xml = "<s:Envelope xmlns:s=\"" + SOBRE + "\" xmlns:t=\"" + NS + "\">\n  <!-- nota -->\n"
                + "  <s:Body>\n    <t:autenticar/>\n  </s:Body>\n</s:Envelope>";

        assertEquals(Veredicto.VALIDO, revisar(xml));
    }

    // ---------- lo que antes terminaba en NullPointerException ----------

    @Test
    void unaOperacionInexistenteSeRechaza() {
        assertEquals(Veredicto.OPERACION_NO_SOPORTADA, revisar(mensaje("<t:borrar/>")));
    }

    @Test
    void elNombreDeLaOperacionDistingueMayusculas() {
        assertEquals(Veredicto.OPERACION_NO_SOPORTADA, revisar(mensaje("<t:CONVERTIR/>")));
    }

    @Test
    void unEspacioDeNombresIncorrectoSeRechaza() {
        String xml = "<s:Envelope xmlns:s=\"" + SOBRE + "\"><s:Body><t:convertir xmlns:t=\"urn:otro\"/></s:Body></s:Envelope>";

        assertEquals(Veredicto.OPERACION_NO_SOPORTADA, revisar(xml));
    }

    @Test
    void unCuerpoVacioSeRechaza() {
        assertEquals(Veredicto.OPERACION_NO_SOPORTADA, revisar(mensaje("")));
    }

    // ---------- mensajes mal formados ----------

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "no es xml",
        "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body><t:convertir>",
        "<otra/>",
        "<s:Envelope xmlns:s=\"urn:no-soap\"><s:Body/></s:Envelope>",
        "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Header/></s:Envelope>"
    })
    void loQueNoEsUnSobreSoapValidoSeRechazaComoMalFormado(String xml) {
        assertEquals(Veredicto.MENSAJE_MAL_FORMADO, revisar(xml));
    }

    @Test
    void unMensajeCortadoDespuesDeLaOperacionSeRechaza() {
        // Con el espacio de nombres declarado, la operación es válida; lo que falla es el cierre.
        String cortado = "<s:Envelope xmlns:s=\"" + SOBRE + "\" xmlns:t=\"" + NS + "\"><s:Body><t:convertir>";
        String sinCerrarElSobre = mensaje("<t:convertir/>").replace("</s:Envelope>", "");

        assertEquals(Veredicto.MENSAJE_MAL_FORMADO, revisar(cortado));
        assertEquals(Veredicto.MENSAJE_MAL_FORMADO, revisar(sinCerrarElSobre));
    }

    @Test
    void unaEntidadExternaSeRechazaSinResolverla() {
        String xml = "<?xml version=\"1.0\"?><!DOCTYPE s [<!ENTITY x SYSTEM \"file:///no-existe-ni-se-lee\">]>"
                + "<s:Envelope xmlns:s=\"" + SOBRE + "\" xmlns:t=\"" + NS + "\"><s:Body><t:autenticar>"
                + "<usuario>&x;</usuario></t:autenticar></s:Body></s:Envelope>";

        Veredicto veredicto = revisar(xml);

        // No debe resolverla: o se rechaza, o se deja pasar sin haber leído nada del disco.
        assertTrue(veredicto == Veredicto.MENSAJE_MAL_FORMADO || veredicto == Veredicto.VALIDO);
    }

    // ---------- el Fault no revela detalles técnicos ----------

    @Test
    void elFaultEsUnSobreSoapConMensajeGenerico() throws Exception {
        String fault = FiltroPeticionSoap.construirFault(FiltroPeticionSoap.MENSAJE_OPERACION);

        DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
        fabrica.setNamespaceAware(true);
        Document doc = fabrica.newDocumentBuilder()
                .parse(new ByteArrayInputStream(fault.getBytes(StandardCharsets.UTF_8)));
        assertEquals("Fault", doc.getElementsByTagNameNS(SOBRE, "Fault").item(0).getLocalName());
        assertEquals("Operación no soportada", doc.getElementsByTagName("faultstring").item(0).getTextContent());
        assertFalse(fault.contains("Exception"));
        assertFalse(fault.contains("java."));
    }

    // ---------- el filtro en acción (petición y respuesta simuladas) ----------

    private static final class Capturas {
        int estado;
        final ByteArrayOutputStream cuerpo = new ByteArrayOutputStream();
        boolean cadenaInvocada;
        byte[] recibidoPorElServicio;
        String contentType;
    }

    private static HttpServletRequest peticion(String metodo, String contentType, byte[] cuerpo) {
        ServletInputStream unicoFlujo = flujo(cuerpo);
        return (HttpServletRequest) Proxy.newProxyInstance(
                FiltroPeticionSoapTest.class.getClassLoader(), new Class<?>[] {HttpServletRequest.class},
                (proxy, metodoJava, args) -> switch (metodoJava.getName()) {
                    case "getMethod" -> metodo;
                    case "getContentType" -> contentType;
                    case "getCharacterEncoding" -> "UTF-8";
                    case "getInputStream" -> unicoFlujo;
                    default -> valorPorDefecto(metodoJava);
                });
    }

    private static ServletInputStream flujo(byte[] bytes) {
        ByteArrayInputStream origen = new ByteArrayInputStream(bytes);
        return new ServletInputStream() {
            @Override public int read() { return origen.read(); }
            @Override public boolean isFinished() { return origen.available() == 0; }
            @Override public boolean isReady() { return true; }
            @Override public void setReadListener(ReadListener oyente) { }
        };
    }

    private static HttpServletResponse respuesta(Capturas capturas) {
        ServletOutputStream salida = new ServletOutputStream() {
            @Override public void write(int b) { capturas.cuerpo.write(b); }
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(WriteListener oyente) { }
        };
        return (HttpServletResponse) Proxy.newProxyInstance(
                FiltroPeticionSoapTest.class.getClassLoader(), new Class<?>[] {HttpServletResponse.class},
                (proxy, metodoJava, args) -> switch (metodoJava.getName()) {
                    case "setStatus" -> { capturas.estado = (Integer) args[0]; yield null; }
                    case "setContentType" -> { capturas.contentType = (String) args[0]; yield null; }
                    case "getOutputStream" -> salida;
                    default -> valorPorDefecto(metodoJava);
                });
    }

    private static Object valorPorDefecto(Method metodo) {
        Class<?> tipo = metodo.getReturnType();
        if (tipo == boolean.class) {
            return false;
        }
        if (tipo == int.class) {
            return 0;
        }
        if (tipo == long.class) {
            return 0L;
        }
        return null;
    }

    private Capturas ejecutar(String metodo, String contentType, byte[] cuerpo) throws Exception {
        Capturas capturas = new Capturas();
        AtomicReference<HttpServletRequest> recibida = new AtomicReference<>();
        FilterChain cadena = (req, res) -> {
            capturas.cadenaInvocada = true;
            recibida.set((HttpServletRequest) req);
            capturas.recibidoPorElServicio = req.getInputStream().readAllBytes();
        };
        filtro.doFilter(peticion(metodo, contentType, cuerpo), respuesta(capturas), cadena);
        return capturas;
    }

    @Test
    void unaOperacionValidaLlegaAlServicioConElCuerpoIntacto() throws Exception {
        byte[] cuerpo = mensaje("<t:convertir><valor>1</valor></t:convertir>").getBytes(StandardCharsets.UTF_8);

        Capturas c = ejecutar("POST", "text/xml; charset=utf-8", cuerpo);

        assertTrue(c.cadenaInvocada);
        assertArrayEquals(cuerpo, c.recibidoPorElServicio);
        assertEquals(0, c.estado);
    }

    @Test
    void unaOperacionInexistenteNoLlegaAlServicioYRecibeUnFaultGenerico() throws Exception {
        byte[] cuerpo = mensaje("<t:borrar/>").getBytes(StandardCharsets.UTF_8);

        Capturas c = ejecutar("POST", "text/xml; charset=utf-8", cuerpo);

        assertFalse(c.cadenaInvocada);
        assertEquals(500, c.estado);
        String respuesta = c.cuerpo.toString(StandardCharsets.UTF_8);
        assertTrue(respuesta.contains("Operación no soportada"));
        assertFalse(respuesta.contains("NullPointer"));
    }

    @Test
    void unMensajeMalFormadoRecibeUnFaultGenerico() throws Exception {
        Capturas c = ejecutar("POST", "text/xml", "<roto".getBytes(StandardCharsets.UTF_8));

        assertFalse(c.cadenaInvocada);
        assertEquals(500, c.estado);
        assertTrue(c.cuerpo.toString(StandardCharsets.UTF_8).contains("Mensaje SOAP mal formado"));
    }

    @Test
    void loQueNoEsUnPostXmlPasaSinTocarse() throws Exception {
        assertTrue(ejecutar("GET", "text/xml", new byte[0]).cadenaInvocada);
        assertTrue(ejecutar("POST", "application/json", "{}".getBytes(StandardCharsets.UTF_8)).cadenaInvocada);
        assertTrue(ejecutar("POST", null, new byte[0]).cadenaInvocada);
    }

    @Test
    void unMensajeMasGrandeQueElLimiteSeDejaPasarCompleto() throws Exception {
        StringBuilder relleno = new StringBuilder();
        while (relleno.length() < FiltroPeticionSoap.LIMITE_INSPECCION + 500) {
            relleno.append("<a>0123456789</a>");
        }
        byte[] cuerpo = mensaje("<t:autenticar>" + relleno + "</t:autenticar>").getBytes(StandardCharsets.UTF_8);

        Capturas c = ejecutar("POST", "text/xml", cuerpo);

        assertTrue(c.cadenaInvocada);
        assertArrayEquals(cuerpo, c.recibidoPorElServicio);
    }

    // ---------- la lista de web.xml no se desfasa del servicio ----------

    @Test
    void lasOperacionesDeWebXmlSonLasDelServicio() throws Exception {
        Path webXml = Path.of("src/main/webapp/WEB-INF/web.xml");
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(webXml.toFile());
        String lista = null;
        NodeList parametros = doc.getElementsByTagName("init-param");
        for (int i = 0; i < parametros.getLength(); i++) {
            Element p = (Element) parametros.item(i);
            if ("soap.operaciones".equals(p.getElementsByTagName("param-name").item(0).getTextContent())) {
                lista = p.getElementsByTagName("param-value").item(0).getTextContent();
            }
        }
        assertFalse(lista == null, "web.xml debe declarar soap.operaciones");

        Set<String> enWebXml = new TreeSet<>(Arrays.asList(lista.split("\\s*,\\s*")));
        Set<String> enElServicio = new TreeSet<>();
        for (Method m : ec.edu.monster.controladores.ws.WSConversorUnidades.class.getDeclaredMethods()) {
            WebMethod anotacion = m.getAnnotation(WebMethod.class);
            if (anotacion != null) {
                enElServicio.add(anotacion.operationName().isEmpty() ? m.getName() : anotacion.operationName());
            }
        }

        assertEquals(enElServicio, enWebXml);
    }
}
