package ec.edu.monster.servicios.conversores;

import ec.edu.monster.modelos.Unidad;

public class ConversorLineal implements IConversor {

    @Override
    public double convertir(double valor, Unidad origen, Unidad destino) {
        return valor * origen.getFactorBase() / destino.getFactorBase();
    }
}
