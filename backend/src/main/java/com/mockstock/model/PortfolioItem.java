package com.mockstock.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "portfolio_items")
public class PortfolioItem {

    @Id
    @Column(name = "symbol", length = 10)
    private String symbol;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "avg_buy_price", nullable = false)
    private double avgBuyPrice;

    public PortfolioItem() {}

    public PortfolioItem(String symbol, int quantity, double avgBuyPrice) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.avgBuyPrice = avgBuyPrice;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getAvgBuyPrice() { return avgBuyPrice; }
    public void setAvgBuyPrice(double avgBuyPrice) { this.avgBuyPrice = avgBuyPrice; }
}
