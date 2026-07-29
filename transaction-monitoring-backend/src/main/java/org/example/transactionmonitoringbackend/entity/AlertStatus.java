package org.example.transactionmonitoringbackend.entity;

public enum AlertStatus {
    OPEN,     // Alert generated but not yet reviewed
    ACKNOWLEDGED, // Alert has been acknowledged by a user
    INVESTIGATING, // Alert is under investigation
    CLOSED, // Investigation complete, resolved or confirmed legitimate
    DISMISSED; // Alert has been dismissed without action

    //check the status switch  valid judge
    public boolean isValidTransition(AlertStatus targetStatus) {
        if (this == targetStatus) {
            return false;
        }
        switch (this){
            case OPEN:
                return targetStatus == ACKNOWLEDGED || targetStatus == DISMISSED;
            case ACKNOWLEDGED:
                return targetStatus == INVESTIGATING || targetStatus == CLOSED || targetStatus == DISMISSED;
            case INVESTIGATING:
                return targetStatus == CLOSED || targetStatus == DISMISSED;
            case CLOSED:
                return false; // No transitions allowed from CLOSED
            case DISMISSED:
                return false; // No transitions allowed from DISMISSED
            default:
                return false;
        }
    }

}
