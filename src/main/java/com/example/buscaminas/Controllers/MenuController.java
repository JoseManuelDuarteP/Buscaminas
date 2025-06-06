package com.example.buscaminas.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MenuController {

    @FXML
    protected void iniciarFacil() {
        abrirJuego(7);
    }

    @FXML
    protected void iniciarIntermedio() {
        abrirJuego(12);
    }

    @FXML
    protected void iniciarDificil() {
        abrirJuego(18);
    }

    @FXML
    protected void salir() {
        System.exit(0);
    }

    @FXML
    protected void abrirJuego(int columnas) {
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/buscaminas/juego.fxml"));
            Parent root = fxmlLoader.load();

            JuegoController juegoController = fxmlLoader.getController();
            juegoController.generarTablero(columnas);

            Stage stage = new Stage();
            stage.setTitle("Buscaminas");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}