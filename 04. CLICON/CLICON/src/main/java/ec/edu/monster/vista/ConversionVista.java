package ec.edu.monster.vista;

import ec.edu.monster.controlador.CatalogoControlador;
import ec.edu.monster.controlador.ConversionControlador;
import ec.edu.monster.modelo.RespuestaCatalogo;
import ec.edu.monster.modelo.RespuestaConversion;
import ec.edu.monster.modelo.RespuestaUnidades;
import ec.edu.monster.modelo.SolicitudConversion;
import ec.edu.monster.util.EntradaUtil;
import ec.edu.monster.util.RespuestaUtil;
import java.io.PrintStream;
import java.util.Optional;

/**
 * Pantalla de conversión: el usuario elige categoría y unidades de las
 * listas que entrega el servidor, escribe el valor y ve el resultado.
 */
public class ConversionVista {

    private final ConversionControlador conversionControlador;
    private final CatalogoControlador catalogoControlador;
    private final EntradaUtil entrada;
    private final PrintStream salida;

    public ConversionVista(
            ConversionControlador conversionControlador,
            CatalogoControlador catalogoControlador,
            EntradaUtil entrada,
            PrintStream salida) {
        this.conversionControlador = conversionControlador;
        this.catalogoControlador = catalogoControlador;
        this.entrada = entrada;
        this.salida = salida;
    }

    public void convertir() {
        RespuestaCatalogo catalogo = catalogoControlador.obtenerCategorias();
        if (!RespuestaUtil.esExitosa(catalogo)) {
            salida.println("No se pudo consultar las categorías: " + RespuestaUtil.describirError(catalogo));
            return;
        }
        Optional<String> categoria = entrada.elegir("Categoría:", catalogo.getCategorias());
        if (categoria.isEmpty()) {
            return;
        }

        RespuestaUnidades unidades = catalogoControlador.obtenerUnidades(categoria.get());
        if (!RespuestaUtil.esExitosa(unidades)) {
            salida.println("No se pudo consultar las unidades: " + RespuestaUtil.describirError(unidades));
            return;
        }
        Optional<String> origen = entrada.elegir("Unidad de origen:", unidades.getUnidades());
        if (origen.isEmpty()) {
            return;
        }
        Optional<String> destino = entrada.elegir("Unidad de destino:", unidades.getUnidades());
        if (destino.isEmpty()) {
            return;
        }
        double valor = entrada.leerDecimal("Valor en " + origen.get());

        RespuestaConversion respuesta = conversionControlador.convertir(
                new SolicitudConversion(categoria.get(), origen.get(), destino.get(), valor));
        salida.println();
        if (RespuestaUtil.esExitosa(respuesta) && respuesta.getResultado() != null) {
            salida.println("Resultado: " + EntradaUtil.formatear(valor) + " " + respuesta.getUnidadOrigen()
                    + " = " + EntradaUtil.formatear(respuesta.getResultado()) + " " + respuesta.getUnidadDestino());
        } else {
            salida.println("No se pudo convertir: " + RespuestaUtil.describirError(respuesta));
        }
    }
}
