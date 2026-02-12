package com.cjrequena.sample.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Summary DTO for Zone 
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Zone summary for lists")
public class ZoneSummaryDTO {

  @Schema(description = "Zone unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
  private String id;

  @Schema(description = "Zone name", example = "Golden Gate Park")
  private String name;

  @Schema(description = "Zone type", example = "PARK")
  private String zoneType;

  @Schema(description = "Active status", example = "true")
  private Boolean active;
}
