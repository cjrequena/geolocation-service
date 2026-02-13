package com.cjrequena.sample.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO for Area 
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Area details")
public class AreaResponseDTO {

  @Schema(description = "Area unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
  private String id;

  @Schema(description = "Area name", example = "Mission District")
  private String name;

  @Schema(description = "Area type", example = "DISTRICT")
  private String areaType;

  @Schema(description = "Postal code", example = "94110")
  private String postalCode;

  @Schema(description = "Population count", example = "48000")
  private Long population;

  @Schema(description = "Is this area active", example = "true")
  private Boolean active;

  @Schema(description = "Custom metadata as key-value pairs")
  private Map<String, Object> metadata;

  @Schema(description = "Creation timestamp", example = "2024-06-01T12:00:00Z")
  private String createdAt;

  @Schema(description = "Last update timestamp", example = "2024-12-15T08:30:00Z")
  private String updatedAt;
}
