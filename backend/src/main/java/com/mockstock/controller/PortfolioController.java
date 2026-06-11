package com.mockstock.controller;

import com.mockstock.dto.ApiResponse;
import com.mockstock.dto.response.PortfolioResponse;
import com.mockstock.service.TradingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portfolio")
public class PortfolioController {

    private final TradingService tradingService;

    public PortfolioController(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PortfolioResponse>> getPortfolio() {
        return ResponseEntity.ok(ApiResponse.ok(tradingService.buildPortfolioResponse()));
    }
}
