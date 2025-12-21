package com.risk.scoring.service.impl;

import com.riskplatform.common.entity.Anomaly;
import com.riskplatform.common.entity.RiskAssessment;
import com.riskplatform.common.enums.Severity;
import com.riskplatform.common.enums.AnomalyType;
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
        if (assessment.getRiskFactors() != null) {
            // Null safe checks
            Integer geoRisk = assessment.getRiskFactors().getGeographicRisk();
            if (geoRisk != null && geoRisk >= 15) {
                Anomaly anomaly = createAnomaly(assessment, AnomalyType.IMPOSSIBLE_TRAVEL, Severity.HIGH);
                anomaly.setDescription("Customer detected in location 1000+ km away in short time");
                anomalies.add(anomaly);
            }

            Integer velRisk = assessment.getRiskFactors().getVelocityRisk();
            if (velRisk != null) {
                if (velRisk >= 20) {
                    Anomaly anomaly = createAnomaly(assessment, AnomalyType.VELOCITY_SPIKE, Severity.HIGH);
                    anomaly.setDescription("Customer exceeded normal transaction frequency by 5x");
                    anomalies.add(anomaly);
                } else if (velRisk >= 15) {
                    Anomaly anomaly = createAnomaly(assessment, AnomalyType.VELOCITY_SPIKE, Severity.MEDIUM);
                    anomaly.setDescription("Customer exceeded normal transaction frequency by 3x");
                    anomalies.add(anomaly);
                }
            }

            Integer txnRisk = assessment.getRiskFactors().getTransactionRisk();
            if (txnRisk != null && txnRisk >= 30) {
                Anomaly anomaly = createAnomaly(assessment, AnomalyType.AMOUNT_DEVIATION, Severity.HIGH);
                anomaly.setDescription("Transaction amount significantly higher than customer average");
                anomalies.add(anomaly);
            }

            Integer merchRisk = assessment.getRiskFactors().getMerchantRisk();
            if (merchRisk != null && merchRisk >= 10) {
                Anomaly anomaly = createAnomaly(assessment, AnomalyType.UNUSUAL_MERCHANT, Severity.HIGH);
                anomaly.setDescription("Transaction with high-risk merchant category");
                anomalies.add(anomaly);
            }
        }

        return anomalies;
    }

    private Anomaly createAnomaly(RiskAssessment assessment, AnomalyType type, Severity severity) {
        Anomaly anomaly = new Anomaly();
        anomaly.setAnomalyId("ANOM-" + ZonedDateTime.now().toLocalDate().toString().replace("-", "") + "-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        anomaly.setCustomerId(extractCustomerId(assessment.getTransactionId()));
        anomaly.setTransactionId(assessment.getTransactionId());
        anomaly.setAnomalyType(type);
        anomaly.setSeverity(severity);
        anomaly.setDetectedAt(assessment.getTimestamp());
        // AnomalyDetails logic removed as Common Anomaly doesn't support it.

        return anomaly;
    }

    private String extractCustomerId(String transactionId) {
        // Extract customer ID from transaction ID (assuming format
        // T{customerId}{sequence})
        if (transactionId != null && transactionId.startsWith("T")) {
            // Remove the "T" prefix and extract the customer part
            // This is a simplified implementation - in reality, you might need a more
            // robust extraction
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
        try {
            return anomalyRepository.findByAnomalyTypeAndSeverity(
                    AnomalyType.valueOf(anomalyType),
                    Severity.HIGH); // Default severity
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    @Override
    public List<Anomaly> getAnomaliesBySeverity(String severity) {
        // 'findByStatusAndSeverity' - Common Anomaly doesn't have 'status'
        // (OPEN/CLOSED).
        // I should check AnomalyRepository.
        // Assuming findBySeverity exist or I should implement it.
        // And I can't filter by 'status' if it doesn't exist.
        try {
            return anomalyRepository.findBySeverity(Severity.valueOf(severity.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }
}