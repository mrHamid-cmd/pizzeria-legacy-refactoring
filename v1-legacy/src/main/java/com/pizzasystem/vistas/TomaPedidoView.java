package com.pizzasystem.vistas;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Clase que gestiona la vista de Toma de Pedidos (Punto de Venta).
 * Vista "tonta": NO calcula precio. Solo muestra y captura selección.
 */
public class TomaPedidoView {
    private Stage stage;
    private BorderPane root;

    // ✅ Strategy selector (en NUEVO PEDIDO)
    public ComboBox<String> cmbEstrategiaPrecio;

    // --- Componentes UI ---

    // Área Izquierda: Selección de base
    public ComboBox<String> cmbMasa;
    public ComboBox<String> cmbSalsa;
    public ComboBox<String> cmbQueso;
    public ComboBox<String> cmbTipoOrilla;

    // Área Central: Listas de añadidos
    public ListView<CheckBox> lstIngredientes;
    public ListView<CheckBox> lstCondimentos;

    // Área Inferior: Totales y Botones
    public Label lblTotal;
    public Button btnConfirmar;
    public Button btnGestionar;

    // Datos
    private ObservableList<CheckBox> ingredientesList;
    private ObservableList<CheckBox> condimentosList;

    // ✅ Hook para que el Controller recalculé total cuando cambie algo en la vista
    private Runnable onCambioSeleccion;

    // ✅ Hook para que el Controller recalculé al cambiar estrategia
    private Runnable onCambioEstrategia;

    /**
     * Constructor de la vista.
     * @param primaryStage El escenario principal de la aplicación.
     */
    public TomaPedidoView(Stage primaryStage) {
        this.stage = primaryStage;
        crearVista();
        cargarDatosPrueba();
    }

