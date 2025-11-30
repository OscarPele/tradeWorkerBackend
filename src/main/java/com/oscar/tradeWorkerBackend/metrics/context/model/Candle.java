package com.oscar.tradeWorkerBackend.metrics.context.model;

import java.math.BigDecimal;

public class Candle {

    private final long openTime;
    private final long closeTime;
    private final BigDecimal open;
    private final BigDecimal high;
    private final BigDecimal low;
    private final BigDecimal close;
    private final BigDecimal volume;

    public Candle(long openTime,
                  long closeTime,
                  BigDecimal open,
                  BigDecimal high,
                  BigDecimal low,
                  BigDecimal close,
                  BigDecimal volume) {
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    public long getOpenTime() {
        return openTime;
    }

    public long getCloseTime() {
        return closeTime;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public BigDecimal getClose() {
        return close;
    }

    public BigDecimal getVolume() {
        return volume;
    }
}
