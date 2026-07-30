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

    /*
     * Stable internal keys for built-in rules.
     * Each enum value maps to one rule type used in code and in database rows.
     * The number is the default rule_id used when no database row exists.
     */
    private enum RuleType {
        AMOUNT_THRESHOLD(1L),
        VELOCITY_RULE(2L),
        NEW_PAYEE_RULE(3L),
        DAILY_LIMIT_RULE(4L);

        /*
         * Legacy fallback rule id.
         * This keeps alert creation compatible with old data and old clients.
         */
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

    /*
     * Load latest rule settings and evaluate all built-in rules.
     * One transaction may trigger multiple rules, so this method returns a list.
     */
    public List<Long> getTriggeredRuleIds(Transaction transaction) {
        Map<String, MonitoringRule> rulesByType = loadRulesByType();
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

    /*
     * Compatibility API for amount threshold rule.
     * Returns 1 when triggered, otherwise 0.
     */
    public int checkSingleTransaction(Transaction transaction) {
        MonitoringRule amountRule = resolveRule(RuleType.AMOUNT_THRESHOLD, loadRulesByType());
        if (Boolean.TRUE.equals(amountRule.getIsActive()) && isAmountExceeded(transaction, amountRule)) {
            return 1;
        }
        return 0;
    }

    /*
     * Compatibility API for velocity rule.
     * Config is read at call time so database changes apply immediately.
     * Returns 2 when triggered, otherwise 0.
     */
    public int checkWindow(Transaction transaction) {
        MonitoringRule velocityRule = resolveRule(RuleType.VELOCITY_RULE, loadRulesByType());
        if (Boolean.TRUE.equals(velocityRule.getIsActive()) && isVelocityExceeded(transaction, velocityRule)) {
            return 2;
        }
        return 0;
    }

    /*
     * Compatibility API for unknown payee rule.
     * Rule switch is controlled by monitoring_rules table.
     * Returns 3 when triggered, otherwise 0.
     */
    public int checkFirstTransactionToPayee(Transaction transaction) {
        MonitoringRule newPayeeRule = resolveRule(RuleType.NEW_PAYEE_RULE, loadRulesByType());
        if (Boolean.TRUE.equals(newPayeeRule.getIsActive()) && isUnknownPayee(transaction)) {
            return 3;
        }
        return 0;
    }

    /*
     * Compatibility API for daily limit rule.
     * Returns 4 when projected daily total exceeds limit, otherwise 0.
     */
    public int dailyLimit(Transaction transaction) {
        MonitoringRule dailyRule = resolveRule(RuleType.DAILY_LIMIT_RULE, loadRulesByType());
        if (Boolean.TRUE.equals(dailyRule.getIsActive()) && isDailyLimitExceeded(transaction, dailyRule)) {
            return 4;
        }
        return 0;
    }

    /*
     * Build a map keyed by normalized rule_type using newest-first records.
     * Invalid rows without rule_type are ignored.
     */
    private Map<String, MonitoringRule> loadRulesByType() {
        List<MonitoringRule> rules = monitoringRuleRepository.getAllRules();
        Map<String, MonitoringRule> rulesByType = new HashMap<>();
        for (MonitoringRule rule : rules) {
            if (rule.getRuleType() == null) {
                continue;
            }
            String normalizedType = normalizeRuleType(rule.getRuleType());
            rulesByType.putIfAbsent(normalizedType, rule);
        }
        return rulesByType;
    }

    /*
     * Resolve one runtime rule object from database data plus defaults.
     * Missing rows are treated as active with safe fallback parameters.
     */
    private MonitoringRule resolveRule(RuleType ruleType, Map<String, MonitoringRule> rulesByType) {
        MonitoringRule configured = rulesByType.get(ruleType.name());
        MonitoringRule resolved = new MonitoringRule();
        resolved.setRuleType(ruleType.name());
        resolved.setId(configured != null && configured.getId() != null ? configured.getId() : ruleType.defaultRuleId);
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

    /*
     * Normalize rule_type into uppercase key format for map lookup.
     */
    private String normalizeRuleType(String ruleType) {
        return ruleType.trim().toUpperCase(Locale.ROOT);
    }

    /*
     * Convert supported timestamp types into Instant.
     * Falls back to current time when input is null or unsupported.
     */
    private Instant toInstantOrNow(Object transactionTime) {
        if (transactionTime instanceof Instant instant) {
            return instant;
        }
        if (transactionTime instanceof LocalDateTime localDateTime) {
            return localDateTime.toInstant(ZoneOffset.UTC);
        }
        return Instant.now();
    }

    /*
     * Return true when transaction amount is greater than threshold.
     */
    private boolean isAmountExceeded(Transaction transaction, MonitoringRule rule) {
        BigDecimal threshold = rule.getThresholdAmount() == null ? defaultAmountThreshold() : rule.getThresholdAmount();
        return transaction.getAmount() != null && transaction.getAmount().compareTo(threshold) > 0;
    }

    /*
     * Return true when transaction frequency in a trailing window exceeds maxCount.
     */
    private boolean isVelocityExceeded(Transaction transaction, MonitoringRule rule) {
        Instant txTime = toInstantOrNow(transaction.getTransTimestamp());
        int windowMinutes = rule.getTimeWindowMinutes() == null ? defaultVelocityWindowMinutes() : rule.getTimeWindowMinutes();
        int maxCount = rule.getMaxCount() == null ? defaultVelocityMaxCount() : rule.getMaxCount();
        Instant velocityStartTime = txTime.minusSeconds(windowMinutes * 60L);
        long recentCount = transactionRepository.countByAccountIdAndTransactionTimeBetween(
                transaction.getAccountId(), velocityStartTime, txTime
        );
        return recentCount + 1 > maxCount;
    }

    /*
     * Return true when payee account is not found in trusted user accounts.
     */
    private boolean isUnknownPayee(Transaction transaction) {
        String payeeId = transaction.getPayeeId();
        return !userRepository.existsByAccountNo(payeeId);
    }

    /*
     * Return true when projected UTC-day total is above the daily limit.
     */
    private boolean isDailyLimitExceeded(Transaction transaction, MonitoringRule rule) {
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

    /*
     * Default threshold for AMOUNT_THRESHOLD.
     */
    private BigDecimal defaultAmountThreshold() {
        return new BigDecimal("10000");
    }

    /*
     * Default maxCount for VELOCITY_RULE.
     */
    private int defaultVelocityMaxCount() {
        return 5;
    }

    /*
     * Default timeWindowMinutes for VELOCITY_RULE.
     */
    private int defaultVelocityWindowMinutes() {
        return 10;
    }

    /*
     * Default daily limit amount for DAILY_LIMIT_RULE.
     */
    private BigDecimal defaultDailyLimit() {
        return new BigDecimal("50000");
    }
}
