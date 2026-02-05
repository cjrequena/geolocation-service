package com.cjrequena.sample.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Summary DTO for Region aggregate.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Region summary for lists")
public class RegionSummaryDTO {

  @Schema(description = "Region unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
  private String id;

  @Schema(description = "Region name", example = "California")
  private String name;

  @Schema(description = "Region type", example = "STATE")
  private String regionType;

  @Schema(description = "Active status", example = "true")
  private Boolean status;
}
