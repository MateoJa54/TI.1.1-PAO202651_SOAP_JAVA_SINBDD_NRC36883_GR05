package ec.edu.monster.vista;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/**
 * Panel con esquinas redondeadas, fondo blanco y borde sutil. Es la "tarjeta"
 * sobre la que se apoyan los formularios y las tablas, para dar orden y
 * profundidad sin recargar la vista.
 */
public class PanelTarjeta extends JPanel {

    private static final long serialVersionUID = 1L;

    private int arco = 18;
    private Color fondo = Tema.SUPERFICIE;
    private Color borde = Tema.BORDE;

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

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int ancho = getWidth();
        int alto = getHeight();

        g2.setColor(fondo);
        g2.fillRoundRect(0, 0, ancho - 1, alto - 1, arco, arco);
        if (borde != null) {
            g2.setColor(borde);
            g2.drawRoundRect(0, 0, ancho - 1, alto - 1, arco, arco);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
