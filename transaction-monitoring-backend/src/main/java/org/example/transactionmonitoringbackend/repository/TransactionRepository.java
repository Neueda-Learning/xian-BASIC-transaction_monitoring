package org.example.transactionmonitoringbackend.repository;

import org.example.transactionmonitoringbackend.entity.Transaction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TransactionRepository {

    private  final JdbcTemplate jdbcTemplate;
    public TransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //POST
    public int addTransaction(Transaction transaction){
        String sql = "INSERT INTO transactions (account_id, payee_id, amount, currency, trans_type, trans_timestamp, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                transaction.getAccountId(),
                transaction.getPayeeId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getTransType(),
                transaction.getTransTimestamp(),
                transaction.getDescription()
        );
    }

}
