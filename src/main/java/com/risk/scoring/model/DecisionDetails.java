package com.risk.scoring.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecisionDetails {
    private String reason;
    private boolean manualReviewRequired;
    private double approvalConfidence;
}