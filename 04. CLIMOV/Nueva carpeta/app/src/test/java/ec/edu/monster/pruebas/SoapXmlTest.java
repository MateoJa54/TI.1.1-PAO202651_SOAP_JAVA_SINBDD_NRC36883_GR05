package ec.edu.monster.pruebas;

import ec.edu.monster.util.SoapXml;
import org.junit.Test;
import org.w3c.dom.Element;
import java.util.Arrays;
import static org.junit.Assert.*;

public class SoapXmlTest {
    @Test public void readsNamespacedResponseValues() throws Exception {
        Element root = SoapXml.parse("<s:Envelope xmlns:s='urn:soap' xmlns:w='urn:ws'>"
                + "<s:Body><w:convertirResponse><return><estado> OK </estado>"
                + "<resultado>12.5</resultado></return></w:convertirResponse></s:Body></s:Envelope>")
                .getDocumentElement();
        Element response = SoapXml.first(root, "convertirResponse");
        Element result = SoapXml.first(response, "return");
        assertEquals("OK", SoapXml.text(result, "estado"));
        assertEquals("12.5", SoapXml.text(result, "resultado"));
    }

    @Test public void readsOnlyDirectRepeatedValues() throws Exception {
        Element root = SoapXml.parse("<return><estado>OK</estado><categorias>MASA</categorias>"
                + "<grupo><categorias>IGNORAR</categorias></grupo><categorias>LONGITUD</categorias></return>")
                .getDocumentElement();
        assertEquals(Arrays.asList("MASA", "LONGITUD"), SoapXml.directTexts(root, "categorias"));
    }

    @Test public void escapesXmlMetacharacters() {
        assertEquals("a&amp;b&lt;c&gt;&quot;d&apos;", SoapXml.escape("a&b<c>\"d'"));
        assertEquals("", SoapXml.escape(null));
    }

    @Test public void rejectsNullAndDoctypeDocuments() {
        assertThrows(IllegalArgumentException.class, () -> SoapXml.parse(null));
        assertThrows(IllegalArgumentException.class,
                () -> SoapXml.parse("<!DOCTYPE x [<!ENTITY e SYSTEM 'file:///secret'>]><x>&e;</x>"));
    }
}
