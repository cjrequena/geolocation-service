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
 * Request DTO for updating an existing Region.
 *
 * <p>All fields are optional. The parent country cannot be changed — regions belong
 * to their country permanently.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update an existing Region")
public class UpdateRegionRequestDTO {

  @NotBlank(message = "Country ID is required")
  @Schema(description = "Parent country ID", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
  private String countryId;

  @NotBlank(message = "Region name is required")
  @Size(min = 2, max = 100, message = "Region name must be between 2 and 100 characters")
  @Schema(description = "Region name", example = "California", required = true)
  private String name;

  @Size(min = 2, max = 10, message = "Region code must be between 2 and 10 characters")
  @Schema(description = "Region code", example = "US-CA")
  private String code;

  @NotBlank(message = "Region type is required")
  @Schema(description = "Region type", example = "STATE", required = true)
  private String regionType;

  @Schema(description = "Associated GeoShape ID", example = "660e8400-e29b-41d4-a716-446655440000")
  private String geoShapeId;

  @Min(value = 0, message = "Population must be non-negative")
  @Schema(description = "Population count", example = "39538223")
  private Long population;

  @NotBlank(message = "Time zone is required")
  @Schema(description = "IANA time zone identifier", example = "America/Los_Angeles")
  private String timeZone;

  @Schema(description = "Is this a area active", example = "false")
  private Boolean active;

  @Schema(description = "Custom metadata as key-value pairs")
  private Map<String, Object> metadata;
}
