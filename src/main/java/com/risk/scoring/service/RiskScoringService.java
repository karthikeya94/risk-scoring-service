package com.risk.scoring.service;

import com.risk.scoring.model.RiskAssessment;
import com.risk.scoring.model.RiskFactors;
import com.risk.scoring.model.enums.Decision;
import com.risk.scoring.model.enums.RiskLevel;
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