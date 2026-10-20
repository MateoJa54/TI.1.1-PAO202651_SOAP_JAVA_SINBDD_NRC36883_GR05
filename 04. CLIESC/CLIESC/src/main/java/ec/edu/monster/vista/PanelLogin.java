package ec.edu.monster.vista;

import ec.edu.monster.modelo.RespuestaAutenticacion;
import ec.edu.monster.util.RespuestaUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Pantalla de acceso con la identidad visual de la aplicación de referencia:
 * a la izquierda, un panel de marca en azul marino con el título del producto
 * y la imagen de la mascota; a la derecha, sobre el fondo claro, la tarjeta con
 * el formulario, el estado de la conexión y la dirección del servidor.
 *
 * <p>Al consultar al servidor el botón se deshabilita, aparece una barra de
 * progreso y la llamada corre fuera del hilo de Swing. En pantallas estrechas
 * el panel de marca se oculta para que el formulario conserve su espacio.
 */
public class PanelLogin extends JPanel {

    private static final long serialVersionUID = 1L;

    /** Ancho mínimo de la ventana en la que se muestra el panel de marca. */
    private static final int UMBRAL_MARCA = 860;
    private static final int ANCHO_CAMPO = 360;
    /** Imagen de marca mostrada en el panel lateral del acceso. */
    private static final String RECURSO_IMAGEN = "/imagenes/Monster.png";

    private final transient MediadorEscritorio mediador;
    private final transient BufferedImage imagenMarca = cargarImagenMarca();

    private final JTextField usuario = new JTextField();
    private final JPasswordField password = new JPasswordField();
    private final JButton ingresar;
    private final JLabel estado = new JLabel(" ");
    private final JProgressBar progreso = Tema.barraProgreso();
    private final JPanel panelMarca;
    private final JLabel logoTarjeta = new JLabel(Iconos.marca(44));
    private final JLabel urlServidor = Tema.etiqueta("", Tema.PEQUENA, Tema.TEXTO_SECUNDARIO);

    public PanelLogin(MediadorEscritorio mediador) {
        this.mediador = mediador;
        setLayout(new BorderLayout());
        setBackground(Tema.FONDO);
        this.ingresar = Tema.botonPrimario("Iniciar sesión", null);
        this.panelMarca = crearPanelMarca();
        add(panelMarca, BorderLayout.WEST);
        add(crearZonaFormulario(), BorderLayout.CENTER);
        prepararEventos();
    }

    // ---------- composición ----------

    private JPanel crearPanelMarca() {
        JPanel marca = new JPanel(new BorderLayout(0, 26));
        marca.setBackground(Tema.NAVY);
        marca.setBorder(new EmptyBorder(44, 34, 40, 34));
        marca.setPreferredSize(new Dimension(360, 0));
        marca.setMinimumSize(new Dimension(360, 0));

        JLabel titulo = Tema.etiqueta("Conversor de Unidades", Tema.TITULO, Color.WHITE);
        marca.add(titulo, BorderLayout.NORTH);
        marca.add(new PanelImagen(imagenMarca), BorderLayout.CENTER);
        return marca;
    }

    /** Carga la imagen de marca desde los recursos; {@code null} si no existe. */
    private static BufferedImage cargarImagenMarca() {
        try (InputStream entrada = PanelLogin.class.getResourceAsStream(RECURSO_IMAGEN)) {
            return entrada == null ? null : ImageIO.read(entrada);
        } catch (IOException e) {
            return null;
        }
    }

    private JComponent crearZonaFormulario() {
        JPanel centro = new JPanel(new GridBagLayout());
        centro.setOpaque(false);

        JPanel columna = new JPanel(new GridBagLayout());
        columna.setOpaque(false);
        GridBagConstraints f = new GridBagConstraints();
        f.gridx = 0;
        f.weightx = 1;
        f.fill = GridBagConstraints.HORIZONTAL;
        f.anchor = GridBagConstraints.WEST;

        f.gridy = 0;
        columna.add(crearTarjeta(), f);
        f.gridy = 1;
        f.insets = new Insets(14, 0, 0, 0);
        columna.add(crearCajaServidor(), f);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;
        centro.add(columna, c);
        return centro;
    }

