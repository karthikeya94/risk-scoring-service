package com.risk.scoring.service.impl;

import com.risk.scoring.model.Location;
import com.risk.scoring.model.dto.RiskCalculationRequest;
import com.risk.scoring.service.RiskFactorService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Set;

@Service
public class GeographicRiskServiceImpl implements RiskFactorService {

    private static final double GEOGRAPHIC_RISK_WEIGHT = 0.15;

    private static final Set<String> HIGH_RISK_COUNTRIES = Set.of("KP", "IR", "SY", "CU");

    private static final int IMPOSSIBLE_TRAVEL_DISTANCE = 1000; // km
    private static final int HIGH_RISK_DISTANCE = 500; // km

    private static final int IMPOSSIBLE_TRAVEL_TIME = 1; // hours
    private static final int HIGH_RISK_TIME = 2; // hours

    @Override
    public int calculateRiskFactor(RiskCalculationRequest request) {
        Location transactionLocation = request.getLocation();
        Location customerLastLocation = request.getCustomerProfile().getLastVerifiedLocation();
        Instant lastTxnTimestamp = request.getCustomerProfile().getLastTransactionTime();
        ZonedDateTime currentTimestamp = ZonedDateTime.ofInstant(request.getTimestamp(),
                java.time.ZoneId.systemDefault());

        if (transactionLocation == null || customerLastLocation == null || lastTxnTimestamp == null) {
            return 0;
        }

        int score = 0;

        long timeBetweenHours = Duration.between(lastTxnTimestamp, currentTimestamp).toHours();

        double distance = calculateHaversineDistance(customerLastLocation, transactionLocation);

        // Impossible travel detection
        if (timeBetweenHours < IMPOSSIBLE_TRAVEL_TIME && distance > IMPOSSIBLE_TRAVEL_DISTANCE) {
            score = 15; // Impossible travel
        } else if (distance > HIGH_RISK_DISTANCE && timeBetweenHours < HIGH_RISK_TIME) {
            score = 12;
        } else if (!request.getCustomerProfile().getAllowedCountries().contains(transactionLocation.getCountry())) {
            score = 10;
        } else if (HIGH_RISK_COUNTRIES.contains(transactionLocation.getCountry())) {
            score = 8;
        } else {
            score = 0;
        }

        return score;
    }

    /**
     * Calculate the great circle distance between two points on the earth using the Haversine formula
     * @param loc1 First location
     * @param loc2 Second location
     * @return Distance in kilometers
     */
    private double calculateHaversineDistance(Location loc1, Location loc2) {
        // For simplicity, we're using a basic approximation
        // In a real implementation, you would use actual coordinates (latitude/longitude)
        // and calculate the precise Haversine distance
        
        // If same country, assume small distance
        if (loc1.getCountry() != null && loc2.getCountry() != null &&
            loc1.getCountry().equals(loc2.getCountry())) {
            return 0;
        }
        
        // Simple approximation - in a real system, you'd use actual coordinates
        // This is just a placeholder for demonstration
        return 1000; // Assume 1000km distance for different countries
    }

    @Override
    public double getWeight() {
        return GEOGRAPHIC_RISK_WEIGHT;
    }
}