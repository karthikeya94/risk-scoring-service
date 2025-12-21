package com.risk.scoring.service.impl;

import com.risk.scoring.model.*;
import com.riskplatform.common.enums.RiskLevel;
import com.riskplatform.common.entity.RiskAssessment;
import com.riskplatform.common.entity.MonthlyStats;
import com.riskplatform.common.entity.RiskFactorStatus;
import com.riskplatform.common.entity.ScoreHistoryEntry;
import com.risk.scoring.model.dto.CustomerRiskProfileResponse;
import com.risk.scoring.model.dto.CustomerProfileSummary;
import com.risk.scoring.repository.CustomerRiskProfileRepository;
import com.risk.scoring.service.CustomerRiskProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
public class CustomerRiskProfileServiceImpl implements CustomerRiskProfileService {

    @Autowired
    private CustomerRiskProfileRepository customerRiskProfileRepository;

    // Debounce threshold - profile won't be updated if score difference is less
    // than this
    private static final int DEBOUNCE_THRESHOLD = 5;

    // Significant change threshold - profile will be updated if score difference is
    // greater than this
    private static final int SIGNIFICANT_CHANGE_THRESHOLD = 10;

    @Override
    public Optional<CustomerRiskProfile> getCustomerRiskProfile(String customerId) {
        return customerRiskProfileRepository.findByCustomerId(customerId);
    }

    @Override
    public boolean updateCustomerRiskProfile(String customerId, CustomerRiskProfile updatedProfile) {
        return customerRiskProfileRepository.updateWithDebounce(customerId, updatedProfile, DEBOUNCE_THRESHOLD);
    }

    @Override
    public CustomerRiskProfile saveCustomerRiskProfile(CustomerRiskProfile profile) {
        return customerRiskProfileRepository.save(profile);
    }

    @Override
    public CustomerRiskProfileResponse getCustomerRiskProfileResponse(String customerId) {
        Optional<CustomerRiskProfile> profileOpt = getCustomerRiskProfile(customerId);

        if (profileOpt.isEmpty()) {
            return null;
        }

        CustomerRiskProfile profile = profileOpt.get();
        CustomerRiskProfileResponse response = new CustomerRiskProfileResponse();
        response.setCustomerId(customerId);
        response.setCurrentRiskScore(profile.getCurrentRiskScore());
        response.setRiskLevel(profile.getRiskLevel());
        response.setLastUpdated(profile.getLastUpdated());

        if (profile.getScoreHistory() != null) {
            response.setScoreHistory(profile.getScoreHistory());
        }

        // Set monthly stats
        response.setMonthlyStats(profile.getMonthlyStats());

        // Set risk profile data
        if (profile.getCustomerProfile() != null) {
            CustomerProfileSummary riskProfile = new CustomerProfileSummary();
            riskProfile.setCustomerSegment("STANDARD"); // Default value
            if (profile.getCustomerProfile().getKycStatus() != null) {
                riskProfile.setKycStatus(profile.getCustomerProfile().getKycStatus());
            }
            riskProfile.setFraudHistory(profile.getCustomerProfile().isFraudHistory());
            riskProfile.setDormant(profile.getCustomerProfile()
                    .getAccountStatus() == com.risk.scoring.model.enums.AccountStatus.DORMANT);
            response.setRiskProfile(riskProfile);
        }

        return response;
    }

    @Override
    public List<CustomerProfileSummary> getRiskTrend(String customerId, int days) {
        // Implementation would go here
        return List.of(); // Placeholder implementation
    }

    @Override
    public boolean isSignificantChange(CustomerRiskProfile currentProfile, CustomerRiskProfile newProfile) {
        if (currentProfile == null || newProfile == null) {
            return true;
        }

        int scoreDifference = Math.abs(currentProfile.getCurrentRiskScore() - newProfile.getCurrentRiskScore());
        boolean riskLevelChanged = currentProfile.getRiskLevel() != newProfile.getRiskLevel();

        return scoreDifference > SIGNIFICANT_CHANGE_THRESHOLD || riskLevelChanged;
    }

