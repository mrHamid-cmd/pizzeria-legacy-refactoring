package com.pizzasystem.controladores;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import com.pizzasystem.vistas.EstadoPedidoClienteView;

import negocio.abstractas.ObservadorPedido;
import negocio.modelo.Pedido;
import negocio.servicios.ServicioPedidos;

public class EstadoPedidoClienteController implements ObservadorPedido {

    private EstadoPedidoClienteView view;
    private Stage stage;

    // Número visible: "PED0001"
    private String numeroPedido;

    // ID numérico: 1
    private int idPedido = -1;

    // Pedido real en memoria
    private Pedido pedido;

    // Servicio
    private final ServicioPedidos servicioPedidos = new ServicioPedidos();

    // Para evitar actualizar cuando ya cerraste la ventana
    private volatile boolean activo = true;

    public void setView(EstadoPedidoClienteView view) {
        this.view = view;
        this.stage = view.getStage();

        // ✅ Se toma de lo que ya puso TomaPedidoController en lblNumeroPedido
        this.numeroPedido = extraerNumeroPedido(view.lblNumeroPedido.getText());
        this.idPedido = extraerIdDesdeNumero(numeroPedido);

        configurarAcciones();
        conectarConPedidoReal();
        refrescarVistaDesdePedido(); // pinta el estado actual al abrir
    }

    private void configurarAcciones() {
        // ✅ Usar hooks de la vista (MVC limpio)
        view.setOnVolver(this::volverAInicio);

        view.setOnActualizar(() -> {
            refrescarVistaDesdePedido();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Estado actualizado");
            alert.setHeaderText("Información actualizada");
            alert.setContentText("Se sincronizó el estado de tu pedido con el sistema.");
            alert.showAndWait();
        });

        // ✅ Cierre limpio
        if (stage != null) {
            stage.setOnCloseRequest(e -> cerrarLimpio());
        }
    }

    private void cerrarLimpio() {
        activo = false;

        if (pedido != null) {
            pedido.quitarObservador(this);
        }

        if (view != null) {
            view.limpiar();
        }
    }

    /**
     * Busca el Pedido real en memoria y se registra como Observador.
     */
    private void conectarConPedidoReal() {
        if (idPedido <= 0) {
            mostrarError("Pedido inválido", "No se pudo obtener el ID del pedido: " + numeroPedido);
            return;
        }

        pedido = servicioPedidos.buscarPedidoPorId(idPedido);

        if (pedido == null) {
            mostrarError("Pedido no encontrado",
                    "No se encontró el pedido " + numeroPedido + " en memoria.\n"
                            + "Asegúrate de crearlo primero y luego abrir su seguimiento.");
            return;
        }

        // ✅ OBSERVER REAL
        pedido.agregarObservador(this);
    }

    /**
     * Pinta la vista con el estado del Pedido real.
     */
    private void refrescarVistaDesdePedido() {
        String estado = "RECIBIDO";

        if (pedido != null) {
            String e = pedido.getNombreEstado();
            if (e != null && !e.isBlank()) estado = e;
        }

        // ✅ Un solo punto de pintado (número + estado)
        view.mostrarPedido(numeroPedido, estado);
    }

    /**
     * =============== OBSERVER ===============
     * Se dispara automáticamente cuando alguien hace:
     * pedido.avanzarEstado() -> notificarObservadores()
     */
    @Override
    public void actualizar(Pedido p) {
        if (!activo) return;

        Platform.runLater(() -> {
            if (!activo) return;

            String estado = p.getNombreEstado();
            if (estado == null || estado.isBlank()) estado = "RECIBIDO";

            view.mostrarPedido(numeroPedido, estado);
        });
    }

    // ===================== Helpers =====================

    private String extraerNumeroPedido(String texto) {
        // Esperado: "Pedido #PED0001" o "Pedido #0001"
        if (texto == null) return "PED0000";

        if (texto.contains("#")) {
            return texto.split("#")[1].trim();
        }
        return texto.replace("Pedido", "").trim();
    }

    private int extraerIdDesdeNumero(String numero) {
        if (numero == null) return -1;

        String n = numero.trim().toUpperCase();
        if (n.startsWith("PED")) {
            n = n.substring(3);
        }

        try {
            return Integer.parseInt(n);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void volverAInicio() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cerrar seguimiento");
        confirm.setHeaderText("¿Deseas cerrar la ventana de seguimiento?");
        confirm.setContentText("Podrás volver a abrirla desde el sistema.");

        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                cerrarLimpio();
                if (stage != null) stage.close();
            }
        });
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
