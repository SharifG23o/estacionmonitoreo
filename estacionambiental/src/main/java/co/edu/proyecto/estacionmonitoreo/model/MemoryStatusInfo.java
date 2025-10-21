package co.edu.proyecto.estacionmonitoreo.model;

public class MemoryStatusInfo {
    public final MemoryStatus status;
    public final long usedMemory;
    public final long totalMemory;
    public final long maxMemory;
    public final long freeMemory;

    public MemoryStatusInfo(MemoryStatus status, long used, long total, long max, long free) {
        this.status = status;
        this.usedMemory = used;
        this.totalMemory = total;
        this.maxMemory = max;
        this.freeMemory = free;
    }

    public double getUsagePercentage() {
        return (usedMemory * 100.0) / totalMemory;
    }

    public String getFormattedInfo() {
        return String.format("%s - Usado: %.2f MB (%.1f%%), Libre: %.2f MB",
                status, usedMemory/1024.0/1024.0, getUsagePercentage(),
                freeMemory/1024.0/1024.0);
    }
}
