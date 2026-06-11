package com.mockstock.controller;

import com.mockstock.dto.ApiResponse;
import com.mockstock.dto.PortfolioResponse;
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

    /**
     * GET /portfolio — portfolio summary with holdings
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PortfolioResponse>> getPortfolio() {
        PortfolioResponse portfolio = tradingService.buildPortfolioResponse();
        return ResponseEntity.ok(ApiResponse.ok(portfolio));
    }
}