    @Override
    public CustomerProfileData getCustomerProfileData(String customerId) {
        Optional<CustomerRiskProfile> profileOpt = getCustomerRiskProfile(customerId);
        return profileOpt.map(CustomerRiskProfile::getCustomerProfile).orElse(null);
    }

    @Override
    public VelocityData getVelocityData(String customerId) {
        Optional<CustomerRiskProfile> profileOpt = getCustomerRiskProfile(customerId);
        // In a real implementation, velocity data might be stored separately or
        // calculated
        // For now, we'll extract what we can from the existing profile or return null
        if (profileOpt.isPresent()) {
            CustomerRiskProfile profile = profileOpt.get();
            // This is a placeholder - in a real implementation, we'd fetch from Redis or
            // calculate
            return new VelocityData(0, 0);
        }
        return null;
    }

    public CustomerRiskProfile createCustomerRiskProfileFromAssessment(RiskAssessment assessment,
            CustomerProfileData customerProfileData) {
        CustomerRiskProfile profile = new CustomerRiskProfile();
        profile.setCurrentRiskScore(assessment.getRiskScore());
        profile.setPreviousRiskScore(0); // Will be set when updating existing profile
        profile.setRiskLevel(assessment.getRiskLevel());
        profile.setLastUpdated(assessment.getTimestamp());
        profile.setVersion(1);

        // Initialize score history
        ScoreHistoryEntry historyEntry = new ScoreHistoryEntry();
        historyEntry.setDate(assessment.getTimestamp());
        historyEntry.setScore(assessment.getRiskScore());
        historyEntry.setLevel(assessment.getRiskLevel());
        profile.setScoreHistory(List.of(historyEntry));

        // Initialize monthly stats
        MonthlyStats monthlyStats = new MonthlyStats();
        monthlyStats.setTransactionCount(1);
        monthlyStats.setAverageRiskScore(assessment.getRiskScore());
        monthlyStats.setHighRiskTransactions(assessment.getRiskLevel() == RiskLevel.HIGH ||
                assessment.getRiskLevel() == RiskLevel.CRITICAL ? 1 : 0);
        // Flagged transactions
        int flaggedCount = 0;
        if (assessment.getRiskFlags() != null) {
            flaggedCount = assessment.getRiskFlags().size();
        }
        monthlyStats.setFlaggedTransactions(flaggedCount);
        profile.setMonthlyStats(monthlyStats);

        // Set risk factors status
        RiskFactorStatus riskFactors = new RiskFactorStatus();
        riskFactors.setCustomerAge(com.risk.scoring.model.enums.CustomerAgeStatus.NORMAL.name());
        if (customerProfileData != null) {
            riskFactors.setFraudHistory(customerProfileData.isFraudHistory());
        }
        riskFactors.setVelocityStatus(com.risk.scoring.model.enums.VelocityStatus.NORMAL.name());
        riskFactors.setGeographicStatus(com.risk.scoring.model.enums.GeographicStatus.NORMAL.name());
        riskFactors.setMerchantStatus(com.risk.scoring.model.enums.MerchantStatus.NORMAL.name());
        profile.setRiskFactors(riskFactors);

        // Set customer profile data
        profile.setCustomerProfile(customerProfileData);

        return profile;
    }

