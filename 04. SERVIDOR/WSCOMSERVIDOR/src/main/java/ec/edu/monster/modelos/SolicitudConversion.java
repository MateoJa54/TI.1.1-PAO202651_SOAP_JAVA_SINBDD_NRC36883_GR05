package ec.edu.monster.modelos;

public class SolicitudConversion {

    private String categoria;
    private String unidadOrigen;
    private String unidadDestino;
    private Double valor;

    public SolicitudConversion() {
    }

    public SolicitudConversion(
            String categoria, String unidadOrigen, String unidadDestino, Double valor) {
        this.categoria = categoria;
        this.unidadOrigen = unidadOrigen;
        this.unidadDestino = unidadDestino;
        this.valor = valor;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getUnidadOrigen() {
        return unidadOrigen;
    }

    public void setUnidadOrigen(String unidadOrigen) {
        this.unidadOrigen = unidadOrigen;
    }

    public String getUnidadDestino() {
        return unidadDestino;
    }

    public void setUnidadDestino(String unidadDestino) {
        this.unidadDestino = unidadDestino;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }
}
