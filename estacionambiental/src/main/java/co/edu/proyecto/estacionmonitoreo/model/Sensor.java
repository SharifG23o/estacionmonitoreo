package co.edu.proyecto.estacionmonitoreo.model;

import co.edu.proyecto.estacionmonitoreo.controller.ThingspeakClient;

public class Sensor {
    private int id;
    private String type;
    private float minThreshold;
    private float maxThreshold;
    private boolean active;

    // Configuración para ThingSpeak
    private int channelId;
    private int fieldNumber;

    public Sensor(int id, String type, float minThreshold, float maxThreshold) {
        this.id = id;
        this.type = type;
        this.minThreshold = minThreshold;
        this.maxThreshold = maxThreshold;
        this.active = true;

        System.out.println("✅ Sensor creado: " + type + " (ID: " + id +
                ") Rango: [" + minThreshold + ", " + maxThreshold + "]");
    }

    /** Configura el canal y campo de ThingSpeak para este sensor */
    public void configureThingSpeak(int channelId, int fieldNumber) {
        this.channelId = channelId;
        this.fieldNumber = fieldNumber;
        System.out.printf("🌐 Sensor %s vinculado a ThingSpeak (Canal %d, Campo %d)%n",
                type, channelId, fieldNumber);
    }

    /** Lee el valor actual desde ThingSpeak */
    public SensorReading readValue() {
        if (!active) return null;

        float value = ThingspeakClient.getLatestFieldValue(channelId, fieldNumber);
        if (Float.isNaN(value)) {
            System.out.printf("⚠ Sin datos válidos de ThingSpeak para %s (Campo %d)%n", type, fieldNumber);
            return null;
        }

        return new SensorReading(id, type, value);
    }

    public boolean isValueNormal(float value) {
        return value >= minThreshold && value <= maxThreshold;
    }

    // --- Getters y Setters ---
    public int getId() { return id; }
    public String getType() { return type; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public float getMinThreshold() { return minThreshold; }
    public float getMaxThreshold() { return maxThreshold; }
}
