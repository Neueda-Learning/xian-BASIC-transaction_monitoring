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

    /*
     * Service layer for alert lifecycle.
     * Alert records are usually created after one or more rules are triggered.
     */
    private final AlertRepository alertRepository;
    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }


    public List<Alert> getAllAlert(){
        return  alertRepository.findAll();
    }

    /*
     * Get one alert by id.
     */
    public Alert getAlertById(Long id){
        return alertRepository.findById(id).orElse(null);
    }

    /*
     * Create a new alert with default LOW severity.
     */
    public void createAlert(Alert alert){
        createAlert(alert, AlertSeverity.LOW);
    }

    /*
     * Create an alert with explicit severity.
     * OPEN status is applied automatically when status is not provided.
     * Severity is applied when caller passes a non-null value.
     */
    public Alert createAlert(Alert alert, AlertSeverity severity) {
        if (alert.getStatus() == null) {
            alert.setStatus(AlertStatus.OPEN);
        }
        if (severity != null) {
            alert.setSeverity(severity);
        }
        return alertRepository.save(alert);
    }


    /*
     * Update alert status with transition validation.
     * Invalid transitions are rejected to keep workflow state correct.
     */
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

    /*
     * Update alert severity level.
     */
    public Alert updateAlertSeverity(Long id, AlertSeverity newSeverity) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new AlertNotFoundException("Alert not found"));
        alert.setSeverity(newSeverity);
        return alertRepository.save(alert);
    }

    /*
     * Return all OPEN alerts for monitoring queue views.
     */
    public List<Alert> getOpenAlerts(){

        return alertRepository.findByStatus(AlertStatus.OPEN);
    }




}
