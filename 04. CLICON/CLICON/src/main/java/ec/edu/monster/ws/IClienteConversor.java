package ec.edu.monster.ws;

import ec.edu.monster.modelo.RespuestaAutenticacion;
import ec.edu.monster.modelo.RespuestaCatalogo;
import ec.edu.monster.modelo.RespuestaConversion;
import ec.edu.monster.modelo.RespuestaUnidades;
import ec.edu.monster.modelo.SolicitudConversion;

/**
 * Lo que los controladores necesitan del servidor, sin saber que detrás hay
 * SOAP (patrón Puerto y Adaptador).
 *
 * <p>Los controladores dependen de esta interfaz y no de
 * {@link ClienteWSConversor}; por eso se pueden probar con un cliente falso,
 * sin servidor.
 *
 * <p>Todos los métodos lanzan {@link ServicioNoDisponibleException} si no se
 * pudo obtener una respuesta del servidor.
 */
public interface IClienteConversor {

    RespuestaAutenticacion autenticar(String usuario, String password);

    RespuestaCatalogo obtenerCategorias(String token);

    RespuestaUnidades obtenerUnidades(String token, String categoria);

    RespuestaConversion convertir(String token, SolicitudConversion solicitud);
}
