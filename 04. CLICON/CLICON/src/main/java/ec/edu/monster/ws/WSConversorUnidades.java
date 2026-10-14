package ec.edu.monster.ws;

import ec.edu.monster.modelo.RespuestaAutenticacion;
import ec.edu.monster.modelo.RespuestaCatalogo;
import ec.edu.monster.modelo.RespuestaConversion;
import ec.edu.monster.modelo.RespuestaUnidades;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;

/**
 * Contrato del servicio SOAP visto desde el cliente.
 *
 * <p>Es una copia, escrita a mano, de lo que publica el WSDL del servidor
 * (carpeta {@code 04. SERVIDOR/contrato}): mismos nombres de operación, de
 * parámetros y mismo espacio de nombres. JAX-WS fabrica en tiempo de
 * ejecución un objeto que implementa esta interfaz y convierte cada llamada
 * en un mensaje SOAP.
 *
 * <p>Se escribe a mano, en lugar de generarla con {@code wsimport}, para que
 * el proyecto compile aunque el servidor esté apagado.
 */
@WebService(name = "WSConversorUnidades", targetNamespace = WSConversorUnidades.ESPACIO_DE_NOMBRES)
public interface WSConversorUnidades {

    String ESPACIO_DE_NOMBRES = "http://ws.monster.edu.ec/";

    @WebMethod(operationName = "autenticar")
    RespuestaAutenticacion autenticar(
            @WebParam(name = "usuario") String usuario,
            @WebParam(name = "password") String password);

    @WebMethod(operationName = "obtenerCategorias")
    RespuestaCatalogo obtenerCategorias(
            @WebParam(name = "token") String token);

    @WebMethod(operationName = "obtenerUnidades")
    RespuestaUnidades obtenerUnidades(
            @WebParam(name = "token") String token,
            @WebParam(name = "categoria") String categoria);

    @WebMethod(operationName = "convertir")
    RespuestaConversion convertir(
            @WebParam(name = "token") String token,
            @WebParam(name = "categoria") String categoria,
            @WebParam(name = "unidadOrigen") String unidadOrigen,
            @WebParam(name = "unidadDestino") String unidadDestino,
            @WebParam(name = "valor") Double valor);
}
