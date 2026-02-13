package com.cjrequena.sample.domain.model.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Rectangle value object representing a rectangular geographic area.
 * Defined by southwest and northeast corner coordinates (bounding box).
 */
@Getter
@EqualsAndHashCode
public class RectangleVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final CoordinateVO southWest;
    private final CoordinateVO northEast;

    private RectangleVO(CoordinateVO southWest, CoordinateVO northEast) {
        if (southWest == null || northEast == null) {
            throw new IllegalArgumentException("Rectangle corners cannot be null");
        }
        validateRectangle(southWest, northEast);
        this.southWest = southWest;
        this.northEast = northEast;
    }

    public static RectangleVO of(CoordinateVO southWest, CoordinateVO northEast) {
        return new RectangleVO(southWest, northEast);
    }

    public static RectangleVO of(
            double swLatitude, double swLongitude,
            double neLatitude, double neLongitude) {
        return new RectangleVO(
            CoordinateVO.of(swLatitude, swLongitude),
            CoordinateVO.of(neLatitude, neLongitude)
        );
    }

    /**
     * Create rectangle from center point and dimensions.
     */
    public static RectangleVO of(
            CoordinateVO center, 
            double widthMeters, 
            double heightMeters) {
        
        if (center == null) {
            throw new IllegalArgumentException("Center cannot be null");
        }
        if (widthMeters <= 0 || heightMeters <= 0) {
            throw new IllegalArgumentException("Width and height must be positive");
        }

        // Approximate degrees from meters
        double latDelta = (heightMeters / 2) / 111320.0; // 1 degree latitude ≈ 111.32 km
        double lonDelta = (widthMeters / 2) / (111320.0 * Math.cos(Math.toRadians(center.getLatitudeAsDouble())));

        double swLat = center.getLatitudeAsDouble() - latDelta;
        double swLon = center.getLongitudeAsDouble() - lonDelta;
        double neLat = center.getLatitudeAsDouble() + latDelta;
        double neLon = center.getLongitudeAsDouble() + lonDelta;

        return RectangleVO.of(swLat, swLon, neLat, neLon);
    }

    /**
     * Get all four corner coordinates.
     */
    public List<CoordinateVO> getCorners() {
        List<CoordinateVO> corners = new ArrayList<>();
        corners.add(southWest); // SW
        corners.add(CoordinateVO.of(southWest.getLatitude(), northEast.getLongitude())); // SE
        corners.add(northEast); // NE
        corners.add(CoordinateVO.of(northEast.getLatitude(), southWest.getLongitude())); // NW
        return corners;
    }

    /**
     * Get center point of the rectangle.
     */
    public CoordinateVO getCenter() {
        BigDecimal centerLat = southWest.getLatitude()
            .add(northEast.getLatitude())
            .divide(BigDecimal.valueOf(2), 6, java.math.RoundingMode.HALF_UP);
        
        BigDecimal centerLon = southWest.getLongitude()
            .add(northEast.getLongitude())
            .divide(BigDecimal.valueOf(2), 6, java.math.RoundingMode.HALF_UP);
        
        return CoordinateVO.of(centerLat, centerLon);
    }

    /**
     * Check if a point is within this rectangle.
     */
    public boolean contains(CoordinateVO point) {
        if (point == null) {
            return false;
        }
        
        return point.getLatitude().compareTo(southWest.getLatitude()) >= 0 &&
               point.getLatitude().compareTo(northEast.getLatitude()) <= 0 &&
               point.getLongitude().compareTo(southWest.getLongitude()) >= 0 &&
               point.getLongitude().compareTo(northEast.getLongitude()) <= 0;
    }

    /**
     * Check if this rectangle intersects with another rectangle.
     */
    public boolean intersects(RectangleVO other) {
        if (other == null) {
            return false;
        }
        
        return !(other.northEast.getLatitude().compareTo(this.southWest.getLatitude()) < 0 ||
                 other.southWest.getLatitude().compareTo(this.northEast.getLatitude()) > 0 ||
                 other.northEast.getLongitude().compareTo(this.southWest.getLongitude()) < 0 ||
                 other.southWest.getLongitude().compareTo(this.northEast.getLongitude()) > 0);
    }

    /**
     * Calculate approximate width in kilometers.
     */
    public double getWidthInKm() {
        PointVO sw = PointVO.of(southWest);
        CoordinateVO se = CoordinateVO.of(
            southWest.getLatitude(), 
            northEast.getLongitude()
        );
        PointVO sePoint = PointVO.of(se);
        return sw.distanceTo(sePoint).getKilometers();
    }

    /**
     * Calculate approximate height in kilometers.
     */
    public double getHeightInKm() {
        PointVO sw = PointVO.of(southWest);
        CoordinateVO nw = CoordinateVO.of(
            northEast.getLatitude(), 
            southWest.getLongitude()
        );
        PointVO nwPoint = PointVO.of(nw);
        return sw.distanceTo(nwPoint).getKilometers();
    }

    /**
     * Calculate approximate area in square kilometers.
     */
    public double getAreaInSquareKm() {
        return getWidthInKm() * getHeightInKm();
    }

    /**
     * Convert to WKT (Well-Known Text) POLYGON format.
     */
    public String toWKT() {
        return String.format("POLYGON((%s %s, %s %s, %s %s, %s %s, %s %s))",
            southWest.getLongitude(), southWest.getLatitude(),
            northEast.getLongitude(), southWest.getLatitude(),
            northEast.getLongitude(), northEast.getLatitude(),
            southWest.getLongitude(), northEast.getLatitude(),
            southWest.getLongitude(), southWest.getLatitude()
        );
    }

    /**
     * Convert to BoundsVO.
     */
    public BoundVO toBounds() {
        return BoundVO.of(northEast, southWest);
    }

    private void validateRectangle(CoordinateVO sw, CoordinateVO ne) {
        if (sw.getLatitude().compareTo(ne.getLatitude()) >= 0) {
            throw new IllegalArgumentException(
                "Southwest latitude must be less than northeast latitude");
        }
        if (sw.getLongitude().compareTo(ne.getLongitude()) >= 0) {
            throw new IllegalArgumentException(
                "Southwest longitude must be less than northeast longitude");
        }
    }

    @Override
    public String toString() {
        return String.format("Rectangle[SW=%s, NE=%s]", southWest, northEast);
    }
}
