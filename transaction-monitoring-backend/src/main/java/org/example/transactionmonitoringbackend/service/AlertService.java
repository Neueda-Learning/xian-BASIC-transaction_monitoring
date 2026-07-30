package org.example.transactionmonitoringbackend.service;


import org.example.transactionmonitoringbackend.entity.Alert;
import org.example.transactionmonitoringbackend.entity.AlertSeverity;
import org.example.transactionmonitoringbackend.entity.AlertStatus;
import org.example.transactionmonitoringbackend.exception.AlertNotFoundException;
import org.example.transactionmonitoringbackend.exception.InvalidStatusTransitionException;
import org.example.transactionmonitoringbackend.repository.AlertRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }


    // get all alert
    // retrieves all alerts from the database.
    public List<Alert> getAllAlert(){
        return  alertRepository.findAll();
    }

    //get By ID
    public Alert getAlertById(Long id){
        return alertRepository.findById(id).orElse(null);
    }

    //create new alert
//    public Alert createAlert(Alert alert){
//        if (alert.getStatus() == null) {
//            alert.setStatus(AlertStatus.OPEN);
//        }
//        return alertRepository.save(alert);
//    }

    // Full method (accepts custom severity)
    //createAlert(Alert alert, AlertSeverity severity) { ... }
    //

    // This overload — caller doesn't need to specify severity
    //createAlert(Alert alert) {
    //    createAlert(alert, AlertSeverity.LOW);  // defaults to LOW
    //}
    public void createAlert(Alert alert){
        createAlert(alert, AlertSeverity.LOW);
    }

    // Full method (accepts custom severity)
    // Input: Alert + optional Severity
    //  ↓ Set status = OPEN (if missing)
    //  ↓ Set severity (if provided)
    //  ↓ Save to DB
    //Output: Saved Alert with ID
    public Alert createAlert(Alert alert, AlertSeverity severity) {
        if (alert.getStatus() == null) {
            alert.setStatus(AlertStatus.OPEN);
        }
        // severity setting
        if (severity != null) {
            alert.setSeverity(severity);
        }
        return alertRepository.save(alert);
    }


    //update alert status
//    public Alert updateAlertStatus(Long id, AlertStatus newStatus){
//        Alert alert = alertRepository.findById(id).orElseThrow(() ->
//                new RuntimeException("Alert not found"));
//        alert.setStatus(newStatus);
//        return alertRepository.save(alert);
//    }

    //Input: id + newStatus
    //  ↓ Find alert (or throw 404)
    //  ↓ Validate transition (or throw 400)
    //  ↓ Update & save to DB
    //Output: Updated Alert
    public Alert updateAlertStatus(Long id, AlertStatus newStatus) {
        Alert alert = alertRepository.findById(id).orElseThrow(() ->
                new AlertNotFoundException("Alert not found"));
        AlertStatus currentStatus = alert.getStatus();
        if (!currentStatus.isValidTransition(newStatus)) {
            throw new InvalidStatusTransitionException(
                    String.format("invalid switch : %s to %s", currentStatus, newStatus)
            );

        }
        alert.setStatus(newStatus);
        return alertRepository.save(alert);
    }

    //update severity
    //Input: id + newSeverity
    //  ↓ Find alert (or throw 404)
    //  ↓ Update severity
    //  ↓ Save to DB
    //Output: Updated Alert
    public Alert updateAlertSeverity(Long id, AlertSeverity newSeverity) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new AlertNotFoundException("Alert not found"));
        alert.setSeverity(newSeverity);
        return alertRepository.save(alert);
    }

    //get open alert
    // Retrieves all alerts with status OPEN from the database.
    //getAllAlert()     → SELECT * FROM alerts
    //getOpenAlerts()   → SELECT * FROM alerts WHERE status = 'OPEN'
    public List<Alert> getOpenAlerts(){
        return alertRepository.findByStatus(AlertStatus.OPEN);
    }

    // get alerts by status
    // Retrieves all alerts with the specified status from the database.
    //getAlertsByStatus(status) → SELECT * FROM alerts WHERE status = 'status'
    public List<Alert> getAlertsByStatus(AlertStatus status) {
        return alertRepository.findByStatus(status);
    }
}


//Alert alert = new Alert();
//        alert.setTransactionId(transaction.getId());
//        alert.setRuleId(rule.getId());
//        alertService.createAlert(alert);
