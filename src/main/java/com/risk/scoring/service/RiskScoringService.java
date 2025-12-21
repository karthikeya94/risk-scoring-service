package com.risk.scoring.service;

import com.riskplatform.common.entity.RiskAssessment;
import com.riskplatform.common.entity.RiskFactors;
import com.riskplatform.common.enums.Decision;
import com.riskplatform.common.enums.RiskLevel;
import com.risk.scoring.model.dto.RiskCalculationRequest;
import com.risk.scoring.model.dto.RiskCalculationResponse;

public interface RiskScoringService {
    RiskCalculationResponse calculateRiskScore(RiskCalculationRequest request);

    RiskFactors calculateRiskFactors(RiskCalculationRequest request);

    RiskLevel determineRiskLevel(int score);

    Decision determineDecision(RiskLevel riskLevel);

    RiskAssessment generateRiskAssessment(RiskCalculationRequest request, RiskFactors factors);

    com.risk.scoring.model.dto.CustomerRiskProfileResponse getCustomerRiskProfile(String customerId);

    com.risk.scoring.model.dto.RiskRuleResponse createOrUpdateRule(com.risk.scoring.model.dto.RiskRuleRequest request);

    com.risk.scoring.model.dto.AnomaliesResponse getAnomalies(String timeWindow);
}