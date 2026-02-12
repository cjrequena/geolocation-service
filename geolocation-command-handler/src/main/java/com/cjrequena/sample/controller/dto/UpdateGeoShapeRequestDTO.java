package com.cjrequena.sample.controller.dto;

import com.cjrequena.sample.domain.model.enums.GeometryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

  @NotBlank(message = "Shape name is required")
  @Size(min = 2, max = 200, message = "Shape name must be between 2 and 200 characters")
  @Schema(description = "Shape name", example = "San Francisco Bay Area", required = true)
  private String name;

  @NotBlank(message = "Geometry type is required")
  @Pattern(regexp = "^(POINT|POLYGON|CIRCLE|RECTANGLE|LINE)$", message = "Geometry type must be POINT|POLYGON|CIRCLE|RECTANGLE|LINE")
  @Schema(description = "Geometry type", example = "Polygon", required = true)
  private GeometryType geometryType;

  @NotBlank(message = "Geometry WKT is required")
  @Schema(description = "Geometry in WKT format", example = "POLYGON((-122.5 37.5, -122.5 38.5, -121.5 38.5, -121.5 37.5, -122.5 37.5))", required = true)
  private String geometryWKT;

  @Schema(description = "Custom metadata as key-value pairs")
  private Map<String, Object> metadata;
}
