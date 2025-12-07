package com.risk.scoring.service;

import com.risk.scoring.model.CustomerProfileData;
import com.risk.scoring.model.CustomerRiskProfile;
import com.risk.scoring.model.VelocityData;
import com.risk.scoring.model.dto.CustomerRiskProfileResponse;
import com.risk.scoring.model.dto.CustomerProfileSummary;

import java.util.List;
import java.util.Optional;

public interface CustomerRiskProfileService {
    Optional<CustomerRiskProfile> getCustomerRiskProfile(String customerId);
    boolean updateCustomerRiskProfile(String customerId, CustomerRiskProfile updatedProfile);
    CustomerRiskProfile saveCustomerRiskProfile(CustomerRiskProfile profile);
    CustomerRiskProfileResponse getCustomerRiskProfileResponse(String customerId);
    List<CustomerProfileSummary> getRiskTrend(String customerId, int days);
    boolean isSignificantChange(CustomerRiskProfile currentProfile, CustomerRiskProfile newProfile);
    
    // Methods for fetching customer data and velocity data
    CustomerProfileData getCustomerProfileData(String customerId);
    VelocityData getVelocityData(String customerId);
}