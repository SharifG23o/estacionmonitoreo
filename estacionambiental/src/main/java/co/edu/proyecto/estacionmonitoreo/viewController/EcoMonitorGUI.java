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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.animation.*;
import javafx.util.Duration;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.*;
import java.util.*;
import java.util.List;

import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.VerticalPositionMark;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;



public class EcoMonitorGUI extends Application {

    private EcoMonitorSystem ecoSystem;

    private Button startButton;
    private Button stopButton;
    private Button exportButton;
    private Label statusLabel;
    private Label memoryLabel;
    private Label readingsLabel;
    private Label alertsLabel;
    private TextArea alertsTextArea;
    
    // Información extendida del sistema
    private Label cpuLabel;
    private Label heapUsageLabel;
    private Label nonHeapUsageLabel;
    private Label threadsLabel;
    private Label uptimeLabel;
    private Label fpsLabel;
    private Label gcLabel;
    private ProgressBar memoryProgressBar;
    private ProgressBar cpuProgressBar;
    private LineChart<Number, Number> memoryChart;
    private XYChart.Series<Number, Number> memorySeries;

    private Map<Integer, LineChart<Number, Number>> sensorCharts;
    private Map<Integer, XYChart.Series<Number, Number>> sensorSeries;
    private Map<Integer, Label> sensorValueLabels;
    private Map<Integer, Label> sensorStatusLabels;
    private Map<Integer, ProgressIndicator> sensorIndicators;

    private int timeCounter = 0;
    private static final int MAX_DATA_POINTS = 50;
    private static final int MEMORY_CHART_POINTS = 60;
    private long startTime;
    private long lastUpdateTime;
    private double currentFps = 0;

    private Thread uiUpdateThread;
    private volatile boolean updatingUI = false;
    
    private MemoryMXBean memoryBean;
    private OperatingSystemMXBean osBean;
    private ThreadMXBean threadBean;
    private List<GarbageCollectorMXBean> gcBeans;
    private long lastGcCount = 0;
    
    // Estadísticas
    private int totalAnomalies = 0;
    private Map<Integer, Double> sensorAverages;
    private Map<Integer, Integer> sensorReadCount;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("EcoMonitor Pro - Sistema Avanzado de Monitoreo Ambiental");

        // Inicialización
        sensorCharts = new HashMap<>();
        sensorSeries = new HashMap<>();
        sensorValueLabels = new HashMap<>();
        sensorStatusLabels = new HashMap<>();
        sensorIndicators = new HashMap<>();
        sensorAverages = new HashMap<>();
        sensorReadCount = new HashMap<>();

        memoryBean = ManagementFactory.getMemoryMXBean();
        osBean = ManagementFactory.getOperatingSystemMXBean();
        threadBean = ManagementFactory.getThreadMXBean();
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        startTime = System.currentTimeMillis();
        lastUpdateTime = startTime;

        ecoSystem = new EcoMonitorSystem();

