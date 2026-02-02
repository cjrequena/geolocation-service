package com.cjrequena.sample.domain.model.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * GeoPoint value object representing a precise geographic point.
 */
@Getter
@EqualsAndHashCode
public final class PointVO implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private static final double EARTH_RADIUS_KM = 6371.0;

  private static final BigDecimal MIN_LATITUDE = BigDecimal.valueOf(-90);
  private static final BigDecimal MAX_LATITUDE = BigDecimal.valueOf(90);
  private static final BigDecimal MIN_LONGITUDE = BigDecimal.valueOf(-180);
  private static final BigDecimal MAX_LONGITUDE = BigDecimal.valueOf(180);

  private final CoordinateVO coordinates;

  private PointVO(CoordinateVO coordinates) {
    if (coordinates == null) {
      throw new IllegalArgumentException("Point coordinates cannot be null");
    }
    this.validateLatitude(coordinates.getLatitude());
    this.validateLongitude(coordinates.getLongitude());
    this.coordinates = coordinates;
  }

  public static PointVO of(BigDecimal latitude, BigDecimal longitude) {
    return new PointVO(CoordinateVO.of(latitude, longitude));
  }

  public static PointVO of(double latitude, double longitude) {
    return new PointVO(CoordinateVO.of(latitude, longitude));
  }

  public static PointVO of(CoordinateVO coordinates) {
    Objects.requireNonNull(coordinates, "Coordinates cannot be null");
    return new PointVO(coordinates);
  }

  /**
   * Get latitude value.
   */
  public double getLatitude() {
    return coordinates.getLatitudeAsDouble();
  }

  /**
   * Get longitude value.
   */
  public double getLongitude() {
    return coordinates.getLongitudeAsDouble();
  }

  /**
   * Convert to WKT (Well-Known Text) format.
   */
  public String toWKT() {
    return String.format("POINT(%s %s)",
      coordinates.getLongitude(),
      coordinates.getLatitude());
  }

  /**
   * Calculate distance to another GeoPoint using the Haversine formula.
   */
  public DistanceVO distanceTo(PointVO other) {
    Objects.requireNonNull(other, "Other point cannot be null");

    double lat1Rad = Math.toRadians(this.coordinates.getLatitude().doubleValue());
    double lat2Rad = Math.toRadians(other.coordinates.getLatitude().doubleValue());
    double deltaLatRad = Math.toRadians(
      other.coordinates.getLatitude().subtract(this.coordinates.getLatitude()).doubleValue()
    );
    double deltaLonRad = Math.toRadians(
      other.coordinates.getLongitude().subtract(this.coordinates.getLongitude()).doubleValue()
    );

    double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2)
      + Math.cos(lat1Rad) * Math.cos(lat2Rad)
      * Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);

    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    double distanceKm = EARTH_RADIUS_KM * c;
    return DistanceVO.ofKilometers(distanceKm);
  }

  /**
   * Check if this point is within a given radius of another point.
   */
  public boolean isWithinRadius(PointVO center, RadiusVO radius) {
    if (center == null || radius == null) {
      return false;
    }
    return this.distanceTo(center)
      .getKilometers()
      <= radius.getKilometers();
  }

  private BigDecimal validateLatitude(BigDecimal latitude) {
    Objects.requireNonNull(latitude, "Latitude cannot be null");

    if (latitude.compareTo(MIN_LATITUDE) < 0 || latitude.compareTo(MAX_LATITUDE) > 0) {
      throw new IllegalArgumentException("Latitude must be between -90 and 90 degrees");
    }
    return latitude;
  }

  private BigDecimal validateLongitude(BigDecimal longitude) {
    Objects.requireNonNull(longitude, "Longitude cannot be null");

    if (longitude.compareTo(MIN_LONGITUDE) < 0 || longitude.compareTo(MAX_LONGITUDE) > 0) {
      throw new IllegalArgumentException("Longitude must be between -180 and 180 degrees");
    }
    return longitude;
  }

  @Override
  public String toString() {
    return String.format(
      "GeoPoint(%s, %s)",
      coordinates.getLatitude().setScale(6, RoundingMode.HALF_UP),
      coordinates.getLongitude().setScale(6, RoundingMode.HALF_UP)
    );
  }
}
