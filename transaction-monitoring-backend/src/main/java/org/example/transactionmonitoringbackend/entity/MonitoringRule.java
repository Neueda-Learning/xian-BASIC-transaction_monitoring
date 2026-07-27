package org.example.transactionmonitoringbackend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MonitoringRule {

    private Long id;
    private String ruleName;
    private String ruleType;
    private String severity;
    private Boolean isActive;
    private BigDecimal thresholdAmount;
    private Integer maxCount;
    private Integer timeWindowMinutes;
    private BigDecimal dailyLimitAmount;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MonitoringRule() {
    }

    public MonitoringRule(Long id, String ruleName, String ruleType, String severity, Boolean isActive,
                          BigDecimal thresholdAmount, Integer maxCount, Integer timeWindowMinutes,
                          BigDecimal dailyLimitAmount, String description,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.ruleName = ruleName;
        this.ruleType = ruleType;
        this.severity = severity;
        this.isActive = isActive;
        this.thresholdAmount = thresholdAmount;
        this.maxCount = maxCount;
        this.timeWindowMinutes = timeWindowMinutes;
        this.dailyLimitAmount = dailyLimitAmount;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public String getRuleType() { return ruleType; }
    public void setRuleType(String ruleType) { this.ruleType = ruleType; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean active) { isActive = active; }

    public BigDecimal getThresholdAmount() { return thresholdAmount; }
    public void setThresholdAmount(BigDecimal thresholdAmount) { this.thresholdAmount = thresholdAmount; }

    public Integer getMaxCount() { return maxCount; }
    public void setMaxCount(Integer maxCount) { this.maxCount = maxCount; }

    public Integer getTimeWindowMinutes() { return timeWindowMinutes; }
    public void setTimeWindowMinutes(Integer timeWindowMinutes) { this.timeWindowMinutes = timeWindowMinutes; }

    public BigDecimal getDailyLimitAmount() { return dailyLimitAmount; }
    public void setDailyLimitAmount(BigDecimal dailyLimitAmount) { this.dailyLimitAmount = dailyLimitAmount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}