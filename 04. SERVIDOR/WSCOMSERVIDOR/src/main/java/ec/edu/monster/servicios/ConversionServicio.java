package ec.edu.monster.servicios;

import ec.edu.monster.modelos.Categoria;
import ec.edu.monster.modelos.CodigoRespuesta;
import ec.edu.monster.modelos.RespuestaConversion;
import ec.edu.monster.modelos.SolicitudConversion;
import ec.edu.monster.modelos.Unidad;
import ec.edu.monster.servicios.conversores.ConversorLineal;
import ec.edu.monster.servicios.conversores.ConversorTemperatura;
import ec.edu.monster.servicios.conversores.IConversor;
import ec.edu.monster.utilidades.Textos;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ConversionServicio {

    private final CatalogoServicio catalogoServicio;
    private final Map<String, IConversor> conversores;

    public ConversionServicio(CatalogoServicio catalogoServicio) {
        this(catalogoServicio, conversoresPorDefecto());
    }

    public ConversionServicio(CatalogoServicio catalogoServicio, Map<String, IConversor> conversores) {
        this.catalogoServicio = Objects.requireNonNull(catalogoServicio, "El catálogo es obligatorio");
        Map<String, IConversor> indice = new HashMap<>();
        conversores.forEach((categoria, conversor) -> indice.put(Textos.normalizar(categoria), conversor));
        for (String categoria : catalogoServicio.obtenerCategorias()) {
            if (!indice.containsKey(Textos.normalizar(categoria))) {
                throw new IllegalStateException("No hay conversor para la categoría: " + categoria);
            }
        }
        this.conversores = Map.copyOf(indice);
    }

    /**
     * Resultado de resolver una solicitud contra el catálogo: o el código del error, o la
     * categoría, las unidades y el conversor con los que se puede ejecutar. Así validar y
     * convertir comparten una sola búsqueda y no puede haber una unidad válida que luego no
     * se encuentre.
     */
    private record Resolucion(
            CodigoRespuesta codigo, Categoria categoria, Unidad origen, Unidad destino, IConversor conversor) {

        static Resolucion error(CodigoRespuesta codigo) {
            return new Resolucion(codigo, null, null, null, null);
        }

        boolean valida() {
            return codigo == CodigoRespuesta.OK_200;
        }
    }

    public CodigoRespuesta validar(SolicitudConversion solicitud) {
        return resolver(solicitud).codigo();
    }

    /**
     * @param solicitud solicitud a convertir
     * @return la respuesta con el resultado, o con el código del error
     */
    public RespuestaConversion convertir(SolicitudConversion solicitud) {
        Resolucion resolucion = resolver(solicitud);
        if (!resolucion.valida()) {
            return FabricaRespuestas.conversionFallida(resolucion.codigo());
        }
        Unidad origen = resolucion.origen();
        Unidad destino = resolucion.destino();

        // Convertir una unidad a sí misma es la identidad. Se resuelve aquí y no en la estrategia:
        // pasar por Celsius (o por el factor base) pierde precisión (0.1 K -> 0.10000000000002274).
        double resultado = mismaUnidad(origen, destino)
                ? solicitud.getValor()
                : resolucion.conversor().convertir(solicitud.getValor(), origen, destino);
        if (!Double.isFinite(resultado)) {
            return FabricaRespuestas.conversionFallida(CodigoRespuesta.DATA_001);
        }
        return FabricaRespuestas.conversionExitosa(resultado, origen.getNombre(), destino.getNombre());
    }

    /** Orden de las reglas: datos, categoría, unidad de origen, unidad de destino, valor posible. */
    private Resolucion resolver(SolicitudConversion solicitud) {
        if (solicitud == null
                || Textos.estaVacio(solicitud.getCategoria())
                || Textos.estaVacio(solicitud.getUnidadOrigen())
                || Textos.estaVacio(solicitud.getUnidadDestino())
                // NaN e Infinity son valores válidos de xsd:double, pero no de una medida.
                || solicitud.getValor() == null
                || !Double.isFinite(solicitud.getValor())) {
            return Resolucion.error(CodigoRespuesta.DATA_001);
        }
        Categoria categoria = catalogoServicio.buscarCategoria(solicitud.getCategoria()).orElse(null);
        if (categoria == null) {
            return Resolucion.error(CodigoRespuesta.DATA_001);
        }
        Unidad origen = catalogoServicio.buscarUnidad(categoria.getNombre(), solicitud.getUnidadOrigen()).orElse(null);
        if (origen == null) {
            return Resolucion.error(codigoUnidadNoEncontrada(solicitud.getUnidadOrigen(), CodigoRespuesta.UNIT_001));
        }
        Unidad destino = catalogoServicio.buscarUnidad(categoria.getNombre(), solicitud.getUnidadDestino()).orElse(null);
        if (destino == null) {
            return Resolucion.error(codigoUnidadNoEncontrada(solicitud.getUnidadDestino(), CodigoRespuesta.UNIT_002));
        }
        IConversor conversor = conversores.get(Textos.normalizar(categoria.getNombre()));
        if (!conversor.admite(solicitud.getValor(), origen)) {
            return Resolucion.error(CodigoRespuesta.DATA_001);
        }
        return new Resolucion(CodigoRespuesta.OK_200, categoria, origen, destino, conversor);
    }

    private static boolean mismaUnidad(Unidad origen, Unidad destino) {
        return Textos.normalizar(origen.getNombre()).equals(Textos.normalizar(destino.getNombre()));
    }

    /** Una unidad que existe en otra categoría es un dato inconsistente; si no existe, no está soportada. */
    private CodigoRespuesta codigoUnidadNoEncontrada(String unidad, CodigoRespuesta codigoNoSoportada) {
        return catalogoServicio.buscarCategoriaDeUnidad(unidad).isPresent()
                ? CodigoRespuesta.DATA_001
                : codigoNoSoportada;
    }

    private static Map<String, IConversor> conversoresPorDefecto() {
        IConversor lineal = new ConversorLineal();
        return Map.of(
                CatalogoServicio.LONGITUD, lineal,
                CatalogoServicio.MASA, lineal,
                CatalogoServicio.TEMPERATURA, new ConversorTemperatura());
    }
}
