package com.oscar.tradeWorkerBackend.metrics.context.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oscar.tradeWorkerBackend.metrics.context.config.MetricsConfig;
import com.oscar.tradeWorkerBackend.metrics.context.model.LiquidationEvent;
import jakarta.annotation.PostConstruct;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Component
public class BinanceFuturesLiquidationStream implements WebSocketHandler {

    private static final String STREAM_URL = "wss://fstream.binance.com/ws/!forceOrder@arr";

    private final ObjectMapper objectMapper;
    private final List<LiquidationEvent> events = new CopyOnWriteArrayList<>();

    public BinanceFuturesLiquidationStream(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    @SuppressWarnings("unused") // lo llama Spring, no código directo
    public void connect() {
        StandardWebSocketClient client = new StandardWebSocketClient();
        client.execute(this, STREAM_URL)
                .whenComplete((session, throwable) -> {
                    // aquí puedes hacer log del error si quieres
                    // log.error("Error al conectar con stream de liquidaciones", throwable);
                });
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        // opcional: log de conexión abierta
        // log.info("Conectado a stream de liquidaciones");
    }

    @Override
    public void handleMessage(@NonNull WebSocketSession session,
                              @NonNull WebSocketMessage<?> message) throws Exception {
        if (!(message instanceof TextMessage textMessage)) {
            return;
        }
        String payload = textMessage.getPayload();
        try {
            handlePayload(payload);
        } catch (IOException e) {
            // log del error si quieres
            // log.warn("Error parseando mensaje de liquidación", e);
        }
    }

    private void handlePayload(String payload) throws IOException {
        JsonNode root = objectMapper.readTree(payload);

        // estructura: { "e": "forceOrder", "E": ..., "o": { ... } }
        JsonNode orderNode = root.path("o");
        if (orderNode.isMissingNode()) {
            return;
        }

        String symbol = orderNode.path("s").asText("");
        if (!MetricsConfig.BTC_PERPETUAL_SYMBOL.equals(symbol)) {
            return; // solo BTCUSDT
        }

        String side = orderNode.path("S").asText("");
        String qtyStr = orderNode.path("z").asText("0");   // qty ejecutada
        String avgPriceStr = orderNode.path("ap").asText("0");
        long time = orderNode.path("T").asLong(root.path("E").asLong(System.currentTimeMillis()));

        BigDecimal executedQty = new BigDecimal(qtyStr);
        BigDecimal avgPrice = new BigDecimal(avgPriceStr);

        LiquidationEvent event = new LiquidationEvent(symbol, side, executedQty, avgPrice, time);
        events.add(event);
        trimOldEvents();
    }

    private void trimOldEvents() {
        long cutoff = System.currentTimeMillis() - Duration.ofHours(24).toMillis();
        events.removeIf(e -> e.getTime() < cutoff);
    }

    public List<LiquidationEvent> getLiquidationsLast24h() {
        long cutoff = System.currentTimeMillis() - Duration.ofHours(24).toMillis();
        return events.stream()
                .filter(e -> e.getTime() >= cutoff)
                .collect(Collectors.toList());
    }

    @Override
    public void handleTransportError(@NonNull WebSocketSession session,
                                     @NonNull Throwable exception) {
        // opcional: log + reconexión
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session,
                                      @NonNull CloseStatus closeStatus) {
        // opcional: log + reconexión
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
