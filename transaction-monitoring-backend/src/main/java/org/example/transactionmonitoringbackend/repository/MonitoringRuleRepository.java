package org.example.transactionmonitoringbackend.repository;

import org.example.transactionmonitoringbackend.entity.MonitoringRule;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MonitoringRuleRepository {

    /*
     * Repository for direct SQL access to monitoring_rules table.
     * Rule data is stored as generic columns and interpreted by FixedRules.
     */
    private final JdbcTemplate jdbcTemplate;

    public MonitoringRuleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /*
     * Row mapper for monitoring_rules.
     * Nullable numeric fields are preserved as null so FixedRules can apply defaults.
     */
    private final RowMapper<MonitoringRule> ruleRowMapper = (rs, rowNum) -> {
        MonitoringRule rule = new MonitoringRule();
        rule.setId(rs.getLong("id"));
        rule.setRuleType(rs.getString("rule_type"));
        rule.setIsActive(rs.getBoolean("is_active"));
        rule.setThresholdAmount(rs.getBigDecimal("threshold_amount"));
        rule.setMaxCount(rs.getInt("max_count"));
        if (rs.wasNull()) rule.setMaxCount(null);

        rule.setTimeWindowMinutes(rs.getInt("time_window_minutes"));
        if (rs.wasNull()) rule.setTimeWindowMinutes(null);

        rule.setDailyLimitAmount(rs.getBigDecimal("daily_limit_amount"));
        return rule;
    };

    public int addRule(MonitoringRule rule) {
        String sql = """
                INSERT INTO monitoring_rules
                (rule_type, is_active, threshold_amount, max_count, time_window_minutes, daily_limit_amount)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        return jdbcTemplate.update(sql,
                rule.getRuleType(),
                rule.getIsActive(),
                rule.getThresholdAmount(),
                rule.getMaxCount(),
                rule.getTimeWindowMinutes(),
                rule.getDailyLimitAmount()
        );
    }

    /*
     * Return all rules in descending id order.
     * FixedRules uses this ordering to select the latest row per rule_type.
     */
    public List<MonitoringRule> getAllRules() {
        String sql = """
                SELECT id, rule_type, is_active, threshold_amount, max_count, time_window_minutes, daily_limit_amount
                FROM monitoring_rules
                ORDER BY id DESC
                """;
        return jdbcTemplate.query(sql, ruleRowMapper);
    }

    public List<MonitoringRule> getLatestRules() {
        String sql = """
                SELECT mr.id, mr.rule_type, mr.is_active, mr.threshold_amount, mr.max_count, mr.time_window_minutes, mr.daily_limit_amount
                FROM monitoring_rules mr
                INNER JOIN (
                    SELECT rule_type, MAX(id) AS latest_id
                    FROM monitoring_rules
                    GROUP BY rule_type
                ) latest ON mr.id = latest.latest_id
                ORDER BY mr.id ASC
                """;
        return jdbcTemplate.query(sql, ruleRowMapper);
    }

    public MonitoringRule getRuleById(Long id) {
        String sql = """
                SELECT id, rule_type, is_active, threshold_amount, max_count, time_window_minutes, daily_limit_amount
                FROM monitoring_rules
                WHERE id = ?
                """;
        return jdbcTemplate.queryForObject(sql, ruleRowMapper, id);
    }

    public List<MonitoringRule> getActiveRules() {
        String sql = """
                SELECT id, rule_type, is_active, threshold_amount, max_count, time_window_minutes, daily_limit_amount
                FROM monitoring_rules
                WHERE is_active = 1
                """;
        return jdbcTemplate.query(sql, ruleRowMapper);
    }

    public List<MonitoringRule> getRuleHistory(int limit) {
        String sql = """
                SELECT id, rule_type, is_active, threshold_amount, max_count, time_window_minutes, daily_limit_amount
                FROM monitoring_rules
                ORDER BY id DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, ruleRowMapper, limit);
    }

    public int deleteRule(Long id) {
        String sql = "DELETE FROM monitoring_rules WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
}