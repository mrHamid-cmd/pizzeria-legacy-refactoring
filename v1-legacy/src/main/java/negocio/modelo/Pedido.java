package negocio.modelo;

import negocio.abstractas.ObservadorPedido;
import negocio.abstractas.EstadoPedido;
import negocio.patrones.state.EstadoRecibido;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Representa un pedido de pizza en el sistema.
 * Implementa:
 * - State: estadoActual
 * - Observer: lista de observadores
 */
public class Pedido {

    private int id;
    private Date fechaHora;
    private Pizza pizza;
    private double total;
    private EstadoPedido estadoActual;
    private final List<ObservadorPedido> observadores = new ArrayList<>();

    public Pedido(Pizza pizza, double total) {
        this.pizza = pizza;
        this.total = total;
        this.fechaHora = new Date();              // fecha/hora al crear el pedido
        this.estadoActual = new EstadoRecibido(); // estado inicial
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getFechaHora() {
        return fechaHora;
    }

    public Pizza getPizza() {
        return pizza;
    }

    public double getTotal() {
        return total;
    }

    /** Útil si en algún momento recalculas/ajustas total (Strategy) */
    public void setTotal(double total) {
        this.total = total;
    }

    public EstadoPedido getEstadoActual() {
        return estadoActual;
    }

    public String getNombreEstado() {
        return (estadoActual != null) ? estadoActual.getNombreEstado() : "";
    }

    /**
     * Cambia el estado actual.
     * IMPORTANTE: aquí también notificamos para que las ventanas se refresquen
     * aunque el cambio venga "desde fuera" (ej. cancelar desde tablero).
     */
    public void setEstadoActual(EstadoPedido nuevo) {
        this.estadoActual = nuevo;
        notificarObservadores();
    }

    /**
     * Avanza el estado del pedido delegando en el objeto EstadoPedido actual.
     * Después de avanzar, notifica a todos los observadores.
     */
    public void avanzarEstado() {
        if (estadoActual == null) return;

        // No avanza si ya está en un estado final
        if (esCancelado() || esEntregado()) {
            return;
        }

        estadoActual.avanzar(this);
        notificarObservadores();
    }

    public boolean esCancelado() {
        return "CANCELADO".equalsIgnoreCase(getNombreEstado());
    }

    public boolean esEntregado() {
        return "ENTREGADO".equalsIgnoreCase(getNombreEstado());
    }

    public boolean esFinalizado() {
        return esCancelado() || esEntregado();
    }

    public void agregarObservador(ObservadorPedido o) {
    if (o != null && !observadores.contains(o)) {
        observadores.add(o);
    }
}

public void quitarObservador(ObservadorPedido o) {
    if (o != null) {
        observadores.remove(o);
    }
}

public void notificarObservadores() {
    // Copia defensiva para evitar problemas si un observador se quita/añade durante la notificación
    List<ObservadorPedido> copia = new ArrayList<>(observadores);
    for (ObservadorPedido o : copia) {
        try {
            o.actualizar(this);
        } catch (Exception ex) {
            // No dejamos que un observador "mate" el flujo del sistema
            System.err.println("Error notificando observador: " + ex.getMessage());
        }
    }
}

}
