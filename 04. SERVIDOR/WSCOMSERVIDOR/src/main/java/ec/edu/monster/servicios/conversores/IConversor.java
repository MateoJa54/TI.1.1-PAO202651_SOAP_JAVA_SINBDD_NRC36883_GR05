package ec.edu.monster.servicios.conversores;

import ec.edu.monster.modelos.Unidad;

public interface IConversor {

    double convertir(double valor, Unidad origen, Unidad destino);
}
