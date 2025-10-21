package co.edu.proyecto.estacionmonitoreo.model;

import java.util.concurrent.ConcurrentLinkedQueue;

public class SensorReadingPool {
    private final ConcurrentLinkedQueue<SensorReading> pool;
    private final int maxSize;
    private volatile int createdCount = 0;
    private volatile int reuseCount = 0;

    public SensorReadingPool(int maxSize) {
        this.maxSize = maxSize;
        this.pool = new ConcurrentLinkedQueue<>();

        // Pre-crear algunos objetos para evitar latencia inicial
        int preCreateCount = Math.min(20, maxSize);
        for (int i = 0; i < preCreateCount; i++) {
            pool.offer(new SensorReading());
            createdCount++;
        }

        System.out.println("🔄 Pool de objetos creado. Pre-creados: " + preCreateCount);
    }

    public SensorReading acquire(int sensorId, String sensorType, float value) {
        SensorReading reading = pool.poll();

        if (reading == null) {
            // Pool vacío, crear nuevo si no hemos alcanzado el límite
            if (createdCount < maxSize) {
                reading = new SensorReading(sensorId, sensorType, value);
                createdCount++;
            } else {
                // Límite alcanzado, crear temporal (no va al pool)
                return new SensorReading(sensorId, sensorType, value);
            }
        } else {
            // Reutilizar objeto del pool
            reading.reset(sensorId, sensorType, value);
            reuseCount++;
        }

        return reading;
    }

    public void release(SensorReading reading) {
        if (reading != null && pool.size() < maxSize) {
            // Opcional: limpiar datos sensibles antes de reutilizar
            pool.offer(reading);
        }
    }

    public String getStats() {
        return String.format("Pool: %d disponibles, %d creados, %d reutilizados (%.1f%% reúso)",
                pool.size(), createdCount, reuseCount,
                createdCount > 0 ? (reuseCount * 100.0 / createdCount) : 0);
    }
}
