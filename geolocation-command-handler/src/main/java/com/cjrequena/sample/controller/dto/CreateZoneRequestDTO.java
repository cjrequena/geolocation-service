package com.cjrequena.sample.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new Zone.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new Zone")
public class CreateZoneRequestDTO {

  @NotBlank(message = "Area ID is required")
  @Schema(description = "Parent area ID", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
  private String areaId;

  @Schema(description = "Associated GeoShape ID", example = "660e8400-e29b-41d4-a716-446655440000")
  private String geoShapeId;

  @NotBlank(message = "Zone name is required")
  @Size(min = 2, max = 100, message = "Zone name must be between 2 and 100 characters")
  @Schema(description = "Zone name", example = "Golden Gate Park", required = true)
  private String name;

  @NotBlank(message = "Zone type is required")
  @Schema(description = "Zone type", example = "PARK", required = true)
  private String zoneType;

  @Size(max = 20, message = "Postal code must not exceed 20 characters")
  @Schema(description = "Postal code", example = "94121")
  private String postalCode;

  @Schema(description = "Is this a zone active", example = "false")
  private Boolean active;
}
