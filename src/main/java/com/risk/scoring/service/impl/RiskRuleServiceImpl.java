package com.risk.scoring.service.impl;

import com.risk.scoring.model.RiskRule;
import com.risk.scoring.model.dto.RiskRuleRequest;
import com.risk.scoring.model.dto.RiskRuleResponse;
import com.risk.scoring.repository.RiskRuleRepository;
import com.risk.scoring.service.RiskRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class RiskRuleServiceImpl implements RiskRuleService {

    @Autowired
    private RiskRuleRepository riskRuleRepository;

    @Override
    public List<RiskRule> getActiveRules() {
        return riskRuleRepository.findByEnabledIsTrue();
    }

    @Override
    public Optional<RiskRule> getRuleById(String ruleId) {
        return riskRuleRepository.findById(ruleId);
    }

    @Override
    public Optional<RiskRule> getRuleByName(String ruleName) {
        return riskRuleRepository.findByRuleName(ruleName);
    }

    @Override
    public RiskRuleResponse saveRule(RiskRuleRequest request) {
        RiskRule rule;
        boolean isNew = (request.getRuleId() == null || request.getRuleId().isEmpty());

        if (isNew) {
            // Create new rule
            rule = new RiskRule();
            rule.setId("RULE-" + UUID.randomUUID().toString().toUpperCase().replace("-", "").substring(0, 8));
            rule.setVersion(1);
            rule.setCreatedAt(Instant.now());
        } else {
            // Update existing rule
            Optional<RiskRule> existingRuleOpt = riskRuleRepository.findById(request.getRuleId());
            if (existingRuleOpt.isEmpty()) {
                throw new IllegalArgumentException("Rule not found with ID: " + request.getRuleId());
            }
            rule = existingRuleOpt.get();
            rule.setVersion(rule.getVersion() + 1);
        }

        // Update rule properties
        rule.setRuleName(request.getRuleName());
        rule.setRuleType(request.getRuleType());
        rule.setParameters(request.getParameters());
        rule.setEnabled(request.isEnabled());
        rule.setEffectiveDate(request.getEffectiveDate());
        rule.setUpdatedAt(Instant.now());

        // Save rule
        rule = riskRuleRepository.save(rule);

        // Create response
        RiskRuleResponse response = new RiskRuleResponse();
        response.setRuleId(rule.getId());
        response.setStatus(rule.isEnabled() ? "ACTIVE" : "INACTIVE");
        response.setVersion(rule.getVersion());
        response.setMessage(
                isNew ? "Risk scoring rule created successfully" : "Risk scoring rule updated successfully");

        return response;
    }

    @Override
    public boolean deleteRule(String ruleId) {
        Optional<RiskRule> ruleOpt = riskRuleRepository.findById(ruleId);
        if (ruleOpt.isPresent()) {
            riskRuleRepository.deleteById(ruleId);
            return true;
        }
        return false;
    }

    @Override
    public RiskRuleResponse activateRule(String ruleId) {
        Optional<RiskRule> ruleOpt = riskRuleRepository.findById(ruleId);
        if (ruleOpt.isEmpty()) {
            throw new IllegalArgumentException("Rule not found with ID: " + ruleId);
        }

        RiskRule rule = ruleOpt.get();
        rule.setEnabled(true);
        rule.setUpdatedAt(Instant.now());
        rule.setVersion(rule.getVersion() + 1);

        rule = riskRuleRepository.save(rule);
        
        RiskRuleResponse response = new RiskRuleResponse();
        response.setRuleId(rule.getId());
        response.setStatus("ACTIVE");
        response.setVersion(rule.getVersion());
        response.setMessage("Risk scoring rule activated successfully");

        return response;
    }

    @Override
    public RiskRuleResponse deactivateRule(String ruleId) {
        Optional<RiskRule> ruleOpt = riskRuleRepository.findById(ruleId);
        if (ruleOpt.isEmpty()) {
            throw new IllegalArgumentException("Rule not found with ID: " + ruleId);
        }

        RiskRule rule = ruleOpt.get();
        rule.setEnabled(false);
        rule.setUpdatedAt(Instant.now());
        rule.setVersion(rule.getVersion() + 1);

        rule = riskRuleRepository.save(rule);
        
        RiskRuleResponse response = new RiskRuleResponse();
        response.setRuleId(rule.getId());
        response.setStatus("INACTIVE");
        response.setVersion(rule.getVersion());
        response.setMessage("Risk scoring rule deactivated successfully");

        return response;
    }
}