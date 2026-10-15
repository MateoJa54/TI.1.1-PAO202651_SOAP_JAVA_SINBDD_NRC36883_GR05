package ec.edu.monster.controlador;

import ec.edu.monster.modelo.Conversion;
import ec.edu.monster.ws.ClienteConversorSoap;
import java.util.List;

/** Coordina las acciones de la vista con el servicio remoto SOAP. */
public final class ConversorControlador {
    private final ClienteConversorSoap servicio;
    private String token;
    public ConversorControlador(String endpoint) { servicio = new ClienteConversorSoap(endpoint); }
    public void iniciarSesion(String usuario, String password) throws Exception {
        token = servicio.autenticar(usuario, password);
    }
    public List<String> obtenerCategorias() throws Exception { return servicio.categorias(token); }
    public List<String> obtenerUnidades(String categoria) throws Exception { return servicio.unidades(token, categoria); }
    public String convertir(String categoria, String origen, String destino, double valor) throws Exception {
        if (token == null) throw new IllegalStateException("Inicia sesión antes de convertir.");
        return servicio.convertir(token, new Conversion(categoria, origen, destino, valor));
    }
    public void cerrarSesion() { token = null; }
}
