package com.oscar.tradeWorkerBackend.metrics.context.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "daily_metrics_snapshot")
public class DailyMetricsSnapshot implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant asOf;

    @Column(precision = 38, scale = 18)
    private BigDecimal return1d;

    @Column(precision = 38, scale = 18)
    private BigDecimal return3d;

    @Column(precision = 38, scale = 18)
    private BigDecimal realizedVol7d;

    @Column(precision = 38, scale = 18)
    private BigDecimal atr14;

    @Column(precision = 38, scale = 18)
    private BigDecimal deltaOpenInterest24h;

    @Column(precision = 38, scale = 18)
    private BigDecimal fundingRateZScore30d;

    @Column(precision = 38, scale = 18)
    private BigDecimal takerBuySellRatio24h;

    @Column(precision = 38, scale = 18)
    private BigDecimal liquidationLongVolumeUsd24h;

    @Column(precision = 38, scale = 18)
    private BigDecimal liquidationShortVolumeUsd24h;

    @Column(precision = 38, scale = 18)
    private BigDecimal volumeRelative24h;

    public DailyMetricsSnapshot() {
    }

    public Long getId() {
        return id;
    }

    public Instant getAsOf() {
        return asOf;
    }

    public void setAsOf(Instant asOf) {
        this.asOf = asOf;
    }

    public BigDecimal getReturn1d() {
        return return1d;
    }

    public void setReturn1d(BigDecimal return1d) {
        this.return1d = return1d;
    }

    public BigDecimal getReturn3d() {
        return return3d;
    }

    public void setReturn3d(BigDecimal return3d) {
        this.return3d = return3d;
    }

    public BigDecimal getRealizedVol7d() {
        return realizedVol7d;
    }

    public void setRealizedVol7d(BigDecimal realizedVol7d) {
        this.realizedVol7d = realizedVol7d;
    }

    public BigDecimal getAtr14() {
        return atr14;
    }

    public void setAtr14(BigDecimal atr14) {
        this.atr14 = atr14;
    }

    public BigDecimal getDeltaOpenInterest24h() {
        return deltaOpenInterest24h;
    }

    public void setDeltaOpenInterest24h(BigDecimal deltaOpenInterest24h) {
        this.deltaOpenInterest24h = deltaOpenInterest24h;
    }

    public BigDecimal getFundingRateZScore30d() {
        return fundingRateZScore30d;
    }

    public void setFundingRateZScore30d(BigDecimal fundingRateZScore30d) {
        this.fundingRateZScore30d = fundingRateZScore30d;
    }

    public BigDecimal getTakerBuySellRatio24h() {
        return takerBuySellRatio24h;
    }

    public void setTakerBuySellRatio24h(BigDecimal takerBuySellRatio24h) {
        this.takerBuySellRatio24h = takerBuySellRatio24h;
    }

    public BigDecimal getLiquidationLongVolumeUsd24h() {
        return liquidationLongVolumeUsd24h;
    }

    public void setLiquidationLongVolumeUsd24h(BigDecimal liquidationLongVolumeUsd24h) {
        this.liquidationLongVolumeUsd24h = liquidationLongVolumeUsd24h;
    }

    public BigDecimal getLiquidationShortVolumeUsd24h() {
        return liquidationShortVolumeUsd24h;
    }

    public void setLiquidationShortVolumeUsd24h(BigDecimal liquidationShortVolumeUsd24h) {
        this.liquidationShortVolumeUsd24h = liquidationShortVolumeUsd24h;
    }

    public BigDecimal getVolumeRelative24h() {
        return volumeRelative24h;
    }

    public void setVolumeRelative24h(BigDecimal volumeRelative24h) {
        this.volumeRelative24h = volumeRelative24h;
    }
}