    private JComponent crearTarjeta() {
        PanelTarjeta tarjeta = new PanelTarjeta(new BorderLayout(0, 24));
        tarjeta.setBorder(Tema.rellenoTarjeta());
        tarjeta.add(cabecera(), BorderLayout.NORTH);
        tarjeta.add(formulario(), BorderLayout.CENTER);
        return tarjeta;
    }

    private JComponent cabecera() {
        JPanel cabecera = new JPanel(new BorderLayout(14, 0));
        cabecera.setOpaque(false);
        cabecera.add(logoTarjeta, BorderLayout.WEST);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        JLabel titulo = Tema.etiqueta("Acceso a tu cuenta", Tema.TARJETA_TITULO, Tema.NAVY);
        JLabel subtitulo = Tema.etiqueta("Introduce tus credenciales para continuar.",
                Tema.SUBTITULO, Tema.TEXTO_SECUNDARIO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(3));
        textos.add(subtitulo);
        cabecera.add(textos, BorderLayout.CENTER);
        return cabecera;
    }

    private JComponent formulario() {
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        Tema.prepararCampo(usuario, "Ej. monster");
        usuario.putClientProperty("JTextField.leadingIcon", Iconos.sesion(16, Tema.TEXTO_TENUE));
        Tema.prepararCampo(password, "Contraseña");
        password.putClientProperty("JPasswordField.leadingIcon", Iconos.candado(16, Tema.TEXTO_TENUE));
        password.putClientProperty("JPasswordField.showRevealButton", Boolean.TRUE);

        usuario.setPreferredSize(new Dimension(ANCHO_CAMPO, 42));
        password.setPreferredSize(new Dimension(ANCHO_CAMPO, 42));

        int fila = 0;
        fila = agregarCampo(formulario, c, fila, "Usuario", usuario);
        fila = agregarCampo(formulario, c, fila, "Contraseña", password);

        ingresar.setPreferredSize(new Dimension(0, 44));
        c.gridy = fila;
        c.insets = new Insets(14, 0, 0, 0);
        formulario.add(ingresar, c);

        estado.setFont(Tema.PEQUENA);
        estado.setForeground(Tema.TEXTO_SECUNDARIO);
        estado.setIconTextGap(7);
        c.gridy = fila + 1;
        c.insets = new Insets(14, 0, 0, 0);
        formulario.add(estado, c);

        progreso.setVisible(false);
        c.gridy = fila + 2;
        c.insets = new Insets(8, 0, 0, 0);
        formulario.add(progreso, c);
        return formulario;
    }

    private int agregarCampo(JPanel formulario, GridBagConstraints c, int fila,
            String etiqueta, JComponent campo) {
        c.gridy = fila;
        c.insets = new Insets(0, 0, 7, 0);
        formulario.add(Tema.etiquetaCampo(etiqueta), c);
        c.gridy = fila + 1;
        c.insets = new Insets(0, 0, 18, 0);
        formulario.add(campo, c);
        return fila + 2;
    }

    private JComponent crearCajaServidor() {
        PanelTarjeta caja = new PanelTarjeta(new BorderLayout(12, 0));
        caja.setArco(14);
        caja.setBorder(new EmptyBorder(13, 16, 13, 16));

        JPanel icono = new JPanel(new BorderLayout());
        icono.setOpaque(false);
        icono.add(new JLabel(Iconos.servidor(20, Tema.MARCA)), BorderLayout.CENTER);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        JLabel titulo = Tema.etiqueta("Servidor SOAP", Tema.ETIQUETA, Tema.NAVY);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        urlServidor.setText(acortarUrl(mediador.url()));
        urlServidor.setToolTipText("Dirección del servicio: " + mediador.url());
        urlServidor.setAlignmentX(Component.LEFT_ALIGNMENT);
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(2));
        textos.add(urlServidor);

