package com.mockstock.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 4, nullable = false)
    private TransactionType type;

    @Column(name = "symbol", length = 10, nullable = false)
    private String symbol;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "price", nullable = false)
    private double price;

    @Column(name = "total_amount", nullable = false)
    private double totalAmount;

    public Transaction() {}

    public Transaction(String id, LocalDateTime timestamp, TransactionType type,
                       String symbol, String companyName, int quantity,
                       double price, double totalAmount) {
        this.id = id;
        this.timestamp = timestamp;
        this.type = type;
        this.symbol = symbol;
        this.companyName = companyName;
        this.quantity = quantity;
        this.price = price;
        this.totalAmount = totalAmount;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
}
