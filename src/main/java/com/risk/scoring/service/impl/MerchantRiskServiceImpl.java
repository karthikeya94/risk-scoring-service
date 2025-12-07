package com.risk.scoring.service.impl;

import com.risk.scoring.model.dto.RiskCalculationRequest;
import com.risk.scoring.service.RiskFactorService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@Service
public class MerchantRiskServiceImpl implements RiskFactorService {

    private static final double MERCHANT_RISK_WEIGHT = 0.10;

    private static final Set<String> HIGH_RISK_CATEGORIES = Set.of("Drugs", "Weapons", "Gambling", "Dark_Web");

    private static final Set<String> MEDIUM_RISK_CATEGORIES = Set.of("Cash_Advance", "Wire_Transfer", "Crypto");

    @Override
    public int calculateRiskFactor(RiskCalculationRequest request) {
        String merchant = request.getMerchant();

        if (merchant == null || merchant.isEmpty()) {
            return 0;
        }

        int score = 0;

        // Check for high-risk merchant categories
        if (HIGH_RISK_CATEGORIES.stream().anyMatch(merchant::contains)) {
            score = 10;
        }
        // Check for medium-risk merchant categories
        else if (MEDIUM_RISK_CATEGORIES.stream().anyMatch(merchant::contains)) {
            score = 6;
        }
        // Check for merchant chargeback rate (placeholder - would come from merchant data in real implementation)
        else if (hasHighChargebackRate(merchant)) {
            score = 4;
        }
        // Check if merchant is new (registered < 30 days)
        else if (isNewMerchant(merchant)) {
            score = 3;
        }
        else {
            score = 0;
        }

        return score;
    }

    /**
     * Placeholder method to check if merchant has high chargeback rate
     * In a real implementation, this would check actual merchant data
     */
    private boolean hasHighChargebackRate(String merchant) {
        // Placeholder implementation
        return false;
    }

    /**
     * Placeholder method to check if merchant is new
     * In a real implementation, this would check merchant registration date
     */
    private boolean isNewMerchant(String merchant) {
        // Placeholder implementation - randomly return true for some merchants
        return merchant.hashCode() % 10 == 0; // 10% chance of being new
    }

    @Override
    public double getWeight() {
        return MERCHANT_RISK_WEIGHT;
    }
}