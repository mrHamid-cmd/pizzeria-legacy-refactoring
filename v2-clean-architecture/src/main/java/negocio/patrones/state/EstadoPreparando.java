package negocio.patrones.state;

import negocio.abstractas.EstadoPedido;
import negocio.modelo.Pedido;

/**
 * Estado PREPARANDO: se están armando los ingredientes de la pizza.
 * De aquí pasa a HORNEANDO.
 */
public class EstadoPreparando implements EstadoPedido {

    @Override
    public void avanzar(Pedido pedido) {
        // De PREPARANDO pasa a HORNEANDO
        pedido.setEstadoActual(new EstadoHorneando());
        // La notificación se hace en Pedido.avanzarEstado()
    }

    @Override
    public String getNombreEstado() {
        return "PREPARANDO";
    }
}
