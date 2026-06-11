package com.mockstock.repository;

import com.mockstock.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findAllByOrderByTimestampDesc();
}
