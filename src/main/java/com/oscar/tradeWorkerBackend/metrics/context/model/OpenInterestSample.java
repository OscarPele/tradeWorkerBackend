package com.oscar.tradeWorkerBackend.metrics.context.model;

import java.math.BigDecimal;

public class OpenInterestSample {

    private String symbol;
    private BigDecimal sumOpenInterest;
    private BigDecimal sumOpenInterestValue;
    private String CMCCirculatingSupply;
    private long timestamp;

    public OpenInterestSample() {
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getSumOpenInterest() {
        return sumOpenInterest;
    }

    public void setSumOpenInterest(BigDecimal sumOpenInterest) {
        this.sumOpenInterest = sumOpenInterest;
    }

    public BigDecimal getSumOpenInterestValue() {
        return sumOpenInterestValue;
    }

    public void setSumOpenInterestValue(BigDecimal sumOpenInterestValue) {
        this.sumOpenInterestValue = sumOpenInterestValue;
    }

    public String getCMCCirculatingSupply() {
        return CMCCirculatingSupply;
    }

    public void setCMCCirculatingSupply(String CMCCirculatingSupply) {
        this.CMCCirculatingSupply = CMCCirculatingSupply;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
