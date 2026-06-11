package com.mockstock.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stocks")
@Getter
@Setter
@NoArgsConstructor
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
}
