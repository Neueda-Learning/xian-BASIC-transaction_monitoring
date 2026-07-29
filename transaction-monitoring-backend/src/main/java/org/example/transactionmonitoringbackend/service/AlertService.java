package org.example.transactionmonitoringbackend.service;


import org.example.transactionmonitoringbackend.entity.Alert;
import org.example.transactionmonitoringbackend.entity.AlertSeverity;
import org.example.transactionmonitoringbackend.entity.AlertStatus;
import org.example.transactionmonitoringbackend.exception.InvalidStatusTransitionException;
import org.example.transactionmonitoringbackend.exception.ResourceNotFoundException;
import org.example.transactionmonitoringbackend.repository.AlertRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }


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
    public Alert updateAlertStatus(Long id, AlertStatus newStatus) {
        Alert alert = alertRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Alert not found"));
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
    public Alert updateAlertSeverity(Long id, AlertSeverity newSeverity) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));
        alert.setSeverity(newSeverity);
        return alertRepository.save(alert);
    }

    //get open alert
    public List<Alert> getOpenAlerts(){

        return alertRepository.findByStatus(AlertStatus.OPEN);
    }




}


//Alert alert = new Alert();
//        alert.setTransactionId(transaction.getId());
//        alert.setRuleId(rule.getId());
//        alertService.createAlert(alert);
