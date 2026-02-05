package com.cjrequena.sample.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for creating a new GeoShape.
 *
 * <p>Accepts geometry in WKT (Well-Known Text) format for easy integration with
 * GIS tools and PostGIS.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new GeoShape")
public class CreateGeoShapeRequestDTO {

  @NotBlank(message = "Shape name is required")
  @Size(min = 2, max = 200, message = "Shape name must be between 2 and 200 characters")
  @Schema(description = "Shape name", example = "San Francisco Bay Area", required = true)
  private String name;

  @NotBlank(message = "Geometry type is required")
  @Pattern(regexp = "^(Point|Polygon|MultiPolygon)$", message = "Geometry type must be Point, Polygon, or MultiPolygon")
  @Schema(description = "Geometry type", example = "Polygon", required = true)
  private String geometryType;

  @NotBlank(message = "Geometry WKT is required")
  @Schema(description = "Geometry in WKT format", example = "POLYGON((-122.5 37.5, -122.5 38.5, -121.5 38.5, -121.5 37.5, -122.5 37.5))", required = true)
  private String geometryWKT;

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
}
