package org.example.transactionmonitoringbackend.repository;

import org.example.transactionmonitoringbackend.entity.MonitoringRule;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class MonitoringRuleRepository {

    private final JdbcTemplate jdbcTemplate;

    public MonitoringRuleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<MonitoringRule> ruleRowMapper = (rs, rowNum) -> {
        MonitoringRule rule = new MonitoringRule();
        rule.setId(rs.getLong("id"));
        rule.setRuleName(rs.getString("rule_name"));
        rule.setRuleType(rs.getString("rule_type"));
        rule.setSeverity(rs.getString("severity"));
        rule.setIsActive(rs.getBoolean("is_active"));
        rule.setThresholdAmount(rs.getBigDecimal("threshold_amount"));
        rule.setMaxCount(rs.getInt("max_count"));
        if (rs.wasNull()) rule.setMaxCount(null);

        rule.setTimeWindowMinutes(rs.getInt("time_window_minutes"));
        if (rs.wasNull()) rule.setTimeWindowMinutes(null);

        rule.setDailyLimitAmount(rs.getBigDecimal("daily_limit_amount"));
        rule.setDescription(rs.getString("description"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            rule.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            rule.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return rule;
    };

    public int addRule(MonitoringRule rule) {
        String sql = """
                INSERT INTO monitoring_rules
                (rule_name, rule_type, severity, is_active, threshold_amount, max_count, time_window_minutes, daily_limit_amount, description)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        return jdbcTemplate.update(sql,
                rule.getRuleName(),
                rule.getRuleType(),
                rule.getSeverity(),
                rule.getIsActive(),
                rule.getThresholdAmount(),
                rule.getMaxCount(),
                rule.getTimeWindowMinutes(),
                rule.getDailyLimitAmount(),
                rule.getDescription()
        );
    }

    public List<MonitoringRule> getAllRules() {
        String sql = "SELECT * FROM monitoring_rules ORDER BY id DESC";
        return jdbcTemplate.query(sql, ruleRowMapper);
    }

    public MonitoringRule getRuleById(Long id) {
        String sql = "SELECT * FROM monitoring_rules WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, ruleRowMapper, id);
    }

    public List<MonitoringRule> getActiveRules() {
        String sql = "SELECT * FROM monitoring_rules WHERE is_active = 1";
        return jdbcTemplate.query(sql, ruleRowMapper);
    }

    public int updateRule(MonitoringRule rule) {
        String sql = """
                UPDATE monitoring_rules
                SET rule_name = ?, rule_type = ?, severity = ?, is_active = ?, threshold_amount = ?,
                    max_count = ?, time_window_minutes = ?, daily_limit_amount = ?, description = ?
                WHERE id = ?
                """;

        return jdbcTemplate.update(sql,
                rule.getRuleName(),
                rule.getRuleType(),
                rule.getSeverity(),
                rule.getIsActive(),
                rule.getThresholdAmount(),
                rule.getMaxCount(),
                rule.getTimeWindowMinutes(),
                rule.getDailyLimitAmount(),
                rule.getDescription(),
                rule.getId()
        );
    }

    public int deleteRule(Long id) {
        String sql = "DELETE FROM monitoring_rules WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
}