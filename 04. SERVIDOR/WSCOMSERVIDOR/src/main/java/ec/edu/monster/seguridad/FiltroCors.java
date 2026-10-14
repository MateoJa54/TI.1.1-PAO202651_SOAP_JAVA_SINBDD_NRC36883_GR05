package ec.edu.monster.seguridad;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class FiltroCors implements Filter {

    static final String PARAMETRO_ORIGENES = "cors.origenes.permitidos";

    private static final String ORIGENES_POR_DEFECTO = "http://localhost:*,http://127.0.0.1:*";
    private static final String COMODIN = "*";
    private static final String COMODIN_DE_PUERTO = ":*";
    private static final String METODOS_PERMITIDOS = "GET, POST, OPTIONS";
    private static final String CABECERAS_PERMITIDAS = "Content-Type, SOAPAction, Accept";
    private static final String SEGUNDOS_DE_CACHE = "3600";

    private List<String> origenesPermitidos;

    public FiltroCors() {
        this(ORIGENES_POR_DEFECTO);
    }

    public FiltroCors(String origenes) {
        this.origenesPermitidos = separar(origenes);
    }

    @Override
    public void init(FilterConfig configuracion) {
        String configurados = configuracion.getInitParameter(PARAMETRO_ORIGENES);
        if (configurados != null && !configurados.isBlank()) {
            origenesPermitidos = separar(configurados);
        }
    }

    @Override
    public void doFilter(ServletRequest peticion, ServletResponse respuesta, FilterChain cadena)
            throws IOException, ServletException {
        HttpServletRequest http = (HttpServletRequest) peticion;
        HttpServletResponse httpRespuesta = (HttpServletResponse) respuesta;
        String origen = http.getHeader("Origin");

        if (esOrigenPermitido(origen)) {
            httpRespuesta.setHeader("Access-Control-Allow-Origin", origen);
            // La respuesta cambia según el origen: las cachés no deben mezclarlas.
            httpRespuesta.setHeader("Vary", "Origin");
            httpRespuesta.setHeader("Access-Control-Allow-Methods", METODOS_PERMITIDOS);
            httpRespuesta.setHeader("Access-Control-Allow-Headers", CABECERAS_PERMITIDAS);
            httpRespuesta.setHeader("Access-Control-Max-Age", SEGUNDOS_DE_CACHE);
        }

        if (esPreflight(http)) {
            // La consulta previa del navegador no es una petición SOAP: no llega al servicio.
            httpRespuesta.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }
        cadena.doFilter(peticion, respuesta);
    }

    public boolean esOrigenPermitido(String origen) {
        if (origen == null || origen.isBlank()) {
            return false;
        }
        return origenesPermitidos.stream().anyMatch(permitido -> coincide(permitido, origen));
    }

    private static boolean coincide(String permitido, String origen) {
        if (permitido.equals(COMODIN) || permitido.equals(origen)) {
            return true;
        }
        if (!permitido.endsWith(COMODIN_DE_PUERTO)) {
            return false;
        }
        String host = permitido.substring(0, permitido.length() - COMODIN_DE_PUERTO.length());
        if (origen.equals(host)) {
            return true;
        }
        // Tras "host:" solo puede venir el puerto; así "http://localhost:80.malo.com" no pasa.
        return origen.startsWith(host + ":")
                && origen.substring(host.length() + 1).chars().allMatch(Character::isDigit)
                && origen.length() > host.length() + 1;
    }

    private static boolean esPreflight(HttpServletRequest peticion) {
        return "OPTIONS".equalsIgnoreCase(peticion.getMethod())
                && peticion.getHeader("Origin") != null
                && peticion.getHeader("Access-Control-Request-Method") != null;
    }

    private static List<String> separar(String origenes) {
        return Arrays.stream(origenes.split(","))
                .map(String::trim)
                .filter(origen -> !origen.isEmpty())
                .toList();
    }
}
