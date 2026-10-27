package ec.edu.monster.vista;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.LinearGradient;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.text.InputType;
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
    private static final int LOGIN_PURPLE = Color.rgb(100, 82, 239);
    private static final int LOGIN_BLUE = Color.rgb(66, 166, 235);
    private static final int LOGIN_MINT = Color.rgb(72, 224, 198);
    private static final int CONVERTER_BG = Color.rgb(7, 22, 46);
    private static final int CONVERTER_PANEL = Color.rgb(11, 34, 65);
    private static final int CONVERTER_FIELD = Color.rgb(18, 48, 83);
    private static final int CONVERTER_TEXT = Color.rgb(239, 246, 255);
    private static final int CONVERTER_MUTED = Color.rgb(143, 177, 220);
    private static final int CONVERTER_CYAN = Color.rgb(48, 209, 238);
    private static final int CONVERTER_PINK = Color.rgb(235, 133, 210);
    private static final String SOAP_PATH = "/WSCOMSERVIDOR/WSConversorUnidades";
    private static final String PREFS = "soap_java_settings";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ConversorControlador controlador;
    private LinearLayout contenido;
    private TextView estado;
    private EditText usuario, password, valor;
    private TextView loginServerAddress;
    private Spinner categorias, origen, destino;
    private ArrayAdapter<String> categoriasAdapter, unidadesAdapterOrigen, unidadesAdapterDestino;
    private String endpoint;

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        String hostDefault = esEmulador() ? "10.0.2.2" : "192.168.1.87";
        String host = getSharedPreferences(PREFS, MODE_PRIVATE).getString("server_host", hostDefault);
        endpoint = crearEndpoint(host);
        controlador = new ConversorControlador(endpoint);
        mostrarLogin();
    }
    private boolean esEmulador() {
        return Build.FINGERPRINT.startsWith("generic") || Build.FINGERPRINT.contains("emulator")
                || Build.MODEL.contains("Emulator") || Build.HARDWARE.contains("ranchu");
    }
    private void prepararPantalla(String titulo, String subtitulo) {
        getWindow().setStatusBarColor(CONVERTER_BG);
        getWindow().setNavigationBarColor(CONVERTER_BG);
        getWindow().getDecorView().setSystemUiVisibility(0);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        contenido = new LinearLayout(this);
        contenido.setOrientation(LinearLayout.VERTICAL);
        contenido.setPadding(dp(22), dp(14), dp(22), dp(18));
        contenido.setBackgroundColor(CONVERTER_BG);
        scroll.addView(contenido);

        boolean pantallaAcceso = "Inicia sesión".equals(titulo);
        if (pantallaAcceso) {
            contenido.addView(new View(this), new LinearLayout.LayoutParams(1, 0, 0.5f));
            ImageView hero = new ImageView(this);
            hero.setImageResource(ec.edu.monster.R.drawable.sully_boo);
            hero.setContentDescription("Sulley y Boo: bienvenida a CLIMOV");
            hero.setScaleType(ImageView.ScaleType.CENTER_CROP);
            hero.setBackground(shape(Color.WHITE, 22));
            hero.setClipToOutline(true);
            hero.setElevation(dp(3));
            contenido.addView(hero, new LinearLayout.LayoutParams(-1, dp(170)));
            TextView brand = label("MONSTER  ·  CONVERSOR DE UNIDADES", 12, TEAL);
            brand.setTypeface(null, Typeface.BOLD);
            brand.setGravity(Gravity.CENTER);
            brand.setPadding(0, dp(14), 0, dp(3));
            contenido.addView(brand);
        } else {
            LinearLayout brand = new LinearLayout(this);
            brand.setGravity(Gravity.CENTER_VERTICAL);
            brand.setOrientation(LinearLayout.HORIZONTAL);
            ImageView mark = new ImageView(this);
            mark.setImageResource(ec.edu.monster.R.drawable.monster_mascot);
            mark.setContentDescription("Mascota Monster del conversor");
            mark.setScaleType(ImageView.ScaleType.FIT_CENTER);
            LinearLayout.LayoutParams markLp = new LinearLayout.LayoutParams(dp(60), dp(82));
            markLp.rightMargin = dp(12);
            brand.addView(mark, markLp);
            LinearLayout brandText = new LinearLayout(this);
            brandText.setOrientation(LinearLayout.VERTICAL);
            TextView brandName = label("MONSTER", 15, CONVERTER_TEXT);
            brandName.setTypeface(null, Typeface.BOLD);
            TextView brandCaption = label("CONVERSOR DE UNIDADES", 10, CONVERTER_CYAN);
            brandCaption.setTypeface(null, Typeface.BOLD);
            brandText.addView(brandName);
            brandText.addView(brandCaption);
            brand.addView(brandText);
            contenido.addView(brand);
        }

        TextView heading = label(titulo, pantallaAcceso ? 29 : 28, CONVERTER_TEXT);
        heading.setTypeface(null, Typeface.BOLD);
        heading.setGravity(pantallaAcceso ? Gravity.CENTER : Gravity.START);
        heading.setPadding(0, pantallaAcceso ? dp(12) : dp(24), 0, dp(4));
        contenido.addView(heading);
        TextView intro = label(subtitulo, 15, CONVERTER_MUTED);
        intro.setGravity(pantallaAcceso ? Gravity.CENTER : Gravity.START);
        intro.setPadding(0, 0, 0, dp(16));
        contenido.addView(intro);
        estado = label("", 13, CONVERTER_PINK);
        estado.setPadding(0, 0, 0, dp(8));
        contenido.addView(estado);
        setContentView(scroll);
    }
    private void mostrarLogin() {
        getWindow().setStatusBarColor(LOGIN_BLUE);
        getWindow().setNavigationBarColor(LOGIN_BLUE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        FrameLayout root = new FrameLayout(this);
        LoginBackdrop backdrop = new LoginBackdrop(this);
        root.addView(backdrop, new FrameLayout.LayoutParams(-1, -1));
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.CENTER_HORIZONTAL);
        page.setPadding(dp(28), dp(26), dp(28), dp(24));
        scroll.addView(page);
        root.addView(scroll, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);

        ImageView hero = new ImageView(this);
        hero.setImageResource(ec.edu.monster.R.drawable.login_monster);
        hero.setContentDescription("Sulley y Boo: portada de SOAP_JAVA");
        hero.setScaleType(ImageView.ScaleType.FIT_CENTER);
        hero.setBackground(shape(Color.WHITE, 26));
        hero.setClipToOutline(true);
        hero.setElevation(dp(4));
        LinearLayout.LayoutParams heroLp = new LinearLayout.LayoutParams(-1, dp(280));
        heroLp.topMargin = dp(8);
        heroLp.bottomMargin = dp(20);
        page.addView(hero, heroLp);

        TextView title = label("SOAP_JAVA", 27, NAVY);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        page.addView(title);
        TextView subtitle = label("Conversor de unidades", 15, MUTED);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subtitleLp = new LinearLayout.LayoutParams(-1, -2);
        subtitleLp.bottomMargin = dp(28);
        page.addView(subtitle, subtitleLp);

        usuario = loginField("Usuario", false, ec.edu.monster.R.drawable.ic_person);
        LinearLayout.LayoutParams userLp = new LinearLayout.LayoutParams(-1, dp(54));
        userLp.bottomMargin = dp(14);
        page.addView(usuario, userLp);
        password = loginField("Contraseña", true, ec.edu.monster.R.drawable.ic_lock);
        LinearLayout.LayoutParams passwordLp = new LinearLayout.LayoutParams(-1, dp(54));
        passwordLp.bottomMargin = dp(12);
        page.addView(password, passwordLp);

        TextView serverLabel = label("Servidor SOAP", 13, MUTED);
        serverLabel.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams serverLabelLp = new LinearLayout.LayoutParams(-1, -2);
        serverLabelLp.leftMargin = dp(8);
        serverLabelLp.topMargin = dp(1);
        page.addView(serverLabel, serverLabelLp);
        LinearLayout serverRow = new LinearLayout(this);
        serverRow.setGravity(Gravity.CENTER_VERTICAL);
        serverRow.setPadding(dp(14), 0, dp(12), 0);
        serverRow.setBackground(shape(Color.rgb(225, 242, 252), 24));
        loginServerAddress = label(hostDesdeEndpoint(endpoint), 14, Color.rgb(57, 127, 178));
        loginServerAddress.setSingleLine(true);
        loginServerAddress.setEllipsize(android.text.TextUtils.TruncateAt.MIDDLE);
        serverRow.addView(loginServerAddress, new LinearLayout.LayoutParams(0, dp(48), 1));
        TextView configureServer = label("Cambiar", 13, LOGIN_PURPLE);
        configureServer.setTypeface(null, Typeface.BOLD);
        configureServer.setGravity(Gravity.CENTER);
        configureServer.setPadding(dp(10), 0, dp(4), 0);
        configureServer.setOnClickListener(v -> mostrarDialogoServidor());
        serverRow.addView(configureServer, new LinearLayout.LayoutParams(-2, dp(48)));
        LinearLayout.LayoutParams serverRowLp = new LinearLayout.LayoutParams(-1, dp(50));
        serverRowLp.bottomMargin = dp(22);
        page.addView(serverRow, serverRowLp);

        Button login = loginButton("Iniciar sesión", v -> {
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
        LinearLayout.LayoutParams loginLp = new LinearLayout.LayoutParams(-1, dp(56));
        page.addView(login, loginLp);
        estado = label("", 13, Color.rgb(176, 42, 57));
        estado.setGravity(Gravity.CENTER);
        estado.setPadding(dp(4), dp(12), dp(4), 0);
        page.addView(estado, new LinearLayout.LayoutParams(-1, -2));
    }

    private EditText loginField(String hint, boolean secret, int icon) {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint(hint);
        input.setTextSize(16);
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.rgb(239, 249, 255));
        input.setPadding(dp(18), 0, dp(18), 0);
        input.setGravity(Gravity.CENTER_VERTICAL);
        input.setBackground(shape(Color.rgb(119, 195, 241), 30));
        input.setCompoundDrawablePadding(dp(10));
        input.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
        input.setCompoundDrawableTintList(ColorStateList.valueOf(Color.WHITE));
        input.setInputType(secret ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        return input;
    }

    private Button loginButton(String text, View.OnClickListener action) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextSize(17);
        b.setTypeface(null, Typeface.BOLD);
        b.setTextColor(Color.WHITE);
        b.setMinHeight(0);
        b.setMinimumHeight(0);
        b.setBackground(shape(LOGIN_PURPLE, 30));
        b.setElevation(dp(3));
        b.setOnClickListener(action);
        return b;
    }

    private void mostrarDialogoServidor() {
        EditText host = new EditText(this);
        host.setSingleLine(true);
        host.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        host.setHint("IP o host, ej. 192.168.1.87");
        host.setText(hostDesdeEndpoint(endpoint));
        host.setSelectAllOnFocus(true);
        int pad = dp(20);
        FrameLayout box = new FrameLayout(this);
        box.setPadding(pad, dp(4), pad, 0);
        box.addView(host, new FrameLayout.LayoutParams(-1, -2));
        new android.app.AlertDialog.Builder(this)
                .setTitle("Conectar con Payara")
                .setMessage("Escribe la IP o el nombre del computador donde está activo el servidor. El teléfono debe poder acceder a esa red.")
                .setView(box)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    try {
                        String newHost = normalizarHost(host.getText().toString());
                        endpoint = crearEndpoint(newHost);
                        controlador = new ConversorControlador(endpoint);
                        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString("server_host", newHost).apply();
                        loginServerAddress.setText(newHost);
                        estado.setText("Servidor configurado. Inicia sesión para conectarte.");
                    } catch (IllegalArgumentException ex) {
                        estado.setText(ex.getMessage());
                    }
                }).show();
    }

    private String normalizarHost(String value) {
        String host = value == null ? "" : value.trim();
        host = host.replaceFirst("^https?://", "");
        int slash = host.indexOf('/');
        if (slash >= 0) host = host.substring(0, slash);
        if (host.endsWith(".")) host = host.substring(0, host.length() - 1);
        if (host.isEmpty() || host.contains(" ")) throw new IllegalArgumentException("Escribe una IP o nombre de servidor válido.");
        if (!host.matches("[A-Za-z0-9._:-]+")) throw new IllegalArgumentException("La dirección del servidor contiene caracteres no válidos.");
        return host;
    }

    private String crearEndpoint(String host) {
        String normalized = normalizarHost(host);
        String authority = normalized.contains(":") && !normalized.startsWith("[") ? normalized : normalized + ":8080";
        return "http://" + authority + SOAP_PATH;
    }

    private String hostDesdeEndpoint(String value) {
        String host = value.replaceFirst("^https?://", "");
        int slash = host.indexOf('/');
        if (slash >= 0) host = host.substring(0, slash);
        if (host.endsWith(":8080")) host = host.substring(0, host.length() - 5);
        return host;
    }
    private void mostrarConversor() {
        prepararPantalla("Conversor de Unidades", "Elige las unidades y convierte en segundos.");
        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView userLabel = label("Conversión", 17, CONVERTER_TEXT);
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
        valor.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);

        LinearLayout unitsRow = new LinearLayout(this);
        unitsRow.setGravity(Gravity.CENTER_VERTICAL);
        unitsRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout fromCol = new LinearLayout(this); fromCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout toCol = new LinearLayout(this); toCol.setOrientation(LinearLayout.VERTICAL);
        fromCol.addView(caption("DE")); origen = spinner(); fromCol.addView(origen);
        toCol.addView(caption("A")); destino = spinner(); toCol.addView(destino);
        unitsRow.addView(fromCol, new LinearLayout.LayoutParams(0, -2, 1));
        TextView arrow = label("⇄", 23, CONVERTER_CYAN); arrow.setGravity(Gravity.CENTER);
        arrow.setBackground(shape(CONVERTER_FIELD, 100));
        LinearLayout.LayoutParams arrowLp = new LinearLayout.LayoutParams(dp(48), dp(48));
        arrowLp.leftMargin = dp(4);
        arrowLp.rightMargin = dp(4);
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
        resultCard.setBackground(gradient(new int[]{Color.rgb(12, 101, 111), Color.rgb(57, 42, 111)}, 20));
        resultCard.setElevation(dp(4));
        LinearLayout.LayoutParams resultLp = new LinearLayout.LayoutParams(-1, -2);
        resultLp.topMargin = dp(16);
        contenido.addView(resultCard, resultLp);
        TextView resultCaption = label("RESULTADO", 12, CONVERTER_CYAN);
        resultCaption.setTypeface(null, Typeface.BOLD);
        resultCard.addView(resultCaption);
        TextView resultado = label("Ingresa un valor y pulsa Convertir", 17, CONVERTER_TEXT);
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
        contenido.addView(new View(this), new LinearLayout.LayoutParams(1, dp(12)));
        agregarMenu(false);

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

    private void agregarMenu(boolean medidasActivas) {
        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.HORIZONTAL);
        menu.setPadding(dp(5), dp(5), dp(5), dp(5));
        GradientDrawable menuBg = shape(CONVERTER_PANEL, 28);
        menuBg.setStroke(dp(1), Color.rgb(29, 77, 124));
        menu.setBackground(menuBg);
        LinearLayout.LayoutParams menuLp = new LinearLayout.LayoutParams(-1, dp(58));
        menuLp.topMargin = dp(10);
        menuLp.bottomMargin = dp(14);
        contenido.addView(menu, menuLp);
        Button conversorTab = menuButton("⇄   Conversor", !medidasActivas, v -> mostrarConversor());
        Button medidasTab = menuButton("▦   Medidas", medidasActivas, v -> mostrarMedidas());
        menu.addView(conversorTab, new LinearLayout.LayoutParams(0, -1, 1));
        menu.addView(medidasTab, new LinearLayout.LayoutParams(0, -1, 1));
    }

    private Button menuButton(String text, boolean active, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextSize(14);
        b.setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
        b.setTextColor(active ? Color.WHITE : CONVERTER_MUTED);
        b.setMinHeight(0);
        b.setMinimumHeight(0);
        b.setPadding(dp(4), 0, dp(4), 0);
        b.setBackground(shape(active ? LOGIN_PURPLE : Color.TRANSPARENT, 22));
        b.setOnClickListener(listener);
        return b;
    }

    private void mostrarMedidas() {
        prepararPantalla("Medidas", "Explora las categorías y unidades disponibles.");
        LinearLayout lista = new LinearLayout(this);
        lista.setOrientation(LinearLayout.VERTICAL);
        contenido.addView(lista, new LinearLayout.LayoutParams(-1, -2));
        agregarMenu(true);
        ejecutar("Consultando medidas del servidor…", () -> {
            java.util.LinkedHashMap<String, List<String>> catalogo = new java.util.LinkedHashMap<>();
            for (String categoria : controlador.obtenerCategorias()) {
                catalogo.put(categoria, controlador.obtenerUnidades(categoria));
            }
            return catalogo;
        }, catalogo -> {
            lista.removeAllViews();
            for (java.util.Map.Entry<String, List<String>> item : catalogo.entrySet()) {
                LinearLayout categoryCard = card();
                LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(-1, -2);
                cardLp.bottomMargin = dp(12);
                lista.addView(categoryCard, cardLp);
                TextView category = label(item.getKey(), 17, CONVERTER_TEXT);
                category.setTypeface(null, Typeface.BOLD);
                categoryCard.addView(category);
                TextView units = label(juntarUnidades(item.getValue()), 14, CONVERTER_MUTED);
                units.setPadding(0, dp(8), 0, 0);
                categoryCard.addView(units);
            }
        });
    }

    private String juntarUnidades(List<String> units) {
        StringBuilder joined = new StringBuilder();
        for (String unit : units) {
            if (joined.length() > 0) joined.append("  ·  ");
            joined.append(unit);
        }
        return joined.toString();
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
        input.setTextSize(16); input.setTextColor(CONVERTER_TEXT); input.setHintTextColor(CONVERTER_MUTED);
        input.setPadding(dp(14), dp(8), dp(14), dp(8));
        input.setBackground(shape(CONVERTER_FIELD, 14));
        input.setCompoundDrawablePadding(dp(10));
        input.setCompoundDrawablesWithIntrinsicBounds(secret
                ? ec.edu.monster.R.drawable.ic_lock : ec.edu.monster.R.drawable.ic_person, 0, 0, 0);
        input.setCompoundDrawableTintList(ColorStateList.valueOf(CONVERTER_MUTED));
        if (secret) input.setInputType(129);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(52));
        lp.bottomMargin = dp(12); input.setLayoutParams(lp); parent.addView(input); return input;
    }
    private Spinner spinner() {
        Spinner s = new Spinner(this);
        s.setPadding(dp(10), 0, dp(8), 0);
        s.setBackground(shape(CONVERTER_FIELD, 14));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(50));
        lp.bottomMargin = dp(12); s.setLayoutParams(lp); return s;
    }
    private ArrayAdapter<String> adapter(List<String> values) {
        ArrayAdapter<String> a = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, values) {
            @Override public View getView(int position, View convertView, android.view.ViewGroup parent) {
                TextView item = (TextView) super.getView(position, convertView, parent);
                item.setTextColor(CONVERTER_TEXT);
                item.setTextSize(14);
                return item;
            }
            @Override public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                TextView item = (TextView) super.getDropDownView(position, convertView, parent);
                item.setTextColor(CONVERTER_TEXT);
                item.setTextSize(15);
                item.setPadding(dp(16), dp(13), dp(16), dp(13));
                item.setBackgroundColor(CONVERTER_PANEL);
                return item;
            }
        };
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); return a;
    }
    private String selected(Spinner s) { Object item = s.getSelectedItem(); return item == null ? "" : item.toString(); }
    private Button boton(String text, boolean primary, View.OnClickListener action) {
        Button b = new Button(this); b.setText(text); b.setAllCaps(false); b.setTextSize(15);
        b.setTypeface(null, Typeface.BOLD); b.setTextColor(Color.WHITE);
        b.setMinHeight(0); b.setMinimumHeight(0); b.setPadding(dp(14), 0, dp(14), 0);
        GradientDrawable bg = primary
                ? gradient(new int[]{CONVERTER_CYAN, LOGIN_PURPLE}, 18)
                : shape(CONVERTER_PANEL, 18);
        if (!primary) bg.setStroke(dp(1), Color.rgb(39, 104, 160));
        b.setBackground(bg);
        b.setElevation(dp(primary ? 4 : 0)); b.setOnClickListener(action);
        return b;
    }
    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(18), dp(18), dp(18), dp(18));
        GradientDrawable bg = shape(CONVERTER_PANEL, 24);
        bg.setStroke(dp(1), Color.rgb(24, 75, 125));
        c.setBackground(bg);
        c.setElevation(dp(3)); return c;
    }
    private void addCaption(LinearLayout parent, String text) { parent.addView(caption(text)); }
    private TextView caption(String text) {
        TextView t = label(text, 13, CONVERTER_MUTED); t.setTypeface(null, Typeface.BOLD);
        t.setPadding(dp(2), dp(4), 0, dp(7)); return t;
    }
    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }
    private GradientDrawable gradient(int[] colors, int radius) {
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
        d.setCornerRadius(dp(radius));
        return d;
    }
    private final class LoginBackdrop extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        LoginBackdrop(Context context) { super(context); }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth(), h = getHeight();
            paint.setStyle(Paint.Style.FILL);
            paint.setShader(new LinearGradient(0, 0, 0, h, Color.WHITE,
                    Color.rgb(204, 250, 246), Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, w, h, paint);
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.SQUARE);
            paint.setStrokeWidth(dp(34));
            paint.setColor(0x685F52EF);
            Path purpleBand = new Path();
            purpleBand.moveTo(-w * .18f, h * .19f);
            purpleBand.lineTo(w * 1.12f, h * .70f);
            canvas.drawPath(purpleBand, paint);
            paint.setColor(0x7651E3D0);
            Path mintBand = new Path();
            mintBand.moveTo(-w * .16f, h * .91f);
            mintBand.lineTo(w * 1.13f, h * .43f);
            canvas.drawPath(mintBand, paint);
            paint.setStyle(Paint.Style.FILL);
        }
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
