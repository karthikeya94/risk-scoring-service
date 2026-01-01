package com.risk.scoring.service;

import com.riskplatform.common.entity.RiskRule;
import com.risk.scoring.model.dto.RiskRuleRequest;
import com.risk.scoring.model.dto.RiskRuleResponse;

import java.util.List;
import java.util.Optional;

public interface RiskRuleService {
    List<RiskRule> getActiveRules();

    Optional<RiskRule> getRuleById(String ruleId);

    RiskRuleResponse saveRule(RiskRuleRequest request);

    boolean deleteRule(String ruleId);

    RiskRuleResponse activateRule(String ruleId);

    RiskRuleResponse deactivateRule(String ruleId);

    Optional<RiskRule> getRuleByName(String ruleName);
}