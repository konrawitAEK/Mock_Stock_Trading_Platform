package com.mockstock.service;

import com.mockstock.dto.response.HoldingItem;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TradingService {

    private static final long USER_ID = 1L;

    private final StockRepository stockRepo;
    private final PortfolioItemRepository portfolioRepo;
    private final TransactionRepository txRepo;
    private final UserStateRepository userStateRepo;
    private final Random random = new Random();

    public TradingService(StockRepository stockRepo,
                          PortfolioItemRepository portfolioRepo,
                          TransactionRepository txRepo,
                          UserStateRepository userStateRepo) {
        this.stockRepo = stockRepo;
        this.portfolioRepo = portfolioRepo;
        this.txRepo = txRepo;
        this.userStateRepo = userStateRepo;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private BigDecimal getCash() {
        return userStateRepo.findById(USER_ID).map(UserState::getCash).orElse(new BigDecimal("100000.00"));
    }

    private void setCash(BigDecimal cash) {
        UserState state = userStateRepo.findById(USER_ID)
                .orElseGet(() -> new UserState(USER_ID, new BigDecimal("100000.00")));
        state.setCash(cash);
        userStateRepo.save(state);
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    @Transactional
    public PortfolioResponse buyStock(String symbol, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be greater than 0");

        Stock stock = stockRepo.findById(symbol)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found: " + symbol));

        BigDecimal price = stock.getCurrentPrice();
        BigDecimal total = price.multiply(BigDecimal.valueOf(quantity));
        BigDecimal cash = getCash();

        if (cash.compareTo(total) < 0) throw new IllegalArgumentException(
                String.format("Insufficient cash. Required: %.2f, Available: %.2f", total, cash));

        setCash(cash.subtract(total));

        PortfolioItem existing = portfolioRepo.findById(symbol).orElse(null);
        if (existing != null) {
            BigDecimal newAvg = existing.getAvgBuyPrice().multiply(BigDecimal.valueOf(existing.getQuantity()))
                    .add(price.multiply(BigDecimal.valueOf(quantity)))
                    .divide(BigDecimal.valueOf(existing.getQuantity() + quantity), 4, RoundingMode.HALF_UP);
            existing.setAvgBuyPrice(newAvg);
            existing.setQuantity(existing.getQuantity() + quantity);
        } else {
            existing = new PortfolioItem(symbol, quantity, price);
        }
        portfolioRepo.save(existing);

        txRepo.save(new Transaction(UUID.randomUUID().toString(), LocalDateTime.now(),
                TransactionType.BUY, symbol, stock.getCompanyName(), quantity, price, total));

        return buildPortfolioResponse();
    }

    @Transactional
    public PortfolioResponse sellStock(String symbol, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be greater than 0");

        Stock stock = stockRepo.findById(symbol)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found: " + symbol));

        PortfolioItem holding = portfolioRepo.findById(symbol)
                .orElseThrow(() -> new IllegalArgumentException("You do not hold any shares of: " + symbol));

        if (holding.getQuantity() < quantity) throw new IllegalArgumentException(
                String.format("Insufficient shares. You hold %d but tried to sell %d",
                        holding.getQuantity(), quantity));

        BigDecimal price = stock.getCurrentPrice();
        BigDecimal total = price.multiply(BigDecimal.valueOf(quantity));

        setCash(getCash().add(total));

        int remaining = holding.getQuantity() - quantity;
        if (remaining == 0) {
            portfolioRepo.deleteById(symbol);
        } else {
            holding.setQuantity(remaining);
            portfolioRepo.save(holding);
        }

        txRepo.save(new Transaction(UUID.randomUUID().toString(), LocalDateTime.now(),
                TransactionType.SELL, symbol, stock.getCompanyName(), quantity, price, total));

        return buildPortfolioResponse();
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

    @Transactional(readOnly = true)
    public PortfolioResponse buildPortfolioResponse() {
        List<HoldingItem> holdings = new ArrayList<>();
        BigDecimal stockMarketValue = BigDecimal.ZERO;
        BigDecimal totalCostBasis = BigDecimal.ZERO;

        for (PortfolioItem item : portfolioRepo.findAll()) {
            Stock stock = stockRepo.findById(item.getSymbol()).orElse(null);
            if (stock == null) continue;
            HoldingItem h = HoldingItem.from(item, stock);
            stockMarketValue = stockMarketValue.add(h.getMarketValue());
            totalCostBasis = totalCostBasis.add(item.getAvgBuyPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            holdings.add(h);
        }

        BigDecimal cash = getCash();
        return new PortfolioResponse(cash, stockMarketValue, cash.add(stockMarketValue),
                stockMarketValue.subtract(totalCostBasis), holdings);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactions() {
        return txRepo.findAllByOrderByTimestampDesc();
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

    @Transactional(readOnly = true)
    public PortfolioItem getPortfolioItem(String symbol) {
        return portfolioRepo.findById(symbol).orElse(null);
    }
}
