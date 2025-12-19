package negocio.patrones.state;

import negocio.abstractas.EstadoPedido;
import negocio.modelo.Pedido;

/**
 * Estado HORNEANDO: la pizza está en el horno.
 * De aquí pasa a TERMINADO.
 */
public class EstadoHorneando implements EstadoPedido {

    @Override
    public void avanzar(Pedido pedido) {
        // De HORNEANDO pasa a TERMINADO
        pedido.setEstadoActual(new EstadoTerminado());
        // La notificación se hace en Pedido.avanzarEstado()
    }

    @Override
    public String getNombreEstado() {
        return "HORNEANDO";
    }
}
