package com.mockstock.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioResponse {
    private BigDecimal cash;
    private BigDecimal stockMarketValue;
    private BigDecimal totalPortfolioValue;
    private BigDecimal totalProfitLoss;
    private List<HoldingItem> holdings;
}
