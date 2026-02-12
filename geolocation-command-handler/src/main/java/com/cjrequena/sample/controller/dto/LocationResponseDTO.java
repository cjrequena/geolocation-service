package com.cjrequena.sample.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO for Location 
 *
 * <p>Flattens PointVO into separate latitude/longitude fields for easier consumption
 * in JSON APIs.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Location details")
public class LocationResponseDTO {

  @Schema(description = "Location unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
  private String id;

  @Schema(description = "Latitude in decimal degrees", example = "37.7749")
  private Double latitude;

  @Schema(description = "Longitude in decimal degrees", example = "-122.4194")
  private Double longitude;

  @Schema(description = "Altitude in meters above sea level", example = "16.50")
  private Double altitudeMeters;

  @Schema(description = "GPS accuracy in meters", example = "3.75")
  private Double accuracyMeters;

  @Schema(description = "Physical address", example = "Golden Gate Park, San Francisco")
  private String address;

  @Schema(description = "Postal code", example = "94121")
  private String postalCode;

  @Schema(description = "Parent zone summary (if assigned)")
  private ZoneSummaryDTO zone;

  @Schema(description = "Custom metadata as key-value pairs")
  private Map<String, Object> metadata;

  @Schema(description = "Active status", example = "true")
  private Boolean active;

  @Schema(description = "Creation timestamp", example = "2024-06-01T12:00:00Z")
  private String createdAt;

  @Schema(description = "Last update timestamp", example = "2024-12-15T08:30:00Z")
  private String updatedAt;
}
