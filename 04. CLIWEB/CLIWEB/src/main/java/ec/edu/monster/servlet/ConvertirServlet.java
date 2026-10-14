package ec.edu.monster.servlet;

import ec.edu.monster.controlador.ConversionControlador;
import ec.edu.monster.modelo.RespuestaConversion;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.modelo.SolicitudConversion;
import ec.edu.monster.util.NumeroUtil;
import ec.edu.monster.ws.ClienteWSConversor;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

@WebServlet("/convertir")
public class ConvertirServlet extends HttpServlet {
    private static final String WS_URL = "http://localhost:8080/WSCOMSERVIDOR/WSConversorUnidades";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("token") == null) {
            response.sendRedirect("index.jsp");
            return;
        }
        SesionModelo sesion = new SesionModelo();
        sesion.setToken((String) session.getAttribute("token"));
        sesion.setExpiracion((String) session.getAttribute("expiracion"));
        String categoria = request.getParameter("categoria");
        String origen = request.getParameter("origen");
        String destino = request.getParameter("destino");
        String valor = request.getParameter("valor");
        
        System.out.println("Valores recibidos:");
        System.out.println("Categoría: " + categoria);
        System.out.println("Origen: " + origen);
        System.out.println("Destino: " + destino);
        System.out.println("Valor: " + valor);
        
        // Guardar los valores seleccionados para restaurarlos
        session.setAttribute("conversion_categoria", categoria);
        session.setAttribute("conversion_origen", origen);
        session.setAttribute("conversion_destino", destino);
        session.setAttribute("conversion_valor", valor);
        session.setAttribute("active_tab", "conversion");
        
        Optional<Double> n = NumeroUtil.interpretarDecimal(valor);
        if (n.isEmpty()) {
            session.setAttribute("conversion_error", "Valor no válido");
            response.sendRedirect("menu.jsp");
            return;
        }
        ConversionControlador ctrl = new ConversionControlador(new ClienteWSConversor(WS_URL), sesion);
        SolicitudConversion s = new SolicitudConversion(categoria, origen, destino, n.get());
        RespuestaConversion r = ctrl.convertir(s);
        if (r != null && "OK".equals(r.getEstado()) && r.getResultado() != null) {
            String res = NumeroUtil.formatear(n.get()) + " " + r.getUnidadOrigen() + " = "
                    + NumeroUtil.formatear(r.getResultado()) + " " + r.getUnidadDestino();
            session.setAttribute("conversion_resultado", res);
        } else {
            String msg = (r == null) ? "Error" : (r.getMensaje() != null ? r.getMensaje() : r.getCodigo());
            session.setAttribute("conversion_error", msg);
        }
        response.sendRedirect("menu.jsp");
    }
}
