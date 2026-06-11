package com.mockstock.dto.response;

import com.mockstock.entity.PortfolioItem;
import com.mockstock.entity.Stock;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockDetailResponse {
    private String symbol;
    private String companyName;
    private BigDecimal currentPrice;
    private BigDecimal previousPrice;
    private BigDecimal dailyChange;
    private BigDecimal changePercent;
    private String sector;
    private String description;
    private int heldQuantity;
    private BigDecimal avgBuyPrice;

    public static StockDetailResponse from(Stock stock, PortfolioItem holding) {
        int qty = holding != null ? holding.getQuantity() : 0;
        BigDecimal avgPrice = holding != null ? holding.getAvgBuyPrice() : BigDecimal.ZERO;
        return new StockDetailResponse(
                stock.getSymbol(),
                stock.getCompanyName(),
                stock.getCurrentPrice(),
                stock.getPreviousPrice(),
                stock.getDailyChange(),
                stock.getChangePercent(),
                stock.getSector(),
                stock.getDescription(),
                qty,
                avgPrice
        );
    }
}
