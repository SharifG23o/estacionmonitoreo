package co.edu.proyecto.estacionmonitoreo.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import co.edu.proyecto.estacionmonitoreo.model.*;



public class EcoMonitorSystem {
    private static final int MAX_READINGS_PER_SENSOR = 1000;
    private static final int MEMORY_CHECK_INTERVAL = 5000; // 5 segundos
    private static final int DATA_COLLECTION_INTERVAL = 1000; // 1 segundo

    private final Map<Integer, Sensor> sensors;
    private final Map<Integer, CircularBuffer<SensorReading>> sensorData;
    private final MemoryMonitor memoryMonitor;
    private final SensorReadingPool readingPool;
    private final List<String> alerts;

    private volatile boolean running;
    private Thread dataThread;
    private Thread memoryThread;

    // Estadísticas del sistema
    private final AtomicLong totalReadings = new AtomicLong(0);
    private final AtomicLong alertsGenerated = new AtomicLong(0);
    private long startTime;

    public EcoMonitorSystem() {
        sensors = new HashMap<>();
        sensorData = new HashMap<Integer, CircularBuffer<SensorReading>>();
        memoryMonitor = new MemoryMonitor();
        readingPool = new SensorReadingPool(200);
        alerts = new ArrayList<>();
        running = false;

        System.out.println("🌱 EcoMonitor System inicializando...");
        setupDefaultSensors();
        startTime = System.currentTimeMillis();

        System.out.println("✅ Sistema EcoMonitor listo para operación");
    }

    private void setupDefaultSensors() {
        System.out.println("🔧 Configurando sensores por defecto...");

        addSensor(1, "TEMPERATURE", 18.0f, 26.0f);    // Temperatura confort (°C)
        addSensor(2, "HUMIDITY", 40.0f, 60.0f);       // Humedad confort (%)
        addSensor(3, "CO2", 400.0f, 800.0f);          // CO2 interior (ppm)
        addSensor(4, "NOISE", 35.0f, 55.0f);          // Ruido oficina (dB)
    }

    public void addSensor(int id, String type, float minThreshold, float maxThreshold) {
        Sensor sensor = new Sensor(id, type, minThreshold, maxThreshold);
        CircularBuffer<SensorReading> buffer = new CircularBuffer<SensorReading>(MAX_READINGS_PER_SENSOR);

        sensors.put(id, sensor);
        sensorData.put(id, buffer);

        System.out.printf("➕ Sensor agregado: %s (Buffer: %d lecturas máx)%n",
                type, MAX_READINGS_PER_SENSOR);
    }

    public void startDataCollection() {
        if (running) {
            System.out.println("⚠️ Sistema ya está ejecutándose");
            return;
        }

        System.out.println("🚀 Iniciando recolección de datos...");
        running = true;

        // Hilo principal de recolección de datos
        dataThread = new Thread(this::dataCollectionLoop, "DataCollection");
        dataThread.setDaemon(true);
        dataThread.start();

        // Hilo de monitoreo de memoria
        memoryThread = new Thread(this::memoryMonitoringLoop, "MemoryMonitor");
        memoryThread.setDaemon(true);
        memoryThread.start();

        System.out.println("✅ Hilos de recolección iniciados");
    }

