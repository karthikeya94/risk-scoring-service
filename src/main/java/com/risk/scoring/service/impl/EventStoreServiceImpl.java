package com.risk.scoring.service.impl;

import com.risk.scoring.model.*;
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
    
    @Override
    public EventStoreEntry saveRiskScoreEvent(RiskAssessment assessment, String customerId) {
        EventStoreEntry event = new EventStoreEntry();
        event.setId("event-" + UUID.randomUUID().toString());
        event.setAggregateId(customerId);
        event.setAggregateType("CustomerRiskProfile");
        event.setEventType("RiskScoreCalculated");
        event.setVersion(getNextVersion(customerId));
        
        // Create event data
        EventData eventData = new EventData();
        eventData.setTransactionId(assessment.getTransactionId());
        eventData.setPreviousScore(assessment.getRiskScore() - 5); // Simplified previous score
        eventData.setNewScore(assessment.getRiskScore());
        eventData.setRiskLevel(assessment.getRiskLevel());
        
        // Set factors map
        Map<String, Integer> factors = new HashMap<>();
        factors.put("transaction", assessment.getRiskFactors().getTransactionRisk());
        factors.put("behavior", assessment.getRiskFactors().getBehaviorRisk());
        factors.put("velocity", assessment.getRiskFactors().getVelocityRisk());
        factors.put("geographic", assessment.getRiskFactors().getGeographicRisk());
        factors.put("merchant", assessment.getRiskFactors().getMerchantRisk());
        eventData.setFactors(factors);
        
        eventData.setDecision(assessment.getDecision());
        eventData.setDecisionDetails(assessment.getDecisionDetails());
        event.setEventData(eventData);
        
        // Create metadata
        Metadata metadata = new Metadata();
        metadata.setCausationId("cmd-txn-" + assessment.getTransactionId());
        metadata.setCorrelationId("corr-" + assessment.getTransactionId());
        metadata.setUserId("system");
        metadata.setSource("RiskScoringEngine");
        event.setMetadata(metadata);
        
        event.setTimestamp(assessment.getTimestamp());
        
        return eventStoreRepository.save(event);
    }
    
    private long getNextVersion(String customerId) {
        // In a real implementation, this would get the next version number from the database
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