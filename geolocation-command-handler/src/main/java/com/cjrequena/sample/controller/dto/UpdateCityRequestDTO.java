package com.cjrequena.sample.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing City.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update an existing City")
public class UpdateCityRequestDTO {

  @Size(min = 2, max = 100, message = "City name must be between 2 and 100 characters")
  @Schema(description = "City name", example = "San Francisco")
  private String name;

  @Schema(description = "IANA time zone identifier", example = "America/Los_Angeles")
  private String timeZone;

  @Schema(description = "Is this a capital city", example = "false")
  private Boolean isCapital;

  @Schema(description = "Associated GeoShape ID", example = "660e8400-e29b-41d4-a716-446655440000")
  private String geoShapeId;

  @Min(value = 0, message = "Population must be non-negative")
  @Schema(description = "Population count", example = "873965")
  private Long population;

  @Schema(description = "Active status", example = "true")
  private Boolean isActive;
}
