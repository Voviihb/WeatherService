import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class WeatherService {
    public static void main(String[] args) throws IOException, InterruptedException {
        double lat = 55.75222;
        double lon = 37.61556;
        int limit = 7;

        // Api ключ достаем из локального конфига
        Properties properties = new Properties();
        try (FileInputStream file = new FileInputStream("weather.properties")) {
            properties.load(file);
        }
        String apiKey = properties.getProperty("api.key");

        HttpClient client = HttpClient.newHttpClient();
        String address = "https://api.weather.yandex.ru/v2/forecast"
                + "?lat=" + lat + "&lon=" + lon + "&limit=" + limit;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(address))
                .header("X-Yandex-Weather-Key", apiKey)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        String json = response.body();
        System.out.println(json);
        if (response.statusCode() != 200) {
            System.out.println("Ошибка API: HTTP " + response.statusCode());
            return;
        }

        JsonObject data = JsonParser.parseString(json).getAsJsonObject();
        double currentTemperature = data.getAsJsonObject("fact").get("temp").getAsDouble();
        System.out.println("Текущая температура: " + currentTemperature + " C");

        // Берём дневную температуру из первых limit элементов forecasts.
        JsonArray forecasts = data.getAsJsonArray("forecasts");
        double sum = 0;
        for (int i = 0; i < limit; i++) {
            JsonObject forecast = forecasts.get(i).getAsJsonObject();
            JsonObject day = forecast.getAsJsonObject("parts").getAsJsonObject("day");
            sum += day.get("temp_avg").getAsDouble();
        }
        System.out.printf("Средняя дневная температура за %d суток: %.2f C%n", limit, sum / limit);
    }
}
