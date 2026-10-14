package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.Main;
import ec.edu.monster.modelo.RespuestaAutenticacion;
import ec.edu.monster.modelo.RespuestaCatalogo;
import ec.edu.monster.modelo.RespuestaConversion;
import ec.edu.monster.modelo.RespuestaUnidades;
import ec.edu.monster.modelo.SolicitudConversion;
import ec.edu.monster.util.EntradaUtil;
import ec.edu.monster.ws.ServicioNoDisponibleException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Casos borde vistos desde la pantalla: respuestas inesperadas del servidor,
 * entradas extrañas del usuario y fallos a mitad de una operación.
 */
class VistasBordeTest {

    private static final String U = ClienteFalso.USUARIO;
    private static final String P = ClienteFalso.PASSWORD;

    private String ejecutar(ClienteFalso servidor, String... teclado) {
        ByteArrayOutputStream pantalla = new ByteArrayOutputStream();
        PrintStream salida = new PrintStream(pantalla, true, StandardCharsets.UTF_8);
        String texto = String.join("\n", teclado) + "\n";
        Main.ejecutar(servidor,
                new EntradaUtil(new ByteArrayInputStream(texto.getBytes(StandardCharsets.UTF_8)), salida),
                salida);
        return pantalla.toString(StandardCharsets.UTF_8);
    }

    private static int veces(String texto, String buscado) {
        return texto.split(Pattern.quote(buscado), -1).length - 1;
    }

    private static void error(ec.edu.monster.modelo.Respuesta r, String codigo, String mensaje) {
        r.setEstado("ERROR");
        r.setCodigo(codigo);
        r.setMensaje(mensaje);
    }

    // ---------- inicio de sesión ----------

    @Test
    void elUsuarioSeEnviaSinEspaciosSobrantes() {
        ClienteFalso servidor = new ClienteFalso();

        String pantalla = ejecutar(servidor, "   monster   ", P, "0");

        assertTrue(pantalla.contains("Acceso concedido"));
        assertEquals("autenticar(monster)", servidor.llamadas.get(0));
    }

    @Test
    void laContrasenaSeEnviaTalComoSeEscribeConSusEspacios() {
        List<String> recibidas = new ArrayList<>();
        ClienteFalso servidor = new ClienteFalso() {
            @Override
            public RespuestaAutenticacion autenticar(String usuario, String password) {
                recibidas.add(password);
                return super.autenticar(usuario, password);
            }
        };

        ejecutar(servidor, U, " clave con espacios ", "");

        assertEquals(List.of(" clave con espacios "), recibidas);
    }

    @Test
    void unaContrasenaVaciaSeEnviaYElServidorDecide() {
        ClienteFalso servidor = new ClienteFalso();

        String pantalla = ejecutar(servidor, U, "", "");

        assertTrue(pantalla.contains("[AUTH-003]"));
        assertEquals(1, servidor.llamadas.size());
    }

