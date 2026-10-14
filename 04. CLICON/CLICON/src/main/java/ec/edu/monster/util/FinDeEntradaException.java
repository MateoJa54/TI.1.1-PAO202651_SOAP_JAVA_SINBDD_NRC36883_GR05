package ec.edu.monster.util;

/**
 * La entrada se cerró (Ctrl+Z en Windows, Ctrl+D en Linux, o el final de un
 * archivo redirigido): ya no hay nada más que leer y el programa debe
 * terminar con orden.
 */
public class FinDeEntradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FinDeEntradaException() {
        super("La entrada se cerró");
    }
}
