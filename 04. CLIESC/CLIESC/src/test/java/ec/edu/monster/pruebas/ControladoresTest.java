package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.controlador.CatalogoControlador;
import ec.edu.monster.controlador.ConversionControlador;
import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.modelo.RespuestaAutenticacion;
import ec.edu.monster.modelo.RespuestaConversion;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.modelo.SolicitudConversion;
import ec.edu.monster.ws.ServicioNoDisponibleException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ControladoresTest {

    private ClienteFalso servidor;
    private SesionModelo sesion;
    private LoginControlador login;
    private CatalogoControlador catalogo;
    private ConversionControlador conversion;

    @BeforeEach
    void preparar() {
        servidor = new ClienteFalso();
        sesion = new SesionModelo();
        login = new LoginControlador(servidor, sesion);
        catalogo = new CatalogoControlador(servidor, sesion);
        conversion = new ConversionControlador(servidor, sesion);
    }

    private void iniciarSesion() {
        login.autenticar(ClienteFalso.USUARIO, ClienteFalso.PASSWORD);
    }

    private static SolicitudConversion solicitud(String origen, String destino, Double valor) {
        return new SolicitudConversion("LONGITUD", origen, destino, valor);
    }

    // ---------- LoginControlador ----------

    @Test
    void alAutenticarseSeGuardaElTokenYLaExpiracion() {
        RespuestaAutenticacion r = login.autenticar(ClienteFalso.USUARIO, ClienteFalso.PASSWORD);

        assertEquals("OK", r.getEstado());
        assertTrue(login.haySesionActiva());
        assertEquals("TOKEN-1", sesion.getToken());
        assertEquals(ClienteFalso.EXPIRACION, login.obtenerExpiracion());
    }

    @Test
    void conCredencialIncorrectaNoHaySesion() {
        RespuestaAutenticacion r = login.autenticar(ClienteFalso.USUARIO, "mala");

        assertEquals("AUTH-003", r.getCodigo());
        assertFalse(login.haySesionActiva());
        assertNull(sesion.getToken());
    }

    @Test
    void unIntentoFallidoDescartaLaSesionAnterior() {
        iniciarSesion();

        login.autenticar(ClienteFalso.USUARIO, "mala");

        assertFalse(login.haySesionActiva());
    }

    @Test
    void cerrarSesionOlvidaElTokenSinLlamarAlServidor() {
        iniciarSesion();
        int llamadasAntes = servidor.llamadas.size();

        login.cerrarSesion();

        assertFalse(login.haySesionActiva());
        assertNull(login.obtenerExpiracion());
        assertEquals(llamadasAntes, servidor.llamadas.size());
    }

    @Test
    void autenticarseDeNuevoReemplazaElToken() {
        iniciarSesion();
        iniciarSesion();

        assertEquals("TOKEN-2", sesion.getToken());
    }

    @Test
    void unaRespuestaOkSinTokenNoAbreSesion() {
        ClienteFalso sinToken = new ClienteFalso() {
            @Override
            public RespuestaAutenticacion autenticar(String usuario, String password) {
                RespuestaAutenticacion r = super.autenticar(usuario, password);
                r.setToken(null);
                return r;
            }
        };
        LoginControlador controlador = new LoginControlador(sinToken, sesion);

        controlador.autenticar(ClienteFalso.USUARIO, ClienteFalso.PASSWORD);

        assertFalse(controlador.haySesionActiva());
    }

    // ---------- controladores protegidos ----------

    @Test
    void lasOperacionesEnvianElTokenDeLaSesion() {
        iniciarSesion();

        catalogo.obtenerCategorias();
        catalogo.obtenerUnidades("MASA");
        conversion.convertir(solicitud("metros", "centímetros", 2.0));

        assertEquals(List.of(
                "autenticar(monster)",
                "obtenerCategorias(TOKEN-1)",
                "obtenerUnidades(TOKEN-1,MASA)",
                "convertir(TOKEN-1,LONGITUD,metros,centímetros,2.0)"), servidor.llamadas);
    }

    @Test
    void laConversionDevuelveElResultadoDelServidor() {
        iniciarSesion();

        RespuestaConversion r = conversion.convertir(solicitud("metros", "centímetros", 2.5));

        assertEquals("OK", r.getEstado());
        assertEquals(250.0, r.getResultado(), 1e-9);
    }

    @Test
    void sinSesionElServidorRespondeAuth001() {
        assertEquals("AUTH-001", catalogo.obtenerCategorias().getCodigo());
        assertEquals("AUTH-001", conversion.convertir(solicitud("metros", "centímetros", 1.0)).getCodigo());
    }

    @Test
    void siLaSesionExpiroSeDescartaElTokenLocal() {
        iniciarSesion();
        servidor.vencerSesion();

        assertEquals("AUTH-002", catalogo.obtenerCategorias().getCodigo());

        assertFalse(login.haySesionActiva());
        assertNull(sesion.getExpiracion());
    }

    @Test
    void laExpiracionSeDetectaEnCualquierOperacion() {
        iniciarSesion();
        servidor.vencerSesion();
        assertEquals("AUTH-002", catalogo.obtenerUnidades("MASA").getCodigo());
        assertFalse(login.haySesionActiva());

        iniciarSesion();
        servidor.vencerSesion();
        assertEquals("AUTH-002", conversion.convertir(solicitud("metros", "centímetros", 1.0)).getCodigo());
        assertFalse(login.haySesionActiva());
    }

    @Test
    void unErrorDeDatosNoCierraLaSesion() {
        iniciarSesion();

        assertEquals("UNIT-001", conversion.convertir(solicitud("leguas", "metros", 1.0)).getCodigo());
        assertEquals("UNIT-002", conversion.convertir(solicitud("metros", "leguas", 1.0)).getCodigo());
        assertEquals("DATA-001", conversion.convertir(solicitud("metros", "centímetros", null)).getCodigo());
        assertEquals("DATA-001", catalogo.obtenerUnidades("VOLUMEN").getCodigo());

        assertTrue(login.haySesionActiva());
    }

    @Test
    void siElServidorNoRespondeLaSesionSeConserva() {
        iniciarSesion();
        servidor.servidorApagado = true;

        assertThrows(ServicioNoDisponibleException.class, () -> catalogo.obtenerCategorias());

        assertTrue(login.haySesionActiva());
    }

    @Test
    void lasDependenciasYLaSolicitudSonObligatorias() {
        assertThrows(NullPointerException.class, () -> new LoginControlador(null, sesion));
        assertThrows(NullPointerException.class, () -> new CatalogoControlador(servidor, null));
        assertThrows(NullPointerException.class, () -> conversion.convertir(null));
    }
}
