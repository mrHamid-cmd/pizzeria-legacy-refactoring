package negocio.patrones.state;

import negocio.abstractas.EstadoPedido;
import negocio.modelo.Pedido;

/**
 * Estado inicial del pedido: RECIBIDO.
 * De aquí pasa a PREPARANDO.
 */
public class EstadoRecibido implements EstadoPedido {

    @Override
    public void avanzar(Pedido pedido) {
        // De RECIBIDO pasa a PREPARANDO
        pedido.setEstadoActual(new EstadoPreparando());
        // La notificación se hace en Pedido.avanzarEstado()
    }

    @Override
    public String getNombreEstado() {
        return "RECIBIDO";
    }
}
