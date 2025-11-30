package com.oscar.tradeWorkerBackend.metrics.context.service;

import com.oscar.tradeWorkerBackend.metrics.context.model.Candle;
import com.oscar.tradeWorkerBackend.metrics.context.model.DailyMetricsSnapshot;
import com.oscar.tradeWorkerBackend.metrics.context.model.FundingRateSample;
import com.oscar.tradeWorkerBackend.metrics.context.model.LiquidationEvent;
import com.oscar.tradeWorkerBackend.metrics.context.model.OpenInterestSample;
import com.oscar.tradeWorkerBackend.metrics.context.model.TakerVolumeSample;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

@Service
public class DailyMetricsCalculator {

    private static final MathContext MC = MathContext.DECIMAL64;

    public DailyMetricsSnapshot computeDailyMetrics(List<Candle> dailyKlines,
                                                    OpenInterestSample[] oiHistory,
                                                    FundingRateSample[] fundingHistory,
                                                    TakerVolumeSample[] takerSamples,
                                                    List<LiquidationEvent> liquidations24h) {
        DailyMetricsSnapshot snapshot = new DailyMetricsSnapshot();

        snapshot.setReturn1d(computeReturn(dailyKlines, 1));
        snapshot.setReturn3d(computeReturn(dailyKlines, 3));
        snapshot.setRealizedVol7d(computeRealizedVol(dailyKlines));
        snapshot.setAtr14(computeAtr(dailyKlines));
        snapshot.setDeltaOpenInterest24h(computeDeltaOpenInterest24h(oiHistory));
        snapshot.setFundingRateZScore30d(computeFundingZScore(fundingHistory));
        snapshot.setTakerBuySellRatio24h(computeTakerBuySellRatio24h(takerSamples));
        computeLiquidationVolumes(liquidations24h, snapshot);
        snapshot.setVolumeRelative24h(computeVolumeRelative(dailyKlines));

        return snapshot;
    }

