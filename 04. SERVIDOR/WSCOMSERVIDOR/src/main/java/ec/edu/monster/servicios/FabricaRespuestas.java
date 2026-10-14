package ec.edu.monster.servicios;

import ec.edu.monster.modelos.CodigoRespuesta;
import ec.edu.monster.modelos.RespuestaAutenticacion;
import ec.edu.monster.modelos.RespuestaCatalogo;
import ec.edu.monster.modelos.RespuestaConversion;
import ec.edu.monster.modelos.RespuestaUnidades;
import java.util.List;
import java.util.Objects;

public final class FabricaRespuestas {

    private static final String MENSAJE_CONVERSION_EXITOSA = "Conversión realizada correctamente";

    private FabricaRespuestas() {
    }

    public static RespuestaAutenticacion autenticacionExitosa(String token, String expiracion) {
        CodigoRespuesta ok = CodigoRespuesta.OK_200;
        return new RespuestaAutenticacion(
                ok.getEstado(), ok.getCodigo(), ok.getMensaje(), token, expiracion);
    }

    public static RespuestaAutenticacion autenticacionFallida(CodigoRespuesta codigo) {
        exigirError(codigo);
        return new RespuestaAutenticacion(
                codigo.getEstado(), codigo.getCodigo(), codigo.getMensaje(), null, null);
    }

    public static RespuestaCatalogo catalogoExitoso(List<String> categorias) {
        CodigoRespuesta ok = CodigoRespuesta.OK_200;
        return new RespuestaCatalogo(ok.getEstado(), ok.getCodigo(), ok.getMensaje(), categorias);
    }

    public static RespuestaCatalogo catalogoFallido(CodigoRespuesta codigo) {
        exigirError(codigo);
        return new RespuestaCatalogo(
                codigo.getEstado(), codigo.getCodigo(), codigo.getMensaje(), List.of());
    }

    public static RespuestaUnidades unidadesExitosas(String categoria, List<String> unidades) {
        CodigoRespuesta ok = CodigoRespuesta.OK_200;
        return new RespuestaUnidades(
                ok.getEstado(), ok.getCodigo(), ok.getMensaje(), categoria, unidades);
    }

    public static RespuestaUnidades unidadesFallidas(CodigoRespuesta codigo) {
        exigirError(codigo);
        return new RespuestaUnidades(
                codigo.getEstado(), codigo.getCodigo(), codigo.getMensaje(), null, List.of());
    }

    public static RespuestaConversion conversionExitosa(
            double resultado, String unidadOrigen, String unidadDestino) {
        CodigoRespuesta ok = CodigoRespuesta.OK_200;
        return new RespuestaConversion(
                ok.getEstado(), ok.getCodigo(), MENSAJE_CONVERSION_EXITOSA,
                resultado, unidadOrigen, unidadDestino);
    }

    public static RespuestaConversion conversionFallida(CodigoRespuesta codigo) {
        exigirError(codigo);
        return new RespuestaConversion(
                codigo.getEstado(), codigo.getCodigo(), codigo.getMensaje(), null, null, null);
    }

    private static void exigirError(CodigoRespuesta codigo) {
        Objects.requireNonNull(codigo, "El código de error es obligatorio");
        if (codigo == CodigoRespuesta.OK_200) {
            throw new IllegalArgumentException("OK_200 no es un código de error");
        }
    }
}
