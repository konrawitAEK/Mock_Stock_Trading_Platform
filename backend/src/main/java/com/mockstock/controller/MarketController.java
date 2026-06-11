package com.mockstock.controller;

import com.mockstock.dto.ApiResponse;
import com.mockstock.model.Stock;
import com.mockstock.service.TradingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/market")
public class MarketController {

    private final TradingService tradingService;

    public MarketController(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    /**
     * POST /market/simulate — simulate price changes, return updated stocks
     */
    @PostMapping("/simulate")
    public ResponseEntity<ApiResponse<List<Stock>>> simulateMarket() {
        List<Stock> updatedStocks = tradingService.simulateMarket();
        return ResponseEntity.ok(ApiResponse.ok(updatedStocks, "Market simulation completed"));
    }
}
