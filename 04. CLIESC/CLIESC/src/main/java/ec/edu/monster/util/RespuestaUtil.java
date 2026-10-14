package ec.edu.monster.util;

import ec.edu.monster.modelo.Respuesta;

/**
 * Interpreta el estado y el código de una respuesta del servidor.
 *
 * <p>Vive aquí, y no en {@code Respuesta}, porque las clases del modelo solo
 * guardan datos.
 */
public final class RespuestaUtil {

    public static final String ESTADO_OK = "OK";
    public static final String TOKEN_INVALIDO = "AUTH-001";
    public static final String SESION_EXPIRADA = "AUTH-002";

    private RespuestaUtil() {
    }

    public static boolean esExitosa(Respuesta respuesta) {
        return respuesta != null && ESTADO_OK.equals(respuesta.getEstado());
    }

    /**
     * @return {@code true} si el servidor ya no reconoce la sesión y el
     *         usuario debe autenticarse otra vez
     */
    public static boolean requiereAutenticarse(Respuesta respuesta) {
        return respuesta != null
                && (TOKEN_INVALIDO.equals(respuesta.getCodigo())
                || SESION_EXPIRADA.equals(respuesta.getCodigo()));
    }

    /** @return el error listo para mostrar, por ejemplo {@code [UNIT-001] Unidad de origen no soportada} */
    public static String describirError(Respuesta respuesta) {
        return "[" + respuesta.getCodigo() + "] " + respuesta.getMensaje();
    }
}
