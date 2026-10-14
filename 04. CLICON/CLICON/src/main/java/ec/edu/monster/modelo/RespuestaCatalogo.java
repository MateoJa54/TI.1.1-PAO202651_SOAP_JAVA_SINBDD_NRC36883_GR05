package ec.edu.monster.modelo;

import java.util.ArrayList;
import java.util.List;

public class RespuestaCatalogo extends Respuesta {

    private List<String> categorias = new ArrayList<>();

    public RespuestaCatalogo() {
    }

    public List<String> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<String> categorias) {
        this.categorias = categorias;
    }
}
