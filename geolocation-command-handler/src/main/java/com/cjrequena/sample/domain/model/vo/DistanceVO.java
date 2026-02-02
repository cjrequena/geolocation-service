package com.cjrequena.sample.domain.model.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Distance value object.
 */
@Getter
@EqualsAndHashCode
public class DistanceVO implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  private final double meters;

  private DistanceVO(double meters) {
    if (meters < 0) {
      throw new IllegalArgumentException("Distance cannot be negative");
    }
    this.meters = meters;
  }

  public static DistanceVO ofMeters(double meters) {
    return new DistanceVO(meters);
  }

  public static DistanceVO ofKilometers(double kilometers) {
    return new DistanceVO(kilometers * 1000);
  }

  public static DistanceVO ofMiles(double miles) {
    return new DistanceVO(miles * 1609.34);
  }

  public double getKilometers() {
    return meters / 1000.0;
  }

  public double getMiles() {
    return meters / 1609.34;
  }

  public boolean isGreaterThan(DistanceVO other) {
    return this.meters > other.meters;
  }

  public boolean isLessThan(DistanceVO other) {
    return this.meters < other.meters;
  }

  @Override
  public String toString() {
    if (meters < 1000) {
      return String.format("%.2f m", meters);
    }
    return String.format("%.2f km", getKilometers());
  }
}
