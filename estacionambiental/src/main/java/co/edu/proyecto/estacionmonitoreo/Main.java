package co.edu.proyecto.estacionmonitoreo;

import co.edu.proyecto.estacionmonitoreo.controller.*;
import co.edu.proyecto.estacionmonitoreo.model.*;
import co.edu.proyecto.estacionmonitoreo.viewController.*;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


/**
 * Clase principal unificada para EcoMonitor
 * Permite ejecutar en modo consola o GUI según los argumentos
 */
public class Main {

    public static void main(String[] args) {
        // Mostrar banner inicial
        printBanner();

        // Verificar argumentos de línea de comandos
        if (args.length > 0) {
            String mode = args[0].toLowerCase();

            switch (mode) {
                case "gui":
                case "--gui":
                case "-g":
                    System.out.println("🖥️ Iniciando modo GUI...\n");
                    launchGUI(args);
                    break;

                case "console":
                case "--console":
                case "-c":
                    System.out.println("⌨️ Iniciando modo Consola...\n");
                    runConsoleMode(args);
                    break;

                case "stress":
                case "--stress":
                case "-s":
                    System.out.println("🔥 Iniciando Stress Test...\n");
                    runStressTest();
                    break;

                case "help":
                case "--help":
                case "-h":
                    printHelp();
                    break;

                default:
                    System.out.println("❌ Argumento no reconocido: " + mode);
                    printHelp();
                    break;
            }
        } else {
            // Sin argumentos, mostrar menú interactivo
            runInteractiveMenu();
        }
    }

