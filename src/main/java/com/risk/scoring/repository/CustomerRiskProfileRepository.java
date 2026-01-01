package com.risk.scoring.repository;

import com.riskplatform.common.entity.CustomerRiskProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerRiskProfileRepository extends MongoRepository<CustomerRiskProfile, String> {

    Optional<CustomerRiskProfile> findByCustomerId(String customerId);

    // Custom method for debounced updates
    default boolean updateWithDebounce(String customerId, CustomerRiskProfile updatedProfile, int debounceThreshold) {
        Optional<CustomerRiskProfile> existingProfileOpt = findByCustomerId(customerId);
        if (existingProfileOpt.isPresent()) {
            CustomerRiskProfile existingProfile = existingProfileOpt.get();
            int scoreDifference = Math
                    .abs(existingProfile.getCurrentRiskScore() - updatedProfile.getCurrentRiskScore());
            // Only update if difference exceeds debounce threshold
            if (scoreDifference > debounceThreshold) {
                save(updatedProfile);
                return true;
            }
            return false;
        } else {
            save(updatedProfile);
            return true;
        }
    }
}