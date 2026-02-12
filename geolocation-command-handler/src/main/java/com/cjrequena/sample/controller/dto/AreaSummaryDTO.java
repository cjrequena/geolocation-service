package com.cjrequena.sample.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Summary DTO for Area 
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Area summary for lists")
public class AreaSummaryDTO {

  @Schema(description = "Area unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
  private String id;

  @Schema(description = "Area name", example = "Mission District")
  private String name;

  @Schema(description = "Area type", example = "DISTRICT")
  private String areaType;

  @Schema(description = "Active status", example = "true")
  private Boolean active;
}
