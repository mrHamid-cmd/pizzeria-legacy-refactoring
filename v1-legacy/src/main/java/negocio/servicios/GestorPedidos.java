package negocio.servicios;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import negocio.patrones.state.EstadoCancelado;
import negocio.abstractas.EstrategiaPrecio;
import negocio.abstractas.EstadoPedido;
import negocio.modelo.Pedido;
import negocio.modelo.Pizza;
import negocio.patrones.strategy.PrecioEstandar;

/**
 * Patrón Singleton
 * RESPONSABILIDAD: Mantener pedidos en MEMORIA y operar con OBJETOS (tiempo real).
 * La persistencia (TXT) se hace en ServicioPedidos.
 */
public class GestorPedidos {

    private static GestorPedidos instancia;

    private final List<Pedido> pedidos = new ArrayList<>();
    private int siguienteId = 1;

    private EstrategiaPrecio estrategia = new PrecioEstandar();

    private GestorPedidos() {}

    public static GestorPedidos getInstancia() {
        if (instancia == null) {
            instancia = new GestorPedidos();
        }
        return instancia;
    }

    public void setEstrategia(EstrategiaPrecio estrategia) {
        if (estrategia != null) {
            this.estrategia = estrategia;
        }
    }

    /**
     * Crea y registra un pedido NUEVO desde la GUI (objeto real).
     * Calcula total con Strategy y asigna ID incremental.
     */
    public Pedido registrarPedido(Pizza pizza) {
        double total = estrategia.calcularTotal(pizza);
        Pedido pedido = new Pedido(pizza, total);
        pedido.setId(siguienteId++);
        pedidos.add(pedido);
        return pedido;
    }

    /**
     * Registra un pedido ya existente (por ejemplo cargado desde TXT).
     * No recalcula total; solo lo mete a memoria.
     */
    public void registrarPedidoExistente(Pedido pedido) {
        if (pedido == null) return;

        pedidos.add(pedido);

        if (pedido.getId() >= siguienteId) {
            siguienteId = pedido.getId() + 1;
        }
    }

    public Pedido buscarPorId(int id) {
        for (Pedido p : pedidos) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    /**
     * Avanza el estado si el pedido existe y NO está finalizado (ENTREGADO/CANCELADO).
     */
    public void avanzarEstado(int idPedido) {
        Pedido p = buscarPorId(idPedido);
        if (p == null) return;

        if (p.esFinalizado()) return;

        p.avanzarEstado(); // aquí ya notifica observadores
    }

    /**
     * Cancela el pedido (solo si no está ya finalizado).
     * Esto dispara notificación porque setEstadoActual notifica.
     */
    public void cancelarPedido(int idPedido) {
    Pedido p = buscarPorId(idPedido);
    if (p == null) return;

    if (p.esFinalizado()) return;

    p.setEstadoActual(new EstadoCancelado());
}


    public List<Pedido> getPedidos() {
        return Collections.unmodifiableList(pedidos);
    }

    // ===== Estado interno CANCELADO (sin archivo aquí) =====
    private static class EstadoCancelado implements EstadoPedido {
        @Override
        public void avanzar(Pedido pedido) {
            // Cancelado no avanza
        }

        @Override
        public String getNombreEstado() {
            return "CANCELADO";
        }
    }
}
