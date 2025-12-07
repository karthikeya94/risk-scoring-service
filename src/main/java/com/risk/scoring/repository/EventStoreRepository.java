package com.risk.scoring.repository;

import com.risk.scoring.model.EventStoreEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@Repository
public interface EventStoreRepository extends MongoRepository<EventStoreEntry, String>{

    List<EventStoreEntry> findByAggregateIdAndAggregateTypeOrderByVersionAsc(
        String aggregateId, String aggregateType);
        
    Page<EventStoreEntry> findByCustomerIdAndDateRange(String customerId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    List<EventStoreEntry> findByEventTypeAndDateRange(String eventType, LocalDateTime startDate, LocalDateTime endDate);
    
    List<EventStoreEntry> findRecentEventsByCustomerId(String customerId, int limit);
    
    void bulkSaveEvents(List<EventStoreEntry> events);
}