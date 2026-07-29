package org.example.transactionmonitoringbackend.controller;


import org.example.transactionmonitoringbackend.entity.Alert;
import org.example.transactionmonitoringbackend.entity.AlertSeverity;
import org.example.transactionmonitoringbackend.entity.AlertStatus;
import org.example.transactionmonitoringbackend.exception.AlertNotFoundException;
import org.example.transactionmonitoringbackend.service.AlertService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/alerts")
public class AlertController {
    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }
    // search all alert
    @GetMapping
    public List<Alert> getAllAlerts() {
        return alertService.getAllAlert();
    }

    //get by id
    @GetMapping("/{id}")
    public ResponseEntity<Alert> getAlertById(@PathVariable Long id) {
        Alert alert = alertService.getAlertById(id);
        if (alert == null) {
            throw new AlertNotFoundException("Alert not found");
        }
        return ResponseEntity.ok(alert);
    }

    //update status
//    @PutMapping("/{id}/status")
//    public  ResponseEntity<Alert>  updateAlertStatus(
//            @PathVariable Long id,
//            @RequestParam AlertStatus status
//    ){
//        Alert updateALert = alertService.updateAlertStatus(id, status);
//        return ResponseEntity.ok(updateALert);
//    }
    @PutMapping("/{id}/status")
    public ResponseEntity<Alert> updateAlertStatus(
            @PathVariable Long id,
            @RequestParam AlertStatus status
    ) {
        Alert updatedAlert = alertService.updateAlertStatus(id, status);
        return ResponseEntity.ok(updatedAlert);
    }
    // find all open alert
    @GetMapping("/open")
    public List<Alert> getOpenAlerts() {
        return alertService.getOpenAlerts();
    }

//    @PostMapping
//    public ResponseEntity<Alert> createAlert(@RequestBody Alert alert) {
//        Alert saved = alertService.createAlert(alert);
//        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
//    }

    @PostMapping
    public ResponseEntity<Alert> createAlert(@RequestBody Alert alert,
                                         @RequestParam(required = false) AlertSeverity severity) {
            Alert saved = alertService.createAlert(alert, severity);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
}

    //update severity
    @PutMapping("/{id}/severity")
    public ResponseEntity<Alert> updateAlertSeverity(
            @PathVariable Long id,
            @RequestParam AlertSeverity severity) {
        Alert updatedAlert = alertService.updateAlertSeverity(id, severity);
        return ResponseEntity.ok(updatedAlert);
    }

}
