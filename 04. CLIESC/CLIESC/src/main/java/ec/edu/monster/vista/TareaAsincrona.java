package ec.edu.monster.vista;

import ec.edu.monster.ws.ServicioNoDisponibleException;
import java.awt.Component;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 * Ejecuta una llamada al servidor fuera del hilo de Swing, para que la ventana
 * no se congele mientras se espera la respuesta. Al terminar, entrega el
 * resultado (o el fallo) de vuelta en el hilo de Swing, que es el único que
 * puede tocar los componentes.
 */
public final class TareaAsincrona {

    private TareaAsincrona() {
    }

    /**
     * Ejecuta {@code trabajo} en segundo plano. Si sale bien llama a
     * {@code alExito}; si falla, muestra un cuadro de diálogo con el motivo.
     */
    public static <T> void ejecutar(Component duenio, Supplier<T> trabajo, Consumer<T> alExito) {
        ejecutar(duenio, trabajo, alExito, causa -> mostrarFallo(duenio, causa));
    }

    /**
     * Igual que el anterior, pero el fallo lo atiende {@code alError} en lugar
     * del cuadro de diálogo por defecto (por ejemplo, para volver a habilitar
     * un botón).
     */
    public static <T> void ejecutar(Component duenio, Supplier<T> trabajo,
            Consumer<T> alExito, Consumer<Throwable> alError) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() {
                return trabajo.get();
            }

            @Override
            protected void done() {
                try {
                    alExito.accept(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    alError.accept(e.getCause() == null ? e : e.getCause());
                } catch (RuntimeException e) {
                    alError.accept(e);
                }
            }
        }.execute();
    }

    /** Cuadro de diálogo de error con el mensaje adecuado para cada fallo. */
    public static void mostrarFallo(Component duenio, Throwable causa) {
        String mensaje = causa instanceof ServicioNoDisponibleException
                ? causa.getMessage() + "\nVerifique que el servidor esté encendido e intente de nuevo."
                : "Error inesperado: " + (causa == null ? "desconocido" : causa.getMessage());
        JOptionPane.showMessageDialog(duenio, mensaje, "Conversor de Unidades Monster", JOptionPane.ERROR_MESSAGE);
    }
}
