package ec.edu.monster.util;

import java.io.Console;
import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Lee y valida lo que el usuario escribe en la consola.
 *
 * <p>Recibe los flujos de entrada y salida por constructor para que las
 * vistas se puedan probar con texto preparado, sin teclado.
 */
public class EntradaUtil {

    /** Dígitos con punto o coma decimal y signo opcional: 12 | -3.5 | 0,25 | 1e3 */
    private static final Pattern NUMERO_DECIMAL =
            Pattern.compile("[+-]?(\\d+([.,]\\d*)?|[.,]\\d+)([eE][+-]?\\d+)?");
    private static final Pattern NUMERO_ENTERO = Pattern.compile("\\d{1,9}");
    private static final MathContext CIFRAS_AL_MOSTRAR = new MathContext(12);
    private static final double LIMITE_NOTACION_GRANDE = 1e15;
    private static final double LIMITE_NOTACION_PEQUENA = 1e-6;

    private final Scanner lector;
    private final PrintStream salida;
    private final Console consola;

    /** Para pruebas: nunca usa la consola real. */
    public EntradaUtil(InputStream entrada, PrintStream salida) {
        this(entrada, salida, StandardCharsets.UTF_8, null);
    }

    /**
     * @param codificacion codificación en la que llega lo que se escribe en el teclado
     * @param consola consola real para leer la contraseña sin mostrarla;
     *                {@code null} si no hay (por ejemplo, dentro de NetBeans)
     */
    public EntradaUtil(InputStream entrada, PrintStream salida, Charset codificacion, Console consola) {
        this.lector = new Scanner(entrada, codificacion);
        this.salida = salida;
        this.consola = consola;
    }

    /**
     * @return la línea escrita, sin espacios al inicio ni al final (puede ser vacía)
     * @throws FinDeEntradaException si la entrada se cerró
     */
    public String leerTexto(String mensaje) {
        return leerLinea(mensaje).trim();
    }

    private String leerLinea(String mensaje) {
        salida.print(mensaje + ": ");
        salida.flush();
        if (!lector.hasNextLine()) {
            throw new FinDeEntradaException();
        }
        return lector.nextLine();
    }

    /**
     * Lee la contraseña sin mostrarla cuando hay consola real. No se le
     * quitan los espacios: en una contraseña son caracteres válidos.
     */
    public String leerPassword(String mensaje) {
        if (consola == null) {
            return leerLinea(mensaje);
        }
        char[] escrito = consola.readPassword("%s: ", mensaje);
        if (escrito == null) {
            throw new FinDeEntradaException();
        }
        return new String(escrito);
    }

    /** Repite la pregunta hasta recibir un número decimal válido. */
    public double leerDecimal(String mensaje) {
        while (true) {
            Optional<Double> numero = interpretarDecimal(leerTexto(mensaje));
            if (numero.isPresent()) {
                return numero.get();
            }
            salida.println("  Valor no válido. Escriba un número, por ejemplo 12.5");
        }
    }

    /** Repite la pregunta hasta recibir un entero entre {@code minimo} y {@code maximo}. */
    public int leerOpcion(String mensaje, int minimo, int maximo) {
        while (true) {
            String texto = leerTexto(mensaje);
            if (NUMERO_ENTERO.matcher(texto).matches()) {
                int opcion = Integer.parseInt(texto);
                if (opcion >= minimo && opcion <= maximo) {
                    return opcion;
                }
            }
            salida.println("  Opción no válida. Escriba un número del " + minimo + " al " + maximo);
        }
    }

    /**
     * Muestra una lista numerada y deja elegir un elemento.
     *
     * @return el elemento elegido, o vacío si el usuario eligió 0 (cancelar)
     */
    public Optional<String> elegir(String titulo, List<String> opciones) {
        salida.println();
        salida.println(titulo);
        for (int i = 0; i < opciones.size(); i++) {
            salida.println("  " + (i + 1) + ". " + opciones.get(i));
        }
        salida.println("  0. Cancelar");
        int opcion = leerOpcion("Opción", 0, opciones.size());
        return opcion == 0 ? Optional.empty() : Optional.of(opciones.get(opcion - 1));
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
