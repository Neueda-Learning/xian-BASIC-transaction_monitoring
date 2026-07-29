package org.example.transactionmonitoringbackend.service;

import org.example.transactionmonitoringbackend.entity.MonitoringRule;
import org.example.transactionmonitoringbackend.repository.MonitoringRuleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MonitoringRuleService {
    // CRUD service for monitoring_rules; runtime evaluation is in FixedRules.
    private final MonitoringRuleRepository monitoringRuleRepository;


    public MonitoringRuleService(MonitoringRuleRepository monitoringRuleRepository) {
        this.monitoringRuleRepository = monitoringRuleRepository;
    }

    public int addRule(MonitoringRule rule) {
        return monitoringRuleRepository.addRule(rule);
    }

    public List<MonitoringRule> getAllRules() {
        return monitoringRuleRepository.getAllRules();
    }

    public MonitoringRule getRuleById(Long id) {
        return monitoringRuleRepository.getRuleById(id);
    }

    public List<MonitoringRule> getActiveRules() {
        return monitoringRuleRepository.getActiveRules();
    }

    public int updateRule(MonitoringRule rule) {
        return monitoringRuleRepository.updateRule(rule);
    }

    public int deleteRule(Long id) {
        return monitoringRuleRepository.deleteRule(id);
    }
}




























