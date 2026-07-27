package org.example.transactionmonitoringbackend.controller;

import org.example.transactionmonitoringbackend.entity.MonitoringRule;
import org.example.transactionmonitoringbackend.service.MonitoringRuleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rules")
public class MonitoringRuleController {

    private final MonitoringRuleService monitoringRuleService;

    public MonitoringRuleController(MonitoringRuleService monitoringRuleService) {
        this.monitoringRuleService = monitoringRuleService;
    }

    @PostMapping
    public String addRule(@RequestBody MonitoringRule rule) {
        int result = monitoringRuleService.addRule(rule);
        if (result == 1) {
            return "add rule success";
        } else {
            return "add rule false";
        }
    }

    @GetMapping
    public List<MonitoringRule> getAllRules() {
        return monitoringRuleService.getAllRules();
    }

    @GetMapping("/{id}")
    public MonitoringRule getRuleById(@PathVariable Long id) {
        return monitoringRuleService.getRuleById(id);
    }

    @GetMapping("/active")
    public List<MonitoringRule> getActiveRules() {
        return monitoringRuleService.getActiveRules();
    }

    @PutMapping("/{id}")
    public String updateRule(@PathVariable Long id, @RequestBody MonitoringRule rule) {
        rule.setId(id);
        int result = monitoringRuleService.updateRule(rule);
        if (result == 1) {
            return "update rule success";
        } else {
            return "update rule false";
        }
    }

    @DeleteMapping("/{id}")
    public String deleteRule(@PathVariable Long id) {
        int result = monitoringRuleService.deleteRule(id);
        if (result == 1) {
            return "delete rule success";
        } else {
            return "delete rule false";
        }
    }
}