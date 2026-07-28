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

    @Test
    void checkWindow_trigger_test() {
        LocalDateTime txTime = LocalDateTime.of(2026, 7, 28, 12, 0, 0);

        // 在10分钟窗口内先插入5笔同账户交易；当前这笔会让 recentCount+1 > 5
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
        tx.setTransTimestamp(txTime.minusHours(8));

        int result = fixedRules.checkWindow(tx);

        assertEquals(2, result);
    }

}
