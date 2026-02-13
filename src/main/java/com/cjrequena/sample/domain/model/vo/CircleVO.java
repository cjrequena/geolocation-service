package com.cjrequena.sample.domain.model.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

/**
 * Circle value object representing a circular geographic area.
 * Defined by a center point and radius.
 */
@Getter
@EqualsAndHashCode
public class CircleVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final CoordinateVO center;
    private final RadiusVO radius;

    private CircleVO(CoordinateVO center, RadiusVO radius) {
        if (center == null) {
            throw new IllegalArgumentException("Circle center cannot be null");
        }
        if (radius == null) {
            throw new IllegalArgumentException("Circle radius cannot be null");
        }
        this.center = center;
        this.radius = radius;
    }

    public static CircleVO of(CoordinateVO center, RadiusVO radius) {
        return new CircleVO(center, radius);
    }

    public static CircleVO of(double latitude, double longitude, double radiusMeters) {
        return new CircleVO(
          CoordinateVO.of(latitude, longitude),
          RadiusVO.of(radiusMeters)
        );
    }

    public static CircleVO ofKilometers(double latitude, double longitude, double radiusKm) {
        return new CircleVO(
          CoordinateVO.of(latitude, longitude),
            RadiusVO.ofKilometers(radiusKm)
        );
    }

    /**
     * Calculate the area of the circle in square kilometers.
     */
    public double getAreaInSquareKm() {
        double radiusKm = radius.getKilometers();
        return Math.PI * radiusKm * radiusKm;
    }

    /**
     * Calculate the circumference in kilometers.
     */
    public double getCircumferenceInKm() {
        return 2 * Math.PI * radius.getKilometers();
    }

    /**
     * Check if a point is within this circle.
     */
    public boolean contains(CoordinateVO point) {
        if (point == null) {
            return false;
        }
        PointVO centerPoint = PointVO.of(center);
        PointVO testPoint = PointVO.of(point);
        DistanceVO distance = centerPoint.distanceTo(testPoint);
        return distance.getMeters() <= radius.getMeters().doubleValue();
    }

    /**
     * Check if another circle intersects with this circle.
     */
    public boolean intersects(CircleVO other) {
        if (other == null) {
            return false;
        }
        PointVO thisCenter = PointVO.of(this.center);
        PointVO otherCenter = PointVO.of(other.center);
        DistanceVO distanceBetweenCenters = thisCenter.distanceTo(otherCenter);
        
        double sumOfRadii = this.radius.getMeters().doubleValue() + 
                           other.radius.getMeters().doubleValue();
        
        return distanceBetweenCenters.getMeters() <= sumOfRadii;
    }

    /**
     * Get center latitude.
     */
    public double getCenterLatitude() {
        return center.getLatitudeAsDouble();
    }

    /**
     * Get center longitude.
     */
    public double getCenterLongitude() {
        return center.getLongitudeAsDouble();
    }

  /**
   * Convert to WKT (Well-Known Text) format.
   * Note: WKT doesn't have native circle support
   * so we represent as Non-Standard WKT (Software Specific) CIRCLE (x x x)
   */
  public String toWKT() {
    return String.format("CIRCLE(%s %s %s)",
      center.getLongitude(),
      center.getLatitude(),
      radius.getMeters());
  }

    @Override
    public String toString() {
        return String.format("Circle[center=%s, radius=%s]", center, radius);
    }
}
