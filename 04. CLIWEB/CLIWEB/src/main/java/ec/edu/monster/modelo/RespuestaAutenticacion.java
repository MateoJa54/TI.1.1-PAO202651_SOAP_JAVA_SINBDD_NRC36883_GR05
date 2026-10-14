package ec.edu.monster.modelo;

/**
 * Respuesta de la operación autenticar.
 */
public class RespuestaAutenticacion extends Respuesta {

    private String token;
    private String expiracion;

    public RespuestaAutenticacion() {
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
