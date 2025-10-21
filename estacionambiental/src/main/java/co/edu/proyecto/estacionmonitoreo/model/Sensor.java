package co.edu.proyecto.estacionmonitoreo.model;


import java.util.Random;

public class Sensor {
    private int id;
    private String type;
    private float currentValue;
    private float minThreshold;
    private float maxThreshold;
    private boolean active;
    private Random random;
    private float baseValue; // Valor base para simulación realista

    public Sensor(int id, String type, float minThreshold, float maxThreshold) {
        this.id = id;
        this.type = type;
        this.minThreshold = minThreshold;
        this.maxThreshold = maxThreshold;
        this.active = true;
        this.random = new Random();

        // Establecer valor base realista según tipo de sensor
        this.baseValue = (minThreshold + maxThreshold) / 2;
        this.currentValue = baseValue;

        System.out.println("✅ Sensor creado: " + type + " (ID: " + id +
                ") Rango: [" + minThreshold + ", " + maxThreshold + "]");
    }

    /**
     * Simula lectura del sensor con variación realista
     */
    public SensorReading readValue() {
        if (!active) {
            return null;
        }

        // Simular variación natural del sensor (±5% del rango)
        float range = maxThreshold - minThreshold;
        float variation = (random.nextFloat() - 0.5f) * (range * 0.1f);

        // Aplicar variación gradual para simular cambios ambientales reales
        currentValue += variation;

        // Mantener dentro de límites físicos razonables
        float physicalMin = minThreshold - (range * 0.2f);
        float physicalMax = maxThreshold + (range * 0.2f);
        currentValue = Math.max(physicalMin, Math.min(physicalMax, currentValue));

        return new SensorReading(id, type, currentValue);
    }

    public boolean isValueNormal(float value) {
        return value >= minThreshold && value <= maxThreshold;
    }

    // Getters
    public int getId() { return id; }
    public String getType() { return type; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public float getMinThreshold() { return minThreshold; }
    public float getMaxThreshold() { return maxThreshold; }
}
