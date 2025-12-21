package com.risk.scoring.service;

import com.riskplatform.common.entity.Anomaly;
import com.riskplatform.common.entity.RiskAssessment;
import com.risk.scoring.model.dto.AnomaliesResponse;

import java.util.List;

public interface AnomalyDetectionService {

    List<Anomaly> detectAnomalies(RiskAssessment assessment);

    void saveAnomalies(List<Anomaly> anomalies);

    AnomaliesResponse getRecentAnomalies(int limit);

    List<Anomaly> getAnomaliesByCustomerId(String customerId);

    List<Anomaly> getAnomaliesByType(String anomalyType);

    List<Anomaly> getAnomaliesBySeverity(String severity);
}