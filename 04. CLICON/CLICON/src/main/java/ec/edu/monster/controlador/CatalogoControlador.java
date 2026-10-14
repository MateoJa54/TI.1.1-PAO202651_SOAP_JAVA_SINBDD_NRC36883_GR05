package ec.edu.monster.controlador;

import ec.edu.monster.modelo.RespuestaCatalogo;
import ec.edu.monster.modelo.RespuestaUnidades;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.ws.IClienteConversor;

public class CatalogoControlador extends ControladorProtegido {

    public CatalogoControlador(IClienteConversor cliente, SesionModelo sesion) {
        super(cliente, sesion);
    }

    public RespuestaCatalogo obtenerCategorias() {
        return revisarSesion(cliente.obtenerCategorias(sesion.getToken()));
    }

    public RespuestaUnidades obtenerUnidades(String categoria) {
        return revisarSesion(cliente.obtenerUnidades(sesion.getToken(), categoria));
    }
}
