package ec.edu.monster.controlador;

import ec.edu.monster.modelo.RespuestaAutenticacion;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.util.RespuestaUtil;
import ec.edu.monster.ws.IClienteConversor;
import java.util.Objects;

public class LoginControlador {

    private final IClienteConversor cliente;
    private final SesionModelo sesion;

    public LoginControlador(IClienteConversor cliente, SesionModelo sesion) {
        this.cliente = Objects.requireNonNull(cliente, "El cliente del servicio es obligatorio");
        this.sesion = Objects.requireNonNull(sesion, "La sesión es obligatoria");
    }

    public RespuestaAutenticacion autenticar(String usuario, String password) {
        RespuestaAutenticacion respuesta = cliente.autenticar(usuario, password);
        if (RespuestaUtil.esExitosa(respuesta) && respuesta.getToken() != null) {
            sesion.setToken(respuesta.getToken());
            sesion.setExpiracion(respuesta.getExpiracion());
        } else {
            cerrarSesion();
        }
        return respuesta;
    }

    public void cerrarSesion() {
        sesion.setToken(null);
        sesion.setExpiracion(null);
    }

    public boolean haySesionActiva() {
        return sesion.getToken() != null;
    }

    public String obtenerExpiracion() {
        return sesion.getExpiracion();
    }
}
