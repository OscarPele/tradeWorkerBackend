package com.oscar.tradeWorkerBackend.metrics.context.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class MetricsConfig {

    public static final String BINANCE_FUTURES_BASE_URL = "https://fapi.binance.com";
    public static final String BTC_PERPETUAL_SYMBOL = "BTCUSDT";

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
