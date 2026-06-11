package com.mockstock.controller;

import com.mockstock.dto.ApiResponse;
import com.mockstock.entity.Transaction;
import com.mockstock.service.TradingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TradingService tradingService;

    public TransactionController(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    /**
     * GET /transactions — all transactions newest-first
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Transaction>>> getTransactions() {
        List<Transaction> transactions = tradingService.getTransactions();
        return ResponseEntity.ok(ApiResponse.ok(transactions));
    }
}
