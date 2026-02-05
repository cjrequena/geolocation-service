package com.cjrequena.sample.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Summary DTO for Country aggregate.
 *
 * <p>Lightweight representation for list endpoints where full detail is not needed.
 * Contains only the essential fields for display in tables or dropdowns.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Country summary for lists")
public class CountrySummaryDTO {

  @Schema(description = "Country unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
  private String id;

  @Schema(description = "Country name", example = "United States")
  private String name;

  @Schema(description = "ISO 3166-1 alpha-2 code", example = "US")
  private String isoCodeAlpha2;

  @Schema(description = "Active status", example = "true")
  private Boolean isActive;
}
