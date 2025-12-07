package com.risk.scoring.model.dto;

import com.risk.scoring.model.CustomerProfileData;
import com.risk.scoring.model.Location;
import com.risk.scoring.model.TransactionData;
import com.risk.scoring.model.VelocityData;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskCalculationRequest {
    @NotBlank(message = "Transaction ID is required")
    private String transactionId;

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotNull(message = "Amount is required")
    private Double amount;

    @NotBlank(message = "Merchant is required")
    private String merchant;

    @NotNull(message = "Location is required")
    private Location location;

    @NotNull(message = "Timestamp is required")
    private Instant timestamp;
    
    private CustomerProfileData customerProfile;
    
    private VelocityData velocityData;
}