package com.oscar.tradeWorkerBackend.metrics.liquidity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@SuppressWarnings("unused") // la usa Spring
public class LiquidityService {

    private final String fredBaseUrl;
    private final String fredApiKey;
    private final String m2SeriesId;
    private final RestTemplate restTemplate;

    public LiquidityService(
            @Value("${fred.base-url}") String fredBaseUrl,
            @Value("${fred.api-key}") String fredApiKey,
            @Value("${fred.m2-series-id}") String m2SeriesId
    ) {
        this.fredBaseUrl = fredBaseUrl;
        this.fredApiKey = fredApiKey;
        this.m2SeriesId = m2SeriesId;
        this.restTemplate = new RestTemplate();
    }

    public LiquidityStatus getCurrentLiquidityStatus() {
        FredResponse response = fetchM2Series();

        if (response == null || response.observations == null || response.observations.isEmpty()) {
            throw new IllegalStateException("No se han recibido observaciones de FRED para M2");
        }

        // Filtramos valores válidos (FRED usa "." para datos faltantes)
        List<FredObservation> valid = response.observations.stream()
                .filter(o -> o.value != null && !o.value.equals("."))
                .sorted(Comparator.comparing(o -> LocalDate.parse(o.date)))
                .toList();

        if (valid.size() < 13) { // último + 12 meses atrás
            throw new IllegalStateException("No hay suficientes observaciones históricas para calcular el YoY de M2");
        }

        // Último valor (más reciente)
        FredObservation latest = valid.get(valid.size() - 1);
        LocalDate latestDate = LocalDate.parse(latest.date);
        BigDecimal latestValue = new BigDecimal(latest.value);

        // Aproximamos "hace un año" usando el dato 12 posiciones atrás
        FredObservation oneYearAgo = valid.get(valid.size() - 13);
        BigDecimal oneYearAgoValue = new BigDecimal(oneYearAgo.value);

        // YoY % = (latest - hace_un_año) / hace_un_año * 100
        BigDecimal yoy = latestValue
                .subtract(oneYearAgoValue)
                .divide(oneYearAgoValue, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        LiquidityRegime regime = classifyRegime(yoy);

        return new LiquidityStatus(
                latestDate.toString(),
                latestValue.setScale(2, RoundingMode.HALF_UP),
                yoy.setScale(2, RoundingMode.HALF_UP),
                regime.name()
        );
    }

    private FredResponse fetchM2Series() {
        String url = UriComponentsBuilder
                .fromUriString(fredBaseUrl + "/fred/series/observations") // evita warning de deprecated
                .queryParam("series_id", m2SeriesId)
                .queryParam("api_key", fredApiKey)
                .queryParam("file_type", "json")
                .build()
                .toUriString();

        return restTemplate.getForObject(url, FredResponse.class);
    }

    private LiquidityRegime classifyRegime(BigDecimal yoy) {
        BigDecimal expansionThreshold = BigDecimal.valueOf(0.5);    // > +0.5% = expansión
        BigDecimal contractionThreshold = BigDecimal.valueOf(-0.5); // < -0.5% = contracción

        if (yoy.compareTo(expansionThreshold) > 0) {
            return LiquidityRegime.EXPANSION;
        } else if (yoy.compareTo(contractionThreshold) < 0) {
            return LiquidityRegime.CONTRACTION;
        } else {
            return LiquidityRegime.NEUTRAL;
        }
    }

    // ============
    //  Tipos DTO
    // ============

    public enum LiquidityRegime {
        EXPANSION,
        CONTRACTION,
        NEUTRAL
    }

    public static class LiquidityStatus {
        public String date;
        public BigDecimal m2Value;
        public BigDecimal yoyChangePct;
        public String regime;

        public LiquidityStatus(String date,
                               BigDecimal m2Value,
                               BigDecimal yoyChangePct,
                               String regime) {
            this.date = date;
            this.m2Value = m2Value;
            this.yoyChangePct = yoyChangePct;
            this.regime = regime;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FredResponse {
        public List<FredObservation> observations;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FredObservation {
        public String date;
        public String value;
    }
}
