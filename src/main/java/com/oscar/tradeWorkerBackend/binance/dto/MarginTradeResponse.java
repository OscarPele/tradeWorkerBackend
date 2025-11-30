package com.oscar.tradeWorkerBackend.binance.dto;

import java.util.Map;

public class MarginTradeResponse {

    private String symbol;
    private OrderSide entrySide;
    private String quantity;
    private String borrowAsset;
    private String borrowAmount;
    private double referencePrice;
    private double takeProfitPrice;
    private double stopLossPrice;
    private Map<String, Object> borrowOrder;
    private Map<String, Object> entryOrder;
    private Map<String, Object> ocoOrder;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public OrderSide getEntrySide() {
        return entrySide;
    }

    public void setEntrySide(OrderSide entrySide) {
        this.entrySide = entrySide;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getBorrowAsset() {
        return borrowAsset;
    }

    public void setBorrowAsset(String borrowAsset) {
        this.borrowAsset = borrowAsset;
    }

    public String getBorrowAmount() {
        return borrowAmount;
    }

    public void setBorrowAmount(String borrowAmount) {
        this.borrowAmount = borrowAmount;
    }

    public double getReferencePrice() {
        return referencePrice;
    }

    public void setReferencePrice(double referencePrice) {
        this.referencePrice = referencePrice;
    }

    public double getTakeProfitPrice() {
        return takeProfitPrice;
    }

    public void setTakeProfitPrice(double takeProfitPrice) {
        this.takeProfitPrice = takeProfitPrice;
    }

    public double getStopLossPrice() {
        return stopLossPrice;
    }

    public void setStopLossPrice(double stopLossPrice) {
        this.stopLossPrice = stopLossPrice;
    }

    public Map<String, Object> getBorrowOrder() {
        return borrowOrder;
    }

    public void setBorrowOrder(Map<String, Object> borrowOrder) {
        this.borrowOrder = borrowOrder;
    }

    public Map<String, Object> getEntryOrder() {
        return entryOrder;
    }

    public void setEntryOrder(Map<String, Object> entryOrder) {
        this.entryOrder = entryOrder;
    }

    public Map<String, Object> getOcoOrder() {
        return ocoOrder;
    }

    public void setOcoOrder(Map<String, Object> ocoOrder) {
        this.ocoOrder = ocoOrder;
    }
}
