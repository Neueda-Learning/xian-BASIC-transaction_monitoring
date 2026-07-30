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

    /**
     * Tests that getAllAlert() returns all alert records in the database.
     * Two alert rows are inserted directly via JdbcTemplate, and the list
     * returned by the service is asserted to have grown by exactly 2 compared
     * to the baseline count captured before insertion.
     */
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

    /**
     * Tests that getAlertById() retrieves the correct alert when queried by its
     * auto-generated primary key.
     * A single alert row is inserted via JdbcTemplate and its generated id is
     * fetched directly from the database. The service method is then called with
     * that id, and the returned Alert is asserted to be non-null and to match
     * all inserted field values (id, transactionId, ruleId, status).
     */
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

    /**
     * Tests that createAlert() persists a new alert record to the database.
     * An Alert object is built with a transactionId and ruleId (status left null
     * so the service assigns the default), saved via the service, and the
     * generated id is then queried directly from the database to confirm the
     * row was actually inserted.
     */
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

    /**
     * Tests that updateAlertStatus() succeeds when the requested status
     * transition is valid (OPEN → ACKNOWLEDGED).
     * An alert is created with status OPEN, its generated id is fetched from
     * the database, and the service method is called to transition it to
     * ACKNOWLEDGED. The returned Alert is asserted to reflect the new status.
     */
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

    /**
     * Tests that getOpenAlerts() returns only alerts whose status is OPEN.
     * One OPEN alert and one CLOSED alert are created via the service. The
     * method is then called and every alert in the result is asserted to have
     * status OPEN and to belong to the expected transaction (9050L).
     */
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

    /**
     * Tests that updateAlertStatus() throws InvalidStatusTransitionException
     * when an illegal transition is attempted (OPEN → CLOSED directly).
     * An OPEN alert is created, its id is retrieved, and the service is called
     * with the forbidden target status. The caught exception is asserted to be
     * non-null, confirming the service enforces valid transition rules.
     */
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

    /**
     * Tests that updateAlertStatus() throws AlertNotFoundException
     * when the given alert id does not exist in the database.
     */
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
