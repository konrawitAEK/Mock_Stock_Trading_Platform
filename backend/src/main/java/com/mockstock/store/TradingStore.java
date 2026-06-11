package com.mockstock.store;

import com.mockstock.model.PortfolioItem;
import com.mockstock.model.Stock;
import com.mockstock.model.Transaction;

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
