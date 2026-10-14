package ec.edu.monster.modelo;

/**
 * Sesión actual del usuario: el token que entregó el servidor y cuándo vence.
 */
public class SesionModelo {

    private String token;
    private String expiracion;

    public SesionModelo() {
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
