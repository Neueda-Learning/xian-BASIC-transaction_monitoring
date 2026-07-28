package org.example.transactionmonitoringbackend.service;


import org.example.transactionmonitoringbackend.entity.Alert;
import org.example.transactionmonitoringbackend.entity.AlertStatus;
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
    public Alert createAlert(Alert alert){
        if (alert.getStatus() == null) {
            alert.setStatus(AlertStatus.OPEN);
        }
        return alertRepository.save(alert);
    }

    //update alert status
    public Alert updateAlertStatus(Long id, AlertStatus newStatus){
        Alert alert = alertRepository.findById(id).orElseThrow(() ->
                new RuntimeException("Alert not found"));
        alert.setStatus(newStatus);
        return alertRepository.save(alert);
    }


}
