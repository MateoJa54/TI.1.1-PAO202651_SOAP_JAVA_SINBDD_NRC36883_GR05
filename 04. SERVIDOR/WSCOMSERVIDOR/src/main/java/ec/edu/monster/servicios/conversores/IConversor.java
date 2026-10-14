package ec.edu.monster.servicios.conversores;

import ec.edu.monster.modelos.Unidad;

public interface IConversor {

    double convertir(double valor, Unidad origen, Unidad destino);

    /**
     * Indica si el valor es físicamente posible en la unidad de origen.
     *
     * <p>Por defecto todo valor se admite; una categoría con un límite físico (por ejemplo, el
     * cero absoluto de la temperatura) lo redefine. Un valor no admitido se responde con DATA-001.
     *
     * @param valor valor escrito por el usuario, en la unidad de origen
     * @param origen unidad del valor
     * @return {@code false} si el valor no existe en la naturaleza
     */
    default boolean admite(double valor, Unidad origen) {
        return true;
    }
}
