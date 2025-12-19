package com.pizzasystem.controladores;

import com.pizzasystem.vistas.DetallePedidoView;
import com.pizzasystem.vistas.PanelControlPedidosView;
import com.pizzasystem.vistas.TomaPedidoView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import negocio.modelo.Pedido;
import negocio.servicios.ServicioPedidos;

public class PanelControlPedidosController {

    // Archivo "visible" (solo para detalle/especificaciones UI)
    private static final String PEDIDOS_FILE = "pedidos.txt";

    private PanelControlPedidosView view;
    private Stage stage;

    private final ServicioPedidos servicioPedidos = new ServicioPedidos();

    // Auto refresco
    private Timeline autoRefresh;

    // Strategy actual (solo UI)
    private boolean promoActiva = false;

    public void setView(PanelControlPedidosView view) {
        this.view = view;
        this.stage = view.getStage();

        configurarAcciones();
        configurarListenersListas();

        // ✅ Carga inicial desde OBJETOS
        cargarPedidosDesdeServicio();

        // ✅ Auto refresh
        iniciarAutoRefresh();

        if (this.stage != null) {
            this.stage.setOnCloseRequest(e -> detenerAutoRefresh());
        }
    }

    // =========================================================
    // Botones
    // =========================================================
    private void configurarAcciones() {

        view.btnNuevoPedido.setOnAction(e -> abrirNuevoPedido());
        view.btnLogout.setOnAction(e -> cerrarSesion());
    }

   
    // =========================================================
    // Doble click en listas -> abrir detalle
    // =========================================================
    private void configurarListenersListas() {
        configurarDobleClick(view.listaRecibido);
        configurarDobleClick(view.listaPreparando);
        configurarDobleClick(view.listaHorneando);
        configurarDobleClick(view.listaTerminado);
        configurarDobleClick(view.listaEntregado);
    }

