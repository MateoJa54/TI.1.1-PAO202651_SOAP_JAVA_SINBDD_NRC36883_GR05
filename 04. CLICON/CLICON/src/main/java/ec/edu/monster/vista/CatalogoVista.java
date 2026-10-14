package ec.edu.monster.vista;

import ec.edu.monster.controlador.CatalogoControlador;
import ec.edu.monster.modelo.RespuestaCatalogo;
import ec.edu.monster.modelo.RespuestaUnidades;
import ec.edu.monster.util.RespuestaUtil;
import java.io.PrintStream;

/**
 * Muestra el catálogo: cada categoría con sus unidades.
 */
public class CatalogoVista {

    private final CatalogoControlador controlador;
    private final PrintStream salida;

    public CatalogoVista(CatalogoControlador controlador, PrintStream salida) {
        this.controlador = controlador;
        this.salida = salida;
    }

    public void mostrarCatalogo() {
        RespuestaCatalogo catalogo = controlador.obtenerCategorias();
        if (!RespuestaUtil.esExitosa(catalogo)) {
            salida.println("No se pudo consultar el catálogo: " + RespuestaUtil.describirError(catalogo));
            return;
        }
        salida.println();
        salida.println("=== CATÁLOGO DE UNIDADES ===");
        if (catalogo.getCategorias().isEmpty()) {
            salida.println("El servidor no tiene categorías registradas.");
            return;
        }
        for (String categoria : catalogo.getCategorias()) {
            RespuestaUnidades unidades = controlador.obtenerUnidades(categoria);
            if (!RespuestaUtil.esExitosa(unidades)) {
                salida.println(categoria + ": " + RespuestaUtil.describirError(unidades));
                if (RespuestaUtil.requiereAutenticarse(unidades)) {
                    return;
                }
                continue;
            }
            salida.println(categoria + ": " + String.join(", ", unidades.getUnidades()));
        }
    }
}
