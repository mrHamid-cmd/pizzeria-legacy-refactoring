package com.pizzasystem.vistas;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.geometry.Pos;

public class PanelControlPedidosView {
    private Stage stage;
    private BorderPane root;

    // Componentes - Área Superior
    public Label lblTitulo;
    public Button btnNuevoPedido;
    public Button btnActualizar;
    public Button btnLogout;


    // Componentes - Área Central (Columnas Kanban)
    public ListView<String> listaRecibido;
    public ListView<String> listaPreparando;
    public ListView<String> listaHorneando;
    public ListView<String> listaTerminado;
    public ListView<String> listaEntregado;

    // Labels para contadores
    private Label lblContadorRecibido;
    private Label lblContadorPreparando;
    private Label lblContadorHorneando;
    private Label lblContadorTerminado;
    private Label lblContadorEntregado;

    // ✅ IMPORTANTES: accesibles para Controller (status bar)
    public Label lblStatus;
    public Label lblTotalPedidos;

    public PanelControlPedidosView(Stage primaryStage) {
        this.stage = primaryStage;
        crearVista();
        cargarDatosPrueba(); // si ya no quieres prueba, lo puedes quitar después
    }

    private void crearVista() {
        root = new BorderPane();

        // -------------------------------------------------------------
        // 1. FONDO DE IMAGEN (/imagenes/fondo4.png)
        // -------------------------------------------------------------
        try {
            String imagePath = getClass().getResource("/imagenes/fondo4.png").toExternalForm();
            root.setStyle(
                "-fx-background-image: url('" + imagePath + "'); " +
                "-fx-background-size: 100% 100%; " +
                "-fx-background-repeat: no-repeat; " +
                "-fx-background-position: center center;"
            );
        } catch (Exception e) {
            System.out.println("⚠️ No se pudo cargar /imagenes/fondo4.png. Usando color sólido.");
            root.setStyle("-fx-background-color: #2c3e50;");
        }

        // ========== ÁREA SUPERIOR ==========
        HBox topBar = new HBox(15);
        topBar.setStyle("-fx-background-color: rgba(44, 62, 80, 0.75); -fx-padding: 15 25;");
        topBar.setAlignment(Pos.CENTER_LEFT);

        lblTitulo = new Label("Tablero de Pizzería");
        lblTitulo.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnNuevoPedido = new Button("Nuevo Pedido");
        btnNuevoPedido.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;");
        btnNuevoPedido.setGraphic(crearIcono("➕"));

        btnActualizar = new Button("Actualizar");
        btnActualizar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;");
        btnActualizar.setGraphic(crearIcono("🔄"));

        btnLogout = new Button("Cerrar Sesión");
        btnLogout.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;");
        btnLogout.setGraphic(crearIcono("🚪"));

        // Orden: título -> combo -> spacer -> botones
       topBar.getChildren().addAll(lblTitulo, spacer, btnNuevoPedido, btnActualizar, btnLogout);

        root.setTop(topBar);

        // ========== ÁREA CENTRAL (KANBAN) ==========
        HBox kanbanBoard = new HBox(15);
        kanbanBoard.setPadding(new Insets(20));
        kanbanBoard.setStyle("-fx-background-color: rgba(236, 240, 241, 0.8);");

        HBox.setHgrow(kanbanBoard, Priority.ALWAYS);

        VBox columnaRecibido   = crearColumnaKanban("Recibido",   "Nuevos Pedidos",        "#e74c3c");
        VBox columnaPreparando = crearColumnaKanban("Preparando", "En Preparación",       "#f39c12");
        VBox columnaHorneando  = crearColumnaKanban("Horneando",  "En Horno",             "#f1c40f");
        VBox columnaTerminado  = crearColumnaKanban("Terminado",  "Listo para Entregar",  "#2ecc71");
        VBox columnaEntregado  = crearColumnaKanban("Entregado",  "Pedidos Entregados",   "#3498db");

        for (VBox columna : new VBox[]{columnaRecibido, columnaPreparando, columnaHorneando, columnaTerminado, columnaEntregado}) {
            VBox.setVgrow(columna, Priority.ALWAYS);
            columna.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(columna, Priority.ALWAYS);
        }

        kanbanBoard.getChildren().addAll(columnaRecibido, columnaPreparando, columnaHorneando, columnaTerminado, columnaEntregado);
        root.setCenter(kanbanBoard);

        // ========== ÁREA INFERIOR ==========
        HBox statusBar = new HBox(10);
        statusBar.setStyle("-fx-background-color: rgba(52, 73, 94, 0.9); -fx-padding: 10 25;");

        // ✅ ahora son fields (para que Controller pueda actualizarlos)
        lblStatus = new Label("Sistema activo - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        lblStatus.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 12px;");

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        lblTotalPedidos = new Label("Pedidos activos: 0");
        lblTotalPedidos.setStyle("-fx-text-fill: #ecf0f1; -fx-font-size: 12px;");

        statusBar.getChildren().addAll(lblStatus, spacer2, lblTotalPedidos);
        root.setBottom(statusBar);
    }

