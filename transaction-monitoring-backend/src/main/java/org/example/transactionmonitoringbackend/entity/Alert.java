package org.example.transactionmonitoringbackend.entity;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Table(name = "alerts")
@Entity
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("transaction_id")
    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;

    @JsonProperty("rule_id")
    @Column(name = "rule_id" ,nullable = false)
    private Long ruleId;

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
