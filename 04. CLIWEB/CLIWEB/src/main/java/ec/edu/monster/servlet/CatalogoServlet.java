package ec.edu.monster.servlet;

import ec.edu.monster.controlador.CatalogoControlador;
import ec.edu.monster.modelo.RespuestaCatalogo;
import ec.edu.monster.modelo.RespuestaUnidades;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.ws.ClienteWSConversor;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/catalogo")
public class CatalogoServlet extends HttpServlet {

    private static final String WS_URL =
            "http://localhost:8080/WSCOMSERVIDOR/WSConversorUnidades";

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("token") == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        SesionModelo sesion = new SesionModelo();
        sesion.setToken((String) session.getAttribute("token"));
        sesion.setExpiracion((String) session.getAttribute("expiracion"));

        CatalogoControlador ctrl = new CatalogoControlador(
                new ClienteWSConversor(WS_URL), sesion
        );

        RespuestaCatalogo cat = ctrl.obtenerCategorias();

        Map<String, String> datos = new LinkedHashMap<>();

        if (cat != null
                && "OK".equals(cat.getEstado())
                && cat.getCategorias() != null) {

            for (String c : cat.getCategorias()) {

                RespuestaUnidades u = ctrl.obtenerUnidades(c);

                if (u != null
                        && "OK".equals(u.getEstado())
                        && u.getUnidades() != null) {

                    // Normalizar los nombres de las unidades (quitar tildes)
                    List<String> unidadesNormalizadas = u.getUnidades()
                            .stream()
                            .map(this::normalizarTexto)
                            .toList();

                    datos.put(c, String.join(", ", unidadesNormalizadas));

                } else if (u != null) {

                    datos.put(
                            c,
                            u.getMensaje() != null
                                    ? u.getMensaje()
                                    : "SIN_DATOS"
                    );

                } else {

                    datos.put(c, "SIN_DATOS");
                }
            }
        }

        request.setAttribute("datos", datos);

        request.getRequestDispatcher("catalogo.jsp")
                .forward(request, response);
    }

    private String normalizarTexto(String texto) {

        if (texto == null) {
            return null;
        }

        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }
}