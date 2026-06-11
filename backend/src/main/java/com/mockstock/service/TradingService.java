package com.mockstock.service;

import com.mockstock.dto.response.HoldingItem;
import com.mockstock.dto.response.PortfolioResponse;
import com.mockstock.entity.PortfolioItem;
import com.mockstock.entity.Stock;
import com.mockstock.entity.Transaction;
import com.mockstock.entity.TransactionType;
import com.mockstock.store.TradingStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
public class TradingService {

    private final TradingStore store;
    private final Random random = new Random();

    public TradingService(TradingStore store) {
        this.store = store;
    }

    @Transactional
    public PortfolioResponse buyStock(String symbol, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Stock stock = store.getStock(symbol);
        if (stock == null) {
            throw new IllegalArgumentException("Stock not found: " + symbol);
        }

        double price = stock.getCurrentPrice();
        double total = price * quantity;

        if (store.getCash() < total) {
            throw new IllegalArgumentException(
                    String.format("Insufficient cash. Required: %.2f, Available: %.2f", total, store.getCash()));
        }

        store.setCash(store.getCash() - total);

        PortfolioItem existing = store.getPortfolioItem(symbol);
        if (existing != null) {
            double newAvg = ((double) existing.getQuantity() * existing.getAvgBuyPrice()
                    + (double) quantity * price)
                    / (existing.getQuantity() + quantity);
            existing.setAvgBuyPrice(newAvg);
            existing.setQuantity(existing.getQuantity() + quantity);
            store.putPortfolioItem(symbol, existing);
        } else {
            store.putPortfolioItem(symbol, new PortfolioItem(symbol, quantity, price));
        }

        store.addTransaction(new Transaction(
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                TransactionType.BUY,
                symbol,
                stock.getCompanyName(),
                quantity,
                price,
                total));

        return buildPortfolioResponse();
    }

    @Transactional
    public PortfolioResponse sellStock(String symbol, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Stock stock = store.getStock(symbol);
        if (stock == null) {
            throw new IllegalArgumentException("Stock not found: " + symbol);
        }

        PortfolioItem holding = store.getPortfolioItem(symbol);
        if (holding == null) {
            throw new IllegalArgumentException("You do not hold any shares of: " + symbol);
        }

        if (holding.getQuantity() < quantity) {
            throw new IllegalArgumentException(
                    String.format("Insufficient shares. You hold %d but tried to sell %d",
                            holding.getQuantity(), quantity));
        }

        double price = stock.getCurrentPrice();
        double total = price * quantity;

        store.setCash(store.getCash() + total);

        int newQuantity = holding.getQuantity() - quantity;
        if (newQuantity == 0) {
            store.removePortfolioItem(symbol);
        } else {
            holding.setQuantity(newQuantity);
            store.putPortfolioItem(symbol, holding);
        }

        store.addTransaction(new Transaction(
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                TransactionType.SELL,
                symbol,
                stock.getCompanyName(),
                quantity,
                price,
                total));

        return buildPortfolioResponse();
    }

    @Transactional
    public List<Stock> simulateMarket() {
        List<Stock> stocks = new ArrayList<>(store.getStocks().values());
        for (Stock stock : stocks) {
            double changePercent = (random.nextDouble() * 10.0) - 5.0;
            double previousPrice = stock.getCurrentPrice();
            double newPrice = Math.max(previousPrice * (1.0 + changePercent / 100.0), 0.01);

            stock.setPreviousPrice(previousPrice);
            stock.setCurrentPrice(newPrice);
            stock.setDailyChange(newPrice - previousPrice);
            stock.setChangePercent((stock.getDailyChange() / previousPrice) * 100.0);
        }
        return stocks;
    }

    @Transactional(readOnly = true)
    public PortfolioResponse buildPortfolioResponse() {
        List<HoldingItem> holdings = new ArrayList<>();
        double stockMarketValue = 0.0;
        double totalCostBasis = 0.0;

        for (PortfolioItem item : store.getPortfolio().values()) {
            Stock stock = store.getStock(item.getSymbol());
            if (stock == null) continue;

            HoldingItem holdingItem = HoldingItem.from(item, stock);
            stockMarketValue += holdingItem.getMarketValue();
            totalCostBasis += item.getAvgBuyPrice() * item.getQuantity();
            holdings.add(holdingItem);
        }

        double totalPortfolioValue = store.getCash() + stockMarketValue;
        double totalProfitLoss = stockMarketValue - totalCostBasis;

        return new PortfolioResponse(store.getCash(), stockMarketValue, totalPortfolioValue, totalProfitLoss, holdings);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactions() {
        return store.getTransactions().stream()
                .sorted(Comparator.comparing(Transaction::getTimestamp).reversed())
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Stock> getStocks() {
        return store.getStocks();
    }

    @Transactional(readOnly = true)
    public Stock getStock(String symbol) {
        return store.getStock(symbol);
    }

    @Transactional(readOnly = true)
    public PortfolioItem getPortfolioItem(String symbol) {
        return store.getPortfolioItem(symbol);
    }
}
