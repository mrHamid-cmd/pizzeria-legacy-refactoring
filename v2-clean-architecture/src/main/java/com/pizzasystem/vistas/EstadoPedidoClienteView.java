package com.pizzasystem.vistas;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.InputStream;

/**
 * Vista para el cliente que muestra el estado actual de su pedido.
 * NOTA MVC: Esta clase SOLO pinta y expone eventos; no consulta lógica de negocio.
 */
public class EstadoPedidoClienteView {

    private Stage stage;
    private StackPane root; // Fondo + tarjeta

    // Componentes UI
    public Label lblNumeroPedido;
    public Label lblMensajeEstado;
    public Button btnVolver;
    public Button btnActualizar;

    // Elementos gráficos
    private Pane[] etapasCirculo;
    private Label[] etiquetasEtapa;
    private Pane[] conectores;

    private Line lineaProgresoFondo;
    private Line lineaProgresoActiva;

    private ImageView[] iconosEtapa;

    private static final String IMAGENES_PATH = "/imagenes/";

    // Paleta
    private final Color COLOR_COMPLETADO = Color.web("#4CAF50"); // Verde
    private final Color COLOR_ACTIVO = Color.web("#FF9800");     // Naranja
    private final Color COLOR_INACTIVO = Color.web("#B0BEC5");   // Gris

    // Datos del proceso
    private final String[] NOMBRES_ETAPAS = {"RECIBIDO", "PREPARANDO", "HORNEANDO", "TERMINADO", "ENTREGADO"};
    private final String[] MENSAJES_ETAPAS = {
            "¡Pedido recibido! Hemos confirmado tu orden.",
            "¡Manos a la obra! Estamos preparando tu pizza con ingredientes frescos.",
            "¡Al calor! Tu pizza se está dorando perfectamente en el horno.",
            "¡Listo! Tu pizza está terminada y esperando para ser entregada.",
            "¡Entregado! Disfruta de tu deliciosa pizza. ¡Gracias por elegirnos!"
    };

    // ✅ Para no acumular animaciones
    private ScaleTransition animPulsoActual;
    private Timeline animHorneando;
    private Timeline animLinea;

    public EstadoPedidoClienteView(Stage primaryStage) {
        this.stage = primaryStage;
        crearVista();
    }

