package com.oscar.tradeWorkerBackend.binance.dto;

import java.util.List;
import java.util.Map;

public class OpenOrdersResponse {

    private boolean hasOpenOrders;
    private List<Map<String, Object>> orders;

    public OpenOrdersResponse() {
    }

    public OpenOrdersResponse(boolean hasOpenOrders, List<Map<String, Object>> orders) {
        this.hasOpenOrders = hasOpenOrders;
        this.orders = orders;
    }

    public boolean isHasOpenOrders() {
        return hasOpenOrders;
    }

    public void setHasOpenOrders(boolean hasOpenOrders) {
        this.hasOpenOrders = hasOpenOrders;
    }

    public List<Map<String, Object>> getOrders() {
        return orders;
    }

    public void setOrders(List<Map<String, Object>> orders) {
        this.orders = orders;
    }
}
