package org.example.transactionmonitoringbackend.service;

import org.example.transactionmonitoringbackend.entity.MonitoringRule;
import org.example.transactionmonitoringbackend.entity.Transaction;
import org.example.transactionmonitoringbackend.repository.MonitoringRuleRepository;
import org.example.transactionmonitoringbackend.repository.TransactionRepository;
import org.example.transactionmonitoringbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class FixedRules {

    // Stable internal keys for the 4 built-in rules.
    private enum RuleType {
        AMOUNT_THRESHOLD(1L),
        VELOCITY_RULE(2L),
        NEW_PAYEE_RULE(3L),
        DAILY_LIMIT_RULE(4L);

        // Legacy alert rule id fallback when DB row is missing.
        private final Long defaultRuleId;

        RuleType(Long defaultRuleId) {
            this.defaultRuleId = defaultRuleId;
        }
    }

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final MonitoringRuleRepository monitoringRuleRepository;
    @Autowired
    public FixedRules(TransactionRepository transactionRepository,
                      UserRepository userRepository,
                      MonitoringRuleRepository monitoringRuleRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.monitoringRuleRepository = monitoringRuleRepository;
    }

    public List<Long> getTriggeredRuleIds(Transaction transaction) {
        // Read current rule config from DB for each incoming transaction.
        Map<String, MonitoringRule> rulesByType = loadRulesByType();
        // A transaction may trigger multiple rules at the same time.
        List<Long> triggeredRuleIds = new ArrayList<>();

        MonitoringRule amountRule = resolveRule(RuleType.AMOUNT_THRESHOLD, rulesByType);
        if (Boolean.TRUE.equals(amountRule.getIsActive()) && isAmountExceeded(transaction, amountRule)) {
            triggeredRuleIds.add(amountRule.getId());
        }

        MonitoringRule velocityRule = resolveRule(RuleType.VELOCITY_RULE, rulesByType);
        if (Boolean.TRUE.equals(velocityRule.getIsActive()) && isVelocityExceeded(transaction, velocityRule)) {
            triggeredRuleIds.add(velocityRule.getId());
        }

        MonitoringRule newPayeeRule = resolveRule(RuleType.NEW_PAYEE_RULE, rulesByType);
        if (Boolean.TRUE.equals(newPayeeRule.getIsActive()) && isUnknownPayee(transaction)) {
            triggeredRuleIds.add(newPayeeRule.getId());
        }

        MonitoringRule dailyRule = resolveRule(RuleType.DAILY_LIMIT_RULE, rulesByType);
        if (Boolean.TRUE.equals(dailyRule.getIsActive()) && isDailyLimitExceeded(transaction, dailyRule)) {
            triggeredRuleIds.add(dailyRule.getId());
        }

        return triggeredRuleIds;
    }

    public int checkSingleTransaction(Transaction transaction) {
        // Keep the original return contract (1/0) used by TransactionService.
        MonitoringRule amountRule = resolveRule(RuleType.AMOUNT_THRESHOLD, loadRulesByType());
        if (Boolean.TRUE.equals(amountRule.getIsActive()) && isAmountExceeded(transaction, amountRule)) {
            return 1;
        }
        return 0;
    }

    public int checkWindow(Transaction transaction) {
        // Load velocity config at call time so DB changes take effect immediately.
        MonitoringRule velocityRule = resolveRule(RuleType.VELOCITY_RULE, loadRulesByType());
        if (Boolean.TRUE.equals(velocityRule.getIsActive()) && isVelocityExceeded(transaction, velocityRule)) {
            return 2;
        }
        return 0;
    }

    public int checkFirstTransactionToPayee(Transaction transaction) {
        // Unknown payee rule can be enabled/disabled from monitoring_rules.
        MonitoringRule newPayeeRule = resolveRule(RuleType.NEW_PAYEE_RULE, loadRulesByType());
        if (Boolean.TRUE.equals(newPayeeRule.getIsActive()) && isUnknownPayee(transaction)) {
            return 3;
        }
        return 0;
    }

    public int dailyLimit(Transaction transaction) {
        // Keep rule code 4 for compatibility with existing alert writes.
        MonitoringRule dailyRule = resolveRule(RuleType.DAILY_LIMIT_RULE, loadRulesByType());
        if (Boolean.TRUE.equals(dailyRule.getIsActive()) && isDailyLimitExceeded(transaction, dailyRule)) {
            return 4;
        }
        return 0;
    }

    private Map<String, MonitoringRule> loadRulesByType() {
        // Read all rows once and resolve by rule_type in memory.
        List<MonitoringRule> rules = monitoringRuleRepository.getAllRules();
        Map<String, MonitoringRule> rulesByType = new HashMap<>();
        for (MonitoringRule rule : rules) {
            if (rule.getRuleType() == null) {
                // Ignore invalid rows without a rule key.
                continue;
            }
            String normalizedType = normalizeRuleType(rule.getRuleType());
            // Keep the latest row only (query is ordered by id DESC).
            rulesByType.putIfAbsent(normalizedType, rule);
        }
        return rulesByType;
    }

    private MonitoringRule resolveRule(RuleType ruleType, Map<String, MonitoringRule> rulesByType) {
        // Build an executable rule with DB values + safe defaults.
        MonitoringRule configured = rulesByType.get(ruleType.name());
        MonitoringRule resolved = new MonitoringRule();
        resolved.setRuleType(ruleType.name());
        resolved.setId(configured != null && configured.getId() != null ? configured.getId() : ruleType.defaultRuleId);
        // If a rule record is missing, keep it enabled and use defaults as fallback.
        resolved.setIsActive(configured == null || configured.getIsActive() == null || configured.getIsActive());

        if (ruleType == RuleType.AMOUNT_THRESHOLD) {
            resolved.setThresholdAmount(configured != null && configured.getThresholdAmount() != null
                    ? configured.getThresholdAmount()
                    : defaultAmountThreshold());
        } else if (ruleType == RuleType.VELOCITY_RULE) {
            resolved.setMaxCount(configured != null && configured.getMaxCount() != null
                    ? configured.getMaxCount()
                    : defaultVelocityMaxCount());
            resolved.setTimeWindowMinutes(configured != null && configured.getTimeWindowMinutes() != null
                    ? configured.getTimeWindowMinutes()
                    : defaultVelocityWindowMinutes());
        } else if (ruleType == RuleType.DAILY_LIMIT_RULE) {
            resolved.setDailyLimitAmount(configured != null && configured.getDailyLimitAmount() != null
                    ? configured.getDailyLimitAmount()
                    : defaultDailyLimit());
        }
        return resolved;
    }

    private String normalizeRuleType(String ruleType) {
        // Normalize input values like "velocity_rule" / " Velocity_Rule ".
        return ruleType.trim().toUpperCase(Locale.ROOT);
    }

    private Instant toInstantOrNow(Object transactionTime) {
        // Accept both Instant and LocalDateTime timestamps from different call paths.
        if (transactionTime instanceof Instant instant) {
            return instant;
        }
        if (transactionTime instanceof LocalDateTime localDateTime) {
            return localDateTime.toInstant(ZoneOffset.UTC);
        }
        return Instant.now();
    }

    private boolean isAmountExceeded(Transaction transaction, MonitoringRule rule) {
        // Null threshold falls back to defaultAmountThreshold().
        BigDecimal threshold = rule.getThresholdAmount() == null ? defaultAmountThreshold() : rule.getThresholdAmount();
        return transaction.getAmount() != null && transaction.getAmount().compareTo(threshold) > 0;
    }

    private boolean isVelocityExceeded(Transaction transaction, MonitoringRule rule) {
        // Count historical transactions in the trailing window.
        Instant txTime = toInstantOrNow(transaction.getTransTimestamp());
        int windowMinutes = rule.getTimeWindowMinutes() == null ? defaultVelocityWindowMinutes() : rule.getTimeWindowMinutes();
        int maxCount = rule.getMaxCount() == null ? defaultVelocityMaxCount() : rule.getMaxCount();
        Instant velocityStartTime = txTime.minusSeconds(windowMinutes * 60L);
        long recentCount = transactionRepository.countByAccountIdAndTransactionTimeBetween(
                transaction.getAccountId(), velocityStartTime, txTime
        );
        return recentCount + 1 > maxCount;
    }

    private boolean isUnknownPayee(Transaction transaction) {
        // Reuse trusted payee source from User_table(account_no).
        String payeeId = transaction.getPayeeId();
        return !userRepository.existsByAccountNo(payeeId);
    }

    private boolean isDailyLimitExceeded(Transaction transaction, MonitoringRule rule) {
        // Calculate projected UTC-day total including current transaction.
        Instant txTime = toInstantOrNow(transaction.getTransTimestamp());
        Instant dayStart = LocalDate.ofInstant(txTime, ZoneOffset.UTC)
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC);
        Instant dayEnd = dayStart.plusSeconds(24*60*60L);
        BigDecimal dailyTotal = transactionRepository.sumAmountByAccountIdAndTransactionTimeBetween(
                transaction.getAccountId(),
                dayStart,
                dayEnd);
        BigDecimal currentAmount = transaction.getAmount() == null ? BigDecimal.ZERO : transaction.getAmount();
        BigDecimal projectedDailyTotal = dailyTotal.add(currentAmount);
        BigDecimal dailyLimit = rule.getDailyLimitAmount() == null ? defaultDailyLimit() : rule.getDailyLimitAmount();
        return projectedDailyTotal.compareTo(dailyLimit) > 0;
    }

    private BigDecimal defaultAmountThreshold() {
        // Default for AMOUNT_THRESHOLD when not configured.
        return new BigDecimal("10000");
    }

    private int defaultVelocityMaxCount() {
        // Default max count for VELOCITY_RULE when not configured.
        return 5;
    }

    private int defaultVelocityWindowMinutes() {
        // Default time window for VELOCITY_RULE when not configured.
        return 10;
    }

    private BigDecimal defaultDailyLimit() {
        // Default daily cap for DAILY_LIMIT_RULE when not configured.
        return new BigDecimal("50000");
    }
}
