package com.pizzasystem.controladores;

import com.pizzasystem.vistas.LoginView;
import com.pizzasystem.vistas.TomaPedidoView;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Controlador para la pantalla de Login.
 *
 * Importante: ya no se usa FXML. El controlador se conecta a la vista
 * mediante el método setView(LoginView view), donde se enganchan los
 * listeners del botón y se inicializan los archivos.
 */
public class LoginController {

    private LoginView view;
    private Stage stage;

    /**
     * Conecta la vista con el controlador.
     */
    public void setView(LoginView view) {
        this.view = view;
        this.stage = view.getStage();

        // Limpiar mensaje de error al inicio
        if (view.lblMensajeError != null) {
            view.lblMensajeError.setText("");
        }

        // Crear archivo de usuarios si no existe
        crearArchivoUsuariosSiNoExiste();

        // Conectar acción del botón de iniciar sesión
        view.btnIniciarSesion.setOnAction(e -> manejarLogin());
    }

    /**
     * Crea el archivo usuarios.txt con algunas cuentas por defecto si no existe.
     */
    private void crearArchivoUsuariosSiNoExiste() {
        try {
            File file = new File("usuarios.txt");
            if (!file.exists()) {
                try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                    writer.println("admin,admin123");
                    writer.println("cocina,cocina123");
                    writer.println("cliente,cliente123");
                    writer.println("empleado,empleado123");
                    writer.println("trabajador,trabajador123");
                    System.out.println("Archivo de usuarios creado exitosamente");
                }
            }
        } catch (IOException e) {
            System.err.println("Error al crear archivo de usuarios: " + e.getMessage());
        }
    }

    /**
     * Lógica cuando el usuario pulsa el botón "Iniciar Sesión".
     */
    private void manejarLogin() {
        String usuario = (view.txtUsuario != null) ? view.txtUsuario.getText() : null;
        String contrasena = (view.txtContrasena != null) ? view.txtContrasena.getText() : null;

        // ✅ OK loguear usuario; ❌ NO loguear contraseña
        System.out.println("Usuario: " + usuario);

        if (usuario == null || usuario.isBlank() || contrasena == null || contrasena.isBlank()) {
            if (view.lblMensajeError != null) {
                view.lblMensajeError.setText("Por favor complete todos los campos");
            }
            return;
        }

        // Verificar credenciales contra archivo (o por defecto)
        if (validarCredenciales(usuario, contrasena)) {
            if (view.lblMensajeError != null) {
                view.lblMensajeError.setText("");
            }

            System.out.println("Login exitoso para usuario: " + usuario);

            // Cerrar ventana de login
            if (stage != null) {
                stage.close();
            }

            // Abrir ventana de toma de pedidos
            abrirTomaPedido();
        } else {
            if (view.lblMensajeError != null) {
                view.lblMensajeError.setText("Usuario o contraseña incorrectos");
            }
        }
    }

    /**
     * Verifica credenciales leyendo usuarios.txt.
     * Si hay problema leyendo el archivo, cae a credenciales por defecto.
     */
    private boolean validarCredenciales(String usuario, String contrasena) {
        File file = new File("usuarios.txt");
        if (!file.exists()) {
            // Si el archivo no existe, usar credenciales por defecto
            return verificarCredencialesPorDefecto(usuario, contrasena);
        }

        // ✅ try-with-resources para cerrar siempre
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String storedUser = parts[0].trim();
                    String storedPass = parts[1].trim();

                    if (storedUser.equals(usuario.trim()) && storedPass.equals(contrasena.trim())) {
                        return true;
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Error al verificar credenciales: " + e.getMessage());
            // Si hay error leyendo el archivo, usar credenciales por defecto
            return verificarCredencialesPorDefecto(usuario, contrasena);
        }

        return false;
    }

    /**
     * Credenciales “hardcodeadas” para desarrollo / respaldo.
     */
    private boolean verificarCredencialesPorDefecto(String usuario, String contrasena) {
        return (usuario.equals("trabajador") && contrasena.equals("trabajador123")) ||
               (usuario.equals("admin")      && contrasena.equals("admin123"))      ||
               (usuario.equals("cocina")     && contrasena.equals("cocina123"))     ||
               (usuario.equals("empleado")   && contrasena.equals("empleado123"))   ||
               (usuario.equals("cliente")    && contrasena.equals("cliente123"));
    }

    /**
     * Abre la ventana de toma de pedidos y conecta su controlador.
     */
    private void abrirTomaPedido() {
        try {
            Stage tomaPedidoStage = new Stage();
            TomaPedidoView tomaPedidoView = new TomaPedidoView(tomaPedidoStage);

            TomaPedidoController controller = new TomaPedidoController();
            controller.setView(tomaPedidoView);

            tomaPedidoView.mostrar();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlertaError("Error al abrir la ventana de toma de pedido: " + e.getMessage());
        }
    }

    private void mostrarAlertaError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Ocurrió un error");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
