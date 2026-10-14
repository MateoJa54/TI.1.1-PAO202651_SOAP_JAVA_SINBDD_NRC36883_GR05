package ec.edu.monster.vista;

import ec.edu.monster.controlador.CatalogoControlador;
import ec.edu.monster.controlador.ConversionControlador;
import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.ws.ClienteWSConversor;
import ec.edu.monster.ws.IClienteConversor;
import java.awt.CardLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Ventana principal (Vista) y mediador entre las pantallas. Mantiene la
 * sesión y los controladores, y decide qué pantalla se ve: el acceso o el
 * menú con el catálogo y la conversión.
 */
public class VentanaPrincipal extends JFrame implements MediadorEscritorio {

    private static final long serialVersionUID = 1L;

    private static final String TARJETA_ACCESO = "acceso";
    private static final String TARJETA_MENU = "menu";
    private static final String TITULO = "Conversor de Unidades Monster - Cliente de escritorio";

    private final SesionModelo sesion = new SesionModelo();
    private final CardLayout tarjetas = new CardLayout();
    private final JPanel contenedor = new JPanel(tarjetas);

    private final PanelLogin panelLogin;
    private final PanelMenu panelMenu;

    private LoginControlador loginControlador;
    private CatalogoControlador catalogoControlador;
    private ConversionControlador conversionControlador;
    private String url;

    public VentanaPrincipal(IClienteConversor cliente, String url) {
        super(TITULO);
        conectarControladores(cliente, url);
        panelLogin = new PanelLogin(this);
        panelMenu = new PanelMenu(this);
        contenedor.setBackground(Tema.FONDO);
        contenedor.add(panelLogin, TARJETA_ACCESO);
        contenedor.add(panelMenu, TARJETA_MENU);
        setContentPane(contenedor);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(820, 580));
        pack();
        setSize(Math.max(getWidth(), 960), Math.max(getHeight(), 660));
        setLocationRelativeTo(null);
        mostrarAcceso();
    }

    /** Reúne cliente, sesión y controladores. No toca la interfaz gráfica. */
    private void conectarControladores(IClienteConversor cliente, String url) {
        this.url = url;
        this.loginControlador = new LoginControlador(cliente, sesion);
        this.catalogoControlador = new CatalogoControlador(cliente, sesion);
        this.conversionControlador = new ConversionControlador(cliente, sesion);
        setTitle(TITULO + "  [" + url + "]");
    }

    // ---------- MediadorEscritorio ----------

    @Override
    public LoginControlador loginControlador() {
        return loginControlador;
    }

    @Override
    public CatalogoControlador catalogoControlador() {
        return catalogoControlador;
    }

    @Override
    public ConversionControlador conversionControlador() {
        return conversionControlador;
    }

    @Override
    public String url() {
        return url;
    }

    @Override
    public void conectar(String url) {
        ClienteWSConversor nuevo = new ClienteWSConversor(url);
        loginControlador.cerrarSesion();
        conectarControladores(nuevo, nuevo.getUrl());
    }

    @Override
    public void sesionIniciada() {
        mostrarMenu();
    }

    @Override
    public void sesionCerrada() {
        loginControlador.cerrarSesion();
        panelLogin.limpiar();
        mostrarAcceso();
    }

    @Override
    public void salir() {
        dispose();
        System.exit(0);
    }

    // ---------- cambio de pantalla ----------

    private void mostrarAcceso() {
        tarjetas.show(contenedor, TARJETA_ACCESO);
        getRootPane().setDefaultButton(panelLogin.getBotonIngresar());
        panelLogin.alMostrar();
    }

    private void mostrarMenu() {
        tarjetas.show(contenedor, TARJETA_MENU);
        getRootPane().setDefaultButton(null);
        panelMenu.alMostrar();
    }
}
