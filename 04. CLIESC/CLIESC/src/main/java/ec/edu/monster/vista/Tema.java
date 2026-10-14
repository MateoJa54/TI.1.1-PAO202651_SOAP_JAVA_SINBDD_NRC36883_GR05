package ec.edu.monster.vista;

import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.JTextComponent;

/**
 * Define la identidad visual del cliente de escritorio: paleta, tipografía y
 * estilos de los controles. Reúne aquí todo lo que da el aspecto "premium"
 * para que las pantallas solo se preocupen por su contenido.
 *
 * <p>El estilo base es FlatLaf (un Look and Feel plano y moderno); encima se
 * afinan colores, radios y márgenes con las claves de {@link UIManager}.
 */
public final class Tema {

    // ---------- Paleta (tonos sobrios corporativos) ----------

    /** Fondo general de la aplicación (gris muy claro). */
    public static final Color FONDO = new Color(0xF4F6FA);
    /** Superficie de las tarjetas y barras. */
    public static final Color SUPERFICIE = Color.WHITE;
    /** Superficie alterna para filas de tablas. */
    public static final Color SUPERFICIE_ALTERNA = new Color(0xF8FAFC);
    /** Líneas y separadores. */
    public static final Color BORDE = new Color(0xE3E7EE);
    /** Texto principal. */
    public static final Color TEXTO = new Color(0x1F2937);
    /** Texto secundario / descriptivo. */
    public static final Color TEXTO_SECUNDARIO = new Color(0x6B7280);
    /** Texto tenue / marcadores de posición. */
    public static final Color TEXTO_TENUE = new Color(0x9AA4B2);
    /** Color de marca (azul corporativo). */
    public static final Color PRIMARIO = new Color(0x2563EB);
    /** Variante oscura de la marca (degradados y presionado). */
    public static final Color PRIMARIO_OSCURO = new Color(0x1D4ED8);
    /** Tinte claro de la marca (fondos seleccionados). */
    public static final Color PRIMARIO_SUAVE = new Color(0xE8F0FE);
    /** Éxito. */
    public static final Color EXITO = new Color(0x15803D);
    public static final Color EXITO_SUAVE = new Color(0xE7F6EC);
    /** Error. */
    public static final Color ERROR = new Color(0xB91C1C);
    public static final Color ERROR_SUAVE = new Color(0xFCEDED);
    /** Sombra de las tarjetas. */
    public static final Color SOMBRA = new Color(0x0F172A);

    // ---------- Tipografía ----------

    private static final String FAMILIA = elegirFamilia();
    public static final FontUIResource FUENTE = new FontUIResource(FAMILIA, java.awt.Font.PLAIN, 13);
    public static final java.awt.Font TITULO = FUENTE.deriveFont(java.awt.Font.BOLD, 22f);
    public static final java.awt.Font SUBTITULO = FUENTE.deriveFont(java.awt.Font.PLAIN, 12.5f);
    public static final java.awt.Font ENCABEZADO = FUENTE.deriveFont(java.awt.Font.BOLD, 16f);
    public static final java.awt.Font ETIQUETA = FUENTE.deriveFont(java.awt.Font.BOLD, 12f);
    public static final java.awt.Font BOTON = FUENTE.deriveFont(java.awt.Font.BOLD, 13f);
    public static final java.awt.Font PEQUENA = FUENTE.deriveFont(java.awt.Font.PLAIN, 11.5f);
    public static final java.awt.Font RESULTADO = FUENTE.deriveFont(java.awt.Font.BOLD, 27f);

    private Tema() {
    }

    /**
     * Instala el Look and Feel y afina los estilos globales. Debe llamarse
     * una sola vez, al arrancar y antes de crear cualquier componente.
     */
    public static void instalar() {
        FlatLightLaf.setup();
        UIManager.put("defaultFont", FUENTE);

        // Marca y radios de los controles.
        UIManager.put("Component.accentColor", PRIMARIO);
        UIManager.put("Component.arc", 12);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 0);

        // Botones.
        UIManager.put("Button.arc", 12);
        UIManager.put("Button.margin", new Insets(9, 18, 9, 18));
        UIManager.put("Button.default.buttonType", "roundRect");

        // Campos de texto.
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("Component.focusColor", PRIMARIO_SUAVE);

