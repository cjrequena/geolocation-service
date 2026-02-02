package com.cjrequena.sample.domain.model.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Bounds value object for bounding box.
 */
@Getter
@EqualsAndHashCode
public class BoundVO implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  private final CoordinateVO northEast;
  private final CoordinateVO southWest;

  private BoundVO(CoordinateVO northEast, CoordinateVO southWest) {
    if (northEast == null || southWest == null) {
      throw new IllegalArgumentException("Bounds coordinates cannot be null");
    }
    this.northEast = northEast;
    this.southWest = southWest;
  }

  public static BoundVO of(CoordinateVO northEast, CoordinateVO southWest) {
    return new BoundVO(northEast, southWest);
  }

  public boolean contains(CoordinateVO point) {
    if (point == null) {
      return false;
    }
    return point.getLatitude().compareTo(southWest.getLatitude()) >= 0 &&
      point.getLatitude().compareTo(northEast.getLatitude()) <= 0 &&
      point.getLongitude().compareTo(southWest.getLongitude()) >= 0 &&
      point.getLongitude().compareTo(northEast.getLongitude()) <= 0;
  }

  @Override
  public String toString() {
    return String.format("Bounds[NE=%s, SW=%s]", northEast, southWest);
  }
}