        // Layout principal
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: linear-gradient(to bottom, #ecf0f1, #d5dbdb);");

        VBox topPanel = createTopPanel(primaryStage);
        mainLayout.setTop(topPanel);

        HBox centerContainer = new HBox(15);
        centerContainer.setPadding(new Insets(10));
        
        ScrollPane centerPanel = createCenterPanel();
        VBox rightPanel = createRightPanel();
        
        HBox.setHgrow(centerPanel, Priority.ALWAYS);
        centerContainer.getChildren().addAll(centerPanel, rightPanel);
        
        ScrollPane mainScroll = new ScrollPane(centerContainer);
        mainScroll.setFitToWidth(true);
        mainScroll.setFitToHeight(true);
        mainScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainScroll.setStyle("-fx-background-color: transparent;");
        
        mainLayout.setCenter(mainScroll);

        VBox bottomPanel = createBottomPanel();
        mainLayout.setBottom(bottomPanel);

        Scene scene = new Scene(mainLayout, 1400, 900);
        
        // IMPORTANTE: Configurar la ventana ANTES de setScene para asegurar decoraciones nativas
        // Esto garantiza que los botones de minimizar, maximizar y cerrar aparezcan
        primaryStage.setResizable(true);
        primaryStage.setScene(scene);
        
        // NO iniciar en maximizado para que se vean los controles
        primaryStage.setMaximized(true);
        
        primaryStage.show();

        // Animación de entrada
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), mainLayout);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        primaryStage.setOnCloseRequest(event -> {
            stopMonitoring();
            Platform.exit();
            System.exit(0);
        });

        System.out.println("=== EcoMonitor Pro Iniciado ===");
        addAlert("🚀 Sistema inicializado correctamente");
    }

    private VBox createTopPanel(Stage stage) {
    VBox topPanel = new VBox(12);
    topPanel.setPadding(new Insets(20));
    topPanel.setStyle("-fx-background-color: linear-gradient(to right, #1a252f, #2c3e50, #34495e); " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 15, 0, 0, 5);");

    // 🔹 Título principal
    HBox titleBox = new HBox(15);
    titleBox.setAlignment(Pos.CENTER_LEFT);

    Label titleLabel = new Label("🌍 EcoMonitor Pro");
    titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
    titleLabel.setTextFill(Color.web("#ecf0f1"));

    Label subtitleLabel = new Label("Sistema Avanzado de Monitoreo Ambiental en Tiempo Real");
    subtitleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
    subtitleLabel.setTextFill(Color.web("#bdc3c7"));

    VBox titleContainer = new VBox(5);
    titleContainer.getChildren().addAll(titleLabel, subtitleLabel);
    titleBox.getChildren().add(titleContainer);

    // 🔹 Panel de control
    HBox buttonPanel = new HBox(15);
    buttonPanel.setAlignment(Pos.CENTER);
    buttonPanel.setPadding(new Insets(10, 0, 10, 0));

    startButton = createStyledButton("▶ Iniciar Monitoreo", "#27ae60", "#229954");
    startButton.setOnAction(e -> startMonitoring());

    stopButton = createStyledButton("⏹ Detener", "#e74c3c", "#c0392b");
    stopButton.setDisable(true);
    stopButton.setOnAction(e -> stopMonitoring());

    Button pauseButton = createStyledButton("⏸ Pausar", "#f39c12", "#d68910");
    pauseButton.setOnAction(e -> togglePause());

    Button resetButton = createStyledButton("🔄 Reiniciar", "#3498db", "#2980b9");
    resetButton.setOnAction(e -> resetCharts());

    // 🔹 Botón exportar con FileChooser
    Button exportButton = createStyledButton("💾 Exportar Datos", "#9b59b6", "#8e44ad");
    exportButton.setOnAction(e -> {
        try {
            exportData((Stage) exportButton.getScene().getWindow());
        } catch (com.itextpdf.text.DocumentException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    });

    buttonPanel.getChildren().addAll(startButton, stopButton, pauseButton, resetButton, exportButton);

    // 🔹 Dashboard de métricas
    GridPane metricsGrid = new GridPane();
    metricsGrid.setAlignment(Pos.CENTER);
    metricsGrid.setHgap(20);
    metricsGrid.setVgap(12);
    metricsGrid.setPadding(new Insets(15, 0, 5, 0));

    statusLabel = createMetricLabel("⚙ Estado: Detenido", "#95a5a6");
    readingsLabel = createMetricLabel("📊 Lecturas: 0", "#3498db");
    alertsLabel = createMetricLabel("⚠ Alertas: 0", "#e74c3c");
    memoryLabel = createMetricLabel("💾 Memoria: 0 MB", "#9b59b6");
    cpuLabel = createMetricLabel("🖥 CPU: 0%", "#f39c12");
    threadsLabel = createMetricLabel("🧵 Hilos: 0", "#1abc9c");
    fpsLabel = createMetricLabel("⚡ FPS: 0", "#e67e22");
    gcLabel = createMetricLabel("🗑 GC: 0", "#34495e");

    metricsGrid.add(createMetricCard(statusLabel), 0, 0);
    metricsGrid.add(createMetricCard(readingsLabel), 1, 0);
    metricsGrid.add(createMetricCard(alertsLabel), 2, 0);
    metricsGrid.add(createMetricCard(memoryLabel), 3, 0);
    metricsGrid.add(createMetricCard(cpuLabel), 0, 1);
    metricsGrid.add(createMetricCard(threadsLabel), 1, 1);
    metricsGrid.add(createMetricCard(fpsLabel), 2, 1);
    metricsGrid.add(createMetricCard(gcLabel), 3, 1);

    topPanel.getChildren().addAll(titleBox, new Separator(), buttonPanel, metricsGrid);
    return topPanel;
}


    private Button createStyledButton(String text, String color, String hoverColor) {
        Button button = new Button(text);
        String baseStyle = "-fx-background-color: %s; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 12 30; -fx-background-radius: 8; " +
                "-fx-font-weight: bold; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2);";
        
        button.setStyle(String.format(baseStyle, color));
        
        button.setOnMouseEntered(e -> {
            button.setStyle(String.format(baseStyle, hoverColor));
            ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(String.format(baseStyle, color));
            ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
        
        return button;
    }

    private Label createMetricLabel(String text, String color) {
        Label label = new Label(text);
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        return label;
    }

    private VBox createMetricCard(Label label) {
        VBox card = new VBox(label);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(12, 20, 12, 20));
        card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.15); " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: rgba(255, 255, 255, 0.2); " +
                "-fx-border-radius: 8; -fx-border-width: 1;");
        return card;
    }

    private ScrollPane createCenterPanel() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(15));
        gridPane.setHgap(20);
        gridPane.setVgap(20);

        Object[][] sensorConfig = {
                {1, "Temperatura", "#e74c3c", "°C", "🌡"},
                {2, "Humedad", "#3498db", "%", "💧"},
                {3, "CO2", "#f39c12", "ppm", "💨"},
                {4, "Índice UV", "#9b59b6", "UV", "☀"}
        };

        int row = 0, col = 0;

        for (Object[] config : sensorConfig) {
            int sensorId = (Integer) config[0];
            String name = (String) config[1];
            String color = (String) config[2];
            String unit = (String) config[3];
            String icon = (String) config[4];

            VBox sensorPanel = createAdvancedSensorPanel(sensorId, name, color, unit, icon);
            gridPane.add(sensorPanel, col, row);

            col++;
            if (col >= 2) {
                col = 0;
                row++;
            }
        }

        ScrollPane scrollPane = new ScrollPane(gridPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        return scrollPane;
    }

    private VBox createAdvancedSensorPanel(int sensorId, String name, String color, String unit, String icon) {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(18));
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 4);");
        panel.setPrefWidth(450);
        panel.setMinWidth(400);
        panel.setMinHeight(350);
        panel.setPrefHeight(350);

        // Header mejorado
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox nameBox = new VBox(3);
        Label nameLabel = new Label(icon + " " + name);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        nameLabel.setStyle("-fx-text-fill: " + color + ";");

        Label statusLabel = new Label("● En línea");
        statusLabel.setFont(Font.font("Arial", 11));
        statusLabel.setStyle("-fx-text-fill: #27ae60;");
        sensorStatusLabels.put(sensorId, statusLabel);

        nameBox.getChildren().addAll(nameLabel, statusLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox valueBox = new VBox(3);
        valueBox.setAlignment(Pos.CENTER_RIGHT);

        Label valueLabel = new Label("-- " + unit);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        valueLabel.setStyle("-fx-text-fill: " + color + ";");
        sensorValueLabels.put(sensorId, valueLabel);

        ProgressIndicator indicator = new ProgressIndicator(0);
        indicator.setPrefSize(30, 30);
        indicator.setStyle("-fx-accent: " + color + ";");
        indicator.setVisible(false);
        sensorIndicators.put(sensorId, indicator);

        valueBox.getChildren().addAll(valueLabel, indicator);

        header.getChildren().addAll(nameBox, spacer, valueBox);

        // Gráfico mejorado
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Tiempo (s)");
        xAxis.setAutoRanging(true);
        xAxis.setForceZeroInRange(false);
        xAxis.setTickLabelFill(Color.web("#7f8c8d"));
        xAxis.setMinorTickVisible(false);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(unit);
        yAxis.setAutoRanging(true);
        yAxis.setTickLabelFill(Color.web("#7f8c8d"));

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setCreateSymbols(false);
        chart.setAnimated(false);
        chart.setPrefHeight(260);
        chart.setMinHeight(260);

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(name);
        chart.getData().add(series);

        String chartStyle = "CHART_COLOR_1: " + color + "; " +
                "-fx-background-color: transparent; " +
                "-fx-stroke-width: 2.5px;";
        chart.setStyle(chartStyle);

        sensorCharts.put(sensorId, chart);
        sensorSeries.put(sensorId, series);

        panel.getChildren().addAll(header, new Separator(), chart);
        return panel;
    }

    private VBox createRightPanel() {
        VBox rightPanel = new VBox(15);
        rightPanel.setPrefWidth(380);

        VBox osPanel = createEnhancedSystemPanel();
        VBox memoryPanel = createEnhancedMemoryPanel();
        VBox alertsPanel = createEnhancedAlertsPanel();
        VBox statsPanel = createStatisticsPanel();

        rightPanel.getChildren().addAll(osPanel, memoryPanel, statsPanel, alertsPanel);
        return rightPanel;
    }

    private VBox createEnhancedSystemPanel() {
        VBox panel = createStyledPanel("💻 Información del Sistema", "#2c3e50");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);

        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        String osArch = System.getProperty("os.arch");
        String javaVersion = System.getProperty("java.version");
        String javaVendor = System.getProperty("java.vendor");
        int processors = Runtime.getRuntime().availableProcessors();

        addStyledInfoRow(grid, 0, "Sistema Operativo:", osName);
        addStyledInfoRow(grid, 1, "Versión SO:", osVersion);
        addStyledInfoRow(grid, 2, "Arquitectura:", osArch);
        addStyledInfoRow(grid, 3, "Java Version:", javaVersion);
        addStyledInfoRow(grid, 4, "Java Vendor:", javaVendor);
        addStyledInfoRow(grid, 5, "Procesadores:", String.valueOf(processors));

        HBox uptimeBox = new HBox(10);
        uptimeBox.setAlignment(Pos.CENTER_LEFT);
        Label uptimeTitle = new Label("⏱ Tiempo Activo:");
        uptimeTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        uptimeTitle.setStyle("-fx-text-fill: #7f8c8d;");
        
        uptimeLabel = new Label("00:00:00");
        uptimeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        uptimeLabel.setStyle("-fx-text-fill: #27ae60;");
        
        uptimeBox.getChildren().addAll(uptimeTitle, uptimeLabel);
        grid.add(uptimeBox, 0, 6, 2, 1);

        panel.getChildren().addAll(grid);
        return panel;
    }

    private VBox createEnhancedMemoryPanel() {
        VBox panel = createStyledPanel("📊 Monitor de Memoria", "#9b59b6");

        VBox content = new VBox(10);

        // Heap Memory
        VBox heapBox = new VBox(5);
        heapUsageLabel = new Label("Heap: 0 MB / 0 MB (0%)");
        heapUsageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        
        memoryProgressBar = new ProgressBar(0);
        memoryProgressBar.setPrefWidth(340);
        memoryProgressBar.setPrefHeight(16);
        memoryProgressBar.setStyle("-fx-accent: #3498db;");
        
        heapBox.getChildren().addAll(heapUsageLabel, memoryProgressBar);

        // Non-Heap Memory
        nonHeapUsageLabel = new Label("Non-Heap: 0 MB");
        nonHeapUsageLabel.setFont(Font.font("Arial", 11));

        // CPU Progress
        VBox cpuBox = new VBox(5);
        Label cpuTitle = new Label("Uso de CPU:");
        cpuTitle.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        
        cpuProgressBar = new ProgressBar(0);
        cpuProgressBar.setPrefWidth(340);
        cpuProgressBar.setPrefHeight(16);
        cpuProgressBar.setStyle("-fx-accent: #e74c3c;");
        
        cpuBox.getChildren().addAll(cpuTitle, cpuProgressBar);

        // Gráfico de memoria histórica
        NumberAxis memXAxis = new NumberAxis();
        memXAxis.setLabel("Tiempo");
        memXAxis.setAutoRanging(true);
        memXAxis.setTickLabelFill(Color.web("#7f8c8d"));

        NumberAxis memYAxis = new NumberAxis();
        memYAxis.setLabel("MB");
        memYAxis.setAutoRanging(true);
        memYAxis.setTickLabelFill(Color.web("#7f8c8d"));

        memoryChart = new LineChart<>(memXAxis, memYAxis);
        memoryChart.setTitle("Uso de Memoria");
        memoryChart.setLegendVisible(false);
        memoryChart.setCreateSymbols(false);
        memoryChart.setAnimated(false);
        memoryChart.setPrefHeight(130);
        memoryChart.setMinHeight(130);
        memoryChart.setStyle("CHART_COLOR_1: #3498db;");

        memorySeries = new XYChart.Series<>();
        memoryChart.getData().add(memorySeries);

        content.getChildren().addAll(heapBox, nonHeapUsageLabel, new Separator(), 
                cpuBox, new Separator(), memoryChart);
        panel.getChildren().add(content);
        
        return panel;
    }

    private VBox createStatisticsPanel() {
        VBox panel = createStyledPanel("📈 Estadísticas", "#f39c12");

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(15);
        statsGrid.setVgap(8);

        Label anomaliesLabel = new Label("Anomalías: 0");
        anomaliesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        Label avgTempLabel = new Label("Temp Promedio: --");
        avgTempLabel.setFont(Font.font("Arial", 11));
        
        Label avgHumLabel = new Label("Humedad Promedio: --");
        avgHumLabel.setFont(Font.font("Arial", 11));

        statsGrid.add(anomaliesLabel, 0, 0);
        statsGrid.add(avgTempLabel, 0, 1);
        statsGrid.add(avgHumLabel, 0, 2);

        panel.getChildren().add(statsGrid);
        return panel;
    }

    private VBox createEnhancedAlertsPanel() {
        VBox panel = createStyledPanel("🔔 Monitor de Alertas", "#e74c3c");

        alertsTextArea = new TextArea();
        alertsTextArea.setEditable(false);
        alertsTextArea.setPrefHeight(200);
        alertsTextArea.setMinHeight(180);
        alertsTextArea.setWrapText(true);
        alertsTextArea.setStyle("-fx-control-inner-background: #f8f9fa; " +
                "-fx-font-family: 'Consolas', 'Courier New', monospace; " +
                "-fx-font-size: 10px; -fx-text-fill: #2c3e50; " +
                "-fx-border-color: #dcdde1; -fx-border-radius: 5;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button clearButton = new Button("🗑 Limpiar");
        clearButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 6 16; -fx-font-size: 11px;");
        clearButton.setOnAction(e -> alertsTextArea.clear());

        Button saveButton = new Button("💾 Guardar");
        saveButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 6 16; -fx-font-size: 11px;");
        saveButton.setOnAction(e -> saveAlertLog());

        buttonBox.getChildren().addAll(clearButton, saveButton);

        panel.getChildren().addAll(alertsTextArea, buttonBox);
        VBox.setVgrow(alertsTextArea, Priority.ALWAYS);
        
        return panel;
    }

    private VBox createBottomPanel() {
        VBox bottomPanel = new VBox(5);
        bottomPanel.setPadding(new Insets(10));
        bottomPanel.setStyle("-fx-background-color: #2c3e50; -fx-background-radius: 8;");

        Label footerLabel = new Label("EcoMonitor Pro v2.0 | Desarrollado con JavaFX | © 2025");
        footerLabel.setFont(Font.font("Arial", 11));
        footerLabel.setTextFill(Color.web("#bdc3c7"));
        
        bottomPanel.setAlignment(Pos.CENTER);
        bottomPanel.getChildren().add(footerLabel);
        
        return bottomPanel;
    }

    private VBox createStyledPanel(String title, String accentColor) {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 3);");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titleLabel.setStyle("-fx-text-fill: " + accentColor + ";");

        panel.getChildren().addAll(titleLabel, new Separator());
        return panel;
    }

    private void addStyledInfoRow(GridPane grid, int row, String label, String value) {
        Label lblName = new Label(label);
        lblName.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        lblName.setStyle("-fx-text-fill: #7f8c8d;");

        Label lblValue = new Label(value);
        lblValue.setFont(Font.font("Arial", 11));
        lblValue.setStyle("-fx-text-fill: #2c3e50;");
        lblValue.setWrapText(true);

        grid.add(lblName, 0, row);
        grid.add(lblValue, 1, row);
    }

    private void startMonitoring() {
        System.out.println("▶ Iniciando monitoreo avanzado...");

        ecoSystem.startDataCollection();

        startButton.setDisable(true);
        stopButton.setDisable(false);
        statusLabel.setText("⚙ Estado: Monitoreando");
        
        // Animación de pulso en el estado
        FadeTransition pulse = new FadeTransition(Duration.millis(1000), statusLabel);
        pulse.setFromValue(1.0);
        pulse.setToValue(0.5);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();

        startTime = System.currentTimeMillis();
        lastUpdateTime = startTime;
        updatingUI = true;
        
        uiUpdateThread = new Thread(this::updateUILoop);
        uiUpdateThread.setDaemon(true);
        uiUpdateThread.start();

        addAlert("✓ Sistema iniciado - Monitoreo activo");
    }

    private void stopMonitoring() {
        System.out.println("⏹ Deteniendo monitoreo...");

        updatingUI = false;

        if (ecoSystem != null) {
            ecoSystem.stop();
        }

        startButton.setDisable(false);
        stopButton.setDisable(true);
        statusLabel.setText("⚙ Estado: Detenido");

        addAlert("⏹ Sistema detenido correctamente");
    }

    private void togglePause() {
        updatingUI = !updatingUI;
        addAlert(updatingUI ? "▶ Monitoreo reanudado" : "⏸ Monitoreo pausado");
    }

    private void updateUILoop() {
        while (updatingUI) {
            try {
                long currentTime = System.currentTimeMillis();
                long deltaTime = currentTime - lastUpdateTime;
                lastUpdateTime = currentTime;
                
                // Calcular FPS
                if (deltaTime > 0) {
                    currentFps = 1000.0 / deltaTime;
                }

                Thread.sleep(1000);

                Platform.runLater(() -> updateUI());

            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void updateUI() {
        timeCounter++;

        // Métricas básicas
        readingsLabel.setText("📊 Lecturas: " + ecoSystem.getTotalReadings());
        alertsLabel.setText("⚠ Alertas: " + ecoSystem.getAlertsGenerated());
        fpsLabel.setText(String.format("⚡ FPS: %.1f", currentFps));

        // Información de memoria
        updateMemoryInfo();

        // Información del sistema
        updateSystemInfo();

        // Tiempo de actividad
        updateUptime();

        // Sensores
        for (int sensorId = 1; sensorId <= 4; sensorId++) {
            updateSensorChart(sensorId);
        }

        // GC Info
        updateGCInfo();
    }

    private void updateMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        
        memoryLabel.setText("💾 Memoria: " + usedMemory + " MB");

        // Heap Memory
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        long heapUsed = heapUsage.getUsed() / 1024 / 1024;
        long heapMax = heapUsage.getMax() / 1024 / 1024;
        double heapPercent = (heapUsed * 100.0 / heapMax);
        
        heapUsageLabel.setText(String.format("Heap: %d MB / %d MB (%.1f%%)", 
                heapUsed, heapMax, heapPercent));
        memoryProgressBar.setProgress(heapUsed / (double) heapMax);

        // Cambiar color según uso
        if (heapPercent > 80) {
            memoryProgressBar.setStyle("-fx-accent: #e74c3c;");
        } else if (heapPercent > 60) {
            memoryProgressBar.setStyle("-fx-accent: #f39c12;");
        } else {
            memoryProgressBar.setStyle("-fx-accent: #27ae60;");
        }

        // Non-Heap
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
        long nonHeapUsed = nonHeapUsage.getUsed() / 1024 / 1024;
        nonHeapUsageLabel.setText(String.format("Non-Heap: %d MB", nonHeapUsed));

        // Actualizar gráfico de memoria
        memorySeries.getData().add(new XYChart.Data<>(timeCounter, heapUsed));
        if (memorySeries.getData().size() > MEMORY_CHART_POINTS) {
            memorySeries.getData().remove(0);
        }
    }

    private void updateSystemInfo() {
        // Hilos activos
        int threadCount = threadBean.getThreadCount();
        threadsLabel.setText("🧵 Hilos: " + threadCount);

        // CPU Load
        try {
            double cpuLoad = osBean.getSystemLoadAverage();
            if (cpuLoad >= 0) {
                double cpuPercent = (cpuLoad / Runtime.getRuntime().availableProcessors()) * 100;
                cpuPercent = Math.min(cpuPercent, 100);
                cpuLabel.setText(String.format("🖥 CPU: %.1f%%", cpuPercent));
                cpuProgressBar.setProgress(cpuPercent / 100.0);
                
                if (cpuPercent > 80) {
                    cpuProgressBar.setStyle("-fx-accent: #e74c3c;");
                } else if (cpuPercent > 50) {
                    cpuProgressBar.setStyle("-fx-accent: #f39c12;");
                } else {
                    cpuProgressBar.setStyle("-fx-accent: #27ae60;");
                }
            } else {
                cpuLabel.setText("🖥 CPU: N/A");
                cpuProgressBar.setProgress(0);
            }
        } catch (Exception e) {
            cpuLabel.setText("🖥 CPU: N/A");
        }
    }

    private void updateUptime() {
        long uptime = System.currentTimeMillis() - startTime;
        long seconds = (uptime / 1000) % 60;
        long minutes = (uptime / (1000 * 60)) % 60;
        long hours = (uptime / (1000 * 60 * 60));
        
        uptimeLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }

    private void updateGCInfo() {
        long totalGcCount = 0;
        GarbageCollectorMXBean[] gcBeans = null;
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            totalGcCount += gcBean.getCollectionCount();
        }
        
        if (totalGcCount > lastGcCount) {
            gcLabel.setText("🗑 GC: " + totalGcCount);
            lastGcCount = totalGcCount;
        } else {
            gcLabel.setText("🗑 GC: " + totalGcCount);
        }
    }

    private void updateSensorChart(int sensorId) {
        SensorReading reading = ecoSystem.getLatestReading(sensorId);

        if (reading != null) {
            XYChart.Series<Number, Number> series = sensorSeries.get(sensorId);
            Label valueLabel = sensorValueLabels.get(sensorId);
            Label statusLabel = sensorStatusLabels.get(sensorId);
            ProgressIndicator indicator = sensorIndicators.get(sensorId);

            if (series != null && valueLabel != null) {
                // Actualizar datos
                series.getData().add(new XYChart.Data<>(timeCounter, reading.getValue()));

                if (series.getData().size() > MAX_DATA_POINTS) {
                    series.getData().remove(0);
                }

                valueLabel.setText(String.format("%.1f", reading.getValue()));

                // Actualizar estadísticas
                updateSensorStatistics(sensorId, reading.getValue());

                // Verificar rangos
                Sensor sensor = ecoSystem.getSensor(sensorId);
                if (sensor != null && !sensor.isValueNormal(reading.getValue())) {
                    String color = getColorForSensor(sensorId);
                    valueLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    statusLabel.setText("● Fuera de rango");
                    statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                    
                    // Mostrar indicador de carga
                    indicator.setVisible(true);
                    
                    // Animación de alerta
                    FadeTransition alert = new FadeTransition(Duration.millis(500), valueLabel);
                    alert.setFromValue(1.0);
                    alert.setToValue(0.3);
                    alert.setCycleCount(2);
                    alert.setAutoReverse(true);
                    alert.play();
                    
                    totalAnomalies++;
                    addAlert(String.format("⚠ %s: %.1f fuera de rango [%.0f-%.0f]",
                            reading.getSensorType(), reading.getValue(),
                            sensor.getMinThreshold(), sensor.getMaxThreshold()));
                } else {
                    String originalColor = getColorForSensor(sensorId);
                    valueLabel.setStyle("-fx-text-fill: " + originalColor + "; -fx-font-weight: bold;");
                    statusLabel.setText("● En línea");
                    statusLabel.setStyle("-fx-text-fill: #27ae60;");
                    indicator.setVisible(false);
                }
            }
        } else {
            // Sin datos
            Label statusLabel = sensorStatusLabels.get(sensorId);
            if (statusLabel != null) {
                statusLabel.setText("● Sin datos");
                statusLabel.setStyle("-fx-text-fill: #95a5a6;");
            }
        }
    }

    private void updateSensorStatistics(int sensorId, double value) {
        sensorAverages.putIfAbsent(sensorId, 0.0);
        sensorReadCount.putIfAbsent(sensorId, 0);
        
        double currentAvg = sensorAverages.get(sensorId);
        int count = sensorReadCount.get(sensorId);
        
        double newAvg = (currentAvg * count + value) / (count + 1);
        
        sensorAverages.put(sensorId, newAvg);
        sensorReadCount.put(sensorId, count + 1);
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
        Platform.runLater(() -> {
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
        });
    }

    private void resetCharts() {
        timeCounter = 0;
        totalAnomalies = 0;
        sensorAverages.clear();
        sensorReadCount.clear();
        
        for (XYChart.Series<Number, Number> series : sensorSeries.values()) {
            series.getData().clear();
        }
        
        if (memorySeries != null) {
            memorySeries.getData().clear();
        }
        
        addAlert("🔄 Gráficos y estadísticas reiniciados");
        
        // Animación de reinicio
        for (LineChart<Number, Number> chart : sensorCharts.values()) {
            RotateTransition rotate = new RotateTransition(Duration.millis(500), chart);
            rotate.setByAngle(360);
            rotate.play();
        }
    }

    private void exportData(Stage stage) throws com.itextpdf.text.DocumentException {
    addAlert("💾 Exportando datos del sistema...");

    // 1️⃣ Crear el contenido del reporte
    StringBuilder export = new StringBuilder();
    export.append("=== REPORTE ECOMONITOR PRO ===\n");
    export.append("Fecha: ").append(java.time.LocalDateTime.now()).append("\n");
    export.append("Tiempo de monitoreo: ").append(uptimeLabel.getText()).append("\n");
    export.append("Total de lecturas: ").append(ecoSystem.getTotalReadings()).append("\n");
    export.append("Total de alertas: ").append(ecoSystem.getAlertsGenerated()).append("\n");
    export.append("Anomalías detectadas: ").append(totalAnomalies).append("\n\n");

    export.append("=== PROMEDIOS DE SENSORES ===\n");
    for (Map.Entry<Integer, Double> entry : sensorAverages.entrySet()) {
        export.append(String.format("Sensor %d: %.2f\n", entry.getKey(), entry.getValue()));
    }

    export.append("\n=== LOG DE ALERTAS ===\n");
    export.append(alertsTextArea.getText());

    // 2️⃣ Mostrar diálogo para elegir ubicación del archivo
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Guardar Reporte PDF");
    fileChooser.getExtensionFilters().add(
        new FileChooser.ExtensionFilter("Archivo PDF (*.pdf)", "*.pdf")
    );
    fileChooser.setInitialFileName("Reporte_EcoMonitor_" +
            java.time.LocalDate.now() + ".pdf");

    java.io.File file = fileChooser.showSaveDialog(stage);
    if (file == null) {
        addAlert("❌ Exportación cancelada por el usuario.");
        return;
    }

    // 3️⃣ Generar PDF con iText
    // 3️⃣ Generar PDF con OpenPDF (basado en iText 2.1.7 / com.lowagie)
try {
    com.itextpdf.text.Document document = new com.itextpdf.text.Document();
    PdfWriter.getInstance(document, new FileOutputStream(file));
    document.open();

    // Encabezado simple (sin fuente personalizada)
    Paragraph title = new Paragraph("Reporte EcoMonitor PRO");
    title.setAlignment(Element.ALIGN_CENTER);
    document.add((com.itextpdf.text.Element) title);
    document.add((com.itextpdf.text.Element) new Paragraph("\n"));

    // Contenido principal simple
    Paragraph content = new Paragraph(export.toString());
    document.add((com.itextpdf.text.Element) content);

    document.close();

    addAlert("✅ Reporte PDF exportado correctamente: " + file.getAbsolutePath());
} catch (DocumentException | IOException e) {
    addAlert("❌ Error al generar el PDF: " + e.getMessage());
    e.printStackTrace();
}



}

    private void saveAlertLog() {
        addAlert("💾 Guardando log de alertas...");
        String log = alertsTextArea.getText();
        System.out.println("=== LOG DE ALERTAS ===\n" + log);
        addAlert("✓ Log guardado");
    }

    public static void main(String[] args) {
        launch(args);
    }
}