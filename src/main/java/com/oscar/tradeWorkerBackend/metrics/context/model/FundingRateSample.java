package com.oscar.tradeWorkerBackend.metrics.context.model;

import java.math.BigDecimal;

public class FundingRateSample {

    private String symbol;
    private BigDecimal fundingRate;
    private long fundingTime;
    private BigDecimal markPrice;

    public FundingRateSample() {
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getFundingRate() {
        return fundingRate;
    }

    public void setFundingRate(BigDecimal fundingRate) {
        this.fundingRate = fundingRate;
    }

    public long getFundingTime() {
        return fundingTime;
    }

    public void setFundingTime(long fundingTime) {
        this.fundingTime = fundingTime;
    }

    public BigDecimal getMarkPrice() {
        return markPrice;
    }

    public void setMarkPrice(BigDecimal markPrice) {
        this.markPrice = markPrice;
    }
}
