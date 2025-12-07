package com.risk.scoring.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyStats {
    private String month; // Format: "2025-11"
    private int transactionCount;
    private BigDecimal totalAmount;
    private int averageRiskScore;
    private int highRiskTransactions;
    private int flaggedTransactions;
}