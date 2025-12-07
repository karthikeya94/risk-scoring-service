package com.risk.scoring.model;

import com.risk.scoring.model.enums.RiskLevel;
import com.risk.scoring.model.enums.Decision;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventData {
    private String transactionId;
    private int previousScore;
    private int newScore;
    private RiskLevel riskLevel;
    private Map<String, Integer> factors; // transaction, behavior, velocity, geographic, merchant
    private Decision decision;
    private DecisionDetails decisionDetails;
}