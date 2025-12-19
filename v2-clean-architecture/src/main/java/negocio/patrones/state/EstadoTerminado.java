package negocio.patrones.state;

import negocio.abstractas.EstadoPedido;
import negocio.modelo.Pedido;

/**
 * Estado TERMINADO: la pizza ya está lista para entrega.
 * De aquí pasa a ENTREGADO.
 */
public class EstadoTerminado implements EstadoPedido {

    @Override
    public void avanzar(Pedido pedido) {
        // De TERMINADO pasa a ENTREGADO
        pedido.setEstadoActual(new EstadoEntregado());
        // La notificación se hace en Pedido.avanzarEstado()
    }

    @Override
    public String getNombreEstado() {
        return "TERMINADO";
    }
}
