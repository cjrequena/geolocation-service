package com.cjrequena.sample.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Summary DTO for GeoShape aggregate.
 *
 * <p>Lightweight representation used in lists and as nested references in other DTOs.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "GeoShape summary for lists")
public class GeoShapeSummaryDTO {

  @Schema(description = "GeoShape unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
  private String id;

  @Schema(description = "Shape name", example = "San Francisco Bay Area")
  private String name;

  @Schema(description = "Geometry type", example = "Polygon")
  private String geometryType;

  @Schema(description = "Active status", example = "true")
  private Boolean active;
}
