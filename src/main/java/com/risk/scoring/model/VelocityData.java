package com.risk.scoring.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VelocityData {
    private int transactionsInLastHour;
    private int transactionsInLastDay;
}