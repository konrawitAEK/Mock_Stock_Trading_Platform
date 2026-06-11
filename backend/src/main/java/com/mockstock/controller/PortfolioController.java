package com.mockstock.controller;

import com.mockstock.dto.ApiResponse;
import com.mockstock.dto.response.PortfolioResponse;
import com.mockstock.service.PortfolioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PortfolioResponse>> getPortfolio() {
        return ResponseEntity.ok(ApiResponse.ok(portfolioService.buildPortfolioResponse()));
    }
}
