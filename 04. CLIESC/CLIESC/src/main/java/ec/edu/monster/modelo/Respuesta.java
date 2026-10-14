package ec.edu.monster.modelo;

/**
 * Datos comunes de toda respuesta del servicio: estado (OK / ERROR), código y mensaje.
 */
public abstract class Respuesta {

    private String estado;
    private String codigo;
    private String mensaje;

    protected Respuesta() {
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
