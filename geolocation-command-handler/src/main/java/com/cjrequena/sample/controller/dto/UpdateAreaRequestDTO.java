package com.cjrequena.sample.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing Area.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update an existing Area")
public class UpdateAreaRequestDTO {

  @Size(min = 2, max = 100, message = "Area name must be between 2 and 100 characters")
  @Schema(description = "Area name", example = "Mission District")
  private String name;

  @Schema(description = "Area type", example = "DISTRICT")
  private String areaType;

  @Size(max = 20, message = "Postal code must not exceed 20 characters")
  @Schema(description = "Postal code", example = "94110")
  private String postalCode;

  @Schema(description = "Associated GeoShape ID", example = "660e8400-e29b-41d4-a716-446655440000")
  private String geoShapeId;

  @Min(value = 0, message = "Population must be non-negative")
  @Schema(description = "Population count", example = "48000")
  private Long population;

  @Schema(description = "Active status", example = "true")
  private Boolean active;
}
