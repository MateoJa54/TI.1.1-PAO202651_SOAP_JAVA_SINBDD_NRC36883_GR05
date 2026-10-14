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

    public CodigoRespuesta validar(SolicitudConversion solicitud) {
        if (solicitud == null
                || Textos.estaVacio(solicitud.getCategoria())
                || Textos.estaVacio(solicitud.getUnidadOrigen())
                || Textos.estaVacio(solicitud.getUnidadDestino())
                // NaN e Infinity son valores válidos de xsd:double, pero no de una medida.
                || solicitud.getValor() == null
                || !Double.isFinite(solicitud.getValor())) {
            return CodigoRespuesta.DATA_001;
        }
        if (!catalogoServicio.existeCategoria(solicitud.getCategoria())) {
            return CodigoRespuesta.DATA_001;
        }
        CodigoRespuesta origen = validarUnidad(
                solicitud.getCategoria(), solicitud.getUnidadOrigen(), CodigoRespuesta.UNIT_001);
        if (origen != CodigoRespuesta.OK_200) {
            return origen;
        }
        return validarUnidad(
                solicitud.getCategoria(), solicitud.getUnidadDestino(), CodigoRespuesta.UNIT_002);
    }

    /**
     * @param solicitud solicitud a convertir
     * @return la respuesta con el resultado, o con el código del error
     */
    public RespuestaConversion convertir(SolicitudConversion solicitud) {
        CodigoRespuesta codigo = validar(solicitud);
        if (codigo != CodigoRespuesta.OK_200) {
            return FabricaRespuestas.conversionFallida(codigo);
        }
        Categoria categoria = catalogoServicio.buscarCategoria(solicitud.getCategoria()).orElseThrow();
        Unidad origen = catalogoServicio
                .buscarUnidad(solicitud.getCategoria(), solicitud.getUnidadOrigen()).orElseThrow();
        Unidad destino = catalogoServicio
                .buscarUnidad(solicitud.getCategoria(), solicitud.getUnidadDestino()).orElseThrow();

        IConversor conversor = conversores.get(Textos.normalizar(categoria.getNombre()));
        double resultado = conversor.convertir(solicitud.getValor(), origen, destino);
        if (!Double.isFinite(resultado)) {
            return FabricaRespuestas.conversionFallida(CodigoRespuesta.DATA_001);
        }
        return FabricaRespuestas.conversionExitosa(resultado, origen.getNombre(), destino.getNombre());
    }

    private CodigoRespuesta validarUnidad(
            String categoria, String unidad, CodigoRespuesta codigoNoSoportada) {
        if (catalogoServicio.buscarUnidad(categoria, unidad).isPresent()) {
            return CodigoRespuesta.OK_200;
        }
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
