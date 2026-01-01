package com.risk.scoring.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.riskplatform.common.entity.CustomerRiskProfile;
import com.riskplatform.common.entity.RiskAssessment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaProducerService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${kafka.topics.risk-score-calculated}")
    private String riskScoreCalculatedTopic;

    @Value("${kafka.topics.risk-profile-updated}")
    private String riskProfileUpdatedTopic;

    @Value("${kafka.topics.risk-alert-high-score}")
    private String riskAlertHighScoreTopic;

    public void sendRiskScoreCalculatedEvent(RiskAssessment assessment) {
        try {
            String message = objectMapper.writeValueAsString(assessment);
            log.info("Sending risk-score-calculated event: " + message);
            kafkaTemplate.send(riskScoreCalculatedTopic, assessment.getTransactionId(), message);
        } catch (Exception e) {
            System.err.println("Error sending risk-score-calculated event: " + e.getMessage());
        }
    }

    public void sendRiskProfileUpdatedEvent(CustomerRiskProfile profile) {
        try {
            String message = objectMapper.writeValueAsString(profile);
            log.info("Sending risk-profile-updated event: " + message);
            kafkaTemplate.send(riskProfileUpdatedTopic, profile.getCustomerId(), message);
        } catch (Exception e) {
            System.err.println("Error sending risk-profile-updated event: " + e.getMessage());
        }
    }

    public void sendHighRiskAlert(RiskAssessment assessment) {
        try {
            String message = objectMapper.writeValueAsString(assessment);
            log.info("Sending high-risk alert: " + message);
            kafkaTemplate.send(riskAlertHighScoreTopic, assessment.getTransactionId(), message);
        } catch (Exception e) {
            System.err.println("Error sending high-risk alert: " + e.getMessage());
        }
    }
}