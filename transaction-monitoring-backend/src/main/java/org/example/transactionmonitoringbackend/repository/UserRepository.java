package org.example.transactionmonitoringbackend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Returns true if a user with the given accountId exists
    public boolean existsByAccountId(String accountId) {
        String sql = "SELECT COUNT(*) FROM users WHERE account_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, accountId);
        return count != null && count > 0;
    }
}
