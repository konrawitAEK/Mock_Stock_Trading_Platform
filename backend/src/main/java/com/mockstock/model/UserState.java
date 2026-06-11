package com.mockstock.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_state")
public class UserState {

    @Id
    @Column(name = "id")
    private long id;

    @Column(name = "cash", nullable = false)
    private double cash;

    public UserState() {}

    public UserState(long id, double cash) {
        this.id = id;
        this.cash = cash;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public double getCash() { return cash; }
    public void setCash(double cash) { this.cash = cash; }
}
