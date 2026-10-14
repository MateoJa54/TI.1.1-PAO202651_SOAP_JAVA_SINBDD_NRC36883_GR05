package ec.edu.monster.vista;

import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.modelo.RespuestaAutenticacion;
import ec.edu.monster.util.EntradaUtil;
import ec.edu.monster.util.RespuestaUtil;
import ec.edu.monster.ws.ServicioNoDisponibleException;
import java.io.PrintStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Pantalla de inicio de sesión.
 */
public class LoginVista {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final LoginControlador controlador;
    private final EntradaUtil entrada;
    private final PrintStream salida;

    public LoginVista(LoginControlador controlador, EntradaUtil entrada, PrintStream salida) {
        this.controlador = controlador;
        this.entrada = entrada;
        this.salida = salida;
    }

    /**
     * Pide usuario y contraseña hasta que el servidor los acepte.
     *
     * @return {@code true} si hay sesión iniciada; {@code false} si el
     *         usuario decidió salir (dejó el usuario vacío)
     */
    public boolean iniciar() {
        while (true) {
            salida.println();
            salida.println("=== INICIO DE SESIÓN ===");
            String usuario = entrada.leerTexto("Usuario (vacío para salir)");
            if (usuario.isEmpty()) {
                return false;
            }
            String password = entrada.leerPassword("Contraseña");
            try {
                RespuestaAutenticacion respuesta = controlador.autenticar(usuario, password);
                if (RespuestaUtil.esExitosa(respuesta) && controlador.haySesionActiva()) {
                    salida.println("Acceso concedido. La sesión vence a las "
                            + horaLocal(respuesta.getExpiracion()) + ".");
                    return true;
                }
                salida.println("No se pudo iniciar sesión: " + RespuestaUtil.describirError(respuesta));
            } catch (ServicioNoDisponibleException e) {
                salida.println(e.getMessage());
                salida.println("Verifique que el servidor esté encendido e intente de nuevo.");
            }
        }
    }

    /** El servidor envía la expiración en UTC; al usuario se le muestra en su hora local. */
    private static String horaLocal(String expiracionUtc) {
        try {
            return HORA.format(Instant.parse(expiracionUtc).atZone(ZoneId.systemDefault()));
        } catch (DateTimeParseException | NullPointerException e) {
            return String.valueOf(expiracionUtc);
        }
    }
}
