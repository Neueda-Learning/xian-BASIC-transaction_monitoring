package org.example.transactionmonitoringbackend.controller;

import org.example.transactionmonitoringbackend.entity.MonitoringRule;
import org.example.transactionmonitoringbackend.service.MonitoringRuleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rules")
@CrossOrigin(origins = "*")
public class MonitoringRuleController {

    /*
     * REST controller for monitoring rule management.
     * It provides basic CRUD endpoints used by admin/config pages.
     */
    private final MonitoringRuleService monitoringRuleService;

    public MonitoringRuleController(MonitoringRuleService monitoringRuleService) {
        this.monitoringRuleService = monitoringRuleService;
    }

    @PostMapping
    public String addRule(@RequestBody MonitoringRule rule) {
        /*
         * Create a new rule row in monitoring_rules table.
         * Return a simple status message for current frontend behavior.
         */
        int result = monitoringRuleService.addRule(rule);
        if (result == 1) {
            return "add rule success";
        } else {
            return "add rule false";
        }
    }

    @GetMapping
    public List<MonitoringRule> getAllRules() {
        /*
         * Return all rule rows ordered by repository query behavior.
         */
        return monitoringRuleService.getAllRules();
    }

    @GetMapping("/{id}")
    public MonitoringRule getRuleById(@PathVariable Long id) {
        return monitoringRuleService.getRuleById(id);
    }

    @GetMapping("/active")
    public List<MonitoringRule> getActiveRules() {
        /*
         * Return only active rules that can be used in runtime checks.
         */
        return monitoringRuleService.getActiveRules();
    }

    @PutMapping("/{id}")
    public String updateRule(@PathVariable Long id, @RequestBody MonitoringRule rule) {
        /*
         * Update one rule by id.
         * Path id is forced into request object to avoid mismatch.
         */
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
        /*
         * Delete one rule by id.
         */
        int result = monitoringRuleService.deleteRule(id);
        if (result == 1) {
            return "delete rule success";
        } else {
            return "delete rule false";
        }
    }
}