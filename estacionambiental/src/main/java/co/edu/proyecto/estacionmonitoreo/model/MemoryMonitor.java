package co.edu.proyecto.estacionmonitoreo.model;


import java.util.concurrent.atomic.AtomicLong;

public class MemoryMonitor {
    private final Runtime runtime;
    private final long maxMemory;
    private final long warningThreshold;
    private final long criticalThreshold;
    private final AtomicLong gcSuggestions = new AtomicLong(0);

    public MemoryMonitor() {
        runtime = Runtime.getRuntime();
        maxMemory = runtime.maxMemory();
        warningThreshold = (long) (maxMemory * 0.75); // 75%
        criticalThreshold = (long) (maxMemory * 0.90); // 90%

        System.out.printf("🖥️ Monitor de memoria inicializado. Máximo: %.2f MB%n",
                maxMemory / 1024.0 / 1024.0);
    }

    public MemoryStatusInfo checkMemoryStatus() {
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        MemoryStatus status;
        if (usedMemory >= criticalThreshold) {
            status = MemoryStatus.CRITICAL;
        } else if (usedMemory >= warningThreshold) {
            status = MemoryStatus.WARNING;
        } else {
            status = MemoryStatus.OK;
        }

        return new MemoryStatusInfo(status, usedMemory, totalMemory, maxMemory, freeMemory);
    }

    public void performEmergencyCleanup() {
        System.out.println("🧹 Ejecutando limpieza de emergencia...");
        gcSuggestions.incrementAndGet();

        // Sugerir GC múltiples veces para ser más efectivo
        for (int i = 0; i < 3; i++) {
            System.gc();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        // Verificar si mejoró
        MemoryStatusInfo afterCleanup = checkMemoryStatus();
        System.out.printf("📊 Post-limpieza: %s (%.1f%% usado)%n",
                afterCleanup.status, afterCleanup.getUsagePercentage());
    }

    public long getGCSuggestions() {
        return gcSuggestions.get();
    }
}
