package com.mockstock.controller;

import com.mockstock.dto.ApiResponse;
import com.mockstock.dto.response.StockDetailResponse;
import com.mockstock.entity.Stock;
import com.mockstock.service.PortfolioService;
import com.mockstock.service.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/stocks")
public class StockController {

    private final StockService stockService;
    private final PortfolioService portfolioService;

    public StockController(StockService stockService, PortfolioService portfolioService) {
        this.stockService = stockService;
        this.portfolioService = portfolioService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Stock>>> getAllStocks() {
        List<Stock> stocks = new ArrayList<>(stockService.getStocks().values());
        return ResponseEntity.ok(ApiResponse.ok(stocks));
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<ApiResponse<StockDetailResponse>> getStockDetail(
            @PathVariable String symbol) {

        Stock stock = stockService.getStock(symbol.toUpperCase());
        if (stock == null) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("Stock not found: " + symbol));
        }

        return ResponseEntity.ok(ApiResponse.ok(
                StockDetailResponse.from(stock, portfolioService.getPortfolioItem(symbol.toUpperCase()))
        ));
    }
}
