package com.cjrequena.sample.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for updating an existing GeoShape.
 *
 * <p>All fields are optional. The geometry itself is <b>immutable</b> — only
 * metadata, measurements, and active status can be updated.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update an existing GeoShape")
public class UpdateGeoShapeRequestDTO {

  @Size(min = 2, max = 200, message = "Shape name must be between 2 and 200 characters")
  @Schema(description = "Shape name", example = "San Francisco Bay Area")
  private String name;

  @Min(value = 0, message = "Area must be non-negative")
  @Schema(description = "Area in square meters", example = "1200000000.50")
  private Double areaSqMeters;

  @Min(value = 0, message = "Perimeter must be non-negative")
  @Schema(description = "Perimeter in meters", example = "250000.75")
  private Double perimeterMeters;

  @Min(value = 0, message = "Radius must be non-negative")
  @Schema(description = "Bounding box radius in meters", example = "50000.0")
  private Double radiusMeters;

  @Schema(description = "Custom metadata as key-value pairs")
  private Map<String, Object> metadata;

  @Schema(description = "Active status", example = "true")
  private Boolean active;
}
