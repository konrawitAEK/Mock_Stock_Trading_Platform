package com.mockstock.service;

import com.mockstock.entity.Stock;
import com.mockstock.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;

@Service
public class MarketService {

    private final StockRepository stockRepo;
    private final Random random = new Random();

    public MarketService(StockRepository stockRepo) {
        this.stockRepo = stockRepo;
    }

    @Transactional
    public List<Stock> simulateMarket() {
        List<Stock> stocks = stockRepo.findAll();
        for (Stock stock : stocks) {
            double changePercent = (random.nextDouble() * 10.0) - 5.0;
            BigDecimal prev = stock.getCurrentPrice();
            BigDecimal next = prev.multiply(BigDecimal.ONE.add(BigDecimal.valueOf(changePercent / 100.0)));
            BigDecimal minPrice = new BigDecimal("0.01");
            if (next.compareTo(minPrice) < 0) next = minPrice;
            stock.setPreviousPrice(prev);
            stock.setCurrentPrice(next);
            stock.setDailyChange(next.subtract(prev));
            stock.setChangePercent(next.subtract(prev).divide(prev, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)));
        }
        stockRepo.saveAll(stocks);
        return stocks;
    }
}