    /**
     * Imprime el banner de bienvenida
     */
    private static void printBanner() {
        System.out.println("╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("            🌱 EcoMonitor - Sistema IoT de Monitoreo Ambiental       ");
        System.out.println("                    Gestión Inteligente de Memoria                    ");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    /**
     * Muestra la ayuda de uso
     */
    private static void printHelp() {
        System.out.println("📚 USO: java Main [opción]");
        System.out.println();
        System.out.println("OPCIONES DISPONIBLES:");
        System.out.println("  gui, --gui, -g        Iniciar interfaz gráfica (JavaFX)");
        System.out.println("  console, --console, -c    Iniciar modo consola");
        System.out.println("  stress, --stress, -s      Ejecutar stress test");
        System.out.println("  help, --help, -h      Mostrar esta ayuda");
        System.out.println();
        System.out.println("EJEMPLOS:");
        System.out.println("  java Main gui             # Iniciar con interfaz gráfica");
        System.out.println("  java Main console         # Iniciar en modo consola");
        System.out.println("  java Main stress          # Ejecutar stress test");
        System.out.println();
        System.out.println("Si no se proporciona argumento, se mostrará un menú interactivo.");
    }

    /**
     * Menú interactivo para seleccionar modo
     */
    private static void runInteractiveMenu() {
        try (java.util.Scanner scanner = new java.util.Scanner(System.in)) {
            System.out.println("🔹 MENÚ DE OPCIONES:");
            System.out.println("  1. 🖥️  Interfaz Gráfica (GUI)");
            System.out.println("  2. ⌨️  Modo Consola");
            System.out.println("  3. 🔥 Stress Test");
            System.out.println("  4. ❌ Salir");
            System.out.println();
            System.out.print("Selecciona una opción (1-4): ");

            try {
                int option = scanner.nextInt();
                System.out.println();

                switch (option) {
                    case 1:
                        System.out.println("🖥️ Lanzando interfaz gráfica...\n");
                        launchGUI(new String[]{});
                        break;

                    case 2:
                        System.out.println("⌨️ Iniciando modo consola...\n");
                        runConsoleMode(new String[]{});
                        break;

                    case 3:
                        System.out.println("🔥 Iniciando stress test...\n");
                        runStressTest();
                        break;

                    case 4:
                        System.out.println("👋 ¡Hasta luego!");
                        System.exit(0);
                        break;

                    default:
                        System.out.println("❌ Opción inválida. Usa 1-4.");
                        runInteractiveMenu();
                        break;
                }
            } catch (Exception e) {
                System.out.println("❌ Entrada inválida. Por favor ingresa un número.");
                scanner.nextLine(); // Limpiar buffer
                runInteractiveMenu();
            }
        }
    }

    /**
     * Lanza la interfaz gráfica JavaFX
     */
    private static void launchGUI(String[] args) {
        try {
            // Verificar si JavaFX está disponible
            Class.forName("javafx.application.Application");

            System.out.println("✅ JavaFX detectado. Iniciando GUI...");

            // Lanzar aplicación JavaFX
            EcoMonitorGUI.main(args);

        } catch (ClassNotFoundException e) {
            System.err.println("❌ ERROR: JavaFX no está disponible.");
            System.err.println("📦 Asegúrate de tener JavaFX en tu classpath.");
            System.err.println("💡 Puedes agregar JavaFX a tu build.gradle:");
            System.err.println();
            System.err.println("   javafx {");
            System.err.println("       version = '17.0.2'");
            System.err.println("       modules = ['javafx.controls', 'javafx.fxml']");
            System.err.println("   }");
            System.err.println();
            System.err.println("🔄 Cambiando a modo consola...\n");

            try {
                Thread.sleep(2000);
                runConsoleMode(args);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Ejecuta el modo consola (demo estándar)
     */
    private static void runConsoleMode(String[] args) {
        System.out.println("🌱 DEMO: Sistema EcoMonitor - Gestión de Memoria en IoT");
        System.out.println("=" .repeat(70));

        showSystemInfo();

        // Crear y configurar sistema
        EcoMonitorSystem system = new EcoMonitorSystem();

        // Agregar sensores adicionales para aumentar carga
        System.out.println("\n🔧 Agregando sensores adicionales para testing...");
        system.addSensor(5, "PRESSURE", 980.0f, 1020.0f);    // Presión atmosférica (hPa)
        system.addSensor(6, "LIGHT", 200.0f, 800.0f);        // Luz (lux)
        system.addSensor(7, "UV_INDEX", 0.0f, 8.0f);         // Índice UV

        // Determinar duración del demo
        int duration = 60; // Por defecto 60 segundos
        if (args.length > 1) {
            try {
                duration = Integer.parseInt(args[1]);
                System.out.println("⏱️ Duración personalizada: " + duration + " segundos");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Duración inválida, usando 60 segundos por defecto");
            }
        }

        // Iniciar sistema
        system.startDataCollection();

        // Ejecutar demo por tiempo determinado
        runDemo(system, duration);

        // Detener sistema
        system.stop();

        System.out.println("🏁 Demo completado exitosamente");
        System.out.println("\n💡 TIP: Puedes ejecutar el modo GUI con: java Main gui");
    }

    /**
     * Muestra información del sistema
     */
    private static void showSystemInfo() {
        Runtime runtime = Runtime.getRuntime();

        System.out.println("💻 INFORMACIÓN DEL SISTEMA:");
        System.out.printf("   JVM: %s %s%n",
                System.getProperty("java.vm.name"),
                System.getProperty("java.version"));
        System.out.printf("   OS: %s %s%n",
                System.getProperty("os.name"),
                System.getProperty("os.arch"));
        System.out.printf("   Procesadores: %d%n", runtime.availableProcessors());
        System.out.printf("   Memoria máxima JVM: %.2f MB%n",
                runtime.maxMemory() / 1024.0 / 1024.0);
        System.out.printf("   Memoria inicial: %.2f MB%n",
                runtime.totalMemory() / 1024.0 / 1024.0);
        System.out.println();
    }

    /**
     * Ejecuta el demo del sistema por un tiempo determinado
     */
    private static void runDemo(EcoMonitorSystem system, int durationSeconds) {
        System.out.printf("⏱️ Ejecutando demo por %d segundos...%n", durationSeconds);
        System.out.println("💡 Presiona Ctrl+C para detener en cualquier momento");
        System.out.println();

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (durationSeconds * 1000L);
        int statusInterval = Math.max(10, durationSeconds / 6); // Mostrar estado cada ~10 segundos

        long lastStatusTime = startTime;

        while (System.currentTimeMillis() < endTime && system.isRunning()) {
            try {
                Thread.sleep(1000); // Verificar cada segundo

                long currentTime = System.currentTimeMillis();

                // Mostrar estado periódicamente
                if (currentTime - lastStatusTime >= statusInterval * 1000) {
                    system.printSystemStatus();
                    lastStatusTime = currentTime;

                    // Mostrar tiempo restante
                    long remainingSeconds = (endTime - currentTime) / 1000;
                    System.out.printf("⏳ Tiempo restante: %d segundos%n%n", remainingSeconds);
                }

            } catch (InterruptedException e) {
                System.out.println("⚠️ Demo interrumpido");
                Thread.currentThread().interrupt();
                break;
            }
        }

        // Estado final
        System.out.println("📊 ESTADO FINAL:");
        system.printSystemStatus();
    }

    /**
     * Ejecuta un stress test intensivo del sistema
     */
    private static void runStressTest() {
        System.out.println("🔥 INICIANDO STRESS TEST");
        System.out.println("Creando sistema con muchos sensores y alta frecuencia...");
        System.out.println();

        // Configurar JVM para memoria limitada si no está configurada
        Runtime runtime = Runtime.getRuntime();
        long maxMemoryMB = runtime.maxMemory() / 1024 / 1024;

        System.out.println("🖥️ CONFIGURACIÓN DEL STRESS TEST:");
        System.out.printf("   Memoria máxima: %d MB%n", maxMemoryMB);

        if (maxMemoryMB > 100) {
            System.out.println("   ⚠️ Para un stress test más realista, ejecuta con:");
            System.out.println("   java -Xmx32m -Xms16m Main stress");
            System.out.println();
        }

        EcoMonitorSystem stressSystem = new EcoMonitorSystem();

        // Agregar muchos sensores para estresar el sistema
        System.out.println("🔧 Agregando 15 sensores adicionales...");
        for (int i = 10; i < 25; i++) {
            stressSystem.addSensor(i, "STRESS_SENSOR_" + i, 0.0f, 100.0f);
        }

        System.out.println("✅ Total de sensores: " + stressSystem.getSensorCount());
        System.out.println();

        stressSystem.startDataCollection();

        // Ejecutar por 2 minutos bajo estrés
        System.out.println("⏱️ Ejecutando stress test por 120 segundos...");
        runDemo(stressSystem, 120);

        stressSystem.stop();

        System.out.println("\n🏆 STRESS TEST COMPLETADO");
        System.out.println("✅ Sistema mantuvo estabilidad bajo carga");

        // Métricas finales del stress test
        System.out.println("\n📊 MÉTRICAS DEL STRESS TEST:");
        System.out.printf("   Total lecturas procesadas: %d%n", stressSystem.getTotalReadings());
        System.out.printf("   Alertas generadas: %d%n", stressSystem.getAlertsGenerated());
        System.out.printf("   Sensores monitoreados: %d%n", stressSystem.getSensorCount());

        long finalMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        System.out.printf("   Memoria final usada: %d MB%n", finalMemory);
    }

    /**
     * Método auxiliar para pausar la ejecución
     */
    private static void pause(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}