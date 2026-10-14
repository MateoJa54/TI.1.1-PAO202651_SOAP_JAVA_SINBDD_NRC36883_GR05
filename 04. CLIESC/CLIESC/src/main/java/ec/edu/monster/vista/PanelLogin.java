package ec.edu.monster.vista;

import ec.edu.monster.modelo.RespuestaAutenticacion;
import ec.edu.monster.util.RespuestaUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Pantalla de acceso: una tarjeta centrada con la marca, los campos de
 * usuario, contraseña y servidor, y el botón principal. Al consultar al
 * servidor el botón se deshabilita y la llamada corre fuera del hilo de Swing.
 */
public class PanelLogin extends JPanel {

    private static final long serialVersionUID = 1L;

    private final transient MediadorEscritorio mediador;

    private final JTextField usuario = new JTextField();
    private final JPasswordField password = new JPasswordField();
    private final JButton ingresar;
    private final JLabel estado = new JLabel(" ");

    public PanelLogin(MediadorEscritorio mediador) {
        this.mediador = mediador;
        setLayout(new GridBagLayout());
        setBackground(Tema.FONDO);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        this.ingresar = Tema.botonPrimario("Iniciar sesión", null);
        construir();
        ingresar.addActionListener(e -> autenticar());
        password.addActionListener(e -> autenticar());
        usuario.addActionListener(e -> password.requestFocusInWindow());
    }

    private void construir() {
        PanelTarjeta tarjeta = new PanelTarjeta(new BorderLayout(0, 24));
        tarjeta.setBorder(Tema.rellenoTarjeta());
        tarjeta.add(cabecera(), BorderLayout.NORTH);
        tarjeta.add(formulario(), BorderLayout.CENTER);
        add(tarjeta);
    }

    private JComponent cabecera() {
        JPanel cabecera = new JPanel();
        cabecera.setOpaque(false);
        cabecera.setLayout(new BoxLayout(cabecera, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel(Iconos.marca(54));
        logo.setAlignmentX(CENTER_ALIGNMENT);
        JLabel titulo = Tema.etiqueta("Conversor de Unidades", Tema.TITULO, Tema.TEXTO);
        titulo.setAlignmentX(CENTER_ALIGNMENT);
        JLabel subtitulo = Tema.etiqueta("Monster · Plataforma de conversión",
                Tema.SUBTITULO, Tema.TEXTO_SECUNDARIO);
        subtitulo.setAlignmentX(CENTER_ALIGNMENT);

        cabecera.add(logo);
        cabecera.add(Box.createVerticalStrut(14));
        cabecera.add(titulo);
        cabecera.add(Box.createVerticalStrut(4));
        cabecera.add(subtitulo);
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

        Tema.prepararCampo(usuario, "Usuario");
        usuario.putClientProperty("JTextField.leadingIcon", Iconos.sesion(16, Tema.TEXTO_TENUE));
        Tema.prepararCampo(password, "Contraseña");
        password.putClientProperty("JPasswordField.showRevealButton", Boolean.TRUE);

        Dimension ancho = new Dimension(320, 40);
        usuario.setPreferredSize(ancho);
        password.setPreferredSize(ancho);

        int fila = 0;
        fila = agregarCampo(formulario, c, fila, "Usuario", usuario);
        fila = agregarCampo(formulario, c, fila, "Contraseña", password);

        ingresar.setPreferredSize(new Dimension(0, 42));
        c.gridy = fila;
        c.insets = new Insets(10, 0, 0, 0);
        formulario.add(ingresar, c);

        estado.setFont(Tema.PEQUENA);
        estado.setForeground(Tema.TEXTO_SECUNDARIO);
        c.gridy = fila + 1;
        c.insets = new Insets(12, 0, 0, 0);
        formulario.add(estado, c);
        return formulario;
    }

    private int agregarCampo(JPanel formulario, GridBagConstraints c, int fila,
            String etiqueta, JComponent campo) {
        c.gridy = fila;
        c.insets = new Insets(0, 0, 6, 0);
        formulario.add(Tema.etiquetaCampo(etiqueta), c);
        c.gridy = fila + 1;
        c.insets = new Insets(0, 0, 16, 0);
        formulario.add(campo, c);
        return fila + 2;
    }

    /** Botón que debe activarse con Enter en la pantalla de acceso. */
    JButton getBotonIngresar() {
        return ingresar;
    }

    /** Se llama cada vez que la pantalla vuelve a mostrarse. */
    public void alMostrar() {
        usuario.requestFocusInWindow();
    }

    /** Deja los campos listos cuando la sesión termina. */
    public void limpiar() {
        usuario.setText("");
        password.setText("");
        ingresar.setEnabled(true);
        mostrarEstado(" ", Tema.TEXTO_SECUNDARIO);
    }

    private void autenticar() {
        String usuarioEscrito = usuario.getText().trim();
        String claveEscrita = new String(password.getPassword());
        try {
            mediador.conectar(mediador.url());
        } catch (IllegalArgumentException e) {
            mostrarEstado(e.getMessage(), Tema.ERROR);
            return;
        }
        ingresar.setEnabled(false);
        mostrarEstado("Contactando al servidor…", Tema.TEXTO_SECUNDARIO);
        TareaAsincrona.ejecutar(this,
                () -> mediador.loginControlador().autenticar(usuarioEscrito, claveEscrita),
                this::alAutenticar,
                causa -> {
                    ingresar.setEnabled(true);
                    mostrarEstado(" ", Tema.TEXTO_SECUNDARIO);
                    TareaAsincrona.mostrarFallo(this, causa);
                });
    }

    private void alAutenticar(RespuestaAutenticacion respuesta) {
        ingresar.setEnabled(true);
        if (RespuestaUtil.esExitosa(respuesta) && mediador.loginControlador().haySesionActiva()) {
            mostrarEstado(" ", Tema.EXITO);
            mediador.sesionIniciada();
        } else {
            mostrarEstado("No se pudo iniciar sesión: " + RespuestaUtil.describirError(respuesta), Tema.ERROR);
            password.setText("");
        }
    }

    private void mostrarEstado(String texto, Color color) {
        estado.setForeground(color);
        if (texto == null || texto.isBlank()) {
            estado.setText(" ");
            return;
        }
        estado.setText("<html><body style='width:320px'>" + escapar(texto) + "</body></html>");
    }

    private static String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
