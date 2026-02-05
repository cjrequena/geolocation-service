package com.cjrequena.sample.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO for GeoShape aggregate.
 *
 * <p>Used for GET endpoints. Flattens all geometry value objects into simple fields.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "GeoShape details")
public class GeoShapeResponseDTO {

  @Schema(description = "GeoShape unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
  private String id;

  @Schema(description = "Shape name", example = "San Francisco Bay Area")
  private String name;

  @Schema(description = "Geometry type", example = "Polygon", allowableValues = {"Point", "Polygon", "MultiPolygon"})
  private String geometryType;

  @Schema(description = "Area in square meters", example = "1200000000.50")
  private Double areaSqMeters;

  @Schema(description = "Perimeter in meters", example = "250000.75")
  private Double perimeterMeters;

  @Schema(description = "Bounding box radius in meters", example = "50000.0")
  private Double radiusMeters;

  @Schema(description = "Custom metadata as key-value pairs")
  private Map<String, Object> metadata;

  @Schema(description = "Active status", example = "true")
  private Boolean active;

  @Schema(description = "Creation timestamp", example = "2024-06-01T12:00:00Z")
  private String createdAt;

  @Schema(description = "Last update timestamp", example = "2024-12-15T08:30:00Z")
  private String updatedAt;
}
