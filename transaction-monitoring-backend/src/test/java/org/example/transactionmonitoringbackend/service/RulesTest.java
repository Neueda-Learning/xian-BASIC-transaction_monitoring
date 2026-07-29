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

    @Test
    void checkSingleTransaction_trigger_test() {
        // test rule 1, set amount out of scope
        Transaction tx = baseTx();
        tx.setAmount(new BigDecimal("10000.01")); // > 10000

        int result = fixedRules.checkSingleTransaction(tx);

        assertEquals(1, result);
    }

    @Test
    void checkWindow_trigger_test() {
        // test rule 2
        LocalDateTime txTime = LocalDateTime.of(2026, 7, 28, 12, 0, 0);
        // add 5 transactions in 5 seconds
        for (int i = 0; i < 5; i++) {
            jdbcTemplate.update(
                    "INSERT INTO transactions(account_id, payee_id, amount, currency, trans_type, trans_timestamp, description) VALUES (?,?,?,?,?,?,?)",
                    "ACC_RULES_002",
                    "PAYEE_RULES_002",
                    new BigDecimal("10.00"),
                    "USD",
                    "DEBIT",
                    txTime.minusMinutes(1).minusSeconds(i),
                    "window-" + i
            );
        }

        Transaction tx = baseTx();
        tx.setAccountId("ACC_RULES_002");
        tx.setPayeeId("PAYEE_RULES_002");
//      tx.setTransTimestamp(txTime.minusHours(8));
        int result = fixedRules.checkWindow(tx);

        assertEquals(2, result);
    }

    @Test
    void checkFirstTransactionToPayee_trigger_test() {
        // test rule 3, set PayeeId with blank
        Transaction tx = baseTx();
        tx.setPayeeId("   ");

        int result = fixedRules.checkFirstTransactionToPayee(tx);

        assertEquals(3, result);
    }

    @Test
    void dailyLimit_boundary_then_exceed_test() {
        // test rule 4
        LocalDateTime dbTime = LocalDateTime.of(2026, 7, 28, 15, 0, 0);
        // set amount 49950, which is in the scope
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
        tx.setTransTimestamp(dbTime.minusHours(8)); // 对齐 FixedRules 的 UTC 处理
        assertEquals(0, fixedRules.dailyLimit(tx));
        // set total amount out of scope
        tx.setAmount(new BigDecimal("51.00"));
        assertEquals(4, fixedRules.dailyLimit(tx));
    }

}
