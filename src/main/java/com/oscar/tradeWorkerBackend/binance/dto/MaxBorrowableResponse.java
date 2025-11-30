package com.oscar.tradeWorkerBackend.binance.dto;

public class MaxBorrowableResponse {

    private String amount;
    private String borrowLimit;

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getBorrowLimit() {
        return borrowLimit;
    }

    public void setBorrowLimit(String borrowLimit) {
        this.borrowLimit = borrowLimit;
    }
}
