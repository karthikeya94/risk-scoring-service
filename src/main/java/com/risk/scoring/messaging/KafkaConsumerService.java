package com.risk.scoring.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.risk.scoring.model.CustomerProfileData;
import com.risk.scoring.model.VelocityData;
import com.risk.scoring.model.dto.RiskCalculationRequest;
import com.risk.scoring.service.CustomerRiskProfileService;
import com.risk.scoring.service.RiskScoringService;
import com.risk.scoring.service.EventStoreService;
import com.risk.scoring.service.AnomalyDetectionService;
import com.riskplatform.common.entity.RiskAssessment;
import com.risk.scoring.model.CustomerRiskProfile;
import com.riskplatform.common.entity.Anomaly;
import com.riskplatform.common.entity.EventStoreEntry;
import com.risk.scoring.service.impl.CustomerRiskProfileServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class KafkaConsumerService {

    @Autowired
    private RiskScoringService riskScoringService;

    @Autowired
    private CustomerRiskProfileService customerRiskProfileService;

    @Autowired
    private EventStoreService eventStoreService;

    @Autowired
    private AnomalyDetectionService anomalyDetectionService;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.transaction-validated}", groupId = "${kafka.group-id}")
    public void consumeTransactionValidatedEvent(String message) {
        try {
            log.info("RISK SCORING EVENT RECEIVED: {}", message);
            TransactionValidatedEvent event = objectMapper.readValue(message, TransactionValidatedEvent.class);
            log.info("RISK SCORING EVENT RECEIVED: {}", event);
            RiskCalculationRequest request = createRiskCalculationRequest(event);

            RiskAssessment assessment = riskScoringService.calculateRiskScore(request).getRiskAssessment();

            String customerId = event.getCustomerId();
            Optional<CustomerRiskProfile> profileOpt = customerRiskProfileService.getCustomerRiskProfile(customerId);
            CustomerRiskProfile profile;

            if (profileOpt.isPresent()) {
                profile = profileOpt.get();
                // Use the improved update method from CustomerRiskProfileServiceImpl
                profile = ((CustomerRiskProfileServiceImpl) customerRiskProfileService)
                        .updateCustomerRiskProfileFromAssessment(profile, assessment);
            } else {
                // Create new profile using the improved method from
                // CustomerRiskProfileServiceImpl
                CustomerProfileData customerProfileData = request.getCustomerProfile();
                profile = ((CustomerRiskProfileServiceImpl) customerRiskProfileService)
                        .createCustomerRiskProfileFromAssessment(assessment, customerProfileData);
                profile.setCustomerId(customerId);
            }

            // Check if we should update the profile using the service's significant change
            // method
            boolean shouldUpdate = false;
            if (profileOpt.isPresent()) {
                shouldUpdate = customerRiskProfileService.isSignificantChange(profileOpt.get(), profile);
            } else {
                // Always update for new profiles
                shouldUpdate = true;
            }

            if (shouldUpdate) {
                customerRiskProfileService.saveCustomerRiskProfile(profile);

                kafkaProducerService.sendRiskProfileUpdatedEvent(profile);
            }

            // Create event data for event store
            com.risk.scoring.model.EventData eventData = new com.risk.scoring.model.EventData();
            eventData.setTransactionId(assessment.getTransactionId());
            eventData.setPreviousScore(profileOpt.map(CustomerRiskProfile::getCurrentRiskScore).orElse(0));
            eventData.setNewScore(assessment.getRiskScore());
            eventData.setRiskLevel(assessment.getRiskLevel());

            // Create factors map
            java.util.Map<String, Integer> factors = new java.util.HashMap<>();
            factors.put("transaction", assessment.getRiskFactors().getTransactionRisk());
            factors.put("behavior", assessment.getRiskFactors().getBehaviorRisk());
            factors.put("velocity", assessment.getRiskFactors().getVelocityRisk());
            factors.put("geographic", assessment.getRiskFactors().getGeographicRisk());
            factors.put("merchant", assessment.getRiskFactors().getMerchantRisk());
            eventData.setFactors(factors);

            eventData.setDecision(assessment.getDecision());
            eventData.setDecisionDetails(assessment.getDecisionDetails());

            EventStoreEntry eventStoreEntry = eventStoreService.saveRiskScoreEvent(assessment, customerId);

            List<Anomaly> anomalies = anomalyDetectionService.detectAnomalies(assessment);
            if (!anomalies.isEmpty()) {
                anomalyDetectionService.saveAnomalies(anomalies);

                if (assessment.getRiskLevel() == com.riskplatform.common.enums.RiskLevel.HIGH ||
                        assessment.getRiskLevel() == com.riskplatform.common.enums.RiskLevel.CRITICAL) {
                    kafkaProducerService.sendHighRiskAlert(assessment);
                }
            }

            kafkaProducerService.sendRiskScoreCalculatedEvent(assessment);

        } catch (Exception e) {
            System.err.println("Error processing transaction-validated event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private RiskCalculationRequest createRiskCalculationRequest(TransactionValidatedEvent event) {
        RiskCalculationRequest request = new RiskCalculationRequest();

        request.setTransactionId(event.getTransactionId());
        request.setCustomerId(event.getCustomerId());
        request.setAmount(event.getAmount());
        request.setMerchant(event.getMerchant());
        // event.getLocation() returns com.risk.scoring.model.Location, but request
        // expects com.riskplatform.common.model.Location
        // Assuming TransactionValidatedEvent will be updated to use common Location
        request.setLocation(event.getLocation());
        request.setTimestamp(event.getTimestamp());

        // Fetch customer profile data from MongoDB
        CustomerProfileData customerProfile = customerRiskProfileService.getCustomerProfileData(event.getCustomerId());
        if (customerProfile == null) {
            // Fallback to mock data if not found
            customerProfile = createMockCustomerProfile(event);
        }
        request.setCustomerProfile(customerProfile);

        // Fetch velocity data from Redis cache
        VelocityData velocityData = customerRiskProfileService.getVelocityData(event.getCustomerId());
        if (velocityData == null) {
            // Fallback to mock data if not found
            velocityData = createMockVelocityData(event);
        }
        request.setVelocityData(velocityData);

        return request;
    }

    private CustomerProfileData createMockCustomerProfile(TransactionValidatedEvent event) {
        CustomerProfileData customerProfile = new CustomerProfileData();
        customerProfile.setRegistrationDate(event.getTimestamp().minusSeconds(86400 * 365)); // 1 year ago
        customerProfile.setKycStatus(com.risk.scoring.model.enums.KycStatus.VERIFIED);
        customerProfile.setAllowedCountries(java.util.Arrays.asList("US", "CA", "GB", "AU", "IN"));
        customerProfile.setDailyLimit(5000.0);
        customerProfile.setAvgTransactionAmount(1000.0);
        customerProfile.setAccountStatus(com.risk.scoring.model.enums.AccountStatus.ACTIVE);
        customerProfile.setFraudHistory(false);
        customerProfile.setFailedTransactionsLast7Days(0);
        customerProfile.setLastVerifiedLocation(event.getLocation());
        customerProfile.setLastTransactionTime(event.getTimestamp().minusSeconds(3600)); // 1 hour ago
        return customerProfile;
    }

    private VelocityData createMockVelocityData(TransactionValidatedEvent event) {
        VelocityData velocityData = new VelocityData();
        velocityData.setTransactionsInLastHour(5);
        velocityData.setTransactionsInLastDay(20);
        return velocityData;
    }
}