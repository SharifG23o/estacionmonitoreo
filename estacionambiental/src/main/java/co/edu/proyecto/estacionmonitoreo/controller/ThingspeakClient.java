package co.edu.proyecto.estacionmonitoreo.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Cliente para obtener lecturas desde ThingSpeak usando Gson.
 */
public class ThingspeakClient {

    private static final String READ_API_URL_TEMPLATE =
        "https://api.thingspeak.com/channels/%d/feeds.json?api_key=%s&results=1";

    private static final String READ_API_KEY = "X7NSX3A4F1ZJA5S5";

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

            // Parsear JSON con Gson (versión moderna)
            JsonElement element = JsonParser.parseString(response.toString());
            JsonObject json = element.getAsJsonObject();
            JsonArray feeds = json.getAsJsonArray("feeds");

            if (feeds == null || feeds.size() == 0) {
                System.out.println("⚠ No hay datos en ThingSpeak.");
                return Float.NaN;
            }

            JsonObject lastFeed = feeds.get(feeds.size() - 1).getAsJsonObject();
            String fieldKey = "field" + fieldNumber;

            if (!lastFeed.has(fieldKey) || lastFeed.get(fieldKey).isJsonNull()) {
                System.out.println("⚠ Campo " + fieldKey + " vacío en ThingSpeak.");
                return Float.NaN;
            }

            return Float.parseFloat(lastFeed.get(fieldKey).getAsString());

        } catch (Exception e) {
            System.err.println("❌ Error obteniendo datos de ThingSpeak: " + e.getMessage());
            return Float.NaN;
        }
    }
}
