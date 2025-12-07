package com.risk.scoring.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskRuleRequest {
    private String ruleId;

    @NotBlank(message = "Rule name is required")
    private String ruleName;

    @NotNull(message = "Rule type is required")
    private String ruleType;

    private Map<String, Object> parameters;

    private boolean enabled = true;

    private Instant effectiveDate;
}