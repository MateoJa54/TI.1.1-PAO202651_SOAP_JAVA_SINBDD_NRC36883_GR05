package ec.edu.monster.pruebas;

import ec.edu.monster.modelo.Conversion;
import ec.edu.monster.ws.ClienteConversorSoap;
import ec.edu.monster.ws.SoapTransport;
import org.junit.Test;
import java.util.Arrays;
import static org.junit.Assert.*;

/** Pruebas de contrato SOAP con un transporte falso y respuestas controladas. */
public class ClienteConversorSoapTest {
    private String responseXml = "";
    private String requestXml = "";
    private int responseStatus = 200;
    private final SoapTransport fakeTransport = (endpoint, request) -> {
        requestXml = request;
        return new SoapTransport.Response(responseStatus, responseXml);
    };
    private final ClienteConversorSoap cliente = new ClienteConversorSoap(
            "http://payara.test/WSCOMSERVIDOR/WSConversorUnidades", fakeTransport);

    @Test public void autenticaYEscapaLasCredencialesEnXml() throws Exception {
        responder("autenticarResponse", "<estado>OK</estado><token>token-abc</token>");
        assertEquals("token-abc", cliente.autenticar("mon&ster", "p<ass"));
        assertTrue(requestXml.contains("<usuario>mon&amp;ster</usuario>"));
        assertTrue(requestXml.contains("<password>p&lt;ass</password>"));
        assertTrue(requestXml.contains("<tns:autenticar xmlns:tns=\"http://ws.monster.edu.ec/\">"));
    }

    @Test public void obtieneCategoriasDesdeLosElementosRepetidos() throws Exception {
        responder("obtenerCategoriasResponse", "<estado>OK</estado><categorias>MASA</categorias>"
                + "<categorias>LONGITUD</categorias>");
        assertEquals(Arrays.asList("MASA", "LONGITUD"), cliente.categorias("token-1"));
        assertTrue(requestXml.contains("<token>token-1</token>"));
    }

    @Test public void obtieneUnidadesYEscapaLaCategoria() throws Exception {
        responder("obtenerUnidadesResponse", "<estado>OK</estado><unidades>metros</unidades>"
                + "<unidades>yardas</unidades>");
        assertEquals(Arrays.asList("metros", "yardas"), cliente.unidades("token-2", "masa & volumen"));
        assertTrue(requestXml.contains("<categoria>masa &amp; volumen</categoria>"));
    }

    @Test public void enviaLaConversionYDevuelveElResultado() throws Exception {
        responder("convertirResponse", "<estado>OK</estado><resultado>2.28084</resultado>");
        Conversion conversion = new Conversion("LONGITUD", "metros", "yardas", 0.7);
        assertEquals("2.28084", cliente.convertir("token-3", conversion));
        assertTrue(requestXml.contains("<unidadOrigen>metros</unidadOrigen>"));
        assertTrue(requestXml.contains("<unidadDestino>yardas</unidadDestino>"));
        assertTrue(requestXml.contains("<valor>0.7</valor>"));
    }

    @Test public void reportaErroresDeNegocioDelServicio() {
        responder("obtenerCategoriasResponse", "<estado>ERROR</estado><mensaje>Token expirado</mensaje>");
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> cliente.categorias("token-expirado"));
        assertEquals("Token expirado", error.getMessage());
    }

    @Test public void reportaFaultSoapAunqueLaRespuestaSeaHttp500() {
        responseStatus = 500;
        responseXml = "<soap:Envelope xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'>"
                + "<soap:Body><soap:Fault><faultstring>Servicio no disponible</faultstring>"
                + "</soap:Fault></soap:Body></soap:Envelope>";
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> cliente.categorias("token"));
        assertEquals("Servicio no disponible", error.getMessage());
    }

    @Test public void rechazaCodigoHttpDeErrorSinSoapFault() {
        responseStatus = 503;
        responseXml = "<soap:Envelope xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'/>";
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> cliente.categorias("token"));
        assertEquals("Respuesta HTTP 503", error.getMessage());
    }

    private void responder(String operationResponse, String resultFields) {
        responseStatus = 200;
        responseXml = "<soap:Envelope xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/' "
                + "xmlns:tns='http://ws.monster.edu.ec/'><soap:Body><tns:" + operationResponse
                + "><return>" + resultFields + "</return></tns:" + operationResponse
                + "></soap:Body></soap:Envelope>";
    }
}
