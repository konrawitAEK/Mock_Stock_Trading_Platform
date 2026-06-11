package com.mockstock.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioResponse {
    private double cash;
    private double stockMarketValue;
    private double totalPortfolioValue;
    private double totalProfitLoss;
    private List<HoldingItem> holdings;
}
