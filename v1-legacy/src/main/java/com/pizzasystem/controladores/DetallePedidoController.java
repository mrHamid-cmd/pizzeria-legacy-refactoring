package com.pizzasystem.controladores;

import javafx.scene.control.*;
import javafx.stage.Stage;

import com.pizzasystem.vistas.DetallePedidoView;
import com.pizzasystem.vistas.PanelControlPedidosView;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import negocio.modelo.Pedido;
import negocio.servicios.ServicioPedidos;

public class DetallePedidoController {

    // Referencia a la vista
    private DetallePedidoView view;
    private Stage stage;

    private String numeroPedido;      // Ej: "PED0003"
    private int idPedidoModelo = -1;  // Ej: 3

    // Archivo "visible" (solo para mostrar especificaciones / ticket)
    private static final String PEDIDOS_FILE = "pedidos.txt";
    private static final String TICKETS_DIR = "tickets";

    // Servicio de negocio (fuente real)
    private final ServicioPedidos servicioPedidos = new ServicioPedidos();

    // Pedido real en memoria
    private Pedido pedido;

    public void setView(DetallePedidoView view) {
        this.view = view;
        this.stage = view.getStage();

        // ✅ Cierre limpio: detiene animaciones aunque cierren con la X
        if (this.stage != null) {
            this.stage.setOnCloseRequest(e -> view.detenerAnimaciones());
        }

        // "Pedido #PED0003" -> "PED0003"
        this.numeroPedido = extraerNumeroPedido(view.lblNumeroPedido.getText());
        this.idPedidoModelo = obtenerIdDesdeNumeroPedido(this.numeroPedido);

        configurarAcciones();
        cargarDatosPedido();           // especificaciones (de pedidos.txt) solo para UI
        conectarPedidoReal();          // estado real desde objetos
        refrescarEstadoDesdeObjeto();  // pinta estado real y botones
    }

    private void configurarAcciones() {
        view.btnRegresar.setOnAction(e -> regresarAlTablero());
        view.btnImprimirTicket.setOnAction(e -> imprimirTicket());
        view.btnEnviarAHorno.setOnAction(e -> avanzarEstado());
        view.btnCancelarPedido.setOnAction(e -> cancelarPedido());
    }

    private String extraerNumeroPedido(String texto) {
        if (texto == null) return "PED0000";
        if (texto.contains("#")) {
            return texto.split("#")[1].trim();
        }
        return texto.replace("Pedido", "").trim();
    }

    private int obtenerIdDesdeNumeroPedido(String numeroPedido) {
        if (numeroPedido == null) return -1;

        String aux = numeroPedido.trim().toUpperCase();
        if (aux.startsWith("PED")) aux = aux.substring(3);

        try {
            return Integer.parseInt(aux);
        } catch (NumberFormatException e) {
            System.err.println("No se pudo convertir numeroPedido a ID: " + numeroPedido);
            return -1;
        }
    }

    // =========================================================
    // 1) DATOS DE ESPECIFICACIONES (solo UI)
    // =========================================================
    private void cargarDatosPedido() {
        try {
            File file = new File(PEDIDOS_FILE);
            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    StringBuilder pedidoActual = new StringBuilder();
                    boolean pedidoEncontrado = false;

                    while ((line = reader.readLine()) != null) {
                        if (line.contains("PEDIDO " + numeroPedido)
                                || line.contains("=== PEDIDO " + numeroPedido)) {
                            pedidoEncontrado = true;
                        }

                        if (pedidoEncontrado) {
                            pedidoActual.append(line).append("\n");

                            // en tu archivo el fin real es: "============================="
                            if (line.startsWith("=============================")) {
                                break;
                            }
                        }
                    }

                    if (pedidoEncontrado) {
                        procesarDatosPedido(pedidoActual.toString());
                        return;
                    }
                }
            }

