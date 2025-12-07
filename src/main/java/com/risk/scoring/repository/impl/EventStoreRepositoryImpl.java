package com.risk.scoring.repository.impl;

import com.risk.scoring.model.EventStoreEntry;
import com.risk.scoring.repository.EventStoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class EventStoreRepositoryImpl {

    @Autowired
    private MongoTemplate mongoTemplate;

    public Page<EventStoreEntry> findByCustomerIdAndDateRange(String customerId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Query query = new Query();
        query.addCriteria(Criteria.where("aggregateId").is(customerId)
                .and("timestamp").gte(startDate).lte(endDate));
        query.with(pageable);

        List<EventStoreEntry> events = mongoTemplate.find(query, EventStoreEntry.class);
        return new PageImpl<>(events, pageable, events.size());
    }

    public List<EventStoreEntry> findByEventTypeAndDateRange(String eventType, LocalDateTime startDate, LocalDateTime endDate) {
        Query query = new Query();
        query.addCriteria(Criteria.where("eventType").is(eventType)
                .and("timestamp").gte(startDate).lte(endDate));

        return mongoTemplate.find(query, EventStoreEntry.class);
    }

    public List<EventStoreEntry> findRecentEventsByCustomerId(String customerId, int limit) {
        Query query = new Query();
        query.addCriteria(Criteria.where("aggregateId").is(customerId));
        query.limit(limit);
        query.with(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "timestamp"));

        return mongoTemplate.find(query, EventStoreEntry.class);
    }

    public void bulkSaveEvents(List<EventStoreEntry> events) {
        mongoTemplate.insert(events, EventStoreEntry.class);
    }
}