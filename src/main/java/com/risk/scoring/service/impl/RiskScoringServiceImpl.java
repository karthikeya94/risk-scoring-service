package com.risk.scoring.service.impl;

import com.riskplatform.common.entity.RiskAssessment;
import com.riskplatform.common.entity.RiskFactors;
import com.riskplatform.common.entity.RiskFlag;
import com.riskplatform.common.entity.DecisionDetails;
import com.riskplatform.common.enums.Decision;
import com.riskplatform.common.enums.RiskLevel;
import com.riskplatform.common.enums.Severity;
import com.risk.scoring.model.dto.RiskCalculationRequest;
import com.risk.scoring.model.dto.RiskCalculationResponse;
import com.risk.scoring.service.RiskFactorService;
import com.risk.scoring.service.RiskScoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class RiskScoringServiceImpl implements RiskScoringService {

    @Autowired
    @Qualifier("transactionRiskServiceImpl")
    private RiskFactorService transactionRiskService;

    @Autowired
    @Qualifier("customerBehaviorRiskServiceImpl")
    private RiskFactorService behaviorRiskService;

    @Autowired
    @Qualifier("velocityRiskServiceImpl")
    private RiskFactorService velocityRiskService;

    @Autowired
    @Qualifier("geographicRiskServiceImpl")
    private RiskFactorService geographicRiskService;

    @Autowired
    @Qualifier("merchantRiskServiceImpl")
    private RiskFactorService merchantRiskService;

    @Override
    public RiskCalculationResponse calculateRiskScore(RiskCalculationRequest request) {
        RiskFactors factors = calculateRiskFactors(request);

        int riskScore = calculateOverallRiskScore(factors);

        RiskLevel riskLevel = determineRiskLevel(riskScore);
        Decision decision = determineDecision(riskLevel);

        RiskAssessment assessment = generateRiskAssessment(request, factors);
        assessment.setRiskScore(riskScore);
        assessment.setRiskLevel(riskLevel);
        assessment.setDecision(decision);

        double approvalConfidence = calculateApprovalConfidence(riskScore, riskLevel);
        if (assessment.getDecisionDetails() != null) {
            assessment.getDecisionDetails().setApprovalConfidence(approvalConfidence);
        }

        RiskCalculationResponse response = new RiskCalculationResponse();
        response.setRiskAssessment(assessment);

        return response;
    }

    @Override
    public RiskFactors calculateRiskFactors(RiskCalculationRequest request) {
        RiskFactors factors = new RiskFactors();
        factors.setTransactionRisk(transactionRiskService.calculateRiskFactor(request));
        factors.setBehaviorRisk(behaviorRiskService.calculateRiskFactor(request));
        factors.setVelocityRisk(velocityRiskService.calculateRiskFactor(request));
        factors.setGeographicRisk(geographicRiskService.calculateRiskFactor(request));
        factors.setMerchantRisk(merchantRiskService.calculateRiskFactor(request));
        return factors;
    }

    private int calculateOverallRiskScore(RiskFactors factors) {
        double score = 0.0;
        score += (factors.getTransactionRisk() != null ? factors.getTransactionRisk() : 0)
                * transactionRiskService.getWeight();
        score += (factors.getBehaviorRisk() != null ? factors.getBehaviorRisk() : 0) * behaviorRiskService.getWeight();
        score += (factors.getVelocityRisk() != null ? factors.getVelocityRisk() : 0) * velocityRiskService.getWeight();
        score += (factors.getGeographicRisk() != null ? factors.getGeographicRisk() : 0)
                * geographicRiskService.getWeight();
        score += (factors.getMerchantRisk() != null ? factors.getMerchantRisk() : 0) * merchantRiskService.getWeight();

        return (int) Math.round(score);
    }

    @Override
    public RiskLevel determineRiskLevel(int score) {
        if (score >= 0 && score <= 20) {
            return RiskLevel.LOW;
        } else if (score <= 50) {
            return RiskLevel.MEDIUM;
        } else if (score <= 75) {
            return RiskLevel.HIGH;
        } else {
            return RiskLevel.CRITICAL;
        }
    }

    @Override
    public Decision determineDecision(RiskLevel riskLevel) {
        switch (riskLevel) {
            case LOW:
                return Decision.ALLOW;
            case MEDIUM:
                return Decision.MONITOR;
            case HIGH:
                return Decision.MANUAL_REVIEW;
            case CRITICAL:
                return Decision.BLOCK;
            default:
                return Decision.BLOCK;
        }
    }

    @Override
    public RiskAssessment generateRiskAssessment(RiskCalculationRequest request, RiskFactors factors) {
        RiskAssessment assessment = new RiskAssessment();
        assessment.setTransactionId(request.getTransactionId());
        assessment.setRiskFactors(factors);
        assessment.setTimestamp(Instant.now());

        DecisionDetails decisionDetails = new DecisionDetails();
        decisionDetails.setManualReviewRequired(false);
        assessment.setDecisionDetails(decisionDetails);

        List<RiskFlag> riskFlags = generateRiskFlags(request, factors);
        assessment.setRiskFlags(riskFlags);

        return assessment;
    }

    private List<RiskFlag> generateRiskFlags(RiskCalculationRequest request, RiskFactors factors) {
        List<RiskFlag> flags = new ArrayList<>();

        // Geographic anomaly flag
        if (factors.getGeographicRisk() != null && factors.getGeographicRisk() > 10) {
            RiskFlag flag = new RiskFlag();
            flag.setFlag("GEOGRAPHIC_ANOMALY");
            flag.setSeverity(Severity.MEDIUM);
            flag.setDescription("Transaction from location 500+ km away");
            flags.add(flag);
        }

        // High velocity flag
        if (factors.getVelocityRisk() != null && factors.getVelocityRisk() > 10) {
            RiskFlag flag = new RiskFlag();
            flag.setFlag("HIGH_VELOCITY");
            flag.setSeverity(Severity.MEDIUM);
            flag.setDescription("Unusually high transaction frequency");
            flags.add(flag);
        }

        // High amount flag
        if (factors.getTransactionRisk() != null && factors.getTransactionRisk() > 20) {
            RiskFlag flag = new RiskFlag();
            flag.setFlag("HIGH_AMOUNT");
            flag.setSeverity(Severity.HIGH);
            flag.setDescription("Transaction amount significantly higher than usual");
            flags.add(flag);
        }

        // Add more flags based on specific risk factor values
        if (factors.getBehaviorRisk() != null && factors.getBehaviorRisk() > 20) {
            RiskFlag flag = new RiskFlag();
            flag.setFlag("CUSTOMER_BEHAVIOR_RISK");
            flag.setSeverity(Severity.HIGH);
            flag.setDescription("High risk based on customer behavior patterns");
            flags.add(flag);
        }

        if (factors.getMerchantRisk() != null && factors.getMerchantRisk() > 5) {
            RiskFlag flag = new RiskFlag();
            flag.setFlag("MERCHANT_RISK");
            flag.setSeverity(Severity.MEDIUM);
            flag.setDescription("Transaction with high-risk merchant");
            flags.add(flag);
        }

        return flags;
    }

    @Autowired
    private com.risk.scoring.repository.CustomerRiskProfileRepository customerRiskProfileRepository;

    @Autowired
    private com.risk.scoring.repository.RiskRuleRepository riskRuleRepository;

    @Autowired
    private com.risk.scoring.repository.AnomalyRepository anomalyRepository;

    @Override
    public com.risk.scoring.model.dto.CustomerRiskProfileResponse getCustomerRiskProfile(String customerId) {
        com.riskplatform.common.entity.CustomerRiskProfile profile = customerRiskProfileRepository
                .findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Customer risk profile not found"));

        com.risk.scoring.model.dto.CustomerRiskProfileResponse response = new com.risk.scoring.model.dto.CustomerRiskProfileResponse();
        response.setCustomerId(profile.getCustomerId());
        response.setCurrentRiskScore(profile.getCurrentRiskScore());
        response.setRiskLevel(profile.getRiskLevel());
        response.setLastUpdated(profile.getLastUpdated());
        response.setScoreHistory(profile.getScoreHistory());
        response.setMonthlyStats(profile.getMonthlyStats());

        // Common entity doesn't store profile summary
        com.risk.scoring.model.dto.CustomerProfileSummary summary = new com.risk.scoring.model.dto.CustomerProfileSummary();
        summary.setCustomerSegment("STANDARD");
        summary.setFraudHistory(Boolean.TRUE
                .equals(profile.getRiskFactorStatus() != null && profile.getRiskFactorStatus().getFraudHistory()));
        response.setRiskProfile(summary);

        return response;
    }

    @Override
    public com.risk.scoring.model.dto.RiskRuleResponse createOrUpdateRule(
            com.risk.scoring.model.dto.RiskRuleRequest request) {
        com.riskplatform.common.entity.RiskRule rule = new com.riskplatform.common.entity.RiskRule();
        if (request.getRuleId() != null) {
            rule = riskRuleRepository.findById(request.getRuleId())
                    .orElse(new com.riskplatform.common.entity.RiskRule());
        }

        rule.setRuleName(request.getRuleName());
        try {
            rule.setRuleType(com.riskplatform.common.enums.RuleType.valueOf(request.getRuleType()));
        } catch (Exception e) {
            rule.setRuleType(com.riskplatform.common.enums.RuleType.VELOCITY_CHECK);
        }
        rule.setParameters(request.getParameters());
        rule.setEnabled(request.isEnabled());
        rule.setEffectiveDate(request.getEffectiveDate());

        com.riskplatform.common.entity.RiskRule savedRule = riskRuleRepository.save(rule);

        com.risk.scoring.model.dto.RiskRuleResponse response = new com.risk.scoring.model.dto.RiskRuleResponse();
        response.setRuleId(savedRule.getRuleId());
        response.setStatus(Boolean.TRUE.equals(savedRule.getEnabled()) ? "ACTIVE" : "INACTIVE");
        response.setVersion(savedRule.getVersion() != null ? savedRule.getVersion().intValue() : 1);
        response.setMessage("Risk scoring rule updated successfully");

        return response;
    }

    @Override
    public com.risk.scoring.model.dto.AnomaliesResponse getAnomalies(String timeWindow) {
        java.time.Instant startTime = java.time.Instant.now().minus(1, java.time.temporal.ChronoUnit.HOURS);
        if ("last_24_hours".equals(timeWindow)) {
            startTime = java.time.Instant.now().minus(24, java.time.temporal.ChronoUnit.HOURS);
        }

        List<com.riskplatform.common.entity.Anomaly> anomalies = anomalyRepository.findByDetectedAtAfter(startTime);

        com.risk.scoring.model.dto.AnomaliesResponse response = new com.risk.scoring.model.dto.AnomaliesResponse();
        response.setAnomalies(anomalies);
        response.setTotalAnomalies(anomalies.size());
        response.setTimeWindow(timeWindow != null ? timeWindow : "last_hour");

        return response;
    }

    private double calculateApprovalConfidence(int riskScore, RiskLevel riskLevel) {
        switch (riskLevel) {
            case LOW:
                return 1.0 - (riskScore / 200.0);
            case MEDIUM:
                return 0.9 - ((riskScore - 20) / 300.0);
            case HIGH:
                return 0.7 - ((riskScore - 50) / 500.0);
            case CRITICAL:
                return 0.4 - ((riskScore - 75) / 625.0);
            default:
                return 0.0;
        }
    }
}