package com.mockstock.dto;

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

    public StockDetailResponse() {}

    public StockDetailResponse(String symbol, String companyName, double currentPrice,
                                double previousPrice, double dailyChange, double changePercent,
                                String sector, String description,
                                int heldQuantity, double avgBuyPrice) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.currentPrice = currentPrice;
        this.previousPrice = previousPrice;
        this.dailyChange = dailyChange;
        this.changePercent = changePercent;
        this.sector = sector;
        this.description = description;
        this.heldQuantity = heldQuantity;
        this.avgBuyPrice = avgBuyPrice;
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

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public double getPreviousPrice() {
        return previousPrice;
    }

    public void setPreviousPrice(double previousPrice) {
        this.previousPrice = previousPrice;
    }

    public double getDailyChange() {
        return dailyChange;
    }

    public void setDailyChange(double dailyChange) {
        this.dailyChange = dailyChange;
    }

    public double getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(double changePercent) {
        this.changePercent = changePercent;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getHeldQuantity() {
        return heldQuantity;
    }

    public void setHeldQuantity(int heldQuantity) {
        this.heldQuantity = heldQuantity;
    }

    public double getAvgBuyPrice() {
        return avgBuyPrice;
    }

    public void setAvgBuyPrice(double avgBuyPrice) {
        this.avgBuyPrice = avgBuyPrice;
    }
}
