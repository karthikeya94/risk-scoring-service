package com.risk.scoring.service;

import com.risk.scoring.model.EventStoreEntry;
import com.risk.scoring.model.RiskAssessment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface EventStoreService {
    EventStoreEntry saveRiskScoreEvent(RiskAssessment assessment, String customerId);

    List<EventStoreEntry> getEventsByCustomerId(String customerId);

    Page<EventStoreEntry> getEventsByCustomerIdAndDateRange(String customerId,
                                                            LocalDateTime startDate,
                                                            LocalDateTime endDate,
                                                            Pageable pageable);

    List<EventStoreEntry> getEventsByEventTypeAndDateRange(String eventType,
                                                           LocalDateTime startDate,
                                                           LocalDateTime endDate);

    List<EventStoreEntry> getRecentEventsByCustomerId(String customerId, int limit);

    EventStoreEntry saveEvent(EventStoreEntry event);

    void bulkSaveEvents(List<EventStoreEntry> events);
}