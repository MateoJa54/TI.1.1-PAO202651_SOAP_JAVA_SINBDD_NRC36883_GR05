package ec.edu.monster.vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

/**
 * Pantalla principal tras iniciar sesión: una barra superior azul marino con
 * la marca, la dirección del servidor y las acciones de sesión (como la barra
 * de estado de la aplicación de referencia), y dos pestañas (catálogo y
 * conversión) sobre el fondo claro.
 */
public class PanelMenu extends JPanel {

    private static final long serialVersionUID = 1L;

    /** Ancho de ventana por debajo del cual la dirección se muestra solo en el tooltip. */
    private static final int UMBRAL_URL = 900;

    private final transient MediadorEscritorio mediador;
    private final PanelCatalogo panelCatalogo;
    private final PanelConversion panelConversion;
    private final JTabbedPane pestanas = new JTabbedPane();

    private final JLabel urlBarra = Tema.etiqueta("", Tema.PEQUENA, Tema.TEXTO_SOBRE_NAVY);
    private final JButton cerrarSesion =
            Tema.botonFantasmaClaro("Cerrar sesión", Iconos.salir(16, Tema.TEXTO_SOBRE_NAVY));
    private final JButton salir = Tema.botonFantasmaClaro("Salir", null);

    public PanelMenu(MediadorEscritorio mediador) {
        this.mediador = mediador;
        this.panelCatalogo = new PanelCatalogo(mediador);
        this.panelConversion = new PanelConversion(mediador);
        setLayout(new BorderLayout());
        setBackground(Tema.FONDO);
        construir();
    }

    private void construir() {
        add(barraSuperior(), BorderLayout.NORTH);

        pestanas.setOpaque(false);
        pestanas.setBackground(Tema.FONDO);
        pestanas.setBorder(new EmptyBorder(18, 24, 24, 24));
        pestanas.addTab("Catálogo", Iconos.catalogo(18, Tema.PRIMARIO), panelCatalogo);
        pestanas.addTab("Conversión", Iconos.convertir(18, Tema.TEXTO_SECUNDARIO), panelConversion);
        pestanas.addChangeListener(e -> actualizarColorPestanas());
        add(pestanas, BorderLayout.CENTER);
    }

    /** El icono de la pestaña activa se tiñe del color de acción. */
    private void actualizarColorPestanas() {
        int activa = pestanas.getSelectedIndex();
        pestanas.setIconAt(0, Iconos.catalogo(18,
                activa == 0 ? Tema.PRIMARIO : Tema.TEXTO_SECUNDARIO));
        pestanas.setIconAt(1, Iconos.convertir(18,
                activa == 1 ? Tema.PRIMARIO : Tema.TEXTO_SECUNDARIO));
    }

    private JComponent barraSuperior() {
        JPanel barra = new JPanel(new BorderLayout(16, 0));
        barra.setBackground(Tema.NAVY);
        barra.setBorder(new EmptyBorder(11, 24, 11, 24));

        JPanel marca = new JPanel(new BorderLayout(12, 0));
        marca.setOpaque(false);
        marca.add(new JLabel(Iconos.marca(34)), BorderLayout.WEST);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        JLabel titulo = Tema.etiqueta("Conversor de Unidades",
                Tema.FUENTE.deriveFont(Font.BOLD, 15.5f), Color.WHITE);
        JLabel subtitulo = Tema.etiqueta("MONSTER · CLIENTE DE ESCRITORIO",
                Tema.FUENTE.deriveFont(Font.BOLD, 10f), Tema.MARCA_CLARO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(2));
        textos.add(subtitulo);
        marca.add(textos, BorderLayout.CENTER);
        barra.add(marca, BorderLayout.WEST);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        derecha.setOpaque(false);
        derecha.add(crearChipServidor());
        derecha.add(Box.createHorizontalStrut(4));
        cerrarSesion.addActionListener(e -> mediador.sesionCerrada());
        salir.addActionListener(e -> mediador.salir());
        derecha.add(cerrarSesion);
        derecha.add(salir);
        barra.add(derecha, BorderLayout.EAST);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                urlBarra.setVisible(getWidth() >= UMBRAL_URL);
            }
        });
        return barra;
    }

    private JComponent crearChipServidor() {
        PanelTarjeta chip = new PanelTarjeta(new FlowLayout(FlowLayout.LEFT, 8, 5));
        chip.setArco(10);
        chip.setSombra(false);
        chip.setFondo(new Color(255, 255, 255, 18));
        chip.setBorde(new Color(255, 255, 255, 48));
        chip.setBorder(new EmptyBorder(5, 12, 5, 12));
        chip.add(new JLabel(Iconos.servidor(16, Tema.MARCA_CLARO)));
        chip.add(urlBarra);
        return chip;
    }

    /**
     * Se llama cada vez que la pantalla vuelve a mostrarse: refresca la
     * información del servidor y de la sesión en la barra superior.
     */
    public void alMostrar() {
        String url = mediador.url() == null ? "" : mediador.url();
        urlBarra.setText(acortarUrl(url));
        StringBuilder ayuda = new StringBuilder("<html>Dirección del servicio:<br>").append(url);
        String expiracion = mediador.loginControlador().obtenerExpiracion();
        if (expiracion != null && !expiracion.isBlank()) {
            ayuda.append("<br>Sesión válida hasta: ").append(expiracion);
        }
        ayuda.append("</html>");
        chipServidor().setToolTipText(ayuda.toString());
        actualizarColorPestanas();
        panelCatalogo.alMostrar();
        panelConversion.alMostrar();
    }

    /** Dirección sin esquema y, si es larga, recortada por la izquierda. */
    private static String acortarUrl(String url) {
        String sinEsquema = url == null ? "" : url.replaceFirst("^https?://", "");
        if (sinEsquema.length() > 36) {
            return "…" + sinEsquema.substring(sinEsquema.length() - 35);
        }
        return sinEsquema;
    }

    private JComponent chipServidor() {
        return (JComponent) urlBarra.getParent();
    }
}