    private void dataCollectionLoop() {
        System.out.println("📊 Loop de recolección iniciado");

        while (running) {
            try {
                // Procesar cada sensor activo
                for (Sensor sensor : sensors.values()) {
                    if (!sensor.isActive()) continue;

                    // 1. Leer valor del sensor
                    SensorReading reading = sensor.readValue();
                    if (reading == null) continue;

                    // 2. Almacenar en buffer circular
                    CircularBuffer<SensorReading> buffer = sensorData.get(sensor.getId());
                    buffer.add(reading);

                    totalReadings.incrementAndGet();

                    // 3. Verificar si necesita alerta
                    if (!sensor.isValueNormal(reading.getValue())) {
                        generateAlert(sensor, reading);
                    }

                    // 4. Debug ocasional
                    if (totalReadings.get() % 100 == 0) {
                        System.out.printf("📈 Lecturas procesadas: %d%n", totalReadings.get());
                    }
                }

                Thread.sleep(DATA_COLLECTION_INTERVAL);

            } catch (InterruptedException e) {
                System.out.println("🛑 Hilo de recolección interrumpido");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("❌ Error en recolección: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("🏁 Loop de recolección terminado");
    }

    private void memoryMonitoringLoop() {
        System.out.println("🖥️ Monitor de memoria iniciado");

        while (running) {
            try {
                MemoryStatusInfo memStatus = memoryMonitor.checkMemoryStatus();

                switch (memStatus.status) {
                    case WARNING:
                        handleMemoryWarning(memStatus);
                        break;

                    case CRITICAL:
                        handleMemoryCritical(memStatus);
                        break;

                    case OK:
                    default:
                        // Todo normal, ocasionalmente mostrar estado
                        if (System.currentTimeMillis() % 30000 < MEMORY_CHECK_INTERVAL) {
                            System.out.println("💚 " + memStatus.getFormattedInfo());
                        }
                        break;
                }

                Thread.sleep(MEMORY_CHECK_INTERVAL);

            } catch (InterruptedException e) {
                System.out.println("🛑 Monitor de memoria interrumpido");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("❌ Error en monitor de memoria: " + e.getMessage());
            }
        }

        System.out.println("🏁 Monitor de memoria terminado");
    }

    private void handleMemoryWarning(MemoryStatusInfo memStatus) {
        System.out.println("⚠️ " + memStatus.getFormattedInfo());
        System.out.println("🔧 Aplicando optimizaciones preventivas...");

        // Reducir frecuencia de recolección temporalmente
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Limpiar alertas antiguas
        if (alerts.size() > 50) {
            alerts.subList(0, alerts.size() - 25).clear();
            System.out.println("🧹 Limpiadas alertas antiguas");
        }
    }

    private void handleMemoryCritical(MemoryStatusInfo memStatus) {
        System.out.println("🚨 " + memStatus.getFormattedInfo());
        System.out.println("🆘 MEMORIA CRÍTICA - Ejecutando limpieza de emergencia...");

        // Limpieza agresiva
        memoryMonitor.performEmergencyCleanup();

        // Desactivar temporalmente sensores no críticos
        sensors.values().stream()
                .filter(s -> s.getType().equals("NOISE"))
                .forEach(s -> {
                    s.setActive(false);
                    System.out.println("⏸️ Sensor " + s.getType() + " desactivado temporalmente");
                });

        // Reducir drásticamente las alertas
        alerts.clear();

        // Pausa más larga para permitir GC
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Reactivar sensores
        sensors.values().forEach(s -> s.setActive(true));
        System.out.println("🔄 Sensores reactivados");
    }

    private void generateAlert(Sensor sensor, SensorReading reading) {
        String alertMsg = String.format("🚨 ALERTA: %s fuera de rango normal [%.1f-%.1f]: %.2f",
                sensor.getType(), sensor.getMinThreshold(),
                sensor.getMaxThreshold(), reading.getValue());

        alerts.add(alertMsg);
        alertsGenerated.incrementAndGet();

        System.out.println(alertMsg);

        // Limitar alertas para evitar spam y uso excesivo de memoria
        if (alerts.size() > 100) {
            alerts.subList(0, 50).clear();
        }
    }

    public void printSystemStatus() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📊 ESTADO DEL SISTEMA ECOMONITOR");
        System.out.println("=".repeat(60));

        // Tiempo de ejecución
        long runTime = (System.currentTimeMillis() - startTime) / 1000;
        System.out.printf("⏱️ Tiempo de ejecución: %d minutos, %d segundos%n",
                runTime / 60, runTime % 60);

        // Estadísticas generales
        System.out.printf("📈 Total lecturas: %d%n", totalReadings.get());
        System.out.printf("🚨 Alertas generadas: %d%n", alertsGenerated.get());
        System.out.printf("📊 Lecturas por segundo: %.1f%n",
                totalReadings.get() / Math.max(runTime, 1.0));

        // Estado de memoria
        MemoryStatusInfo memStatus = memoryMonitor.checkMemoryStatus();
        System.out.println("🖥️ " + memStatus.getFormattedInfo());
        System.out.printf("🧹 Limpiezas GC sugeridas: %d%n", memoryMonitor.getGCSuggestions());

        // Pool de objetos
        System.out.println("🔄 " + readingPool.getStats());

        // Estado de sensores
        System.out.println("\n📡 SENSORES:");
        for (Sensor sensor : sensors.values()) {
            CircularBuffer<SensorReading> buffer = sensorData.get(sensor.getId());
            SensorReading latest = buffer.getLatest();

            String status = sensor.isActive() ? "🟢 ACTIVO" : "🔴 INACTIVO";
            String value = (latest != null) ? String.format("%.2f", latest.getValue()) : "N/A";
            String normalStatus = (latest != null && sensor.isValueNormal(latest.getValue()))
                    ? "✅" : "⚠️";

            System.out.printf("  %s %s (ID:%d): %s %s [%.1f-%.1f] Buffer: %d/%d%n",
                    normalStatus, sensor.getType(), sensor.getId(), value, status,
                    sensor.getMinThreshold(), sensor.getMaxThreshold(),
                    buffer.getCurrentSize(), MAX_READINGS_PER_SENSOR);
        }

        // Alertas recientes
        if (!alerts.isEmpty()) {
            System.out.println("\n🚨 ÚLTIMAS ALERTAS:");
            alerts.stream().skip(Math.max(0, alerts.size() - 3))
                    .forEach(alert -> System.out.println("  " + alert));
        }

        // Uso de memoria por buffers
        System.out.println("\n💾 USO DE MEMORIA POR BUFFERS:");
        for (Map.Entry<Integer, CircularBuffer<SensorReading>> entry : sensorData.entrySet()) {
            System.out.printf("  Sensor %d: ", entry.getKey());
            entry.getValue().printMemoryUsage();
        }

        System.out.println("=".repeat(60) + "\n");
    }

    public void stop() {
        if (!running) {
            System.out.println("⚠️ Sistema ya está detenido");
            return;
        }

        System.out.println("🛑 Deteniendo sistema EcoMonitor...");
        running = false;

        // Esperar que los hilos terminen
        try {
            if (dataThread != null) {
                dataThread.join(2000);
            }
            if (memoryThread != null) {
                memoryThread.join(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("✅ Sistema EcoMonitor detenido correctamente");
        printFinalStats();
    }

    private void printFinalStats() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("📈 ESTADÍSTICAS FINALES");
        System.out.println("=".repeat(50));

        long totalTime = (System.currentTimeMillis() - startTime) / 1000;
        System.out.printf("⏱️ Tiempo total: %d segundos%n", totalTime);
        System.out.printf("📊 Lecturas totales: %d%n", totalReadings.get());
        System.out.printf("🚨 Alertas totales: %d%n", alertsGenerated.get());
        System.out.printf("💻 Limpieza GC: %d veces%n", memoryMonitor.getGCSuggestions());

        if (totalTime > 0) {
            System.out.printf("⚡ Promedio: %.1f lecturas/segundo%n",
                    totalReadings.get() / (double)totalTime);
        }

        System.out.println("🔄 " + readingPool.getStats());
        System.out.println("=".repeat(50));
    }

    // Getters para testing
    public int getSensorCount() { return sensors.size(); }
    public long getTotalReadings() { return totalReadings.get(); }
    public long getAlertsGenerated() { return alertsGenerated.get(); }
    public boolean isRunning() { return running; }

    // Metodos para que la GUI pueda acceder a los datos
    public Sensor getSensor(int id) {
        return sensors.get(id);
    }

    public SensorReading getLatestReading(int sensorId) {
        CircularBuffer<SensorReading> buffer = sensorData.get(sensorId);
        if (buffer != null) {
            return buffer.getLatest();
        }
        return null;
    }

    public Map<Integer, Sensor> getAllSensors() {
        return sensors;
    }

    public void shutdown() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'shutdown'");
    }
}