package com.risk.scoring.model.dto;

import com.risk.scoring.model.enums.KycStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfileSummary {
    private String customerSegment;
    private KycStatus kycStatus;
    private boolean fraudHistory;
    private boolean dormant;
}