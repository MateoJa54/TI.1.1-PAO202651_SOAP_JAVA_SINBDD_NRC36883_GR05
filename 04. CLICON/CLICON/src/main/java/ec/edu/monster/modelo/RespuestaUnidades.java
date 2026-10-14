package ec.edu.monster.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Respuesta de la operación obtenerUnidades.
 */
public class RespuestaUnidades extends Respuesta {

    private String categoria;
    private List<String> unidades = new ArrayList<>();

    public RespuestaUnidades() {
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public List<String> getUnidades() {
        return unidades;
    }

    public void setUnidades(List<String> unidades) {
        this.unidades = unidades;
    }
}
