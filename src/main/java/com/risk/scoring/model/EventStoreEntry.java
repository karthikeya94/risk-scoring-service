package com.risk.scoring.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "eventstore")
public class EventStoreEntry {
    @Id
    private String id;
    private String aggregateId; // Customer ID
    private String aggregateType; // "CustomerRiskProfile"
    private String eventType; // "RiskScoreCalculated"
    private int eventVersion;
    private EventData eventData;
    private Metadata metadata;
    private long version; // For optimistic locking
    private Instant timestamp;
}