        // Pestañas tipo "subrayado": limpias y minimalistas.
        UIManager.put("TabbedPane.tabType", "underlined");
        UIManager.put("TabbedPane.showTabSeparators", Boolean.FALSE);
        UIManager.put("TabbedPane.tabHeight", 42);
        UIManager.put("TabbedPane.tabInsets", new Insets(8, 18, 8, 18));
        UIManager.put("TabbedPane.selectedForeground", PRIMARIO);
        UIManager.put("TabbedPane.underlineColor", PRIMARIO);
        UIManager.put("TabbedPane.hoverColor", PRIMARIO_SUAVE);
        UIManager.put("TabbedPane.focusColor", PRIMARIO_SUAVE);

        // Tablas.
        UIManager.put("Table.showHorizontalLines", Boolean.TRUE);
        UIManager.put("Table.showVerticalLines", Boolean.FALSE);
        UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
        UIManager.put("Table.gridColor", BORDE);
        UIManager.put("Table.selectionBackground", PRIMARIO_SUAVE);
        UIManager.put("Table.selectionForeground", TEXTO);
        UIManager.put("Table.rowHeight", 38);

        // Barras de desplazamiento discretas.
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        UIManager.put("ScrollBar.track", FONDO);

        // Globos y separadores coherentes.
        UIManager.put("Separator.foreground", BORDE);
    }

    // ---------- Fábricas de componentes ----------

    /** Botón principal: relleno con el color de marca y texto blanco. */
    public static JButton botonPrimario(String texto, Icon icono) {
        JButton boton = new JButton(texto, icono);
        boton.setFont(BOTON);
        boton.putClientProperty("JButton.buttonType", "roundRect");
        boton.setBackground(PRIMARIO);
        boton.setForeground(Color.WHITE);
        return prepararBoton(boton);
    }

    /** Botón secundario: neutro, con borde, para acciones de apoyo. */
    public static JButton botonSecundario(String texto, Icon icono) {
        JButton boton = new JButton(texto, icono);
        boton.setFont(BOTON);
        boton.putClientProperty("JButton.buttonType", "roundRect");
        boton.setBackground(SUPERFICIE);
        boton.setForeground(TEXTO);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(9, 16, 9, 16)));
        return prepararBoton(boton);
    }

    /** Botón fantasma: sin fondo ni borde, para las acciones de la barra. */
    public static JButton botonFantasma(String texto, Icon icono) {
        JButton boton = new JButton(texto, icono);
        boton.setFont(BOTON);
        boton.putClientProperty("JButton.buttonType", "borderless");
        boton.setForeground(TEXTO_SECUNDARIO);
        boton.setBackground(new Color(0, 0, 0, 0));
        return prepararBoton(boton);
    }

    /** Botón solo icono, cuadrado, para acciones como "intercambiar". */
    public static JButton botonIcono(Icon icono, String descripcion) {
        JButton boton = new JButton(icono);
        boton.putClientProperty("JButton.buttonType", "roundRect");
        boton.setBackground(SUPERFICIE);
        boton.setToolTipText(descripcion);
        boton.setPreferredSize(new Dimension(40, 40));
        return prepararBoton(boton);
    }

    private static JButton prepararBoton(JButton boton) {
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setFocusPainted(false);
        return boton;
    }

    /** Etiqueta con fuente y color a medida. */
    public static JLabel etiqueta(String texto, java.awt.Font fuente, Color color) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(fuente);
        etiqueta.setForeground(color);
        return etiqueta;
    }

    /** Etiqueta de campo de formulario (pequeña, en mayúsculas sutiles). */
    public static JLabel etiquetaCampo(String texto) {
        return etiqueta(texto, ETIQUETA, TEXTO_SECUNDARIO);
    }

    /** Aplica la tipografía y, opcionalmente, un texto de ayuda al campo. */
    public static void prepararCampo(JTextComponent campo, String marcador) {
        campo.setFont(FUENTE);
        if (marcador != null) {
            campo.putClientProperty("JTextField.placeholderText", marcador);
        }
    }

    /** Borde interior estándar para el contenido de una tarjeta. */
    public static Border rellenoTarjeta() {
        return BorderFactory.createEmptyBorder(26, 28, 26, 28);
    }

    private static String elegirFamilia() {
        Set<String> disponibles;
        try {
            disponibles = new HashSet<>(Arrays.asList(
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        } catch (RuntimeException e) {
            return java.awt.Font.SANS_SERIF;
        }
        for (String candidata : new String[] {
            "Segoe UI Variable Text", "Segoe UI", "Inter", "Helvetica Neue", "Roboto", "Noto Sans"}) {
            if (disponibles.contains(candidata)) {
                return candidata;
            }
        }
        return java.awt.Font.SANS_SERIF;
    }
}
