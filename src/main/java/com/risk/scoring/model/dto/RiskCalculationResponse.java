package com.risk.scoring.model.dto;

import com.riskplatform.common.entity.RiskAssessment;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskCalculationResponse {
    private RiskAssessment riskAssessment;
}