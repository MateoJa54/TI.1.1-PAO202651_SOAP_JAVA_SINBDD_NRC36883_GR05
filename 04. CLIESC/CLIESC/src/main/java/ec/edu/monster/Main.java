package ec.edu.monster;

import ec.edu.monster.vista.Tema;
import ec.edu.monster.vista.VentanaPrincipal;
import ec.edu.monster.ws.ClienteWSConversor;
import ec.edu.monster.ws.IClienteConversor;
import java.awt.EventQueue;
import javax.swing.JOptionPane;

/**
 * Punto de arranque del cliente de escritorio. Es el único lugar que conoce
 * las clases concretas: crea el adaptador SOAP y la ventana principal.
 *
 * <p>La dirección del servicio se toma, en este orden, de: el primer argumento
 * del programa, la propiedad {@code -Dws.url}, la variable de entorno
 * {@code WS_URL} o, si no hay ninguna, el servidor local. Una vez abierta la
 * ventana, el usuario puede cambiarla escribiéndola en la pantalla de acceso.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        String url = resolverUrl(args);
        EventQueue.invokeLater(() -> arrancar(url));
    }

    /** Crea el adaptador y muestra la ventana. Se ejecuta en el hilo de Swing. */
    static void arrancar(String url) {
        Tema.instalar();
        IClienteConversor cliente;
        try {
            cliente = new ClienteWSConversor(url);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null,
                    e.getMessage() + "\nEjemplo: " + ClienteWSConversor.URL_POR_DEFECTO,
                    "Dirección del servicio no válida", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new VentanaPrincipal(cliente, url).setVisible(true);
    }

    public static String resolverUrl(String[] args) {
        if (args != null && args.length > 0 && !args[0].isBlank()) {
            return args[0].trim();
        }
        String url = System.getProperty("ws.url");
        if (url == null || url.isBlank()) {
            url = System.getenv("WS_URL");
        }
        return (url == null || url.isBlank()) ? ClienteWSConversor.URL_POR_DEFECTO : url.trim();
    }
}
