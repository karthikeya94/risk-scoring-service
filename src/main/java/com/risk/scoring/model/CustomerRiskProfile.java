package com.risk.scoring.model;

import com.risk.scoring.model.enums.RiskLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "customerRiskProfiles")
public class CustomerRiskProfile {
    @Id
    private String id;
    private String customerId;
    private int currentRiskScore;
    private int previousRiskScore;
    private RiskLevel riskLevel;
    private Instant lastUpdated;
    private String updatedBy;
    private List<ScoreHistoryEntry> scoreHistory;
    private MonthlyStats monthlyStats;
    private RiskFactorStatus riskFactors;
    private CustomerProfileData customerProfile;
    private long version;
}