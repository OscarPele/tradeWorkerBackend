package com.oscar.tradeWorkerBackend.binance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateMarginTradeRequest {

    @NotBlank
    private String symbol = "BTCUSDC";

    @NotNull
    private OrderSide side;

    /**
     * Porcentaje de beneficio sobre el precio de entrada para colocar el TP.
     * Ej: 1.5 = 1.5% sobre el precio actual.
     */
    private double takeProfitPercent;

    /**
     * Porcentaje de pérdida asumida sobre el precio de entrada para colocar el SL.
     * Ej: 0.8 = -0.8% sobre el precio actual.
     */
    private double stopLossPercent;

    private boolean isolated = false;

    private double leverage = 20;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public OrderSide getSide() {
        return side;
    }

    public void setSide(OrderSide side) {
        this.side = side;
    }

    public double getTakeProfitPercent() {
        return takeProfitPercent;
    }

    public void setTakeProfitPercent(double takeProfitPercent) {
        this.takeProfitPercent = takeProfitPercent;
    }

    public double getStopLossPercent() {
        return stopLossPercent;
    }

    public void setStopLossPercent(double stopLossPercent) {
        this.stopLossPercent = stopLossPercent;
    }

    public boolean isIsolated() {
        return isolated;
    }

    public void setIsolated(boolean isolated) {
        this.isolated = isolated;
    }

    public double getLeverage() {
        return leverage;
    }

    public void setLeverage(double leverage) {
        this.leverage = leverage;
    }
}
