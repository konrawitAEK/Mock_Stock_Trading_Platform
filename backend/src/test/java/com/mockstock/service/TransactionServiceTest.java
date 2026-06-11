package com.mockstock.service;

import com.mockstock.entity.Transaction;
import com.mockstock.entity.TransactionType;
import com.mockstock.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock TransactionRepository txRepo;

    @InjectMocks TransactionService transactionService;

    @Test
    @DisplayName("1. Transactions: returned newest-first from repository")
    void getTransactions_newestFirst() {
        Transaction buy  = new Transaction("1", LocalDateTime.now().minusMinutes(2),
                TransactionType.BUY,  "AAPL", "Apple", 5, BigDecimal.valueOf(182.50), BigDecimal.valueOf(912.50));
        Transaction sell = new Transaction("2", LocalDateTime.now(),
                TransactionType.SELL, "AAPL", "Apple", 5, BigDecimal.valueOf(190.00), BigDecimal.valueOf(950.00));

        when(txRepo.findAllByOrderByTimestampDesc()).thenReturn(List.of(sell, buy));

        List<Transaction> result = transactionService.getTransactions();

        assertEquals(2, result.size());
        assertEquals(TransactionType.SELL, result.get(0).getType());
        assertEquals(TransactionType.BUY,  result.get(1).getType());
    }
}
