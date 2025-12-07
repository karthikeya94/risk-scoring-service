package com.risk.scoring.model;

import com.risk.scoring.model.enums.CustomerAgeStatus;
import com.risk.scoring.model.enums.VelocityStatus;
import com.risk.scoring.model.enums.GeographicStatus;
import com.risk.scoring.model.enums.MerchantStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskFactorStatus {
    private CustomerAgeStatus customerAge;
    private boolean fraudHistory;
    private VelocityStatus velocityStatus;
    private GeographicStatus geographicStatus;
    private MerchantStatus merchantStatus;
}