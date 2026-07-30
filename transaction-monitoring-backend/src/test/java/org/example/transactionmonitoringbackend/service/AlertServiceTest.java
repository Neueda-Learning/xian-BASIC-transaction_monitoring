package org.example.transactionmonitoringbackend.service;

import org.example.transactionmonitoringbackend.entity.Alert;
import org.example.transactionmonitoringbackend.entity.AlertStatus;
import org.example.transactionmonitoringbackend.exception.AlertNotFoundException;
import org.example.transactionmonitoringbackend.exception.InvalidStatusTransitionException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
        alert.setStatus(null);

        alertService.createAlert(alert);
        Long id = jdbcTemplate.queryForObject(
                "SELECT id FROM alerts WHERE transaction_id = ? and rule_id = ?",
                Long.class,
                9020L, 4L
        );

        assertNotNull(id);
    }

    @Test
    void updateAlertStatus_validTransition_test() {
        Alert alert = new Alert();
        alert.setTransactionId(9030L);
        alert.setRuleId(1L);
        alert.setStatus(AlertStatus.OPEN);
        alertService.createAlert(alert);

        Long savedId = jdbcTemplate.queryForObject(
                "SELECT id FROM alerts WHERE transaction_id = ? and rule_id = ?",
                Long.class,
                9030L, 1L
        );

        Alert updated = alertService.updateAlertStatus(savedId, AlertStatus.ACKNOWLEDGED);

        assertEquals(AlertStatus.ACKNOWLEDGED, updated.getStatus());
    }

    @Test
    void getOpenAlerts_test() {
        Alert open = new Alert();
        open.setTransactionId(9050L);
        open.setRuleId(1L);
        open.setStatus(AlertStatus.OPEN);
        alertService.createAlert(open);

        Alert closed = new Alert();
        closed.setTransactionId(9051L);
        closed.setRuleId(2L);
        closed.setStatus(AlertStatus.CLOSED);
        alertService.createAlert(closed);

        List<Alert> openAlerts = alertService.getOpenAlerts();

        for (Alert a : openAlerts) {
            assertTrue(a.getStatus() == AlertStatus.OPEN);
            assertTrue(a.getTransactionId().equals(9050L) );
        }

    }

    @Test
    void updateAlertStatus_invalidTransition_test() {
        Alert alert = new Alert();
        alert.setTransactionId(9031L);
        alert.setRuleId(1L);
        alert.setStatus(AlertStatus.OPEN);
        alertService.createAlert(alert);

        Long savedId = jdbcTemplate.queryForObject(
                "SELECT id FROM alerts WHERE transaction_id = ? and rule_id = ?",
                Long.class,
                9031L, 1L
        );

        InvalidStatusTransitionException exception = null;
        try {
            alertService.updateAlertStatus(savedId, AlertStatus.CLOSED);
        } catch (InvalidStatusTransitionException e) {
            exception = e;
        }
        assertNotNull(exception);
    }

    @Test
    void updateAlertStatus_notFound_test() {
        AlertNotFoundException exception = null;
        try {
            alertService.updateAlertStatus(999999L, AlertStatus.ACKNOWLEDGED);
        } catch (AlertNotFoundException e) {
            exception = e;
        }
        assertNotNull(exception);
    }
}