    /**
     * Configura la estructura visual principal.
     * 1. Carga la imagen de fondo (fondo3.png).
     * 2. Crea una "Tarjeta Central" con transparencia.
     * 3. Organiza los sub-paneles dentro de la tarjeta.
     */
    private void crearVista() {
        root = new BorderPane();

        // --- 1. CONFIGURACIÓN DEL FONDO ---
        try {
            String imagePath = getClass().getResource("/imagenes/fondo3.png").toExternalForm();
            root.setStyle(
                    "-fx-background-image: url('" + imagePath + "'); " +
                            "-fx-background-size: 100% 100%; " +
                            "-fx-background-repeat: no-repeat; " +
                            "-fx-background-position: center center;"
            );
        } catch (Exception e) {
            System.out.println("⚠️ No se pudo cargar /imagenes/fondo3.png");
            root.setStyle("-fx-background-color: #ecf0f1;");
        }

        // --- 2. TARJETA PRINCIPAL (CONTENEDOR FLOTANTE) ---
        VBox mainCard = new VBox(0);

        mainCard.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.85); " +
                        "-fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 20, 0, 0, 10);"
        );

        mainCard.setMaxWidth(820);
        mainCard.setMaxHeight(700);
        mainCard.setAlignment(Pos.TOP_CENTER);

        // --- 3. HEADER (Título + Strategy Combo) ---
        HBox header = new HBox(15);
        header.setStyle("-fx-background-color: rgba(44, 62, 80, 0.95); -fx-padding: 15 20; -fx-background-radius: 10 10 0 0;");
        header.setAlignment(Pos.CENTER_LEFT);

        Label titulo = new Label("Sistema de Punto de Venta - Crear Pizza");
        titulo.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        Region spacerHeader = new Region();
        HBox.setHgrow(spacerHeader, Priority.ALWAYS);

        Label lblEstrategia = new Label("Precio:");
        lblEstrategia.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        cmbEstrategiaPrecio = new ComboBox<>();
        cmbEstrategiaPrecio.setPrefWidth(160);
        cmbEstrategiaPrecio.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-font-weight: bold;");

        header.getChildren().addAll(titulo, spacerHeader, lblEstrategia, cmbEstrategiaPrecio);

        // --- 4. CONTENIDO INTERNO ---
        VBox contentContainer = new VBox(20);
        contentContainer.setPadding(new Insets(20));

        HBox topRow = new HBox(20);
        topRow.setPrefHeight(500);
        topRow.setAlignment(Pos.CENTER);

        VBox leftPanel = crearPanelBasePizza();
        leftPanel.setPrefWidth(300);

        VBox centerPanel = crearPanelAñadidos();
        centerPanel.setPrefWidth(400);

        topRow.getChildren().addAll(leftPanel, centerPanel);

        HBox bottomPanel = crearPanelResumenAcciones();

        contentContainer.getChildren().addAll(topRow, bottomPanel);

        mainCard.getChildren().addAll(header, contentContainer);

        root.setCenter(mainCard);
    }

    /**
     * Crea el panel izquierdo para seleccionar la base de la pizza.
     */
    private VBox crearPanelBasePizza() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));

        panel.setStyle("-fx-background-color: rgba(255, 255, 255, 0.5); " +
                "-fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-border-width: 1;");

        Label tituloSeccion = new Label("Base de Pizza");
        tituloSeccion.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        VBox masaBox = crearSelector("Tipo de Masa:", cmbMasa = new ComboBox<>());
        VBox salsaBox = crearSelector("Salsa Base:", cmbSalsa = new ComboBox<>());
        VBox quesoBox = crearSelector("Queso Principal:", cmbQueso = new ComboBox<>());
        VBox orillaBox = crearSelector("Tipo de Orilla:", cmbTipoOrilla = new ComboBox<>());

        panel.getChildren().addAll(tituloSeccion, masaBox, salsaBox, quesoBox, orillaBox);
        return panel;
    }

    /**
     * Método auxiliar para crear un bloque de Label + ComboBox.
     */
    private VBox crearSelector(String labelText, ComboBox<String> combo) {
        VBox box = new VBox(5);
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");
        combo.setMaxWidth(Double.MAX_VALUE);
        box.getChildren().addAll(lbl, combo);
        return box;
    }

    /**
     * Crea el panel central para ingredientes y condimentos.
     */
    private VBox crearPanelAñadidos() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));

        panel.setStyle("-fx-background-color: rgba(255, 255, 255, 0.5); " +
                "-fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-border-width: 1;");

        Label tituloSeccion = new Label("Ingredientes y Extras");
        tituloSeccion.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        VBox ingredientesBox = new VBox(5);
        Label lblIngredientes = new Label("Ingredientes Adicionales:");
        lblIngredientes.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");

        lstIngredientes = new ListView<>();
        lstIngredientes.setPrefHeight(200);
        lstIngredientes.setStyle("-fx-background-color: transparent; -fx-control-inner-background: rgba(255,255,255,0.4);");

        ingredientesBox.getChildren().addAll(lblIngredientes, lstIngredientes);

        Separator separator = new Separator();

        VBox condimentosBox = new VBox(5);
        Label lblCondimentos = new Label("Condimentos y Especias:");
        lblCondimentos.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");

        lstCondimentos = new ListView<>();
        lstCondimentos.setPrefHeight(150);
        lstCondimentos.setStyle("-fx-background-color: transparent; -fx-control-inner-background: rgba(255,255,255,0.4);");

        condimentosBox.getChildren().addAll(lblCondimentos, lstCondimentos);

        panel.getChildren().addAll(tituloSeccion, ingredientesBox, separator, condimentosBox);
        return panel;
    }

    /**
     * Crea el panel inferior con el resumen de precio y botones de acción.
     */
    private HBox crearPanelResumenAcciones() {
        HBox panel = new HBox(20);
        panel.setPadding(new Insets(20));

        panel.setStyle("-fx-background-color: rgba(255, 255, 255, 0.6); " +
                "-fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-border-width: 1;");

        panel.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox totalBox = new VBox(5);
        totalBox.setAlignment(Pos.CENTER_RIGHT);

        Label lblTotalTexto = new Label("Total a Pagar:");
        lblTotalTexto.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");

        lblTotal = new Label("$0.00");
        lblTotal.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        totalBox.getChildren().addAll(lblTotalTexto, lblTotal);

        HBox botonesBox = new HBox(15);
        botonesBox.setAlignment(Pos.CENTER);

        btnGestionar = new Button("Gestionar Pedidos");
        btnGestionar.setStyle("-fx-font-size: 14px; -fx-pref-width: 150; -fx-pref-height: 40; " +
                "-fx-background-color: rgba(149, 165, 166, 0.9); -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        btnConfirmar = new Button("Confirmar Pedido");
        btnConfirmar.setStyle("-fx-font-size: 14px; -fx-pref-width: 150; -fx-pref-height: 40; " +
                "-fx-background-color: rgba(39, 174, 96, 0.9); -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnConfirmar.setDefaultButton(true);

        botonesBox.getChildren().addAll(btnGestionar, btnConfirmar);

        panel.getChildren().addAll(botonesBox, spacer, totalBox);
        return panel;
    }

    /**
     * Carga los datos iniciales en los ComboBoxes y Listas.
     */
    private void cargarDatosPrueba() {
        // ✅ Estrategia precio
        cmbEstrategiaPrecio.setItems(FXCollections.observableArrayList("Estándar", "Promoción"));
        cmbEstrategiaPrecio.getSelectionModel().selectFirst();

        ObservableList<String> masas = FXCollections.observableArrayList(
                "Masa Tradicional", "Masa Delgada", "Masa Integral", "Sin Gluten"
        );
        cmbMasa.setItems(masas);
        cmbMasa.getSelectionModel().selectFirst();

        ObservableList<String> salsas = FXCollections.observableArrayList(
                "Salsa de Tomate Clásica", "Salsa Blanca/Bechamel", "Salsa BBQ", "Sin Salsa"
        );
        cmbSalsa.setItems(salsas);
        cmbSalsa.getSelectionModel().selectFirst();

        ObservableList<String> quesos = FXCollections.observableArrayList(
                "Mozzarella", "Queso Vegano", "Doble Queso", "Sin Queso"
        );
        cmbQueso.setItems(quesos);
        cmbQueso.getSelectionModel().selectFirst();

        ObservableList<String> orillas = FXCollections.observableArrayList(
                "Orilla Tradicional", "Orilla Rellena de Queso", "Orilla con Ajo y Parmesano"
        );
        cmbTipoOrilla.setItems(orillas);
        cmbTipoOrilla.getSelectionModel().selectFirst();

        // Ingredientes
        ingredientesList = FXCollections.observableArrayList();
        String[] ingredientes = {
            "Pepperoni (+$15)", "Champiñones (+$15)", "Pimientos (+$15)",
            "Cebolla (+$15)", "Aceitunas (+$15)", "Jamón (+$15)",
            "Piña (+$15)", "Tocino (+$15)", "Salchicha (+$15)",
            "Extra Queso (+$15)"
        };



        for (String ingrediente : ingredientes) {
            CheckBox cb = new CheckBox(ingrediente);
            cb.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50;");
            ingredientesList.add(cb);
        }
        lstIngredientes.setItems(ingredientesList);

        // Condimentos
        condimentosList = FXCollections.observableArrayList();
        String[] condimentos = {
                "Orégano", "Hojuelas de Chile", "Parmesano en Polvo",
                "Aceite de Oliva Extra", "Sal", "Pimienta Negra",
                "Albahaca Fresca", "Ajo en Polvo"
        };

        for (String condimento : condimentos) {
            CheckBox cb = new CheckBox(condimento);
            cb.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50;");
            condimentosList.add(cb);
        }
        lstCondimentos.setItems(condimentosList);

        configurarListeners();

        // ✅ La vista NO calcula; mostramos un valor inicial “neutral”
        actualizarTotal(0.00);
    }

    /**
     * Configura listeners para avisar al Controller que cambió algo.
     */
    private void configurarListeners() {
        cmbMasa.valueProperty().addListener((obs, oldVal, newVal) -> dispararCambioSeleccion());
        cmbSalsa.valueProperty().addListener((obs, oldVal, newVal) -> dispararCambioSeleccion());
        cmbQueso.valueProperty().addListener((obs, oldVal, newVal) -> dispararCambioSeleccion());
        cmbTipoOrilla.valueProperty().addListener((obs, oldVal, newVal) -> dispararCambioSeleccion());

        // ✅ estrategia también dispara recalculo
        if (cmbEstrategiaPrecio != null) {
            cmbEstrategiaPrecio.valueProperty().addListener((obs, oldVal, newVal) -> dispararCambioEstrategia());
        }

        for (CheckBox cb : ingredientesList) {
            cb.selectedProperty().addListener((obs, oldVal, newVal) -> dispararCambioSeleccion());
        }

        for (CheckBox cb : condimentosList) {
            cb.selectedProperty().addListener((obs, oldVal, newVal) -> dispararCambioSeleccion());
        }
    }

    private void dispararCambioSeleccion() {
        if (onCambioSeleccion != null) {
            onCambioSeleccion.run();
        }
    }

    private void dispararCambioEstrategia() {
        if (onCambioEstrategia != null) {
            onCambioEstrategia.run();
        } else {
            // Si no lo conectaron explícitamente, por defecto recalcula con el mismo hook
            if (onCambioSeleccion != null) onCambioSeleccion.run();
        }
    }

    /**
     * ✅ La vista solo pinta el total (la lógica la hace el Controller/Servicio).
     */
    public void actualizarTotal(double total) {
        lblTotal.setText(String.format("$%.2f", total));
    }

    // --- Hook para el Controller ---
    public void setOnCambioSeleccion(Runnable r) {
        this.onCambioSeleccion = r;
    }

    // ✅ Hook para el Controller (estrategia)
    public void setOnCambioEstrategia(Runnable r) {
        this.onCambioEstrategia = r;
    }

    // --- Getters para obtener la información desde el Controlador ---

    public String getMasaSeleccionada() { return cmbMasa.getValue(); }
    public String getSalsaSeleccionada() { return cmbSalsa.getValue(); }
    public String getQuesoSeleccionado() { return cmbQueso.getValue(); }
    public String getOrillaSeleccionada() { return cmbTipoOrilla.getValue(); }

    // ✅ estrategia seleccionada
    public String getEstrategiaSeleccionada() {
        return (cmbEstrategiaPrecio == null) ? null : cmbEstrategiaPrecio.getValue();
    }

    public ObservableList<String> getIngredientesSeleccionados() {
        ObservableList<String> seleccionados = FXCollections.observableArrayList();
        for (CheckBox cb : ingredientesList) {
            if (cb.isSelected()) {
                String texto = cb.getText(); // "Pepperoni (+$1.50)"
                int idx = texto.indexOf(" (");
                String nombre = (idx > 0) ? texto.substring(0, idx).trim() : texto.trim();
                seleccionados.add(nombre);
            }
        }
        return seleccionados;
    }

    public ObservableList<String> getCondimentosSeleccionados() {
        ObservableList<String> seleccionados = FXCollections.observableArrayList();
        for (CheckBox cb : condimentosList) {
            if (cb.isSelected()) seleccionados.add(cb.getText());
        }
        return seleccionados;
    }

    public double getTotal() {
        try {
            return Double.parseDouble(lblTotal.getText().replace("$", ""));
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Muestra la ventana principal.
     */
    public void mostrar() {
        Scene scene = new Scene(root, 1400, 800);

        if (getClass().getResource("/styles/estilo.css") != null) {
            scene.getStylesheets().add(getClass().getResource("/styles/estilo.css").toExternalForm());
        }

        stage.setTitle("Sistema de Pizzería - Punto de Venta");
        stage.setScene(scene);
        stage.show();
    }

    public BorderPane getRoot() { return root; }
    public Stage getStage() { return stage; }

    /**
     * Resetea todos los campos a sus valores por defecto.
     */
    public void limpiarSeleccion() {
        cmbEstrategiaPrecio.getSelectionModel().selectFirst();

        cmbMasa.getSelectionModel().selectFirst();
        cmbSalsa.getSelectionModel().selectFirst();
        cmbQueso.getSelectionModel().selectFirst();
        cmbTipoOrilla.getSelectionModel().selectFirst();

        for (CheckBox cb : ingredientesList) cb.setSelected(false);
        for (CheckBox cb : condimentosList) cb.setSelected(false);

        // ✅ La vista no calcula, deja un valor neutro
        actualizarTotal(0.00);

        // ✅ Si quieres que el controller recalculé inmediatamente:
        dispararCambioSeleccion();
    }
}
