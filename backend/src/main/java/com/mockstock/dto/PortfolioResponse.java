package com.mockstock.dto;

import java.util.List;

public class PortfolioResponse {

    private double cash;
    private double stockMarketValue;
    private double totalPortfolioValue;
    private double totalProfitLoss;
    private List<HoldingItem> holdings;

    public PortfolioResponse() {}

    public PortfolioResponse(double cash, double stockMarketValue,
                              double totalPortfolioValue, double totalProfitLoss,
                              List<HoldingItem> holdings) {
        this.cash = cash;
        this.stockMarketValue = stockMarketValue;
        this.totalPortfolioValue = totalPortfolioValue;
        this.totalProfitLoss = totalProfitLoss;
        this.holdings = holdings;
    }

    // Getters and Setters

    public double getCash() {
        return cash;
    }

    public void setCash(double cash) {
        this.cash = cash;
    }

    public double getStockMarketValue() {
        return stockMarketValue;
    }

    public void setStockMarketValue(double stockMarketValue) {
        this.stockMarketValue = stockMarketValue;
    }

    public double getTotalPortfolioValue() {
        return totalPortfolioValue;
    }

    public void setTotalPortfolioValue(double totalPortfolioValue) {
        this.totalPortfolioValue = totalPortfolioValue;
    }

    public double getTotalProfitLoss() {
        return totalProfitLoss;
    }

    public void setTotalProfitLoss(double totalProfitLoss) {
        this.totalProfitLoss = totalProfitLoss;
    }

    public List<HoldingItem> getHoldings() {
        return holdings;
    }

    public void setHoldings(List<HoldingItem> holdings) {
        this.holdings = holdings;
    }
}
