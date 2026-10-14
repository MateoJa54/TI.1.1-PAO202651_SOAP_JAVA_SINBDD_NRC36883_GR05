package ec.edu.monster.modelos;

public enum CodigoRespuesta {

    OK_200("OK", "200", "Operación realizada correctamente"),
    AUTH_001("ERROR", "AUTH-001", "Token inválido"),
    AUTH_002("ERROR", "AUTH-002", "Sesión expirada, vuelva a autenticarse"),
    AUTH_003("ERROR", "AUTH-003", "Usuario o contraseña incorrectos"),
    UNIT_001("ERROR", "UNIT-001", "Unidad de origen no soportada"),
    UNIT_002("ERROR", "UNIT-002", "Unidad de destino no soportada"),
    DATA_001("ERROR", "DATA-001", "Datos de la solicitud inconsistentes"),
    SYS_001("ERROR", "SYS-001", "Error interno del servidor");

    private final String estado;
    private final String codigo;
    private final String mensaje;

    CodigoRespuesta(String estado, String codigo, String mensaje) {
        this.estado = estado;
        this.codigo = codigo;
        this.mensaje = mensaje;
    }

    public String getEstado() {
        return estado;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getMensaje() {
        return mensaje;
    }
}
