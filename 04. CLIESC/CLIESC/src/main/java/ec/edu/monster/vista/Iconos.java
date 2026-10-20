package ec.edu.monster.vista;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.Icon;

/**
 * Iconos vectoriales dibujados con Java2D (sin archivos ni fuentes externas).
 * Comparten un mismo trazo para que el conjunto se vea coherente: línea
 * redondeada, mismo grosor y mismo margen.
 */
public final class Iconos {

    private Iconos() {
    }

    /** Marca de la aplicación: cuadro redondeado azul con dos flechas. */
    public static Icon marca(int tam) {
        return new Icon() {
            @Override
            public int getIconWidth() {
                return tam;
            }

            @Override
            public int getIconHeight() {
                return tam;
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.translate(x, y);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arco = Math.round(tam * 0.3f);
                g2.setPaint(new GradientPaint(0, 0, new Color(0x3B82F6), tam, tam, Tema.PRIMARIO_OSCURO));
                g2.fillRoundRect(0, 0, tam - 1, tam - 1, arco, arco);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(Math.max(1.6f, tam * 0.085f),
                        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                double m = tam * 0.3;
                flecha(g2, m, tam * 0.40, tam - m, tam * 0.40, tam);
                flecha(g2, tam - m, tam * 0.60, m, tam * 0.60, tam);
                g2.dispose();
            }
        };
    }

    /** Lista con viñetas: catálogo. */
    public static Icon catalogo(int tam, Color color) {
        return vectorial(tam, color, (g, lado) -> {
            double p = lado * 0.2;
            double r = Math.max(1.4, lado * 0.05);
            for (int i = 0; i < 3; i++) {
                double y = p + (lado - 2 * p) * i / 2.0;
                g.fill(new Ellipse2D.Double(p - r, y - r, r * 2, r * 2));
                g.draw(new Line2D.Double(p + r * 2.6, y, lado - p, y));
            }
        });
    }

    /** Dos flechas opuestas: conversión. */
    public static Icon convertir(int tam, Color color) {
        return vectorial(tam, color, (g, lado) -> {
            double p = lado * 0.18;
            flecha(g, p, lado * 0.36, lado - p, lado * 0.36, lado);
            flecha(g, lado - p, lado * 0.64, p, lado * 0.64, lado);
        });
    }

    /** Flechas para intercambiar origen y destino. */
    public static Icon intercambiar(int tam, Color color) {
        return convertir(tam, color);
    }

    /** Flecha circular: actualizar. */
    public static Icon refrescar(int tam, Color color) {
        return vectorial(tam, color, (g, lado) -> {
            double p = lado * 0.18;
            double r = (lado - 2 * p) / 2.0;
            double cx = lado / 2.0;
            double cy = lado / 2.0;
            g.draw(new Arc2D.Double(cx - r, cy - r, r * 2, r * 2, 55, 280, Arc2D.OPEN));

            double th = Math.toRadians(55);
            double px = cx + r * Math.cos(th);
            double py = cy - r * Math.sin(th);
            double tx = -Math.sin(th);
            double ty = -Math.cos(th);
            double nx = -ty;
            double ny = tx;
            double s = Math.max(3.2, lado * 0.22);
            Path2D punta = new Path2D.Double();
            punta.moveTo(px + tx * s, py + ty * s);
            punta.lineTo(px + nx * s * 0.72, py + ny * s * 0.72);
            punta.lineTo(px - nx * s * 0.72, py - ny * s * 0.72);
            punta.closePath();
            g.fill(punta);
        });
    }

    /** Persona: sesión. */
    public static Icon sesion(int tam, Color color) {
        return vectorial(tam, color, (g, lado) -> {
            double radio = lado * 0.17;
            g.draw(new Ellipse2D.Double(lado * 0.5 - radio, lado * 0.16, radio * 2, radio * 2));
            double p = lado * 0.2;
            g.draw(new Arc2D.Double(p, lado * 0.52, lado - 2 * p, lado * 0.72, 0, 180, Arc2D.OPEN));
        });
    }

    /** Puerta con flecha de salida: cerrar sesión / salir. */
    public static Icon salir(int tam, Color color) {
        return vectorial(tam, color, (g, lado) -> {
            double p = lado * 0.2;
            double xi = p;
            g.draw(new Line2D.Double(xi, p, xi, lado - p));
            g.draw(new Line2D.Double(xi, p, xi + lado * 0.2, p));
            g.draw(new Line2D.Double(xi, lado - p, xi + lado * 0.2, lado - p));
            double y = lado / 2.0;
            double xf = lado - p;
            g.draw(new Line2D.Double(lado * 0.34, y, xf, y));
            double largo = Math.max(3, lado * 0.18);
            g.draw(new Line2D.Double(xf, y, xf - largo, y - largo * 0.7));
            g.draw(new Line2D.Double(xf, y, xf - largo, y + largo * 0.7));
        });
    }

    /** Pila de servidores: dirección del servicio. */
    public static Icon servidor(int tam, Color color) {
        return vectorial(tam, color, (g, lado) -> {
            double p = lado * 0.18;
            double w = lado - 2 * p;
            double h = lado * 0.24;
            double y1 = lado * 0.26;
            double y2 = lado * 0.54;
            g.draw(new RoundRectangle2D.Double(p, y1, w, h, 4, 4));
            g.draw(new RoundRectangle2D.Double(p, y2, w, h, 4, 4));
            double d = lado * 0.09;
            g.fill(new Ellipse2D.Double(p + w - d - lado * 0.06, y1 + h / 2 - d / 2, d, d));
            g.fill(new Ellipse2D.Double(p + w - d - lado * 0.06, y2 + h / 2 - d / 2, d, d));
        });
    }

    /** Candado cerrado: campo de contraseña. */
    public static Icon candado(int tam, Color color) {
        return vectorial(tam, color, (g, lado) -> {
            double anchoCuerpo = lado * 0.56;
            double altoCuerpo = lado * 0.4;
            double xCuerpo = (lado - anchoCuerpo) / 2.0;
            double yCuerpo = lado * 0.5;
            double radioAro = lado * 0.2;
            double centroX = lado / 2.0;
            double yBase = yCuerpo + radioAro * 0.55;
            g.draw(new Arc2D.Double(centroX - radioAro, yBase - radioAro * 2,
                    radioAro * 2, radioAro * 2, 0, 180, Arc2D.OPEN));
            g.draw(new Line2D.Double(centroX - radioAro, yBase, centroX - radioAro, yCuerpo + 2));
            g.draw(new Line2D.Double(centroX + radioAro, yBase, centroX + radioAro, yCuerpo + 2));
            g.draw(new RoundRectangle2D.Double(xCuerpo, yCuerpo, anchoCuerpo, altoCuerpo,
                    lado * 0.14, lado * 0.14));
        });
    }

    /** Dos hojas superpuestas: copiar al portapapeles. */
    public static Icon copiar(int tam, Color color) {
        return vectorial(tam, color, (g, lado) -> {
            double m = lado * 0.24;
            double w = lado * 0.46;
            double h = lado * 0.52;
            double arco = lado * 0.1;
            g.draw(new RoundRectangle2D.Double(m, m + lado * 0.1, w, h, arco, arco));
            g.draw(new RoundRectangle2D.Double(lado - m - w, lado - m - h - lado * 0.1, w, h, arco, arco));
        });
    }

    /** Palomita: operación exitosa / copiado. */
    public static Icon exito(int tam, Color color) {
        return vectorial(tam, color, (g, lado) -> {
            g.draw(new Line2D.Double(lado * 0.2, lado * 0.54, lado * 0.43, lado * 0.76));
            g.draw(new Line2D.Double(lado * 0.43, lado * 0.76, lado * 0.8, lado * 0.26));
        });
    }

    /** Triángulo con signo de exclamación: avisos y validación. */
    public static Icon aviso(int tam, Color color) {
        return vectorial(tam, color, (g, lado) -> {
            double puntaX = lado / 2.0;
            double baseY = lado * 0.84;
            double margen = lado * 0.1;
            g.draw(new Line2D.Double(puntaX, lado * 0.14, lado - margen, baseY));
            g.draw(new Line2D.Double(lado - margen, baseY, margen, baseY));
            g.draw(new Line2D.Double(margen, baseY, puntaX, lado * 0.14));
            double y = lado * 0.4;
            g.draw(new Line2D.Double(puntaX, y, puntaX, lado * 0.62));
            double r = Math.max(1.2, lado * 0.055);
            g.fill(new Ellipse2D.Double(puntaX - r, lado * 0.71 - r, r * 2, r * 2));
        });
    }

    // ---------- infraestructura ----------

    /** Traza una flecha con punta en el extremo (x2, y2). */
    private static void flecha(Graphics2D g, double x1, double y1, double x2, double y2, double lado) {
        g.draw(new Line2D.Double(x1, y1, x2, y2));
        double angulo = Math.atan2(y2 - y1, x2 - x1);
        double largo = Math.max(3, lado * 0.17);
        double a = Math.PI / 6;
        g.draw(new Line2D.Double(x2, y2,
                x2 - largo * Math.cos(angulo - a), y2 - largo * Math.sin(angulo - a)));
        g.draw(new Line2D.Double(x2, y2,
                x2 - largo * Math.cos(angulo + a), y2 - largo * Math.sin(angulo + a)));
    }

    @FunctionalInterface
    private interface Dibujo {
        void pintar(Graphics2D g, int lado);
    }

    private static Icon vectorial(int tam, Color color, Dibujo dibujo) {
        return new Icon() {
            @Override
            public int getIconWidth() {
                return tam;
            }

            @Override
            public int getIconHeight() {
                return tam;
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.translate(x, y);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(Math.max(1.5f, tam / 9f),
                        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                dibujo.pintar(g2, tam);
                g2.dispose();
            }
        };
    }
}
