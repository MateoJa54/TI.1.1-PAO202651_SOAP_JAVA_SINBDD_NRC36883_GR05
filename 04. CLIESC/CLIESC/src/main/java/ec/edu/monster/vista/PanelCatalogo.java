package ec.edu.monster.vista;

import ec.edu.monster.modelo.RespuestaCatalogo;
import ec.edu.monster.modelo.RespuestaUnidades;
import ec.edu.monster.util.RespuestaUtil;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

/**
 * Pestaña del catálogo: presenta en una tabla las categorías con sus unidades.
 * Los datos se piden al servidor; el cliente no los conoce de antemano.
 */
public class PanelCatalogo extends PanelTarjeta {

    private static final long serialVersionUID = 1L;

    private final transient MediadorEscritorio mediador;
    private final DefaultTableModel modelo;
    private final JTable tabla;
    private final JScrollPane scroll;
    private final JLabel vacio;
    private final JButton actualizar;
    private boolean consultado;

    public PanelCatalogo(MediadorEscritorio mediador) {
        this.mediador = mediador;
        this.modelo = new DefaultTableModel(new Object[] {"Categoría", "Unidades"}, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        this.tabla = crearTabla();
        this.scroll = new JScrollPane(tabla);
        this.vacio = crearVacio();
        this.actualizar = Tema.botonSecundario("Actualizar", Iconos.refrescar(16, Tema.TEXTO));
        construir();
        actualizar.addActionListener(e -> cargar());
    }

    private JTable crearTabla() {
        JTable t = new JTable(modelo);
        t.setFillsViewportHeight(true);
        t.setRowHeight(40);
        t.setFont(Tema.FUENTE);
        t.setForeground(Tema.TEXTO);
        t.setBackground(Tema.SUPERFICIE);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setGridColor(Tema.BORDE);
        t.setSelectionBackground(Tema.PRIMARIO_SUAVE);
        t.setSelectionForeground(Tema.TEXTO);
        t.setFocusable(false);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        t.setDefaultRenderer(Object.class, new Renderizador());

        JTableHeader encabezado = t.getTableHeader();
        encabezado.setReorderingAllowed(false);
        encabezado.setFont(Tema.ETIQUETA);
        encabezado.setBackground(Tema.SUPERFICIE);
        encabezado.setForeground(Tema.TEXTO_SECUNDARIO);
        encabezado.setPreferredSize(new Dimension(0, 42));

        TableColumnModel columnas = t.getColumnModel();
        columnas.getColumn(0).setPreferredWidth(190);
        columnas.getColumn(0).setMaxWidth(280);
        return t;
    }

    private JLabel crearVacio() {
        JLabel etiqueta = Tema.etiqueta("No hay categorías registradas en el servidor.",
                Tema.FUENTE, Tema.TEXTO_SECUNDARIO);
        etiqueta.setHorizontalAlignment(JLabel.CENTER);
        etiqueta.setOpaque(true);
        etiqueta.setBackground(Tema.SUPERFICIE);
        return etiqueta;
    }

    private void construir() {
        setLayout(new BorderLayout(0, 18));
        setBorder(Tema.rellenoTarjeta());

        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.BORDE));
        scroll.setViewportBorder(null);
        JViewport view = scroll.getViewport();
        view.setBackground(Tema.SUPERFICIE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(encabezado(), BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private JComponent encabezado() {
        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        JLabel titulo = Tema.etiqueta("Catálogo de unidades", Tema.ENCABEZADO, Tema.TEXTO);
        JLabel subtitulo = Tema.etiqueta("Categorías y unidades disponibles en el servidor",
                Tema.PEQUENA, Tema.TEXTO_SECUNDARIO);
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        subtitulo.setAlignmentX(LEFT_ALIGNMENT);
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(2));
        textos.add(subtitulo);
        fila.add(textos, BorderLayout.WEST);
        return fila;
    }

    /** Consulta el catálogo la primera vez que se muestra la pestaña. */
    public void alMostrar() {
        if (!consultado) {
            cargar();
        }
    }

    public void cargar() {
        consultado = true;
        actualizar.setEnabled(false);
        mostrarVacio("Consultando al servidor…");
        TareaAsincrona.ejecutar(this, this::consultar, this::mostrar, causa -> {
            actualizar.setEnabled(true);
            TareaAsincrona.mostrarFallo(this, causa);
        });
    }

    private Datos consultar() {
        RespuestaCatalogo catalogo = mediador.catalogoControlador().obtenerCategorias();
        if (!RespuestaUtil.esExitosa(catalogo)) {
            return new Datos(List.of(), "No se pudo consultar el catálogo: " + RespuestaUtil.describirError(catalogo));
        }
        if (catalogo.getCategorias().isEmpty()) {
            return new Datos(List.of(), "El servidor no tiene categorías registradas.");
        }
        List<String[]> filas = new ArrayList<>();
        for (String categoria : catalogo.getCategorias()) {
            RespuestaUnidades unidades = mediador.catalogoControlador().obtenerUnidades(categoria);
            if (RespuestaUtil.esExitosa(unidades)) {
                filas.add(new String[] {categoria, String.join(", ", unidades.getUnidades())});
            } else {
                filas.add(new String[] {categoria, RespuestaUtil.describirError(unidades)});
                if (RespuestaUtil.requiereAutenticarse(unidades)) {
                    break;
                }
            }
        }
        return new Datos(filas, "No hay datos para mostrar.");
    }

    private void mostrar(Datos datos) {
        actualizar.setEnabled(true);
        modelo.setRowCount(0);
        for (String[] fila : datos.filas()) {
            modelo.addRow(fila);
        }
        if (modelo.getRowCount() == 0) {
            mostrarVacio(datos.aviso());
        } else {
            scroll.setViewportView(tabla);
        }
        if (!mediador.loginControlador().haySesionActiva()) {
            mediador.sesionCerrada();
        }
    }

    private void mostrarVacio(String mensaje) {
        vacio.setText(mensaje);
        scroll.setViewportView(vacio);
    }

    /** Filas y mensaje para cuando no hay datos. */
    private record Datos(List<String[]> filas, String aviso) {
    }

    /** Renderizador de filas: franjas alternas, relleno y jerarquía en la primera columna. */
    private static final class Renderizador extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                boolean enfocada, int fila, int columna) {
            super.getTableCellRendererComponent(tabla, valor, seleccionada, enfocada, fila, columna);
            setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
            boolean primera = columna == 0;
            setFont(primera ? Tema.FUENTE.deriveFont(Font.BOLD) : Tema.FUENTE);
            if (seleccionada) {
                setBackground(Tema.PRIMARIO_SUAVE);
                setForeground(Tema.TEXTO);
            } else {
                setBackground(fila % 2 == 0 ? Tema.SUPERFICIE : Tema.SUPERFICIE_ALTERNA);
                setForeground(primera ? Tema.TEXTO : Tema.TEXTO_SECUNDARIO);
            }
            setOpaque(true);
            return this;
        }
    }
}