    private void crearVista() {
        root = new StackPane();

        // Fondo
        try {
            String imagePath = getClass().getResource("/imagenes/fondo5.png").toExternalForm();
            root.setStyle(
                    "-fx-background-image: url('" + imagePath + "'); " +
                            "-fx-background-size: 100% 100%; " +
                            "-fx-background-repeat: no-repeat; " +
                            "-fx-background-position: center center;"
            );
        } catch (Exception e) {
            System.out.println("⚠️ No se pudo cargar /imagenes/fondo5.png");
            root.setStyle("-fx-background-color: #ecf0f1;");
        }

        VBox mainContainer = new VBox(0);
        mainContainer.setMaxWidth(1000);
        mainContainer.setMaxHeight(650);
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.85); " +
                        "-fx-background-radius: 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 30, 0, 0, 10);"
        );

        Pane header = crearEncabezado();
        Pane trackingArea = crearAreaSeguimiento();
        Pane messageArea = crearAreaMensaje();

        mainContainer.getChildren().addAll(header, trackingArea, messageArea);
        root.getChildren().add(mainContainer);
    }

    private Pane crearEncabezado() {
        Pane header = new Pane();
        header.setPrefHeight(120);

        Stop[] stops = new Stop[]{
                new Stop(0, Color.web("#D32F2F")),
                new Stop(1, Color.web("#B71C1C"))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);
        header.setBackground(new Background(new BackgroundFill(
                gradient, new CornerRadii(20, 20, 0, 0, false), Insets.EMPTY)));

        btnVolver = new Button("← Volver");
        btnVolver.setLayoutX(30);
        btnVolver.setLayoutY(40);
        btnVolver.setStyle("-fx-background-color: rgba(255,255,255,0.2); " +
                "-fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-font-size: 14px; -fx-padding: 8 20; " +
                "-fx-background-radius: 20; -fx-cursor: hand;");

        btnActualizar = new Button("🔄 Actualizar");
        btnActualizar.setLayoutX(850);
        btnActualizar.setLayoutY(40);
        btnActualizar.setStyle("-fx-background-color: rgba(255,255,255,0.2); " +
                "-fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-font-size: 14px; -fx-padding: 8 20; " +
                "-fx-background-radius: 20; -fx-cursor: hand;");

        Label titulo = new Label("Seguimiento de tu Pizza");
        titulo.setLayoutX(350);
        titulo.setLayoutY(30);
        titulo.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold; " +
                "-fx-effect: dropshadow(one-pass-box, rgba(0,0,0,0.3), 2, 0, 0, 1);");

        lblNumeroPedido = new Label("Pedido #----");
        lblNumeroPedido.setLayoutX(420);
        lblNumeroPedido.setLayoutY(70);
        lblNumeroPedido.setStyle("-fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 18px;");

        header.getChildren().addAll(btnVolver, btnActualizar, titulo, lblNumeroPedido);
        return header;
    }

    private Pane crearAreaSeguimiento() {
        Pane trackingPane = new Pane();
        trackingPane.setPrefHeight(300);
        trackingPane.setStyle("-fx-background-color: transparent;");

        Label tituloSeguimiento = new Label("Estado de tu Pedido");
        tituloSeguimiento.setLayoutX(50);
        tituloSeguimiento.setLayoutY(30);
        tituloSeguimiento.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        double startX = 100;
        double lineY = 150;
        double spacing = 180;
        double endXLine = startX + (spacing * 4);

        lineaProgresoFondo = new Line(startX, lineY, endXLine, lineY);
        lineaProgresoFondo.setStroke(COLOR_INACTIVO);
        lineaProgresoFondo.setStrokeWidth(6);
        lineaProgresoFondo.setStrokeLineCap(StrokeLineCap.ROUND);

        lineaProgresoActiva = new Line(startX, lineY, startX, lineY);
        lineaProgresoActiva.setStroke(COLOR_COMPLETADO);
        lineaProgresoActiva.setStrokeWidth(6);
        lineaProgresoActiva.setStrokeLineCap(StrokeLineCap.ROUND);

        etapasCirculo = new Pane[5];
        etiquetasEtapa = new Label[5];
        iconosEtapa = new ImageView[5];
        conectores = new Pane[4];

        for (int i = 0; i < 5; i++) {
            double x = startX + (i * spacing);

            Circle circle = new Circle(x, lineY, 35);
            circle.setFill(Color.WHITE);
            circle.setStroke(COLOR_INACTIVO);
            circle.setStrokeWidth(4);
            circle.setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.2)));

            ImageView icon = new ImageView();
            icon.setFitWidth(35);
            icon.setFitHeight(35);
            icon.setLayoutX(x - 17.5);
            icon.setLayoutY(lineY - 17.5);

            Label label = new Label(NOMBRES_ETAPAS[i]);
            label.setLayoutX(x - 40);
            label.setLayoutY(lineY + 45);
            label.setPrefWidth(80);
            label.setAlignment(Pos.CENTER);
            label.setStyle("-fx-font-weight: bold; -fx-text-fill: #7f8c8d; -fx-font-size: 11px;");

            Pane etapaPane = new Pane();
            etapaPane.getChildren().addAll(circle, icon, label);

            etapasCirculo[i] = etapaPane;
            etiquetasEtapa[i] = label;
            iconosEtapa[i] = icon;

            cargarIcono(i);

            if (i < 4) {
                Pane conector = crearConector(x + 35, lineY, x + spacing - 35, lineY);
                conectores[i] = conector;
            }
        }

        trackingPane.getChildren().addAll(tituloSeguimiento, lineaProgresoFondo, lineaProgresoActiva);

        for (Pane conector : conectores) {
            if (conector != null) trackingPane.getChildren().add(conector);
        }
        for (Pane etapa : etapasCirculo) {
            trackingPane.getChildren().add(etapa);
        }

        return trackingPane;
    }

    private void cargarIcono(int index) {
        String[] iconFiles = {"recibido.png", "preparando.png", "horneando.png", "terminado.png", "entregado.png"};

        try {
            InputStream is = getClass().getResourceAsStream(IMAGENES_PATH + iconFiles[index]);
            if (is != null) {
                iconosEtapa[index].setImage(new Image(is));
            } else {
                throw new Exception("Imagen no encontrada");
            }
        } catch (Exception e) {
            String[] emojis = {"📝", "👨‍🍳", "🔥", "🍕", "🛵"};
            Label fallback = new Label(emojis[index]);
            fallback.setStyle("-fx-font-size: 20px;");
            Pane etapa = etapasCirculo[index];

            Circle c = (Circle) etapa.getChildren().get(0);
            fallback.layoutXProperty().bind(c.centerXProperty().subtract(12));
            fallback.layoutYProperty().bind(c.centerYProperty().subtract(15));

            etapa.getChildren().add(fallback);
        }
    }

    private Pane crearConector(double startX, double startY, double endX, double endY) {
        Pane conector = new Pane();
        Line line = new Line(startX, startY, endX, endY);
        line.setStroke(Color.TRANSPARENT);
        conector.getChildren().add(line);
        return conector;
    }

    private Pane crearAreaMensaje() {
        Pane messagePane = new Pane();
        messagePane.setPrefHeight(230);
        messagePane.setStyle("-fx-background-color: rgba(255, 243, 224, 0.6); -fx-background-radius: 0 0 20 20;");

        Pane messageBox = new Pane();
        messageBox.setLayoutX(150);
        messageBox.setLayoutY(30);
        messageBox.setPrefSize(700, 120);
        messageBox.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.7); " +
                        "-fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3); " +
                        "-fx-border-color: #FF9800; -fx-border-width: 0 0 0 5;"
        );

        Label icono = new Label("💬");
        icono.setLayoutX(30);
        icono.setLayoutY(40);
        icono.setStyle("-fx-font-size: 28px;");

        lblMensajeEstado = new Label("Consultando estado...");
        lblMensajeEstado.setLayoutX(80);
        lblMensajeEstado.setLayoutY(25);
        lblMensajeEstado.setPrefWidth(580);
        lblMensajeEstado.setWrapText(true);
        lblMensajeEstado.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e65100;");

        Label subtitulo = new Label("Actualizaremos el estado en tiempo real.");
        subtitulo.setLayoutX(80);
        subtitulo.setLayoutY(65);
        subtitulo.setPrefWidth(580);
        subtitulo.setWrapText(true);
        subtitulo.setStyle("-fx-font-size: 14px; -fx-text-fill: #5d4037;");

        messageBox.getChildren().addAll(icono, lblMensajeEstado, subtitulo);
        messagePane.getChildren().add(messageBox);

        return messagePane;
    }

    // =================== API MVC (Controller usa esto) ===================

    /** Hook para el controller */
    public void setOnVolver(Runnable action) {
        btnVolver.setOnAction(e -> { if (action != null) action.run(); });
    }

    /** Hook para el controller */
    public void setOnActualizar(Runnable action) {
        btnActualizar.setOnAction(e -> { if (action != null) action.run(); });
    }

    /** El controller manda el número y estado, y la vista solo pinta */
    public void mostrarPedido(String numeroPedido, String estado) {
        lblNumeroPedido.setText("Pedido #" + (numeroPedido == null ? "----" : numeroPedido));
        actualizarEstadoVisual(estado);
    }

    // ================= LÓGICA VISUAL (pintado) =================

    public void actualizarEstadoVisual(String estado) {
        detenerAnimaciones();
        resetearEtapas();

        int estadoIndex = -1;
        for (int i = 0; i < NOMBRES_ETAPAS.length; i++) {
            if (NOMBRES_ETAPAS[i].equalsIgnoreCase(estado)) {
                estadoIndex = i;
                break;
            }
        }
        if (estadoIndex == -1) return;

        double porcentaje = (double) estadoIndex / (NOMBRES_ETAPAS.length - 1);
        actualizarLineaProgreso(porcentaje);

        for (int i = 0; i <= estadoIndex; i++) {
            if (i < estadoIndex) {
                marcarEtapaComoCompletada(i);
            } else {
                marcarEtapaComoActiva(i);
                lblMensajeEstado.setText(MENSAJES_ETAPAS[i]);
                if (i == 2) animarEtapaHorneando();
            }
        }
    }

    private void resetearEtapas() {
        for (int i = 0; i < etapasCirculo.length; i++) {
            Pane etapa = etapasCirculo[i];
            Circle circle = (Circle) etapa.getChildren().get(0);
            circle.setStroke(COLOR_INACTIVO);
            circle.setFill(Color.WHITE);
            circle.setEffect(null);
            circle.setScaleX(1.0);
            circle.setScaleY(1.0);
            etiquetasEtapa[i].setStyle("-fx-font-weight: bold; -fx-text-fill: #95a5a6; -fx-font-size: 11px;");
        }
        lineaProgresoActiva.setEndX(lineaProgresoActiva.getStartX());
    }

    private void marcarEtapaComoCompletada(int index) {
        Pane etapa = etapasCirculo[index];
        Circle circle = (Circle) etapa.getChildren().get(0);

        circle.setStroke(COLOR_COMPLETADO);
        circle.setFill(COLOR_COMPLETADO);
        etiquetasEtapa[index].setStyle("-fx-font-weight: bold; -fx-text-fill: " + toHex(COLOR_COMPLETADO) + "; -fx-font-size: 12px;");
        circle.setEffect(new DropShadow(10, COLOR_COMPLETADO));
    }

    private void marcarEtapaComoActiva(int index) {
        Pane etapa = etapasCirculo[index];
        Circle circle = (Circle) etapa.getChildren().get(0);

        circle.setStroke(COLOR_ACTIVO);
        circle.setFill(Color.WHITE);
        circle.setStrokeWidth(4);
        etiquetasEtapa[index].setStyle("-fx-font-weight: bold; -fx-text-fill: " + toHex(COLOR_ACTIVO) + "; -fx-font-size: 13px;");

        animPulsoActual = new ScaleTransition(Duration.millis(800), circle);
        animPulsoActual.setFromX(1.0);
        animPulsoActual.setFromY(1.0);
        animPulsoActual.setToX(1.15);
        animPulsoActual.setToY(1.15);
        animPulsoActual.setCycleCount(Animation.INDEFINITE);
        animPulsoActual.setAutoReverse(true);
        animPulsoActual.play();
    }

    private void animarEtapaHorneando() {
        Pane etapa = etapasCirculo[2];
        Circle circle = (Circle) etapa.getChildren().get(0);

        animHorneando = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(circle.strokeProperty(), COLOR_ACTIVO)),
                new KeyFrame(Duration.seconds(0.5), new KeyValue(circle.strokeProperty(), Color.RED)),
                new KeyFrame(Duration.seconds(1.0), new KeyValue(circle.strokeProperty(), COLOR_ACTIVO))
        );
        animHorneando.setCycleCount(Animation.INDEFINITE);
        animHorneando.play();
    }

    private void actualizarLineaProgreso(double porcentaje) {
        double startX = lineaProgresoActiva.getStartX();
        double totalWidth = lineaProgresoFondo.getEndX() - startX;
        double targetEnd = startX + (totalWidth * porcentaje);

        if (animLinea != null) animLinea.stop();

        animLinea = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(lineaProgresoActiva.endXProperty(), lineaProgresoActiva.getEndX())),
                new KeyFrame(Duration.seconds(0.8), new KeyValue(lineaProgresoActiva.endXProperty(), targetEnd))
        );
        animLinea.play();
    }

    private void detenerAnimaciones() {
        if (animPulsoActual != null) animPulsoActual.stop();
        animPulsoActual = null;

        if (animHorneando != null) animHorneando.stop();
        animHorneando = null;

        if (animLinea != null) animLinea.stop();
        animLinea = null;
    }

    private String toHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    public void mostrar() {
        Scene scene = new Scene(root, 1400, 800);
        stage.setTitle("Sistema de Pizzería - Seguimiento de Pedido");
        stage.setScene(scene);
        stage.show();
    }

    public StackPane getRoot() { return root; }
    public Stage getStage() { return stage; }

    /** Llamar cuando cierres la ventana si quieres limpiar recursos */
    public void limpiar() {
        detenerAnimaciones();
    }
}
