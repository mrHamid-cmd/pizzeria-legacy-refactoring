module com.mycompany.pizzasystemnuevo {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.mycompany.pizzasystemnuevo to javafx.fxml;
    exports com.mycompany.pizzasystemnuevo;
}