    @Test
    void muchosIntentosFallidosNoBloqueanAlUsuario() {
        String[] teclado = new String[2 * 30 + 3];
        for (int i = 0; i < 30; i++) {
            teclado[2 * i] = U;
            teclado[2 * i + 1] = "mala" + i;
        }
        teclado[60] = U;
        teclado[61] = P;
        teclado[62] = "0";

        String pantalla = ejecutar(new ClienteFalso(), teclado);

        assertEquals(30, veces(pantalla, "[AUTH-003]"));
        assertTrue(pantalla.contains("Acceso concedido"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"no-es-una-fecha", "", "2026-13-45T99:99:99Z", "14/10/2026 12:15"})
    void unaExpiracionIlegibleSeMuestraTalCualYNoImpideEntrar(String expiracion) {
        ClienteFalso servidor = new ClienteFalso() {
            @Override
            public RespuestaAutenticacion autenticar(String usuario, String password) {
                RespuestaAutenticacion r = super.autenticar(usuario, password);
                r.setExpiracion(expiracion);
                return r;
            }
        };

        String pantalla = ejecutar(servidor, U, P, "0");

        assertTrue(pantalla.contains("Acceso concedido. La sesión vence a las " + expiracion + "."));
        assertTrue(pantalla.contains("CONVERSOR DE UNIDADES MONSTER"));
    }

    @Test
    void sinFechaDeExpiracionTambienSePuedeEntrar() {
        ClienteFalso servidor = new ClienteFalso() {
            @Override
            public RespuestaAutenticacion autenticar(String usuario, String password) {
                RespuestaAutenticacion r = super.autenticar(usuario, password);
                r.setExpiracion(null);
                return r;
            }
        };

        assertTrue(ejecutar(servidor, U, P, "0").contains("CONVERSOR DE UNIDADES MONSTER"));
    }

    @Test
    void unOkSinTokenNoDejaPasarAlMenu() {
        ClienteFalso servidor = new ClienteFalso() {
            @Override
            public RespuestaAutenticacion autenticar(String usuario, String password) {
                RespuestaAutenticacion r = super.autenticar(usuario, password);
                r.setToken(null);
                return r;
            }
        };

        String pantalla = ejecutar(servidor, U, P, "");

        assertFalse(pantalla.contains("CONVERSOR DE UNIDADES MONSTER"));
        assertTrue(pantalla.contains("No se pudo iniciar sesión"));
    }

    @ParameterizedTest
    @CsvSource({
        "DATA-001, Datos de la solicitud inconsistentes",
        "SYS-001, Error interno del servidor",
        "XYZ-999, Código que el cliente no conoce"})
    void cualquierErrorDeAutenticacionSeMuestraConSuCodigo(String codigo, String mensaje) {
        ClienteFalso servidor = new ClienteFalso() {
            @Override
            public RespuestaAutenticacion autenticar(String usuario, String password) {
                RespuestaAutenticacion r = new RespuestaAutenticacion();
                error(r, codigo, mensaje);
                return r;
            }
        };

        String pantalla = ejecutar(servidor, U, P, "");

        assertTrue(pantalla.contains("[" + codigo + "] " + mensaje));
        assertFalse(pantalla.contains("CONVERSOR DE UNIDADES MONSTER"));
    }

    // ---------- catálogo ----------

    @Test
    void siUnaCategoriaFallaLasDemasIgualSeMuestran() {
        ClienteFalso servidor = new ClienteFalso() {
            @Override
            public RespuestaUnidades obtenerUnidades(String token, String categoria) {
                RespuestaUnidades r = super.obtenerUnidades(token, categoria);
                if ("LONGITUD".equals(categoria)) {
                    error(r, "SYS-001", "Error interno del servidor");
                }
                return r;
            }
        };

        String pantalla = ejecutar(servidor, U, P, "1", "0");

        assertTrue(pantalla.contains("LONGITUD: [SYS-001] Error interno del servidor"));
        assertTrue(pantalla.contains("MASA: gramos, kilogramos"));
    }

    @Test
    void siLaSesionExpiraAMitadDelCatalogoNoSeSigueConsultando() {
        ClienteFalso servidor = new ClienteFalso() {
            @Override
            public RespuestaUnidades obtenerUnidades(String token, String categoria) {
                vencerSesion();
                return super.obtenerUnidades(token, categoria);
            }
        };

        String pantalla = ejecutar(servidor, U, P, "1", "");

        assertTrue(pantalla.contains("LONGITUD: [AUTH-002]"));
        assertFalse(pantalla.contains("MASA:"));
        assertEquals(1, servidor.llamadas.stream().filter(l -> l.startsWith("obtenerUnidades")).count());
        assertEquals(2, veces(pantalla, "INICIO DE SESIÓN"));
    }

    @Test
    void unaCategoriaSinUnidadesSeMuestraVacia() {
        ClienteFalso servidor = new ClienteFalso() {
            @Override
            public RespuestaUnidades obtenerUnidades(String token, String categoria) {
                RespuestaUnidades r = super.obtenerUnidades(token, categoria);
                r.setUnidades(List.of());
                return r;
            }
        };

        assertTrue(ejecutar(servidor, U, P, "1", "0").contains("LONGITUD: " + System.lineSeparator()));
    }

    @Test
    void siElCatalogoRespondeErrorSeMuestraYElMenuContinua() {
        ClienteFalso servidor = new ClienteFalso() {
            @Override
            public RespuestaCatalogo obtenerCategorias(String token) {
                RespuestaCatalogo r = new RespuestaCatalogo();
                error(r, "SYS-001", "Error interno del servidor");
                return r;
            }
        };

        String pantalla = ejecutar(servidor, U, P, "1", "2", "0");

        assertTrue(pantalla.contains("No se pudo consultar el catálogo: [SYS-001]"));
        assertTrue(pantalla.contains("No se pudo consultar las categorías: [SYS-001]"));
        assertEquals(3, veces(pantalla, "CONVERSOR DE UNIDADES MONSTER"));
    }

    // ---------- conversión: lo que escribe el usuario ----------

    @ParameterizedTest(name = "[{0}] -> {1}")
    @CsvSource(delimiter = '|', value = {
        "2,5 | 2.5 metros = 250 centímetros",
        "2.5 | 2.5 metros = 250 centímetros",
        "  3  | 3 metros = 300 centímetros",
        "-1 | -1 metros = -100 centímetros",
        "+4 | 4 metros = 400 centímetros",
        "0 | 0 metros = 0 centímetros",
        "-0 | 0 metros = 0 centímetros",
        "1e2 | 100 metros = 10000 centímetros",
        ".5 | 0.5 metros = 50 centímetros",
        "0,001 | 0.001 metros = 0.1 centímetros",
        "007 | 7 metros = 700 centímetros"})
    void elValorSeAceptaEnVariosFormatos(String escrito, String esperado) {
        String pantalla = ejecutar(new ClienteFalso(), U, P, "2", "1", "2", "1", escrito, "0");

        assertTrue(pantalla.contains("Resultado: " + esperado), pantalla);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "1.2.3", "12 metros", "NaN", "Infinity", "1e999", "--1", "$5", "0x10", "1/2"})
    void unValorInvalidoNuncaLlegaAlServidor(String escrito) {
        ClienteFalso servidor = new ClienteFalso();

        String pantalla = ejecutar(servidor, U, P, "2", "1", "2", "1", escrito);

        assertTrue(pantalla.contains("Valor no válido"));
        assertTrue(servidor.llamadas.stream().noneMatch(l -> l.startsWith("convertir")));
    }

