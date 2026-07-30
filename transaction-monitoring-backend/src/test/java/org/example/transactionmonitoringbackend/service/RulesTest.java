package org.example.transactionmonitoringbackend.service;

import org.example.transactionmonitoringbackend.entity.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class RulesTest {
    @Autowired
    private FixedRules fixedRules;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /*
     * Create a reusable baseline transaction object for rule unit tests.
     */
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

    /*
     * Rule 1 test:
     * amount above threshold should trigger code 1.
     */
    @Test
    void checkSingleTransaction_trigger_test() {
        Transaction tx = baseTx();
        tx.setAmount(new BigDecimal("10000.01"));

        int result = fixedRules.checkSingleTransaction(tx);

        assertEquals(1, result);
    }

    /*
     * Rule 2 test:
     * insert enough transactions in the same window to trigger velocity rule.
     */
    @Test
    void checkWindow_trigger_test() {
        Instant txInstant = Instant.parse("2026-07-28T04:00:00Z");
        for (int i = 0; i < 5; i++) {
            Instant seedInstant = txInstant.minusSeconds(60 + i);
            jdbcTemplate.update(
                    "INSERT INTO transactions(account_id, payee_id, amount, currency, trans_type, trans_timestamp, description) VALUES (?,?,?,?,?,?,?)",
                    "ACC_RULES_002",
                    "PAYEE_RULES_002",
                    new BigDecimal("10.00"),
                    "USD",
                    "DEBIT",
                    Timestamp.from(seedInstant),
                    "window-" + i
            );
        }

        Transaction tx = baseTx();
        tx.setAccountId("ACC_RULES_002");
        tx.setPayeeId("PAYEE_RULES_002");
        tx.setTransTimestamp(LocalDateTime.ofInstant(txInstant, ZoneOffset.UTC));

        int result = fixedRules.checkWindow(tx);
        assertEquals(2, result);
    }

    /*
     * Rule 3 test:
     * blank or unknown payee id should trigger unknown payee rule.
     */
    @Test
    void checkFirstTransactionToPayee_trigger_test() {
        Transaction tx = baseTx();
        tx.setPayeeId("   ");

        int result = fixedRules.checkFirstTransactionToPayee(tx);

        assertEquals(3, result);
    }

    /*
     * Rule 4 test:
     * verify boundary case (equal to limit) and exceed case (> limit).
     */
    @Test
    void dailyLimit_boundary_then_exceed_test() {
        LocalDateTime dbTime = LocalDateTime.of(2026, 7, 28, 15, 0, 0);
        jdbcTemplate.update(
                "INSERT INTO transactions(account_id, payee_id, amount, currency, trans_type, trans_timestamp, description) VALUES (?,?,?,?,?,?,?)",
                "ACC_RULES_004",
                "PAYEE_RULES_004",
                new BigDecimal("49950.00"),
                "USD",
                "DEBIT",
                dbTime.minusHours(1),
                "seed-daily-limit"
        );

        Transaction tx = baseTx();
        tx.setAccountId("ACC_RULES_004");
        tx.setPayeeId("PAYEE_RULES_004");
        tx.setAmount(new BigDecimal("50.00"));
        tx.setTransTimestamp(dbTime.minusHours(8));
        assertEquals(0, fixedRules.dailyLimit(tx));
        tx.setAmount(new BigDecimal("51.00"));
        assertEquals(4, fixedRules.dailyLimit(tx));
    }

}
