package com.cjrequena.sample.domain.model.vo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Radius in meters value object.
 */
@Getter
@Builder
@Jacksonized
@EqualsAndHashCode
public class RadiusVO implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  private final BigDecimal meters;

  private RadiusVO(BigDecimal meters) {
    if (meters == null || meters.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Radius must be positive");
    }
    this.meters = meters.setScale(2, RoundingMode.HALF_UP);
  }

  public static RadiusVO of(BigDecimal meters) {
    return new RadiusVO(meters);
  }

  public static RadiusVO of(double meters) {
    return new RadiusVO(BigDecimal.valueOf(meters));
  }

  public static RadiusVO ofKilometers(double kilometers) {
    return new RadiusVO(BigDecimal.valueOf(kilometers * 1000));
  }

  public double getKilometers() {
    return meters.doubleValue() / 1000.0;
  }

  @Override
  public String toString() {
    if (meters.doubleValue() < 1000) {
      return meters + " m";
    }
    return String.format("%.2f km", getKilometers());
  }
}
