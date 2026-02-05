package com.cjrequena.sample.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

  @Size(min = 2, max = 100, message = "Region name must be between 2 and 100 characters")
  @Schema(description = "Region name", example = "California")
  private String name;

  @Schema(description = "Region type", example = "STATE")
  private String regionType;

  @Schema(description = "Associated GeoShape ID", example = "660e8400-e29b-41d4-a716-446655440000")
  private String geoShapeId;

  @Min(value = 0, message = "Population must be non-negative")
  @Schema(description = "Population count", example = "39538223")
  private Long population;

  @Schema(description = "Active status", example = "true")
  private Boolean status;
}
