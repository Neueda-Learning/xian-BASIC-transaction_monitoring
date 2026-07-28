package org.example.transactionmonitoringbackend.service;

import org.example.transactionmonitoringbackend.entity.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class RulesTest {
    @Autowired
    private FixedRules fixedRules;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Transaction baseTx() {
        Transaction tx = new Transaction();
        tx.setAccountId("ACC_RULES_001");
        tx.setPayeeId("PAYEE_RULES_001");
        tx.setAmount(new BigDecimal("100.00"));
        tx.setCurrency("USD");
        tx.setTransType("DEBIT");
        tx.setTransTimestamp(LocalDateTime.of(2026, 7, 28, 12, 0, 0));
        tx.setDescription("rules-test");
        return tx;
    }

    @Test
    void checkSingleTransaction_trigger_test() {
        Transaction tx = baseTx();
        tx.setAmount(new BigDecimal("10000.01")); // > 10000

        int result = fixedRules.checkSingleTransaction(tx);

        assertEquals(1, result);
    }

}
