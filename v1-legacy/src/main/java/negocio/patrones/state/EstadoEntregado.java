package negocio.patrones.state;

import negocio.abstractas.EstadoPedido;
import negocio.modelo.Pedido;

/**
 * Estado ENTREGADO: estado final del ciclo.
 * Ya no avanza más.
 */
public class EstadoEntregado implements EstadoPedido {

    @Override
    public void avanzar(Pedido pedido) {
        // Estado final, ya no hay transición.
        // Si quisieras, podrías loguear o lanzar una advertencia.
    }

    @Override
    public String getNombreEstado() {
        return "ENTREGADO";
    }
}
