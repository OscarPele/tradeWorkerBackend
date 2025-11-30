package com.oscar.tradeWorkerBackend.binance.dto.exchange;

import java.util.List;

public class SymbolInfo {

    private String symbol;
    private List<TradingFilter> filters;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public List<TradingFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<TradingFilter> filters) {
        this.filters = filters;
    }
}
