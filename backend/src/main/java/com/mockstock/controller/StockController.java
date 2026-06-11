package com.mockstock.controller;

import com.mockstock.dto.ApiResponse;
import com.mockstock.dto.StockDetailResponse;
import com.mockstock.model.PortfolioItem;
import com.mockstock.model.Stock;
import com.mockstock.store.InMemoryStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/stocks")
public class StockController {

    private final InMemoryStore store;

    public StockController(InMemoryStore store) {
        this.store = store;
    }

    /**
     * GET /stocks — list all stocks
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Stock>>> getAllStocks() {
        List<Stock> stocks = new ArrayList<>(store.getStocks().values());
        return ResponseEntity.ok(ApiResponse.ok(stocks));
    }

    /**
     * GET /stocks/{symbol} — stock detail with user holding info
     */
    @GetMapping("/{symbol}")
    public ResponseEntity<ApiResponse<StockDetailResponse>> getStockDetail(
            @PathVariable String symbol) {

        Stock stock = store.getStock(symbol.toUpperCase());
        if (stock == null) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("Stock not found: " + symbol));
        }

        PortfolioItem holding = store.getPortfolioItem(symbol.toUpperCase());
        int heldQuantity = holding != null ? holding.getQuantity() : 0;
        double avgBuyPrice = holding != null ? holding.getAvgBuyPrice() : 0.0;

        StockDetailResponse detail = new StockDetailResponse(
                stock.getSymbol(),
                stock.getCompanyName(),
                stock.getCurrentPrice(),
                stock.getPreviousPrice(),
                stock.getDailyChange(),
                stock.getChangePercent(),
                stock.getSector(),
                stock.getDescription(),
                heldQuantity,
                avgBuyPrice
        );

        return ResponseEntity.ok(ApiResponse.ok(detail));
    }
}
