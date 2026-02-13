package com.cjrequena.sample.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Region 
 *
 * <p>Includes nested country and geoShape summaries to provide context without
 * circular references.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Region details")
public class RegionResponseDTO {

  @Schema(description = "Region unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
  private String id;

  @Schema(description = "Region name", example = "California")
  private String name;

  @Schema(description = "Region type", example = "STATE")
  private String regionType;

  @Schema(description = "Population count", example = "39538223")
  private Long population;

  @Schema(description = "Active status", example = "true")
  private Boolean status;

  @Schema(description = "Creation timestamp", example = "2024-02-01T09:00:00Z")
  private String createdAt;

  @Schema(description = "Last update timestamp", example = "2024-08-15T17:45:00Z")
  private String updatedAt;
}
