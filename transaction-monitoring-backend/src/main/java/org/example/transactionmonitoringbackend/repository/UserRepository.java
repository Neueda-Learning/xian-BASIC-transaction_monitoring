package org.example.transactionmonitoringbackend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Returns true if a user with the given account_no exists
    public boolean existsByAccountNo(String account_no) {
        String sql = "SELECT COUNT(*) FROM User_table WHERE account_no = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, account_no);
        return count != null && count > 0;
    }

    // add user for service use
    public void addAccountNo(@RequestParam String userName,@RequestParam String accountNo){
        String sql = "INSERT INTO User_table(user_name, account_no) VALUES (?, ?)";
        jdbcTemplate.update(sql,userName,accountNo);

    }
}
