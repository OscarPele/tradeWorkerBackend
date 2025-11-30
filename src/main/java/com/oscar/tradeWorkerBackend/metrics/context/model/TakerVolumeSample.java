package com.oscar.tradeWorkerBackend.metrics.context.model;

import java.math.BigDecimal;

public class TakerVolumeSample {

    private BigDecimal buySellRatio;
    private BigDecimal buyVol;
    private BigDecimal sellVol;
    private long timestamp;

    public TakerVolumeSample() {
    }

    public BigDecimal getBuySellRatio() {
        return buySellRatio;
    }

    public void setBuySellRatio(BigDecimal buySellRatio) {
        this.buySellRatio = buySellRatio;
    }

    public BigDecimal getBuyVol() {
        return buyVol;
    }

    public void setBuyVol(BigDecimal buyVol) {
        this.buyVol = buyVol;
    }

    public BigDecimal getSellVol() {
        return sellVol;
    }

    public void setSellVol(BigDecimal sellVol) {
        this.sellVol = sellVol;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
