package ec.edu.monster.servicios;

import ec.edu.monster.modelos.Categoria;
import ec.edu.monster.modelos.CodigoRespuesta;
import ec.edu.monster.modelos.RespuestaCatalogo;
import ec.edu.monster.modelos.RespuestaUnidades;
import ec.edu.monster.modelos.Unidad;
import ec.edu.monster.utilidades.Textos;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CatalogoServicio {

    public static final String LONGITUD = "LONGITUD";
    public static final String MASA = "MASA";
    public static final String TEMPERATURA = "TEMPERATURA";

    private static final double SIN_FACTOR = 1.0;

    private final Map<String, Categoria> categorias;

    public CatalogoServicio() {
        this(catalogoPorDefecto());
    }

    public CatalogoServicio(List<Categoria> categorias) {
        Map<String, Categoria> indice = new LinkedHashMap<>();
        for (Categoria categoria : categorias) {
            indice.put(Textos.normalizar(categoria.getNombre()), categoria);
        }
        this.categorias = Collections.unmodifiableMap(indice);
    }

    /** @return los nombres de las categorías, en el orden del catálogo */
    public List<String> obtenerCategorias() {
        return categorias.values().stream().map(Categoria::getNombre).toList();
    }

    public List<String> obtenerUnidades(String categoria) {
        return buscarCategoria(categoria)
                .map(encontrada -> encontrada.getUnidades().stream().map(Unidad::getNombre).toList())
                .orElse(List.of());
    }

    public RespuestaCatalogo consultarCategorias() {
        return FabricaRespuestas.catalogoExitoso(obtenerCategorias());
    }

    public RespuestaUnidades consultarUnidades(String categoria) {
        Optional<Categoria> encontrada = buscarCategoria(categoria);
        if (encontrada.isEmpty()) {
            return FabricaRespuestas.unidadesFallidas(CodigoRespuesta.DATA_001);
        }
        // Se devuelve el nombre oficial ("LONGITUD"), no lo que escribió el cliente.
        return FabricaRespuestas.unidadesExitosas(
                encontrada.get().getNombre(), obtenerUnidades(categoria));
    }

    public boolean existeCategoria(String categoria) {
        return buscarCategoria(categoria).isPresent();
    }

    public Optional<Categoria> buscarCategoria(String nombre) {
        return Optional.ofNullable(categorias.get(Textos.normalizar(nombre)));
    }

    public Optional<Unidad> buscarUnidad(String categoria, String nombre) {
        return buscarCategoria(categoria).flatMap(encontrada -> buscarEn(encontrada, nombre));
    }

    public Optional<Categoria> buscarCategoriaDeUnidad(String nombre) {
        return categorias.values().stream()
                .filter(categoria -> buscarEn(categoria, nombre).isPresent())
                .findFirst();
    }

    private static Optional<Unidad> buscarEn(Categoria categoria, String nombre) {
        String buscado = Textos.normalizar(nombre);
        if (buscado.isEmpty()) {
            return Optional.empty();
        }
        return categoria.getUnidades().stream()
                .filter(unidad -> Textos.normalizar(unidad.getNombre()).equals(buscado))
                .findFirst();
    }

    private static List<Categoria> catalogoPorDefecto() {
        return List.of(
                new Categoria(LONGITUD, List.of(
                        new Unidad("centímetros", "cm", 0.01),
                        new Unidad("metros", "m", 1.0),
                        new Unidad("pies", "ft", 0.3048),
                        new Unidad("yardas", "yd", 0.9144),
                        new Unidad("millas", "mi", 1609.344))),
                new Categoria(MASA, List.of(
                        new Unidad("gramos", "g", 0.001),
                        new Unidad("kilogramos", "kg", 1.0),
                        new Unidad("onzas", "oz", 0.028349523125),
                        new Unidad("libras", "lb", 0.45359237),
                        new Unidad("quintales", "qq", 45.359237))),
                new Categoria(TEMPERATURA, List.of(
                        new Unidad("celsius", "°C", SIN_FACTOR),
                        new Unidad("fahrenheit", "°F", SIN_FACTOR),
                        new Unidad("kelvin", "K", SIN_FACTOR),
                        new Unidad("newton", "°N", SIN_FACTOR),
                        new Unidad("reaumur", "°Ré", SIN_FACTOR))));
    }
}
