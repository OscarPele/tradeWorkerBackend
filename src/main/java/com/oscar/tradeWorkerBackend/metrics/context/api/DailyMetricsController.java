package com.oscar.tradeWorkerBackend.metrics.context.api;

import com.oscar.tradeWorkerBackend.metrics.context.model.DailyMetricsSnapshot;
import com.oscar.tradeWorkerBackend.metrics.context.service.DailyMetricsContextService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/metrics/btc/daily")
public class DailyMetricsController {

    private final DailyMetricsContextService contextService;

    public DailyMetricsController(DailyMetricsContextService contextService) {
        this.contextService = contextService;
    }

    /**
     * Devuelve el último snapshot almacenado en BBDD.
     * Si no existe ninguno, calcula y persiste el primero y lo devuelve.
     * GET /metrics/btc/daily/latest
     */
    @GetMapping("/latest")
    public DailyMetricsSnapshot getLatestPersisted() {
        DailyMetricsSnapshot snapshot = contextService.getLatestSnapshotFromDatabase();

        if (snapshot == null) {
            // Primera llamada: no hay datos, los generamos y guardamos
            snapshot = contextService.computeAndSaveCurrentBtcDailyMetrics();
            if (snapshot == null) {
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "No se pudieron generar las métricas actuales"
                );
            }
        }

        return snapshot;
    }
}
