package com.oscar.tradeWorkerBackend.binance;

import com.oscar.tradeWorkerBackend.binance.dto.CreateMarginTradeRequest;
import com.oscar.tradeWorkerBackend.binance.dto.MarginAccountResponse;
import com.oscar.tradeWorkerBackend.binance.dto.MarginTradeResponse;
import com.oscar.tradeWorkerBackend.binance.dto.OpenOrdersResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/binance")
public class BinanceMarginController {

    private final BinanceClient binanceClient;
    private final BinanceTradeService binanceTradeService;

    public BinanceMarginController(
            BinanceClient binanceClient,
            BinanceTradeService binanceTradeService
    ) {
        this.binanceClient = binanceClient;
        this.binanceTradeService = binanceTradeService;
    }

    @GetMapping("/margin-account")
    public MarginAccountResponse getMarginAccount() {
        return binanceClient.getMarginAccount();
    }

    @PostMapping("/margin/order/oco")
    public MarginTradeResponse createMarginOcoOrder(
            @Valid @RequestBody CreateMarginTradeRequest request
    ) {
        return binanceTradeService.createMarginTrade(request);
    }

    @GetMapping("/margin/open-orders")
    public OpenOrdersResponse getOpenOrders(
            @RequestParam(defaultValue = "BTCUSDC") String symbol,
            @RequestParam(defaultValue = "false") boolean isolated
    ) {
        return binanceTradeService.getOpenOrders(symbol, isolated);
    }
}
