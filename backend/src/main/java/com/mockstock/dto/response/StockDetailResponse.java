package com.mockstock.dto.response;

import com.mockstock.entity.PortfolioItem;
import com.mockstock.entity.Stock;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockDetailResponse {
    private String symbol;
    private String companyName;
    private double currentPrice;
    private double previousPrice;
    private double dailyChange;
    private double changePercent;
    private String sector;
    private String description;
    private int heldQuantity;
    private double avgBuyPrice;

    public static StockDetailResponse from(Stock stock, PortfolioItem holding) {
        int qty = holding != null ? holding.getQuantity() : 0;
        double avgPrice = holding != null ? holding.getAvgBuyPrice() : 0.0;
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
