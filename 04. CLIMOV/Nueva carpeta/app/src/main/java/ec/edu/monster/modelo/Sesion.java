package ec.edu.monster.modelo;

public final class Sesion {
    private final String token;
    public Sesion(String token) { this.token = token; }
    public String getToken() { return token; }
}
