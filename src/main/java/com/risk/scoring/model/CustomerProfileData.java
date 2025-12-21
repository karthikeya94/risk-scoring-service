package com.risk.scoring.model;

import com.risk.scoring.model.enums.KycStatus;
import com.risk.scoring.model.enums.AccountStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfileData {
    private Instant registrationDate;
    private KycStatus kycStatus;
    private List<String> allowedCountries;
    private Double dailyLimit;
    private Double avgTransactionAmount;
    private AccountStatus accountStatus;
    private boolean fraudHistory;
    private int failedTransactionsLast7Days;
    private com.riskplatform.common.model.Location lastVerifiedLocation;
    private Instant lastTransactionTime;
}