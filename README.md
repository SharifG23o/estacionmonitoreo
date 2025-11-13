# 📊 Estación de Monitoreo Ambiental - Análisis de Infraestructura Computacional

![GitHub repo size](https://img.shields.io/github/repo-size/SharifG23o/Estacion-Monitoreo-Ambiental?color=blue&label=Tamaño%20del%20Repo)
![GitHub language count](https://img.shields.io/github/languages/count/SharifG23o/Estacion-Monitoreo-Ambiental?color=purple&label=Lenguajes)
![GitHub top language](https://img.shields.io/github/languages/top/SharifG23o/Estacion-Monitoreo-Ambiental?color=orange&label=Lenguaje%20Principal)
![GitHub license](https://img.shields.io/github/license/SharifG23o/Estacion-Monitoreo-Ambiental?color=green&label=Licencia)

---

Este repositorio contiene el **proyecto final** de la asignatura **Fundamentos de Infraestructura Computacional** de la **Universidad del Quindío 🏫**, correspondiente al programa de **Ingeniería de Sistemas y Computación**. El proyecto implementa un sistema IoT embebido para el análisis práctico de infraestructura computacional, enfocándose en monitoreo de recursos, comunicación por buses de datos y optimización de rendimiento.

---

## 📑 Tabla de Contenidos

1. [📝 Descripción General](#-descripción-general)
2. [🎯 Objetivos del Proyecto](#-objetivos-del-proyecto)
3. [📂 Estructura del Repositorio](#-estructura-del-repositorio)
4. [🛠️ Tecnologías y Hardware Utilizados](#️-tecnologías-y-hardware-utilizados)
5. [⚙️ Instalación y Configuración](#️-instalación-y-configuración)
6. [🔧 Requisitos Funcionales](#-requisitos-funcionales)
7. [📊 Arquitectura del Sistema](#-arquitectura-del-sistema)
8. [📈 Métricas y Evaluación](#-métricas-y-evaluación)
9. [📜 Licencia](#-licencia)
10. [🤝 Contribuciones](#-contribuciones)
11. [📧 Contacto](#-contacto)

---

## 📝 Descripción General

La **Estación de Monitoreo Ambiental** es un sistema IoT basado en dispositivos embebidos que permite estudiar y analizar el comportamiento de la infraestructura computacional en tiempo real. El proyecto integra sensores ambientales con un enfoque académico en:

- 🔍 **Monitoreo de recursos del sistema** (RAM, CPU, temperatura interna)
- 🚌 **Análisis de comunicación por buses de datos** (I2C, SPI, WiFi)
- ⚡ **Optimización de rendimiento energético** y uso de recursos
- 📡 **Transmisión de datos y métricas** en tiempo real
- 🌡️ **Monitoreo de variables ambientales** (temperatura, humedad, calidad del aire, radiación UV)

Este proyecto tiene como propósito didáctico comprender la infraestructura computacional en sistemas embebidos, analizando latencias, throughput, consumo de recursos y comportamiento de protocolos de comunicación.

---

## 🎯 Objetivos del Proyecto

### Objetivo General
Implementar un sistema IoT que permita estudiar y analizar el comportamiento de la infraestructura computacional en dispositivos embebidos.

### Objetivos Específicos
- Monitorear y optimizar el uso de recursos del sistema (RAM, CPU, energía)
- Analizar el rendimiento de buses de comunicación (I2C, SPI, WiFi)
- Implementar estrategias de transmisión eficiente de datos
- Documentar métricas de rendimiento de la infraestructura computacional
- Desarrollar un dashboard para visualización de datos del sistema

---

## 📂 Estructura del Repositorio

```
📁 estacionmonitoreo/
│
├── 📂 co.edu.proyecto.estacionmonitoreo
│   ├── Main.java                  # Punto de entrada del sistema
│   ├── controller/
│   │   └── EcoMonitorSystem.java  # Controlador principal y lógica central
│   ├── model/
│   │   ├── Sensor.java            # Clase base de sensores
│   │   ├── SensorReading.java     # Lecturas de sensores con timestamp
│   │   ├── CircularBuffer.java    # Buffer circular de lecturas
│   │   ├── MemoryMonitor.java     # Monitoreo y gestión de memoria JVM
│   │   └── SensorReadingPool.java # Pool de objetos para optimización
│   ├── viewController/
│   │   └── EcoMonitorGUI.java     # Interfaz gráfica en JavaFX
│   └── module-info.java           # Configuración de módulo Java
│
└── 📄 README.md
```

---

## 🛠️ Tecnologías y Hardware Utilizados

### Hardware

**Microcontrolador/Microprocesador:**
- 🔷 **ESP32** 
- RAM: 520 KB (ESP32)
- Flash: 4 MB
- WiFi integrado (802.11 b/g/n)

**Sensores:**
- 🌡️ **DHT22** - Temperatura y Humedad (I2C/Digital)
- 💨 **MQ-135** - Calidad del Aire (Analógico)
- ☀️ **GUVA-S12SD** - Radiación UV (Analógico)

**Almacenamiento:**
- 💾 Nube y dispositivo local

**Alimentación:**
- ⚡ USB 5V / Regulador 3.3V

### Software y Herramientas

**Entorno de Desarrollo:**
- 💻 **Arduino IDE** 
- 🔧 **Visual Studio Code** 

**Lenguajes:**
- 🟦 **C/C++** 
- 🟨 **Java**

**Librerías Principales (Ejemplo ESP32):**
```cpp
- WiFi.h          // Conectividad WiFi
- DHT.h           // Sensor DHT22
- ArduinoJson.h   // Serialización de datos
- ESP.h           // Monitoreo del sistema
- Wire.h          // Comunicación I2C
- SPI.h           // Comunicación SPI
- SD.h            // Gestión de tarjeta SD
```

**Control de Versiones:**
- 🌱 **Git & GitHub** - Versionamiento y colaboración

---

## ⚙️ Instalación y Configuración

### Requisitos Previos

1. **PlatformIO** o **Arduino IDE** instalado
2. Drivers USB para el microcontrolador (CP2102, CH340, etc.)
3. Conexión WiFi disponible
4. Cable USB para programación

### Paso 1: Clonar el Repositorio

```bash
git clone https://github.com/SharifG23o/Estacion-Monitoreo-Ambiental.git
cd Estacion-Monitoreo-Ambiental
```

### Paso 2: Configurar Credenciales WiFi

Editar el archivo `firmware/src/config.h`:

```cpp
#define WIFI_SSID "tu-red-wifi"
#define WIFI_PASSWORD "tu-contraseña"
#define SERVER_URL "http://tu-servidor.com/api"
```

### Paso 3: Compilar y Cargar el Firmware

**Con PlatformIO:**
```bash
cd firmware
pio run --target upload
pio device monitor
```

**Con Arduino IDE:**
1. Abrir `firmware/src/main.ino`
2. Seleccionar placa y puerto
3. Compilar y cargar

### Paso 4: Configurar Dashboard (Opcional)

```bash
cd dashboard
# Abrir index.html en navegador o servir con servidor local
python -m http.server 8000
```

### Paso 5: Verificar Funcionamiento

Monitorear el puerto serial para ver:
- Conexión WiFi establecida
- Lecturas de sensores
- Métricas del sistema
- Transmisión de datos

---

## 🔧 Requisitos Funcionales

### 🖥️ Monitoreo de Recursos del Sistema

| ID | Descripción |
|---|---|
| **RF-001** | Monitorear uso de memoria RAM en tiempo real (heap disponible/usado) |
| **RF-002** | Medir uso de procesador (% CPU, frecuencia actual) |
| **RF-003** | Registrar temperatura interna del microcontrolador |
| **RF-004** | Monitorear voltaje de alimentación y consumo energético |
| **RF-005** | Detectar y registrar reinicios del sistema y errores de memoria |

### 🚌 Comunicación por Buses de Datos

| ID | Descripción |
|---|---|
| **RF-006** | Implementar comunicación I2C con los sensores |
| **RF-007** | Monitorear velocidad y latencia de transmisión I2C |
| **RF-008** | Implementar comunicación SPI para almacenamiento local |
| **RF-009** | Monitorear tráfico de datos por WiFi (bytes enviados/recibidos) |
| **RF-010** | Registrar errores de comunicación y reintentos |

### 🌡️ Medición de Sensores

| ID | Descripción |
|---|---|
| **RF-011** | Leer temperatura y humedad cada 30 segundos |
| **RF-012** | Medir calidad del aire cada 120 segundos |
| **RF-013** | Registrar intensidad UV cada 120 segundos |
| **RF-014** | Validar lecturas y detectar sensores desconectados |

### 📡 Transmisión de Datos

| ID | Descripción |
|---|---|
| **RF-015** | Enviar datos cada 15 minutos vía WiFi |
| **RF-016** | Incluir métricas de sistema en cada transmisión |
| **RF-017** | Implementar buffer local para 20 lecturas |

---

## 📊 Arquitectura del Sistema

### Diagrama de Bloques

```
┌─────────────────────────────────────────────────────┐
│              SISTEMA EMBEBIDO (ESP32)               │
│                                                     │
│  ┌──────────────┐      ┌──────────────────────┐   │
│  │   Sensores   │──I2C─▶│  Microcontrolador   │   │
│  │  DHT22       │      │                      │   │
│  │  MQ-135      │      │  - Monitor RAM       │   │
│  │  UV Sensor   │      │  - Monitor CPU       │   │
│  └──────────────┘      │  - Monitor Temp      │   │
│                        │  - Buffer Datos      │   │
│  ┌──────────────┐      └──────────┬───────────┘   │
│  │  microSD     │◀─SPI────────────┘                │
│  │  (Logs)      │                                  │
│  └──────────────┘      ┌──────────▼───────────┐   │
│                        │    WiFi Module       │   │
│                        │  - Monitor RSSI      │   │
│                        │  - Monitor TX/RX     │   │
│                        └──────────┬───────────┘   │
└────────────────────────────────────┼──────────────┘
                                     │
                          WiFi (802.11 b/g/n)
                                     │
                         ┌───────────▼────────────┐
                         │   Servidor / Cloud     │
                         │   Dashboard Web        │
                         └────────────────────────┘
```

### Especificaciones de Buses de Comunicación

**Bus I2C:**
- Velocidad: 100 kHz (modo estándar)
- Pines: GPIO 21 (SDA), GPIO 22 (SCL)
- Latencia objetivo: <5ms por lectura

**Bus SPI:**
- Velocidad: ~1 MHz
- Uso: Almacenamiento en microSD
- Throughput objetivo: >100 KB/s

**WiFi:**
- Protocolo: 802.11 b/g/n
- Uso estimado: 1-5 KB cada 15 minutos
- RSSI objetivo: >-70 dBm

---

## 📈 Métricas y Evaluación

### Objetivos de Rendimiento

| Métrica | Objetivo | Método de Medición |
|---------|----------|--------------------|
| **Uso de RAM** | < 60% del total | `ESP.getFreeHeap()` |
| **CPU Idle** | > 75% del tiempo | Análisis de tareas |
| **Latencia I2C** | < 10ms por sensor | Timestamps |
| **Uptime WiFi** | > 95% | Contador de desconexiones |
| **Reinicios** | 0 inesperados/24h | Registro de boot |
| **Consumo energético** | < 500 mA promedio | Medición externa |

### Ejemplo de Uso de Recursos (ESP32)

**Memoria RAM:**
- Sistema base: ~50 KB
- WiFi stack: ~40 KB
- Buffers sensores: ~5 KB
- Buffer transmisión: ~10 KB
- Variables: ~15 KB
- **Heap libre mínimo:** ~400 KB
- **Total utilizado:** 120 KB (23% de 520 KB) ✅

**Uso de CPU:**
- Lectura sensores: 5% (cada 30-60s)
- Procesamiento: 2% continuo
- Transmisión WiFi: 10% (2-3s cada 15min)
- Monitoreo sistema: 3% continuo
- **Sistema idle:** 80% promedio ✅

---

## 📜 Licencia

Este proyecto está licenciado bajo la **Licencia MIT** - ver el archivo [LICENSE](LICENSE) para más detalles.

**Nota:** Este repositorio tiene fines estrictamente académicos y educativos.

---

## 🤝 Contribuciones

Las contribuciones son bienvenidas siguiendo estas pautas:

1. **Fork** el repositorio
2. Crear una **rama** para tu feature (`git checkout -b feature/nueva-funcionalidad`)
3. **Commit** tus cambios (`git commit -m 'Agregar nueva funcionalidad'`)
4. **Push** a la rama (`git push origin feature/nueva-funcionalidad`)
5. Abrir un **Pull Request**

## 📧 Contacto

Para consultas académicas, colaboraciones o soporte técnico:

- 👨‍💻 **Autores:** Sharif Giraldo Obando
- 🎓 **Programa:** Ingeniería de Sistemas y Computación
- 🏫 **Universidad:** Universidad del Quindío – Armenia, Colombia
- 📚 **Asignatura:** Fundamentos de Infraestructura Computacional
- 📧 **Correos institucionales:**
  - sharif.giraldoo@uqvirtual.edu.co
- 🔗 **GitHub:** [SharifG23o](https://github.com/SharifG23o)

---

## 🎓 Entregables del Proyecto

1. ✅ **Código fuente** documentado con métricas de sistema
2. ✅ **Reporte de análisis** de infraestructura computacional
3. ✅ **Dashboard básico** mostrando métricas de sistema y sensores
4. ✅ **Documentación técnica** de buses de comunicación y rendimiento
5. ✅ **Análisis de optimización** de recursos del sistema

---

✨ *Este repositorio apoya el proceso formativo en la asignatura **Fundamentos de Infraestructura Computacional**, promoviendo la comprensión práctica de sistemas embebidos, análisis de rendimiento y optimización de recursos computacionales en dispositivos IoT.*

---

**Última actualización:** Noviembre 2025  
**Versión del Firmware:** 1.0.0  
**Estado del Proyecto:** 🚧 En Desarrollo
