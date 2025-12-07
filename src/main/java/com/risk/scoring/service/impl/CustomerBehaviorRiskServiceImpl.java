package com.risk.scoring.service.impl;

import com.risk.scoring.model.CustomerProfileData;
import com.risk.scoring.model.dto.RiskCalculationRequest;
import com.risk.scoring.service.RiskFactorService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class CustomerBehaviorRiskServiceImpl implements RiskFactorService {

    private static final double BEHAVIOR_RISK_WEIGHT = 0.25;

    @Override
    public int calculateRiskFactor(RiskCalculationRequest request) {
        CustomerProfileData customerProfile = request.getCustomerProfile();

        if (customerProfile == null) {
            return 0;
        }

        int score = 0;

        // Calculate customer age in days
        long customerAgeDays = ChronoUnit.DAYS.between(
            customerProfile.getRegistrationDate(),
            Instant.now()
        );

        if (customerAgeDays < 30) {
            // New customer
            score = 25;
        } else if (customerProfile.isFraudHistory()) {
            score = 20;
        } else if (customerProfile.getFailedTransactionsLast7Days() > 3) {
            score = 15;
        } else if (customerProfile.getAccountStatus() == com.risk.scoring.model.enums.AccountStatus.DORMANT) {
            // DORMANT = no txn > 90 days
            score = 12;
        } else {
            score = 0;
        }

        return score;
    }

    @Override
    public double getWeight() {
        return BEHAVIOR_RISK_WEIGHT;
    }
}