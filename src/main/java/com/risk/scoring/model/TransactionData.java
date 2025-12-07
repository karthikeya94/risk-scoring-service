package com.risk.scoring.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionData {
    private String transactionId;
    private String customerId;
    private Double amount;
    private String merchant;
    private Location location;
    private Instant timestamp;
    private String channel;
    private boolean newLocation;
}