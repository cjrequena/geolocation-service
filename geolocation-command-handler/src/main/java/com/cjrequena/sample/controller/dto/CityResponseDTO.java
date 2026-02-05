package com.cjrequena.sample.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for City aggregate.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "City details")
public class CityResponseDTO {

  @Schema(description = "City unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
  private String id;

  @Schema(description = "City name", example = "San Francisco")
  private String name;

  @Schema(description = "Population count", example = "873965")
  private Long population;

  @Schema(description = "IANA time zone identifier", example = "America/Los_Angeles")
  private String timeZone;

  @Schema(description = "Is this a capital city", example = "false")
  private Boolean isCapital;

  @Schema(description = "Parent region summary")
  private RegionSummaryDTO region;

  @Schema(description = "Associated GeoShape summary (if any)")
  private GeoShapeSummaryDTO geoShape;

  @Schema(description = "Active status", example = "true")
  private Boolean isActive;

  @Schema(description = "Creation timestamp", example = "2024-04-12T07:30:00Z")
  private String createdAt;

  @Schema(description = "Last update timestamp", example = "2024-11-01T16:00:00Z")
  private String updatedAt;
}
