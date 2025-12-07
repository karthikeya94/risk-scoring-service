package com.risk.scoring.model.dto;

import com.risk.scoring.model.enums.RiskLevel;
import com.risk.scoring.model.ScoreHistoryEntry;
import com.risk.scoring.model.MonthlyStats;
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