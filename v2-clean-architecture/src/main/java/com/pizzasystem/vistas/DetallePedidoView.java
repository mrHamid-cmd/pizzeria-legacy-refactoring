package com.pizzasystem.vistas;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

/**
 * Vista de Detalle de Pedido.
 * Muestra la información completa del pedido, el precio total y el estado actual.
 * Permite cambiar el estado del pedido y realizar acciones (Imprimir, Cancelar).
 */
public class DetallePedidoView {

    private Stage stage;
    private StackPane root;
    private BorderPane cardContainer;

    // =========================================================
    // COMPONENTES PÚBLICOS (Para acceso desde el Controlador)
    // =========================================================
    
    // Navegación e Info
    public Button btnRegresar;
    public Label lblNumeroPedido;
    
    // Lista de items y Precio
    public ListView<String> listaEspecificaciones;
    public Label lblTotal; // 

    // Panel de Estado (Derecha)
    public ProgressBar progressBarEstado;
    public Label lblEstadoTexto;
    public Circle indicadorCircular;
    public Label lblLetraCircular;
    
    // Botones de Acción
    public Button btnImprimirTicket;
    public Button btnEnviarAHorno; 
    public Button btnCancelarPedido;

    // Lógica interna de animación
    private String estadoActual;
    private Timeline animacionBarra;
    private Timeline animacionCirculoColor;
    private Timeline animacionCircularPulse;

    public DetallePedidoView(Stage primaryStage) {
        this.stage = primaryStage;
        this.estadoActual = "RECIBIDO";
        crearVista();
    }

