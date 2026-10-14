package ec.edu.monster.seguridad;

import ec.edu.monster.modelos.CodigoRespuesta;
import ec.edu.monster.utilidades.Textos;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TokenServicio {

    private static final Duration VIGENCIA_POR_DEFECTO = Duration.ofMinutes(15);

    private final Duration vigencia;
    private final Clock reloj;
    private final Map<String, Instant> tokens = new ConcurrentHashMap<>();

    public TokenServicio() {
        this(VIGENCIA_POR_DEFECTO, Clock.systemUTC());
    }

    public TokenServicio(Duration vigencia, Clock reloj) {
        Objects.requireNonNull(vigencia, "La vigencia es obligatoria");
        if (vigencia.isNegative() || vigencia.isZero()) {
            throw new IllegalArgumentException("La vigencia debe ser positiva: " + vigencia);
        }
        this.vigencia = vigencia;
        this.reloj = Objects.requireNonNull(reloj, "El reloj es obligatorio");
    }

    public String generarToken() {
        Instant ahora = reloj.instant();
        tokens.values().removeIf(expiracion -> !ahora.isBefore(expiracion));
        String token = UUID.randomUUID().toString();
        tokens.put(token, ahora.plus(vigencia));
        return token;
    }

    public Optional<Instant> obtenerExpiracion(String token) {
        if (token == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(tokens.get(token));
    }

    public CodigoRespuesta validarToken(String token) {
        if (Textos.estaVacio(token)) {
            return CodigoRespuesta.AUTH_001;
        }
        Instant expiracion = tokens.get(token);
        if (expiracion == null) {
            return CodigoRespuesta.AUTH_001;
        }
        if (!reloj.instant().isBefore(expiracion)) {
            tokens.remove(token, expiracion);
            return CodigoRespuesta.AUTH_002;
        }
        return CodigoRespuesta.OK_200;
    }
}
