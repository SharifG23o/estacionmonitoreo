package co.edu.proyecto.estacionmonitoreo.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Cliente para obtener lecturas desde ThingSpeak usando Gson.
 * Lee el último valor disponible de un canal y campo específico.
 */
public class ThingSpeakClient {

    // URL base con tu canal y API Key de lectura
    private static final String READ_API_URL_TEMPLATE =
        "https://api.thingspeak.com/channels/%d/feeds.json?api_key=%s&results=1";

    // Tu API key de lectura (solo lectura, no de escritura)
    private static final String READ_API_KEY = "X7NSX3A4F1ZJA5S5";

    /**
     * Obtiene el último valor de un campo (field) de tu canal en ThingSpeak.
     *
     * @param channelId   ID del canal ThingSpeak (ej. 3149359)
     * @param fieldNumber Número del campo (1–8)
     * @return Valor numérico leído o Float.NaN si no hay dato
     */
    public static float getLatestFieldValue(int channelId, int fieldNumber) {
        try {
            String urlString = String.format(READ_API_URL_TEMPLATE, channelId, READ_API_KEY);
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);

            if (conn.getResponseCode() != 200) {
                System.err.println("⚠ Error HTTP " + conn.getResponseCode() + " al leer canal " + channelId);
                return Float.NaN;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            reader.close();

            // Parsear el JSON con Gson
            JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
            JsonArray feeds = json.getAsJsonArray("feeds");

            if (feeds == null || feeds.size() == 0) {
                System.out.println("⚠ No hay datos disponibles en ThingSpeak.");
                return Float.NaN;
            }

            JsonObject lastFeed = feeds.get(feeds.size() - 1).getAsJsonObject();
            String fieldKey = "field" + fieldNumber;

            if (!lastFeed.has(fieldKey) || lastFeed.get(fieldKey).isJsonNull()) {
                System.out.println("⚠ Campo " + fieldKey + " vacío en ThingSpeak.");
                return Float.NaN;
            }

            String fieldValue = lastFeed.get(fieldKey).getAsString();
            return Float.parseFloat(fieldValue);

        } catch (Exception e) {
            System.err.println("❌ Error obteniendo datos de ThingSpeak: " + e.getMessage());
            return Float.NaN;
        }
    }
}
