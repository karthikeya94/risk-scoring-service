package com.risk.scoring.model.dto;

import com.riskplatform.common.enums.RiskLevel;
import com.riskplatform.common.entity.ScoreHistoryEntry;
import com.riskplatform.common.entity.MonthlyStats;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRiskProfileResponse {
    private String customerId;
    private int currentRiskScore;
    private RiskLevel riskLevel;
    private Instant lastUpdated;
    private List<ScoreHistoryEntry> scoreHistory;
    private MonthlyStats monthlyStats;
    private CustomerProfileSummary riskProfile;
}