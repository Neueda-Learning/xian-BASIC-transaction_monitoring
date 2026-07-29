package org.example.transactionmonitoringbackend.service;

import org.example.transactionmonitoringbackend.entity.Alert;
import org.example.transactionmonitoringbackend.entity.AlertStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
public class AlertServiceTest {
    @Autowired
    private AlertService alertService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void getAllAlert_Test() {
        List<Alert> before = alertService.getAllAlert();
        jdbcTemplate.update(
                "INSERT INTO alerts(transaction_id, rule_id, status, created_at) values (?, ?, ?, ?)",
                "955", 1, "OPEN", LocalDateTime.now()
        );
        jdbcTemplate.update(
                "INSERT INTO alerts(transaction_id, rule_id, status, created_at) values (?, ?, ?, ?)",
                "955", 2, "OPEN", LocalDateTime.now()
        );
        List<Alert> after = alertService.getAllAlert();
        assertEquals(before.size() + 2, after.size());
    }

    @Test
    void getAlertByID() {
        jdbcTemplate.update(
                "INSERT INTO alerts(transaction_id, rule_id, status, created_at) values (?, ?, ?, ?)",
                "955", 1, "OPEN", LocalDateTime.now()
        );
        Long id = jdbcTemplate.queryForObject(
                "SELECT id FROM alerts WHERE transaction_id = ?",
                Long.class,
                955L
        );
        Alert alert = alertService.getAlertById(id);
        assertNotNull(alert);
        assertEquals(id, alert.getId());
        assertEquals(955L, alert.getTransactionId());
        assertEquals(1, alert.getRuleId());
        assertEquals("OPEN", alert.getStatus().name());
    }

    @Test
    public void createAlert_test() {
        Alert alert = new Alert();
        alert.setTransactionId(9020L);
        alert.setRuleId(4L);
        alert.setStatus(null); // 验证默认 OPEN

        Alert saved = alertService.createAlert(alert);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals(AlertStatus.OPEN, saved.getStatus());
        assertEquals(9020L, saved.getTransactionId());
        assertEquals(4L, saved.getRuleId());
    }

    @Test
    void updateAlertStatus_validTransition_test() {
        Alert alert = new Alert();
        alert.setTransactionId(9030L);
        alert.setRuleId(1L);
        alert.setStatus(AlertStatus.OPEN);
        Alert saved = alertService.createAlert(alert);

        Alert updated = alertService.updateAlertStatus(saved.getId(), AlertStatus.ACKNOWLEDGED);

        assertEquals(AlertStatus.ACKNOWLEDGED, updated.getStatus());
    }
}
