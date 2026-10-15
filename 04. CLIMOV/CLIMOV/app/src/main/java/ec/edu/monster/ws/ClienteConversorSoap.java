package ec.edu.monster.ws;

import ec.edu.monster.modelo.Conversion;
import ec.edu.monster.util.SoapXml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.util.List;

/** Cliente SOAP 1.1 para las cuatro operaciones publicadas por WSConversorUnidades. */
public final class ClienteConversorSoap {
    private final String endpoint;
    private final SoapTransport transport;

    public ClienteConversorSoap(String endpoint) {
        this(endpoint, new HttpSoapTransport());
    }

    /** Constructor con transporte sustituible para probar el protocolo sin red externa. */
    public ClienteConversorSoap(String endpoint, SoapTransport transport) {
        if (endpoint == null || endpoint.trim().isEmpty()) {
            throw new IllegalArgumentException("El endpoint SOAP es obligatorio.");
        }
        if (transport == null) throw new IllegalArgumentException("El transporte SOAP es obligatorio.");
        this.endpoint = endpoint;
        this.transport = transport;
    }

    public String autenticar(String usuario, String password) throws Exception {
        Element e = invoke("autenticar", "<usuario>" + SoapXml.escape(usuario) + "</usuario><password>"
                + SoapXml.escape(password) + "</password>");
        String estado = SoapXml.text(e, "estado");
        String token = SoapXml.text(e, "token");
        if (!"OK".equalsIgnoreCase(estado) || token.isEmpty())
            throw new IllegalStateException(message(e, "Credenciales inválidas."));
        return token;
    }

    public List<String> categorias(String token) throws Exception {
        Element e = invoke("obtenerCategorias", tokenField(token));
        ensureOk(e);
        return SoapXml.directTexts(e, "categorias");
    }

    public List<String> unidades(String token, String categoria) throws Exception {
        Element e = invoke("obtenerUnidades", tokenField(token) + "<categoria>"
                + SoapXml.escape(categoria) + "</categoria>");
        ensureOk(e);
        return SoapXml.directTexts(e, "unidades");
    }

    public String convertir(String token, Conversion c) throws Exception {
        String fields = tokenField(token) + "<categoria>" + SoapXml.escape(c.getCategoria()) + "</categoria>"
                + "<unidadOrigen>" + SoapXml.escape(c.getUnidadOrigen()) + "</unidadOrigen>"
                + "<unidadDestino>" + SoapXml.escape(c.getUnidadDestino()) + "</unidadDestino>"
                + "<valor>" + c.getValor() + "</valor>";
        Element e = invoke("convertir", fields);
        ensureOk(e);
        String result = SoapXml.text(e, "resultado");
        if (result.isEmpty()) throw new IllegalStateException(message(e, "El servidor no devolvió resultado."));
        return result;
    }

    private static String tokenField(String token) {
        return "<token>" + SoapXml.escape(token) + "</token>";
    }

    private static void ensureOk(Element e) {
        if (!"OK".equalsIgnoreCase(SoapXml.text(e, "estado")))
            throw new IllegalStateException(message(e, "El servidor rechazó la operación."));
    }

    private static String message(Element e, String fallback) {
        String message = SoapXml.text(e, "mensaje");
        return message.isEmpty() ? fallback : message;
    }

    private Element invoke(String operation, String fields) throws Exception {
        String body = SoapXml.envelope(operation, fields);
        SoapTransport.Response http = transport.post(endpoint, body);
        Document doc = SoapXml.parse(http.getBody());
        Element root = doc.getDocumentElement();
        Element fault = SoapXml.first(root, "Fault");
        if (fault != null) throw new IllegalStateException(SoapXml.text(fault, "faultstring"));
        if (http.getStatusCode() < 200 || http.getStatusCode() >= 300)
            throw new IllegalStateException("Respuesta HTTP " + http.getStatusCode());
        Element response = SoapXml.first(root, operation + "Response");
        Element result = response == null ? null : SoapXml.first(response, "return");
        if (result == null) throw new IllegalStateException("Respuesta SOAP incompleta para " + operation);
        return result;
    }
}
