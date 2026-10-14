package ec.edu.monster.ws;

/**
 * No se obtuvo una respuesta del servidor: está apagado, la dirección es
 * incorrecta, la red falló o contestó algo que no es el servicio esperado.
 *
 * <p>Es distinta de una respuesta con estado ERROR: en ese caso el servidor
 * sí contestó y explicó el motivo con un código.
 */
public class ServicioNoDisponibleException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ServicioNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
