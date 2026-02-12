package com.cjrequena.sample.controller.dto;

import com.cjrequena.sample.domain.model.enums.LocationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for creating a new Location.
 *
 * <p>Latitude and longitude are required. Zone assignment is optional.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new Location")
public class CreateLocationRequestDTO {

  @NotBlank(message = "Location name is required")
  @Size(min = 2, max = 100, message = "Area name must be between 2 and 100 characters")
  @Schema(description = "Location name", example = "HOTEL", required = true)
  private String name;

  //@NotBlank(message = "Location type is required")
  //@Pattern(regexp = "^(POINT|POLYGON|CIRCLE|RECTANGLE|LINE)$", message = "Geometry type must be POINT|POLYGON|CIRCLE|RECTANGLE|LINE")
  @Schema(description = "Location type", example = "HOTEL")
  private LocationType locationType;

  @NotNull(message = "Latitude is required")
  @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
  @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
  @Schema(description = "Latitude in decimal degrees", example = "37.7749", required = true)
  private Double latitude;

  @NotNull(message = "Longitude is required")
  @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
  @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
  @Schema(description = "Longitude in decimal degrees", example = "-122.4194", required = true)
  private Double longitude;

  @DecimalMin(value = "0.0", message = "Altitude must be non-negative")
  @Schema(description = "Altitude in meters above sea level", example = "16.50")
  private Double altitudeMeters;

  @DecimalMin(value = "0.0", message = "Accuracy must be non-negative")
  @Schema(description = "GPS accuracy in meters", example = "3.75")
  private Double accuracyMeters;

  @Size(max = 500, message = "Address must not exceed 500 characters")
  @Schema(description = "Physical address", example = "Golden Gate Park, San Francisco")
  private String address;

  @Size(max = 20, message = "Postal code must not exceed 20 characters")
  @Schema(description = "Postal code", example = "94121")
  private String postalCode;

  @Schema(description = "Parent zone ID (if assigning to a zone)", example = "550e8400-e29b-41d4-a716-446655440000")
  private String zoneId;

  @Schema(description = "Is this a area active", example = "false")
  private Boolean active;

  @Schema(description = "Custom metadata as key-value pairs")
  private Map<String, Object> metadata;
}
