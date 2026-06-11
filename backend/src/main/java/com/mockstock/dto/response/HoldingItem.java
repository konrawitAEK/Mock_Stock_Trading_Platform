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
public class HoldingItem {
    private String symbol;
    private String companyName;
    private int quantity;
    private double avgBuyPrice;
    private double currentPrice;
    private double marketValue;
    private double profitLoss;

    public static HoldingItem from(PortfolioItem item, Stock stock) {
        double marketValue = stock.getCurrentPrice() * item.getQuantity();
        double costBasis = item.getAvgBuyPrice() * item.getQuantity();
        return new HoldingItem(
                item.getSymbol(),
                stock.getCompanyName(),
                item.getQuantity(),
                item.getAvgBuyPrice(),
                stock.getCurrentPrice(),
                marketValue,
                marketValue - costBasis
        );
    }
}
