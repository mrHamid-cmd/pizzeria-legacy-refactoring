package com.pizzasystem.vistas;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.ColumnConstraints;
import javafx.geometry.Insets;
import javafx.stage.Stage;

public class LoginView {
    private Stage stage;
    private BorderPane root;

    // Componentes
    public TextField txtUsuario;
    public PasswordField txtContrasena;
    public Button btnIniciarSesion;
    public Label lblMensajeError;

    public LoginView(Stage primaryStage) {
        this.stage = primaryStage;
        crearVista();
    }

    private void crearVista() {
        root = new BorderPane();

        // ---------------------------------------------------------
        // 1. CONFIGURACIÓN DEL FONDO (fondo4.png) EN PANTALLA COMPLETA
        // ---------------------------------------------------------
        try {
            String imagePath = getClass().getResource("/imagenes/fondo4.png").toExternalForm();
            root.setStyle(
                "-fx-background-image: url('" + imagePath + "'); " +
                "-fx-background-size: 100% 100%; " +
                "-fx-background-repeat: no-repeat; " +
                "-fx-background-position: center center;"
            );
        } catch (Exception e) {
            System.out.println("⚠️ No se pudo cargar /imagenes/fondo4.png");
            root.setStyle("-fx-background-color: #2c3e50;");
        }

        // ---------------------------------------------------------
        // 2. TARJETA DE LOGIN
        // ---------------------------------------------------------
        VBox loginCard = new VBox(20);

        loginCard.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.9); " +
            "-fx-background-radius: 15; " +
            "-fx-padding: 40; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 5);"
        );

        loginCard.setMaxWidth(450);
        loginCard.setMaxHeight(400);
        loginCard.setAlignment(Pos.CENTER);

        Label titulo = new Label("Sistema de Pizzería");
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPrefWidth(100);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPrefWidth(200);
        grid.getColumnConstraints().addAll(col1, col2);

        Label lblUsuario = new Label("Usuario:");
        lblUsuario.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");

        txtUsuario = new TextField();
        txtUsuario.setPromptText("Ingrese su usuario");
        txtUsuario.setStyle("-fx-background-radius: 5; -fx-padding: 8;");

        Label lblContrasena = new Label("Contraseña:");
        lblContrasena.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");

        txtContrasena = new PasswordField();
        txtContrasena.setPromptText("Ingrese su contraseña");
        txtContrasena.setStyle("-fx-background-radius: 5; -fx-padding: 8;");

        btnIniciarSesion = new Button("Iniciar Sesión");
        btnIniciarSesion.setStyle(
            "-fx-background-color: #e67e22; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px; " +
            "-fx-pref-width: 150; " +
            "-fx-background-radius: 5; " +
            "-fx-cursor: hand;"
        );

        // ✅ ENTER = iniciar sesión (UX)
        btnIniciarSesion.setDefaultButton(true);

        lblMensajeError = new Label();
        lblMensajeError.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");

        grid.add(lblUsuario, 0, 0);
        grid.add(txtUsuario, 1, 0);
        grid.add(lblContrasena, 0, 1);
        grid.add(txtContrasena, 1, 1);
        grid.add(btnIniciarSesion, 1, 2);

        GridPane.setHalignment(btnIniciarSesion, javafx.geometry.HPos.CENTER);
        GridPane.setMargin(btnIniciarSesion, new Insets(15, 0, 0, 0));

        loginCard.getChildren().addAll(titulo, grid, lblMensajeError);
        root.setCenter(loginCard);
    }

    public void mostrar() {
        Scene scene = new Scene(root, 1400, 800);

        if (getClass().getResource("/styles/estilo.css") != null) {
            scene.getStylesheets().add(getClass().getResource("/styles/estilo.css").toExternalForm());
        }

        stage.setTitle("Sistema de Pizzería - Login");
        stage.setScene(scene);
        stage.show();
    }

    public BorderPane getRoot() {
        return root;
    }

    public Stage getStage() {
        return stage;
    }
}
