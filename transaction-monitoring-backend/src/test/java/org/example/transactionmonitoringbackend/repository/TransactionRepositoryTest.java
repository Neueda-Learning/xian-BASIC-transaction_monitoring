package org.example.transactionmonitoringbackend.repository;

import org.example.transactionmonitoringbackend.entity.Transaction;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
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
        tx.setTransTimestamp(LocalDateTime.parse("2023-06-01T10:00:00"));
        tx.setDescription("testbycode");
        int actual = repository.addTransaction(tx);
        assertEquals(1, actual);
    }

    @Test
    void getAllTransactions_test() {
        List<Transaction> listBefore = repository.getAllTransactions();
        jdbcTemplate.update(
                "INSERT INTO transactions(account_id, payee_id, amount, currency, trans_type, trans_timestamp, description) VALUES (?,?,?,?,?,?,?)",
                "106", "107", new BigDecimal("120.00"), "USD", "DEBIT",
                LocalDateTime.of(2026, 7, 28, 11, 0, 0), "seed-1"
        );
        jdbcTemplate.update(
                "INSERT INTO transactions(account_id, payee_id, amount, currency, trans_type, trans_timestamp, description) VALUES (?,?,?,?,?,?,?)",
                "107", "106", new BigDecimal("80.20"), "USD", "CREDIT",
                LocalDateTime.of(2026, 7, 28, 11, 5, 0), "seed-2"
        );
        List<Transaction> list = repository.getAllTransactions();
        assertEquals(listBefore.size() + 2, list.size());
    }

    @Test
    void getTransactionById_test() {
        jdbcTemplate.update(
                "INSERT INTO transactions(account_id, payee_id, amount, currency, trans_type, trans_timestamp, description) VALUES (?,?,?,?,?,?,?)",
                "A10004", "P20004", new BigDecimal("300.00"), "USD", "DEBIT",
                LocalDateTime.of(2026, 7, 28, 12, 0, 0), "seed-single"
        );

        Long id = jdbcTemplate.queryForObject(
                "SELECT id FROM transactions WHERE account_id=?",
                Long.class,
                "A10004"
        );

        Transaction tx = repository.getTransactionById(id);

        assertNotNull(tx);
        assertEquals(id, tx.getId());
        assertEquals("A10004", tx.getAccountId());
        assertEquals("P20004", tx.getPayeeId());
        assertEquals(0, tx.getAmount().compareTo(new BigDecimal("300.00")));
        assertEquals("DEBIT", tx.getTransType());
    }
}