    @Test
    void unResultadoEnormeSeMuestraAbreviado() {
        String pantalla = ejecutar(new ClienteFalso(), U, P, "2", "1", "2", "1", "1e300", "0");

        assertTrue(pantalla.contains("Resultado: 1E+300 metros = 1E+302 centímetros"), pantalla);
    }

    @Test
    void unResultadoDiminutoNoSeMuestraComoCero() {
        String pantalla = ejecutar(new ClienteFalso(), U, P, "2", "1", "1", "2", "0.0000001", "0");

        assertTrue(pantalla.contains("= 1E-9 metros"), pantalla);
    }

    @ParameterizedTest
    @ValueSource(strings = {"3", "99", "-1", "uno", "1.0", " ", "1 2"})
    void unaPosicionInvalidaEnUnaListaSeVuelveAPedir(String escrito) {
        String pantalla = ejecutar(new ClienteFalso(), U, P, "2", escrito, "1", "2", "1", "5", "0");

        assertTrue(pantalla.contains("Opción no válida. Escriba un número del 0 al 2"));
        assertTrue(pantalla.contains("Resultado: 5 metros = 500 centímetros"));
    }

    @Test
    void seEnviaExactamenteLaUnidadElegidaConSuTilde() {
        ClienteFalso servidor = new ClienteFalso();

        ejecutar(servidor, U, P, "2", "1", "1", "2", "1", "0");

        assertTrue(servidor.llamadas.contains("convertir(TOKEN-1,LONGITUD,centímetros,metros,1.0)"));
    }

    @Test
    void variasConversionesSeguidasUsanElMismoToken() {
        ClienteFalso servidor = new ClienteFalso();

        ejecutar(servidor, U, P,
                "2", "1", "2", "1", "1",
                "2", "2", "2", "1", "2",
                "2", "1", "1", "2", "3",
                "0");

        List<String> conversiones = servidor.llamadas.stream().filter(l -> l.startsWith("convertir")).toList();
        assertEquals(3, conversiones.size());
        assertTrue(conversiones.stream().allMatch(l -> l.startsWith("convertir(TOKEN-1,")));
        assertEquals(1, servidor.llamadas.stream().filter(l -> l.startsWith("autenticar")).count());
    }

    // ---------- conversión: lo que responde el servidor ----------

    @ParameterizedTest
    @CsvSource({
        "UNIT-001, Unidad de origen no soportada",
        "UNIT-002, Unidad de destino no soportada",
        "DATA-001, Datos de la solicitud inconsistentes",
        "SYS-001, Error interno del servidor",
        "XYZ-999, Código que el cliente no conoce"})
    void unErrorDeConversionSeMuestraYLaSesionContinua(String codigo, String mensaje) {
        ClienteFalso servidor = new ClienteFalso() {
            @Override
            public RespuestaConversion convertir(String token, SolicitudConversion solicitud) {
                RespuestaConversion r = new RespuestaConversion();
                error(r, codigo, mensaje);
                return r;
            }
        };

        String pantalla = ejecutar(servidor, U, P, "2", "1", "2", "1", "5", "1", "0");

        assertTrue(pantalla.contains("No se pudo convertir: [" + codigo + "] " + mensaje));
        assertFalse(pantalla.contains("Resultado:"));
        assertFalse(pantalla.contains("Debe autenticarse de nuevo"));
        assertTrue(pantalla.contains("LONGITUD: centímetros, metros"));
    }

