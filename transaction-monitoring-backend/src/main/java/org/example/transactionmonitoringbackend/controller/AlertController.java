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
//enable cross-origin requests from all origins
@CrossOrigin(origins = "*")
//map requests to /api/alerts
@RequestMapping("/api/alerts")
public class AlertController {
    private final AlertService alertService;

    /* assigns the passed service object to the class member variable alertService,
     completing dependency injection for invoking service-layer business logic inside the controller*/
    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    // search all alert
    /* Queries and returns all alert records by calling the corresponding business method in AlertService,
     with the return data type being a collection of Alert entities */
    @GetMapping
    public List<Alert> getAllAlerts() {
        return alertService.getAllAlert();
    }

    //get by id
    //@GetMapping("/{id}"):
    // Maps HTTP GET requests with a dynamic path variable id to this method.
    @GetMapping("/{id}")
    //@PathVariable Long id: Extracts numeric ID value from the request URL path.
    public ResponseEntity<Alert> getAlertById(@PathVariable Long id) {
        //Invokes service layer method to fetch single alert record by primary key ID.
        Alert alert = alertService.getAlertById(id);
        if (alert == null) {
            //If no matching alert exists, custom AlertNotFoundException is thrown.
            // Throw AlertNotFoundException if alert not found
            throw new AlertNotFoundException("Alert not found");
        }
 //ResponseEntity.ok(alert): Returns found alert data with HTTP 200 OK status code.
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
            //
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

    // find alerts by status
    @GetMapping("/status")
    public List<Alert> getAlertsByStatus(@RequestParam AlertStatus status) {
        return alertService.getAlertsByStatus(status);
    }

//    @PostMapping
//    public ResponseEntity<Alert> createAlert(@RequestBody Alert alert) {
//        Alert saved = alertService.createAlert(alert);
//        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
//    }

    @PostMapping
    //@RequestParam(required = false) AlertSeverity severity:
    // Optional query parameter for alert severity;
    public ResponseEntity<Alert> createAlert(@RequestBody Alert alert,
                                         @RequestParam(required = false) AlertSeverity severity) {
           //  //Call service method to save new alert data.
            Alert saved = alertService.createAlert(alert, severity);
            // can be omitted when calling the interface.
        // Return saved alert data with HTTP 201 Created status code,standard for resource creatio
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
}

    //update severity
    // Updates the severity of an existing alert identified by its ID.
// Handles HTTP PUT requests to: /alerts/{id}/severity?severity=<value>
// @PathVariable Long id       - extracts the alert ID from the URL path (e.g., /alerts/5/severity)
// @RequestParam AlertSeverity severity - reads the new severity level from the query string (e.g., ?severity=HIGH)
// Delegates to alertService.updateAlertSeverity() to apply the change in the database.
// Returns the updated Alert object with HTTP 200 OK on success.
    @PutMapping("/{id}/severity")
    public ResponseEntity<Alert> updateAlertSeverity(
            @PathVariable Long id,
            @RequestParam AlertSeverity severity) {
        Alert updatedAlert = alertService.updateAlertSeverity(id, severity);
        return ResponseEntity.ok(updatedAlert);
    }

}
