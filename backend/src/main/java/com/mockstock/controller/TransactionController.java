package com.mockstock.controller;

import com.mockstock.dto.ApiResponse;
import com.mockstock.entity.Transaction;
import com.mockstock.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Transaction>>> getTransactions() {
        List<Transaction> transactions = transactionService.getTransactions();
        return ResponseEntity.ok(ApiResponse.ok(transactions));
    }
}
