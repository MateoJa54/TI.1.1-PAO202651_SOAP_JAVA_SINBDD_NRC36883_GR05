package ec.edu.monster.vista;

import ec.edu.monster.modelo.RespuestaCatalogo;
import ec.edu.monster.modelo.RespuestaConversion;
import ec.edu.monster.modelo.RespuestaUnidades;
import ec.edu.monster.modelo.SolicitudConversion;
import ec.edu.monster.util.NumeroUtil;
import ec.edu.monster.util.RespuestaUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Optional;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Pestaña de conversión: el usuario elige categoría y unidades de las listas
 * que entrega el servidor, escribe el valor y ve el resultado en una tarjeta
 * destacada. El cálculo y la validación los hace el servidor: aquí no hay
 * factores ni fórmulas.
 */
public class PanelConversion extends PanelTarjeta {

    private static final long serialVersionUID = 1L;

    private final transient MediadorEscritorio mediador;

    private final JComboBox<String> categoria = new JComboBox<>();
    private final JComboBox<String> origen = new JComboBox<>();
    private final JComboBox<String> destino = new JComboBox<>();
    private final JTextField valor = new JTextField();
    private final JButton intercambiar = Tema.botonIcono(Iconos.intercambiar(18, Tema.TEXTO),
            "Intercambiar unidad de origen y destino");
    private final JButton convertir = Tema.botonPrimario("Convertir", Iconos.convertir(17, java.awt.Color.WHITE));
    private final JLabel resultado = new JLabel("El resultado aparecerá aquí");
    private final PanelTarjeta panelResultado = new PanelTarjeta(new GridBagLayout());

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
        valor.addActionListener(e -> convertir());
    }

    private void construir() {
        setLayout(new BorderLayout(0, 22));
        setBorder(Tema.rellenoTarjeta());
        add(encabezado(), BorderLayout.NORTH);
        add(cuerpo(), BorderLayout.CENTER);
    }

    private JComponent encabezado() {
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        JLabel titulo = Tema.etiqueta("Conversión de unidades", Tema.ENCABEZADO, Tema.TEXTO);
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
        JPanel cuerpo = new JPanel(new BorderLayout(0, 22));
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
        categoria.setPreferredSize(new Dimension(0, 40));
        origen.setPreferredSize(new Dimension(0, 40));
        destino.setPreferredSize(new Dimension(0, 40));
        Tema.prepararCampo(valor, "Escriba un número, por ejemplo 12.5");
        valor.putClientProperty("JTextField.showClearButton", Boolean.TRUE);
        valor.setPreferredSize(new Dimension(0, 40));

        c.gridy = 0;
        c.insets = new Insets(0, 0, 6, 0);
        formulario.add(Tema.etiquetaCampo("Categoría"), c);
        c.gridy = 1;
        c.insets = new Insets(0, 0, 18, 0);
        formulario.add(categoria, c);

        c.gridy = 2;
        c.insets = new Insets(0, 0, 18, 0);
        formulario.add(unidades(), c);

        c.gridy = 3;
        c.insets = new Insets(0, 0, 6, 0);
        formulario.add(Tema.etiquetaCampo("Valor"), c);
        c.gridy = 4;
        c.insets = new Insets(0, 0, 22, 0);
        formulario.add(valor, c);

        convertir.setPreferredSize(new Dimension(0, 44));
        c.gridy = 5;
        c.insets = new Insets(0, 0, 0, 0);
        formulario.add(convertir, c);
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
        g.insets = new Insets(0, 0, 6, 0);
        panel.add(Tema.etiquetaCampo("Unidad de origen"), g);
        g.gridx = 2;
        panel.add(Tema.etiquetaCampo("Unidad de destino"), g);

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
        g.insets = new Insets(0, 12, 0, 12);
        panel.add(intercambiar, g);
        return panel;
    }

    private JComponent tarjetaResultado() {
        panelResultado.setFondo(Tema.FONDO);
        panelResultado.setArco(14);
        panelResultado.setBorde(null);
        resultado.setHorizontalAlignment(JLabel.CENTER);
        resultado.setFont(Tema.FUENTE);
        resultado.setForeground(Tema.TEXTO_SECUNDARIO);
        panelResultado.add(resultado);
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
            mostrarInfo("Seleccione la categoría y las unidades.");
            return;
        }
        Optional<Double> numero = NumeroUtil.interpretarDecimal(valor.getText());
        if (numero.isEmpty()) {
            mostrarInfo("Valor no válido. Escriba un número, por ejemplo 12.5");
            return;
        }
        double ingresado = numero.get();
        convertir.setEnabled(false);
        SolicitudConversion solicitud =
                new SolicitudConversion(categoriaElegida, origenElegido, destinoElegido, ingresado);
        TareaAsincrona.ejecutar(this,
                () -> mediador.conversionControlador().convertir(solicitud),
                respuesta -> mostrarConversion(respuesta, ingresado),
                causa -> {
                    convertir.setEnabled(true);
                    TareaAsincrona.mostrarFallo(this, causa);
                });
    }

    private void mostrarConversion(RespuestaConversion respuesta, double ingresado) {
        convertir.setEnabled(true);
        if (RespuestaUtil.esExitosa(respuesta) && respuesta.getResultado() != null) {
            mostrarExito(NumeroUtil.formatear(ingresado) + " " + respuesta.getUnidadOrigen()
                    + " = " + NumeroUtil.formatear(respuesta.getResultado()) + " " + respuesta.getUnidadDestino());
        } else {
            mostrarError("No se pudo convertir: " + RespuestaUtil.describirError(respuesta));
        }
        cerrarSiExpiro();
    }

    private void mostrarExito(String texto) {
        panelResultado.setFondo(Tema.EXITO_SUAVE);
        resultado.setFont(Tema.RESULTADO);
        resultado.setForeground(Tema.EXITO);
        resultado.setText(texto);
    }

    private void mostrarInfo(String texto) {
        panelResultado.setFondo(Tema.FONDO);
        resultado.setFont(Tema.FUENTE);
        resultado.setForeground(Tema.TEXTO_SECUNDARIO);
        resultado.setText(texto);
    }

    private void mostrarError(String texto) {
        panelResultado.setFondo(Tema.ERROR_SUAVE);
        resultado.setFont(Tema.FUENTE);
        resultado.setForeground(Tema.ERROR);
        resultado.setText("<html><body style='width:460px'>" + escapar(texto) + "</body></html>");
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
