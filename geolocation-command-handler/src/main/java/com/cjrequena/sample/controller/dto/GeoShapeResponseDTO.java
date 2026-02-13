package com.cjrequena.sample.controller.dto;

import com.cjrequena.sample.domain.model.enums.GeometryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO for GeoShape 
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

  @Schema(description = "Shape name", example = "San Francisco Bay Area", required = true)
  private String name;

  @Schema(description = "Geometry type", example = "Polygon", required = true)
  private GeometryType geometryType;

  @Schema(description = "Geometry in WKT format", example = "POLYGON((-122.5 37.5, -122.5 38.5, -121.5 38.5, -121.5 37.5, -122.5 37.5))", required = true)
  private String geometryWKT;

  @Schema(description = "Custom metadata as key-value pairs")
  private Map<String, Object> metadata;

  @Schema(description = "Active status", example = "true")
  private Boolean active;

  @Schema(description = "Creation timestamp", example = "2024-06-01T12:00:00Z")
  private String createdAt;

  @Schema(description = "Last update timestamp", example = "2024-12-15T08:30:00Z")
  private String updatedAt;
}
