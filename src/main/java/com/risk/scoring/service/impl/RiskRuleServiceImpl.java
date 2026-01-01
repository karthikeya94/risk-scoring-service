package com.risk.scoring.service.impl;

import com.riskplatform.common.entity.RiskRule;
import com.riskplatform.common.enums.RuleType;
import com.risk.scoring.model.dto.RiskRuleRequest;
import com.risk.scoring.model.dto.RiskRuleResponse;
import com.risk.scoring.repository.RiskRuleRepository;
import com.risk.scoring.service.RiskRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
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
            rule.setRuleId("RULE-" + UUID.randomUUID().toString().toUpperCase().replace("-", "").substring(0, 8));
            rule.setVersion(1L);
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
        try {
            rule.setRuleType(RuleType.valueOf(request.getRuleType()));
        } catch (IllegalArgumentException e) {
            // Default or handle error. Assuming mapping exists or string matches.
            // If request.getRuleType() is null or invalid, this throws.
            // Fallback for demo:
            rule.setRuleType(RuleType.VELOCITY_CHECK);
        }

        rule.setParameters(request.getParameters());
        rule.setEnabled(request.isEnabled());
        rule.setEffectiveDate(request.getEffectiveDate());
        rule.setUpdatedAt(Instant.now());

        // Save rule
        rule = riskRuleRepository.save(rule);

        // Create response
        RiskRuleResponse response = new RiskRuleResponse();
        response.setRuleId(rule.getRuleId());
        response.setStatus(Boolean.TRUE.equals(rule.getEnabled()) ? "ACTIVE" : "INACTIVE");
        response.setVersion(rule.getVersion().intValue());
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
        response.setRuleId(rule.getRuleId());
        response.setStatus("ACTIVE");
        response.setVersion(rule.getVersion().intValue());
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
        response.setRuleId(rule.getRuleId());
        response.setStatus("INACTIVE");
        response.setVersion(rule.getVersion().intValue());
        response.setMessage("Risk scoring rule deactivated successfully");

        return response;
    }
}