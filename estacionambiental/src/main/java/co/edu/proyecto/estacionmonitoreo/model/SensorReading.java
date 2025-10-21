package co.edu.proyecto.estacionmonitoreo.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//  Clase que representa una lectura de sensor
public class SensorReading {
    private long timestamp;        // 8 bytes
    private float value;          // 4 bytes
    private int sensorId;        // 4 bytes
    private String sensorType;   // referencia 8 bytes + contenido variable

    public SensorReading(int sensorId, String sensorType, float value) {
        this.timestamp = System.currentTimeMillis();
        this.sensorId = sensorId;
        this.sensorType = sensorType;
        this.value = value;
    }

    // Constructor para reutilización en pool
    public SensorReading() {
        this(0, "", 0.0f);
    }

    // Método para reutilizar objeto existente
    public void reset(int sensorId, String sensorType, float value) {
        this.timestamp = System.currentTimeMillis();
        this.sensorId = sensorId;
        this.sensorType = sensorType;
        this.value = value;
    }

    public int getApproximateSize() {
        // Object header (12 bytes) + long (8) + float (4) + int (4) + String reference (8)
        // + String content (sensorType.length() * 2 + 40 bytes String overhead)
        int baseSize = 36; // Object + primitives + reference
        int stringSize = (sensorType != null) ? sensorType.length() * 2 + 40 : 0;
        return baseSize + stringSize;
    }

    // Getters
    public long getTimestamp() { return timestamp; }
    public float getValue() { return value; }
    public int getSensorId() { return sensorId; }
    public String getSensorType() { return sensorType; }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return String.format("[%s] Sensor %d (%s): %.2f",
                LocalDateTime.now().format(formatter),
                sensorId, sensorType, value);
    }
}
