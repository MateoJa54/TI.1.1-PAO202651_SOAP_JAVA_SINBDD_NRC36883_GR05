package ec.edu.monster.servicios.conversores;

import ec.edu.monster.utilidades.Textos;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.DoubleUnaryOperator;

public enum EscalaTemperatura {

    CELSIUS("celsius", celsius -> celsius, celsius -> celsius),
    FAHRENHEIT("fahrenheit", f -> (f - 32) * 5 / 9, c -> c * 9 / 5 + 32),
    KELVIN("kelvin", k -> k - 273.15, c -> c + 273.15),
    NEWTON("newton", n -> n * 100 / 33, c -> c * 33 / 100),
    REAUMUR("reaumur", r -> r * 5 / 4, c -> c * 4 / 5);

    private final String nombre;
    private final DoubleUnaryOperator haciaCelsius;
    private final DoubleUnaryOperator desdeCelsius;

    EscalaTemperatura(
            String nombre, DoubleUnaryOperator haciaCelsius, DoubleUnaryOperator desdeCelsius) {
        this.nombre = nombre;
        this.haciaCelsius = haciaCelsius;
        this.desdeCelsius = desdeCelsius;
    }

    public double aCelsius(double valor) {
        return haciaCelsius.applyAsDouble(valor);
    }

    public double desdeCelsius(double celsius) {
        return desdeCelsius.applyAsDouble(celsius);
    }

    public static Optional<EscalaTemperatura> deNombre(String nombreUnidad) {
        String buscado = Textos.normalizar(nombreUnidad);
        return Arrays.stream(values())
                .filter(escala -> escala.nombre.equals(buscado))
                .findFirst();
    }
}
