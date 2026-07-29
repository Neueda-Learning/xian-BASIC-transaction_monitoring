package org.example.transactionmonitoringbackend.repository;

import org.example.transactionmonitoringbackend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class TransactionRepository  {

    private  final JdbcTemplate jdbcTemplate;
    public TransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //POST
    public int addTransaction(Transaction transaction){
        String sql = "INSERT INTO transactions (account_id, payee_id, amount, currency, trans_type, trans_timestamp, description, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                transaction.getAccountId(),
                transaction.getPayeeId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getTransType(),
                transaction.getTransTimestamp(),
                transaction.getDescription(),
                transaction.getStatus()
        );
    }

    // GET ALL
    public List<Transaction> getAllTransactions() {
        String sql = "SELECT * FROM transactions";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Transaction transaction = new Transaction();
            transaction.setId(rs.getLong("id"));
            transaction.setAccountId(rs.getString("account_id"));
            transaction.setPayeeId(rs.getString("payee_id"));
            transaction.setAmount(rs.getBigDecimal("amount"));
            transaction.setCurrency(rs.getString("currency"));
            transaction.setTransType(rs.getString("trans_type"));
            transaction.setTransTimestamp(rs.getTimestamp("trans_timestamp").toLocalDateTime());
            transaction.setDescription(rs.getString("description"));
            transaction.setStatus(rs.getString("status"));
            Timestamp createdAt = rs.getTimestamp("created_at");
            if (createdAt != null) {
                transaction.setCreatedAt(createdAt.toLocalDateTime());
            }

            return transaction;
        });
    }

    // GET BY ID
    public Transaction getTransactionById(Long id) {
        String sql = "SELECT * FROM transactions WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            Transaction transaction = new Transaction();
            transaction.setId(rs.getLong("id"));
            transaction.setAccountId(rs.getString("account_id"));
            transaction.setPayeeId(rs.getString("payee_id"));
            transaction.setAmount(rs.getBigDecimal("amount"));
            transaction.setCurrency(rs.getString("currency"));
            transaction.setTransType(rs.getString("trans_type"));
            transaction.setTransTimestamp(rs.getTimestamp("trans_timestamp").toLocalDateTime());
            transaction.setDescription(rs.getString("description"));
            transaction.setStatus(rs.getString("status"));
            Timestamp createdAt = rs.getTimestamp("created_at");
            if (createdAt != null) {
                transaction.setCreatedAt(createdAt.toLocalDateTime());
            }

            return transaction;
        }, id);
    }

    public long countByAccountIdAndTransactionTimeBetween(String accountId, Instant startTime, Instant endTime){
        String sql = """
                select count(*)
                from transactions
                where account_id = ?
                and trans_timestamp between ? and ?
                """;
        Long count = jdbcTemplate.queryForObject(
                sql,
                Long.class,
                accountId,
                Timestamp.from(startTime),
                Timestamp.from(endTime)
        );
        return count == null ? 0L : count;
    }

    public BigDecimal sumAmountByAccountIdAndTransactionTimeBetween(String accountId, Instant startTime, Instant endTime){
        String sql = """
                    select coalesce(sum(amount), 0)
                    from transactions
                    where account_id = ?
                        and trans_timestamp >= ?
                        and trans_timestamp < ?
                """;
        BigDecimal sum = jdbcTemplate.queryForObject(
                sql,
                BigDecimal.class,
                accountId,
                Timestamp.from(startTime),
                Timestamp.from(endTime)
        );
        return sum == null ? BigDecimal.ZERO : sum;
    }

    public long countByAccountIdAndPayeeId(String accountId, String payeeId) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE account_id = ? AND payee_id = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, accountId, payeeId);
        return count == null ? 0L : count;
    }

    //add and get id
    public Transaction save(Transaction transaction){
        String sql = "INSERT INTO transactions (" +
                "account_id, payee_id,amount, currency, trans_type, trans_timestamp, description, status" +
                ") VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection ->{
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, transaction.getAccountId());
            ps.setString(2, transaction.getPayeeId());
            ps.setBigDecimal(3, transaction.getAmount());
            ps.setString(4, transaction.getCurrency());
            ps.setString(5, transaction.getTransType());
            ps.setTimestamp(6, Timestamp.valueOf(transaction.getTransTimestamp()));
            ps.setString(7, transaction.getDescription());
            ps.setString(8, transaction.getStatus());
            return ps;
        }, keyHolder);
        transaction.setId(keyHolder.getKey().longValue());
        return transaction;
    }

    public List<Transaction> filterByAmountAndTimeRange(BigDecimal minAmount,
                                                        BigDecimal maxAmount,
                                                        LocalDateTime startTime,
                                                        LocalDateTime endTime) {
        String sql = "SELECT * FROM transactions " +
                "WHERE amount BETWEEN ? AND ? " +
                "AND trans_timestamp BETWEEN ? AND ? " +
                "ORDER BY trans_timestamp DESC";

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapTransactionRow(rs),
                minAmount,
                maxAmount,
                Timestamp.valueOf(startTime),
                Timestamp.valueOf(endTime)
        );
    }

    private Transaction mapTransactionRow(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setId(rs.getLong("id"));
        transaction.setAccountId(rs.getString("account_id"));
        transaction.setPayeeId(rs.getString("payee_id"));
        transaction.setAmount(rs.getBigDecimal("amount"));
        transaction.setCurrency(rs.getString("currency"));
        transaction.setTransType(rs.getString("trans_type"));

        Timestamp transTimestamp = rs.getTimestamp("trans_timestamp");
        if (transTimestamp != null) {
            transaction.setTransTimestamp(transTimestamp.toLocalDateTime());
        }

        transaction.setDescription(rs.getString("description"));
        transaction.setStatus(rs.getString("status"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            transaction.setCreatedAt(createdAt.toLocalDateTime());
        }

        return transaction;
    }

}
