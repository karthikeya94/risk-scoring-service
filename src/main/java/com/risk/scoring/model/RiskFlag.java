package com.risk.scoring.model;

import com.risk.scoring.model.enums.Severity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskFlag {
    private String flag;
    private Severity severity;
    private String description;
}