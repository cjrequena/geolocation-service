package com.cjrequena.sample.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for updating an existing Location.
 *
 * <p>All fields are optional, including coordinates (which can be corrected if incorrect).</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update an existing Location")
public class UpdateLocationRequestDTO {

  @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
  @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
  @Schema(description = "Latitude in decimal degrees", example = "37.7749")
  private Double latitude;

  @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
  @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
  @Schema(description = "Longitude in decimal degrees", example = "-122.4194")
  private Double longitude;

  @DecimalMin(value = "0.0", message = "Altitude must be non-negative")
  @Schema(description = "Altitude in meters above sea level", example = "16.50")
  private Double altitudeMeters;

  @DecimalMin(value = "0.0", message = "Accuracy must be non-negative")
  @Schema(description = "GPS accuracy in meters", example = "3.75")
  private Double accuracyMeters;

  @Size(max = 500, message = "Address must not exceed 500 characters")
  @Schema(description = "Physical address", example = "Golden Gate Park, San Francisco")
  private String address;

  @Size(max = 20, message = "Postal code must not exceed 20 characters")
  @Schema(description = "Postal code", example = "94121")
  private String postalCode;

  @Schema(description = "Parent zone ID (null to unassign from zone)", example = "550e8400-e29b-41d4-a716-446655440000")
  private String zoneId;

  @Schema(description = "Custom metadata as key-value pairs")
  private Map<String, Object> metadata;

  @Schema(description = "Active status", example = "true")
  private Boolean active;
}
