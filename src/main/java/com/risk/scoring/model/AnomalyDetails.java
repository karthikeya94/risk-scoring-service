package com.risk.scoring.model;


import com.risk.scoring.model.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyDetails {
    private RiskFactors riskFactors;
    private int riskScore;
    private RiskLevel riskLevel;
}