package ec.edu.monster.vista;

import ec.edu.monster.modelo.RespuestaCatalogo;
import ec.edu.monster.modelo.RespuestaConversion;
import ec.edu.monster.modelo.RespuestaUnidades;
import ec.edu.monster.modelo.SolicitudConversion;
import ec.edu.monster.util.NumeroUtil;
import ec.edu.monster.util.RespuestaUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.Optional;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

/**
 * Pestaña de conversión: el usuario elige categoría y unidades de las listas
 * que entrega el servidor, escribe el valor y ve el resultado en una tarjeta
 * destacada (fondo teal, valor grande y ecuación completa copiable). El
 * cálculo y la validación los hace el servidor: aquí no hay factores ni
 * fórmulas.
 */
public class PanelConversion extends PanelTarjeta {

    private static final long serialVersionUID = 1L;

    private final transient MediadorEscritorio mediador;

    private final JComboBox<String> categoria = new JComboBox<>();
    private final JComboBox<String> origen = new JComboBox<>();
    private final JComboBox<String> destino = new JComboBox<>();
    private final JTextField valor = new JTextField();
    private final JButton intercambiar =
            Tema.botonIconoMarca(Iconos.intercambiar(18, Tema.MARCA),
                    "Intercambiar unidad de origen y destino");
    private final JButton convertir = Tema.botonPrimario("Convertir",
            Iconos.convertir(17, java.awt.Color.WHITE));
    private final JButton limpiar = Tema.botonSecundario("Limpiar", null);

    private final PanelTarjeta panelResultado = new PanelTarjeta(new BorderLayout(0, 4));
    private final JLabel resultadoPie = Tema.etiqueta("RESULTADO",
            Tema.FUENTE.deriveFont(Font.BOLD, 11.5f), Tema.MARCA_OSCURO);
    private final JLabel resultadoValor = Tema.etiqueta("Ingresa un valor y pulsa Convertir",
            Tema.MENSAJE, Tema.TEXTO_SECUNDARIO);
    private final JLabel resultadoFormula = Tema.etiqueta("", Tema.SUBTITULO, Tema.TEXTO_SECUNDARIO);
    private final JButton copiar = Tema.botonIcono(Iconos.copiar(16, Tema.MARCA_OSCURO),
            "Copiar el resultado");
    private final JProgressBar progreso = Tema.barraProgreso();
    private final JLabel estadoCarga = Tema.etiqueta("Convirtiendo…",
            Tema.PEQUENA, Tema.MARCA_OSCURO);
    private final JPanel columnaResultado = new JPanel();
    private final JPanel panelCarga = new JPanel();

    private String ultimoResultadoCopiable;
    private boolean actualizando;
    private boolean categoriasCargadas;

    public PanelConversion(MediadorEscritorio mediador) {
        this.mediador = mediador;
        construir();
        categoria.addActionListener(e -> {
            if (!actualizando) {
                cargarUnidades();
            }
        });
        intercambiar.addActionListener(e -> intercambiarUnidades());
        convertir.addActionListener(e -> convertir());
        limpiar.addActionListener(e -> limpiarCampos());
        valor.addActionListener(e -> convertir());
        copiar.addActionListener(e -> copiarResultado());
    }

    private void construir() {
        setLayout(new BorderLayout(0, 20));
        setBorder(Tema.rellenoTarjeta());
        add(encabezado(), BorderLayout.NORTH);
        add(cuerpo(), BorderLayout.CENTER);
    }

