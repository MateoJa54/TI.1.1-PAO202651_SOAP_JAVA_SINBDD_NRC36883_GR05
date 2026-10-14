package ec.edu.monster.controlador;

import ec.edu.monster.modelo.RespuestaConversion;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.modelo.SolicitudConversion;
import ec.edu.monster.ws.IClienteConversor;
import java.util.Objects;

/**
 * Envía la solicitud de conversión al servidor (caso de uso Realizar
 * conversión). El cálculo y la validación son del servidor: el cliente no
 * conoce factores ni fórmulas.
 */
public class ConversionControlador extends ControladorProtegido {

    public ConversionControlador(IClienteConversor cliente, SesionModelo sesion) {
        super(cliente, sesion);
    }

    public RespuestaConversion convertir(SolicitudConversion solicitud) {
        Objects.requireNonNull(solicitud, "La solicitud es obligatoria");
        return revisarSesion(cliente.convertir(sesion.getToken(), solicitud));
    }
}
