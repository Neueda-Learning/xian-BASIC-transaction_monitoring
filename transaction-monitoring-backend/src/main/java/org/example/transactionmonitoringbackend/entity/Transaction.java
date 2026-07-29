package org.example.transactionmonitoringbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "account_id", nullable = false, length = 64)
    private String accountId;

    @NotBlank
    @Column(name = "payee_id", nullable = false, length = 64)
    private String payeeId;

    @NotNull
    @Digits(integer = 18, fraction = 2)
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @NotBlank
    @Column(name = "trans_type", nullable = false, length = 20)
    private String transType;

    @Column(name = "trans_timestamp", nullable = false)
    private LocalDateTime transTimestamp;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    private String status;

    public Transaction(Long id, String accountId, String payeeId, BigDecimal amount, String currency, String transType, LocalDateTime transTimestamp, String description, LocalDateTime createdAt, String status) {
        this.id = id;
        this.accountId = accountId;
        this.payeeId = payeeId;
        this.amount = amount;
        this.currency = currency;
        this.transType = transType;
        this.transTimestamp = transTimestamp;
        this.description = description;
        this.createdAt = createdAt;
        this.status = status;
    }
    public Transaction() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getPayeeId() {
        return payeeId;
    }

    public void setPayeeId(String payeeId) {
        this.payeeId = payeeId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTransType() {
        return transType;
    }

    public void setTransType(String transType) {
        this.transType = transType;
    }

    public LocalDateTime getTransTimestamp() {
        return transTimestamp;
    }

    public void setTransTimestamp(LocalDateTime transTimestamp) {
        this.transTimestamp = transTimestamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
