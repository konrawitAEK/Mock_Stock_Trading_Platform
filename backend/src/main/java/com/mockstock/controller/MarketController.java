package com.mockstock.controller;

import com.mockstock.dto.ApiResponse;
import com.mockstock.entity.Stock;
import com.mockstock.service.MarketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/market")
public class MarketController {

    private final MarketService marketService;

    public MarketController(MarketService marketService) {
        this.marketService = marketService;
    }

    @PostMapping("/simulate")
    public ResponseEntity<ApiResponse<List<Stock>>> simulateMarket() {
        List<Stock> updatedStocks = marketService.simulateMarket();
        return ResponseEntity.ok(ApiResponse.ok(updatedStocks, "Market simulation completed"));
    }
}
