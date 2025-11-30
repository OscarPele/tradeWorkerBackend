package com.oscar.tradeWorkerBackend.binance;

import com.oscar.tradeWorkerBackend.binance.dto.CreateMarginTradeRequest;
import com.oscar.tradeWorkerBackend.binance.dto.MarginAccountResponse;
import com.oscar.tradeWorkerBackend.binance.dto.MarginTradeResponse;
import com.oscar.tradeWorkerBackend.binance.dto.OpenOrdersResponse;
import com.oscar.tradeWorkerBackend.binance.dto.OrderSide;
import com.oscar.tradeWorkerBackend.binance.dto.SymbolFilters;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class BinanceTradeService {

    private static final BigDecimal ORDER_BUFFER = new BigDecimal("0.99"); // reduce 1% to evitar 2010 por slippage/comisiones

    private final BinanceClient binanceClient;

    public BinanceTradeService(BinanceClient binanceClient) {
        this.binanceClient = binanceClient;
    }

    public MarginTradeResponse createMarginTrade(CreateMarginTradeRequest request) {
        validateRequest(request);

        String symbol = request.getSymbol().toUpperCase(Locale.ROOT).trim();
        OrderSide entrySide = request.getSide();

        BigDecimal referencePrice = BigDecimal.valueOf(binanceClient.getTickerPrice(symbol));
        SymbolFilters filters = binanceClient.getSymbolFilters(symbol);

        SymbolAssets assets = parseAssets(symbol);
        String assetToBorrow = entrySide == OrderSide.BUY ? assets.quoteAsset() : assets.baseAsset();

        MarginAccountResponse account = binanceClient.getMarginAccount();
        BigDecimal freeEquity = getFreeBalance(account, assetToBorrow);
        BigDecimal leverage = BigDecimal.valueOf(request.getLeverage());
        BigDecimal maxBorrowable = getMaxBorrowableAmount(assetToBorrow, request.isIsolated(), symbol);

        if (maxBorrowable.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Binance no permite pedir prestado " + assetToBorrow + " (máximo disponible: 0). Aporta colateral o revisa el modo (cross/isolated).");
        }

        BigDecimal targetBorrow = freeEquity.compareTo(BigDecimal.ZERO) > 0
                ? freeEquity.multiply(leverage.subtract(BigDecimal.ONE), MathContext.DECIMAL64)
                : maxBorrowable;

        BigDecimal borrowAmount = targetBorrow.min(maxBorrowable);

        if (borrowAmount.compareTo(BigDecimal.ZERO) <= 0) {
            borrowAmount = maxBorrowable;
        }

        BigDecimal totalToUse = freeEquity.add(borrowAmount);

        if (totalToUse.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("No hay fondos utilizables ni capacidad de préstamo en " + assetToBorrow);
        }

        BigDecimal quantity = entrySide == OrderSide.BUY
                ? totalToUse.divide(referencePrice, MathContext.DECIMAL64)
                : totalToUse;

        BigDecimal bufferedQuantity = quantity.multiply(ORDER_BUFFER);
        BigDecimal bufferedQuote = totalToUse.multiply(ORDER_BUFFER);

        if (bufferedQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad resultante es cero tras aplicar buffer; revisa fondos y apalancamiento.");
        }

        BigDecimal takeProfitPrice = calculateTakeProfit(referencePrice, request.getTakeProfitPercent(), entrySide);
        BigDecimal stopLossPrice = calculateStopLoss(referencePrice, request.getStopLossPercent(), entrySide);

        takeProfitPrice = applyTick(takeProfitPrice, filters.getTickSize());
        stopLossPrice = applyTick(stopLossPrice, filters.getTickSize());

        String quantityStr = formatAmount(applyStep(bufferedQuantity, filters.getStepSize()), scaleFrom(filters.getStepSize()));
        String quoteOrderQtyStr = entrySide == OrderSide.BUY
                ? formatAmount(bufferedQuote, scaleFrom(filters.getTickSize()))
                : null;
        String borrowAmountStr = formatAmount(borrowAmount, scaleFrom(filters.getTickSize()));
        String takeProfitStr = formatAmount(takeProfitPrice, scaleFrom(filters.getTickSize()));
        String stopLossStr = formatAmount(stopLossPrice, scaleFrom(filters.getTickSize()));

        Map<String, Object> borrowResponse = binanceClient.borrowAsset(
                assetToBorrow,
                borrowAmountStr,
                request.isIsolated(),
                symbol
        );

        Map<String, Object> entryOrder = binanceClient.placeMarginMarketOrder(
                symbol,
                entrySide,
                quantityStr,
                quoteOrderQtyStr,
                request.isIsolated()
        );

        Map<String, Object> ocoOrder = binanceClient.placeMarginOcoOrder(
                symbol,
                opposite(entrySide),
                quantityStr,
                takeProfitStr,
                stopLossStr,
                request.isIsolated()
        );

        MarginTradeResponse response = new MarginTradeResponse();
        response.setSymbol(symbol);
        response.setEntrySide(entrySide);
        response.setQuantity(quantityStr);
        response.setBorrowAsset(assetToBorrow);
        response.setBorrowAmount(borrowAmountStr);
        response.setReferencePrice(referencePrice.doubleValue());
        response.setTakeProfitPrice(takeProfitPrice.doubleValue());
        response.setStopLossPrice(stopLossPrice.doubleValue());
        response.setBorrowOrder(borrowResponse);
        response.setEntryOrder(entryOrder);
        response.setOcoOrder(ocoOrder);

        return response;
    }

    public OpenOrdersResponse getOpenOrders(String symbol, boolean isolated) {
        List<Map<String, Object>> orders = binanceClient.getOpenMarginOrders(symbol, isolated);
        return new OpenOrdersResponse(!orders.isEmpty(), orders);
    }

    private SymbolAssets parseAssets(String symbol) {
        List<String> knownQuotes = List.of("USDT", "USDC", "BUSD", "FDUSD", "TUSD", "DAI", "EUR", "BTC", "ETH");

        for (String quote : knownQuotes) {
            if (symbol.endsWith(quote)) {
                String base = symbol.substring(0, symbol.length() - quote.length());
                return new SymbolAssets(base, quote);
            }
        }

        throw new IllegalArgumentException("No se pudo deducir el par base/quote del símbolo " + symbol);
    }

    private BigDecimal calculateTakeProfit(BigDecimal reference, double percent, OrderSide side) {
        BigDecimal delta = percentage(reference, percent);
        return side == OrderSide.BUY
                ? reference.add(delta)
                : reference.subtract(delta);
    }

    private BigDecimal calculateStopLoss(BigDecimal reference, double percent, OrderSide side) {
        BigDecimal delta = percentage(reference, percent);
        BigDecimal stop = side == OrderSide.BUY
                ? reference.subtract(delta)
                : reference.add(delta);

        if (stop.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El cálculo del stop loss devuelve precio <= 0, revisa los porcentajes");
        }

        return stop;
    }

    private BigDecimal percentage(BigDecimal reference, double percent) {
        return reference.multiply(
                BigDecimal.valueOf(percent)
                        .divide(BigDecimal.valueOf(100), MathContext.DECIMAL64)
        );
    }

    private String formatAmount(BigDecimal value, int scale) {
        return value
                .setScale(scale, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    private OrderSide opposite(OrderSide side) {
        return side == OrderSide.BUY ? OrderSide.SELL : OrderSide.BUY;
    }

    private void validateRequest(CreateMarginTradeRequest request) {
        if (request.getSide() == null) {
            throw new IllegalArgumentException("El side de la orden es obligatorio (BUY o SELL)");
        }

        if (request.getTakeProfitPercent() <= 0 || request.getStopLossPercent() <= 0) {
            throw new IllegalArgumentException("Los porcentajes de TP/SL deben ser mayores que 0");
        }

        if (request.getLeverage() <= 1) {
            throw new IllegalArgumentException("El apalancamiento debe ser mayor que 1 (ej: 20)");
        }
    }

    private record SymbolAssets(String baseAsset, String quoteAsset) {
    }

    private BigDecimal getFreeBalance(MarginAccountResponse account, String asset) {
        if (account.getUserAssets() == null) {
            return BigDecimal.ZERO;
        }

        return account.getUserAssets()
                .stream()
                .filter(a -> asset.equalsIgnoreCase(a.getAsset()))
                .findFirst()
                .map(a -> new BigDecimal(a.getFree()))
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal getMaxBorrowableAmount(String asset, boolean isolated, String symbol) {
        var response = binanceClient.getMaxBorrowable(asset, isolated, symbol);
        String amount = response != null ? response.getAmount() : "0";
        if (amount == null) {
            amount = "0";
        }
        try {
            return new BigDecimal(amount);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("No se pudo parsear maxBorrowable para " + asset + ": " + amount, e);
        }
    }

    private BigDecimal applyTick(BigDecimal value, BigDecimal tickSize) {
        if (tickSize == null) {
            return value;
        }
        return value
                .divide(tickSize, 0, RoundingMode.DOWN)
                .multiply(tickSize);
    }

    private BigDecimal applyStep(BigDecimal value, BigDecimal stepSize) {
        if (stepSize == null) {
            return value;
        }
        return value
                .divide(stepSize, 0, RoundingMode.DOWN)
                .multiply(stepSize);
    }

    private int scaleFrom(BigDecimal value) {
        if (value == null) {
            return 8;
        }
        return Math.max(0, value.stripTrailingZeros().scale());
    }
}
