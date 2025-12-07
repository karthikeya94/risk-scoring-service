package com.risk.scoring.repository;

import com.risk.scoring.model.Anomaly;
import com.risk.scoring.model.enums.AnomalyType;
import com.risk.scoring.model.enums.Severity;
import com.risk.scoring.model.enums.AnomalyStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;

@Repository
public interface AnomalyRepository extends MongoRepository<Anomaly, String> {
    List<Anomaly> findByCustomerId(String customerId);

    List<Anomaly> findByAnomalyTypeAndSeverity(AnomalyType anomalyType, Severity severity);

    List<Anomaly> findByDetectedAtAfter(Instant timestamp);

    List<Anomaly> findByStatusAndSeverity(AnomalyStatus status, Severity severity);
}