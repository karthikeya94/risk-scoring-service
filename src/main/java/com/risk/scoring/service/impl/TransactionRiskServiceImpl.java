package com.risk.scoring.service.impl;

import com.risk.scoring.model.CustomerProfileData;
import com.risk.scoring.model.TransactionData;
import com.risk.scoring.model.dto.RiskCalculationRequest;
import com.risk.scoring.service.RiskFactorService;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class TransactionRiskServiceImpl implements RiskFactorService {

    private static final double TRANSACTION_RISK_WEIGHT = 0.30;

    @Override
    public int calculateRiskFactor(RiskCalculationRequest request) {
        CustomerProfileData customerProfile = request.getCustomerProfile();

        if (customerProfile == null) {
            return 0;
        }

        int score = 0;

        double amount = request.getAmount().doubleValue();
        double avgTransactionAmount = customerProfile.getAvgTransactionAmount().doubleValue();
        double dailyLimit = customerProfile.getDailyLimit().doubleValue();

        if (amount > avgTransactionAmount * 3) {
            score = 30;
        } else if (amount > dailyLimit * 0.8) {
            score = 20;
        } else if (amount > avgTransactionAmount) {
            score = 10;
        } else {
            score = 0;
        }

        java.time.Instant transactionTime = request.getTimestamp();
        java.time.ZoneId zoneId = java.time.ZoneId.systemDefault();
        java.time.LocalTime localTime = transactionTime.atZone(zoneId).toLocalTime();

        // Add bonus points for unusual timing (outside 6 AM - 10 PM)
        if (localTime.isBefore(java.time.LocalTime.of(6, 0)) || localTime.isAfter(java.time.LocalTime.of(22, 0))) {
            score += 5;
        }

        // Add bonus points for high-risk channels
        // For now, we'll check if the request has a channel field that indicates high risk
        // In a real implementation, this would come from the transaction data
        if (isHighRiskChannel(request)) {
            score += 8;
        }

        return Math.min(score, 38);
    }

    private boolean isHighRiskChannel(RiskCalculationRequest request) {
        // Placeholder implementation - in a real system this would check the actual channel
        // For example, mobile transactions from new locations might be considered high risk
        return request.getLocation() != null &&
               request.getCustomerProfile() != null &&
               request.getCustomerProfile().getLastVerifiedLocation() != null &&
               !request.getLocation().getCountry().equals(
                   request.getCustomerProfile().getLastVerifiedLocation().getCountry());
    }

    @Override
    public double getWeight() {
        return TRANSACTION_RISK_WEIGHT;
    }
}