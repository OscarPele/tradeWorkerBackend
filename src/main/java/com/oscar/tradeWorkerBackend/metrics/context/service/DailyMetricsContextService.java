package com.oscar.tradeWorkerBackend.metrics.context.service;

import com.oscar.tradeWorkerBackend.metrics.context.client.BinanceFuturesClient;
import com.oscar.tradeWorkerBackend.metrics.context.client.BinanceFuturesLiquidationStream;
import com.oscar.tradeWorkerBackend.metrics.context.config.MetricsConfig;
import com.oscar.tradeWorkerBackend.metrics.context.model.Candle;
import com.oscar.tradeWorkerBackend.metrics.context.model.DailyMetricsSnapshot;
import com.oscar.tradeWorkerBackend.metrics.context.model.FundingRateSample;
import com.oscar.tradeWorkerBackend.metrics.context.model.LiquidationEvent;
import com.oscar.tradeWorkerBackend.metrics.context.model.OpenInterestSample;
import com.oscar.tradeWorkerBackend.metrics.context.model.TakerVolumeSample;
import com.oscar.tradeWorkerBackend.metrics.context.repository.DailyMetricsSnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class DailyMetricsContextService {

    private final BinanceFuturesClient binanceFuturesClient;
    private final BinanceFuturesLiquidationStream liquidationStream;
    private final DailyMetricsCalculator dailyMetricsCalculator;
    private final DailyMetricsSnapshotRepository snapshotRepository;

    public DailyMetricsContextService(BinanceFuturesClient binanceFuturesClient,
                                      BinanceFuturesLiquidationStream liquidationStream,
                                      DailyMetricsCalculator dailyMetricsCalculator,
                                      DailyMetricsSnapshotRepository snapshotRepository) {
        this.binanceFuturesClient = binanceFuturesClient;
        this.liquidationStream = liquidationStream;
        this.dailyMetricsCalculator = dailyMetricsCalculator;
        this.snapshotRepository = snapshotRepository;
    }

    /**
     * Calcula las métricas diarias actuales de BTCUSDT (sin persistir).
     */
    public DailyMetricsSnapshot computeCurrentBtcDailyMetrics() {
        // 40 días de klines dan margen para todas las ventanas (7d, 14d, 30d volumen, etc.)
        List<Candle> dailyKlines =
                binanceFuturesClient.getDailyKlines(MetricsConfig.BTC_PERPETUAL_SYMBOL, 40);

        OpenInterestSample[] oiHistory24h =
                binanceFuturesClient.getOpenInterestHistoryLast24h(MetricsConfig.BTC_PERPETUAL_SYMBOL);

        FundingRateSample[] fundingHistory30d =
                binanceFuturesClient.getFundingRateHistoryLast30d(MetricsConfig.BTC_PERPETUAL_SYMBOL);

        TakerVolumeSample[] takerSamples24h =
                binanceFuturesClient.getTakerBuySellLast24h(MetricsConfig.BTC_PERPETUAL_SYMBOL);

        List<LiquidationEvent> liquidationsLast24h =
                liquidationStream.getLiquidationsLast24h();

        DailyMetricsSnapshot snapshot = dailyMetricsCalculator.computeDailyMetrics(
                dailyKlines,
                oiHistory24h,
                fundingHistory30d,
                takerSamples24h,
                liquidationsLast24h
        );

        snapshot.setAsOf(Instant.now());
        return snapshot;
    }

    /**
     * Calcula y persiste las métricas diarias actuales.
     */
    @Transactional
    public DailyMetricsSnapshot computeAndSaveCurrentBtcDailyMetrics() {
        DailyMetricsSnapshot snapshot = computeCurrentBtcDailyMetrics();
        return snapshotRepository.save(snapshot);
    }

    /**
     * Recupera el último snapshot persistido.
     */
    public DailyMetricsSnapshot getLatestSnapshotFromDatabase() {
        return snapshotRepository.findTopByOrderByAsOfDesc().orElse(null);
    }
}