    private VBox crearColumnaKanban(String id, String titulo, String color) {
        VBox columna = new VBox(10);
        columna.setPadding(new Insets(15));

        columna.setStyle("-fx-background-color: rgba(255, 255, 255, 0.6); " +
                "-fx-border-radius: 8; -fx-background-radius: 8; " +
                "-fx-border-color: #bdc3c7; -fx-border-width: 1;");

        columna.setPrefWidth(250);
        columna.setMinWidth(200);
        columna.setMaxWidth(Double.MAX_VALUE);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label colorIndicator = new Label("●");
        colorIndicator.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 20px;");

        Label lblTituloColumna = new Label(titulo);
        lblTituloColumna.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label lblContador = new Label("(0)");
        lblContador.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");

        switch (id) {
            case "Recibido":   lblContadorRecibido = lblContador; break;
            case "Preparando": lblContadorPreparando = lblContador; break;
            case "Horneando":  lblContadorHorneando = lblContador; break;
            case "Terminado":  lblContadorTerminado = lblContador; break;
            case "Entregado":  lblContadorEntregado = lblContador; break;
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(colorIndicator, lblTituloColumna, spacer, lblContador);

        ListView<String> listaPedidos = new ListView<>();
        listaPedidos.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent; -fx-padding: 0;");
        listaPedidos.setPrefHeight(500);

        switch (id) {
            case "Recibido":   listaRecibido = listaPedidos; break;
            case "Preparando": listaPreparando = listaPedidos; break;
            case "Horneando":  listaHorneando = listaPedidos; break;
            case "Terminado":  listaTerminado = listaPedidos; break;
            case "Entregado":  listaEntregado = listaPedidos; break;
        }

        VBox.setVgrow(listaPedidos, Priority.ALWAYS);

        listaPedidos.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(item);
                    setFont(Font.font("System", 12));
                    setWrapText(true);

                    String baseStyle = "-fx-border-color: #bdc3c7; -fx-border-width: 0 0 1 0; -fx-padding: 10; -fx-background-radius: 5; ";

                    if (item.toUpperCase().contains("CANCELADO")) {
                        setStyle(baseStyle + "-fx-background-color: #fadbd8; -fx-text-fill: red; -fx-font-weight: bold;");
                    } else if (item.contains("URGENTE") || item.contains("Espera:")) {
                        setStyle(baseStyle + "-fx-background-color: #fff3cd; -fx-text-fill: black;");
                    } else {
                        setStyle(baseStyle + "-fx-background-color: rgba(255, 255, 255, 0.8); -fx-text-fill: black;");
                    }

                    setOnMouseEntered(e -> setStyle(baseStyle + "-fx-background-color: #e3f2fd; -fx-cursor: hand; -fx-text-fill: black;"));
                    setOnMouseExited(e -> {
                        if (getItem() != null) {
                            if (getItem().toUpperCase().contains("CANCELADO")) {
                                setStyle(baseStyle + "-fx-background-color: #fadbd8; -fx-text-fill: red; -fx-font-weight: bold;");
                            } else if (getItem().contains("URGENTE") || getItem().contains("Espera:")) {
                                setStyle(baseStyle + "-fx-background-color: #fff3cd; -fx-text-fill: black;");
                            } else {
                                setStyle(baseStyle + "-fx-background-color: rgba(255, 255, 255, 0.8); -fx-text-fill: black;");
                            }
                        }
                    });
                }
            }
        });

        columna.getChildren().addAll(header, listaPedidos);
        return columna;
    }

    private Label crearIcono(String texto) {
        Label icono = new Label(texto);
        icono.setStyle("-fx-font-size: 14px; -fx-padding: 0 5 0 0;");
        return icono;
    }

    private void cargarDatosPrueba() {
        // Si ya estás en modo real 100% puedes borrar esto luego.
        refrescarContadores();
    }

    // ✅ público: el Controller puede llamarlo después de cargar listas
    public void refrescarContadores() {
        if (lblContadorRecibido != null && listaRecibido != null) {
            lblContadorRecibido.setText("(" + listaRecibido.getItems().size() + ")");
        }
        if (lblContadorPreparando != null && listaPreparando != null) {
            lblContadorPreparando.setText("(" + listaPreparando.getItems().size() + ")");
        }
        if (lblContadorHorneando != null && listaHorneando != null) {
            lblContadorHorneando.setText("(" + listaHorneando.getItems().size() + ")");
        }
        if (lblContadorTerminado != null && listaTerminado != null) {
            lblContadorTerminado.setText("(" + listaTerminado.getItems().size() + ")");
        }
        if (lblContadorEntregado != null && listaEntregado != null) {
            lblContadorEntregado.setText("(" + listaEntregado.getItems().size() + ")");
        }
    }

    public void mostrar() {
        Scene scene = new Scene(root, 1400, 800);
        if (getClass().getResource("/styles/estilo.css") != null) {
            scene.getStylesheets().add(getClass().getResource("/styles/estilo.css").toExternalForm());
        }
        stage.setTitle("Sistema de Pizzería - Panel de Control");
        stage.setScene(scene);
        stage.show();
    }

    public BorderPane getRoot() { return root; }
    public Stage getStage() { return stage; }
}
