package com.oscar.tradeWorkerBackend.binance;

import com.oscar.tradeWorkerBackend.binance.dto.MarginAccountResponse;
import com.oscar.tradeWorkerBackend.binance.dto.MaxBorrowableResponse;
import com.oscar.tradeWorkerBackend.binance.dto.OrderSide;
import com.oscar.tradeWorkerBackend.binance.dto.PriceTickerResponse;
import com.oscar.tradeWorkerBackend.binance.dto.SymbolFilters;
import com.oscar.tradeWorkerBackend.binance.dto.exchange.ExchangeInfoResponse;
import com.oscar.tradeWorkerBackend.binance.dto.exchange.SymbolInfo;
import com.oscar.tradeWorkerBackend.binance.dto.exchange.TradingFilter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
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

    public Map<String, Object> borrowAsset(String asset, String amount, boolean isolated, String symbol) {
        long timestamp = Instant.now().toEpochMilli();

        UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
                .queryParam("asset", asset)
                .queryParam("amount", amount)
                .queryParam("timestamp", timestamp);

        if (isolated) {
            builder.queryParam("isIsolated", "TRUE");
            builder.queryParam("symbol", symbol);
        }

        String queryString = Objects.requireNonNull(builder.build().getQuery());
        String url = buildSignedUrl("/sapi/v1/margin/loan", queryString);

        return exchange(url, HttpMethod.POST, new ParameterizedTypeReference<>() {
        });
    }

    public Map<String, Object> placeMarginMarketOrder(
            String symbol,
            OrderSide side,
            String quantity,
            String quoteOrderQty,
            boolean isolated
    ) {
        long timestamp = Instant.now().toEpochMilli();

        UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
                .queryParam("symbol", symbol)
                .queryParam("side", side.name())
                .queryParam("type", "MARKET")
                .queryParam("timestamp", timestamp);

        if (quoteOrderQty != null) {
            builder.queryParam("quoteOrderQty", quoteOrderQty);
        } else {
            builder.queryParam("quantity", quantity);
        }

        if (isolated) {
            builder.queryParam("isIsolated", "TRUE");
        }

        String queryString = Objects.requireNonNull(builder.build().getQuery());
        String url = buildSignedUrl("/sapi/v1/margin/order", queryString);

        return exchange(url, HttpMethod.POST, new ParameterizedTypeReference<>() {
        });
    }

    public Map<String, Object> placeMarginOcoOrder(
            String symbol,
            OrderSide side,
            String quantity,
            String takeProfitPrice,
            String stopLossPrice,
            boolean isolated
    ) {
        long timestamp = Instant.now().toEpochMilli();

        UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
                .queryParam("symbol", symbol)
                .queryParam("side", side.name())
                .queryParam("quantity", quantity)
                .queryParam("price", takeProfitPrice)
                .queryParam("stopPrice", stopLossPrice)
                .queryParam("stopLimitPrice", stopLossPrice)
                .queryParam("stopLimitTimeInForce", "GTC")
                .queryParam("timestamp", timestamp);

        if (isolated) {
            builder.queryParam("isIsolated", "TRUE");
        }

        String queryString = Objects.requireNonNull(builder.build().getQuery());
        String url = buildSignedUrl("/sapi/v1/margin/order/oco", queryString);

        return exchange(url, HttpMethod.POST, new ParameterizedTypeReference<>() {
        });
    }

    public double getTickerPrice(String symbol) {
        String url = properties.getBaseUrl()
                + "/api/v3/ticker/price?symbol="
                + symbol;

        PriceTickerResponse response = restTemplate.getForObject(url, PriceTickerResponse.class);

        if (response == null || response.getPrice() == null) {
            throw new IllegalStateException("No se pudo obtener el precio de " + symbol + " desde Binance");
        }

        return Double.parseDouble(response.getPrice());
    }

    public MaxBorrowableResponse getMaxBorrowable(String asset, boolean isolated, String symbol) {
        long timestamp = Instant.now().toEpochMilli();

        UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
                .queryParam("asset", asset)
                .queryParam("timestamp", timestamp);

        if (isolated) {
            builder.queryParam("isIsolated", "TRUE");
            builder.queryParam("symbol", symbol);
        }

        String queryString = Objects.requireNonNull(builder.build().getQuery());
        String url = buildSignedUrl("/sapi/v1/margin/maxBorrowable", queryString);

        return exchange(url, HttpMethod.GET, MaxBorrowableResponse.class);
    }

    public SymbolFilters getSymbolFilters(String symbol) {
        String url = properties.getBaseUrl()
                + "/api/v3/exchangeInfo?symbol="
                + symbol;

        ExchangeInfoResponse response = restTemplate.getForObject(url, ExchangeInfoResponse.class);

        if (response == null || response.getSymbols() == null || response.getSymbols().isEmpty()) {
            throw new IllegalStateException("No se pudo obtener exchangeInfo para " + symbol);
        }

        SymbolInfo info = response.getSymbols().getFirst();

        BigDecimal tickSize = null;
        BigDecimal stepSize = null;

        if (info.getFilters() != null) {
            for (TradingFilter filter : info.getFilters()) {
                if ("PRICE_FILTER".equalsIgnoreCase(filter.getFilterType())) {
                    tickSize = new BigDecimal(filter.getTickSize());
                }
                if ("LOT_SIZE".equalsIgnoreCase(filter.getFilterType())) {
                    stepSize = new BigDecimal(filter.getStepSize());
                }
            }
        }

        if (tickSize == null || stepSize == null) {
            throw new IllegalStateException("No se encontraron filtros PRICE_FILTER/LOT_SIZE para " + symbol);
        }

        return new SymbolFilters(tickSize, stepSize);
    }

    public java.util.List<java.util.Map<String, Object>> getOpenMarginOrders(String symbol, boolean isolated) {
        long timestamp = Instant.now().toEpochMilli();

        UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
                .queryParam("timestamp", timestamp);

        if (symbol != null && !symbol.isBlank()) {
            builder.queryParam("symbol", symbol);
        }

        if (isolated) {
            builder.queryParam("isIsolated", "TRUE");
        }

        String queryString = Objects.requireNonNull(builder.build().getQuery());
        String url = buildSignedUrl("/sapi/v1/margin/openOrders", queryString);

        return exchange(url, HttpMethod.GET, new ParameterizedTypeReference<>() {
        });
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

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-MBX-APIKEY", properties.getApiKey());
        return headers;
    }

    private <T> T exchange(String url, HttpMethod method, Class<T> responseType) {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());

        ResponseEntity<T> response = restTemplate.exchange(
                URI.create(url),
                method,
                entity,
                responseType
        );

        return Objects.requireNonNull(response.getBody());
    }

    private <T> T exchange(String url, HttpMethod method, ParameterizedTypeReference<T> responseType) {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());

        ResponseEntity<T> response = restTemplate.exchange(
                URI.create(url),
                method,
                entity,
                responseType
        );

        return Objects.requireNonNull(response.getBody());
    }

    private String buildSignedUrl(String path, String queryString) {
        String signature = sign(queryString, properties.getApiSecret());
        return properties.getBaseUrl()
                + path
                + "?"
                + queryString
                + "&signature="
                + signature;
    }
}
