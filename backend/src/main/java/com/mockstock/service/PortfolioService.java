package com.mockstock.service;

import com.mockstock.dto.response.HoldingItem;
import com.mockstock.dto.response.PortfolioResponse;
import com.mockstock.entity.PortfolioItem;
import com.mockstock.entity.Stock;
import com.mockstock.entity.UserState;
import com.mockstock.repository.PortfolioItemRepository;
import com.mockstock.repository.StockRepository;
import com.mockstock.repository.UserStateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PortfolioService {

    private static final long USER_ID = 1L;

    private final PortfolioItemRepository portfolioRepo;
    private final StockRepository stockRepo;
    private final UserStateRepository userStateRepo;

    public PortfolioService(PortfolioItemRepository portfolioRepo,
                            StockRepository stockRepo,
                            UserStateRepository userStateRepo) {
        this.portfolioRepo = portfolioRepo;
        this.stockRepo = stockRepo;
        this.userStateRepo = userStateRepo;
    }

    public BigDecimal getCash() {
        return userStateRepo.findById(USER_ID).map(UserState::getCash).orElse(new BigDecimal("100000.00"));
    }

    public void setCash(BigDecimal cash) {
        UserState state = userStateRepo.findById(USER_ID)
                .orElseGet(() -> new UserState(USER_ID, new BigDecimal("100000.00")));
        state.setCash(cash);
        userStateRepo.save(state);
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
    public PortfolioItem getPortfolioItem(String symbol) {
        return portfolioRepo.findById(symbol).orElse(null);
    }
}
