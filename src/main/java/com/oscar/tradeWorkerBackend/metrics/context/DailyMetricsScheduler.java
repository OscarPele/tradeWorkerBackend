package com.oscar.tradeWorkerBackend.metrics.context;

import com.oscar.tradeWorkerBackend.metrics.context.service.DailyMetricsContextService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyMetricsScheduler {

    private final DailyMetricsContextService contextService;

    public DailyMetricsScheduler(DailyMetricsContextService contextService) {
        this.contextService = contextService;
    }

    /**
     * Recalcula y persiste las métricas de BTCUSDT periódicamente.
     * Ejemplo: cada 15 minutos.
     */
    @Scheduled(cron = "0 */15 * * * *")
    public void updateBtcDailyMetricsPeriodically() {
        contextService.computeAndSaveCurrentBtcDailyMetrics();
    }
}
