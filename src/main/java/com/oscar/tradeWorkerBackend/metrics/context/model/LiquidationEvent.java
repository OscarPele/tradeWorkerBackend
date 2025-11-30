package com.oscar.tradeWorkerBackend.metrics.context.model;

import java.math.BigDecimal;

public class LiquidationEvent {

    private String symbol;
    private String side; // "BUY" o "SELL"
    private BigDecimal executedQty;
    private BigDecimal avgPrice;
    private long time;

    public LiquidationEvent() {
    }

    public LiquidationEvent(String symbol,
                            String side,
                            BigDecimal executedQty,
                            BigDecimal avgPrice,
                            long time) {
        this.symbol = symbol;
        this.side = side;
        this.executedQty = executedQty;
        this.avgPrice = avgPrice;
        this.time = time;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public BigDecimal getExecutedQty() {
        return executedQty;
    }

    public void setExecutedQty(BigDecimal executedQty) {
        this.executedQty = executedQty;
    }

    public BigDecimal getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(BigDecimal avgPrice) {
        this.avgPrice = avgPrice;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
