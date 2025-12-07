package com.risk.scoring.messaging;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RejectionDetails {
    private Double requestedAmount;
    private Double customerLimit;
    private String validationErrors;
}