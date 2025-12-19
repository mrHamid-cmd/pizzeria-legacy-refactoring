package com.mycompany.pizzasystemnuevo;

import javafx.application.Application;
import javafx.stage.Stage;

// 🔹 importa tus vistas/controladores reales
import com.pizzasystem.vistas.LoginView;
import com.pizzasystem.controladores.LoginController;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        // 1) Crear la vista de login
        LoginView loginView = new LoginView(stage);

        // 2) Crear el controlador
        LoginController loginController = new LoginController();

        // 3) Conectar vista ↔ controlador
        loginController.setView(loginView);

        // 4) Mostrar login
        loginView.mostrar();
    }

    public static void main(String[] args) {
        launch();
    }
}
