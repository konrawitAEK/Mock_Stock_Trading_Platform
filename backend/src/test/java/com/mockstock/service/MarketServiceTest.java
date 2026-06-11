package com.mockstock.service;

import com.mockstock.entity.Stock;
import com.mockstock.repository.StockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketServiceTest {

    @Mock StockRepository stockRepo;

    @InjectMocks MarketService marketService;

    private final Stock aapl = new Stock("AAPL", "Apple Inc.",   BigDecimal.valueOf(182.50), "Tech",       "Apple");
    private final Stock tsla = new Stock("TSLA", "Tesla Inc.",   BigDecimal.valueOf(238.00), "Automotive", "Tesla");

    @Test
    @DisplayName("1. Simulate market: prices change within ±5%")
    void simulateMarket_pricesWithinBounds() {
        Stock msft = new Stock("MSFT", "Microsoft", BigDecimal.valueOf(375.20), "Tech", "MS");
        when(stockRepo.findAll()).thenReturn(List.of(aapl, tsla, msft));
        when(stockRepo.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        List<Stock> result = marketService.simulateMarket();

        assertEquals(3, result.size());
        for (Stock s : result) {
            assertTrue(s.getChangePercent().abs().doubleValue() <= 5.01);
            assertTrue(s.getCurrentPrice().doubleValue() >= 0.01);
        }
    }

    @Test
    @DisplayName("2. Simulate market: previousPrice and dailyChange updated correctly")
    void simulateMarket_fieldsUpdated() {
        BigDecimal originalPrice = aapl.getCurrentPrice();
        when(stockRepo.findAll()).thenReturn(List.of(aapl));
        when(stockRepo.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        marketService.simulateMarket();

        assertEquals(0, originalPrice.compareTo(aapl.getPreviousPrice()));
        assertEquals(0, aapl.getCurrentPrice().subtract(originalPrice).compareTo(aapl.getDailyChange()));
    }
}
