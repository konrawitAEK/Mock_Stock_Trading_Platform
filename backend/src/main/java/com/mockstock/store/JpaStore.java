package com.mockstock.store;

import com.mockstock.model.PortfolioItem;
import com.mockstock.model.Stock;
import com.mockstock.model.Transaction;
import com.mockstock.model.UserState;
import com.mockstock.repository.PortfolioItemRepository;
import com.mockstock.repository.StockRepository;
import com.mockstock.repository.TransactionRepository;
import com.mockstock.repository.UserStateRepository;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JpaStore implements TradingStore {

    private static final long USER_STATE_ID = 1L;

    private final StockRepository stockRepo;
    private final PortfolioItemRepository portfolioRepo;
    private final TransactionRepository txRepo;
    private final UserStateRepository userStateRepo;

    public JpaStore(StockRepository stockRepo,
                    PortfolioItemRepository portfolioRepo,
                    TransactionRepository txRepo,
                    UserStateRepository userStateRepo) {
        this.stockRepo = stockRepo;
        this.portfolioRepo = portfolioRepo;
        this.txRepo = txRepo;
        this.userStateRepo = userStateRepo;
    }

    @Override
    public double getCash() {
        return userStateRepo.findById(USER_STATE_ID)
                .map(UserState::getCash)
                .orElse(100_000.0);
    }

    @Override
    public void setCash(double cash) {
        UserState state = userStateRepo.findById(USER_STATE_ID)
                .orElseGet(() -> new UserState(USER_STATE_ID, 100_000.0));
        state.setCash(cash);
        userStateRepo.save(state);
    }

    @Override
    public Stock getStock(String symbol) {
        return stockRepo.findById(symbol).orElse(null);
    }

    @Override
    public Map<String, Stock> getStocks() {
        return stockRepo.findAll().stream()
                .collect(Collectors.toMap(
                        Stock::getSymbol, s -> s,
                        (a, b) -> a,
                        LinkedHashMap::new));
    }

    @Override
    public PortfolioItem getPortfolioItem(String symbol) {
        return portfolioRepo.findById(symbol).orElse(null);
    }

    @Override
    public Map<String, PortfolioItem> getPortfolio() {
        return portfolioRepo.findAll().stream()
                .collect(Collectors.toMap(
                        PortfolioItem::getSymbol, p -> p,
                        (a, b) -> a,
                        LinkedHashMap::new));
    }

    @Override
    public void putPortfolioItem(String symbol, PortfolioItem item) {
        portfolioRepo.save(item);
    }

    @Override
    public void removePortfolioItem(String symbol) {
        portfolioRepo.deleteById(symbol);
    }

    @Override
    public void addTransaction(Transaction transaction) {
        txRepo.save(transaction);
    }

    @Override
    public List<Transaction> getTransactions() {
        return txRepo.findAllByOrderByTimestampDesc();
    }
}
