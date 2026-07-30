package org.example.transactionmonitoringbackend.entity;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.LocalDateTime;


//used to bind the Java entity class to the physical table in  database.
//name = "xxxx"  xxxx is the real table name in the database,
// if not specified, the default table name is the class name.
@Table(name = "alerts")
//Mark as JPA entity, map to database table
@Entity
public class Alert {
    // mark primary key
    @Id
    // define primary key generate stargedy
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // JSON field name → Java field name
    @JsonProperty("transaction_id")
    //Java field name → Database column name
    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;

    @JsonProperty("rule_id")
    @Column(name = "rule_id" ,nullable = false)
    private Long ruleId;

    //Enum Mapping
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertStatus status = AlertStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertSeverity severity = AlertSeverity.LOW;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    public Alert() {
        this.createdAt = LocalDateTime.now();
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AlertSeverity severity) {
        this.severity = severity;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