    private void crearVista() {
        // 1. Configuración del Root (Fondo)
        root = new StackPane();
        try {
            String imagePath = getClass().getResource("/imagenes/fondo4.png").toExternalForm();
            root.setStyle(
                "-fx-background-image: url('" + imagePath + "'); " +
                "-fx-background-size: cover; " +
                "-fx-background-position: center center;"
            );
        } catch (Exception e) {
            root.setStyle("-fx-background-color: #ecf0f1;");
        }

        // 2. Tarjeta Central
        cardContainer = new BorderPane();
        cardContainer.setMaxSize(1100, 700);
        cardContainer.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.95); " + 
            "-fx-background-radius: 20; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 10);"
        );

        // A. TOP
        cardContainer.setTop(crearEncabezado());

        // B. CENTER
        GridPane centerGrid = new GridPane();
        centerGrid.setPadding(new Insets(20, 40, 20, 40));
        centerGrid.setHgap(40);
        
        ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setPercentWidth(50);
        centerGrid.getColumnConstraints().addAll(col1, col2);

        centerGrid.add(crearPanelTicket(), 0, 0); // Izquierda: Lista y Total
        centerGrid.add(crearPanelEstado(), 1, 0); // Derecha: Estado gráfico

        cardContainer.setCenter(centerGrid);

        // C. BOTTOM
        cardContainer.setBottom(crearBotoneraInferior());

        root.getChildren().add(cardContainer);
        iniciarAnimacionCircular();
    }

    private HBox crearEncabezado() {
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(25, 30, 15, 30));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-border-color: #ecf0f1; -fx-border-width: 0 0 2 0;");

        btnRegresar = new Button("← Volver al Panel");
        btnRegresar.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #555; " +
            "-fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;"
        );
        btnRegresar.setOnMouseEntered(e -> btnRegresar.setTextFill(Color.web("#d35400")));
        btnRegresar.setOnMouseExited(e -> btnRegresar.setTextFill(Color.web("#555")));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        lblNumeroPedido = new Label("Pedido #0000");
        lblNumeroPedido.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        topBar.getChildren().addAll(btnRegresar, spacer, lblNumeroPedido);
        return topBar;
    }

    private VBox crearPanelTicket() {
        VBox vBox = new VBox(10);
        Label lblTitulo = new Label("📝 Detalle del Producto");
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #7f8c8d;");

        listaEspecificaciones = new ListView<>();
        listaEspecificaciones.setPrefHeight(400);
        listaEspecificaciones.setStyle(
            "-fx-background-color: white; -fx-control-inner-background: white; " +
            "-fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-padding: 5;"
        );

        // CellFactory para dar formato al texto (Negritas, colores, etc.)
        listaEspecificaciones.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(item);
                    setFont(Font.font("System", 14));
                    setWrapText(true);

                    if (item.startsWith("Base:") || item.startsWith("Salsa:") || 
                        item.startsWith("Queso:") || item.startsWith("Orilla:")) {
                        setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 5;");
                    } else if (item.startsWith("–")) {
                        setStyle("-fx-text-fill: #34495e; -fx-padding: 2 2 2 20;");
                    } else if (item.startsWith("+")) {
                        setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic; -fx-padding: 2 2 2 20;");
                    } else if (item.contains("Cliente:") || item.contains("Hora:")) {
                        setStyle("-fx-text-fill: #2980b9; -fx-font-weight: bold; -fx-padding: 5;");
                    } else {
                        setStyle("-fx-text-fill: #34495e; -fx-padding: 5;");
                    }
                }
            }
        });

        HBox totalBox = new HBox(10);
        totalBox.setAlignment(Pos.CENTER_RIGHT);
        totalBox.setPadding(new Insets(10, 0, 0, 0));
        
        Label lblTotalTexto = new Label("Total a Pagar:");
        lblTotalTexto.setStyle("-fx-font-size: 18px; -fx-text-fill: #7f8c8d;");
        
        lblTotal = new Label("$0.00");
        lblTotal.setStyle("-fx-font-size: 32px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
        
        totalBox.getChildren().addAll(lblTotalTexto, lblTotal);
        vBox.getChildren().addAll(lblTitulo, listaEspecificaciones, totalBox);
        return vBox;
    }

    private VBox crearPanelEstado() {
        VBox vBox = new VBox(25);
        vBox.setAlignment(Pos.TOP_CENTER);
        vBox.setPadding(new Insets(20));
        vBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 15; -fx-border-color: #e0e0e0; -fx-border-radius: 15;");

        Label lblTitulo = new Label("Estado Actual");
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #7f8c8d;");

        StackPane circularContainer = new StackPane();
        indicadorCircular = new Circle(60);
        indicadorCircular.setFill(Color.web("#e74c3c"));
        indicadorCircular.setEffect(new DropShadow(10, Color.rgb(0,0,0,0.2)));

        lblLetraCircular = new Label("R");
        lblLetraCircular.setStyle("-fx-font-size: 40px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        circularContainer.getChildren().addAll(indicadorCircular, lblLetraCircular);

        lblEstadoTexto = new Label("RECIBIDO");
        lblEstadoTexto.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        progressBarEstado = new ProgressBar(0.2);
        progressBarEstado.setPrefWidth(350);
        progressBarEstado.setPrefHeight(20);
        progressBarEstado.setStyle("-fx-accent: #e74c3c; -fx-control-inner-background: #ecf0f1;");

        vBox.getChildren().addAll(lblTitulo, circularContainer, lblEstadoTexto, progressBarEstado);
        return vBox;
    }

    private HBox crearBotoneraInferior() {
        HBox bottomBar = new HBox(20);
        bottomBar.setPadding(new Insets(20, 30, 30, 30));
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setStyle("-fx-border-color: #ecf0f1; -fx-border-width: 2 0 0 0;");

        String styleBase = "-fx-font-weight: bold; -fx-font-size: 15px; -fx-padding: 12 30; -fx-background-radius: 25; -fx-cursor: hand;";

        btnImprimirTicket = new Button("🖨️ Imprimir Ticket");
        btnImprimirTicket.setStyle(styleBase + "-fx-background-color: #95a5a6; -fx-text-fill: white;");

        btnEnviarAHorno = new Button("⚙️ Iniciar Preparación");
        btnEnviarAHorno.setStyle(styleBase + "-fx-background-color: #3498db; -fx-text-fill: white;");
        btnEnviarAHorno.setPrefWidth(250);

        btnCancelarPedido = new Button("✕ Cancelar Pedido");
        btnCancelarPedido.setStyle(styleBase + "-fx-background-color: #e74c3c; -fx-text-fill: white;");

        bottomBar.getChildren().addAll(btnImprimirTicket, btnEnviarAHorno, btnCancelarPedido);
        return bottomBar;
    }

    // ==========================================
    // MÉTODOS PÚBLICOS PARA EL CONTROLADOR
    // ==========================================

    /**
     * ✅ MÉTODO NUEVO: Actualiza el precio total en la pantalla.
     */
    public void actualizarTotal(double total) {
        lblTotal.setText(String.format("$%.2f", total));
    }

    /**
     * ✅ MÉTODO NUEVO: Llena la lista de detalles del pedido.
     */
    public void llenarDetalles(List<String> items) {
        listaEspecificaciones.getItems().clear();
        if (items != null) {
            listaEspecificaciones.getItems().addAll(items);
        }
    }

    /**
     * Actualiza toda la interfaz visual (colores, barra, texto) según el estado
     */
    public void actualizarEstadoVisual(String nuevoEstado) {
        this.estadoActual = nuevoEstado;
        
        String textoEstado = "";
        double progreso = 0.0;
        Color colorCirculo = Color.GRAY;
        String textoBoton = "Iniciar Preparación";
        String letraCirculo = "R";
        String colorBarra = "#3498db"; 
        
        switch (nuevoEstado.toUpperCase()) {
            case "RECIBIDO":
                textoEstado = "RECIBIDO"; progreso = 0.2; colorCirculo = Color.web("#e74c3c");
                colorBarra = "#e74c3c"; textoBoton = "Iniciar Preparación"; letraCirculo = "R";
                break;
            case "PREPARANDO":
                textoEstado = "EN PREPARACIÓN"; progreso = 0.4; colorCirculo = Color.web("#e67e22");
                colorBarra = "#e67e22"; textoBoton = "Enviar a Horno"; letraCirculo = "P";
                break;
            case "HORNEANDO":
                textoEstado = "HORNEANDO"; progreso = 0.6; colorCirculo = Color.web("#f1c40f");
                colorBarra = "#f1c40f"; textoBoton = "Marcar como Listo"; letraCirculo = "H";
                break;
            case "TERMINADO":
                textoEstado = "LISTO PARA ENTREGA"; progreso = 0.8; colorCirculo = Color.web("#2ecc71");
                colorBarra = "#2ecc71"; textoBoton = "Marcar como Entregado"; letraCirculo = "L";
                break;
            case "ENTREGADO":
                textoEstado = "ENTREGADO"; progreso = 1.0; colorCirculo = Color.web("#3498db");
                colorBarra = "#3498db"; textoBoton = "Completado"; letraCirculo = "E";
                break;
            case "CANCELADO":
                textoEstado = "CANCELADO"; progreso = 0.0; colorCirculo = Color.web("#7f8c8d");
                colorBarra = "#7f8c8d"; textoBoton = "Cancelado"; letraCirculo = "C";
                break;
        }
        
        lblEstadoTexto.setText(textoEstado);
        lblEstadoTexto.setTextFill(colorCirculo);

        animarBarraDeProgreso(progreso, colorBarra);
        animarCambioColorCirculo(colorCirculo);
        actualizarLetraCirculo(letraCirculo);
        
        btnEnviarAHorno.setText(textoBoton);
        
        // Estilos dinámicos del botón de acción principal
        if (nuevoEstado.equalsIgnoreCase("ENTREGADO") || nuevoEstado.equalsIgnoreCase("CANCELADO")) {
            btnEnviarAHorno.setDisable(true);
            btnEnviarAHorno.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 25; -fx-padding: 12 30; -fx-font-weight: bold;");
        } else {
            btnEnviarAHorno.setDisable(false);
            // Determinar color del botón según la acción siguiente
            String siguiente = getSiguienteEstado();
            String colorBtn = colorBarra;
             switch (siguiente) {
                case "PREPARANDO": colorBtn = "#e67e22"; break;
                case "HORNEANDO": colorBtn = "#f1c40f"; break;
                case "TERMINADO": colorBtn = "#2ecc71"; break;
                case "ENTREGADO": colorBtn = "#3498db"; break;
            }
            String textColor = siguiente.equals("HORNEANDO") ? "black" : "white";
            btnEnviarAHorno.setStyle("-fx-background-color: " + colorBtn + "; -fx-text-fill: " + textColor + "; -fx-background-radius: 25; -fx-padding: 12 30; -fx-font-weight: bold; -fx-cursor: hand;");
        }
    }

    public String getSiguienteEstado() {
        switch (estadoActual.toUpperCase()) {
            case "RECIBIDO": return "PREPARANDO";
            case "PREPARANDO": return "HORNEANDO";
            case "HORNEANDO": return "TERMINADO";
            case "TERMINADO": return "ENTREGADO";
            default: return estadoActual;
        }
    }

    public void detenerAnimaciones() {
        if (animacionBarra != null) animacionBarra.stop();
        if (animacionCirculoColor != null) animacionCirculoColor.stop();
        if (animacionCircularPulse != null) animacionCircularPulse.stop();
    }

    public String getEstadoActual() { return estadoActual; }
    public Stage getStage() { return stage; }
    public StackPane getRoot() { return root; }
    
    public void mostrar() {
        Scene scene = new Scene(root, 1400, 800);
        stage.setTitle("Sistema de Pizzería - Detalle de Pedido");
        stage.setScene(scene);
        stage.show();
    }

    // ==========================================
    // ANIMACIONES INTERNAS
    // ==========================================

    private void iniciarAnimacionCircular() {
        animacionCircularPulse = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(indicadorCircular.scaleXProperty(), 1.0),
                new KeyValue(indicadorCircular.scaleYProperty(), 1.0)
            ),
            new KeyFrame(Duration.seconds(1.0), 
                new KeyValue(indicadorCircular.scaleXProperty(), 1.1),
                new KeyValue(indicadorCircular.scaleYProperty(), 1.1)
            ),
            new KeyFrame(Duration.seconds(2.0), 
                new KeyValue(indicadorCircular.scaleXProperty(), 1.0),
                new KeyValue(indicadorCircular.scaleYProperty(), 1.0)
            )
        );
        animacionCircularPulse.setCycleCount(Animation.INDEFINITE);
        animacionCircularPulse.play();
    }

    private void animarBarraDeProgreso(double progresoFinal, String colorHex) {
        if (animacionBarra != null) animacionBarra.stop();
        
        animacionBarra = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(progressBarEstado.progressProperty(), progressBarEstado.getProgress())),
            new KeyFrame(Duration.seconds(0.8), new KeyValue(progressBarEstado.progressProperty(), progresoFinal))
        );
        
        progressBarEstado.setStyle("-fx-accent: " + colorHex + "; -fx-control-inner-background: #ecf0f1;");
        animacionBarra.play();
    }

    private void animarCambioColorCirculo(Color nuevoColor) {
        if (animacionCirculoColor != null) animacionCirculoColor.stop();
        Color colorActual = (Color) indicadorCircular.getFill();
        
        animacionCirculoColor = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(indicadorCircular.fillProperty(), colorActual)),
            new KeyFrame(Duration.seconds(0.5), new KeyValue(indicadorCircular.fillProperty(), nuevoColor))
        );
        animacionCirculoColor.play();
    }

    private void actualizarLetraCirculo(String nuevaLetra) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), lblLetraCircular);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            lblLetraCircular.setText(nuevaLetra);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), lblLetraCircular);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }
}