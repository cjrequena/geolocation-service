package com.cjrequena.sample.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Zone 
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Zone details")
public class ZoneResponseDTO {

  @Schema(description = "Zone unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
  private String id;

  @Schema(description = "Zone name", example = "Golden Gate Park")
  private String name;

  @Schema(description = "Zone type", example = "PARK")
  private String zoneType;

  @Schema(description = "Postal code", example = "94121")
  private String postalCode;

  @Schema(description = "Parent area summary")
  private AreaSummaryDTO area;

  @Schema(description = "Active status", example = "true")
  private Boolean active;

  @Schema(description = "Creation timestamp", example = "2024-05-08T11:15:00Z")
  private String createdAt;

  @Schema(description = "Last update timestamp", example = "2024-10-22T09:00:00Z")
  private String updatedAt;
}
