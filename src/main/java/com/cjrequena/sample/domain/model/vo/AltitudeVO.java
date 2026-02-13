package com.cjrequena.sample.domain.model.vo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Altitude value object.
 */
@Getter
@EqualsAndHashCode
public class AltitudeVO implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  private final BigDecimal meters;

  @Builder
  private AltitudeVO(BigDecimal meters) {
    if (meters == null) {
      throw new IllegalArgumentException("Altitude cannot be null");
    }
    this.meters = meters.setScale(2, RoundingMode.HALF_UP);
  }

  public static AltitudeVO of(BigDecimal meters) {
    return AltitudeVO
      .builder()
      .meters(meters)
      .build();
  }

  public static AltitudeVO of(double meters) {
    return AltitudeVO
      .builder()
      .meters(BigDecimal.valueOf(meters))
      .build();
  }

  public boolean isAboveSeaLevel() {
    return meters.compareTo(BigDecimal.ZERO) > 0;
  }

  public boolean isBelowSeaLevel() {
    return meters.compareTo(BigDecimal.ZERO) < 0;
  }

  @Override
  public String toString() {
    return meters + " m";
  }
}
