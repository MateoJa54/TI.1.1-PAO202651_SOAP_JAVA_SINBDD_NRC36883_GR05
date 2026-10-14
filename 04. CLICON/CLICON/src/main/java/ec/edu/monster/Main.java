package ec.edu.monster;

import ec.edu.monster.controlador.CatalogoControlador;
import ec.edu.monster.controlador.ConversionControlador;
import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.util.EntradaUtil;
import ec.edu.monster.util.FinDeEntradaException;
import ec.edu.monster.vista.CatalogoVista;
import ec.edu.monster.vista.ConversionVista;
import ec.edu.monster.vista.LoginVista;
import ec.edu.monster.vista.MenuVista;
import ec.edu.monster.ws.ClienteWSConversor;
import ec.edu.monster.ws.IClienteConversor;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

/**
 * Punto de arranque del cliente de consola. Es el único lugar que conoce las
 * clases concretas: crea cada objeto y se lo entrega a quien lo necesita
 * (inyección de dependencias por constructor).
 *
 * <p>La dirección del servicio se toma, en este orden, de: el primer
 * argumento del programa, la propiedad {@code -Dws.url}, la variable de
 * entorno {@code WS_URL} o, si no hay ninguna, el servidor local.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        String url = resolverUrl(args);
        System.out.println("Servidor: " + url);
        EntradaUtil entrada = new EntradaUtil(System.in, System.out, codificacionDelTeclado(), System.console());
        IClienteConversor cliente;
        try {
            cliente = new ClienteWSConversor(url);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            System.out.println("Ejemplo: " + ClienteWSConversor.URL_POR_DEFECTO);
            return;
        }
        ejecutar(cliente, entrada, System.out);
    }

    /**
     * Arma el cliente y lo ejecuta hasta que el usuario salga. Recibe sus
     * dependencias para poder probar el recorrido completo sin servidor ni teclado.
     */
    public static void ejecutar(IClienteConversor cliente, EntradaUtil entrada, PrintStream salida) {
        SesionModelo sesion = new SesionModelo();
        LoginControlador loginControlador = new LoginControlador(cliente, sesion);
        CatalogoControlador catalogoControlador = new CatalogoControlador(cliente, sesion);
        ConversionControlador conversionControlador = new ConversionControlador(cliente, sesion);

        LoginVista loginVista = new LoginVista(loginControlador, entrada, salida);
        MenuVista menuVista = new MenuVista(
                loginControlador,
                new CatalogoVista(catalogoControlador, salida),
                new ConversionVista(conversionControlador, catalogoControlador, entrada, salida),
                entrada,
                salida);
        try {
            boolean salir = false;
            while (!salir && loginVista.iniciar()) {
                salir = menuVista.ejecutar();
            }
        } catch (FinDeEntradaException e) {
            salida.println();
        }
        salida.println("Hasta pronto.");
    }

    /**
     * En Windows el teclado de la consola no llega en UTF-8; si se leyera con
     * la codificación equivocada, una "ñ" en la contraseña llegaría dañada.
     */
    private static Charset codificacionDelTeclado() {
        if (System.console() != null) {
            return System.console().charset();
        }
        String nombre = System.getProperty("stdin.encoding", System.getProperty("native.encoding"));
        try {
            return nombre == null ? Charset.defaultCharset() : Charset.forName(nombre);
        } catch (IllegalArgumentException e) {
            return Charset.defaultCharset();
        }
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
