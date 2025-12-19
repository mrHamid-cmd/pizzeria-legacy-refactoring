package com.pizzasystem.controladores;

import javafx.scene.control.*;
import javafx.stage.Stage;
import com.pizzasystem.vistas.DetallePedidoView;
import com.pizzasystem.vistas.PanelControlPedidosView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

import negocio.modelo.Pedido;
import negocio.servicios.ServicioPedidos;

public class DetallePedidoController {

    private DetallePedidoView view;
    private Stage stage;

    private String numeroPedido;      
    private int idPedidoModelo = -1;  

    private final ServicioPedidos servicioPedidos = new ServicioPedidos();
    private Pedido pedido;

    public void setView(DetallePedidoView view) {
        this.view = view;
        this.stage = view.getStage();

        // Detener animaciones al cerrar
        if (this.stage != null) {
            this.stage.setOnCloseRequest(e -> view.detenerAnimaciones());
        }

        // Obtener ID del pedido
        this.numeroPedido = extraerNumeroPedido(view.lblNumeroPedido.getText());
        this.idPedidoModelo = obtenerIdDesdeNumeroPedido(this.numeroPedido);

        // Configurar botones
        configurarAcciones();

        // 1. Conectar con el objeto real en memoria
        conectarPedidoReal();

        // 2. Cargar la visualización (Separando Condimentos)
        cargarDatosVisuales();            
        
        // 3. Actualizar estado y botones
        refrescarEstadoDesdeObjeto();   
    }

    private void configurarAcciones() {
        view.btnRegresar.setOnAction(e -> regresarAlTablero());
        view.btnImprimirTicket.setOnAction(e -> imprimirTicket());
        view.btnEnviarAHorno.setOnAction(e -> avanzarEstado());
        view.btnCancelarPedido.setOnAction(e -> cancelarPedido());
    }

    // =========================================================
    // LÓGICA DE CARGA DE DATOS (Ingredientes vs Condimentos)
    // =========================================================
    
    private void cargarDatosVisuales() {
        // A) PRECIO
        if (pedido != null) {
            view.actualizarTotal(pedido.getTotal());
        } else {
            view.actualizarTotal(0.0);
        }

        // B) DETALLES
        // Primero intentamos leer el log antiguo si existe (para compatibilidad)
        String datosLog = servicioPedidos.obtenerTextoLogPedido(numeroPedido);

        if (datosLog != null) {
            procesarDatosLog(datosLog);
        } else if (pedido != null) {
            // Si no hay log, usamos el objeto en memoria y separamos las listas
            generarDetallesDesdeObjeto();
        } else {
            usarDatosPrueba();
        }
    }

