package com.risk.scoring.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.risk.scoring.model.*;
import com.riskplatform.common.entity.EventStoreEntry;
import com.riskplatform.common.entity.RiskAssessment;
import com.riskplatform.common.entity.EventMetadata;
import com.risk.scoring.repository.EventStoreRepository;
import com.risk.scoring.service.EventStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class EventStoreServiceImpl implements EventStoreService {

    @Autowired
    private EventStoreRepository eventStoreRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public EventStoreEntry saveRiskScoreEvent(RiskAssessment assessment, String customerId) {
        EventStoreEntry event = new EventStoreEntry();
        // Common EventStoreEntry uses String id (which is @Id) so we can set it or let
        // Mongo gen it.
        // Local used "event-" + UUID.
        event.setId("event-" + UUID.randomUUID().toString());
        event.setAggregateId(customerId);
        event.setAggregateType("CustomerRiskProfile");
        event.setEventType("RiskScoreCalculated");
        long v = getNextVersion(customerId);
        event.setEventVersion((int) v);
        event.setVersion(v);

        // Create event map
        Map<String, Object> eventDataMap = new HashMap<>();
        eventDataMap.put("transactionId", assessment.getTransactionId());
        eventDataMap.put("previousScore", assessment.getRiskScore() - 5); // Simplified previous score logic
        eventDataMap.put("newScore", assessment.getRiskScore());
        eventDataMap.put("riskLevel", assessment.getRiskLevel());

        // factors map
        Map<String, Integer> factors = new HashMap<>();
        if (assessment.getRiskFactors() != null) {
            factors.put("transaction", assessment.getRiskFactors().getTransactionRisk());
            factors.put("behavior", assessment.getRiskFactors().getBehaviorRisk());
            factors.put("velocity", assessment.getRiskFactors().getVelocityRisk());
            factors.put("geographic", assessment.getRiskFactors().getGeographicRisk());
            factors.put("merchant", assessment.getRiskFactors().getMerchantRisk());
        }
        eventDataMap.put("factors", factors);

        eventDataMap.put("decision", assessment.getDecision());
        eventDataMap.put("decisionDetails", assessment.getDecisionDetails());

        event.setEventData(eventDataMap);

        // Create metadata
        EventMetadata metadata = new EventMetadata();
        metadata.setCausationId("cmd-txn-" + assessment.getTransactionId());
        metadata.setCorrelationId("corr-" + assessment.getTransactionId());
        metadata.setUserId("system");
        metadata.setSource("RiskScoringEngine");
        event.setMetadata(metadata);

        event.setTimestamp(assessment.getTimestamp());

        return eventStoreRepository.save(event);
    }

    private long getNextVersion(String customerId) {
        // In a real implementation, this would get the next version number from the
        // database
        // For now, we'll return a fixed value
        return System.currentTimeMillis() % 1000;
    }

    @Override
    public List<EventStoreEntry> getEventsByCustomerId(String customerId) {
        return eventStoreRepository.findByAggregateIdAndAggregateTypeOrderByVersionAsc(
                customerId, "CustomerRiskProfile");
    }

    @Override
    public Page<EventStoreEntry> getEventsByCustomerIdAndDateRange(String customerId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {
        return eventStoreRepository.findByCustomerIdAndDateRange(customerId, startDate, endDate, pageable);
    }

    @Override
    public List<EventStoreEntry> getEventsByEventTypeAndDateRange(String eventType,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        return eventStoreRepository.findByEventTypeAndDateRange(eventType, startDate, endDate);
    }

    @Override
    public List<EventStoreEntry> getRecentEventsByCustomerId(String customerId, int limit) {
        return eventStoreRepository.findRecentEventsByCustomerId(customerId, limit);
    }

    @Override
    public EventStoreEntry saveEvent(EventStoreEntry event) {
        return eventStoreRepository.save(event);
    }

    @Override
    public void bulkSaveEvents(List<EventStoreEntry> events) {
        eventStoreRepository.bulkSaveEvents(events);
    }
}