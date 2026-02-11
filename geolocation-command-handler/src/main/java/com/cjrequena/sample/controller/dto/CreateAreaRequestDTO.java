package com.cjrequena.sample.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new Area.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new Area")
public class CreateAreaRequestDTO {

  @NotBlank(message = "Area name is required")
  @Size(min = 2, max = 100, message = "Area name must be between 2 and 100 characters")
  @Schema(description = "Area name", example = "Mission District", required = true)
  private String name;

  @NotBlank(message = "City ID is required")
  @Schema(description = "Parent city ID", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
  private String cityId;

  @NotBlank(message = "Area type is required")
  @Schema(description = "Area type", example = "DISTRICT", required = true)
  private String areaType;

  @Size(max = 20, message = "Postal code must not exceed 20 characters")
  @Schema(description = "Postal code", example = "94110")
  private String postalCode;

  @Schema(description = "Associated GeoShape ID", example = "660e8400-e29b-41d4-a716-446655440000")
  private String geoShapeId;

  @Min(value = 0, message = "Population must be non-negative")
  @Schema(description = "Population count", example = "48000")
  private Long population;

  @Schema(description = "Is this a area active", example = "false")
  private Boolean active;
}
