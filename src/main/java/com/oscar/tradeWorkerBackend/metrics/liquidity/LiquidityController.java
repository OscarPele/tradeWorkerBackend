package com.oscar.tradeWorkerBackend.metrics.liquidity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SuppressWarnings("unused") // la usa Spring por reflexión
public class LiquidityController {

    private final LiquidityService liquidityService;

    public LiquidityController(LiquidityService liquidityService) {
        this.liquidityService = liquidityService;
    }

    @GetMapping("/api/liquidity/status")
    public LiquidityService.LiquidityStatus getLiquidityStatus() {
        return liquidityService.getCurrentLiquidityStatus();
    }
}
