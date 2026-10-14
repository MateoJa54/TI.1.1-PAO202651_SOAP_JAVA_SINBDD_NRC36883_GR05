package ec.edu.monster.vista;

import ec.edu.monster.controlador.CatalogoControlador;
import ec.edu.monster.controlador.ConversionControlador;
import ec.edu.monster.controlador.LoginControlador;

/**
 * Lo que las pantallas Swing necesitan de la ventana que las contiene, sin
 * conocerla. La implementa {@link VentanaPrincipal}.
 *
 * <p>Los paneles piden los controladores cada vez que los usan (no los
 * guardan) para que, si el usuario cambia de servidor, empiecen a trabajar
 * con la conexión nueva.
 */
public interface MediadorEscritorio {

    LoginControlador loginControlador();

    CatalogoControlador catalogoControlador();

    ConversionControlador conversionControlador();

    /** Dirección del servicio a la que está conectado el cliente. */
    String url();

    /**
     * Conecta a otra dirección y descarta la sesión actual.
     *
     * @throws IllegalArgumentException si la dirección no es válida
     */
    void conectar(String url);

    /** La autenticación fue correcta: hay que pasar a la pantalla principal. */
    void sesionIniciada();

    /** La sesión se cerró o el servidor la rechazó: hay que volver al acceso. */
    void sesionCerrada();

    /** Cierra el programa. */
    void salir();
}