    /**
     * Toma los datos del objeto Pedido y separa ingredientes de condimentos
     * para mostrarlos ordenados en la vista.
     */
    private void generarDetallesDesdeObjeto() {
        List<String> items = new ArrayList<>();
        
        // 1. Datos Base de la Pizza
        if (pedido.getPizza() != null) {
            String masa = pedido.getPizza().getMasa() != null ? pedido.getPizza().getMasa() : "Estándar";
            String salsa = pedido.getPizza().getSalsa() != null ? pedido.getPizza().getSalsa() : "Tomate";
            String queso = pedido.getPizza().getQueso() != null ? pedido.getPizza().getQueso() : "Mozzarella";
            String orilla = pedido.getPizza().getTipoOrilla()!= null ? pedido.getPizza().getTipoOrilla(): "Normal";
            
            items.add("Base: " + masa);
            items.add("Salsa: " + salsa);
            items.add("Queso: " + queso);
            items.add("Orilla: " + orilla);
        } else {
            items.add("Información de pizza no disponible");
        }
        
        items.add(""); // Espacio

        // Listas temporales
        List<String> listaIngredientes = new ArrayList<>();
        List<String> listaCondimentos = new ArrayList<>();

        // 2. Clasificación Inteligente
        // Recorremos la lista de ingredientes del modelo y los separamos
        if (pedido.getPizza() != null && pedido.getPizza().getIngredientes() != null) {
            for (String item : pedido.getPizza().getIngredientes()) {
                if (esCondimento(item)) {
                    listaCondimentos.add(item);
                } else {
                    listaIngredientes.add(item);
                }
            }
        }
        
        // NOTA: Si tu clase Pizza tiene una lista aparte llamada getCondimentos(), 
        // agrégala aquí también:
        /* if (pedido.getPizza().getCondimentos() != null) {
            listaCondimentos.addAll(pedido.getPizza().getCondimentos());
        }
        */

        // 3. Agregar a la vista - Ingredientes
        if (!listaIngredientes.isEmpty()) {
            items.add("Ingredientes Adicionales:");
            for (String ing : listaIngredientes) {
                items.add("– " + ing);
            }
            items.add("");
        }

        // 4. Agregar a la vista - Condimentos
        if (!listaCondimentos.isEmpty()) {
            items.add("Condimentos y Especias:");
            for (String cond : listaCondimentos) {
                items.add("+ " + cond);
            }
            items.add("");
        }
        
        // 5. Hora
        items.add("Hora: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));

        view.llenarDetalles(items);
    }

    /**
     * Determina si un item es un condimento basándose en palabras clave.
     */
    private boolean esCondimento(String nombre) {
        if (nombre == null) return false;
        String n = nombre.toLowerCase().trim();
        
        // Lista de palabras clave para detectar especias
        return n.contains("orégano") || n.contains("oregano") ||
               n.contains("parmesano") || n.contains("polvo") ||
               n.contains("chile") || n.contains("quebrado") ||
               n.contains("pimienta") || n.contains("ajo") || 
               n.contains("albahaca") || n.contains("romero") ||
               n.contains("finas hierbas") || n.contains("perejil") ||
               n.contains("salsa") || n.contains("valentina") || 
               n.contains("botanera") || n.contains("catsup") || 
               n.contains("ketchup") || n.contains("mostaza") || 
               n.contains("aceite") || n.contains("chimichurri");
    }

    private void procesarDatosLog(String datosPedido) {
        String[] lineas = datosPedido.split("\n");
        String masa = "N/A", salsa = "N/A", queso = "N/A", orilla = "N/A";
        List<String> ingreds = new ArrayList<>();
        List<String> conds = new ArrayList<>();

        // Parseo compatible con el formato antiguo
        for (String linea : lineas) {
            if (linea.startsWith("Masa:")) masa = linea.replace("Masa:", "").trim();
            else if (linea.startsWith("Salsa:")) salsa = linea.replace("Salsa:", "").trim();
            else if (linea.startsWith("Queso:")) queso = linea.replace("Queso:", "").trim();
            else if (linea.startsWith("Orilla:")) orilla = linea.replace("Orilla:", "").trim();
            else if (linea.startsWith("Ingredientes:")) {
                String texto = linea.replace("Ingredientes:", "").trim();
                for (String s : texto.split(", ")) if(!s.isBlank()) ingreds.add("– " + s.trim());
            } else if (linea.startsWith("Condimentos:")) {
                String texto = linea.replace("Condimentos:", "").trim();
                for (String s : texto.split(", ")) if(!s.isBlank()) conds.add("+ " + s.trim());
            }
        }

        List<String> items = new ArrayList<>();
        items.add("Base: " + masa);
        items.add("Salsa: " + salsa);
        items.add("Queso: " + queso);
        items.add("Orilla: " + orilla);
        items.add("");

        if (!ingreds.isEmpty()) {
            items.add("Ingredientes Adicionales:");
            items.addAll(ingreds);
            items.add("");
        }
        if (!conds.isEmpty()) {
            items.add("Condimentos:");
            items.addAll(conds);
            items.add("");
        }

        items.add("Hora: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        view.llenarDetalles(items);
    }

    private void usarDatosPrueba() {
        List<String> items = new ArrayList<>();
        items.add("Base: Masa Tradicional");
        items.add("Salsa: Clásica");
        items.add("");
        items.add("– Sin información detallada");
        
        view.llenarDetalles(items);
        if (pedido == null) view.actualizarTotal(0.00);
    }

    // =========================================================
    // HELPERS DE MODELO
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
        if (pedido != null && pedido.getNombreEstado() != null) {
            estado = pedido.getNombreEstado().trim();
        }
        view.actualizarEstadoVisual(estado);
        
        boolean bloquear = "CANCELADO".equalsIgnoreCase(estado) || "ENTREGADO".equalsIgnoreCase(estado);
        view.btnEnviarAHorno.setDisable(bloquear);
        view.btnCancelarPedido.setDisable(bloquear);
    }

    private String extraerNumeroPedido(String texto) {
        if (texto == null) return "PED0000";
        if (texto.contains("#")) return texto.split("#")[1].trim();
        return texto.replace("Pedido", "").trim();
    }

    private int obtenerIdDesdeNumeroPedido(String numeroPedido) {
        if (numeroPedido == null) return -1;
        String aux = numeroPedido.trim().toUpperCase();
        if (aux.startsWith("PED")) aux = aux.substring(3);
        try {
            return Integer.parseInt(aux);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // =========================================================
    // ACCIONES DE BOTONES
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
            String estado = view.getEstadoActual();
            String total = view.lblTotal.getText();
            List<String> items = new ArrayList<>(view.listaEspecificaciones.getItems());

            String rutaArchivo = servicioPedidos.generarTicketFisico(numeroPedido, estado, items, total);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Ticket Generado");
            alert.setHeaderText("Ticket impreso exitosamente");
            alert.setContentText("Guardado en:\n" + rutaArchivo);
            alert.showAndWait();

        } catch (Exception e) {
            mostrarError("Error", "Error al generar ticket", e.getMessage());
        }
    }

    private void avanzarEstado() {
        if (idPedidoModelo <= 0) return; 
        
        conectarPedidoReal(); 
        if (pedido == null) return;
        
        servicioPedidos.avanzarEstadoPedido(idPedidoModelo);
        
        conectarPedidoReal();
        refrescarEstadoDesdeObjeto();
        
        mostrarInfo("Estado Actualizado", "¡Estado cambiado!", "Ahora: " + view.getEstadoActual());
    }

    private void cancelarPedido() {
        if (idPedidoModelo <= 0) return;
        
        conectarPedidoReal();
        if (pedido == null) return;
        
        servicioPedidos.cancelarPedido(idPedidoModelo);
        
        conectarPedidoReal();
        refrescarEstadoDesdeObjeto();
        
        mostrarInfo("Pedido Cancelado", null, "El pedido ha sido cancelado.");
    }

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