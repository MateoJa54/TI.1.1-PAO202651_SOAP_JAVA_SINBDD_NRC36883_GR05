package ec.edu.monster.pruebas;

import ec.edu.monster.modelo.Respuesta;
import ec.edu.monster.modelo.RespuestaAutenticacion;
import ec.edu.monster.modelo.RespuestaCatalogo;
import ec.edu.monster.modelo.RespuestaConversion;
import ec.edu.monster.modelo.RespuestaUnidades;
import ec.edu.monster.modelo.SolicitudConversion;
import ec.edu.monster.ws.IClienteConversor;
import ec.edu.monster.ws.ServicioNoDisponibleException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Servidor simulado para probar controladores y vistas sin red.
 *
 * <p>Se comporta como el servidor real en lo que el cliente necesita:
 * credencial única, token, catálogo y los códigos de error. Las pruebas
 * pueden "vencer" la sesión o "apagar" el servidor.
 */
class ClienteFalso implements IClienteConversor {

    static final String USUARIO = "monster";
    static final String PASSWORD = "clave";
    static final String EXPIRACION = "2026-10-14T12:15:00Z";

    private static final Map<String, List<String>> CATALOGO = Map.of(
            "LONGITUD", List.of("centímetros", "metros"),
            "MASA", List.of("gramos", "kilogramos"));

    final List<String> llamadas = new ArrayList<>();
    boolean servidorApagado;
    private int tokensEmitidos;
    private String tokenVigente;
    private String tokenVencido;

    /** El token actual deja de servir: el próximo uso responde AUTH-002. */
    void vencerSesion() {
        tokenVencido = tokenVigente;
        tokenVigente = null;
    }

    @Override
    public RespuestaAutenticacion autenticar(String usuario, String password) {
        registrar("autenticar(" + usuario + ")");
        RespuestaAutenticacion r = new RespuestaAutenticacion();
        if (USUARIO.equals(usuario) && PASSWORD.equals(password)) {
            tokenVigente = "TOKEN-" + (++tokensEmitidos);
            llenar(r, "OK", "200", "Operación realizada correctamente");
            r.setToken(tokenVigente);
            r.setExpiracion(EXPIRACION);
        } else {
            llenar(r, "ERROR", "AUTH-003", "Usuario o contraseña incorrectos");
        }
        return r;
    }

    @Override
    public RespuestaCatalogo obtenerCategorias(String token) {
        registrar("obtenerCategorias(" + token + ")");
        RespuestaCatalogo r = new RespuestaCatalogo();
        if (validarToken(r, token)) {
            r.setCategorias(List.of("LONGITUD", "MASA"));
        }
        return r;
    }

    @Override
    public RespuestaUnidades obtenerUnidades(String token, String categoria) {
        registrar("obtenerUnidades(" + token + "," + categoria + ")");
        RespuestaUnidades r = new RespuestaUnidades();
        if (validarToken(r, token)) {
            if (CATALOGO.containsKey(categoria)) {
                r.setCategoria(categoria);
                r.setUnidades(CATALOGO.get(categoria));
            } else {
                llenar(r, "ERROR", "DATA-001", "Datos de la solicitud inconsistentes");
            }
        }
        return r;
    }

    @Override
    public RespuestaConversion convertir(String token, SolicitudConversion s) {
        registrar("convertir(" + token + "," + s.getCategoria() + "," + s.getUnidadOrigen()
                + "," + s.getUnidadDestino() + "," + s.getValor() + ")");
        RespuestaConversion r = new RespuestaConversion();
        if (!validarToken(r, token)) {
            return r;
        }
        List<String> unidades = CATALOGO.get(s.getCategoria());
        if (unidades == null || s.getValor() == null) {
            llenar(r, "ERROR", "DATA-001", "Datos de la solicitud inconsistentes");
        } else if (!unidades.contains(s.getUnidadOrigen())) {
            llenar(r, "ERROR", "UNIT-001", "Unidad de origen no soportada");
        } else if (!unidades.contains(s.getUnidadDestino())) {
            llenar(r, "ERROR", "UNIT-002", "Unidad de destino no soportada");
        } else {
            llenar(r, "OK", "200", "Conversión realizada correctamente");
            r.setResultado(s.getValor() * factor(s.getUnidadOrigen()) / factor(s.getUnidadDestino()));
            r.setUnidadOrigen(s.getUnidadOrigen());
            r.setUnidadDestino(s.getUnidadDestino());
        }
        return r;
    }

    private void registrar(String llamada) {
        if (servidorApagado) {
            throw new ServicioNoDisponibleException("No se pudo comunicar con el servidor en http://falso", null);
        }
        llamadas.add(llamada);
    }

    private boolean validarToken(Respuesta r, String token) {
        if (token != null && token.equals(tokenVigente)) {
            llenar(r, "OK", "200", "Operación realizada correctamente");
            return true;
        }
        if (token != null && token.equals(tokenVencido)) {
            tokenVencido = null;
            llenar(r, "ERROR", "AUTH-002", "Sesión expirada, vuelva a autenticarse");
        } else {
            llenar(r, "ERROR", "AUTH-001", "Token inválido");
        }
        return false;
    }

    private static void llenar(Respuesta r, String estado, String codigo, String mensaje) {
        r.setEstado(estado);
        r.setCodigo(codigo);
        r.setMensaje(mensaje);
    }

    private static double factor(String unidad) {
        return switch (unidad) {
            case "centímetros" -> 0.01;
            case "gramos" -> 0.001;
            default -> 1.0;
        };
    }
}
