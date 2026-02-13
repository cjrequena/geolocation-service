package com.cjrequena.sample.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO for Country 
 *
 * <p>Used for GET endpoints. Flattens all value objects (IsoCodeVO, PopulationVO,
 * AuditInfoVO) into simple fields for easier JSON serialization.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Country details")
public class CountryResponseDTO {

  @Schema(description = "Country unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
  private String id;

  @Schema(description = "Country name", example = "United States")
  private String name;

  @Schema(description = "ISO 3166-1 alpha-2 code", example = "US")
  private String isoCodeAlpha2;

  @Schema(description = "ISO 3166-1 alpha-3 code", example = "USA")
  private String isoCodeAlpha3;

  @Schema(description = "ISO 3166-1 numeric code", example = "840")
  private String isoCodeNumeric;

  @Schema(description = "International dialing code", example = "+1")
  private String phoneCode;

  @Schema(description = "ISO 4217 currency code", example = "USD")
  private String currencyCode;

  @Schema(description = "Capital city name", example = "Washington D.C.")
  private String capital;

  @Schema(description = "Population count", example = "331002651")
  private Long population;

  @Schema(description = "Is this country active", example = "true")
  private Boolean active;

  @Schema(description = "Custom metadata as key-value pairs")
  private Map<String, Object> metadata;

  @Schema(description = "Creation timestamp", example = "2024-06-01T12:00:00Z")
  private String createdAt;

  @Schema(description = "Last update timestamp", example = "2024-12-15T08:30:00Z")
  private String updatedAt;
}
