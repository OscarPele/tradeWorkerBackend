package com.oscar.tradeWorkerBackend.binance;

import com.oscar.tradeWorkerBackend.binance.dto.MarginAccountResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;

@Service
public class BinanceClient {

    private final BinanceProperties properties;
    private final RestTemplate restTemplate;

    public BinanceClient(BinanceProperties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
    }

    public MarginAccountResponse getMarginAccount() {
        long timestamp = Instant.now().toEpochMilli();
        String queryString = "timestamp=" + timestamp;
        String signature = sign(queryString, properties.getApiSecret());

        String url = properties.getBaseUrl()
                + "/sapi/v1/margin/account?"
                + queryString
                + "&signature="
                + signature;

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-MBX-APIKEY", properties.getApiKey());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<MarginAccountResponse> response = restTemplate.exchange(
                URI.create(url),
                HttpMethod.GET,
                entity,
                MarginAccountResponse.class
        );

        return Objects.requireNonNull(response.getBody());
    }

    private String sign(String data, String secret) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            sha256_HMAC.init(secretKey);
            byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder result = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(b & 0xff);
                if (hex.length() == 1) {
                    result.append('0');
                }
                result.append(hex);
            }
            return result.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Error firmando petición a Binance", e);
        }
    }
}
