package ec.edu.monster.vista;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.*;
import ec.edu.monster.controlador.ConversorControlador;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Vista Android: presenta el flujo de autenticación y conversión del servicio SOAP. */
public final class MainActivity extends Activity {
    private static final int NAVY = Color.rgb(19, 48, 73);
    private static final int BLUE = Color.rgb(37, 99, 235);
    private static final int TEAL = Color.rgb(15, 133, 145);
    private static final int BG = Color.rgb(243, 247, 251);
    private static final int MUTED = Color.rgb(104, 122, 139);
    private static final int INK = Color.rgb(31, 49, 66);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ConversorControlador controlador;
    private LinearLayout contenido;
    private TextView estado;
    private EditText usuario, password, valor;
    private Spinner categorias, origen, destino;
    private ArrayAdapter<String> categoriasAdapter, unidadesAdapterOrigen, unidadesAdapterDestino;
    private String endpoint;

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        endpoint = esEmulador() ? "http://10.0.2.2:8080/WSCOMSERVIDOR/WSConversorUnidades"
                : "http://192.168.1.87:8080/WSCOMSERVIDOR/WSConversorUnidades";
        controlador = new ConversorControlador(endpoint);
        mostrarLogin();
    }
    private boolean esEmulador() {
        return Build.FINGERPRINT.startsWith("generic") || Build.FINGERPRINT.contains("emulator")
                || Build.MODEL.contains("Emulator") || Build.HARDWARE.contains("ranchu");
    }
    private void prepararPantalla(String titulo, String subtitulo) {
        getWindow().setStatusBarColor(NAVY);
        getWindow().setNavigationBarColor(NAVY);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        contenido = new LinearLayout(this);
        contenido.setOrientation(LinearLayout.VERTICAL);
        contenido.setPadding(dp(22), dp(24), dp(22), dp(28));
        contenido.setBackgroundColor(BG);
        scroll.addView(contenido);

        LinearLayout brand = new LinearLayout(this);
        brand.setGravity(Gravity.CENTER_VERTICAL);
        brand.setOrientation(LinearLayout.HORIZONTAL);
        ImageView mark = new ImageView(this);
        boolean pantallaAcceso = "Hola de nuevo".equals(titulo);
        mark.setImageResource(pantallaAcceso
                ? ec.edu.monster.R.drawable.sully_boo
                : ec.edu.monster.R.drawable.monster_mascot);
        mark.setContentDescription(pantallaAcceso
                ? "Sulley y Boo en la pantalla de acceso"
                : "Mascota Monster del conversor");
        mark.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams markLp = pantallaAcceso
                ? new LinearLayout.LayoutParams(dp(92), dp(52))
                : new LinearLayout.LayoutParams(dp(60), dp(82));
        markLp.rightMargin = dp(12);
        brand.addView(mark, markLp);
        LinearLayout brandText = new LinearLayout(this);
        brandText.setOrientation(LinearLayout.VERTICAL);
        TextView brandName = label("MONSTER", 15, TEAL);
        brandName.setTypeface(null, Typeface.BOLD);
        TextView brandCaption = label("CONVERSOR DE UNIDADES", 10, MUTED);
        brandCaption.setTypeface(null, Typeface.BOLD);
        brandText.addView(brandName);
        brandText.addView(brandCaption);
        brand.addView(brandText);
        contenido.addView(brand);

        TextView heading = label(titulo, 28, NAVY);
        heading.setTypeface(null, Typeface.BOLD);
        heading.setPadding(0, dp(24), 0, dp(4));
        contenido.addView(heading);
        TextView intro = label(subtitulo, 15, MUTED);
        intro.setPadding(0, 0, 0, dp(16));
        contenido.addView(intro);
        estado = label("", 13, Color.rgb(184, 49, 58));
        estado.setPadding(0, 0, 0, dp(8));
        contenido.addView(estado);
        setContentView(scroll);
    }
    private void mostrarLogin() {
        prepararPantalla("Hola de nuevo", "Inicia sesión con las credenciales del servicio.");
        LinearLayout card = card();
        contenido.addView(card);
        TextView cardTitle = label("Acceso", 20, NAVY);
        cardTitle.setTypeface(null, Typeface.BOLD);
        cardTitle.setPadding(0, 0, 0, dp(10));
        card.addView(cardTitle);
        usuario = editEn(card, "Usuario", false);
        password = editEn(card, "Contraseña", true);
        Button login = boton("Iniciar sesión", true, v -> {
            String userText = usuario.getText().toString().trim();
            String passwordText = password.getText().toString();
            if (userText.isEmpty() || passwordText.isEmpty()) {
                estado.setText("Completa el usuario y la contraseña para continuar.");
                return;
            }
            ejecutar("Conectando con el servidor…", () -> {
                controlador.iniciarSesion(userText, passwordText);
                return "LOGIN_OK";
            }, resultado -> { ocultarTeclado(); mostrarConversor(); });
        });
        LinearLayout.LayoutParams loginLp = new LinearLayout.LayoutParams(-1, dp(54));
        loginLp.topMargin = dp(8);
        card.addView(login, loginLp);
        TextView server = label("Servidor SOAP\n" + endpoint, 11, MUTED);
        server.setPadding(dp(2), dp(18), dp(2), 0);
        contenido.addView(server);
    }
    private void mostrarConversor() {
        prepararPantalla("Conversor", "Elige las unidades y convierte en segundos.");
        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView userLabel = label("Conversión", 17, NAVY);
        userLabel.setTypeface(null, Typeface.BOLD);
        top.addView(userLabel, new LinearLayout.LayoutParams(0, -2, 1));
        Button logout = boton("Cerrar sesión", false, v -> {
            controlador.cerrarSesion();
            mostrarLogin();
        });
        logout.setTextSize(13);
        top.addView(logout, new LinearLayout.LayoutParams(-2, dp(42)));
        contenido.addView(top);

        LinearLayout form = card();
        LinearLayout.LayoutParams formLp = new LinearLayout.LayoutParams(-1, -2);
        formLp.topMargin = dp(14);
        contenido.addView(form, formLp);
        addCaption(form, "CATEGORÍA");
        categorias = spinner(); form.addView(categorias);
        addCaption(form, "VALOR");
        valor = editEn(form, "Ej. 69,5", false);
        valor.setInputType(8194);

        LinearLayout unitsRow = new LinearLayout(this);
        unitsRow.setGravity(Gravity.CENTER_VERTICAL);
        unitsRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout fromCol = new LinearLayout(this); fromCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout toCol = new LinearLayout(this); toCol.setOrientation(LinearLayout.VERTICAL);
        fromCol.addView(caption("DE")); origen = spinner(); fromCol.addView(origen);
        toCol.addView(caption("A")); destino = spinner(); toCol.addView(destino);
        unitsRow.addView(fromCol, new LinearLayout.LayoutParams(0, -2, 1));
        TextView arrow = label("→", 23, TEAL); arrow.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams arrowLp = new LinearLayout.LayoutParams(dp(40), dp(48));
        unitsRow.addView(arrow, arrowLp);
        unitsRow.addView(toCol, new LinearLayout.LayoutParams(0, -2, 1));
        form.addView(unitsRow);

        Button swap = boton("⇄   Intercambiar unidades", false, v -> intercambiarUnidades());
        LinearLayout.LayoutParams swapLp = new LinearLayout.LayoutParams(-1, dp(46));
        swapLp.topMargin = dp(4);
        form.addView(swap, swapLp);

        LinearLayout resultCard = new LinearLayout(this);
        resultCard.setOrientation(LinearLayout.VERTICAL);
        resultCard.setPadding(dp(18), dp(16), dp(18), dp(16));
        resultCard.setBackground(shape(Color.rgb(226, 243, 244), 18));
        LinearLayout.LayoutParams resultLp = new LinearLayout.LayoutParams(-1, -2);
        resultLp.topMargin = dp(16);
        contenido.addView(resultCard, resultLp);
        TextView resultCaption = label("RESULTADO", 11, TEAL);
        resultCaption.setTypeface(null, Typeface.BOLD);
        resultCard.addView(resultCaption);
        TextView resultado = label("Ingresa un valor y pulsa Convertir", 17, NAVY);
        resultado.setTypeface(null, Typeface.BOLD);
        resultado.setPadding(0, dp(6), 0, 0);
        resultCard.addView(resultado);

        Button convert = boton("Convertir", true, v -> {
            String raw = valor.getText().toString().trim();
            if (raw.isEmpty()) { estado.setText("Ingresa el valor que deseas convertir."); return; }
            try {
                double n = Double.parseDouble(raw.replace(',', '.'));
                if (!Double.isFinite(n)) throw new NumberFormatException();
                String cat = selected(categorias), from = selected(origen), to = selected(destino);
                if (from.isEmpty() || to.isEmpty()) { estado.setText("Espera a que se carguen las unidades."); return; }
                ejecutar("Convirtiendo…", () -> controlador.convertir(cat, from, to, n),
                        r -> resultado.setText(r + " " + to));
            } catch (NumberFormatException ex) { estado.setText("El valor debe ser un número válido."); }
        });
        LinearLayout.LayoutParams convertLp = new LinearLayout.LayoutParams(-1, dp(56));
        convertLp.topMargin = dp(16);
        contenido.addView(convert, convertLp);

        categoriasAdapter = adapter(new ArrayList<>()); categorias.setAdapter(categoriasAdapter);
        unidadesAdapterOrigen = adapter(new ArrayList<>()); origen.setAdapter(unidadesAdapterOrigen);
        unidadesAdapterDestino = adapter(new ArrayList<>()); destino.setAdapter(unidadesAdapterDestino);
        categorias.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(AdapterView<?> parent) { }
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (categoriasAdapter.getCount() == 0) return;
                String categoria = categoriasAdapter.getItem(position);
                ejecutar("Cargando unidades…", () -> controlador.obtenerUnidades(categoria), items -> {
                    unidadesAdapterOrigen.clear(); unidadesAdapterOrigen.addAll(items); unidadesAdapterOrigen.notifyDataSetChanged();
                    unidadesAdapterDestino.clear(); unidadesAdapterDestino.addAll(items); unidadesAdapterDestino.notifyDataSetChanged();
                });
            }
        });
        ejecutar("Cargando categorías…", () -> controlador.obtenerCategorias(), items -> {
            categoriasAdapter.clear(); categoriasAdapter.addAll(items); categoriasAdapter.notifyDataSetChanged();
        });
    }
    private void intercambiarUnidades() {
        int from = origen.getSelectedItemPosition();
        int to = destino.getSelectedItemPosition();
        if (from >= 0 && to >= 0 && unidadesAdapterOrigen.getCount() > 0) {
            origen.setSelection(to);
            destino.setSelection(from);
        }
    }
    private interface Task<T> { T run() throws Exception; }
    private interface Success<T> { void accept(T value); }
    private <T> void ejecutar(String mensaje, Task<T> tarea, Success<T> success) {
        estado.setText(mensaje);
        executor.execute(() -> {
            try {
                T result = tarea.run();
                runOnUiThread(() -> { estado.setText(""); success.accept(result); });
            } catch (Exception e) {
                runOnUiThread(() -> estado.setText(e.getMessage() == null ? "No se pudo conectar al servidor." : e.getMessage()));
            }
        });
    }
    private EditText editEn(LinearLayout parent, String hint, boolean secret) {
        EditText input = new EditText(this); input.setSingleLine(true); input.setHint(hint);
        input.setTextSize(16); input.setTextColor(INK); input.setHintTextColor(MUTED);
        input.setPadding(dp(14), dp(8), dp(14), dp(8));
        input.setBackground(shape(Color.rgb(247, 249, 252), 13));
        if (secret) input.setInputType(129);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(52));
        lp.bottomMargin = dp(12); input.setLayoutParams(lp); parent.addView(input); return input;
    }
    private Spinner spinner() {
        Spinner s = new Spinner(this);
        s.setPadding(dp(10), 0, dp(8), 0);
        s.setBackground(shape(Color.rgb(247, 249, 252), 12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(50));
        lp.bottomMargin = dp(12); s.setLayoutParams(lp); return s;
    }
    private ArrayAdapter<String> adapter(List<String> values) {
        ArrayAdapter<String> a = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, values);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); return a;
    }
    private String selected(Spinner s) { Object item = s.getSelectedItem(); return item == null ? "" : item.toString(); }
    private Button boton(String text, boolean primary, View.OnClickListener action) {
        Button b = new Button(this); b.setText(text); b.setAllCaps(false); b.setTextSize(15);
        b.setTypeface(null, Typeface.BOLD); b.setTextColor(primary ? Color.WHITE : BLUE);
        b.setMinHeight(0); b.setMinimumHeight(0); b.setPadding(dp(14), 0, dp(14), 0);
        b.setBackground(shape(primary ? BLUE : Color.WHITE, 15));
        b.setElevation(dp(primary ? 3 : 0)); b.setOnClickListener(action);
        return b;
    }
    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(18), dp(18), dp(18), dp(18)); c.setBackground(shape(Color.WHITE, 20));
        c.setElevation(dp(2)); return c;
    }
    private void addCaption(LinearLayout parent, String text) { parent.addView(caption(text)); }
    private TextView caption(String text) {
        TextView t = label(text, 11, MUTED); t.setTypeface(null, Typeface.BOLD);
        t.setPadding(dp(2), dp(4), 0, dp(7)); return t;
    }
    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }
    private TextView label(String text, int size, int color) {
        TextView t = new TextView(this); t.setText(text); t.setTextSize(size); t.setTextColor(color); return t;
    }
    private int dp(int n) { return (int) (n * getResources().getDisplayMetrics().density + .5f); }
    private void ocultarTeclado() {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(usuario.getWindowToken(), 0);
    }
    @Override protected void onDestroy() { executor.shutdownNow(); super.onDestroy(); }
}
