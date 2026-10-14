package ec.edu.monster.modelos;

import java.util.ArrayList;
import java.util.List;

public class RespuestaUnidades extends Respuesta {

    private String categoria;
    private List<String> unidades = new ArrayList<>();

    public RespuestaUnidades() {
        super();
    }

    public RespuestaUnidades(
            String estado, String codigo, String mensaje,
            String categoria, List<String> unidades) {
        super(estado, codigo, mensaje);
        this.categoria = categoria;
        this.unidades = new ArrayList<>(unidades);
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
