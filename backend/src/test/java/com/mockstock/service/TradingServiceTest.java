package com.mockstock.service;

import com.mockstock.dto.response.PortfolioResponse;
import com.mockstock.entity.PortfolioItem;
import com.mockstock.entity.Stock;
import com.mockstock.entity.Transaction;
import com.mockstock.entity.TransactionType;
import com.mockstock.entity.UserState;
import com.mockstock.repository.PortfolioItemRepository;
import com.mockstock.repository.StockRepository;
import com.mockstock.repository.TransactionRepository;
import com.mockstock.repository.UserStateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradingServiceTest {

    @Mock StockRepository stockRepo;
    @Mock PortfolioItemRepository portfolioRepo;
    @Mock TransactionRepository txRepo;
    @Mock UserStateRepository userStateRepo;

    @InjectMocks TradingService tradingService;

    private final Stock aapl  = new Stock("AAPL",  "Apple Inc.",   BigDecimal.valueOf(182.50), "Tech",       "Apple");
    private final Stock tsla  = new Stock("TSLA",  "Tesla Inc.",   BigDecimal.valueOf(238.00), "Automotive", "Tesla");
    private final Stock nvda  = new Stock("NVDA",  "NVIDIA Corp.", BigDecimal.valueOf(495.00), "Semi",       "NVIDIA");
    private UserState userState;

    @BeforeEach
    void setUp() {
        userState = new UserState(1L, new BigDecimal("100000.00"));
        lenient().when(userStateRepo.findById(1L)).thenReturn(Optional.of(userState));
        lenient().when(userStateRepo.save(any())).thenAnswer(inv -> {
            UserState s = inv.getArgument(0);
            userState.setCash(s.getCash());
            return s;
        });
        lenient().when(portfolioRepo.findAll()).thenReturn(List.of());
        lenient().when(portfolioRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(txRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(txRepo.findAllByOrderByTimestampDesc()).thenReturn(List.of());
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
        PortfolioResponse response = tradingService.buyStock("AAPL", 10);

        assertEquals(100_000.0 - expectedCost, userState.getCash().doubleValue(), 0.001);
        assertEquals(100_000.0 - expectedCost, response.getCash().doubleValue(), 0.001);

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
        when(portfolioRepo.findById("AAPL"))
                .thenReturn(Optional.of(existing));

        aapl.setCurrentPrice(BigDecimal.valueOf(200.00));
        tradingService.buyStock("AAPL", 5);

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
                () -> tradingService.buyStock("NVDA", 300));

        assertTrue(ex.getMessage().toLowerCase().contains("insufficient cash"));
        assertEquals(100_000.0, userState.getCash().doubleValue(), 0.001);
        verify(portfolioRepo, never()).save(any());
        verify(txRepo, never()).save(any());
    }

    @Test
    @DisplayName("Buy fails: invalid quantity (zero)")
    void buyStock_invalidQuantityZero() {
        assertThrows(IllegalArgumentException.class, () -> tradingService.buyStock("AAPL", 0));
        verify(stockRepo, never()).findById(anyString());
    }

    @Test
    @DisplayName("Buy fails: invalid quantity (negative)")
    void buyStock_invalidQuantityNegative() {
        assertThrows(IllegalArgumentException.class, () -> tradingService.buyStock("AAPL", -5));
        verify(stockRepo, never()).findById(anyString());
    }

    @Test
    @DisplayName("Buy fails: stock symbol not found")
    void buyStock_stockNotFound() {
        when(stockRepo.findById("UNKNOWN")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> tradingService.buyStock("UNKNOWN", 10));

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
        tradingService.sellStock("TSLA", 10);

        assertEquals(100_000.0 + expectedTotal, userState.getCash().doubleValue(), 0.001);

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

        tradingService.sellStock("TSLA", 5);

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
                () -> tradingService.sellStock("AAPL", 10));

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
                () -> tradingService.sellStock("AAPL", 5));

        assertTrue(ex.getMessage().toLowerCase().contains("do not hold"));
    }

    @Test
    @DisplayName("Sell fails: invalid quantity (zero)")
    void sellStock_invalidQuantityZero() {
        assertThrows(IllegalArgumentException.class, () -> tradingService.sellStock("AAPL", 0));
        verify(stockRepo, never()).findById(anyString());
    }

    @Test
    @DisplayName("Sell fails: stock symbol not found")
    void sellStock_stockNotFound() {
        when(stockRepo.findById("UNKNOWN")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> tradingService.sellStock("UNKNOWN", 5));

        assertTrue(ex.getMessage().toLowerCase().contains("not found"));
    }

    // =========================================================================
    // SIMULATE MARKET TESTS
    // =========================================================================

    @Test
    @DisplayName("Simulate market: prices change within ±5%")
    void simulateMarket_pricesWithinBounds() {
        Stock msft = new Stock("MSFT", "Microsoft", BigDecimal.valueOf(375.20), "Tech", "MS");
        when(stockRepo.findAll()).thenReturn(List.of(aapl, tsla, msft));
        when(stockRepo.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        List<Stock> result = tradingService.simulateMarket();

        assertEquals(3, result.size());
        for (Stock s : result) {
            assertTrue(s.getChangePercent().abs().doubleValue() <= 5.01);
            assertTrue(s.getCurrentPrice().doubleValue() >= 0.01);
        }
    }

    @Test
    @DisplayName("Simulate market: previousPrice and dailyChange updated correctly")
    void simulateMarket_fieldsUpdated() {
        BigDecimal originalPrice = aapl.getCurrentPrice();
        when(stockRepo.findAll()).thenReturn(List.of(aapl));
        when(stockRepo.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        tradingService.simulateMarket();

        assertEquals(0, originalPrice.compareTo(aapl.getPreviousPrice()));
        assertEquals(0, aapl.getCurrentPrice().subtract(originalPrice).compareTo(aapl.getDailyChange()));
    }

    // =========================================================================
    // TRANSACTION TESTS
    // =========================================================================

    @Test
    @DisplayName("Transactions: returned newest-first from repository")
    void getTransactions_newestFirst() {
        Transaction buy  = new Transaction("1", LocalDateTime.now().minusMinutes(2),
                TransactionType.BUY,  "AAPL", "Apple", 5, BigDecimal.valueOf(182.50), BigDecimal.valueOf(912.50));
        Transaction sell = new Transaction("2", LocalDateTime.now(),
                TransactionType.SELL, "AAPL", "Apple", 5, BigDecimal.valueOf(190.00), BigDecimal.valueOf(950.00));

        when(txRepo.findAllByOrderByTimestampDesc()).thenReturn(List.of(sell, buy));

        List<Transaction> result = tradingService.getTransactions();

        assertEquals(2, result.size());
        assertEquals(TransactionType.SELL, result.get(0).getType());
        assertEquals(TransactionType.BUY,  result.get(1).getType());
    }
}
