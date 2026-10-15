package ec.edu.monster.ws;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** Transporte HTTP de producción para el endpoint SOAP de Payara. */
public final class HttpSoapTransport implements SoapTransport {
    @Override public Response post(String endpoint, String requestXml) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        connection.setConnectTimeout(7000);
        connection.setReadTimeout(15000);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        connection.setRequestProperty("Accept", "text/xml");
        try {
            try (OutputStream out = connection.getOutputStream()) {
                out.write(requestXml.getBytes(StandardCharsets.UTF_8));
            }
            int status = connection.getResponseCode();
            InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
            if (stream == null) return new Response(status, "");
            String xml;
            try (InputStream in = stream; ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int count;
                while ((count = in.read(buffer)) != -1) bytes.write(buffer, 0, count);
                xml = bytes.toString(StandardCharsets.UTF_8.name());
            }
            return new Response(status, xml);
        } finally {
            connection.disconnect();
        }
    }
}
