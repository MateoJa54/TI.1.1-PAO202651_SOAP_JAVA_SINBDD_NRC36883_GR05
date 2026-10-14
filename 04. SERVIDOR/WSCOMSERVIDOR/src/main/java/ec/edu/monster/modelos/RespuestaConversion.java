package ec.edu.monster.modelos;

public class RespuestaConversion extends Respuesta {

    private Double resultado;
    private String unidadOrigen;
    private String unidadDestino;

    public RespuestaConversion() {
        super();
    }

    public RespuestaConversion(
            String estado, String codigo, String mensaje,
            Double resultado, String unidadOrigen, String unidadDestino) {
        super(estado, codigo, mensaje);
        this.resultado = resultado;
        this.unidadOrigen = unidadOrigen;
        this.unidadDestino = unidadDestino;
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
