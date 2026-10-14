package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.Main;
import ec.edu.monster.modelo.RespuestaCatalogo;
import ec.edu.monster.util.EntradaUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * Recorre el programa completo (vistas + controladores) como lo haría un
 * usuario: cada texto es una línea escrita en el teclado, y se revisa lo que
 * aparece en pantalla. El servidor es simulado.
 */
class FlujoConsolaTest {

    private static final String[] LOGIN = {ClienteFalso.USUARIO, ClienteFalso.PASSWORD};

    private final ClienteFalso servidor = new ClienteFalso();

    private String ejecutar(ClienteFalso servidorUsado, String... teclado) {
        ByteArrayOutputStream pantalla = new ByteArrayOutputStream();
        PrintStream salida = new PrintStream(pantalla, true, StandardCharsets.UTF_8);
        String texto = String.join("\n", teclado) + "\n";
        EntradaUtil entrada = new EntradaUtil(
                new ByteArrayInputStream(texto.getBytes(StandardCharsets.UTF_8)), salida);

        Main.ejecutar(servidorUsado, entrada, salida);

        return pantalla.toString(StandardCharsets.UTF_8);
    }

    private String ejecutar(String... teclado) {
        return ejecutar(servidor, teclado);
    }

    private static int veces(String texto, String buscado) {
        return texto.split(java.util.regex.Pattern.quote(buscado), -1).length - 1;
    }

    // ---------- inicio de sesión ----------

    @Test
    void conUsuarioVacioElProgramaTerminaSinLlamarAlServidor() {
        String pantalla = ejecutar("");

        assertTrue(pantalla.contains("INICIO DE SESIÓN"));
        assertTrue(pantalla.endsWith("Hasta pronto." + System.lineSeparator()));
        assertTrue(servidor.llamadas.isEmpty());
    }

    @Test
    void conCredencialCorrectaSeMuestraElMenu() {
        String pantalla = ejecutar(LOGIN[0], LOGIN[1], "0");

        assertTrue(pantalla.contains("Acceso concedido"));
        assertTrue(pantalla.contains("CONVERSOR DE UNIDADES MONSTER"));
    }

    @Test
    void conCredencialIncorrectaSeMuestraElErrorYSeVuelveAPreguntar() {
        String pantalla = ejecutar("monster", "mala", LOGIN[0], LOGIN[1], "0");

        assertTrue(pantalla.contains("[AUTH-003] Usuario o contraseña incorrectos"));
        assertEquals(2, veces(pantalla, "INICIO DE SESIÓN"));
        assertTrue(pantalla.contains("Acceso concedido"));
    }

    @Test
    void elMenuNoApareceSinAutenticarse() {
        String pantalla = ejecutar("monster", "mala", "otro", "peor", "");

        assertFalse(pantalla.contains("CONVERSOR DE UNIDADES MONSTER"));
    }

    // ---------- catálogo ----------

    @Test
    void elCatalogoMuestraCadaCategoriaConSusUnidades() {
        String pantalla = ejecutar(LOGIN[0], LOGIN[1], "1", "0");

        assertTrue(pantalla.contains("LONGITUD: centímetros, metros"));
        assertTrue(pantalla.contains("MASA: gramos, kilogramos"));
    }

    @Test
    void unCatalogoVacioSeInformaSinFallar() {
        ClienteFalso sinCategorias = new ClienteFalso() {
            @Override
            public RespuestaCatalogo obtenerCategorias(String token) {
                RespuestaCatalogo r = super.obtenerCategorias(token);
                r.setCategorias(java.util.List.of());
                return r;
            }
        };

        String pantalla = ejecutar(sinCategorias, LOGIN[0], LOGIN[1], "1", "0");

        assertTrue(pantalla.contains("no tiene categorías"));
    }

    // ---------- conversión ----------

    @Test
    void unaConversionCompletaMuestraElResultado() {
        // 2 = convertir, 1 = LONGITUD, 2 = metros, 1 = centímetros, valor 2,5
        String pantalla = ejecutar(LOGIN[0], LOGIN[1], "2", "1", "2", "1", "2,5", "0");

        assertTrue(pantalla.contains("Resultado: 2.5 metros = 250 centímetros"));
        assertTrue(servidor.llamadas.contains("convertir(TOKEN-1,LONGITUD,metros,centímetros,2.5)"));
    }

    @Test
    void unValorNoNumericoSeVuelveAPedirYNoSeEnviaAlServidor() {
        String pantalla = ejecutar(LOGIN[0], LOGIN[1], "2", "1", "2", "1", "abc", "", "10", "0");

        assertEquals(2, veces(pantalla, "Valor no válido"));
        assertTrue(pantalla.contains("Resultado: 10 metros = 1000 centímetros"));
        assertEquals(1, servidor.llamadas.stream().filter(l -> l.startsWith("convertir")).count());
    }

