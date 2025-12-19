package com.pizzasystem.controladores;

import com.pizzasystem.vistas.DetallePedidoView;
import com.pizzasystem.vistas.PanelControlPedidosView;
import com.pizzasystem.vistas.TomaPedidoView;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

// YA NO HAY java.io.* (File, BufferedReader, etc.)
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import negocio.modelo.Pedido;
import negocio.servicios.ServicioPedidos;

public class PanelControlPedidosController {

    private PanelControlPedidosView view;
    private Stage stage;

    // Conexión única con los datos
    private final ServicioPedidos servicioPedidos = new ServicioPedidos();

    // Auto refresco
    private Timeline autoRefresh;

    public void setView(PanelControlPedidosView view) {
        this.view = view;
        this.stage = view.getStage();

        configurarAcciones();
        configurarListenersListas();

        // Carga inicial desde OBJETOS
        cargarPedidosDesdeServicio();

        // Iniciar auto refresh
        iniciarAutoRefresh();

        if (this.stage != null) {
            this.stage.setOnCloseRequest(e -> detenerAutoRefresh());
        }
        
        // Actualizamos estado inicial del footer
        setStatus("Sistema listo - " + ahora());
        setActivos(servicioPedidos.listarPedidos().size());
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
    // Tablero desde OBJETOS (Fuente de Verdad: Servicio)
    // =========================================================
    private void cargarPedidosDesdeServicio() {

        // Limpiar listas
        view.listaRecibido.getItems().clear();
        view.listaPreparando.getItems().clear();
        view.listaHorneando.getItems().clear();
        view.listaTerminado.getItems().clear();
        view.listaEntregado.getItems().clear();

        int activos = 0;
        var listaPedidos = servicioPedidos.listarPedidos();

        for (Pedido p : listaPedidos) {

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

            // Lógica de conteo de activos
            if (!estado.equalsIgnoreCase("ENTREGADO") && !estado.equalsIgnoreCase("CANCELADO")) {
                activos++;
            }

            // Distribuir en columnas
            switch (estado.toUpperCase()) {
                case "RECIBIDO":   view.listaRecibido.getItems().add(display); break;
                case "PREPARANDO": view.listaPreparando.getItems().add(display); break;
                case "HORNEANDO":  view.listaHorneando.getItems().add(display); break;
                case "TERMINADO":  view.listaTerminado.getItems().add(display); break;
                case "ENTREGADO":  view.listaEntregado.getItems().add(display); break;
                case "CANCELADO":  view.listaRecibido.getItems().add(display); break; 
                default:           view.listaRecibido.getItems().add(display);
            }
        }
        
        setActivos(activos);
        setStatus("Actualizado: " + ahora());
    }

    // =========================================================
    // Abrir detalle (Delegación limpia)
    // =========================================================
    private void abrirDetallePedidoDesdeItem(String item) {
        String numeroPedido = extraerNumeroPedido(item); // "PED0003"
        // No necesitamos buscar el estado ni cargar datos aquí. 
        // El DetallePedidoController lo hará él mismo al iniciarse.
        
        abrirDetallePedidoWindow(numeroPedido);
    }

    private void abrirDetallePedidoWindow(String numeroPedido) {
        try {
            Stage detalleStage = new Stage();
            DetallePedidoView detalleView = new DetallePedidoView(detalleStage);

            // Configuramos SOLO el dato clave (el ID en el label)
            // El DetallePedidoController leerá este label para saber qué buscar en la DB/Archivo
            detalleView.lblNumeroPedido.setText("Pedido #" + numeroPedido);

            // Instanciamos el controlador del detalle
            DetallePedidoController controller = new DetallePedidoController();
            
            // Al llamar a setView, el controlador interno se despertará,
            // llamará al ServicioPedidos y cargará sus propios datos (log y estado).
            controller.setView(detalleView);

            detalleView.mostrar();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir el detalle del pedido", e.getMessage());
        }
    }

    // =========================================================
    // Helpers
    // =========================================================

    private String safeEstado(Pedido p) {
        try {
            String e = p.getNombreEstado();
            return (e == null || e.isBlank()) ? "RECIBIDO" : e.trim();
        } catch (Exception ex) {
            return "RECIBIDO";
        }
    }

    private String ahora() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
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
                // Formato esperado: "Pedido #0003 - ..."
                String sub = parts[1]; 
                // Tomamos lo que esté antes del espacio o guión
                if(sub.contains(" ")) sub = sub.split(" ")[0];
                else if(sub.contains("-")) sub = sub.split("-")[0];
                
                return "PED" + sub.trim(); // "PED0003"
            }
        }
        return "PED0000";
    }

    // =========================================================
    // Auto refresh
    // =========================================================
    private void iniciarAutoRefresh() {
        detenerAutoRefresh();
        // Refrescar cada 2 segundos para ver cambios de otros usuarios/hilos
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
    // Actualización UI Footer
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