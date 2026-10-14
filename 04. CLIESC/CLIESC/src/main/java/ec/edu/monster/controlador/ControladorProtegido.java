package ec.edu.monster.controlador;

import ec.edu.monster.modelo.Respuesta;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.util.RespuestaUtil;
import ec.edu.monster.ws.IClienteConversor;
import java.util.Objects;

public abstract class ControladorProtegido {

    protected final IClienteConversor cliente;
    protected final SesionModelo sesion;

    protected ControladorProtegido(IClienteConversor cliente, SesionModelo sesion) {
        this.cliente = Objects.requireNonNull(cliente, "El cliente del servicio es obligatorio");
        this.sesion = Objects.requireNonNull(sesion, "La sesión es obligatoria");
    }

    protected <R extends Respuesta> R revisarSesion(R respuesta) {
        if (RespuestaUtil.requiereAutenticarse(respuesta)) {
            sesion.setToken(null);
            sesion.setExpiracion(null);
        }
        return respuesta;
    }
}
