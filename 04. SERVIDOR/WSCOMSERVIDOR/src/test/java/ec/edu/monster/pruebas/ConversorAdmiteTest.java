package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.modelos.CodigoRespuesta;
import ec.edu.monster.modelos.SolicitudConversion;
import ec.edu.monster.modelos.Unidad;
import ec.edu.monster.servicios.CatalogoServicio;
import ec.edu.monster.servicios.ConversionServicio;
import ec.edu.monster.servicios.conversores.ConversorLineal;
import ec.edu.monster.servicios.conversores.ConversorTemperatura;
import ec.edu.monster.servicios.conversores.IConversor;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Contrato de IConversor.admite: los valores físicamente imposibles se rechazan con DATA-001 y las
 * implementaciones que no declaran un límite siguen funcionando igual que antes.
 */
class ConversorAdmiteTest {

    private static final Unidad KELVIN = new Unidad("kelvin", "K", 1.0);
    private static final Unidad CELSIUS = new Unidad("celsius", "°C", 1.0);
    private static final Unidad METROS = new Unidad("metros", "m", 1.0);

    @Test
    void unConversorSinLimiteAdmiteCualquierValor() {
        IConversor sinLimite = (valor, origen, destino) -> valor;

        assertTrue(sinLimite.admite(-1e12, METROS));
        assertTrue(sinLimite.admite(0.0, METROS));
    }

    @Test
    void elConversorLinealAdmiteNegativos() {
        assertTrue(new ConversorLineal().admite(-5.0, METROS));
    }

    @Test
    void laTemperaturaAdmiteElCeroAbsolutoYRechazaLoQueEstaBajoEl() {
        ConversorTemperatura conversor = new ConversorTemperatura();

        assertTrue(conversor.admite(0.0, KELVIN));
        assertFalse(conversor.admite(-0.0001, KELVIN));
        assertTrue(conversor.admite(-273.15, CELSIUS));
        assertFalse(conversor.admite(-273.16, CELSIUS));
    }

    @Test
    void unaUnidadDeTemperaturaDesconocidaNoSeRechazaAqui() {
        // El error de una unidad sin fórmula es de configuración (SYS-001), no del dato del usuario.
        assertTrue(new ConversorTemperatura().admite(-1000.0, new Unidad("rankine", "R", 1.0)));
    }

    @Test
    void unConversorPersonalizadoSinLimiteSigueFuncionandoEnElServicio() {
        IConversor identidad = (valor, origen, destino) -> valor;
        ConversionServicio servicio = new ConversionServicio(new CatalogoServicio(), Map.of(
                CatalogoServicio.LONGITUD, identidad,
                CatalogoServicio.MASA, identidad,
                CatalogoServicio.TEMPERATURA, identidad));

        // Con un conversor que no limita nada, la temperatura "bajo cero" ya no se rechaza.
        assertEquals(CodigoRespuesta.OK_200,
                servicio.validar(new SolicitudConversion("TEMPERATURA", "kelvin", "celsius", -50.0)));
    }

    @Test
    void unConversorPersonalizadoQueRechazaDevuelveData001() {
        IConversor rechazaNegativos = new IConversor() {
            @Override
            public double convertir(double valor, Unidad origen, Unidad destino) {
                return valor;
            }

            @Override
            public boolean admite(double valor, Unidad origen) {
                return valor >= 0;
            }
        };
        IConversor lineal = new ConversorLineal();
        ConversionServicio servicio = new ConversionServicio(new CatalogoServicio(), Map.of(
                CatalogoServicio.LONGITUD, rechazaNegativos,
                CatalogoServicio.MASA, lineal,
                CatalogoServicio.TEMPERATURA, new ConversorTemperatura()));

        assertEquals(CodigoRespuesta.DATA_001,
                servicio.validar(new SolicitudConversion("LONGITUD", "metros", "pies", -1.0)));
        assertEquals(CodigoRespuesta.OK_200,
                servicio.validar(new SolicitudConversion("LONGITUD", "metros", "pies", 1.0)));
    }
}
