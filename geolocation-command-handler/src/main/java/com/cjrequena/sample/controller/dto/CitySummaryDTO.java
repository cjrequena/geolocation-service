package com.cjrequena.sample.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Summary DTO for City 
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "City summary for lists")
public class CitySummaryDTO {

  @Schema(description = "City unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
  private String id;

  @Schema(description = "City name", example = "San Francisco")
  private String name;

  @Schema(description = "Is this a capital city", example = "false")
  private Boolean isCapital;

  @Schema(description = "Active status", example = "true")
  private Boolean isActive;
}
