package ec.edu.monster.modelos;

public class RespuestaAutenticacion extends Respuesta {

    private String token;
    private String expiracion;

    public RespuestaAutenticacion() {
        super();
    }

    public RespuestaAutenticacion(
            String estado, String codigo, String mensaje, String token, String expiracion) {
        super(estado, codigo, mensaje);
        this.token = token;
        this.expiracion = expiracion;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getExpiracion() {
        return expiracion;
    }

    public void setExpiracion(String expiracion) {
        this.expiracion = expiracion;
    }
}
