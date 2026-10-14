package ec.edu.monster.servicios.conversores;

import ec.edu.monster.modelos.Unidad;


public class ConversorTemperatura implements IConversor {

    @Override
    public double convertir(double valor, Unidad origen, Unidad destino) {
        EscalaTemperatura escalaOrigen = escalaDe(origen);
        EscalaTemperatura escalaDestino = escalaDe(destino);
        return escalaDestino.desdeCelsius(escalaOrigen.aCelsius(valor));
    }

    private static EscalaTemperatura escalaDe(Unidad unidad) {
        return EscalaTemperatura.deNombre(unidad.getNombre())
                .orElseThrow(() -> new IllegalStateException(
                        "No hay fórmula de temperatura para la unidad: " + unidad.getNombre()));
    }
}
