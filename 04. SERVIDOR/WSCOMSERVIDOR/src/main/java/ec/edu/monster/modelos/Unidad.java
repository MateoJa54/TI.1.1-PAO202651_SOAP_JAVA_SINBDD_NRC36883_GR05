package ec.edu.monster.modelos;


public final class Unidad {

    private final String nombre;
    private final String simbolo;
    private final double factorBase;

    public Unidad(String nombre, String simbolo, double factorBase) {
        this.nombre = nombre;
        this.simbolo = simbolo;
        this.factorBase = factorBase;
    }

    public String getNombre() {
        return nombre;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public double getFactorBase() {
        return factorBase;
    }
}