            usarDatosPrueba();

        } catch (IOException e) {
            System.err.println("Error al cargar datos del pedido: " + e.getMessage());
            usarDatosPrueba();
        }
    }

    private void procesarDatosPedido(String datosPedido) {
        String[] lineas = datosPedido.split("\n");

        String masa = "", salsa = "", queso = "", orilla = "";
        StringBuilder ingredientes = new StringBuilder();
        StringBuilder condimentos = new StringBuilder();
        String total = "$0.00";
        String cliente = "Cliente";

        for (String linea : lineas) {
            if (linea.startsWith("Masa:")) {
                masa = linea.replace("Masa:", "").trim();
            } else if (linea.startsWith("Salsa:")) {
                salsa = linea.replace("Salsa:", "").trim();
            } else if (linea.startsWith("Queso:")) {
                queso = linea.replace("Queso:", "").trim();
            } else if (linea.startsWith("Orilla:")) {
                orilla = linea.replace("Orilla:", "").trim();
            } else if (linea.startsWith("Ingredientes:")) {
                String ingreds = linea.replace("Ingredientes:", "").trim();
                if (!ingreds.isEmpty()) {
                    for (String item : ingreds.split(", ")) {
                        if (!item.trim().isEmpty()) {
                            ingredientes.append("– ").append(item.trim()).append("\n");
                        }
                    }
                }
            } else if (linea.startsWith("Condimentos:")) {
                String conds = linea.replace("Condimentos:", "").trim();
                if (!conds.isEmpty()) {
                    for (String item : conds.split(", ")) {
                        if (!item.trim().isEmpty()) {
                            condimentos.append("+ ").append(item.trim()).append("\n");
                        }
                    }
                }
            } else if (linea.startsWith("Total:")) {
                total = linea.replace("Total:", "").trim();
            } else if (linea.startsWith("Cliente:")) {
                cliente = linea.replace("Cliente:", "").trim();
            }
        }

        view.listaEspecificaciones.getItems().clear();
        view.listaEspecificaciones.getItems().addAll(
                "Base: " + masa,
                "Salsa: " + salsa,
                "Queso: " + queso,
                "Orilla: " + orilla,
                ""
        );

        if (ingredientes.length() > 0) {
            view.listaEspecificaciones.getItems().add("Ingredientes Adicionales:");
            view.listaEspecificaciones.getItems().addAll(ingredientes.toString().split("\n"));
            view.listaEspecificaciones.getItems().add("");
        }

        if (condimentos.length() > 0) {
            view.listaEspecificaciones.getItems().add("Condimentos:");
            view.listaEspecificaciones.getItems().addAll(condimentos.toString().split("\n"));
            view.listaEspecificaciones.getItems().add("");
        }

        view.listaEspecificaciones.getItems().addAll(
                "Cliente: " + cliente,
                "Hora del pedido: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                ""
        );

        view.lblTotal.setText(total);
    }

    private void usarDatosPrueba() {
        String[] especificaciones = {
                "Base: Masa Tradicional",
                "Salsa: Salsa de Tomate Clásica",
                "Queso: Mozzarella",
                "Orilla: Orilla Tradicional",
                "",
                "Ingredientes Adicionales:",
                "– Pepperoni",
                "– Champiñones",
                "– Pimientos",
                "",
                "Condimentos:",
                "+ Orégano",
                "+ Parmesano en Polvo",
                "",
                "Cliente: Cliente de Prueba",
                "Hora del pedido: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        };

        view.listaEspecificaciones.getItems().clear();
        view.listaEspecificaciones.getItems().addAll(especificaciones);
        view.lblTotal.setText("$18.50");
    }

    // =========================================================
    // 2) ESTADO REAL (OBJETO)
    // =========================================================
    private void conectarPedidoReal() {
        if (idPedidoModelo <= 0) {
            pedido = null;
            return;
        }
        pedido = servicioPedidos.buscarPedidoPorId(idPedidoModelo);
    }

    private void refrescarEstadoDesdeObjeto() {
        String estado = "RECIBIDO";

        if (pedido != null) {
            String n = pedido.getNombreEstado();
            if (n != null && !n.isBlank()) estado = n.trim();
        }

        view.actualizarEstadoVisual(estado);

        boolean bloquear = "CANCELADO".equalsIgnoreCase(estado) || "ENTREGADO".equalsIgnoreCase(estado);
        view.btnEnviarAHorno.setDisable(bloquear);
        view.btnCancelarPedido.setDisable(bloquear);
    }

    // =========================================================
    // Acciones
    // =========================================================
    private void regresarAlTablero() {
        view.detenerAnimaciones();
        if (stage != null) stage.close();

        Stage panelStage = new Stage();
        PanelControlPedidosView panelView = new PanelControlPedidosView(panelStage);

        PanelControlPedidosController controller = new PanelControlPedidosController();
        controller.setView(panelView);

        panelView.mostrar();
    }

    private void imprimirTicket() {
        try {
            File directorio = new File(TICKETS_DIR);
            if (!directorio.exists()) directorio.mkdirs();

            String nombreArchivo = TICKETS_DIR + "/ticket_" + numeroPedido + "_"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";

            try (PrintWriter writer = new PrintWriter(new FileWriter(nombreArchivo))) {
                writer.println("========================================");
                writer.println("           PIZZERÍA DEL SISTEMA         ");
                writer.println("========================================");
                writer.println("Ticket: " + numeroPedido);
                writer.println("Fecha: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                writer.println("Estado: " + view.getEstadoActual());
                writer.println("----------------------------------------");
                writer.println("ESPECIFICACIONES DE LA PIZZA:");
                writer.println("----------------------------------------");

                for (String item : view.listaEspecificaciones.getItems()) {
                    writer.println(item);
                }

                writer.println("========================================");
                writer.println("TOTAL: " + view.lblTotal.getText());
                writer.println("========================================");
                writer.println("         ¡GRACIAS POR SU COMPRA!        ");
                writer.println("========================================");
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Ticket Generado");
            alert.setHeaderText("Ticket impreso exitosamente");
            alert.setContentText("Se ha generado el archivo:\n" + nombreArchivo);
            alert.showAndWait();

        } catch (IOException e) {
            mostrarError("Error", "Error al generar ticket", e.getMessage());
        }
    }

    private void avanzarEstado() {
        if (idPedidoModelo <= 0) {
            mostrarError("Error", "Pedido inválido", "No se pudo identificar el pedido para avanzar estado.");
            return;
        }

        conectarPedidoReal();
        if (pedido == null) {
            mostrarError("Error", "Pedido no encontrado", "No se encontró el pedido en memoria para avanzar estado.");
            return;
        }

        String estadoActual = pedido.getNombreEstado();
        if ("CANCELADO".equalsIgnoreCase(estadoActual) || "ENTREGADO".equalsIgnoreCase(estadoActual)) {
            mostrarInfo("Información", "Acción no disponible",
                    "El pedido ya está en estado '" + estadoActual + "'.");
            refrescarEstadoDesdeObjeto();
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Cambio de Estado");
        confirmacion.setHeaderText("¿Avanzar estado del pedido?");
        confirmacion.setContentText("Esta acción avanzará el pedido al siguiente estado y se reflejará en el tablero y seguimiento.");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // ✅ fuente real: negocio
                servicioPedidos.avanzarEstadoPedido(idPedidoModelo);

                // refrescar desde el objeto (no inventar estado por UI)
                conectarPedidoReal();
                refrescarEstadoDesdeObjeto();

                Alert exito = new Alert(Alert.AlertType.INFORMATION);
                exito.setTitle("Estado Actualizado");
                exito.setHeaderText("¡Estado cambiado exitosamente!");
                exito.setContentText("El pedido ahora está en estado: " + view.getEstadoActual());
                exito.showAndWait();
            }
        });
    }

    private void cancelarPedido() {
        if (idPedidoModelo <= 0) {
            mostrarError("Error", "Pedido inválido", "No se pudo identificar el pedido para cancelarlo.");
            return;
        }

        conectarPedidoReal();
        if (pedido == null) {
            mostrarError("Error", "Pedido no encontrado", "No se encontró el pedido en memoria para cancelarlo.");
            return;
        }

        String estadoActual = pedido.getNombreEstado();
        if ("CANCELADO".equalsIgnoreCase(estadoActual)) {
            mostrarInfo("Información", "Ya cancelado", "Este pedido ya está cancelado.");
            refrescarEstadoDesdeObjeto();
            return;
        }
        if ("ENTREGADO".equalsIgnoreCase(estadoActual)) {
            mostrarInfo("Información", "No permitido", "No puedes cancelar un pedido entregado.");
            refrescarEstadoDesdeObjeto();
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Cancelación");
        confirmacion.setHeaderText("¿Cancelar este pedido?");
        confirmacion.setContentText("Esta acción no se puede deshacer. El pedido será marcado como cancelado.");

        ButtonType btnSi = new ButtonType("Sí, Cancelar");
        ButtonType btnNo = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmacion.getButtonTypes().setAll(btnSi, btnNo);

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == btnSi) {
                // ✅ fuente real: negocio
                servicioPedidos.cancelarPedido(idPedidoModelo);

                conectarPedidoReal();
                refrescarEstadoDesdeObjeto();

                Alert exito = new Alert(Alert.AlertType.INFORMATION);
                exito.setTitle("Pedido Cancelado");
                exito.setHeaderText(null);
                exito.setContentText("El pedido ha sido cancelado exitosamente.");
                exito.showAndWait();
            }
        });
    }

    // ✅ Helpers correctos (INFO vs ERROR)
    private void mostrarInfo(String titulo, String encabezado, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(encabezado);
        alert.setContentText(contenido);
        alert.showAndWait();
    }

    private void mostrarError(String titulo, String encabezado, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(encabezado);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}
