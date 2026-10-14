package ec.edu.monster.modelos;

public abstract class Respuesta {

    private String estado;
    private String codigo;
    private String mensaje;

    /** Lo necesita JAXB para reconstruir el mensaje SOAP. */
    protected Respuesta() {
    }

    protected Respuesta(String estado, String codigo, String mensaje) {
        this.estado = estado;
        this.codigo = codigo;
        this.mensaje = mensaje;
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
