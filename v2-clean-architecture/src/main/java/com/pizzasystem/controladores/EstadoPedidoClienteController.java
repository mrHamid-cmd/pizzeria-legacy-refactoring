package com.pizzasystem.controladores;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import com.pizzasystem.vistas.EstadoPedidoClienteView;

import negocio.abstractas.ObservadorPedido;
import negocio.modelo.Pedido;
import negocio.servicios.ServicioPedidos;

/**
 * Controlador para la vista del Cliente (Tracking de Pedido).
 * * IMPLEMENTA EL PATRÓN OBSERVER:
 * Esta clase "escucha" los cambios en el objeto Pedido. Cuando el empleado
 * cambia el estado en el Panel de Control, este controlador recibe la notificación
 * y actualiza la barra de progreso automáticamente.
 */
public class EstadoPedidoClienteController implements ObservadorPedido {

    private EstadoPedidoClienteView view;
    private Stage stage;

    // Identificadores del pedido
    private String numeroPedido; // Formato visual: "PED0001"
    private int idPedido = -1;   // ID lógico: 1

    // Referencia al modelo (Sujeto Observable)
    private Pedido pedido;

    // Fachada de servicios
    private final ServicioPedidos servicioPedidos = new ServicioPedidos();

    // Bandera para controlar la concurrencia y evitar actualizaciones en ventanas cerradas
    private volatile boolean activo = true;

    /**
     * Inicializa el controlador, vincula la vista y conecta con el modelo.
     * @param view La vista gráfica asociada.
     */
    public void setView(EstadoPedidoClienteView view) {
        this.view = view;
        this.stage = view.getStage();

        // 1. Extraer ID del label (configurado previamente por la ventana anterior)
        this.numeroPedido = extraerNumeroPedido(view.lblNumeroPedido.getText());
        this.idPedido = extraerIdDesdeNumero(numeroPedido);

        // 2. Configurar botones y eventos
        configurarAcciones();

        // 3. Buscar el objeto real en memoria y suscribirse a cambios
        conectarConPedidoReal();

        // 4. Pintar el estado inicial
        refrescarVistaDesdePedido(); 
    }

    private void configurarAcciones() {
        // Botón "Volver"
        view.setOnVolver(this::volverAInicio);

        // Botón manual "Actualizar" (por si el usuario quiere forzar la vista)
        view.setOnActualizar(() -> {
            refrescarVistaDesdePedido();
            mostrarAlertaInfo("Sincronizado", "El estado del pedido está actualizado.");
        });

        // Evento de cierre de ventana (X)
        if (stage != null) {
            stage.setOnCloseRequest(e -> cerrarLimpio());
        }
    }

    /**
     * Limpia las suscripciones para evitar fugas de memoria (Memory Leaks).
     * Si no hacemos esto, el objeto Pedido seguiría intentando notificar a una ventana cerrada.
     */
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
     * Busca el Pedido en la capa de negocio y se registra como Observador.
     */
    private void conectarConPedidoReal() {
        if (idPedido <= 0) {
            mostrarError("Error interno", "ID de pedido inválido: " + numeroPedido);
            return;
        }

        // Usamos el servicio para buscar en el Gestor (Memoria)
        pedido = servicioPedidos.buscarPedidoPorId(idPedido);

        if (pedido == null) {
            mostrarError("Pedido no encontrado",
                    "El sistema no encuentra el pedido " + numeroPedido + " en los registros activos.");
            return;
        }

        // ✅ SUSCRIPCIÓN AL PATRÓN OBSERVER
        pedido.agregarObservador(this);
    }

    /**
     * Lee el estado actual del modelo y actualiza la UI.
     */
    private void refrescarVistaDesdePedido() {
        String estado = "RECIBIDO"; // Valor por defecto

        if (pedido != null) {
            String e = pedido.getNombreEstado();
            if (e != null && !e.isBlank()) estado = e;
        }

        // Actualiza el indicador visual (colores y barra de progreso)
        view.mostrarPedido(numeroPedido, estado);
    }

    /**
     * MÉTOD DEL PATRÓN OBSERVER.
     * Se ejecuta automáticamente cuando el objeto Pedido cambia de estado.
     * * @param p El objeto Pedido que ha cambiado.
     */
    @Override
    public void actualizar(Pedido p) {
        if (!activo) return;

        // Platform.runLater es CRUCIAL aquí.
        // Los cambios de estado pueden venir de hilos de fondo o eventos externos.
        // JavaFX exige que cualquier cambio en la UI se haga en el hilo principal (JavaFX Thread).
        Platform.runLater(() -> {
            if (!activo) return;

            String estado = p.getNombreEstado();
            if (estado == null || estado.isBlank()) estado = "RECIBIDO";

            view.mostrarPedido(numeroPedido, estado);
        });
    }

    // =========================================================
    // Métodos Auxiliares y de Parseo
    // =========================================================

    private String extraerNumeroPedido(String texto) {
        // Convierte "Pedido #PED0001" -> "PED0001"
        if (texto == null) return "PED0000";
        if (texto.contains("#")) {
            return texto.split("#")[1].trim();
        }
        return texto.replace("Pedido", "").trim();
    }

    private int extraerIdDesdeNumero(String numero) {
        // Convierte "PED0001" -> 1
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
        confirm.setContentText("Podrás volver a consultarla con tu número de pedido.");

        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                cerrarLimpio();
                if (stage != null) stage.close();
            }
        });
    }

    private void mostrarAlertaInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText("Ocurrió un problema");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}