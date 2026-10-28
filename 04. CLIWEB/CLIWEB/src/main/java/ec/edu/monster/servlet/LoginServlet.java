package ec.edu.monster.servlet;

import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.modelo.RespuestaAutenticacion;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.ws.ClienteWSConversor;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final String WS_URL = "http://localhost:8080/WSCOMSERVIDOR/WSConversorUnidades";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String usuario = request.getParameter("usuario");
        String password = request.getParameter("password");
        HttpSession session = request.getSession();
        SesionModelo sesion = new SesionModelo();
        LoginControlador login = new LoginControlador(new ClienteWSConversor(WS_URL), sesion);
        try {
            RespuestaAutenticacion respuesta = login.autenticar(usuario, password);
            if (login.haySesionActiva()) {
                session.setAttribute("token", sesion.getToken());
                session.setAttribute("usuario", usuario);
                session.setAttribute("expiracion", sesion.getExpiracion());
                response.sendRedirect("menu.jsp");
            } else {
                String mensaje = respuesta == null ? null : respuesta.getMensaje();
                request.setAttribute("error", mensaje == null || mensaje.isBlank()
                        ? "Credenciales inválidas"
                        : mensaje);
                request.getRequestDispatcher("index.jsp").forward(request, response);
            }
        } catch (Exception e) {
            String mensaje = e.getMessage();
            request.setAttribute("error", mensaje == null || mensaje.isBlank()
                    ? "No se pudo completar la autenticación"
                    : mensaje);
            request.getRequestDispatcher("index.jsp").forward(request, response);
        }
    }
}
