package com.mockstock.dto;

public class HoldingItem {

    private String symbol;
    private String companyName;
    private int quantity;
    private double avgBuyPrice;
    private double currentPrice;
    private double marketValue;
    private double profitLoss;

    public HoldingItem() {}

    public HoldingItem(String symbol, String companyName, int quantity,
                       double avgBuyPrice, double currentPrice,
                       double marketValue, double profitLoss) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.quantity = quantity;
        this.avgBuyPrice = avgBuyPrice;
        this.currentPrice = currentPrice;
        this.marketValue = marketValue;
        this.profitLoss = profitLoss;
    }

    // Getters and Setters

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getAvgBuyPrice() {
        return avgBuyPrice;
    }

    public void setAvgBuyPrice(double avgBuyPrice) {
        this.avgBuyPrice = avgBuyPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public double getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(double marketValue) {
        this.marketValue = marketValue;
    }

    public double getProfitLoss() {
        return profitLoss;
    }

    public void setProfitLoss(double profitLoss) {
        this.profitLoss = profitLoss;
    }
}
