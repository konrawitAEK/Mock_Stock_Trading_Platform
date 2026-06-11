package com.mockstock.controller;

import com.mockstock.dto.ApiResponse;
import com.mockstock.dto.request.OrderRequest;
import com.mockstock.dto.response.PortfolioResponse;
import com.mockstock.service.TradingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final TradingService tradingService;

    public OrderController(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    @PostMapping("/buy")
    public ResponseEntity<ApiResponse<PortfolioResponse>> buyStock(
            @RequestBody OrderRequest request) {
        PortfolioResponse portfolio = tradingService.buyStock(
                request.getSymbol().toUpperCase(), request.getQuantity());
        return ResponseEntity.ok(ApiResponse.ok(portfolio, "Buy order executed successfully"));
    }

    @PostMapping("/sell")
    public ResponseEntity<ApiResponse<PortfolioResponse>> sellStock(
            @RequestBody OrderRequest request) {
        PortfolioResponse portfolio = tradingService.sellStock(
                request.getSymbol().toUpperCase(), request.getQuantity());
        return ResponseEntity.ok(ApiResponse.ok(portfolio, "Sell order executed successfully"));
    }
}
