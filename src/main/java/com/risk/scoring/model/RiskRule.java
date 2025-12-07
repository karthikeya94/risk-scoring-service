package com.risk.scoring.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.Map;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "riskRules")
public class RiskRule {
    @Id
    private String id;
    private String ruleName;
    private String ruleType;
    private Map<String, Object> parameters;
    private boolean enabled;
    private Instant effectiveDate;
    private Instant createdAt;
    private Instant updatedAt;
    private int version;
}