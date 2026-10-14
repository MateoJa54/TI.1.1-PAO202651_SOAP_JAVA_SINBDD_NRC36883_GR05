package ec.edu.monster.modelo;

public class RespuestaConversion extends Respuesta {

    private Double resultado;
    private String unidadOrigen;
    private String unidadDestino;

    public RespuestaConversion() {
    }

    public Double getResultado() {
        return resultado;
    }

    public void setResultado(Double resultado) {
        this.resultado = resultado;
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
}
