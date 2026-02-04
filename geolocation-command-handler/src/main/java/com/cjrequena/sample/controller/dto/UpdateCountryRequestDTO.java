package com.cjrequena.sample.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing Country.
 *
 * <p>Used for PUT/PATCH endpoints. All fields are optional — clients send only
 * the fields they want to update. ISO codes are <b>not</b> included as they are
 * immutable after creation.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update an existing country")
public class UpdateCountryRequestDTO {

  @Size(min = 2, max = 100, message = "Country name must be between 2 and 100 characters")
  @Schema(description = "Country name", example = "United States")
  private String name;

  @Size(max = 10, message = "Phone code must not exceed 10 characters")
  @Pattern(regexp = "^\\+[0-9]{1,4}$", message = "Phone code must start with + followed by 1-4 digits")
  @Schema(description = "International dialing code", example = "+1")
  private String phoneCode;

  @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
  @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be three uppercase letters")
  @Schema(description = "ISO 4217 currency code", example = "USD")
  private String currencyCode;

  @Size(max = 100, message = "Capital name must not exceed 100 characters")
  @Schema(description = "Capital city name", example = "Washington D.C.")
  private String capital;

  @Min(value = 0, message = "Population must be a non-negative number")
  @Schema(description = "Population count", example = "331002651")
  private Long population;

  @Schema(description = "Active status — set to false to deactivate", example = "true")
  private Boolean isActive;
}
