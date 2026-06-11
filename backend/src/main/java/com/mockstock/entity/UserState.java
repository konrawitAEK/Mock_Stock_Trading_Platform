package com.mockstock.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "user_state")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserState {

    @Id
    @Column(name = "id")
    private long id;

    @Column(name = "cash", nullable = false)
    private BigDecimal cash;
}
