package kr.kro.backas.backassurvivalpackextended.util;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** 외부 JSON/텍스트 API 공용 클라이언트 (네이버/야후 등 User-Agent가 필요한 곳 포함) */
public final class JsonHttpClient {

    private static final Gson GSON = new Gson();
    private static final String USER_AGENT = "Mozilla/5.0 (BackasSurvivalPack)";

    private JsonHttpClient() {
    }

    public static <T> T get(String url, Type responseType) throws IOException {
        String body = getRaw(url);
        return GSON.fromJson(body, responseType);
    }

    public static String getRaw(String url) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(8000);

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder buffer = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append('\n');
                }
                return buffer.toString();
            }
        } finally {
            if (connection != null) connection.disconnect();
        }
    }
}
