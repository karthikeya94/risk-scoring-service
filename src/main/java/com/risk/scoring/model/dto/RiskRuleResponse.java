package com.risk.scoring.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskRuleResponse {
    private String ruleId;
    private String status;
    private int version;
    private String message;
}