package co.edu.proyecto.estacionmonitoreo.viewController;

import co.edu.proyecto.estacionmonitoreo.model.*;
import co.edu.proyecto.estacionmonitoreo.controller.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.*;

public class EcoMonitorGUI extends Application {

    private EcoMonitorSystem ecoSystem;

    private Button startButton;
    private Button stopButton;
    private Label statusLabel;
    private Label memoryLabel;
    private Label readingsLabel;
    private Label alertsLabel;
    private TextArea alertsTextArea;

    private Map<Integer, LineChart<Number, Number>> sensorCharts;
    private Map<Integer, XYChart.Series<Number, Number>> sensorSeries;
    private Map<Integer, Label> sensorValueLabels;

    private int timeCounter = 0;
    private static final int MAX_DATA_POINTS = 50;

    private Thread uiUpdateThread;
    private volatile boolean updatingUI = false;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("EcoMonitor - Sistema de Monitoreo Ambiental");

        sensorCharts = new HashMap<Integer, LineChart<Number, Number>>();
        sensorSeries = new HashMap<Integer, XYChart.Series<Number, Number>>();
        sensorValueLabels = new HashMap<Integer, Label>();

        ecoSystem = new EcoMonitorSystem();

        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(10));

        VBox topPanel = createTopPanel();
        mainLayout.setTop(topPanel);

        ScrollPane centerPanel = createCenterPanel();
        mainLayout.setCenter(centerPanel);

        VBox rightPanel = createRightPanel();
        mainLayout.setRight(rightPanel);

        Scene scene = new Scene(mainLayout, 1400, 800);
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            stopMonitoring();
            Platform.exit();
            System.exit(0);
        });

        System.out.println("Interfaz grafica iniciada");
    }

    private VBox createTopPanel() {
        VBox topPanel = new VBox(10);
        topPanel.setPadding(new Insets(10));
        topPanel.setStyle("-fx-background-color: #2c3e50; -fx-background-radius: 5;");

        Label titleLabel = new Label("EcoMonitor - Sistema de Monitoreo en Tiempo Real");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.WHITE);

        HBox buttonPanel = new HBox(15);
        buttonPanel.setAlignment(Pos.CENTER);

        startButton = new Button("Iniciar Monitoreo");
        startButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20;");
        startButton.setOnAction(e -> startMonitoring());

        stopButton = new Button("Detener Monitoreo");
        stopButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20;");
        stopButton.setDisable(true);
        stopButton.setOnAction(e -> stopMonitoring());

        Button resetButton = new Button("Reiniciar Graficos");
        resetButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 20;");
        resetButton.setOnAction(e -> resetCharts());

        buttonPanel.getChildren().addAll(startButton, stopButton, resetButton);

        HBox statusPanel = new HBox(30);
        statusPanel.setAlignment(Pos.CENTER);
        statusPanel.setPadding(new Insets(10, 0, 0, 0));

        statusLabel = new Label("Estado: Detenido");
        statusLabel.setTextFill(Color.LIGHTGRAY);
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        readingsLabel = new Label("Lecturas: 0");
        readingsLabel.setTextFill(Color.LIGHTGRAY);
        readingsLabel.setFont(Font.font("Arial", 14));

        alertsLabel = new Label("Alertas: 0");
        alertsLabel.setTextFill(Color.LIGHTGRAY);
        alertsLabel.setFont(Font.font("Arial", 14));

        memoryLabel = new Label("Memoria: 0 MB");
        memoryLabel.setTextFill(Color.LIGHTGRAY);
        memoryLabel.setFont(Font.font("Arial", 14));

        statusPanel.getChildren().addAll(statusLabel, readingsLabel, alertsLabel, memoryLabel);

        topPanel.getChildren().addAll(titleLabel, buttonPanel, statusPanel);
        return topPanel;
    }

    private ScrollPane createCenterPanel() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(15);
        gridPane.setVgap(15);

        Object[][] sensorConfig = {
                {1, "Temperatura", "#e74c3c", "°C"},
                {2, "Humedad", "#3498db", "%"},
                {3, "CO2", "#f39c12", "ppm"},
                {4, "Ruido", "#9b59b6", "dB"}
        };

        int row = 0, col = 0;

        for (Object[] config : sensorConfig) {
            int sensorId = (Integer) config[0];
            String name = (String) config[1];
            String color = (String) config[2];
            String unit = (String) config[3];

            VBox sensorPanel = createSensorPanel(sensorId, name, color, unit);
            gridPane.add(sensorPanel, col, row);

            col++;
            if (col >= 2) {
                col = 0;
                row++;
            }
        }

        ScrollPane scrollPane = new ScrollPane(gridPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #ecf0f1;");

        return scrollPane;
    }

    private VBox createSensorPanel(int sensorId, String sensorName, String color, String unit) {
        VBox panel = new VBox(5);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 2);");
        panel.setPrefWidth(400);
        panel.setPrefHeight(300);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(sensorName);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        nameLabel.setStyle("-fx-text-fill: " + color + ";");

        Label valueLabel = new Label("-- " + unit);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        valueLabel.setStyle("-fx-text-fill: " + color + ";");
        sensorValueLabels.put(sensorId, valueLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(nameLabel, spacer, valueLabel);

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Tiempo (s)");
        xAxis.setAutoRanging(true);
        xAxis.setForceZeroInRange(false);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(unit);
        yAxis.setAutoRanging(true);

        LineChart<Number, Number> chart = new LineChart<Number, Number>(xAxis, yAxis);
        chart.setTitle(sensorName);
        chart.setLegendVisible(false);
        chart.setCreateSymbols(false);
        chart.setAnimated(false);
        chart.setPrefHeight(250);

        XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
        series.setName(sensorName);
        chart.getData().add(series);

        chart.setStyle("CHART_COLOR_1: " + color + ";");

        sensorCharts.put(sensorId, chart);
        sensorSeries.put(sensorId, series);

        panel.getChildren().addAll(header, chart);
        return panel;
    }

    private VBox createRightPanel() {
        VBox rightPanel = new VBox(10);
        rightPanel.setPadding(new Insets(10));
        rightPanel.setPrefWidth(300);
        rightPanel.setStyle("-fx-background-color: #ecf0f1;");

        Label alertsTitle = new Label("Alertas del Sistema");
        alertsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        alertsTitle.setStyle("-fx-text-fill: #e74c3c;");

        alertsTextArea = new TextArea();
        alertsTextArea.setEditable(false);
        alertsTextArea.setPrefHeight(300);
        alertsTextArea.setWrapText(true);
        alertsTextArea.setStyle("-fx-control-inner-background: #fff; " +
                "-fx-font-family: 'Courier New'; -fx-font-size: 11px;");

        Button clearAlertsButton = new Button("Limpiar Alertas");
        clearAlertsButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        clearAlertsButton.setOnAction(e -> alertsTextArea.clear());

        VBox infoPanel = new VBox(8);
        infoPanel.setPadding(new Insets(10));
        infoPanel.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        Label infoTitle = new Label("Informacion del Sistema");
        infoTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Label jvmLabel = new Label("JVM: " + System.getProperty("java.version"));
        jvmLabel.setFont(Font.font("Arial", 12));

        Runtime runtime = Runtime.getRuntime();
        Label maxMemLabel = new Label(String.format("Memoria Max: %.0f MB",
                runtime.maxMemory() / 1024.0 / 1024.0));
        maxMemLabel.setFont(Font.font("Arial", 12));

        Label sensorsLabel = new Label("Sensores Activos: 4");
        sensorsLabel.setFont(Font.font("Arial", 12));

        infoPanel.getChildren().addAll(infoTitle, new Separator(),
                jvmLabel, maxMemLabel, sensorsLabel);

        rightPanel.getChildren().addAll(alertsTitle, alertsTextArea, clearAlertsButton,
                new Separator(), infoPanel);

        return rightPanel;
    }

    private void startMonitoring() {
        System.out.println("Iniciando monitoreo...");

        ecoSystem.startDataCollection();

        startButton.setDisable(true);
        stopButton.setDisable(false);
        statusLabel.setText("Estado: Monitoreando");
        statusLabel.setTextFill(Color.LIGHTGREEN);

        updatingUI = true;
        uiUpdateThread = new Thread(this::updateUILoop);
        uiUpdateThread.setDaemon(true);
        uiUpdateThread.start();

        addAlert("Sistema iniciado correctamente");
    }

    private void stopMonitoring() {
        System.out.println("Deteniendo monitoreo...");

        updatingUI = false;

        if (ecoSystem != null) {
            ecoSystem.stop();
        }

        startButton.setDisable(false);
        stopButton.setDisable(true);
        statusLabel.setText("Estado: Detenido");
        statusLabel.setTextFill(Color.LIGHTGRAY);

        addAlert("Sistema detenido");
    }

    private void updateUILoop() {
        while (updatingUI) {
            try {
                Thread.sleep(1000);

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        updateUI();
                    }
                });

            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void updateUI() {
        timeCounter++;

        readingsLabel.setText("Lecturas: " + ecoSystem.getTotalReadings());
        alertsLabel.setText("Alertas: " + ecoSystem.getAlertsGenerated());

        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        memoryLabel.setText("Memoria: " + usedMemory + " MB");

        for (int sensorId = 1; sensorId <= 4; sensorId++) {
            updateSensorChart(sensorId);
        }
    }

    private void updateSensorChart(int sensorId) {
        // AQUI ESTA EL CAMBIO PRINCIPAL - Usa ecoSystem directamente
        SensorReading reading = ecoSystem.getLatestReading(sensorId);

        if (reading != null) {
            XYChart.Series<Number, Number> series = sensorSeries.get(sensorId);
            Label valueLabel = sensorValueLabels.get(sensorId);

            if (series != null && valueLabel != null) {
                series.getData().add(new XYChart.Data<Number, Number>(timeCounter, reading.getValue()));

                if (series.getData().size() > MAX_DATA_POINTS) {
                    series.getData().remove(0);
                }

                valueLabel.setText(String.format("%.1f", reading.getValue()));

                // AQUI ESTA EL SEGUNDO CAMBIO - Usa ecoSystem directamente
                Sensor sensor = ecoSystem.getSensor(sensorId);
                if (sensor != null && !sensor.isValueNormal(reading.getValue())) {
                    valueLabel.setStyle("-fx-text-fill: #e74c3c;");
                    addAlert(String.format("%s: %.1f fuera de rango [%.0f-%.0f]",
                            reading.getSensorType(), reading.getValue(),
                            sensor.getMinThreshold(), sensor.getMaxThreshold()));
                } else {
                    String originalColor = getColorForSensor(sensorId);
                    valueLabel.setStyle("-fx-text-fill: " + originalColor + ";");
                }
            }
        }
    }

    private String getColorForSensor(int sensorId) {
        switch (sensorId) {
            case 1: return "#e74c3c";
            case 2: return "#3498db";
            case 3: return "#f39c12";
            case 4: return "#9b59b6";
            default: return "#000000";
        }
    }

    private void addAlert(String message) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                String timestamp = java.time.LocalTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
                alertsTextArea.appendText(String.format("[%s] %s\n", timestamp, message));

                alertsTextArea.setScrollTop(Double.MAX_VALUE);

                String[] lines = alertsTextArea.getText().split("\n");
                if (lines.length > 100) {
                    StringBuilder newText = new StringBuilder();
                    for (int i = lines.length - 100; i < lines.length; i++) {
                        newText.append(lines[i]).append("\n");
                    }
                    alertsTextArea.setText(newText.toString());
                }
            }
        });
    }

    private void resetCharts() {
        timeCounter = 0;
        for (XYChart.Series<Number, Number> series : sensorSeries.values()) {
            series.getData().clear();
        }
        addAlert("Graficos reiniciados");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
