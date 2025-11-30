package com.oscar.tradeWorkerBackend.binance.dto;

import java.math.BigDecimal;

public class SymbolFilters {

    private final BigDecimal tickSize;
    private final BigDecimal stepSize;

    public SymbolFilters(BigDecimal tickSize, BigDecimal stepSize) {
        this.tickSize = tickSize;
        this.stepSize = stepSize;
    }

    public BigDecimal getTickSize() {
        return tickSize;
    }

    public BigDecimal getStepSize() {
        return stepSize;
    }
}
