package com.oscar.tradeWorkerBackend.metrics.context.client;

import com.oscar.tradeWorkerBackend.metrics.context.config.MetricsConfig;
import com.oscar.tradeWorkerBackend.metrics.context.model.Candle;
import com.oscar.tradeWorkerBackend.metrics.context.model.FundingRateSample;
import com.oscar.tradeWorkerBackend.metrics.context.model.OpenInterestSample;
import com.oscar.tradeWorkerBackend.metrics.context.model.TakerVolumeSample;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class BinanceFuturesClient {

    private final RestTemplate restTemplate;

    public BinanceFuturesClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Candle> getDailyKlines(String symbol, int limit) {
        String url = MetricsConfig.BINANCE_FUTURES_BASE_URL
                + "/fapi/v1/klines?symbol=" + symbol
                + "&interval=1d"
                + "&limit=" + limit;

        Object[][] raw = restTemplate.getForObject(url, Object[][].class);
        List<Candle> candles = new ArrayList<>();
        if (raw == null) {
            return candles;
        }

        for (Object[] entry : raw) {
            if (entry == null || entry.length < 7) {
                continue;
            }
            long openTime = ((Number) entry[0]).longValue();
            BigDecimal open = new BigDecimal((String) entry[1]);
            BigDecimal high = new BigDecimal((String) entry[2]);
            BigDecimal low = new BigDecimal((String) entry[3]);
            BigDecimal close = new BigDecimal((String) entry[4]);
            BigDecimal volume = new BigDecimal((String) entry[5]);
            long closeTime = ((Number) entry[6]).longValue();

            candles.add(new Candle(openTime, closeTime, open, high, low, close, volume));
        }

        return candles;
    }

    public OpenInterestSample[] getOpenInterestHistoryLast24h(String symbol) {
        String url = MetricsConfig.BINANCE_FUTURES_BASE_URL
                + "/futures/data/openInterestHist?symbol=" + symbol
                + "&period=1h"
                + "&limit=24";

        return restTemplate.getForObject(url, OpenInterestSample[].class);
    }

    public FundingRateSample[] getFundingRateHistoryLast30d(String symbol) {
        long now = System.currentTimeMillis();
        long thirtyDaysAgo = now - Duration.ofDays(30).toMillis();

        String url = MetricsConfig.BINANCE_FUTURES_BASE_URL
                + "/fapi/v1/fundingRate?symbol=" + symbol
                + "&startTime=" + thirtyDaysAgo
                + "&endTime=" + now
                + "&limit=1000";

        return restTemplate.getForObject(url, FundingRateSample[].class);
    }

    public TakerVolumeSample[] getTakerBuySellLast24h(String symbol) {
        String url = MetricsConfig.BINANCE_FUTURES_BASE_URL
                + "/futures/data/takerlongshortRatio?symbol=" + symbol
                + "&period=1h"
                + "&limit=24";

        return restTemplate.getForObject(url, TakerVolumeSample[].class);
    }
}
