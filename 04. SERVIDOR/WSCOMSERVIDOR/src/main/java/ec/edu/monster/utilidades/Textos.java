package ec.edu.monster.utilidades;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class Textos {

    private static final Pattern MARCAS_DIACRITICAS = Pattern.compile("\\p{M}+");

    // String.trim() solo quita hasta U+0020: no recorta el espacio duro (U+00A0) que llega al
    // copiar y pegar desde una página web. \p{Z} cubre todos los separadores Unicode.
    private static final Pattern ESPACIOS_EXTREMOS = Pattern.compile("^[\\s\\p{Z}]+|[\\s\\p{Z}]+$");

    private Textos() {
    }

    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String recortado = ESPACIOS_EXTREMOS.matcher(texto).replaceAll("");
        String descompuesto = Normalizer.normalize(recortado, Normalizer.Form.NFD);
        return MARCAS_DIACRITICAS.matcher(descompuesto).replaceAll("").toLowerCase(Locale.ROOT);
    }

    public static boolean estaVacio(String texto) {
        return texto == null || ESPACIOS_EXTREMOS.matcher(texto).replaceAll("").isEmpty();
    }
}
