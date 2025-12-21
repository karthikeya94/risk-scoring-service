package com.risk.scoring.repository;

import com.riskplatform.common.entity.Anomaly;
import com.riskplatform.common.enums.AnomalyType;
import com.riskplatform.common.enums.Severity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;

@Repository
public interface AnomalyRepository extends MongoRepository<Anomaly, String> {
    List<Anomaly> findByCustomerId(String customerId);

    List<Anomaly> findByAnomalyTypeAndSeverity(AnomalyType anomalyType, Severity severity);

    List<Anomaly> findByDetectedAtAfter(Instant timestamp);

    List<Anomaly> findBySeverity(Severity severity);
}