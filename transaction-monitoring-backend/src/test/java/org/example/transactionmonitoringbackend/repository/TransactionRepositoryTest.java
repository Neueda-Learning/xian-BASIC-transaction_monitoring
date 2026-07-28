package org.example.transactionmonitoringbackend.repository;

import org.aspectj.lang.annotation.Before;
import org.example.transactionmonitoringbackend.entity.Transaction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionRepositoryTest {
    @Autowired
    private TransactionRepository repository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    Transaction tx;

    @Test
    void addTransaction_test() {
        tx = new Transaction();
        tx.setAccountId("103");
        tx.setPayeeId("104");
        tx.setAmount(BigDecimal.valueOf(50));
        tx.setTransType("DEBIT");
        tx.setTransTimestamp(LocalDateTime.parse("2023-06-01 10:00:00"));
        tx.setDescription("testbycode");
        int actual = repository.addTransaction(tx);
        assertEquals(1, actual);
    }

}
