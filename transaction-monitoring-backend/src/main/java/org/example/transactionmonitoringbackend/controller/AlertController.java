package org.example.transactionmonitoringbackend.controller;


import org.example.transactionmonitoringbackend.entity.Alert;
import org.example.transactionmonitoringbackend.entity.AlertStatus;
import org.example.transactionmonitoringbackend.service.AlertService;
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
       Alert alert  = alertService.getAlertById(id);
       if (alert == null) {
           return ResponseEntity.notFound().build();
       }else {
           return ResponseEntity.ok(alert);
       }
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
    public  ResponseEntity<?>  updateAlertStatus(
            @PathVariable Long id,
            @RequestParam AlertStatus status
    ) {
        try {
            Alert updatedAlert = alertService.updateAlertStatus(id, status);
            return ResponseEntity.ok(updatedAlert);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }





    // find all open alert
    @GetMapping("/open")
    public List<Alert> getOpenAlerts() {
        return alertService.getOpenAlerts();
    }

}
