package ec.edu.monster.vista;

import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.util.EntradaUtil;
import ec.edu.monster.ws.ServicioNoDisponibleException;
import java.io.PrintStream;

/**
 * Menú principal: se muestra mientras haya sesión iniciada.
 */
public class MenuVista {

    private static final int SALIR = 0;
    private static final int CATALOGO = 1;
    private static final int CONVERTIR = 2;
    private static final int CERRAR_SESION = 3;

    private final LoginControlador loginControlador;
    private final CatalogoVista catalogoVista;
    private final ConversionVista conversionVista;
    private final EntradaUtil entrada;
    private final PrintStream salida;

    public MenuVista(
            LoginControlador loginControlador,
            CatalogoVista catalogoVista,
            ConversionVista conversionVista,
            EntradaUtil entrada,
            PrintStream salida) {
        this.loginControlador = loginControlador;
        this.catalogoVista = catalogoVista;
        this.conversionVista = conversionVista;
        this.entrada = entrada;
        this.salida = salida;
    }

    /**
     * Atiende el menú hasta que la sesión termine o el usuario salga.
     *
     * @return {@code true} si el usuario eligió salir del programa;
     *         {@code false} si solo terminó la sesión (hay que volver al inicio de sesión)
     */
    public boolean ejecutar() {
        while (loginControlador.haySesionActiva()) {
            salida.println();
            salida.println("=== CONVERSOR DE UNIDADES MONSTER ===");
            salida.println("  1. Consultar catálogo de unidades");
            salida.println("  2. Realizar conversión");
            salida.println("  3. Cerrar sesión");
            salida.println("  0. Salir");
            int opcion = entrada.leerOpcion("Opción", SALIR, CERRAR_SESION);
            if (opcion == SALIR) {
                return true;
            }
            atender(opcion);
        }
        return false;
    }

    private void atender(int opcion) {
        try {
            switch (opcion) {
                case CATALOGO -> catalogoVista.mostrarCatalogo();
                case CONVERTIR -> conversionVista.convertir();
                case CERRAR_SESION -> {
                    loginControlador.cerrarSesion();
                    salida.println("Sesión cerrada.");
                    return;
                }
                default -> throw new IllegalStateException("Opción no contemplada: " + opcion);
            }
        } catch (ServicioNoDisponibleException e) {
            salida.println(e.getMessage());
            salida.println("Verifique que el servidor esté encendido e intente de nuevo.");
            return;
        }
        if (!loginControlador.haySesionActiva()) {
            salida.println("Debe autenticarse de nuevo para continuar.");
        }
    }
}
