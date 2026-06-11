package com.mockstock.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "stocks")
public class Stock {

    @Id
    @Column(name = "symbol", length = 10)
    private String symbol;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "current_price", nullable = false)
    private double currentPrice;

    @Column(name = "previous_price", nullable = false)
    private double previousPrice;

    @Column(name = "daily_change", nullable = false)
    private double dailyChange;

    @Column(name = "change_percent", nullable = false)
    private double changePercent;

    @Column(name = "sector", nullable = false, length = 100)
    private String sector;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    public Stock() {}

    public Stock(String symbol, String companyName, double currentPrice,
                 String sector, String description) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.currentPrice = currentPrice;
        this.previousPrice = currentPrice;
        this.dailyChange = 0.0;
        this.changePercent = 0.0;
        this.sector = sector;
        this.description = description;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }

    public double getPreviousPrice() { return previousPrice; }
    public void setPreviousPrice(double previousPrice) { this.previousPrice = previousPrice; }

    public double getDailyChange() { return dailyChange; }
    public void setDailyChange(double dailyChange) { this.dailyChange = dailyChange; }

    public double getChangePercent() { return changePercent; }
    public void setChangePercent(double changePercent) { this.changePercent = changePercent; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
