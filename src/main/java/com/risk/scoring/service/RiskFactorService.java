package com.risk.scoring.service;

import com.risk.scoring.model.dto.RiskCalculationRequest;
public interface RiskFactorService {
    int calculateRiskFactor(RiskCalculationRequest request);
    double getWeight();
}