    private void configurarDobleClick(ListView<String> lista) {
        if (lista == null) return;

        lista.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String item = lista.getSelectionModel().getSelectedItem();
                if (item != null) abrirDetallePedidoDesdeItem(item);
            }
        });
    }

    // =========================================================
    // Navegación
    // =========================================================
    private void abrirNuevoPedido() {
        detenerAutoRefresh();
        if (stage != null) stage.close();

        Stage tomaPedidoStage = new Stage();
        TomaPedidoView tomaPedidoView = new TomaPedidoView(tomaPedidoStage);

        TomaPedidoController controller = new TomaPedidoController();
        controller.setView(tomaPedidoView);

        tomaPedidoView.mostrar();
    }

    private void cerrarSesion() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Cerrar Sesión");
        confirmacion.setHeaderText("¿Está seguro de cerrar sesión?");
        confirmacion.setContentText("Se cerrará el Panel de Control.");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                detenerAutoRefresh();
                if (stage != null) stage.close();
                System.out.println("Sesión cerrada");
            }
        });
    }

    // =========================================================
    // Tablero desde OBJETOS
    // =========================================================
    private void cargarPedidosDesdeServicio() {

        // limpiar
        view.listaRecibido.getItems().clear();
        view.listaPreparando.getItems().clear();
        view.listaHorneando.getItems().clear();
        view.listaTerminado.getItems().clear();
        view.listaEntregado.getItems().clear();

        int activos = 0;

        for (Pedido p : servicioPedidos.listarPedidos()) {

            String estado = safeEstado(p);
            String id4 = String.format("%04d", p.getId());

            String textoBase = String.format(
                    "Pedido #%s - Pizza personalizada\nTotal: $%.2f",
                    id4,
                    p.getTotal()
            );

            String display = "CANCELADO".equalsIgnoreCase(estado)
                    ? "[CANCELADO] " + textoBase
                    : textoBase;

            // activos = no entregado y no cancelado
            if (!estado.equalsIgnoreCase("ENTREGADO") && !estado.equalsIgnoreCase("CANCELADO")) {
                activos++;
            }

            switch (estado.toUpperCase()) {
                case "RECIBIDO":
                    view.listaRecibido.getItems().add(display);
                    break;
                case "PREPARANDO":
                    view.listaPreparando.getItems().add(display);
                    break;
                case "HORNEANDO":
                    view.listaHorneando.getItems().add(display);
                    break;
                case "TERMINADO":
                    view.listaTerminado.getItems().add(display);
                    break;
                case "ENTREGADO":
                    view.listaEntregado.getItems().add(display);
                    break;
                case "CANCELADO":
                    // si no hay columna cancelado, lo dejamos en Recibido
                    view.listaRecibido.getItems().add(display);
                    break;
                default:
                    view.listaRecibido.getItems().add(display);
            }
        }

        // actualizar contadores de columnas (tu view lo hace interno con sizes, así que basta con que existan items)
        // si quieres, aquí podrías llamar a un método actualizarContadores() si lo haces público.
    }

    private String safeEstado(Pedido p) {
        try {
            String e = p.getNombreEstado();
            return (e == null || e.isBlank()) ? "RECIBIDO" : e.trim();
        } catch (Exception ex) {
            return "RECIBIDO";
        }
    }

    private String ahora() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    // =========================================================
    // Abrir detalle
    // =========================================================
    private void abrirDetallePedidoDesdeItem(String item) {
        String numeroPedido = extraerNumeroPedido(item); // "PED0003"
        int id = idFromNumeroPedido(numeroPedido);       // 3

        Pedido p = (id > 0) ? servicioPedidos.buscarPedidoPorId(id) : null;
        String estadoReal = (p != null) ? safeEstado(p) : "RECIBIDO";

        abrirDetallePedido(numeroPedido, estadoReal);
    }

    private int idFromNumeroPedido(String numeroPedido) {
        if (numeroPedido == null) return -1;
        String aux = numeroPedido.trim().toUpperCase();
        if (aux.startsWith("PED")) aux = aux.substring(3);
        try {
            return Integer.parseInt(aux);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String extraerNumeroPedido(String pedidoInfo) {
        String texto = pedidoInfo;
        if (texto == null) return "PED0000";

        if (texto.startsWith("[CANCELADO]")) {
            texto = texto.replace("[CANCELADO] ", "");
        }

        if (texto.contains("#")) {
            String[] parts = texto.split("#");
            if (parts.length > 1) {
                String numero = parts[1].split(" ")[0]; // "0003"
                return "PED" + numero;                  // "PED0003"
            }
        }
        return "PED0000";
    }

    private void abrirDetallePedido(String numeroPedido, String estado) {
        try {
            Stage detalleStage = new Stage();
            DetallePedidoView detalleView = new DetallePedidoView(detalleStage);

            detalleView.lblNumeroPedido.setText("Pedido #" + numeroPedido);

            // primer frame visual
            detalleView.actualizarEstadoVisual(estado);
            configurarIndicadorEstado(detalleView, estado);

            // specs desde pedidos.txt (solo UI)
            cargarEspecificacionesParaVista(detalleView, numeroPedido);

            DetallePedidoController controller = new DetallePedidoController();
            controller.setView(detalleView);

            detalleView.mostrar();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir el detalle del pedido", e.getMessage());
        }
    }

    // =========================================================
    // Specs desde pedidos.txt (solo UI)
    // =========================================================
    private void cargarEspecificacionesParaVista(DetallePedidoView detalleView, String numeroPedido) {
        File file = new File(PEDIDOS_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            StringBuilder pedidoActual = new StringBuilder();
            boolean pedidoEncontrado = false;

            while ((line = reader.readLine()) != null) {
                if (line.contains("=== PEDIDO " + numeroPedido)) {
                    pedidoEncontrado = true;
                }

                if (pedidoEncontrado) {
                    pedidoActual.append(line).append("\n");
                    if (line.startsWith("=============================")) break;
                }
            }

            if (pedidoEncontrado) {
                procesarEspecificacionesParaVista(detalleView, pedidoActual.toString());
            }
        } catch (IOException e) {
            System.err.println("Error al cargar especificaciones: " + e.getMessage());
        }
    }

    private void procesarEspecificacionesParaVista(DetallePedidoView detalleView, String datosPedido) {
        String[] lineas = datosPedido.split("\n");
        detalleView.listaEspecificaciones.getItems().clear();

        for (String linea : lineas) {
            if (linea.startsWith("Masa:")) {
                detalleView.listaEspecificaciones.getItems().add("Base: " + linea.replace("Masa:", "").trim());
            } else if (linea.startsWith("Salsa:")) {
                detalleView.listaEspecificaciones.getItems().add("Salsa: " + linea.replace("Salsa:", "").trim());
            } else if (linea.startsWith("Queso:")) {
                detalleView.listaEspecificaciones.getItems().add("Queso: " + linea.replace("Queso:", "").trim());
            } else if (linea.startsWith("Orilla:")) {
                detalleView.listaEspecificaciones.getItems().add("Orilla: " + linea.replace("Orilla:", "").trim());
            } else if (linea.startsWith("Ingredientes:")) {
                detalleView.listaEspecificaciones.getItems().add("");
                detalleView.listaEspecificaciones.getItems().add("Ingredientes Adicionales:");
                String ingreds = linea.replace("Ingredientes:", "").trim();
                if (!ingreds.isEmpty()) {
                    String[] items = ingreds.split(", ");
                    for (String it : items) {
                        if (!it.trim().isEmpty()) {
                            detalleView.listaEspecificaciones.getItems().add("– " + it.trim());
                        }
                    }
                }
            } else if (linea.startsWith("Condimentos:")) {
                detalleView.listaEspecificaciones.getItems().add("");
                detalleView.listaEspecificaciones.getItems().add("Condimentos:");
                String conds = linea.replace("Condimentos:", "").trim();
                if (!conds.isEmpty()) {
                    String[] items = conds.split(", ");
                    for (String it : items) {
                        if (!it.trim().isEmpty()) {
                            detalleView.listaEspecificaciones.getItems().add("+ " + it.trim());
                        }
                    }
                }
            } else if (linea.startsWith("Total:")) {
                detalleView.listaEspecificaciones.getItems().add("");
                detalleView.lblTotal.setText(linea.replace("Total:", "").trim());
            }
        }

        detalleView.listaEspecificaciones.getItems().add("");
        detalleView.listaEspecificaciones.getItems().add(
                "Hora del pedido: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        );
    }

    private void configurarIndicadorEstado(DetallePedidoView detalleView, String estado) {
        switch (estado.toUpperCase()) {
            case "RECIBIDO":
                detalleView.indicadorCircular.setFill(javafx.scene.paint.Color.RED);
                break;
            case "PREPARANDO":
                detalleView.indicadorCircular.setFill(javafx.scene.paint.Color.ORANGE);
                break;
            case "HORNEANDO":
                detalleView.indicadorCircular.setFill(javafx.scene.paint.Color.YELLOW);
                break;
            case "TERMINADO":
                detalleView.indicadorCircular.setFill(javafx.scene.paint.Color.GREEN);
                break;
            case "ENTREGADO":
                detalleView.indicadorCircular.setFill(javafx.scene.paint.Color.BLUE);
                break;
            case "CANCELADO":
                detalleView.indicadorCircular.setFill(javafx.scene.paint.Color.DARKRED);
                break;
            default:
                detalleView.indicadorCircular.setFill(javafx.scene.paint.Color.GRAY);
        }
    }

    // =========================================================
    // Auto refresh
    // =========================================================
    private void iniciarAutoRefresh() {
        detenerAutoRefresh();

        autoRefresh = new Timeline(
                new KeyFrame(Duration.seconds(2), e -> cargarPedidosDesdeServicio())
        );
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();
    }

    private void detenerAutoRefresh() {
        if (autoRefresh != null) {
            autoRefresh.stop();
            autoRefresh = null;
        }
    }

    // =========================================================
    // Helpers para status/activos (sin romper si aún no los expones)
    // =========================================================
    private void setStatus(String txt) {
        if (view.lblStatus != null) view.lblStatus.setText(txt);
    }

    private void setActivos(int n) {
        if (view.lblTotalPedidos != null) view.lblTotalPedidos.setText("Pedidos activos: " + n);
    }

    private void mostrarAlerta(String titulo, String encabezado, String contenido) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(encabezado);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}
