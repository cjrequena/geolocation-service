package com.cjrequena.sample.domain.model.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Line value object representing a line string (path) through multiple points.
 * Also known as LineString or Polyline in GIS terminology.
 */
@Getter
@EqualsAndHashCode
public class LineVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int MIN_POINTS = 2;
    
    private final List<CoordinateVO> points;

    private LineVO(List<CoordinateVO> points) {
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("Line points cannot be null or empty");
        }
        if (points.size() < MIN_POINTS) {
            throw new IllegalArgumentException(
                "Line must have at least " + MIN_POINTS + " points");
        }
        validatePoints(points);
        this.points = Collections.unmodifiableList(new ArrayList<>(points));
    }

    public static LineVO of(List<CoordinateVO> points) {
        return new LineVO(points);
    }

    /**
     * Create a line from latitude/longitude pairs.
     */
    public static LineVO of(double[][] latLonPairs) {
        if (latLonPairs == null || latLonPairs.length < MIN_POINTS) {
            throw new IllegalArgumentException(
                "Must provide at least " + MIN_POINTS + " coordinate pairs");
        }
        
        List<CoordinateVO> points = new ArrayList<>();
        for (double[] pair : latLonPairs) {
            if (pair.length != 2) {
                throw new IllegalArgumentException("Each pair must have exactly 2 values [lat, lon]");
            }
            points.add(CoordinateVO.of(pair[0], pair[1]));
        }
        
        return new LineVO(points);
    }

    /**
     * Create a simple line between two points.
     */
    public static LineVO between(CoordinateVO start, CoordinateVO end) {
        List<CoordinateVO> points = new ArrayList<>();
        points.add(start);
        points.add(end);
        return new LineVO(points);
    }

    /**
     * Create a line from multiple coordinates.
     */
    public static LineVO of(CoordinateVO... coordinates) {
        if (coordinates == null || coordinates.length < MIN_POINTS) {
            throw new IllegalArgumentException(
                "Must provide at least " + MIN_POINTS + " coordinates");
        }
        List<CoordinateVO> points = new ArrayList<>();
        for (CoordinateVO coord : coordinates) {
            points.add(coord);
        }
        return new LineVO(points);
    }

    /**
     * Get the number of points in the line.
     */
    public int getPointCount() {
        return points.size();
    }

    /**
     * Get the start point of the line.
     */
    public CoordinateVO getStart() {
        return points.get(0);
    }

    /**
     * Get the end point of the line.
     */
    public CoordinateVO getEnd() {
        return points.get(points.size() - 1);
    }

    /**
     * Get a specific point by index.
     */
    public CoordinateVO getPoint(int index) {
        if (index < 0 || index >= points.size()) {
            throw new IndexOutOfBoundsException(
                "Point index " + index + " is out of bounds [0, " + (points.size() - 1) + "]");
        }
        return points.get(index);
    }

    /**
     * Calculate the total length of the line in kilometers.
     */
    public double getLengthInKm() {
        double totalLength = 0;
        
        for (int i = 0; i < points.size() - 1; i++) {
            PointVO p1 = PointVO.of(points.get(i));
            PointVO p2 = PointVO.of(points.get(i + 1));
            totalLength += p1.distanceTo(p2).getKilometers();
        }
        
        return totalLength;
    }

    /**
     * Calculate the length of a specific segment.
     */
    public double getSegmentLengthInKm(int segmentIndex) {
        if (segmentIndex < 0 || segmentIndex >= points.size() - 1) {
            throw new IndexOutOfBoundsException(
                "Segment index " + segmentIndex + " is out of bounds [0, " + (points.size() - 2) + "]");
        }
        
        PointVO p1 = PointVO.of(points.get(segmentIndex));
        PointVO p2 = PointVO.of(points.get(segmentIndex + 1));
        return p1.distanceTo(p2).getKilometers();
    }

    /**
     * Get the bounding box of the line.
     */
    public RectangleVO getBoundingBox() {
        BigDecimal minLat = points.get(0).getLatitude();
        BigDecimal maxLat = points.get(0).getLatitude();
        BigDecimal minLon = points.get(0).getLongitude();
        BigDecimal maxLon = points.get(0).getLongitude();
        
        for (CoordinateVO point : points) {
            if (point.getLatitude().compareTo(minLat) < 0) {
                minLat = point.getLatitude();
            }
            if (point.getLatitude().compareTo(maxLat) > 0) {
                maxLat = point.getLatitude();
            }
            if (point.getLongitude().compareTo(minLon) < 0) {
                minLon = point.getLongitude();
            }
            if (point.getLongitude().compareTo(maxLon) > 0) {
                maxLon = point.getLongitude();
            }
        }
        
        return RectangleVO.of(
            CoordinateVO.of(minLat, minLon),
            CoordinateVO.of(maxLat, maxLon)
        );
    }

    /**
     * Check if the line is closed (forms a loop).
     */
    public boolean isClosed() {
        return points.get(0).equals(points.get(points.size() - 1));
    }

    /**
     * Check if the line is a simple straight segment (only 2 points).
     */
    public boolean isSimpleSegment() {
        return points.size() == 2;
    }

    /**
     * Get the number of segments in the line.
     */
    public int getSegmentCount() {
        return points.size() - 1;
    }

    /**
     * Calculate the straight-line distance between start and end points.
     */
    public double getStraightDistanceInKm() {
        PointVO start = PointVO.of(getStart());
        PointVO end = PointVO.of(getEnd());
        return start.distanceTo(end).getKilometers();
    }

    /**
     * Calculate how much longer the line path is compared to straight distance.
     * Returns 1.0 if the line is straight, >1.0 if it has curves.
     */
    public double getTortuosity() {
        double pathLength = getLengthInKm();
        double straightDistance = getStraightDistanceInKm();
        
        if (straightDistance == 0) {
            return 1.0;
        }
        
        return pathLength / straightDistance;
    }

    /**
     * Get a subline from startIndex to endIndex (inclusive).
     */
    public LineVO getSubLine(int startIndex, int endIndex) {
        if (startIndex < 0 || endIndex >= points.size() || startIndex >= endIndex) {
            throw new IllegalArgumentException("Invalid subline indices");
        }
        
        List<CoordinateVO> subPoints = new ArrayList<>(
            points.subList(startIndex, endIndex + 1));
        return new LineVO(subPoints);
    }

    /**
     * Reverse the direction of the line.
     */
    public LineVO reverse() {
        List<CoordinateVO> reversed = new ArrayList<>(points);
        Collections.reverse(reversed);
        return new LineVO(reversed);
    }

    /**
     * Convert to WKT (Well-Known Text) format.
     */
    public String toWKT() {
        String coords = points.stream()
            .map(c -> String.format("%s %s", c.getLongitude(), c.getLatitude()))
            .collect(Collectors.joining(", "));
        
        return String.format("LINESTRING(%s)", coords);
    }

    /**
     * Convert to GeoJSON coordinates array format.
     */
    public String toGeoJSON() {
        String coords = points.stream()
            .map(c -> String.format("[%s, %s]", 
                c.getLongitude(), c.getLatitude()))
            .collect(Collectors.joining(", "));
        
        return String.format("[%s]", coords);
    }

    /**
     * Convert to encoded polyline format (simplified).
     */
    public String toEncodedPolyline() {
        // This would typically use the Google Polyline encoding algorithm
        // For now, return a simplified representation
        return points.stream()
            .map(c -> String.format("%.6f,%.6f", 
                c.getLatitudeAsDouble(), c.getLongitudeAsDouble()))
            .collect(Collectors.joining("|"));
    }

    private void validatePoints(List<CoordinateVO> points) {
        for (CoordinateVO point : points) {
            if (point == null) {
                throw new IllegalArgumentException("Line points cannot contain null values");
            }
        }
    }

    @Override
    public String toString() {
        return String.format("Line[points=%d, length=%.2f km, tortuosity=%.2f]", 
            getPointCount(), getLengthInKm(), getTortuosity());
    }
}
