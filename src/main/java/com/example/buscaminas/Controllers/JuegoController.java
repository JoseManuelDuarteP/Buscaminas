package com.example.buscaminas.Controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;

public class JuegoController {

    @FXML
    protected GridPane tablero;
    @FXML
    protected Label contadorBanderas;
    @FXML
    protected Label reloj;

    int columnasSta;
    int banderasPuestas = 0;
    int bombasGlob;
    int casillasTotal;

    Timeline timeline;
    int segundos;

    private EventHandler<MouseEvent> bombHandler = BOMB();
    private EventHandler<MouseEvent> safeHandler = noBOMB();
    private EventHandler<MouseEvent> discoveredHandler = event ->  {};

    protected void generarTablero(int columnas) {
        contadorBanderas.setMaxWidth(Double.MAX_VALUE);
        contadorBanderas.setAlignment(Pos.CENTER);

        columnasSta = columnas;
        casillasTotal = columnas * columnas;

        List<Integer> bombasPuestas = new ArrayList<>();
        int bombas = (columnas*columnas * 15) / 100;
        bombasGlob = bombas;

        contadorBanderas.setText("🚩 " + banderasPuestas + " / " + bombas);

        tablero.getChildren().clear();
        tablero.getColumnConstraints().clear();
        tablero.getRowConstraints().clear();

        for (int i = 0; i < casillasTotal; i++) {
            StackPane casilla = crearCasilla();

            int fila = i / columnas;
            int columna = i % columnas;

            casilla.setId(String.valueOf(i));
            casilla.setOnMouseClicked(safeHandler);
            tablero.add(casilla, columna, fila);
        }

        while (bombasPuestas.size() < bombas) {
            Random r = new Random();
            int random = r.nextInt(tablero.getChildren().size());

            if (!bombasPuestas.contains(random)) {
                bombasPuestas.add(random);
            }

        }

        for (Integer b : bombasPuestas) {
            Optional<Node> yi = tablero.getChildren().stream()
                    .filter(p -> p.getId().equals(String.valueOf(b)))
                    .findFirst();

            yi.ifPresent(y -> y.setOnMouseClicked(bombHandler));
        }
        bombasPuestas.sort(null);
        iniciarReloj();
    }

    protected StackPane crearCasilla() {
        Rectangle casilla = new Rectangle(25, 25);
        casilla.setStroke(Color.BLACK);
        casilla.setFill(Color.LIGHTGRAY);

        StackPane stackPane = new StackPane(casilla);
        return stackPane;
    }

