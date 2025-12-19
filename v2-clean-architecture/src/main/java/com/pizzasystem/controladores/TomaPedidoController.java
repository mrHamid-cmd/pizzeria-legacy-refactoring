package com.pizzasystem.controladores;

import javafx.scene.control.*;
import javafx.stage.Stage;

import com.pizzasystem.vistas.EstadoPedidoClienteView;
import com.pizzasystem.vistas.PanelControlPedidosView;
import com.pizzasystem.vistas.TomaPedidoView;

import java.util.ArrayList;
import java.util.List;

// Modelo y Patrones
import negocio.abstractas.PizzaBuilder;
import negocio.modelo.Pedido;
import negocio.modelo.Pizza;
import negocio.patrones.builder.PizzaPersonalizadaBuilder;
import negocio.patrones.strategy.PrecioEstandar;
import negocio.patrones.strategy.PrecioPromocion;
import negocio.servicios.ServicioPedidos;

public class TomaPedidoController {

    private TomaPedidoView view;
    private Stage stage;

    // Conexión única con los datos
    private final ServicioPedidos servicioPedidos = new ServicioPedidos();

    public void setView(TomaPedidoView view) {
        this.view = view;
        this.stage = view.getStage();
        configurarAcciones();
    }

    private void configurarAcciones() {
        view.btnConfirmar.setOnAction(e -> confirmarPedido());
        view.btnGestionar.setOnAction(e -> abrirPanelControl());

        // Al cambiar cualquier selección de pizza, recalcular preview
        view.setOnCambioSeleccion(this::recalcularTotalPreview);

        // Recalcular cuando cambie el combo de estrategia
        view.setOnCambioEstrategia(this::recalcularTotalPreview);

        // Total inicial (preview) al abrir
        recalcularTotalPreview();
    }

    // =========================
    // Strategy helper (UI)
    // =========================
    private boolean estrategiaPromoActiva() {
        String sel = view.getEstrategiaSeleccionada();
        if (sel == null) return false;
        return sel.toUpperCase().contains("PROMO");
    }

    /**
     * Recalcula el total SIN crear pedido (Solo visual/preview).
     */
    private void recalcularTotalPreview() {
        if (view == null) return;

        // Validación básica visual
        if (view.getMasaSeleccionada() == null
                || view.getSalsaSeleccionada() == null
                || view.getQuesoSeleccionado() == null
                || view.getOrillaSeleccionada() == null) {

            view.actualizarTotal(0.00);
            return;
        }

        PizzaBuilder builder = new PizzaPersonalizadaBuilder()
                .conMasa(view.getMasaSeleccionada())
                .conSalsa(view.getSalsaSeleccionada())
                .conQueso(view.getQuesoSeleccionado())
                .conTipoOrilla(view.getOrillaSeleccionada());

        // Agregamos ingredientes temporales
        for (String ing : view.getIngredientesSeleccionados()) {
            if (ing != null && !ing.isBlank()) builder.agregarIngrediente(ing.trim());
        }
        for (String con : view.getCondimentosSeleccionados()) {
            if (con != null && !con.isBlank()) builder.agregarCondimento(con.trim());
        }

        Pizza pizzaTemp = builder.build();

        // Calculamos precio temporal
        double total = estrategiaPromoActiva()
                ? new PrecioPromocion().calcularTotal(pizzaTemp)
                : new PrecioEstandar().calcularTotal(pizzaTemp);

        view.actualizarTotal(total);
    }

    /**
     * ============================
     * CONFIRMAR PEDIDO (OFICIAL)
     * ============================
     */
    private void confirmarPedido() {

        // 1. Validación UI
        if (view.getMasaSeleccionada() == null
                || view.getSalsaSeleccionada() == null
                || view.getQuesoSeleccionado() == null
                || view.getOrillaSeleccionada() == null) {

            mostrarAlerta("Error", "Selección incompleta", "Seleccione todos los elementos base.");
            return;
        }

        List<String> ingredientes = new ArrayList<>(view.getIngredientesSeleccionados());
        List<String> condimentos = new ArrayList<>(view.getCondimentosSeleccionados());

        // 2. Configurar Estrategia en el negocio
        servicioPedidos.cambiarEstrategiaPromocion(estrategiaPromoActiva());

        // 3. Crear Pedido (El Servicio guarda en memoria y en la "Base de Datos" principal)
        Pedido pedido = servicioPedidos.crearPedidoDesdeDatosSimples(
                view.getMasaSeleccionada(),
                view.getSalsaSeleccionada(),
                view.getQuesoSeleccionado(),
                view.getOrillaSeleccionada(),
                ingredientes,
                condimentos
        );

        String numeroPedido = String.format("PED%04d", pedido.getId());

        // 4. Generar Ticket Físico (Delegado al servicio -> RepositorioTickets)
        try {
            // Preparamos la lista de specs para imprimir en el ticket
            List<String> specsTicket = new ArrayList<>();
            specsTicket.add("Masa: " + pedido.getPizza().getMasa());
            specsTicket.add("Salsa: " + pedido.getPizza().getSalsa());
            specsTicket.add("Queso: " + pedido.getPizza().getQueso());
            specsTicket.add("Orilla: " + pedido.getPizza().getTipoOrilla());
            
            String strIng = String.join(", ", pedido.getPizza().getIngredientes());
            if(!strIng.isEmpty()) specsTicket.add("Ingredientes: " + strIng);
            
            String strCon = String.join(", ", pedido.getPizza().getCondimentos());
            if(!strCon.isEmpty()) specsTicket.add("Condimentos: " + strCon);

            // Llamada al servicio para IO
            servicioPedidos.generarTicketFisico(
                    numeroPedido, 
                    pedido.getNombreEstado(), 
                    specsTicket, 
                    String.format("$%.2f", pedido.getTotal())
            );

        } catch (Exception ex) {
            System.err.println("Advertencia: No se pudo generar el ticket físico. " + ex.getMessage());
        }

        // 5. Feedback al usuario
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Pedido Confirmado");
        alert.setHeaderText("¡Pedido creado correctamente!");
        alert.setContentText(
                "Número de pedido: " + numeroPedido + "\n"
                        + String.format("Total: $%.2f", pedido.getTotal())
                        + "\n\nEl pedido ha sido enviado a cocina."
        );

        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                abrirEstadoPedidoWindow(numeroPedido);
                
                // Reset UI
                view.limpiarSeleccion();
                recalcularTotalPreview();
            }
        });
    }

    // ============================
    // VISTA CLIENTE (Navegación)
    // ============================
    private void abrirEstadoPedidoWindow(String numeroPedido) {
        try {
            Stage estadoStage = new Stage();
            EstadoPedidoClienteView estadoView = new EstadoPedidoClienteView(estadoStage);

            estadoView.lblNumeroPedido.setText("Pedido #" + numeroPedido);

            EstadoPedidoClienteController controller = new EstadoPedidoClienteController();
            controller.setView(estadoView);

            estadoView.mostrar();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la ventana de estado", e.getMessage());
        }
    }

    // ============================
    // PANEL DE CONTROL (Navegación)
    // ============================
    private void abrirPanelControl() {
        try {
            if (stage != null) stage.close();

            Stage panelStage = new Stage();
            PanelControlPedidosView panelView = new PanelControlPedidosView(panelStage);

            PanelControlPedidosController controller = new PanelControlPedidosController();
            controller.setView(panelView);

            panelView.mostrar();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir el panel de control", e.getMessage());
        }
    }

    private void mostrarAlerta(String titulo, String encabezado, String contenido) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(encabezado);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}