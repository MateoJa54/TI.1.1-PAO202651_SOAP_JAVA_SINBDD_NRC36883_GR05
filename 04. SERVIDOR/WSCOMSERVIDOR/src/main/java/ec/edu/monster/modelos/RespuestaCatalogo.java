package ec.edu.monster.modelos;

import java.util.ArrayList;
import java.util.List;

public class RespuestaCatalogo extends Respuesta {

    private List<String> categorias = new ArrayList<>();

    public RespuestaCatalogo() {
        super();
    }

    public RespuestaCatalogo(
            String estado, String codigo, String mensaje, List<String> categorias) {
        super(estado, codigo, mensaje);
        this.categorias = new ArrayList<>(categorias);
    }

    public List<String> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<String> categorias) {
        this.categorias = categorias;
    }
}
