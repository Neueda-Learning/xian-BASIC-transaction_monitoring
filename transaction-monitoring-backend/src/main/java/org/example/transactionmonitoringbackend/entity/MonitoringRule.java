package org.example.transactionmonitoringbackend.entity;

import java.math.BigDecimal;

public class MonitoringRule {

    // Minimal fields required by runtime rule evaluation.
    private Long id;
    private String ruleType;
    private Boolean isActive;
    private BigDecimal thresholdAmount;
    private Integer maxCount;
    private Integer timeWindowMinutes;
    private BigDecimal dailyLimitAmount;

    public MonitoringRule() {
    }

    public MonitoringRule(Long id,
                          String ruleType,
                          Boolean isActive,
                          BigDecimal thresholdAmount,
                          Integer maxCount,
                          Integer timeWindowMinutes,
                          BigDecimal dailyLimitAmount) {
        this.id = id;
        this.ruleType = ruleType;
        this.isActive = isActive;
        this.thresholdAmount = thresholdAmount;
        this.maxCount = maxCount;
        this.timeWindowMinutes = timeWindowMinutes;
        this.dailyLimitAmount = dailyLimitAmount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRuleType() { return ruleType; }
    public void setRuleType(String ruleType) { this.ruleType = ruleType; }

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
}