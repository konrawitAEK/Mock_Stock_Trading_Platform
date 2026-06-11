package com.mockstock.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

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
    private BigDecimal currentPrice;

    @Column(name = "previous_price", nullable = false)
    private BigDecimal previousPrice;

    @Column(name = "daily_change", nullable = false)
    private BigDecimal dailyChange;

    @Column(name = "change_percent", nullable = false)
    private BigDecimal changePercent;

    @Column(name = "sector", nullable = false, length = 100)
    private String sector;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    public Stock(String symbol, String companyName, BigDecimal currentPrice,
                 String sector, String description) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.currentPrice = currentPrice;
        this.previousPrice = currentPrice;
        this.dailyChange = BigDecimal.ZERO;
        this.changePercent = BigDecimal.ZERO;
        this.sector = sector;
        this.description = description;
    }
}