    @Test
    void seAceptanValoresNegativosYCero() {
        String pantalla = ejecutar(LOGIN[0], LOGIN[1],
                "2", "1", "2", "1", "-3",
                "2", "1", "2", "1", "0",
                "0");

        assertTrue(pantalla.contains("Resultado: -3 metros = -300 centímetros"));
        assertTrue(pantalla.contains("Resultado: 0 metros = 0 centímetros"));
    }

    @Test
    void convertirALaMismaUnidadEstaPermitido() {
        String pantalla = ejecutar(LOGIN[0], LOGIN[1], "2", "2", "1", "1", "7", "0");

        assertTrue(pantalla.contains("Resultado: 7 gramos = 7 gramos"));
    }

    @Test
    void cancelarEnCualquierPasoVuelveAlMenuSinConvertir() {
        String pantalla = ejecutar(LOGIN[0], LOGIN[1],
                "2", "0",            // cancela en la categoría
                "2", "1", "0",       // cancela en la unidad de origen
                "2", "1", "1", "0",  // cancela en la unidad de destino
                "0");

        assertFalse(pantalla.contains("Resultado:"));
        assertEquals(4, veces(pantalla, "CONVERSOR DE UNIDADES MONSTER"));
        assertTrue(servidor.llamadas.stream().noneMatch(l -> l.startsWith("convertir")));
    }

    @Test
    void unaOpcionDeMenuInvalidaSeVuelveAPedir() {
        String pantalla = ejecutar(LOGIN[0], LOGIN[1], "9", "x", "", "0");

        assertEquals(3, veces(pantalla, "Opción no válida"));
        assertEquals(1, veces(pantalla, "CONVERSOR DE UNIDADES MONSTER"));
    }

    // ---------- sesión ----------

    @Test
    void cerrarSesionVuelveAlInicioDeSesion() {
        String pantalla = ejecutar(LOGIN[0], LOGIN[1], "3", "");

        assertTrue(pantalla.contains("Sesión cerrada."));
        assertEquals(2, veces(pantalla, "INICIO DE SESIÓN"));
    }

    @Test
    void siLaSesionExpiraSePideAutenticarseYLuegoSePuedeContinuar() {
        ClienteFalso servidorQueVence = new ClienteFalso() {
            private boolean yaVencio;

            @Override
            public RespuestaCatalogo obtenerCategorias(String token) {
                if (!yaVencio) {
                    yaVencio = true;
                    vencerSesion();
                }
                return super.obtenerCategorias(token);
            }
        };

        String pantalla = ejecutar(servidorQueVence,
                LOGIN[0], LOGIN[1], "1",   // la sesión vence al consultar
                LOGIN[0], LOGIN[1], "1",   // se autentica otra vez y consulta
                "0");

        assertTrue(pantalla.contains("[AUTH-002] Sesión expirada, vuelva a autenticarse"));
        assertTrue(pantalla.contains("Debe autenticarse de nuevo para continuar."));
        assertEquals(2, veces(pantalla, "INICIO DE SESIÓN"));
        assertTrue(pantalla.contains("LONGITUD: centímetros, metros"));
    }

    // ---------- servidor apagado y fin de la entrada ----------

    @Test
    void siElServidorEstaApagadoAlIniciarSesionSeExplicaYSePuedeReintentar() {
        servidor.servidorApagado = true;

        String pantalla = ejecutar(LOGIN[0], LOGIN[1], "");

        assertTrue(pantalla.contains("No se pudo comunicar con el servidor"));
        assertTrue(pantalla.contains("Verifique que el servidor esté encendido"));
        assertEquals(2, veces(pantalla, "INICIO DE SESIÓN"));
    }

    @Test
    void siElServidorSeApagaConLaSesionAbiertaElMenuSigueDisponible() {
        ClienteFalso seApaga = new ClienteFalso() {
            @Override
            public RespuestaCatalogo obtenerCategorias(String token) {
                servidorApagado = true;
                return super.obtenerCategorias(token);
            }
        };

        String pantalla = ejecutar(seApaga, LOGIN[0], LOGIN[1], "1", "0");

        assertTrue(pantalla.contains("No se pudo comunicar con el servidor"));
        assertEquals(2, veces(pantalla, "CONVERSOR DE UNIDADES MONSTER"));
        assertFalse(pantalla.contains("Debe autenticarse de nuevo"));
    }

    @Test
    void siLaEntradaSeCierraEnCualquierPuntoElProgramaTerminaConOrden() {
        for (String[] teclado : new String[][] {
            {LOGIN[0]},
            {LOGIN[0], LOGIN[1]},
            {LOGIN[0], LOGIN[1], "2"},
            {LOGIN[0], LOGIN[1], "2", "1", "2", "1"}}) {
            String pantalla = ejecutar(new ClienteFalso(), teclado);

            assertTrue(pantalla.endsWith("Hasta pronto." + System.lineSeparator()),
                    "Con " + teclado.length + " líneas de entrada");
        }
    }
}
