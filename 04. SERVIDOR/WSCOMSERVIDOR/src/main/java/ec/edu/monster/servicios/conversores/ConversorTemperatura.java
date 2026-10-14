package ec.edu.monster.servicios.conversores;

import ec.edu.monster.modelos.Unidad;


public class ConversorTemperatura implements IConversor {

    /** Cero absoluto en grados Celsius: ninguna temperatura puede estar por debajo. */
    static final double CERO_ABSOLUTO_CELSIUS = -273.15;

    /** Margen para el error de coma flotante: -459.67 °F debe aceptarse como cero absoluto. */
    private static final double TOLERANCIA = 1e-9;

    @Override
    public double convertir(double valor, Unidad origen, Unidad destino) {
        EscalaTemperatura escalaOrigen = escalaDe(origen);
        EscalaTemperatura escalaDestino = escalaDe(destino);
        return escalaDestino.desdeCelsius(escalaOrigen.aCelsius(valor));
    }

    @Override
    public boolean admite(double valor, Unidad origen) {
        return EscalaTemperatura.deNombre(origen.getNombre())
                .map(escala -> escala.aCelsius(valor) >= CERO_ABSOLUTO_CELSIUS - TOLERANCIA)
                .orElse(true);
    }

    private static EscalaTemperatura escalaDe(Unidad unidad) {
        return EscalaTemperatura.deNombre(unidad.getNombre())
                .orElseThrow(() -> new IllegalStateException(
                        "No hay fórmula de temperatura para la unidad: " + unidad.getNombre()));
    }
}
