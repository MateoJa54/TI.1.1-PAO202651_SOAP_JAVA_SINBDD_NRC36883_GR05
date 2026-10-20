package ec.edu.monster.vista;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/**
 * Panel con esquinas redondeadas, fondo blanco, borde sutil y una sombra
 * muy discreta asomando por la derecha y el borde inferior. Es la "tarjeta"
 * sobre la que se apoyan los formularios y las tablas, para dar orden y
 * profundidad sin recargar la vista.
 *
 * <p>El relleno se dibuja algunos píxeles por encima del borde inferior y
 * derecho para dejar sitio a la sombra; los márgenes de contenido de las
 * tarjetas (26 px o más) garantizan que el contenido nunca caiga sobre ella.
 */
public class PanelTarjeta extends JPanel {

    private static final long serialVersionUID = 1L;

    /** Ancho de la sombra que asoma por la derecha y el inferior. */
    private static final int RESPIRO_SOMBRA = 4;
    /** Altura adicional de la sombra en el borde inferior. */
    private static final int SOMBRA_INFERIOR = 6;

    private int arco = 18;
    private Color fondo = Tema.SUPERFICIE;
    private Color borde = Tema.BORDE;
    private boolean sombra = true;

    public PanelTarjeta() {
        super();
        setOpaque(false);
    }

    public PanelTarjeta(LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }

    public void setFondo(Color fondo) {
        this.fondo = fondo;
        repaint();
    }

    public void setBorde(Color borde) {
        this.borde = borde;
        repaint();
    }

    public void setArco(int arco) {
        this.arco = arco;
        repaint();
    }

    /** Activa o desactiva la sombra suave (por defecto, activa). */
    public void setSombra(boolean sombra) {
        this.sombra = sombra;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int ancho = getWidth();
        int alto = getHeight();
        boolean amplio = sombra && ancho > 24 && alto > 24;

        if (amplio) {
            // Dos capas translúcidas muy tenues simulan una sombra difusa.
            int base = Math.max(3, RESPIRO_SOMBRA - 1);
            g2.setColor(new Color(Tema.SOMBRA.getRed(), Tema.SOMBRA.getGreen(),
                    Tema.SOMBRA.getBlue(), 9));
            g2.fillRoundRect(base, base + 2,
                    ancho - base - 1, alto - base - 2, arco, arco);
            g2.setColor(new Color(Tema.SOMBRA.getRed(), Tema.SOMBRA.getGreen(),
                    Tema.SOMBRA.getBlue(), 13));
            g2.fillRoundRect(RESPIRO_SOMBRA, SOMBRA_INFERIOR,
                    ancho - RESPIRO_SOMBRA - 1, alto - SOMBRA_INFERIOR - 1, arco, arco);
        }

        int cuerpoAncho = amplio ? ancho - RESPIRO_SOMBRA : ancho;
        int cuerpoAlto = amplio ? alto - SOMBRA_INFERIOR : alto;

        g2.setColor(fondo);
        g2.fillRoundRect(0, 0, cuerpoAncho - 1, cuerpoAlto - 1, arco, arco);
        if (borde != null) {
            g2.setColor(borde);
            g2.drawRoundRect(0, 0, cuerpoAncho - 1, cuerpoAlto - 1, arco, arco);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
