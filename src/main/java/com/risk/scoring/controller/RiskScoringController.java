package com.risk.scoring.controller;

import com.risk.scoring.model.Anomaly;
import com.risk.scoring.model.dto.*;
import com.risk.scoring.service.RiskScoringService;
import com.risk.scoring.service.CustomerRiskProfileService;
import com.risk.scoring.service.AnomalyDetectionService;
import com.risk.scoring.service.RiskRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/v1/risk")
@Tag(name = "Risk Scoring", description = "Endpoints for calculating and managing risk scores")
public class RiskScoringController {

    @Autowired
    private RiskScoringService riskScoringService;

    @Autowired
    private CustomerRiskProfileService customerRiskProfileService;

    @Autowired
    private AnomalyDetectionService anomalyDetectionService;

    @Autowired
    private RiskRuleService riskRuleService;

    @PostMapping("/calculate")
    @Operation(summary = "Calculate risk score for a transaction", description = "Calculates a risk score based on transaction data, customer profile, and other factors")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Risk score calculated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RiskCalculationResponse.class))), @ApiResponse(responseCode = "400", description = "Invalid request data provided")})
    public ResponseEntity<RiskCalculationResponse> calculateRiskScore(@Valid @RequestBody RiskCalculationRequest request) {
        try {
            RiskCalculationResponse response = riskScoringService.calculateRiskScore(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/customer/{customerId}/score")
    @Operation(summary = "Get customer risk profile", description = "Retrieves the complete risk profile for a specific customer")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Customer risk profile retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerRiskProfileResponse.class))), @ApiResponse(responseCode = "404", description = "Customer not found"), @ApiResponse(responseCode = "500", description = "Internal server error")})
    public ResponseEntity<CustomerRiskProfileResponse> getCustomerRiskProfile(@Parameter(description = "ID of the customer", required = true) @PathVariable String customerId) {
        try {
            CustomerRiskProfileResponse response = customerRiskProfileService.getCustomerRiskProfileResponse(customerId);
            if (response != null) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/rules")
    @Operation(summary = "Create or update a risk rule", description = "Creates a new risk rule or updates an existing one")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Risk rule created/updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RiskRuleResponse.class))), @ApiResponse(responseCode = "400", description = "Invalid request data provided"), @ApiResponse(responseCode = "500", description = "Internal server error")})
    public ResponseEntity<RiskRuleResponse> createOrUpdateRule(@Parameter(description = "Risk rule request object", required = true) @Valid @RequestBody RiskRuleRequest request) {
        try {
            RiskRuleResponse response = riskRuleService.saveRule(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/rules/{ruleId}/activate")
    @Operation(summary = "Activate a risk rule", description = "Activates an existing risk rule by its ID")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Risk rule activated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RiskRuleResponse.class))), @ApiResponse(responseCode = "404", description = "Risk rule not found"), @ApiResponse(responseCode = "500", description = "Internal server error")})
    public ResponseEntity<RiskRuleResponse> activateRule(@Parameter(description = "ID of the risk rule to activate", required = true) @PathVariable String ruleId) {
        try {
            RiskRuleResponse response = riskRuleService.activateRule(ruleId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/rules/{ruleId}/deactivate")
    @Operation(summary = "Deactivate a risk rule", description = "Deactivates an existing risk rule by its ID")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Risk rule deactivated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RiskRuleResponse.class))), @ApiResponse(responseCode = "404", description = "Risk rule not found"), @ApiResponse(responseCode = "500", description = "Internal server error")})
    public ResponseEntity<RiskRuleResponse> deactivateRule(@Parameter(description = "ID of the risk rule to deactivate", required = true) @PathVariable String ruleId) {
        try {
            RiskRuleResponse response = riskRuleService.deactivateRule(ruleId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/rules/{ruleId}")
    @Operation(summary = "Delete a risk rule", description = "Deletes an existing risk rule by its ID")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Risk rule deleted successfully"), @ApiResponse(responseCode = "404", description = "Risk rule not found"), @ApiResponse(responseCode = "500", description = "Internal server error")})
    public ResponseEntity<Void> deleteRule(@Parameter(description = "ID of the risk rule to delete", required = true) @PathVariable String ruleId) {
        try {
            boolean deleted = riskRuleService.deleteRule(ruleId);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/anomalies")
    @Operation(summary = "Get detected anomalies", description = "Retrieves a list of detected anomalies")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Anomalies retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AnomaliesResponse.class))), @ApiResponse(responseCode = "500", description = "Internal server error")})
    public ResponseEntity<AnomaliesResponse> getAnomalies() {
        try {
            AnomaliesResponse response = anomalyDetectionService.getRecentAnomalies(50);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/customer/{customerId}/trend")
    @Operation(summary = "Get customer risk trend", description = "Retrieves the risk trend for a customer over a specified number of days")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Customer risk trend retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerProfileSummary.class))), @ApiResponse(responseCode = "500", description = "Internal server error")})
    public ResponseEntity<List<CustomerProfileSummary>> getCustomerRiskTrend(@Parameter(description = "ID of the customer", required = true) @PathVariable String customerId, @Parameter(description = "Number of days to retrieve trend for", example = "30") @RequestParam(defaultValue = "30") int days) {
        try {
            List<CustomerProfileSummary> trend = customerRiskProfileService.getRiskTrend(customerId, days);
            return ResponseEntity.ok(trend);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/anomalies/customer/{customerId}")
    @Operation(summary = "Get anomalies by customer ID", description = "Retrieves all anomalies associated with a specific customer")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Anomalies retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Anomaly.class))), @ApiResponse(responseCode = "500", description = "Internal server error")})
    public ResponseEntity<List<Anomaly>> getAnomaliesByCustomerId(@Parameter(description = "ID of the customer", required = true) @PathVariable String customerId) {
        try {
            List<Anomaly> anomalies = anomalyDetectionService.getAnomaliesByCustomerId(customerId);
            return ResponseEntity.ok(anomalies);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/anomalies/type/{anomalyType}")
    @Operation(summary = "Get anomalies by type", description = "Retrieves all anomalies of a specific type")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Anomalies retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Anomaly.class))), @ApiResponse(responseCode = "500", description = "Internal server error")})
    public ResponseEntity<List<Anomaly>> getAnomaliesByType(@Parameter(description = "Type of anomaly to retrieve", required = true) @PathVariable String anomalyType) {
        try {
            List<Anomaly> anomalies = anomalyDetectionService.getAnomaliesByType(anomalyType);
            return ResponseEntity.ok(anomalies);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/anomalies/severity/{severity}")
    @Operation(summary = "Get anomalies by severity", description = "Retrieves all anomalies with a specific severity level")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Anomalies retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Anomaly.class))), @ApiResponse(responseCode = "500", description = "Internal server error")})
    public ResponseEntity<List<Anomaly>> getAnomaliesBySeverity(@Parameter(description = "Severity level of anomalies to retrieve", required = true) @PathVariable String severity) {
        try {
            List<Anomaly> anomalies = anomalyDetectionService.getAnomaliesBySeverity(severity);
            return ResponseEntity.ok(anomalies);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}