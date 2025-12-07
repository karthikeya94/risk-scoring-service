package com.risk.scoring.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Merchant {
    private String id;
    private String name;
    private String category;
    private String riskCategory;
    private String country;
    private double chargebackRate;
    private boolean registeredWithin30Days;
}