    /**
     * Update existing customer risk profile with new assessment
     */
    public CustomerRiskProfile updateCustomerRiskProfileFromAssessment(CustomerRiskProfile existingProfile,
            RiskAssessment assessment) {
        // Set previous score before updating
        existingProfile.setPreviousRiskScore(existingProfile.getCurrentRiskScore());

        // Update basic fields
        existingProfile.setCurrentRiskScore(assessment.getRiskScore());
        existingProfile.setRiskLevel(assessment.getRiskLevel());
        existingProfile.setLastUpdated(assessment.getTimestamp());
        existingProfile.setUpdatedBy("RiskScoringEngine");
        existingProfile.setVersion(existingProfile.getVersion() + 1);

        // Update score history
        ScoreHistoryEntry historyEntry = new ScoreHistoryEntry();
        historyEntry.setDate(assessment.getTimestamp());
        historyEntry.setScore(assessment.getRiskScore());
        historyEntry.setLevel(assessment.getRiskLevel());

        // Add to history (keep last 30 entries)
        if (existingProfile.getScoreHistory() != null) {
            // Need a mutable list
            List<ScoreHistoryEntry> history = new ArrayList<>(existingProfile.getScoreHistory());
            history.add(0, historyEntry);
            // Keep only last 30 entries
            if (history.size() > 30) {
                history = history.subList(0, 30);
            }
            existingProfile.setScoreHistory(history);
        } else {
            existingProfile.setScoreHistory(List.of(historyEntry));
        }

        // Update monthly stats
        updateMonthlyStats(existingProfile, assessment);

        // Update risk factors status based on flags
        updateRiskFactorsStatus(existingProfile, assessment);

        return existingProfile;
    }

    private void updateMonthlyStats(CustomerRiskProfile profile, RiskAssessment assessment) {
        MonthlyStats monthlyStats = profile.getMonthlyStats();
        if (monthlyStats == null) {
            monthlyStats = new MonthlyStats();
            profile.setMonthlyStats(monthlyStats);
        }

        // Update counters
        // Check for nulls as common Integer can be null
        int transactionCount = monthlyStats.getTransactionCount() != null ? monthlyStats.getTransactionCount() : 0;
        monthlyStats.setTransactionCount(transactionCount + 1);

        // Recalculate average risk score
        int avgScore = monthlyStats.getAverageRiskScore() != null ? monthlyStats.getAverageRiskScore() : 0;
        int currentTransactionCount = transactionCount + 1; // Since we incremented logic but not stored yet if we
                                                            // reused variable

        // Wait, logic above used 'transactionCount + 1' for setTransactionCount.
        // Formula: (oldAvg * oldCount + newScore) / newCount
        int totalScore = avgScore * transactionCount + assessment.getRiskScore();
        monthlyStats.setAverageRiskScore(totalScore / currentTransactionCount);

        // Update high risk transactions counter
        int highRiskCount = monthlyStats.getHighRiskTransactions() != null ? monthlyStats.getHighRiskTransactions() : 0;
        if (assessment.getRiskLevel() == RiskLevel.HIGH || assessment.getRiskLevel() == RiskLevel.CRITICAL) {
            monthlyStats.setHighRiskTransactions(highRiskCount + 1);
        } else {
            monthlyStats.setHighRiskTransactions(highRiskCount);
        }

        // Update flagged transactions counter
        int flaggedCount = monthlyStats.getFlaggedTransactions() != null ? monthlyStats.getFlaggedTransactions() : 0;
        int newFlags = 0;
        if (assessment.getRiskFlags() != null) {
            newFlags = assessment.getRiskFlags().size();
        }
        monthlyStats.setFlaggedTransactions(flaggedCount + newFlags);
    }

    private void updateRiskFactorsStatus(CustomerRiskProfile profile, RiskAssessment assessment) {
        RiskFactorStatus riskFactors = profile.getRiskFactors();
        if (riskFactors == null) {
            riskFactors = new RiskFactorStatus();
            profile.setRiskFactors(riskFactors);
        }

        // Update based on risk factors
        if (assessment.getRiskFactors().getGeographicRisk() != null
                && assessment.getRiskFactors().getGeographicRisk() > 10) {
            riskFactors.setGeographicStatus(com.risk.scoring.model.enums.GeographicStatus.ANOMALY_DETECTED.name());
        }

        if (assessment.getRiskFactors().getVelocityRisk() != null
                && assessment.getRiskFactors().getVelocityRisk() > 15) {
            riskFactors.setVelocityStatus(com.risk.scoring.model.enums.VelocityStatus.HIGH_VELOCITY.name());
        }
    }
}