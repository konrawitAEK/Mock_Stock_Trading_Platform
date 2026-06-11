package com.mockstock.service;

import com.mockstock.dto.response.PortfolioResponse;
import com.mockstock.entity.PortfolioItem;
import com.mockstock.entity.Stock;
import com.mockstock.entity.Transaction;
import com.mockstock.entity.TransactionType;
import com.mockstock.store.InMemoryStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TradingServiceTest {

    private InMemoryStore store;
    private TradingService tradingService;

    @BeforeEach
    void setUp() {
        store = new InMemoryStore();
        tradingService = new TradingService(store);
    }

    // =========================================================================
    // BUY TESTS
    // =========================================================================

    @Test
    @DisplayName("Buy success: cash decreases, holding created, transaction recorded")
    void buyStock_success() {
        double initialCash = store.getCash(); // 100,000
        String symbol = "AAPL";
        int quantity = 10;
        double price = store.getStock(symbol).getCurrentPrice(); // 182.50
        double expectedTotal = price * quantity;

        PortfolioResponse response = tradingService.buyStock(symbol, quantity);

        // Cash should have decreased
        assertEquals(initialCash - expectedTotal, store.getCash(), 0.001);

        // Holding should exist
        PortfolioItem holding = store.getPortfolioItem(symbol);
        assertNotNull(holding);
        assertEquals(quantity, holding.getQuantity());
        assertEquals(price, holding.getAvgBuyPrice(), 0.001);

        // Transaction should be recorded
        List<Transaction> transactions = store.getTransactions();
        assertEquals(1, transactions.size());
        Transaction tx = transactions.get(0);
        assertEquals(TransactionType.BUY, tx.getType());
        assertEquals(symbol, tx.getSymbol());
        assertEquals(quantity, tx.getQuantity());
        assertEquals(price, tx.getPrice(), 0.001);
        assertEquals(expectedTotal, tx.getTotalAmount(), 0.001);

        // Portfolio response should be correct
        assertNotNull(response);
        assertEquals(initialCash - expectedTotal, response.getCash(), 0.001);
    }

    @Test
    @DisplayName("Buy success: average buy price recalculated correctly on second purchase")
    void buyStock_avgPriceRecalculated() {
        String symbol = "AAPL";
        Stock stock = store.getStock(symbol);

        // First buy: 10 shares at 182.50
        tradingService.buyStock(symbol, 10);
        double firstPrice = stock.getCurrentPrice();

        // Manually change price to simulate market movement before second buy
        stock.setCurrentPrice(200.00);
        double secondPrice = stock.getCurrentPrice();

        // Second buy: 5 shares at 200.00
        tradingService.buyStock(symbol, 5);

        PortfolioItem holding = store.getPortfolioItem(symbol);
        assertNotNull(holding);
        assertEquals(15, holding.getQuantity());

        double expectedAvg = (10 * firstPrice + 5 * secondPrice) / 15.0;
        assertEquals(expectedAvg, holding.getAvgBuyPrice(), 0.001);
    }

    @Test
    @DisplayName("Buy fails: insufficient cash")
    void buyStock_insufficientCash() {
        // NVDA is 495.00; buying 300 would cost 148,500 > 100,000 cash
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> tradingService.buyStock("NVDA", 300));

        assertTrue(ex.getMessage().toLowerCase().contains("insufficient cash"));
        // Cash should be unchanged
        assertEquals(100_000.0, store.getCash(), 0.001);
        // No holding should be created
        assertNull(store.getPortfolioItem("NVDA"));
        // No transaction should be recorded
        assertTrue(store.getTransactions().isEmpty());
    }

    @Test
    @DisplayName("Buy fails: invalid quantity (zero)")
    void buyStock_invalidQuantityZero() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> tradingService.buyStock("AAPL", 0));

        assertTrue(ex.getMessage().toLowerCase().contains("quantity"));
        assertEquals(100_000.0, store.getCash(), 0.001);
        assertNull(store.getPortfolioItem("AAPL"));
    }

    @Test
    @DisplayName("Buy fails: invalid quantity (negative)")
    void buyStock_invalidQuantityNegative() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> tradingService.buyStock("AAPL", -5));

        assertTrue(ex.getMessage().toLowerCase().contains("quantity"));
        assertEquals(100_000.0, store.getCash(), 0.001);
    }

    @Test
    @DisplayName("Buy fails: stock symbol not found")
    void buyStock_stockNotFound() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> tradingService.buyStock("UNKNOWN", 10));

        assertTrue(ex.getMessage().toLowerCase().contains("not found"));
        assertEquals(100_000.0, store.getCash(), 0.001);
    }

    // =========================================================================
    // SELL TESTS
    // =========================================================================

    @Test
    @DisplayName("Sell success: cash increases, holding reduced, transaction recorded")
    void sellStock_success() {
        String symbol = "TSLA";
        int buyQty = 20;
        int sellQty = 10;

        tradingService.buyStock(symbol, buyQty);
        double cashAfterBuy = store.getCash();
        double sellPrice = store.getStock(symbol).getCurrentPrice();
        double expectedSellTotal = sellPrice * sellQty;

        PortfolioResponse response = tradingService.sellStock(symbol, sellQty);

        // Cash should have increased
        assertEquals(cashAfterBuy + expectedSellTotal, store.getCash(), 0.001);

        // Holding should be reduced
        PortfolioItem holding = store.getPortfolioItem(symbol);
        assertNotNull(holding);
        assertEquals(buyQty - sellQty, holding.getQuantity());

        // avgBuyPrice should NOT change on sell
        assertEquals(sellPrice, holding.getAvgBuyPrice(), 0.001);

        // 2 transactions recorded (1 buy + 1 sell)
        List<Transaction> transactions = store.getTransactions();
        assertEquals(2, transactions.size());
        Transaction sellTx = transactions.get(1);
        assertEquals(TransactionType.SELL, sellTx.getType());
        assertEquals(symbol, sellTx.getSymbol());
        assertEquals(sellQty, sellTx.getQuantity());
        assertEquals(sellPrice, sellTx.getPrice(), 0.001);
        assertEquals(expectedSellTotal, sellTx.getTotalAmount(), 0.001);

        // Portfolio response should be valid
        assertNotNull(response);
    }

    @Test
    @DisplayName("Sell success: holding removed entirely when all shares sold")
    void sellStock_holdingRemovedWhenAllSold() {
        String symbol = "MSFT";
        tradingService.buyStock(symbol, 5);
        tradingService.sellStock(symbol, 5);

        assertNull(store.getPortfolioItem(symbol));
    }

    @Test
    @DisplayName("Sell fails: exceed held quantity")
    void sellStock_exceedQuantity() {
        String symbol = "GOOGL";
        tradingService.buyStock(symbol, 5);
        double cashAfterBuy = store.getCash();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> tradingService.sellStock(symbol, 10));

        assertTrue(ex.getMessage().toLowerCase().contains("insufficient shares"));
        // Cash unchanged since sell failed
        assertEquals(cashAfterBuy, store.getCash(), 0.001);
        // Holding quantity still 5
        assertEquals(5, store.getPortfolioItem(symbol).getQuantity());
    }

    @Test
    @DisplayName("Sell fails: stock not held in portfolio")
    void sellStock_stockNotHeld() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> tradingService.sellStock("AMZN", 5));

        assertTrue(ex.getMessage().toLowerCase().contains("do not hold"));
        assertEquals(100_000.0, store.getCash(), 0.001);
    }

    @Test
    @DisplayName("Sell fails: invalid quantity (zero)")
    void sellStock_invalidQuantityZero() {
        tradingService.buyStock("NVDA", 2);
        double cashAfterBuy = store.getCash();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> tradingService.sellStock("NVDA", 0));

        assertTrue(ex.getMessage().toLowerCase().contains("quantity"));
        assertEquals(cashAfterBuy, store.getCash(), 0.001);
    }

    @Test
    @DisplayName("Sell fails: stock symbol not found")
    void sellStock_stockNotFound() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> tradingService.sellStock("NOTREAL", 5));

        assertTrue(ex.getMessage().toLowerCase().contains("not found"));
    }

    // =========================================================================
    // SIMULATE MARKET TESTS
    // =========================================================================

    @Test
    @DisplayName("Simulate market: all stocks returned")
    void simulateMarket_returnsAllStocks() {
        List<Stock> updatedStocks = tradingService.simulateMarket();
        assertEquals(10, updatedStocks.size());
    }

    @Test
    @DisplayName("Simulate market: prices always remain positive (never zero or negative)")
    void simulateMarket_pricesAlwaysPositive() {
        // Run simulation many times to check the floor is enforced
        for (int i = 0; i < 100; i++) {
            List<Stock> updatedStocks = tradingService.simulateMarket();
            for (Stock stock : updatedStocks) {
                assertTrue(stock.getCurrentPrice() > 0,
                        "Price for " + stock.getSymbol() + " should always be > 0");
            }
        }
    }

    @Test
    @DisplayName("Simulate market: previousPrice updated to old currentPrice")
    void simulateMarket_previousPriceUpdated() {
        String symbol = "AAPL";
        double originalPrice = store.getStock(symbol).getCurrentPrice();

        tradingService.simulateMarket();

        Stock stock = store.getStock(symbol);
        assertEquals(originalPrice, stock.getPreviousPrice(), 0.001,
                "previousPrice should equal the pre-simulation currentPrice");
    }

    @Test
    @DisplayName("Simulate market: dailyChange and changePercent are consistent")
    void simulateMarket_dailyChangeConsistency() {
        tradingService.simulateMarket();

        for (Stock stock : store.getStocks().values()) {
            double expectedDailyChange = stock.getCurrentPrice() - stock.getPreviousPrice();
            double expectedChangePercent = (expectedDailyChange / stock.getPreviousPrice()) * 100.0;

            assertEquals(expectedDailyChange, stock.getDailyChange(), 0.0001,
                    "dailyChange should equal currentPrice - previousPrice for " + stock.getSymbol());
            assertEquals(expectedChangePercent, stock.getChangePercent(), 0.0001,
                    "changePercent should be computed correctly for " + stock.getSymbol());
        }
    }

    // =========================================================================
    // TRANSACTIONS ORDER TEST
    // =========================================================================

    @Test
    @DisplayName("Transactions returned newest-first")
    void getTransactions_newestFirst() {
        tradingService.buyStock("AAPL", 5);
        tradingService.buyStock("TSLA", 3);
        tradingService.sellStock("AAPL", 2);

        List<Transaction> transactions = tradingService.getTransactions();
        assertEquals(3, transactions.size());

        // Newest (sell AAPL) should be first
        assertEquals(TransactionType.SELL, transactions.get(0).getType());
        assertEquals("AAPL", transactions.get(0).getSymbol());

        // Then buy TSLA
        assertEquals(TransactionType.BUY, transactions.get(1).getType());
        assertEquals("TSLA", transactions.get(1).getSymbol());

        // Then first buy AAPL
        assertEquals(TransactionType.BUY, transactions.get(2).getType());
        assertEquals("AAPL", transactions.get(2).getSymbol());
    }

    // =========================================================================
    // PORTFOLIO RESPONSE TESTS
    // =========================================================================

    @Test
    @DisplayName("Portfolio response: empty portfolio returns all cash and zero market value")
    void buildPortfolioResponse_emptyPortfolio() {
        PortfolioResponse response = tradingService.buildPortfolioResponse();

        assertEquals(100_000.0, response.getCash(), 0.001);
        assertEquals(0.0, response.getStockMarketValue(), 0.001);
        assertEquals(100_000.0, response.getTotalPortfolioValue(), 0.001);
        assertEquals(0.0, response.getTotalProfitLoss(), 0.001);
        assertTrue(response.getHoldings().isEmpty());
    }

    @Test
    @DisplayName("Portfolio response: profit/loss calculated correctly")
    void buildPortfolioResponse_profitLossCalculation() {
        String symbol = "AAPL";
        int quantity = 10;
        tradingService.buyStock(symbol, quantity);

        double buyPrice = store.getStock(symbol).getCurrentPrice();

        // Manually set a higher current price to simulate gain
        store.getStock(symbol).setCurrentPrice(buyPrice + 10.0);

        PortfolioResponse response = tradingService.buildPortfolioResponse();

        double expectedMarketValue = (buyPrice + 10.0) * quantity;
        double expectedPnL = (buyPrice + 10.0 - buyPrice) * quantity; // 10 * quantity

        assertEquals(expectedMarketValue, response.getStockMarketValue(), 0.001);
        assertEquals(expectedPnL, response.getTotalProfitLoss(), 0.001);
    }
}
