package com.risk.scoring.service.impl;

import com.risk.scoring.model.VelocityData;
import com.risk.scoring.model.dto.RiskCalculationRequest;
import com.risk.scoring.service.RiskFactorService;
import org.springframework.stereotype.Service;

@Service
public class VelocityRiskServiceImpl implements RiskFactorService {

    private static final double VELOCITY_RISK_WEIGHT = 0.20;

    private static final int TXN_COUNT_1HOUR_THRESHOLD = 20;
    private static final int TXN_COUNT_1DAY_THRESHOLD_HIGH = 100;
    private static final int TXN_COUNT_1DAY_THRESHOLD_MEDIUM = 50;

    @Override
    public int calculateRiskFactor(RiskCalculationRequest request) {
        VelocityData velocityData = request.getVelocityData();

        if (velocityData == null) {
            return 0;
        }

        int score = 0;
        int txnCount1Hour = velocityData.getTransactionsInLastHour();
        int txnCount1Day = velocityData.getTransactionsInLastDay();

        if (txnCount1Hour > TXN_COUNT_1HOUR_THRESHOLD) {
            score = 20;
        } else if (txnCount1Day > TXN_COUNT_1DAY_THRESHOLD_HIGH) {
            score = 15;
        } else if (txnCount1Day > TXN_COUNT_1DAY_THRESHOLD_MEDIUM) {
            score = 8;
        }

        return score;
    }

    @Override
    public double getWeight() {
        return VELOCITY_RISK_WEIGHT;
    }
}