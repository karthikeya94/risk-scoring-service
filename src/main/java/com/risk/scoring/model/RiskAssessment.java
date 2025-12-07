package com.risk.scoring.model;

import com.risk.scoring.model.enums.RiskLevel;
import com.risk.scoring.model.enums.Decision;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskAssessment {
    private String transactionId;
    private int riskScore; // 0-100
    private RiskLevel riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
    private Decision decision; // APPROVE, APPROVE_WITH_MONITORING, MANUAL_REVIEW, REJECT_BLOCK
    private DecisionDetails decisionDetails;
    private RiskFactors riskFactors;
    private List<RiskFlag> riskFlags;
    private Instant timestamp;
    private TransactionData transactionData; // Add this field to store transaction data
}