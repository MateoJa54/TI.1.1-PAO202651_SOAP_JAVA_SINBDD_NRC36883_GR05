package ec.edu.monster.controladores.ws;

import ec.edu.monster.modelos.CodigoRespuesta;
import ec.edu.monster.modelos.RespuestaAutenticacion;
import ec.edu.monster.modelos.RespuestaCatalogo;
import ec.edu.monster.modelos.RespuestaConversion;
import ec.edu.monster.modelos.RespuestaUnidades;
import ec.edu.monster.modelos.SolicitudConversion;
import ec.edu.monster.seguridad.AutenticacionServicio;
import ec.edu.monster.seguridad.TokenServicio;
import ec.edu.monster.servicios.CatalogoServicio;
import ec.edu.monster.servicios.ConversionServicio;
import ec.edu.monster.servicios.FabricaRespuestas;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebService(serviceName = "WSConversorUnidades", targetNamespace = "http://ws.monster.edu.ec/")
public class WSConversorUnidades {

    private static final Logger LOGGER = Logger.getLogger(WSConversorUnidades.class.getName());
    //usuario quemado
    private static final String USUARIO_POR_DEFECTO = "monster";
    private static final String PASSWORD_POR_DEFECTO = "monster2026";

    private final AutenticacionServicio autenticacionServicio;
    private final TokenServicio tokenServicio;
    private final CatalogoServicio catalogoServicio;
    private final ConversionServicio conversionServicio;

    public WSConversorUnidades() {
        this.tokenServicio = new TokenServicio();
        this.catalogoServicio = new CatalogoServicio();
        this.conversionServicio = new ConversionServicio(catalogoServicio);
        this.autenticacionServicio = new AutenticacionServicio(
                leerConfiguracion("ws.usuario", "WS_USUARIO", USUARIO_POR_DEFECTO),
                leerConfiguracion("ws.password", "WS_PASSWORD", PASSWORD_POR_DEFECTO),
                tokenServicio);
    }

    public WSConversorUnidades(
            AutenticacionServicio autenticacionServicio,
            TokenServicio tokenServicio,
            CatalogoServicio catalogoServicio,
            ConversionServicio conversionServicio) {
        this.autenticacionServicio = autenticacionServicio;
        this.tokenServicio = tokenServicio;
        this.catalogoServicio = catalogoServicio;
        this.conversionServicio = conversionServicio;
    }

    @WebMethod(operationName = "autenticar")
    public RespuestaAutenticacion autenticar(
            @WebParam(name = "usuario") String usuario,
            @WebParam(name = "password") String password) {
        return ejecutarSeguro(
                () -> autenticacionServicio.autenticar(usuario, password),
                FabricaRespuestas::autenticacionFallida);
    }

    @WebMethod(operationName = "obtenerCategorias")
    public RespuestaCatalogo obtenerCategorias(
            @WebParam(name = "token") String token) {
        return ejecutarProtegido(
                token,
                catalogoServicio::consultarCategorias,
                FabricaRespuestas::catalogoFallido);
    }

    @WebMethod(operationName = "obtenerUnidades")
    public RespuestaUnidades obtenerUnidades(
            @WebParam(name = "token") String token,
            @WebParam(name = "categoria") String categoria) {
        return ejecutarProtegido(
                token,
                () -> catalogoServicio.consultarUnidades(categoria),
                FabricaRespuestas::unidadesFallidas);
    }

    @WebMethod(operationName = "convertir")
    public RespuestaConversion convertir(
            @WebParam(name = "token") String token,
            @WebParam(name = "categoria") String categoria,
            @WebParam(name = "unidadOrigen") String unidadOrigen,
            @WebParam(name = "unidadDestino") String unidadDestino,
            @WebParam(name = "valor") Double valor) {
        SolicitudConversion solicitud =
                new SolicitudConversion(categoria, unidadOrigen, unidadDestino, valor);
        return ejecutarProtegido(
                token,
                () -> conversionServicio.convertir(solicitud),
                FabricaRespuestas::conversionFallida);
    }

    private <R> R ejecutarProtegido(
            String token, Supplier<R> operacion, Function<CodigoRespuesta, R> alFallar) {
        return ejecutarSeguro(() -> {
            CodigoRespuesta codigo = tokenServicio.validarToken(token);
            return codigo == CodigoRespuesta.OK_200 ? operacion.get() : alFallar.apply(codigo);
        }, alFallar);
    }

    private <R> R ejecutarSeguro(Supplier<R> operacion, Function<CodigoRespuesta, R> alFallar) {
        try {
            return operacion.get();
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Error inesperado al procesar la petición", e);
            return alFallar.apply(CodigoRespuesta.SYS_001);
        }
    }

    private static String leerConfiguracion(String propiedad, String variable, String porDefecto) {
        String valor = System.getProperty(propiedad);
        if (valor == null || valor.isBlank()) {
            valor = System.getenv(variable);
        }
        return (valor == null || valor.isBlank()) ? porDefecto : valor;
    }
}
