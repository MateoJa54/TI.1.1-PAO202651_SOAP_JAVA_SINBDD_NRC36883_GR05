package ec.edu.monster.utilidades;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class Textos {

    private static final Pattern MARCAS_DIACRITICAS = Pattern.compile("\\p{M}+");

    private Textos() {
    }

    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String descompuesto = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD);
        return MARCAS_DIACRITICAS.matcher(descompuesto).replaceAll("").toLowerCase(Locale.ROOT);
    }

    public static boolean estaVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
