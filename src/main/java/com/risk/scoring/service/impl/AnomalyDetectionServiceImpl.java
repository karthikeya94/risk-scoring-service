package com.risk.scoring.service.impl;

import com.risk.scoring.model.*;
import com.risk.scoring.model.enums.Severity;
import com.risk.scoring.model.dto.AnomaliesResponse;
import com.risk.scoring.repository.AnomalyRepository;
import com.risk.scoring.service.AnomalyDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AnomalyDetectionServiceImpl implements AnomalyDetectionService {
    
    @Autowired
    private AnomalyRepository anomalyRepository;
    
    @Override
    public List<Anomaly> detectAnomalies(RiskAssessment assessment) {
        List<Anomaly> anomalies = new ArrayList<>();
        if (assessment.getRiskFactors().getGeographicRisk() >= 15) {
            Anomaly anomaly = createAnomaly(assessment, "IMPOSSIBLE_TRAVEL", Severity.HIGH);
            anomaly.setDescription("Customer detected in location 1000+ km away in short time");
            anomalies.add(anomaly);
        }
        if (assessment.getRiskFactors().getVelocityRisk() >= 20) {
            Anomaly anomaly = createAnomaly(assessment, "VELOCITY_SPIKE", Severity.HIGH);
            anomaly.setDescription("Customer exceeded normal transaction frequency by 5x");
            anomalies.add(anomaly);
        } else if (assessment.getRiskFactors().getVelocityRisk() >= 15) {
            Anomaly anomaly = createAnomaly(assessment, "VELOCITY_SPIKE", Severity.MEDIUM);
            anomaly.setDescription("Customer exceeded normal transaction frequency by 3x");
            anomalies.add(anomaly);
        }
        if (assessment.getRiskFactors().getTransactionRisk() >= 30) {
            Anomaly anomaly = createAnomaly(assessment, "HIGH_AMOUNT_TRANSACTION", Severity.HIGH);
            anomaly.setDescription("Transaction amount significantly higher than customer average");
            anomalies.add(anomaly);
        }
        if (assessment.getRiskFactors().getMerchantRisk() >= 10) {
            Anomaly anomaly = createAnomaly(assessment, "SUSPICIOUS_MERCHANT", Severity.HIGH);
            anomaly.setDescription("Transaction with high-risk merchant category");
            anomalies.add(anomaly);
        }
        
        return anomalies;
    }
    
    private Anomaly createAnomaly(RiskAssessment assessment, String type, Severity severity) {
        Anomaly anomaly = new Anomaly();
        anomaly.setId("ANOM-" + ZonedDateTime.now().toLocalDate().toString().replace("-", "") + "-" + 
                     UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        anomaly.setCustomerId(extractCustomerId(assessment.getTransactionId()));
        anomaly.setTransactionId(assessment.getTransactionId());
        anomaly.setAnomalyType(com.risk.scoring.model.enums.AnomalyType.valueOf(type));
        anomaly.setSeverity(severity);
        anomaly.setDetectedAt(assessment.getTimestamp());
        AnomalyDetails details = new AnomalyDetails();
        details.setRiskFactors(assessment.getRiskFactors());
        details.setRiskScore(assessment.getRiskScore());
        details.setRiskLevel(assessment.getRiskLevel());
        anomaly.setDetails(details);
        
        return anomaly;
    }
    
    private String extractCustomerId(String transactionId) {
        // Extract customer ID from transaction ID (assuming format T{customerId}{sequence})
        if (transactionId != null && transactionId.startsWith("T")) {
            // Remove the "T" prefix and extract the customer part
            // This is a simplified implementation - in reality, you might need a more robust extraction
            return transactionId.substring(1, Math.min(7, transactionId.length()));
        }
        return "UNKNOWN";
    }
    
    @Override
    public void saveAnomalies(List<Anomaly> anomalies) {
        if (anomalies != null && !anomalies.isEmpty()) {
            anomalyRepository.saveAll(anomalies);
        }
    }
    
    @Override
    public AnomaliesResponse getRecentAnomalies(int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "detectedAt"));
        List<Anomaly> anomalies = anomalyRepository.findAll(pageRequest).getContent();
        
        AnomaliesResponse response = new AnomaliesResponse();
        response.setAnomalies(anomalies);
        response.setTotalAnomalies((int) anomalyRepository.count());
        response.setTimeWindow("last_" + limit + "_anomalies");
        
        return response;
    }
    
    @Override
    public List<Anomaly> getAnomaliesByCustomerId(String customerId) {
        return anomalyRepository.findByCustomerId(customerId);
    }
    
    @Override
    public List<Anomaly> getAnomaliesByType(String anomalyType) {
        return anomalyRepository.findByAnomalyTypeAndSeverity(
            com.risk.scoring.model.enums.AnomalyType.valueOf(anomalyType),
            com.risk.scoring.model.enums.Severity.HIGH); // Default severity
    }
    
    @Override
    public List<Anomaly> getAnomaliesBySeverity(String severity) {
        return anomalyRepository.findByStatusAndSeverity(
            com.risk.scoring.model.enums.AnomalyStatus.OPEN,
            Severity.valueOf(severity.toUpperCase()));
    }
}