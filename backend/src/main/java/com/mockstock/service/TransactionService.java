package com.mockstock.service;

import com.mockstock.entity.Transaction;
import com.mockstock.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository txRepo;

    public TransactionService(TransactionRepository txRepo) {
        this.txRepo = txRepo;
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactions() {
        return txRepo.findAllByOrderByTimestampDesc();
    }
}
