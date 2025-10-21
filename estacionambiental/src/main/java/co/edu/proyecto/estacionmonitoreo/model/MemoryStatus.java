package co.edu.proyecto.estacionmonitoreo.model;


public enum MemoryStatus {
    OK("✅ Normal"),
    WARNING("⚠️ Advertencia"),
    CRITICAL("🚨 Crítico");

    private final String displayName;
    MemoryStatus(String displayName) { this.displayName = displayName; }

    @Override
    public String toString() { return displayName; }
}
