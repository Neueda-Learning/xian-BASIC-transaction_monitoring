package org.example.transactionmonitoringbackend.entity;

public enum AlertStatus {
    OPEN,     // Alert generated but not yet reviewed
    ACKNOWLEDGED, // Alert has been acknowledged by a user
    INVESTIGATING, // Alert is under investigation
    CLOSED, // Investigation complete, resolved or confirmed legitimate
    DISMISSED // Alert has been dismissed without action

}
