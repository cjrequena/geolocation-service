package com.cjrequena.sample.controller.dto;

import com.cjrequena.sample.domain.model.enums.ZoneType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for updating an existing Zone.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update an existing Zone")
public class UpdateZoneRequestDTO {

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
  //@Pattern(regexp = "^(POINT|POLYGON|CIRCLE|RECTANGLE|LINE)$", message = "Geometry type must be POINT|POLYGON|CIRCLE|RECTANGLE|LINE")
  @Schema(description = "Zone type", example = "COMMERCIAL", required = true)
  private ZoneType zoneType;

  @Size(max = 20, message = "Postal code must not exceed 20 characters")
  @Schema(description = "Postal code", example = "94121")
  private String postalCode;

  @Schema(description = "Is this a zone active", example = "false")
  private Boolean active;

  @Schema(description = "Custom metadata as key-value pairs")
  private Map<String, Object> metadata;
}
