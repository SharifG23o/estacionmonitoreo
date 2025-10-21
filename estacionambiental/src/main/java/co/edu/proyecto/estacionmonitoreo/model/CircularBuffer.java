package co.edu.proyecto.estacionmonitoreo.model;

import java.util.ArrayList;
import java.util.List;

public class CircularBuffer<T> {
    private final Object[] buffer;
    private volatile int head = 0;
    private volatile int tail = 0;
    private volatile int size = 0;
    private final int capacity;
    private final Object lock = new Object();

    public CircularBuffer(int capacity) {
        this.capacity = capacity;
        this.buffer = new Object[capacity];
        System.out.println("📦 Buffer circular creado con capacidad: " + capacity);
    }

    public void add(T item) {
        synchronized (lock) {
            buffer[tail] = item;

            if (size == capacity) {
                // Buffer lleno, sobrescribir el más antiguo
                head = (head + 1) % capacity;
            } else {
                size++;
            }

            tail = (tail + 1) % capacity;
        }
    }

    @SuppressWarnings("unchecked")
    public List<T> getRecentReadings(int count) {
        synchronized (lock) {
            List<T> result = new ArrayList<T>();
            int itemsToGet = Math.min(count, size);

            for (int i = 0; i < itemsToGet; i++) {
                int index = (head + size - itemsToGet + i) % capacity;
                result.add((T) buffer[index]);
            }

            return result;
        }
    }

    @SuppressWarnings("unchecked")
    public T getLatest() {
        synchronized (lock) {
            if (size == 0) return null;
            int latestIndex = (tail - 1 + capacity) % capacity;
            return (T) buffer[latestIndex];
        }
    }

    public int getCurrentSize() {
        return size;
    }

    public boolean isFull() {
        return size == capacity;
    }

    public void printMemoryUsage() {
        synchronized (lock) {
            if (size > 0 && buffer[head] instanceof SensorReading) {
                SensorReading sample = (SensorReading) buffer[head];
                int approxItemSize = sample.getApproximateSize();
                int totalMemory = approxItemSize * size;

                System.out.printf("🔍 Buffer: %d/%d elementos, ~%d bytes usados%n",
                        size, capacity, totalMemory);
            }
        }
    }
}
