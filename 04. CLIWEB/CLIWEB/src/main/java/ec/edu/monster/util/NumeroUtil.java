package ec.edu.monster.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Interpreta y presenta números escritos por el usuario en el cliente de
 * escritorio. Son las mismas reglas que usa el cliente de consola, reunidas
 * aquí porque las vistas Swing no leen de un flujo de texto.
 */
public final class NumeroUtil {

    /** Dígitos con punto o coma decimal y signo opcional: 12 | -3.5 | 0,25 | 1e3 */
    private static final Pattern NUMERO_DECIMAL =
            Pattern.compile("[+-]?(\\d+([.,]\\d*)?|[.,]\\d+)([eE][+-]?\\d+)?");

    private static final MathContext CIFRAS_AL_MOSTRAR = new MathContext(12);
    private static final double LIMITE_NOTACION_GRANDE = 1e15;
    private static final double LIMITE_NOTACION_PEQUENA = 1e-6;

    private NumeroUtil() {
    }

    /**
     * Convierte un texto en número. Acepta punto o coma decimal.
     *
     * @return el número, o vacío si el texto no es un número finito
     */
    public static Optional<Double> interpretarDecimal(String texto) {
        if (texto == null || !NUMERO_DECIMAL.matcher(texto.trim()).matches()) {
            return Optional.empty();
        }
        double numero = Double.parseDouble(texto.trim().replace(',', '.'));
        return Double.isFinite(numero) ? Optional.of(numero) : Optional.empty();
    }

    /**
     * @return el número con 12 cifras significativas y sin ceros sobrantes:
     *         1609.344, 0.3048, 100. Los muy grandes o muy pequeños se muestran
     *         en notación científica (1E+20) para que no ocupen toda la pantalla
     */
    public static String formatear(double numero) {
        if (numero == 0) {
            return "0";
        }
        BigDecimal redondeado = new BigDecimal(numero).round(CIFRAS_AL_MOSTRAR).stripTrailingZeros();
        double magnitud = Math.abs(numero);
        return (magnitud >= LIMITE_NOTACION_GRANDE || magnitud < LIMITE_NOTACION_PEQUENA)
                ? redondeado.toString()
                : redondeado.toPlainString();
    }
}
