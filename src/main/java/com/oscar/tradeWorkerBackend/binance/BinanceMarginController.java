package com.oscar.tradeWorkerBackend.binance;

import com.oscar.tradeWorkerBackend.binance.dto.MarginAccountResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/binance")
public class BinanceMarginController {

    private final BinanceClient binanceClient;

    public BinanceMarginController(BinanceClient binanceClient) {
        this.binanceClient = binanceClient;
    }

    @GetMapping("/margin-account")
    public MarginAccountResponse getMarginAccount() {
        return binanceClient.getMarginAccount();
    }
}
