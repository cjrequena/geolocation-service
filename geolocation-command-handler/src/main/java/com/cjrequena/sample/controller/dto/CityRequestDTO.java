package com.cjrequena.sample.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for creating a new City.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new City")
public class CityRequestDTO {

  @NotBlank(message = "City name is required")
  @Size(min = 2, max = 100, message = "City name must be between 2 and 100 characters")
  @Schema(description = "City name", example = "San Francisco", required = true)
  private String name;

  @NotBlank(message = "Region ID is required")
  @Schema(description = "Parent region ID", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
  private String regionId;

  @NotBlank(message = "Time zone is required")
  @Schema(description = "IANA time zone identifier", example = "America/Los_Angeles", required = true)
  private String timeZone;

  @Schema(description = "Is this a capital city", example = "false")
  private Boolean capital;

  @Schema(description = "Associated GeoShape ID", example = "660e8400-e29b-41d4-a716-446655440000")
  private String geoShapeId;

  @Min(value = 0, message = "Population must be non-negative")
  @Schema(description = "Population count", example = "873965")
  private Long population;

  @Size(max = 20, message = "Postal code must not exceed 20 characters")
  @Schema(description = "Postal code", example = "94110")
  private String postalCode;

  @Schema(description = "Is this a city active", example = "false")
  private Boolean active;

  @Schema(description = "Custom metadata as key-value pairs")
  private Map<String, Object> metadata;
}
