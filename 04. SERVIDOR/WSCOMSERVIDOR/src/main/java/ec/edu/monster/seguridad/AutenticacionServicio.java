package ec.edu.monster.seguridad;

import ec.edu.monster.modelos.CodigoRespuesta;
import ec.edu.monster.modelos.RespuestaAutenticacion;
import ec.edu.monster.servicios.FabricaRespuestas;
import ec.edu.monster.utilidades.Textos;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class AutenticacionServicio {

    private final String usuario;
    private final String password;
    private final TokenServicio tokenServicio;

    public AutenticacionServicio(String usuario, String password, TokenServicio tokenServicio) {
        if (Textos.estaVacio(usuario) || Textos.estaVacio(password)) {
            throw new IllegalArgumentException("La credencial configurada no puede estar vacía");
        }
        this.usuario = usuario;
        this.password = password;
        this.tokenServicio = Objects.requireNonNull(tokenServicio, "El servicio de tokens es obligatorio");
    }
    public RespuestaAutenticacion autenticar(String usuarioIngresado, String passwordIngresado) {
        if (Textos.estaVacio(usuarioIngresado) || Textos.estaVacio(passwordIngresado)) {
            return FabricaRespuestas.autenticacionFallida(CodigoRespuesta.DATA_001);
        }
        boolean credencialValida = coinciden(usuario, usuarioIngresado)
                & coinciden(password, passwordIngresado);
        if (!credencialValida) {
            return FabricaRespuestas.autenticacionFallida(CodigoRespuesta.AUTH_003);
        }
        String token = tokenServicio.generarToken();
        Instant expiracion = tokenServicio.obtenerExpiracion(token)
                .orElseThrow(() -> new IllegalStateException("El token recién generado no existe"));
        return FabricaRespuestas.autenticacionExitosa(
                token, expiracion.truncatedTo(ChronoUnit.SECONDS).toString());
    }

    private static boolean coinciden(String esperado, String recibido) {
        return MessageDigest.isEqual(
                esperado.getBytes(StandardCharsets.UTF_8),
                recibido.getBytes(StandardCharsets.UTF_8));
    }
}