        caja.add(icono, BorderLayout.WEST);
        caja.add(textos, BorderLayout.CENTER);
        return caja;
    }

    /** Dirección sin esquema y, si es larga, recortada por la izquierda. */
    private static String acortarUrl(String url) {
        if (url == null) {
            return "";
        }
        String sinEsquema = url.replaceFirst("^https?://", "");
        return sinEsquema.length() > 46 ? "…" + sinEsquema.substring(sinEsquema.length() - 45)
                : sinEsquema;
    }

    // ---------- eventos ----------

    private void prepararEventos() {
        ingresar.addActionListener(e -> autenticar());
        password.addActionListener(e -> autenticar());
        usuario.addActionListener(e -> password.requestFocusInWindow());
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                boolean conMarca = getWidth() >= UMBRAL_MARCA;
                if (panelMarca.isVisible() != conMarca) {
                    panelMarca.setVisible(conMarca);
                    logoTarjeta.setVisible(!conMarca);
                }
            }
        });
    }

    /** Botón que debe activarse con Enter en la pantalla de acceso. */
    JButton getBotonIngresar() {
        return ingresar;
    }

    /** Se llama cada vez que la pantalla vuelve a mostrarse. */
    public void alMostrar() {
        urlServidor.setText(acortarUrl(mediador.url()));
        usuario.requestFocusInWindow();
    }

    /** Deja los campos listos cuando la sesión termina. */
    public void limpiar() {
        usuario.setText("");
        password.setText("");
        mostrarCargando(false);
        mostrarEstado(" ", Tema.TEXTO_SECUNDARIO, null);
    }

    private void autenticar() {
        String usuarioEscrito = usuario.getText().trim();
        String claveEscrita = new String(password.getPassword());
        if (usuarioEscrito.isEmpty() || claveEscrita.isEmpty()) {
            mostrarEstado("Completa el usuario y la contraseña para continuar.",
                    Tema.ERROR, Iconos.aviso(14, Tema.ERROR));
            return;
        }
        try {
            mediador.conectar(mediador.url());
        } catch (IllegalArgumentException e) {
            mostrarEstado(e.getMessage(), Tema.ERROR, Iconos.aviso(14, Tema.ERROR));
            return;
        }
        mostrarCargando(true);
        mostrarEstado("Contactando al servidor…", Tema.TEXTO_SECUNDARIO,
                Iconos.servidor(14, Tema.TEXTO_SECUNDARIO));
        TareaAsincrona.ejecutar(this,
                () -> mediador.loginControlador().autenticar(usuarioEscrito, claveEscrita),
                this::alAutenticar,
                causa -> {
                    mostrarCargando(false);
                    mostrarEstado(" ", Tema.TEXTO_SECUNDARIO, null);
                    TareaAsincrona.mostrarFallo(this, causa);
                });
    }

    private void alAutenticar(RespuestaAutenticacion respuesta) {
        mostrarCargando(false);
        if (RespuestaUtil.esExitosa(respuesta) && mediador.loginControlador().haySesionActiva()) {
            mostrarEstado(" ", Tema.EXITO, null);
            mediador.sesionIniciada();
        } else {
            mostrarEstado("No se pudo iniciar sesión: " + RespuestaUtil.describirError(respuesta),
                    Tema.ERROR, Iconos.aviso(14, Tema.ERROR));
            password.setText("");
            password.requestFocusInWindow();
        }
    }

    private void mostrarCargando(boolean cargando) {
        ingresar.setEnabled(!cargando);
        usuario.setEnabled(!cargando);
        password.setEnabled(!cargando);
        progreso.setVisible(cargando);
    }

    private void mostrarEstado(String texto, Color color, javax.swing.Icon icono) {
        estado.setForeground(color);
        estado.setIcon(icono);
        if (texto == null || texto.isBlank()) {
            estado.setText(" ");
            return;
        }
        estado.setText("<html><body style='width:" + ANCHO_CAMPO + "px'>" + escapar(texto)
                + "</body></html>");
    }

    private static String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /**
     * Lienzo que pinta la imagen de marca centrada y escalada para ocupar el
     * espacio disponible, conservando siempre su relación de aspecto.
     */
    private static final class PanelImagen extends JPanel {

        private static final long serialVersionUID = 1L;

        private final transient BufferedImage imagen;

        PanelImagen(BufferedImage imagen) {
            this.imagen = imagen;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (imagen == null || getWidth() <= 0 || getHeight() <= 0) {
                return;
            }
            double escala = Math.min((double) getWidth() / imagen.getWidth(),
                    (double) getHeight() / imagen.getHeight());
            int ancho = (int) Math.round(imagen.getWidth() * escala);
            int alto = (int) Math.round(imagen.getHeight() * escala);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(imagen, (getWidth() - ancho) / 2, (getHeight() - alto) / 2,
                    ancho, alto, null);
            g2.dispose();
        }
    }
}
