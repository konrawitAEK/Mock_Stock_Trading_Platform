package com.mockstock.store;

import com.mockstock.entity.PortfolioItem;
import com.mockstock.entity.Stock;
import com.mockstock.entity.Transaction;

import java.util.List;
import java.util.Map;

public interface TradingStore {
    double getCash();
    void setCash(double cash);

    Stock getStock(String symbol);
    Map<String, Stock> getStocks();

    PortfolioItem getPortfolioItem(String symbol);
    Map<String, PortfolioItem> getPortfolio();
    void putPortfolioItem(String symbol, PortfolioItem item);
    void removePortfolioItem(String symbol);

    void addTransaction(Transaction transaction);
    List<Transaction> getTransactions();
}
