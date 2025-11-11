#include <WiFi.h>
#include "ThingSpeak.h"
#include "DHT.h"
#include <LiquidCrystal.h>

// ============================
// 🔹 LCD (RS, E, D4, D5, D6, D7)
// ============================
LiquidCrystal lcd(26, 25, 14, 27, 33, 32);

// ============================
// 🔹 Sensores
// ============================
#define DHTPIN 4
#define DHTTYPE DHT22
#define MQ135_PIN 34
#define UV_PIN 35
DHT dht(DHTPIN, DHTTYPE);

// ============================
// 🔹 WiFi y ThingSpeak
// ============================
const char* ssid = "Galaxy S24 1AD6";
const char* password = "sharif02e";
WiFiClient cliente;
unsigned long channelID = 3149359;
const char* writeAPIKey = "EHWN4K9TWXDUITLQ";

void setup() {
  Serial.begin(115200);
  delay(1000);

  // --- Inicialización reforzada del LCD ---
  lcd.begin(16, 2);
  delay(200);
  lcd.clear();
  lcd.print("LCD inicializando");
  delay(1500);
  lcd.clear();
  lcd.print("Iniciando sensores");

  dht.begin();
  pinMode(MQ135_PIN, INPUT);
  pinMode(UV_PIN, INPUT);

  // --- WiFi ---
  lcd.clear();
  lcd.print("Conectando WiFi");
  Serial.print("Conectando a WiFi ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);

  int intentos = 0;
  while (WiFi.status() != WL_CONNECTED && intentos < 20) {
    delay(500);
    Serial.print(".");
    lcd.setCursor(0, 1);
    lcd.print(".");
    intentos++;
  }

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\n✅ WiFi conectado");
    lcd.clear();
    lcd.print("WiFi conectado!");
  } else {
    Serial.println("\n⚠️ No se pudo conectar");
    lcd.clear();
    lcd.print("Error WiFi!");
  }

  delay(1500);
  ThingSpeak.begin(cliente);
}

// ============================
// 🔹 Bucle principal
// ============================
void loop() {
  // --- Lecturas ---
  float temperatura = dht.readTemperature();
  float humedad = dht.readHumidity();
  int valorMQ = analogRead(MQ135_PIN);
  float voltajeMQ = (valorMQ / 4095.0) * 3.3;
  float ppmAprox = (voltajeMQ / 3.3) * 1000.0;
  int valorUV = analogRead(UV_PIN);
  float voltajeUV = (valorUV / 4095.0) * 3.3;
  float indiceUV = (voltajeUV - 0.99) * (15.0 / (2.8 - 0.99));
  if (indiceUV < 0) indiceUV = 0;

  // --- Consola Serial ---
  Serial.println("------ LECTURAS ------");
  Serial.printf("Temp: %.1f °C | Hum: %.1f %%\n", temperatura, humedad);
  Serial.printf("MQ135: %.2f V (%.0f ppm)\n", voltajeMQ, ppmAprox);
  Serial.printf("UV: %.2f V (%.1f)\n", voltajeUV, indiceUV);
  Serial.println("----------------------");

  // --- Mostrar en LCD ---
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.printf("T:%.1f°C H:%.1f%%", temperatura, humedad);
  lcd.setCursor(0, 1);
  lcd.printf("PPM:%.1f Uv:%.1f", ppmAprox, indiceUV);

  // --- Enviar a ThingSpeak ---
  ThingSpeak.setField(1, temperatura);
  ThingSpeak.setField(2, humedad);
  ThingSpeak.setField(3, ppmAprox);
  ThingSpeak.setField(4, indiceUV);
  int resp = ThingSpeak.writeFields(channelID, writeAPIKey);

  if (resp == 200) {
    Serial.println("✅ Datos enviados correctamente a ThingSpeak.");
  } else {
    Serial.print("⚠️ Error al enviar. Código: ");
    Serial.println(resp);
  }

  delay(20000);
}
