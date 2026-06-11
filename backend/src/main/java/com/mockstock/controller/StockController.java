package com.mockstock.controller;

import com.mockstock.dto.ApiResponse;
import com.mockstock.dto.response.StockDetailResponse;
import com.mockstock.entity.Stock;
import com.mockstock.service.TradingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/stocks")
public class StockController {

    private final TradingService tradingService;

    public StockController(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Stock>>> getAllStocks() {
        List<Stock> stocks = new ArrayList<>(tradingService.getStocks().values());
        return ResponseEntity.ok(ApiResponse.ok(stocks));
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<ApiResponse<StockDetailResponse>> getStockDetail(
            @PathVariable String symbol) {

        Stock stock = tradingService.getStock(symbol.toUpperCase());
        if (stock == null) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("Stock not found: " + symbol));
        }

        return ResponseEntity.ok(ApiResponse.ok(
                StockDetailResponse.from(stock, tradingService.getPortfolioItem(symbol.toUpperCase()))
        ));
    }
}
