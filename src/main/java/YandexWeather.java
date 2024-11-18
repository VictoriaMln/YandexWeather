import okhttp3.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class YandexWeather {
    private static final String API_KEY = "74c2a6ba-def3-48c9-8033-b8fd22b813c8";
    private static final String API_URL = "https://api.weather.yandex.ru/v2/forecast";

    public static void main(String[] args) {
        //координаты
        double lat = 7.76774;
        double lon = 98.30643;

        //период прогноза в днях
        int limit = 7;

        OkHttpClient client = new OkHttpClient();

        //построение запроса
        HttpUrl url = HttpUrl.parse(API_URL).newBuilder()
                .addQueryParameter("lat", String.valueOf(lat))
                .addQueryParameter("lon", String.valueOf(lon))
                .addQueryParameter("limit", String.valueOf(limit))
                .addQueryParameter("hourse", "false")
                .build();

        //создание запроса с заголовками
        Request request = new Request.Builder()
                .url(url)
                .addHeader("X-Yandex-API-Key", API_KEY)
                .build();

        //выполнение запроса
        Call call = client.newCall(request);
        try (Response response = call.execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();

                //Вывод полного ответа
                System.out.println("Полный ответ от сервиса:");
                System.out.println(responseBody);

                //преобразование JSON-ответа
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(responseBody);

                //получение текущей температуры
                int currentTemp = jsonNode.get("fact").get("temp").asInt();
                System.out.println("\nТекущая температура: " + currentTemp + "°C");

                //вычисление средненй температуры за период
                JsonNode forecasts = jsonNode.get("forecasts");
                if (forecasts.isArray()) {
                    double sumTemp = 0;
                    int count = 0;
                    for (JsonNode forecast : forecasts) {
                        JsonNode parts = forecast.get("parts");
                        JsonNode day = parts.get("day");
                        if (day != null && day.has("temp_avg")) { //извлекаем средние температуры из каждого дня
                            sumTemp += day.get("temp_avg").asDouble();
                            count++;
                        }
                    }
                    if (count > 0) {
                        double averageTemp = sumTemp / count;
                        System.out.printf("Средняя температура за %d дней: %.2f°C\n", limit, averageTemp);
                    } else {
                        System.out.println("Не удалось получить данные для расчета средней температуры.");
                    }
                }
            } else {
                System.out.println("Ошибка при запросе данных: " + response.code() + " - " + response.message());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}