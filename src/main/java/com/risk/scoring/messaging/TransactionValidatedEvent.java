package com.risk.scoring.messaging;

import com.risk.scoring.model.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TransactionValidatedEvent {
    private String eventId;
    private String transactionId;
    private String customerId;
    private Double amount;
    private String currency;
    private String merchant;
    private String merchantCategory;
    private Location location;
    private Instant timestamp;
    private String channel;
    private String device;
    private String eventType;
    private Instant eventTimestamp;
    private String correlationId;
    private String rejectionReason;
    private RejectionDetails rejectionDetails;
}