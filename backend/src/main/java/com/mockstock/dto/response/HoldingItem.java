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
public class HoldingItem {
    private String symbol;
    private String companyName;
    private int quantity;
    private BigDecimal avgBuyPrice;
    private BigDecimal currentPrice;
    private BigDecimal marketValue;
    private BigDecimal profitLoss;

    public static HoldingItem from(PortfolioItem item, Stock stock) {
        BigDecimal marketValue = stock.getCurrentPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        BigDecimal costBasis = item.getAvgBuyPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new HoldingItem(
                item.getSymbol(),
                stock.getCompanyName(),
                item.getQuantity(),
                item.getAvgBuyPrice(),
                stock.getCurrentPrice(),
                marketValue,
                marketValue.subtract(costBasis)
        );
    }
}
