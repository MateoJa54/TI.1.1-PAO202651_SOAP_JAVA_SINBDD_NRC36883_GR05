package ec.edu.monster.modelos;

import java.util.List;


public final class Categoria {

    private final String nombre;
    private final List<Unidad> unidades;

    public Categoria(String nombre, List<Unidad> unidades) {
        this.nombre = nombre;
        this.unidades = List.copyOf(unidades);
    }

    public String getNombre() {
        return nombre;
    }

    public List<Unidad> getUnidades() {
        return unidades;
    }
}
