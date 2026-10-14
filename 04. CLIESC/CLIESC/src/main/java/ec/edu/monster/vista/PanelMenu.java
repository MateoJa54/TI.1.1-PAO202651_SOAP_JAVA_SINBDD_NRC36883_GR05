package ec.edu.monster.vista;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

/**
 * Pantalla principal tras iniciar sesión: una barra superior con la marca, el
 * servidor conectado y las acciones de sesión, y dos pestañas (catálogo y
 * conversión) sobre un fondo claro.
 */
public class PanelMenu extends JPanel {

    private static final long serialVersionUID = 1L;

    private final transient MediadorEscritorio mediador;
    private final PanelCatalogo panelCatalogo;
    private final PanelConversion panelConversion;
    private final JTabbedPane pestanas = new JTabbedPane();    private final JLabel conexion = new JLabel();

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
        pestanas.setBorder(new EmptyBorder(16, 22, 20, 22));
        pestanas.addTab("Catálogo", Iconos.catalogo(18, Tema.TEXTO_SECUNDARIO), panelCatalogo);
        pestanas.addTab("Conversión", Iconos.convertir(18, Tema.TEXTO_SECUNDARIO), panelConversion);
        add(pestanas, BorderLayout.CENTER);
    }

    private JComponent barraSuperior() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(Tema.SUPERFICIE);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.BORDE),
                new EmptyBorder(12, 22, 12, 22)));

        JPanel marca = new JPanel(new BorderLayout(12, 0));
        marca.setOpaque(false);
        marca.add(new JLabel(Iconos.marca(34)), BorderLayout.WEST);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        JLabel titulo = Tema.etiqueta("Conversor de Unidades", Tema.ENCABEZADO, Tema.TEXTO);
        JLabel subtitulo = Tema.etiqueta("Cliente de escritorio", Tema.PEQUENA, Tema.TEXTO_SECUNDARIO);
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        subtitulo.setAlignmentX(LEFT_ALIGNMENT);
        textos.add(titulo);
        textos.add(subtitulo);
        marca.add(textos, BorderLayout.CENTER);
        barra.add(marca, BorderLayout.WEST);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        derecha.setOpaque(false);

        JButton cerrarSesion = Tema.botonFantasma("Cerrar sesión", Iconos.salir(16, Tema.TEXTO_SECUNDARIO));
        JButton salir = Tema.botonFantasma("Salir", null);
        cerrarSesion.addActionListener(e -> mediador.sesionCerrada());
        salir.addActionListener(e -> mediador.salir());
        derecha.add(cerrarSesion);
        derecha.add(Box.createHorizontalStrut(2));
        derecha.add(salir);
        barra.add(derecha, BorderLayout.EAST);
        return barra;
    }

    /** Se llama cada vez que la pantalla vuelve a mostrarse. */
    public void alMostrar() {
        panelCatalogo.alMostrar();
        panelConversion.alMostrar();
    }
}