    private JComponent encabezado() {
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        JLabel titulo = Tema.etiqueta("Conversión de unidades", Tema.ENCABEZADO, Tema.NAVY);
        JLabel subtitulo = Tema.etiqueta("Elija la categoría, las unidades y escriba el valor a convertir",
                Tema.PEQUENA, Tema.TEXTO_SECUNDARIO);
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        subtitulo.setAlignmentX(LEFT_ALIGNMENT);
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(2));
        textos.add(subtitulo);
        return textos;
    }

    private JComponent cuerpo() {
        JPanel cuerpo = new JPanel(new BorderLayout(0, 18));
        cuerpo.setOpaque(false);
        cuerpo.add(formulario(), BorderLayout.NORTH);
        cuerpo.add(tarjetaResultado(), BorderLayout.CENTER);
        return cuerpo;
    }

    private JComponent formulario() {
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        categoria.setFont(Tema.FUENTE);
        origen.setFont(Tema.FUENTE);
        destino.setFont(Tema.FUENTE);
        categoria.setPreferredSize(new Dimension(0, 42));
        origen.setPreferredSize(new Dimension(0, 42));
        destino.setPreferredSize(new Dimension(0, 42));
        Tema.prepararCampo(valor, "Escriba un número, por ejemplo 12.5");
        valor.setFont(Tema.VALOR_ENTRADA);
        valor.putClientProperty("JTextField.showClearButton", Boolean.TRUE);
        valor.setPreferredSize(new Dimension(0, 44));

        c.gridy = 0;
        c.insets = new Insets(0, 0, 7, 0);
        formulario.add(Tema.etiquetaCampo("Categoría"), c);
        c.gridy = 1;
        c.insets = new Insets(0, 0, 16, 0);
        formulario.add(categoria, c);

        c.gridy = 2;
        c.insets = new Insets(0, 0, 16, 0);
        formulario.add(unidades(), c);

        c.gridy = 3;
        c.insets = new Insets(0, 0, 7, 0);
        formulario.add(Tema.etiquetaCampo("Valor"), c);
        c.gridy = 4;
        c.insets = new Insets(0, 0, 18, 0);
        formulario.add(valor, c);

        c.gridy = 5;
        c.insets = new Insets(0, 0, 0, 0);
        formulario.add(acciones(), c);
        return formulario;
    }

    private JComponent unidades() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0;
        g.insets = new Insets(0, 0, 7, 0);
        panel.add(Tema.etiquetaCampo("De"), g);
        g.gridx = 2;
        panel.add(Tema.etiquetaCampo("A"), g);

        g.gridy = 1;
        g.gridx = 0;
        g.insets = new Insets(0, 0, 0, 0);
        panel.add(origen, g);
        g.gridx = 2;
        panel.add(destino, g);

        g.gridx = 1;
        g.weightx = 0;
        g.fill = GridBagConstraints.NONE;
        g.anchor = GridBagConstraints.SOUTH;
        g.insets = new Insets(0, 10, 0, 10);
        panel.add(intercambiar, g);
        return panel;
    }

    private JComponent acciones() {
        JPanel acciones = new JPanel(new GridBagLayout());
        acciones.setOpaque(false);
        GridBagConstraints a = new GridBagConstraints();
        a.gridy = 0;
        a.anchor = GridBagConstraints.WEST;

        convertir.setPreferredSize(new Dimension(0, 44));
        a.gridx = 0;
        a.weightx = 1;
        a.fill = GridBagConstraints.HORIZONTAL;
        acciones.add(convertir, a);

        Dimension anchoLimpiar = limpiar.getPreferredSize();
        limpiar.setPreferredSize(new Dimension(anchoLimpiar.width, 44));
        a.gridx = 1;
        a.weightx = 0;
        a.fill = GridBagConstraints.NONE;
        a.insets = new Insets(0, 10, 0, 0);
        acciones.add(limpiar, a);
        return acciones;
    }

    private JComponent tarjetaResultado() {
        panelResultado.setFondo(Tema.MARCA_SUAVE);
        panelResultado.setArco(16);
        panelResultado.setBorde(null);

        JPanel contenido = new JPanel(new BorderLayout(0, 8));
        contenido.setOpaque(false);
        contenido.setBorder(new EmptyBorder(18, 24, 18, 24));

        copiar.setVisible(false);
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);
        cabecera.add(resultadoPie, BorderLayout.WEST);
        cabecera.add(copiar, BorderLayout.EAST);
        contenido.add(cabecera, BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridBagLayout());
        centro.setOpaque(false);
        columnaResultado.setOpaque(false);
        columnaResultado.setLayout(new BoxLayout(columnaResultado, BoxLayout.Y_AXIS));

        resultadoValor.setAlignmentX(LEFT_ALIGNMENT);
        resultadoFormula.setAlignmentX(LEFT_ALIGNMENT);
        columnaResultado.add(resultadoValor);
        columnaResultado.add(Box.createVerticalStrut(4));
        columnaResultado.add(resultadoFormula);

        panelCarga.setOpaque(false);
        panelCarga.setLayout(new BoxLayout(panelCarga, BoxLayout.Y_AXIS));
        progreso.setVisible(false);
        estadoCarga.setVisible(false);
        progreso.setAlignmentX(LEFT_ALIGNMENT);
        estadoCarga.setAlignmentX(LEFT_ALIGNMENT);
        panelCarga.add(progreso);
        panelCarga.add(Box.createVerticalStrut(7));
        panelCarga.add(estadoCarga);
        panelCarga.setVisible(false);
        columnaResultado.add(panelCarga);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.CENTER;
        centro.add(columnaResultado, gc);

        contenido.add(centro, BorderLayout.CENTER);
        panelResultado.add(contenido, BorderLayout.CENTER);
        mostrarVacio();
        return panelResultado;
    }

    /** Carga las categorías la primera vez que se muestra la pestaña. */
    public void alMostrar() {
        if (!categoriasCargadas) {
            cargarCategorias();
        }
    }

    public void cargarCategorias() {
        categoriasCargadas = true;
        TareaAsincrona.ejecutar(this,
                () -> mediador.catalogoControlador().obtenerCategorias(),
                this::mostrarCategorias);
    }

    private void mostrarCategorias(RespuestaCatalogo respuesta) {
        if (!RespuestaUtil.esExitosa(respuesta)) {
            mostrarError("No se pudo consultar las categorías: " + RespuestaUtil.describirError(respuesta));
            cerrarSiExpiro();
            return;
        }
        actualizando = true;
        categoria.removeAllItems();
        for (String nombre : respuesta.getCategorias()) {
            categoria.addItem(nombre);
        }
        actualizando = false;
        if (categoria.getItemCount() > 0) {
            cargarUnidades();
        }
    }

    private void cargarUnidades() {
        String seleccionada = (String) categoria.getSelectedItem();
        if (seleccionada == null) {
            return;
        }
        TareaAsincrona.ejecutar(this,
                () -> mediador.catalogoControlador().obtenerUnidades(seleccionada),
                this::mostrarUnidades);
    }

    private void mostrarUnidades(RespuestaUnidades respuesta) {
        actualizando = true;
        origen.removeAllItems();
        destino.removeAllItems();
        if (RespuestaUtil.esExitosa(respuesta)) {
            for (String unidad : respuesta.getUnidades()) {
                origen.addItem(unidad);
                destino.addItem(unidad);
            }
            if (destino.getItemCount() > 1) {
                destino.setSelectedIndex(1);
            }
        } else {
            mostrarError("No se pudo consultar las unidades: " + RespuestaUtil.describirError(respuesta));
        }
        actualizando = false;
        cerrarSiExpiro();
    }

    private void intercambiarUnidades() {
        Object de = origen.getSelectedItem();
        Object a = destino.getSelectedItem();
        if (de == null || a == null) {
            return;
        }
        actualizando = true;
        origen.setSelectedItem(a);
        destino.setSelectedItem(de);
        actualizando = false;
    }

    private void convertir() {
        String categoriaElegida = (String) categoria.getSelectedItem();
        String origenElegido = (String) origen.getSelectedItem();
        String destinoElegido = (String) destino.getSelectedItem();
        if (categoriaElegida == null || origenElegido == null || destinoElegido == null) {
            mostrarError("Seleccione la categoría y las unidades.");
            return;
        }
        Optional<Double> numero = NumeroUtil.interpretarDecimal(valor.getText());
        if (numero.isEmpty()) {
            mostrarError("Valor no válido. Escriba un número, por ejemplo 12.5");
            return;
        }
        double ingresado = numero.get();
        convertir.setEnabled(false);
        mostrarCargando();
        SolicitudConversion solicitud =
                new SolicitudConversion(categoriaElegida, origenElegido, destinoElegido, ingresado);
        TareaAsincrona.ejecutar(this,
                () -> mediador.conversionControlador().convertir(solicitud),
                respuesta -> mostrarConversion(respuesta, ingresado),
                causa -> {
                    convertir.setEnabled(true);
                    mostrarVacio();
                    TareaAsincrona.mostrarFallo(this, causa);
                });
    }

    private void mostrarConversion(RespuestaConversion respuesta, double ingresado) {
        convertir.setEnabled(true);
        if (RespuestaUtil.esExitosa(respuesta) && respuesta.getResultado() != null) {
            String destinoFinal = respuesta.getUnidadDestino();
            String formula = NumeroUtil.formatear(ingresado) + " " + respuesta.getUnidadOrigen()
                    + " = " + NumeroUtil.formatear(respuesta.getResultado()) + " " + destinoFinal;
            mostrarExito(NumeroUtil.formatear(respuesta.getResultado()) + " " + destinoFinal, formula);
        } else {
            mostrarError("No se pudo convertir: " + RespuestaUtil.describirError(respuesta));
        }
        cerrarSiExpiro();
    }

    // ---------- estados de la tarjeta de resultado ----------

    private void mostrarExito(String valorDestacado, String formula) {
        ultimoResultadoCopiable = formula;
        panelResultado.setFondo(Tema.MARCA_SUAVE);
        resultadoPie.setText("RESULTADO");
        resultadoPie.setForeground(Tema.MARCA_OSCURO);
        resultadoPie.setVisible(true);
        copiar.setVisible(true);
        cargarNodoCarga(false);
        resultadoFormula.setText(envolver(formula, Tema.SUBTITULO));
        resultadoFormula.setVisible(true);
        resultadoValor.setIcon(null);
        resultadoValor.setFont(Tema.RESULTADO);
        resultadoValor.setForeground(Tema.NAVY);
        resultadoValor.setText(envolver(valorDestacado, Tema.RESULTADO));
        resultadoValor.setVisible(true);
    }

    private void mostrarError(String texto) {
        ultimoResultadoCopiable = null;
        panelResultado.setFondo(Tema.ERROR_SUAVE);
        resultadoPie.setText("AVISO");
        resultadoPie.setForeground(Tema.ERROR);
        resultadoPie.setVisible(true);
        copiar.setVisible(false);
        cargarNodoCarga(false);
        resultadoFormula.setVisible(false);
        resultadoFormula.setText("");
        resultadoValor.setIcon(Iconos.aviso(16, Tema.ERROR));
        resultadoValor.setFont(Tema.MENSAJE);
        resultadoValor.setForeground(Tema.ERROR);
        resultadoValor.setText(envolver(texto, Tema.MENSAJE));
        resultadoValor.setVisible(true);
    }

    private void mostrarVacio() {
        ultimoResultadoCopiable = null;
        panelResultado.setFondo(Tema.MARCA_SUAVE);
        resultadoPie.setText("RESULTADO");
        resultadoPie.setForeground(Tema.MARCA_OSCURO);
        resultadoPie.setVisible(true);
        copiar.setVisible(false);
        cargarNodoCarga(false);
        resultadoFormula.setVisible(false);
        resultadoFormula.setText("");
        resultadoValor.setIcon(Iconos.convertir(16, Tema.TEXTO_SECUNDARIO));
        resultadoValor.setFont(Tema.MENSAJE);
        resultadoValor.setForeground(Tema.TEXTO_SECUNDARIO);
        resultadoValor.setText("Ingresa un valor y pulsa Convertir");
        resultadoValor.setVisible(true);
    }

    private void mostrarCargando() {
        panelResultado.setFondo(Tema.MARCA_SUAVE);
        resultadoPie.setText("RESULTADO");
        resultadoPie.setForeground(Tema.MARCA_OSCURO);
        resultadoPie.setVisible(true);
        copiar.setVisible(false);
        resultadoValor.setVisible(false);
        resultadoFormula.setVisible(false);
        cargarNodoCarga(true);
    }

    private void cargarNodoCarga(boolean visible) {
        panelCarga.setVisible(visible);
        progreso.setVisible(visible);
        estadoCarga.setVisible(visible);
    }

    /** Texto plano si cabe; con ajuste de línea (HTML) si no, para no recortar. */
    private String envolver(String texto, Font fuente) {
        if (texto == null) {
            return "";
        }
        int disponible = panelResultado.getWidth() > 0
                ? Math.max(300, panelResultado.getWidth() - 56)
                : 460;
        int ancho = resultadoValor.getFontMetrics(fuente).stringWidth(texto);
        if (ancho <= disponible) {
            return texto;
        }
        return "<html><body style='width:" + disponible + "px'>" + escapar(texto) + "</body></html>";
    }

    private void limpiarCampos() {
        valor.setText("");
        mostrarVacio();
        valor.requestFocusInWindow();
    }

    private void copiarResultado() {
        if (ultimoResultadoCopiable == null) {
            return;
        }
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(ultimoResultadoCopiable), null);
        copiar.setIcon(Iconos.exito(16, Tema.MARCA_OSCURO));
        copiar.setToolTipText("Copiado al portapapeles");
        Timer reinicio = new Timer(1600, e -> {
            copiar.setIcon(Iconos.copiar(16, Tema.MARCA_OSCURO));
            copiar.setToolTipText("Copiar el resultado");
        });
        reinicio.setRepeats(false);
        reinicio.start();
    }

    private static String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void cerrarSiExpiro() {
        if (!mediador.loginControlador().haySesionActiva()) {
            mediador.sesionCerrada();
        }
    }
}
