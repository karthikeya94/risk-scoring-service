package com.risk.scoring.model;

import com.risk.scoring.model.enums.RiskLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreHistoryEntry {
    private Instant date;
    private int score;
    private RiskLevel level;
}