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

    /**
     * Builds a base Transaction pre-populated with default test values.
     * Each test overrides only the fields relevant to the rule under test.
     */
    private Transaction baseTx() {
        // initial a base transaction
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

    /**
     * Tests Rule 1: amount exceeding 10,000 should trigger the rule and return 1.
     * The transaction amount is set to 10,000.01 (just above the threshold).
     */
    @Test
    void checkSingleTransaction_trigger_test() {
        // test rule 1, set amount out of scope
        Transaction tx = baseTx();
        tx.setAmount(new BigDecimal("10000.01")); // > 10000

        int result = fixedRules.checkSingleTransaction(tx);

        assertEquals(1, result);
    }

    /**
     * Tests Rule 2: five or more transactions within a rolling window should return 2.
     * Five historical rows are seeded just before the test transaction's timestamp.
     */
    @Test
    void checkWindow_trigger_test() {
        Instant txInstant = Instant.parse("2026-07-28T04:00:00Z");
        // test rule 2
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

    /**
     * Tests Rule 3: a blank or whitespace-only payee id should trigger the rule and return 3.
     */
    @Test
    void checkFirstTransactionToPayee_trigger_test() {
        // test rule 3, set PayeeId with blank
        Transaction tx = baseTx();
        tx.setPayeeId("   ");

        int result = fixedRules.checkFirstTransactionToPayee(tx);

        assertEquals(3, result);
    }

    /**
     * Tests Rule 4: daily total must not exceed 50,000. Verifies both boundary and exceeded cases.
     * A seed row of 49,950 is inserted; 50.00 passes (total = 50,000, returns 0),
     * then 50.01 triggers the rule and returns 4.
     */
    @Test
    void dailyLimit_boundary_then_exceed_test() {
        // test rule 4
        Instant txInstant = Instant.parse("2026-07-28T07:00:00Z");
        Instant seedInstant = txInstant.minusSeconds(3600);

        // set amount 49950, which is in the scope
        jdbcTemplate.update(
                "INSERT INTO transactions(account_id, payee_id, amount, currency, trans_type, trans_timestamp, description) VALUES (?,?,?,?,?,?,?)",
                "ACC_RULES_004",
                "PAYEE_RULES_004",
                new BigDecimal("49950.00"),
                "USD",
                "DEBIT",
                Timestamp.from(seedInstant),
                "seed-daily-limit"
        );

        Transaction tx = baseTx();
        tx.setAccountId("ACC_RULES_004");
        tx.setPayeeId("PAYEE_RULES_004");
        tx.setAmount(new BigDecimal("50.00"));
        tx.setTransTimestamp(LocalDateTime.ofInstant(txInstant, ZoneOffset.UTC));
        assertEquals(0, fixedRules.dailyLimit(tx));
        tx.setAmount(new BigDecimal("50.01"));
        assertEquals(4, fixedRules.dailyLimit(tx));
    }

}
