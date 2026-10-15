package ec.edu.monster.modelo;

public final class Conversion {
    private final String categoria, unidadOrigen, unidadDestino;
    private final double valor;
    public Conversion(String categoria, String unidadOrigen, String unidadDestino, double valor) {
        this.categoria = categoria; this.unidadOrigen = unidadOrigen;
        this.unidadDestino = unidadDestino; this.valor = valor;
    }
    public String getCategoria() { return categoria; }
    public String getUnidadOrigen() { return unidadOrigen; }
    public String getUnidadDestino() { return unidadDestino; }
    public double getValor() { return valor; }
}
