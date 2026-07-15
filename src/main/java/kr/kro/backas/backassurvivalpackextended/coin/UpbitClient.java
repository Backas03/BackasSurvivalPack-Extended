package kr.kro.backas.backassurvivalpackextended.coin;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** 업비트 시세 조회(Quotation) API 클라이언트. 인증 불필요, IP당 초당 10회 제한. */
public final class UpbitClient {

    public static final String BASE_URL = "https://api.upbit.com";
    private static final Gson GSON = new Gson();

    private UpbitClient() {
    }

    public static <T> T get(String pathAndQuery, Class<T> responseType) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(BASE_URL + pathAndQuery).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder buffer = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                return GSON.fromJson(buffer.toString(), responseType);
            }
        } finally {
            if (connection != null) connection.disconnect();
        }
    }
}
