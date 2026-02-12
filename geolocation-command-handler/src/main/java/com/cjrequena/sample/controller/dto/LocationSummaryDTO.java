package com.cjrequena.sample.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Summary DTO for Location 
 *
 * <p>Lightweight representation for lists. Address may be truncated if long.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Location summary for lists")
public class LocationSummaryDTO {

  @Schema(description = "Location unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
  private String id;

  @Schema(description = "Latitude in decimal degrees", example = "37.7749")
  private Double latitude;

  @Schema(description = "Longitude in decimal degrees", example = "-122.4194")
  private Double longitude;

  @Schema(description = "Physical address (may be truncated)", example = "Golden Gate Park, San Francisco")
  private String address;

  @Schema(description = "Active status", example = "true")
  private Boolean active;
}
