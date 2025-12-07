package com.risk.scoring.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskFactors {
    private int transactionRisk;
    private int behaviorRisk;
    private int velocityRisk;
    private int geographicRisk;
    private int merchantRisk;
}