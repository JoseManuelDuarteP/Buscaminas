module com.example.buscaminas {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.almasb.fxgl.all;
    requires java.desktop;
    requires javafx.media;

    opens com.example.buscaminas to javafx.fxml;
    exports com.example.buscaminas;
    exports com.example.buscaminas.Controllers;
    opens com.example.buscaminas.Controllers to javafx.fxml;
}