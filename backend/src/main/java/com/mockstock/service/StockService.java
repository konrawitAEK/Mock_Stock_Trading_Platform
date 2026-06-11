package com.mockstock.service;

import com.mockstock.entity.Stock;
import com.mockstock.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StockService {

    private final StockRepository stockRepo;

    public StockService(StockRepository stockRepo) {
        this.stockRepo = stockRepo;
    }

    @Transactional(readOnly = true)
    public Map<String, Stock> getStocks() {
        return stockRepo.findAll().stream()
                .collect(Collectors.toMap(Stock::getSymbol, s -> s, (a, b) -> a, LinkedHashMap::new));
    }

    @Transactional(readOnly = true)
    public Stock getStock(String symbol) {
        return stockRepo.findById(symbol).orElse(null);
    }
}
