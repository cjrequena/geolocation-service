package com.cjrequena.sample.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing Zone.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update an existing Zone")
public class UpdateZoneRequestDTO {

  @Size(min = 2, max = 100, message = "Zone name must be between 2 and 100 characters")
  @Schema(description = "Zone name", example = "Golden Gate Park")
  private String name;

  @Schema(description = "Zone type", example = "PARK")
  private String zoneType;

  @Size(max = 20, message = "Postal code must not exceed 20 characters")
  @Schema(description = "Postal code", example = "94121")
  private String postalCode;

  @Schema(description = "Associated GeoShape ID", example = "660e8400-e29b-41d4-a716-446655440000")
  private String geoShapeId;

  @Schema(description = "Active status", example = "true")
  private Boolean active;
}
