package com.risk.scoring.model.dto;

import com.risk.scoring.model.Anomaly;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnomaliesResponse {
    private List<Anomaly> anomalies;
    private int totalAnomalies;
    private String timeWindow;
}