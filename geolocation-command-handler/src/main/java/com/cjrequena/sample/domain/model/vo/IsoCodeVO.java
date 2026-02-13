package com.cjrequena.sample.domain.model.vo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.io.Serial;
import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * ISO Code value object for countries.
 */
@Builder
@Getter
@Jacksonized
@EqualsAndHashCode
public class IsoCodeVO implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;
  private static final Pattern ALPHA2_PATTERN = Pattern.compile("^[A-Z]{2}$");
  private static final Pattern ALPHA3_PATTERN = Pattern.compile("^[A-Z]{3}$");

  private final String alpha2;
  private final String alpha3;
  private final String numeric;

  private IsoCodeVO(String alpha2, String alpha3, String numeric) {
    validateAlpha2(alpha2);
    if (alpha3 != null) {
      validateAlpha3(alpha3);
    }
    this.alpha2 = alpha2;
    this.alpha3 = alpha3;
    this.numeric = numeric;
  }

  public static IsoCodeVO of(String alpha2, String alpha3, String numeric) {
    return new IsoCodeVO(alpha2, alpha3, numeric);
  }

  public static IsoCodeVO of(String alpha2) {
    return new IsoCodeVO(alpha2, null, null);
  }

  private void validateAlpha2(String alpha2) {
    if (alpha2 == null || !ALPHA2_PATTERN.matcher(alpha2).matches()) {
      throw new IllegalArgumentException("ISO Alpha-2 code must be 2 uppercase letters");
    }
  }

  private void validateAlpha3(String alpha3) {
    if (!ALPHA3_PATTERN.matcher(alpha3).matches()) {
      throw new IllegalArgumentException("ISO Alpha-3 code must be 3 uppercase letters");
    }
  }

  @Override
  public String toString() {
    return alpha2;
  }
}