    protected EventHandler<MouseEvent> BOMB() {
        return event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                ponerBandera().handle(event);

            } else if (event.getButton() == MouseButton.PRIMARY) {
                if (checkBandera(event)) return;

                URL sUrl = getClass().getResource("/sr.mp3");
                if (sUrl != null) {
                    AudioClip audioClip = new AudioClip(sUrl.toString());
                    audioClip.play();
                }
                detenerTiempo();

                Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                alerta.setContentText("💣 BOMB");
                alerta.showAndWait();

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                stage.close();
            }
        };
    }

    protected EventHandler<MouseEvent> noBOMB() {
        return event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                ponerBandera().handle(event);

            } else if (event.getButton() == MouseButton.PRIMARY) {
                StackPane stackPane = (StackPane) event.getSource();

                if (checkBandera(event)) return;

                Rectangle hijo = (Rectangle) stackPane.getChildren().getFirst();
                hijo.setFill(Color.WHITE);

                stackPane.setOnMouseClicked(discoveredHandler);

                comprobarAlrededor(stackPane);

                if (victoria())
                    ganar(event);
            }
        };
    }

    protected EventHandler<MouseEvent> ponerBandera() {
        return event -> {
            StackPane stackPane = (StackPane) event.getSource();

            Optional<Node> bandera = stackPane.getChildren().stream()
                    .filter(n -> n instanceof Label && "🚩".equals(((Label) n).getText()))
                    .findFirst();

            if (bandera.isPresent()) {
                stackPane.getChildren().remove(bandera.get());
                banderasPuestas--;
                contadorBanderas.setText("🚩 " + banderasPuestas + " / " + bombasGlob);
            } else {

                Label banderaLabel = new Label("🚩");
                banderaLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
                stackPane.getChildren().add(banderaLabel);
                banderasPuestas++;
                contadorBanderas.setText("🚩 " + banderasPuestas + " / " + bombasGlob);
            }
            event.consume();
        };
    }

    protected boolean checkBandera(MouseEvent event) {
        StackPane stackPane = (StackPane) event.getSource();

        boolean marcado = stackPane.getChildren().stream()
                .anyMatch(p -> p instanceof Label && "🚩".equals(((Label) p).getText()));

        return marcado;
    }

    protected void comprobarAlrededor(StackPane stackPane) {
        List<Integer> vecinos = cogerCasillasAlrededor(stackPane);
        List<Integer> vecinosDelVecino;
        StackPane padre;
        StackPane padre2;
        Rectangle hijo;
        int bombas;

        for (Integer vecino : vecinos) {
            padre = (StackPane) tablero.getChildren().get(vecino);

            if (padre.getOnMouseClicked() == discoveredHandler) continue;
            if (padre.getOnMouseClicked() == bombHandler) continue;

            bombas = 0;
            hijo = (Rectangle) padre.getChildren().getFirst();
            vecinosDelVecino = cogerCasillasAlrededor(padre);

            for (Integer vecino2 : vecinosDelVecino) {
                padre2 = (StackPane) tablero.getChildren().get(vecino2);

                if (padre2.getOnMouseClicked() == bombHandler) {
                    bombas++;
                }
            }

            if (bombas > 0) {
                ponerNumeros(hijo, padre, bombas);
                padre.setOnMouseClicked(discoveredHandler);

            } else {
                hijo.setFill(Color.WHITE);
                padre.setOnMouseClicked(discoveredHandler);

                comprobarAlrededor(padre);
            }
        }
    }


    protected List<Integer> cogerCasillasAlrededor(StackPane stackPane) {
        int id = Integer.parseInt(stackPane.getId());
        List<Integer> vecinos = new ArrayList<>();

        int filas = columnasSta;
        int columnas = columnasSta;

        int fila = id / columnas;
        int columna = id % columnas;

        for (int df = -1; df <= 1; df++) {

            for (int dc = -1; dc <= 1; dc++) {

                if (df == 0 && dc == 0) continue;

                int nuevaFila = fila + df;
                int nuevaColumna = columna + dc;

                if (nuevaFila >= 0 && nuevaFila < filas && nuevaColumna >= 0 && nuevaColumna < columnas) {
                    int vecino = nuevaFila * columnas + nuevaColumna;
                    vecinos.add(vecino);
                }
            }
        }

        return vecinos;
    }

    protected void ponerNumeros(Rectangle hijo, StackPane padre, int bombas) {
        hijo.setFill(Color.WHITE);
        Label numBombas = new Label(String.valueOf(bombas));
        numBombas.setText(String.valueOf(bombas));
        numBombas.setStyle("-fx-font-weight: bold; -fx-font-size: 17");

        switch (bombas) {
            case 1 -> numBombas.setTextFill(Color.BLUE);
            case 2 -> numBombas.setTextFill(Color.GREEN);
            case 3 -> numBombas.setTextFill(Color.RED);
            case 4 -> numBombas.setTextFill(Color.DARKBLUE);
            case 5 -> numBombas.setTextFill(Color.DARKRED);
            case 6 -> numBombas.setTextFill(Color.DARKTURQUOISE);
            case 7 -> numBombas.setTextFill(Color.BLACK);
            case 8 -> numBombas.setTextFill(Color.GRAY);

        }

        padre.getChildren().clear();
        padre.getChildren().addAll(hijo, numBombas);
    }

    protected boolean victoria() {
        return tablero.getChildren().stream()
                .noneMatch(p -> p.getOnMouseClicked() == safeHandler);
    }

    protected void ganar(MouseEvent event) {
        detenerTiempo();

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setContentText("🏆 ¡HAS GANADO!");
        alerta.showAndWait();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    protected void iniciarReloj() {
        segundos = 0;
        reloj.setText("⏱ 0s");

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            segundos++;
            reloj.setText("⏱ " + segundos + "s");
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    protected void detenerTiempo() {
        timeline.stop();
    }
}
