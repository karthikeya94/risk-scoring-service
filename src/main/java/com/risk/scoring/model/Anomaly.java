package com.risk.scoring.model;

import com.risk.scoring.model.enums.AnomalyType;
import com.risk.scoring.model.enums.Severity;
import com.risk.scoring.model.enums.AnomalyStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "anomalies")
public class Anomaly {
    @Id
    private String id;
    private String customerId;
    private String transactionId;
    private AnomalyType anomalyType;
    private Severity severity;
    private String description;
    private AnomalyDetails details;
    private Instant detectedAt;
    private AnomalyStatus status;
    private String assignedTo;
    private Instant resolvedAt;
    private String resolutionNotes;
}