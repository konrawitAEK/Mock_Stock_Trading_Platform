package com.mockstock.controller;

import com.mockstock.dto.ApiResponse;
import com.mockstock.dto.request.OrderRequest;
import com.mockstock.dto.request.SymbolRequest;
import com.mockstock.dto.response.PortfolioResponse;
import com.mockstock.dto.response.TradeLimitsResponse;
import com.mockstock.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/limits")
    public ResponseEntity<ApiResponse<TradeLimitsResponse>> getTradeLimits(@RequestBody SymbolRequest request) {
        TradeLimitsResponse limits = orderService.getTradeLimits(request.getSymbol().toUpperCase());
        return ResponseEntity.ok(ApiResponse.ok(limits, "OK"));
    }

    @PostMapping("/buy")
    public ResponseEntity<ApiResponse<PortfolioResponse>> buyStock(
            @RequestBody OrderRequest request) {
        PortfolioResponse portfolio = orderService.buyStock(
                request.getSymbol().toUpperCase(), request.getQuantity());
        return ResponseEntity.ok(ApiResponse.ok(portfolio, "Buy order executed successfully"));
    }

    @PostMapping("/sell")
    public ResponseEntity<ApiResponse<PortfolioResponse>> sellStock(
            @RequestBody OrderRequest request) {
        PortfolioResponse portfolio = orderService.sellStock(
                request.getSymbol().toUpperCase(), request.getQuantity());
        return ResponseEntity.ok(ApiResponse.ok(portfolio, "Sell order executed successfully"));
    }
}
