package com.mockstock.store;

import com.mockstock.entity.PortfolioItem;
import com.mockstock.entity.Stock;
import com.mockstock.entity.Transaction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory implementation of TradingStore.
 * Used directly in unit tests; not registered as a Spring bean.
 */
public class InMemoryStore implements TradingStore {

    private double cash = 100_000.0;

    private final Map<String, Stock> stocks = new LinkedHashMap<>();
    private final Map<String, PortfolioItem> portfolio = new LinkedHashMap<>();
    private final List<Transaction> transactions = new ArrayList<>();

    public InMemoryStore() {
        initStocks();
    }

    private void initStocks() {
        addStock("AAPL", "Apple Inc.", 182.50, "Tech",
                "Apple Inc. designs, manufactures, and markets smartphones, personal computers, " +
                "tablets, wearables, and accessories worldwide. Known for the iPhone, Mac, iPad, and services ecosystem.");
        addStock("TSLA", "Tesla Inc.", 238.00, "Automotive",
                "Tesla, Inc. designs, develops, manufactures, and sells electric vehicles, energy generation, " +
                "and storage systems. A pioneer in sustainable transport and clean energy solutions.");
        addStock("MSFT", "Microsoft Corp.", 375.20, "Tech",
                "Microsoft Corporation develops and supports software, services, devices, and solutions worldwide. " +
                "Products include Windows, Office 365, Azure cloud platform, and Xbox gaming systems.");
        addStock("GOOGL", "Alphabet Inc.", 141.80, "Tech",
                "Alphabet Inc. provides online advertising services, search engine technology, cloud computing, " +
                "software, and hardware products. Parent company of Google, YouTube, and DeepMind.");
        addStock("AMZN", "Amazon.com Inc.", 178.90, "E-Commerce",
                "Amazon.com, Inc. engages in the retail sale of consumer products, subscriptions, and web services. " +
                "Operates AWS cloud platform and leads in global e-commerce and logistics.");
        addStock("NVDA", "NVIDIA Corp.", 495.00, "Semiconductors",
                "NVIDIA Corporation provides graphics and compute and networking solutions. " +
                "Leader in GPU technology powering AI, gaming, data centers, and autonomous vehicles.");
        addStock("META", "Meta Platforms", 326.40, "Social Media",
                "Meta Platforms, Inc. develops products that enable people to connect and share through mobile devices, " +
                "PCs, and other surfaces. Operates Facebook, Instagram, WhatsApp, and the metaverse platform.");
        addStock("NFLX", "Netflix Inc.", 445.60, "Entertainment",
                "Netflix, Inc. provides entertainment services worldwide. Offers TV series, documentaries, feature films, " +
                "and mobile games across various genres and languages via streaming.");
        addStock("AMD", "Advanced Micro Devices", 168.30, "Semiconductors",
                "Advanced Micro Devices, Inc. operates as a semiconductor company worldwide. " +
                "Designs and markets CPUs, GPUs, and data center solutions competing with Intel and NVIDIA.");
        addStock("BABA", "Alibaba Group", 85.70, "E-Commerce",
                "Alibaba Group Holding Limited provides technology infrastructure and marketing reach to merchants, " +
                "brands, retailers, and other businesses globally. Operates Taobao, Tmall, and Alibaba Cloud.");
    }

    private void addStock(String symbol, String companyName, double price, String sector, String description) {
        stocks.put(symbol, new Stock(symbol, companyName, price, sector, description));
    }

    @Override public double getCash() { return cash; }
    @Override public void setCash(double cash) { this.cash = cash; }

    @Override public Stock getStock(String symbol) { return stocks.get(symbol); }
    @Override public Map<String, Stock> getStocks() { return stocks; }

    @Override public PortfolioItem getPortfolioItem(String symbol) { return portfolio.get(symbol); }
    @Override public Map<String, PortfolioItem> getPortfolio() { return portfolio; }
    @Override public void putPortfolioItem(String symbol, PortfolioItem item) { portfolio.put(symbol, item); }
    @Override public void removePortfolioItem(String symbol) { portfolio.remove(symbol); }

    @Override public void addTransaction(Transaction tx) { transactions.add(tx); }
    @Override public List<Transaction> getTransactions() { return transactions; }
}
