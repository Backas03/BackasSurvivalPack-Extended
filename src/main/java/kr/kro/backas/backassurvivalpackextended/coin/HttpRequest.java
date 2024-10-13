package kr.kro.backas.backassurvivalpackextended.coin;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

public class HttpRequest {
    private final String endPoint;
    private final String method;
    private final String query;

    public HttpRequest(String endPoint, String method, String query) {
        this.endPoint = endPoint;
        this.method = method;
        this.query = query;
    }

    public CoinTickResult getResponse() throws IOException, NoSuchAlgorithmException {
        /*
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(query.getBytes(StandardCharsets.UTF_8));

        String queryHash = String.format("%0128x", new BigInteger(1, md.digest()));

        String authenticationToken = "Bearer " + Jwts.builder()
                .claim("access_key", UpbitAPI.ACCESS_KEY)
                .claim("nonce", UUID.randomUUID())
                .claim("query_hash", queryHash)
                .claim("query_hash_alg", "SHA512")
                .signWith(Keys.hmacShaKeyFor(UpbitAPI.SECRET_KEY.getBytes(StandardCharsets.UTF_8)));

         */

        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(endPoint + query);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", "application/json");
            //connection.setRequestProperty("Authorization", authenticationToken);
            connection.connect();
            int responseCode = connection.getResponseCode();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder buffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            return new Gson().fromJson(buffer.toString(), CoinTickResult[].class)[0];
        } finally {
            if (connection != null) connection.disconnect();
            if (reader != null) reader.close();
        }
    }
}