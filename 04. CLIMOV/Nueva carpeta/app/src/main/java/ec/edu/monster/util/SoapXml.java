package ec.edu.monster.util;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SoapXml {
    private SoapXml() { }

    public static String envelope(String operation, String fields) {
        if (operation == null || !operation.matches("[A-Za-z][A-Za-z0-9]*")) {
            throw new IllegalArgumentException("Nombre de operación SOAP inválido.");
        }
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><tns:"
                + operation + " xmlns:tns=\"http://ws.monster.edu.ec/\">" + (fields == null ? "" : fields)
                + "</tns:" + operation + "></soap:Body></soap:Envelope>";
    }

    public static Document parse(String xml) throws Exception {
        if (xml == null || xml.toUpperCase(Locale.ROOT).contains("<!DOCTYPE")) {
            throw new IllegalArgumentException("La respuesta XML está vacía o contiene un DOCTYPE no permitido.");
        }
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        f.setExpandEntityReferences(false);
        return f.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }
    public static Element first(Element parent, String name) {
        NodeList list = parent.getElementsByTagNameNS("*", name);
        return list.getLength() == 0 ? null : (Element) list.item(0);
    }
    public static String text(Element parent, String name) {
        Element e = first(parent, name);
        return e == null ? "" : e.getTextContent().trim();
    }
    public static List<String> directTexts(Element parent, String name) {
        List<String> values = new ArrayList<>();
        for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element && name.equals(n.getLocalName())) values.add(n.getTextContent().trim());
        }
        return values;
    }
    public static String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }
}