    @ParameterizedTest
    @CsvSource({"AUTH-001, Token inválido", "AUTH-002, 'Sesión expirada, vuelva a autenticarse'"})
    void siElTokenDejaDeServirAlConvertirSeVuelveAlInicioDeSesion(String codigo, String mensaje) {
        ClienteFalso servidor = new ClienteFalso() {
            @Override
            public RespuestaConversion convertir(String token, SolicitudConversion solicitud) {
                RespuestaConversion r = new RespuestaConversion();
                error(r, codigo, mensaje);
                return r;
            }
        };

        String pantalla = ejecutar(servidor, U, P, "2", "1", "2", "1", "5", "");

        assertTrue(pantalla.contains("[" + codigo + "] " + mensaje));
        assertTrue(pantalla.contains("Debe autenticarse de nuevo para continuar."));
        assertEquals(2, veces(pantalla, "INICIO DE SESIÓN"));
        assertEquals(1, veces(pantalla, "CONVERSOR DE UNIDADES MONSTER"));
    }

    @Test
    void unOkSinResultadoNoSeMuestraComoConversionExitosa() {
        ClienteFalso servidor = new ClienteFalso() {
            @Override
            public RespuestaConversion convertir(String token, SolicitudConversion solicitud) {
                RespuestaConversion r = super.convertir(token, solicitud);
                r.setResultado(null);
                return r;
            }
        };

        String pantalla = ejecutar(servidor, U, P, "2", "1", "2", "1", "5", "0");

        assertFalse(pantalla.contains("Resultado:"));
        assertTrue(pantalla.contains("No se pudo convertir"));
    }

    @Test
    void seMuestranLosNombresDeUnidadQueDevuelveElServidor() {
        ClienteFalso servidor = new ClienteFalso() {
            @Override
            public RespuestaConversion convertir(String token, SolicitudConversion solicitud) {
                RespuestaConversion r = super.convertir(token, solicitud);
                r.setUnidadOrigen("METROS-OFICIAL");
                r.setUnidadDestino("CM-OFICIAL");
                return r;
            }
        };

        String pantalla = ejecutar(servidor, U, P, "2", "1", "2", "1", "5", "0");

        assertTrue(pantalla.contains("Resultado: 5 METROS-OFICIAL = 500 CM-OFICIAL"));
    }

    @Test
    void siFallanLasUnidadesNoSePideElValor() {
        ClienteFalso servidor = new ClienteFalso() {
            @Override
            public RespuestaUnidades obtenerUnidades(String token, String categoria) {
                RespuestaUnidades r = new RespuestaUnidades();
                error(r, "DATA-001", "Datos de la solicitud inconsistentes");
                return r;
            }
        };

        String pantalla = ejecutar(servidor, U, P, "2", "1", "0");

        assertTrue(pantalla.contains("No se pudo consultar las unidades: [DATA-001]"));
        assertFalse(pantalla.contains("Valor en"));
    }

    // ---------- el servidor deja de responder ----------

    @Test
    void siElServidorCaeJustoAlConvertirSeAvisaYNoSePierdeLaSesion() {
        ClienteFalso servidor = new ClienteFalso() {
            @Override
            public RespuestaConversion convertir(String token, SolicitudConversion solicitud) {
                throw new ServicioNoDisponibleException("No se pudo comunicar con el servidor en http://falso", null);
            }
        };

        String pantalla = ejecutar(servidor, U, P, "2", "1", "2", "1", "5", "1", "0");

        assertTrue(pantalla.contains("No se pudo comunicar con el servidor"));
        assertFalse(pantalla.contains("Debe autenticarse de nuevo"));
        assertTrue(pantalla.contains("LONGITUD: centímetros, metros"));
    }

    @Test
    void siElServidorVuelveSePuedeSeguirTrabajandoSinReiniciarElPrograma() {
        ClienteFalso servidor = new ClienteFalso() {
            private int consultas;

            @Override
            public RespuestaCatalogo obtenerCategorias(String token) {
                if (++consultas == 1) {
                    throw new ServicioNoDisponibleException("No se pudo comunicar con el servidor", null);
                }
                return super.obtenerCategorias(token);
            }
        };

        String pantalla = ejecutar(servidor, U, P, "1", "1", "0");

        assertEquals(1, veces(pantalla, "No se pudo comunicar con el servidor"));
        assertTrue(pantalla.contains("MASA: gramos, kilogramos"));
    }

    @Test
    void elProgramaSiempreSeDespide() {
        for (String[] teclado : new String[][] {
            {""}, {U, P, "0"}, {U, P, "3", ""}, {U, "mala"}, {U, P, "2", "1"}}) {
            assertTrue(ejecutar(new ClienteFalso(), teclado).endsWith("Hasta pronto." + System.lineSeparator()));
        }
    }
}
