package com.cjrequena.sample.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

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

  @Schema(description = "Is this zone active", example = "true")
  private Boolean active;

  @Schema(description = "Custom metadata as key-value pairs")
  private Map<String, Object> metadata;

  @Schema(description = "Creation timestamp", example = "2024-06-01T12:00:00Z")
  private String createdAt;

  @Schema(description = "Last update timestamp", example = "2024-12-15T08:30:00Z")
  private String updatedAt;
}
