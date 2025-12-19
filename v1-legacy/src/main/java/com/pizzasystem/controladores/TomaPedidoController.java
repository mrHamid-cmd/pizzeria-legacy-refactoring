package com.pizzasystem.controladores;

import javafx.scene.control.*;
import javafx.stage.Stage;

import com.pizzasystem.vistas.EstadoPedidoClienteView;
import com.pizzasystem.vistas.PanelControlPedidosView;
import com.pizzasystem.vistas.TomaPedidoView;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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

    // Bitácora visible (NO lógica de negocio)
    private static final String PEDIDOS_FILE = "pedidos.txt";

    // Servicio real
    private final ServicioPedidos servicioPedidos = new ServicioPedidos();

    public void setView(TomaPedidoView view) {
        this.view = view;
        this.stage = view.getStage();
        configurarAcciones();
    }

    private void configurarAcciones() {
        view.btnConfirmar.setOnAction(e -> confirmarPedido());
        view.btnGestionar.setOnAction(e -> abrirPanelControl());

        // ✅ Al cambiar cualquier selección de pizza, recalcular preview
        view.setOnCambioSeleccion(this::recalcularTotalPreview);

        // ✅ IMPORTANTÍSIMO: también recalcular cuando cambie el combo de estrategia
        // (esto lo conectas desde la vista cuando agregues el ComboBox)
        view.setOnCambioEstrategia(this::recalcularTotalPreview);

        // ✅ Total inicial (preview) al abrir
        recalcularTotalPreview();
    }

    // =========================
    // Strategy helper
    // =========================
    private boolean estrategiaPromoActiva() {
        // Esto depende de lo que guardes en el combo.
        // Ejemplo: "Estándar" y "Promoción"
        String sel = view.getEstrategiaSeleccionada();
        if (sel == null) return false;
        return sel.toUpperCase().contains("PROMO");
    }

    /**
     * Recalcula el total SIN crear pedido.
     * - Construye una Pizza "temporal" con Builder
     * - Calcula con Strategy según el ComboBox (preview)
     */
    private void recalcularTotalPreview() {
        if (view == null) return;

        // Si todavía no hay selección base completa, mostramos 0
        if (view.getMasaSeleccionada() == null
                || view.getSalsaSeleccionada() == null
                || view.getQuesoSeleccionado() == null
                || view.getOrillaSeleccionada() == null) {

            view.actualizarTotal(0.00);
            return;
        }

        List<String> ingredientes = new ArrayList<>(view.getIngredientesSeleccionados());
        List<String> condimentos = new ArrayList<>(view.getCondimentosSeleccionados());

        PizzaBuilder builder = new PizzaPersonalizadaBuilder()
                .conMasa(view.getMasaSeleccionada())
                .conSalsa(view.getSalsaSeleccionada())
                .conQueso(view.getQuesoSeleccionado())
                .conTipoOrilla(view.getOrillaSeleccionada());

        for (String ing : ingredientes) {
            if (ing != null && !ing.isBlank()) builder.agregarIngrediente(ing.trim());
        }

        for (String con : condimentos) {
            if (con != null && !con.isBlank()) builder.agregarCondimento(con.trim());
        }

        Pizza pizzaTemp = builder.build();

        // ✅ Strategy según combo (solo preview)
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

        if (view.getMasaSeleccionada() == null
                || view.getSalsaSeleccionada() == null
                || view.getQuesoSeleccionado() == null
                || view.getOrillaSeleccionada() == null) {

            mostrarAlerta(
                    "Error",
                    "Selección incompleta",
                    "Seleccione todos los elementos base de la pizza."
            );
            return;
        }

        List<String> ingredientes = new ArrayList<>(view.getIngredientesSeleccionados());
        List<String> condimentos = new ArrayList<>(view.getCondimentosSeleccionados());

        // ✅ IMPORTANTE: setear estrategia SOLO para el pedido que estás creando ahora
        // (no recalcula los anteriores, solo afecta los próximos pedidos creados)
        servicioPedidos.cambiarEstrategiaPromocion(estrategiaPromoActiva());

        // ✅ CREACIÓN REAL DEL PEDIDO
        Pedido pedido = servicioPedidos.crearPedidoDesdeDatosSimples(
                view.getMasaSeleccionada(),
                view.getSalsaSeleccionada(),
                view.getQuesoSeleccionado(),
                view.getOrillaSeleccionada(),
                ingredientes,
                condimentos
        );

        String numeroPedido = String.format("PED%04d", pedido.getId());

        // Bitácora visual
        guardarPedidoEnBitacora(numeroPedido, pedido);

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
                abrirEstadoPedido(numeroPedido);
                view.limpiarSeleccion();
                recalcularTotalPreview(); // refrescar preview a 0 o lo que corresponda
            }
        });
    }

    // ============================
    // BITÁCORA TXT (SOLO VISUAL)
    // ============================
    private void guardarPedidoEnBitacora(String numeroPedido, Pedido pedido) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(PEDIDOS_FILE, true))) {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String fecha = LocalDateTime.now().format(formatter);

            writer.println("=== PEDIDO " + numeroPedido + " ===");
            writer.println("Fecha: " + fecha);
            writer.println("Masa: " + pedido.getPizza().getMasa());
            writer.println("Salsa: " + pedido.getPizza().getSalsa());
            writer.println("Queso: " + pedido.getPizza().getQueso());
            writer.println("Orilla: " + pedido.getPizza().getTipoOrilla());

            writer.println("Ingredientes: " + String.join(", ", pedido.getPizza().getIngredientes()));
            writer.println("Condimentos: " + String.join(", ", pedido.getPizza().getCondimentos()));

            writer.println(String.format("Total: $%.2f", pedido.getTotal()));
            writer.println("Estado: " + pedido.getNombreEstado());
            writer.println("=============================\n");

        } catch (IOException e) {
            System.err.println("Error al guardar bitácora: " + e.getMessage());
        }
    }

    // ============================
    // VISTA CLIENTE
    // ============================
    private void abrirEstadoPedido(String numeroPedido) {
        Stage estadoStage = new Stage();
        EstadoPedidoClienteView estadoView = new EstadoPedidoClienteView(estadoStage);

        estadoView.lblNumeroPedido.setText("Pedido #" + numeroPedido);

        EstadoPedidoClienteController controller = new EstadoPedidoClienteController();
        controller.setView(estadoView);

        estadoView.mostrar();
    }

    // ============================
    // PANEL DE CONTROL
    // ============================
    private void abrirPanelControl() {
        if (stage != null) stage.close();

        Stage panelStage = new Stage();
        PanelControlPedidosView panelView = new PanelControlPedidosView(panelStage);

        PanelControlPedidosController controller = new PanelControlPedidosController();
        controller.setView(panelView);

        panelView.mostrar();
    }

    private void mostrarAlerta(String titulo, String encabezado, String contenido) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(encabezado);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}
