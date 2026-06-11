package com.mockstock.service;

import com.mockstock.dto.response.PortfolioResponse;
import com.mockstock.entity.PortfolioItem;
import com.mockstock.entity.Stock;
import com.mockstock.entity.Transaction;
import com.mockstock.entity.TransactionType;
import com.mockstock.repository.PortfolioItemRepository;
import com.mockstock.repository.StockRepository;
import com.mockstock.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OrderService {

    private final StockRepository stockRepo;
    private final PortfolioItemRepository portfolioRepo;
    private final TransactionRepository txRepo;
    private final PortfolioService portfolioService;

    public OrderService(StockRepository stockRepo,
                        PortfolioItemRepository portfolioRepo,
                        TransactionRepository txRepo,
                        PortfolioService portfolioService) {
        this.stockRepo = stockRepo;
        this.portfolioRepo = portfolioRepo;
        this.txRepo = txRepo;
        this.portfolioService = portfolioService;
    }

    @Transactional
    public PortfolioResponse buyStock(String symbol, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be greater than 0");

        Stock stock = stockRepo.findById(symbol)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found: " + symbol));

        BigDecimal price = stock.getCurrentPrice();
        BigDecimal total = price.multiply(BigDecimal.valueOf(quantity));
        BigDecimal cash = portfolioService.getCash();

        if (cash.compareTo(total) < 0) throw new IllegalArgumentException(
                String.format("Insufficient cash. Required: %.2f, Available: %.2f", total, cash));

        portfolioService.setCash(cash.subtract(total));

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

        return portfolioService.buildPortfolioResponse();
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

        portfolioService.setCash(portfolioService.getCash().add(total));

        int remaining = holding.getQuantity() - quantity;
        if (remaining == 0) {
            portfolioRepo.deleteById(symbol);
        } else {
            holding.setQuantity(remaining);
            portfolioRepo.save(holding);
        }

        txRepo.save(new Transaction(UUID.randomUUID().toString(), LocalDateTime.now(),
                TransactionType.SELL, symbol, stock.getCompanyName(), quantity, price, total));

        return portfolioService.buildPortfolioResponse();
    }
}
