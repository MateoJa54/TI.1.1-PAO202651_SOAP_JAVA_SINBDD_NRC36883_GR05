package ec.edu.monster.ws;

/** Abstracción del transporte HTTP para probar el cliente SOAP sin depender de Payara. */
public interface SoapTransport {
    Response post(String endpoint, String requestXml) throws Exception;

    final class Response {
        private final int statusCode;
        private final String body;

        public Response(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        public int getStatusCode() { return statusCode; }
        public String getBody() { return body; }
    }
}
