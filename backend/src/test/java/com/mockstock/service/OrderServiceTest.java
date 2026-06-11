package com.mockstock.service;

import com.mockstock.dto.response.PortfolioResponse;
import com.mockstock.entity.PortfolioItem;
import com.mockstock.entity.Stock;
import com.mockstock.entity.Transaction;
import com.mockstock.entity.TransactionType;
import com.mockstock.repository.PortfolioItemRepository;
import com.mockstock.repository.StockRepository;
import com.mockstock.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock StockRepository stockRepo;
    @Mock PortfolioItemRepository portfolioRepo;
    @Mock TransactionRepository txRepo;
    @Mock PortfolioService portfolioService;

    @InjectMocks OrderService orderService;

    private final Stock aapl = new Stock("AAPL", "Apple Inc.",   BigDecimal.valueOf(182.50), "Tech",       "Apple");
    private final Stock tsla = new Stock("TSLA", "Tesla Inc.",   BigDecimal.valueOf(238.00), "Automotive", "Tesla");
    private final Stock nvda = new Stock("NVDA", "NVIDIA Corp.", BigDecimal.valueOf(495.00), "Semi",       "NVIDIA");

    @BeforeEach
    void setUp() {
        lenient().when(portfolioService.getCash()).thenReturn(new BigDecimal("100000.00"));
        lenient().when(portfolioService.buildPortfolioResponse()).thenReturn(
                new PortfolioResponse(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, List.of()));
        lenient().when(portfolioRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(txRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    // =========================================================================
    // BUY TESTS
    // =========================================================================

    @Test
    @DisplayName("Buy success: cash decreases, holding saved, transaction recorded")
    void buyStock_success() {
        when(stockRepo.findById("AAPL")).thenReturn(Optional.of(aapl));
        when(portfolioRepo.findById("AAPL")).thenReturn(Optional.empty());

        double expectedCost = 182.50 * 10;
        orderService.buyStock("AAPL", 10);

        ArgumentCaptor<BigDecimal> cashCap = ArgumentCaptor.forClass(BigDecimal.class);
        verify(portfolioService).setCash(cashCap.capture());
        assertEquals(100_000.0 - expectedCost, cashCap.getValue().doubleValue(), 0.001);

        ArgumentCaptor<PortfolioItem> itemCap = ArgumentCaptor.forClass(PortfolioItem.class);
        verify(portfolioRepo).save(itemCap.capture());
        assertEquals("AAPL", itemCap.getValue().getSymbol());
        assertEquals(10, itemCap.getValue().getQuantity());
        assertEquals(182.50, itemCap.getValue().getAvgBuyPrice().doubleValue(), 0.001);

        ArgumentCaptor<Transaction> txCap = ArgumentCaptor.forClass(Transaction.class);
        verify(txRepo).save(txCap.capture());
        assertEquals(TransactionType.BUY, txCap.getValue().getType());
        assertEquals(expectedCost, txCap.getValue().getTotalAmount().doubleValue(), 0.001);
    }

    @Test
    @DisplayName("Buy success: avg price recalculated correctly on second purchase")
    void buyStock_avgPriceRecalculated() {
        PortfolioItem existing = new PortfolioItem("AAPL", 10, BigDecimal.valueOf(182.50));
        when(stockRepo.findById("AAPL")).thenReturn(Optional.of(aapl));
        when(portfolioRepo.findById("AAPL")).thenReturn(Optional.of(existing));

        aapl.setCurrentPrice(BigDecimal.valueOf(200.00));
        orderService.buyStock("AAPL", 5);

        ArgumentCaptor<PortfolioItem> cap = ArgumentCaptor.forClass(PortfolioItem.class);
        verify(portfolioRepo).save(cap.capture());
        assertEquals(15, cap.getValue().getQuantity());
        double expectedAvg = (10 * 182.50 + 5 * 200.00) / 15.0;
        assertEquals(expectedAvg, cap.getValue().getAvgBuyPrice().doubleValue(), 0.001);
    }

    @Test
    @DisplayName("Buy fails: insufficient cash")
    void buyStock_insufficientCash() {
        when(stockRepo.findById("NVDA")).thenReturn(Optional.of(nvda));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.buyStock("NVDA", 300));

        assertTrue(ex.getMessage().toLowerCase().contains("insufficient cash"));
        verify(portfolioService, never()).setCash(any());
        verify(portfolioRepo, never()).save(any());
        verify(txRepo, never()).save(any());
    }

    @Test
    @DisplayName("Buy fails: invalid quantity (zero)")
    void buyStock_invalidQuantityZero() {
        assertThrows(IllegalArgumentException.class, () -> orderService.buyStock("AAPL", 0));
        verify(stockRepo, never()).findById(anyString());
    }

    @Test
    @DisplayName("Buy fails: invalid quantity (negative)")
    void buyStock_invalidQuantityNegative() {
        assertThrows(IllegalArgumentException.class, () -> orderService.buyStock("AAPL", -5));
        verify(stockRepo, never()).findById(anyString());
    }

    @Test
    @DisplayName("Buy fails: stock symbol not found")
    void buyStock_stockNotFound() {
        when(stockRepo.findById("UNKNOWN")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.buyStock("UNKNOWN", 10));

        assertTrue(ex.getMessage().toLowerCase().contains("not found"));
    }

    // =========================================================================
    // SELL TESTS
    // =========================================================================

    @Test
    @DisplayName("Sell success: cash increases, holding reduced, transaction recorded")
    void sellStock_success() {
        PortfolioItem holding = new PortfolioItem("TSLA", 20, BigDecimal.valueOf(238.00));
        when(stockRepo.findById("TSLA")).thenReturn(Optional.of(tsla));
        when(portfolioRepo.findById("TSLA")).thenReturn(Optional.of(holding));

        double expectedTotal = 238.00 * 10;
        orderService.sellStock("TSLA", 10);

        ArgumentCaptor<BigDecimal> cashCap = ArgumentCaptor.forClass(BigDecimal.class);
        verify(portfolioService).setCash(cashCap.capture());
        assertEquals(100_000.0 + expectedTotal, cashCap.getValue().doubleValue(), 0.001);

        ArgumentCaptor<PortfolioItem> cap = ArgumentCaptor.forClass(PortfolioItem.class);
        verify(portfolioRepo).save(cap.capture());
        assertEquals(10, cap.getValue().getQuantity());

        ArgumentCaptor<Transaction> txCap = ArgumentCaptor.forClass(Transaction.class);
        verify(txRepo).save(txCap.capture());
        assertEquals(TransactionType.SELL, txCap.getValue().getType());
        assertEquals(expectedTotal, txCap.getValue().getTotalAmount().doubleValue(), 0.001);
    }

    @Test
    @DisplayName("Sell success: holding deleted when all shares sold")
    void sellStock_holdingDeletedWhenAllSold() {
        PortfolioItem holding = new PortfolioItem("TSLA", 5, BigDecimal.valueOf(238.00));
        when(stockRepo.findById("TSLA")).thenReturn(Optional.of(tsla));
        when(portfolioRepo.findById("TSLA")).thenReturn(Optional.of(holding));

        orderService.sellStock("TSLA", 5);

        verify(portfolioRepo).deleteById("TSLA");
        verify(portfolioRepo, never()).save(any());
    }

    @Test
    @DisplayName("Sell fails: exceed held quantity")
    void sellStock_exceedQuantity() {
        PortfolioItem holding = new PortfolioItem("AAPL", 5, BigDecimal.valueOf(182.50));
        when(stockRepo.findById("AAPL")).thenReturn(Optional.of(aapl));
        when(portfolioRepo.findById("AAPL")).thenReturn(Optional.of(holding));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.sellStock("AAPL", 10));

        assertTrue(ex.getMessage().toLowerCase().contains("insufficient shares"));
        verify(portfolioRepo, never()).save(any());
        verify(txRepo, never()).save(any());
    }

    @Test
    @DisplayName("Sell fails: stock not held")
    void sellStock_notHeld() {
        when(stockRepo.findById("AAPL")).thenReturn(Optional.of(aapl));
        when(portfolioRepo.findById("AAPL")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.sellStock("AAPL", 5));

        assertTrue(ex.getMessage().toLowerCase().contains("do not hold"));
    }

    @Test
    @DisplayName("Sell fails: invalid quantity (zero)")
    void sellStock_invalidQuantityZero() {
        assertThrows(IllegalArgumentException.class, () -> orderService.sellStock("AAPL", 0));
        verify(stockRepo, never()).findById(anyString());
    }

    @Test
    @DisplayName("Sell fails: stock symbol not found")
    void sellStock_stockNotFound() {
        when(stockRepo.findById("UNKNOWN")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.sellStock("UNKNOWN", 5));

        assertTrue(ex.getMessage().toLowerCase().contains("not found"));
    }
}