    private BigDecimal computeReturn(List<Candle> klines, int days) {
        if (klines == null || klines.size() < days + 1) {
            return null;
        }
        int lastIndex = klines.size() - 1;
        int prevIndex = lastIndex - days;
        Candle last = klines.get(lastIndex);
        Candle prev = klines.get(prevIndex);
        BigDecimal closeNow = last.getClose();
        BigDecimal closePrev = prev.getClose();

        if (closePrev == null || closePrev.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return closeNow.divide(closePrev, MC).subtract(BigDecimal.ONE);
    }

    private BigDecimal computeRealizedVol(List<Candle> klines) {
        int windowDays = 7;

        if (klines == null || klines.size() < windowDays + 1) {
            return null;
        }

        int needed = windowDays + 1;
        int startIndex = klines.size() - needed;

        double[] returns = new double[windowDays];
        double sum = 0.0;

        for (int i = 0; i < windowDays; i++) {
            Candle prev = klines.get(startIndex + i);
            Candle curr = klines.get(startIndex + i + 1);

            BigDecimal closePrev = prev.getClose();
            BigDecimal closeCurr = curr.getClose();

            if (closePrev == null || closePrev.compareTo(BigDecimal.ZERO) == 0) {
                return null;
            }

            double r = closeCurr.divide(closePrev, MC)
                    .subtract(BigDecimal.ONE)
                    .doubleValue();
            returns[i] = r;
            sum += r;
        }

        double mean = sum / windowDays;
        double var = 0.0;

        for (double r : returns) {
            double diff = r - mean;
            var += diff * diff;
        }

        var = var / (windowDays - 1);

        double sigmaDaily = Math.sqrt(var);

        // anualizar sobre 365 días y pasar a porcentaje
        double sigmaAnnualPercent = sigmaDaily * Math.sqrt(365.0) * 100.0;

        return BigDecimal.valueOf(sigmaAnnualPercent);
    }


    private BigDecimal computeAtr(List<Candle> klines) {
        if (klines == null || klines.size() < 14 + 1) {
            return null;
        }

        int needed = 14 + 1;
        int startIndex = klines.size() - needed;

        double sumTr = 0.0;

        for (int i = 0; i < 14; i++) {
            Candle prev = klines.get(startIndex + i);
            Candle curr = klines.get(startIndex + i + 1);

            BigDecimal highLow = curr.getHigh().subtract(curr.getLow()).abs();
            BigDecimal highClosePrev = curr.getHigh().subtract(prev.getClose()).abs();
            BigDecimal lowClosePrev = curr.getLow().subtract(prev.getClose()).abs();

            BigDecimal tr = highLow.max(highClosePrev).max(lowClosePrev);
            sumTr += tr.doubleValue();
        }

        double atr = sumTr / 14;
        return BigDecimal.valueOf(atr);
    }

    private BigDecimal computeDeltaOpenInterest24h(OpenInterestSample[] oiHistory) {
        if (oiHistory == null || oiHistory.length < 2) {
            return null;
        }

        // Suponiendo el array ordenado de más antiguo a más reciente
        OpenInterestSample first = oiHistory[0];
        OpenInterestSample last = oiHistory[oiHistory.length - 1];

        BigDecimal oiFirst = first.getSumOpenInterest();
        BigDecimal oiLast = last.getSumOpenInterest();

        if (oiFirst == null || oiLast == null || oiFirst.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        // (OI_now - OI_prev) / OI_prev * 100 -> porcentaje
        BigDecimal change = oiLast.subtract(oiFirst);
        return change
                .divide(oiFirst, MC)
                .multiply(BigDecimal.valueOf(100));
    }


    private BigDecimal computeFundingZScore(FundingRateSample[] fundingHistory) {
        if (fundingHistory == null || fundingHistory.length < 5) {
            return null;
        }

        int n = fundingHistory.length;
        double[] values = new double[n];
        double sum = 0.0;

        for (int i = 0; i < n; i++) {
            BigDecimal fr = fundingHistory[i].getFundingRate();
            if (fr == null) {
                return null;
            }
            double v = fr.doubleValue();
            values[i] = v;
            sum += v;
        }

        double mean = sum / n;
        double var = 0.0;

        for (int i = 0; i < n; i++) {
            double diff = values[i] - mean;
            var += diff * diff;
        }

        var = var / (n - 1);

        double std = Math.sqrt(var);
        if (std == 0.0) {
            return BigDecimal.ZERO;
        }

        double latest = values[n - 1];
        double z = (latest - mean) / std;
        return BigDecimal.valueOf(z);
    }

    private BigDecimal computeTakerBuySellRatio24h(TakerVolumeSample[] samples) {
        if (samples == null || samples.length == 0) {
            return null;
        }

        BigDecimal buySum = BigDecimal.ZERO;
        BigDecimal sellSum = BigDecimal.ZERO;

        for (TakerVolumeSample s : samples) {
            if (s.getBuyVol() != null) {
                buySum = buySum.add(s.getBuyVol());
            }
            if (s.getSellVol() != null) {
                sellSum = sellSum.add(s.getSellVol());
            }
        }

        if (sellSum.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return buySum.divide(sellSum, MC);
    }

    private void computeLiquidationVolumes(List<LiquidationEvent> events, DailyMetricsSnapshot snapshot) {
        if (events == null || events.isEmpty()) {
            snapshot.setLiquidationLongVolumeUsd24h(null);
            snapshot.setLiquidationShortVolumeUsd24h(null);
            return;
        }

        BigDecimal longUsd = BigDecimal.ZERO;
        BigDecimal shortUsd = BigDecimal.ZERO;

        for (LiquidationEvent e : events) {
            if (e.getExecutedQty() == null || e.getAvgPrice() == null || e.getSide() == null) {
                continue;
            }

            BigDecimal notional = e.getExecutedQty().multiply(e.getAvgPrice());
            String sideUpper = e.getSide().toUpperCase();

            if ("SELL".equals(sideUpper)) {
                // Liquidación de largos
                longUsd = longUsd.add(notional);
            } else if ("BUY".equals(sideUpper)) {
                // Liquidación de cortos
                shortUsd = shortUsd.add(notional);
            }
        }

        snapshot.setLiquidationLongVolumeUsd24h(longUsd);
        snapshot.setLiquidationShortVolumeUsd24h(shortUsd);
    }

    private BigDecimal computeVolumeRelative(List<Candle> klines) {
        if (klines == null || klines.size() < 2) {
            return null;
        }

        int lastIndex = klines.size() - 1;
        Candle last = klines.get(lastIndex);
        BigDecimal volLast = last.getVolume();

        if (volLast == null) {
            return null;
        }

        BigDecimal sumPrev = BigDecimal.ZERO;
        int count = 0;

        for (int i = 0; i < lastIndex; i++) {
            BigDecimal v = klines.get(i).getVolume();
            if (v != null) {
                sumPrev = sumPrev.add(v);
                count++;
            }
        }

        if (count == 0) {
            return null;
        }

        BigDecimal avgPrev = sumPrev.divide(BigDecimal.valueOf(count), MC);

        if (avgPrev.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return volLast.divide(avgPrev, MC);
    }
}
