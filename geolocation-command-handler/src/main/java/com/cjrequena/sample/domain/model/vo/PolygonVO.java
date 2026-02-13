package com.cjrequena.sample.domain.model.vo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Polygon value object representing a closed polygon geographic area.
 * Defined by an ordered list of coordinates forming the boundary.
 */
@Getter
@Builder
@Jacksonized
@EqualsAndHashCode
public class PolygonVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final int MIN_VERTICES = 3;
    
    private final List<CoordinateVO> vertices;

    private PolygonVO(List<CoordinateVO> vertices) {
        if (vertices == null || vertices.isEmpty()) {
            throw new IllegalArgumentException("Polygon vertices cannot be null or empty");
        }
        if (vertices.size() < MIN_VERTICES) {
            throw new IllegalArgumentException(
                "Polygon must have at least " + MIN_VERTICES + " vertices");
        }
        validateVertices(vertices);
        
        // Ensure polygon is closed (first and last points are the same)
        if (!vertices.get(0).equals(vertices.get(vertices.size() - 1))) {
            List<CoordinateVO> closedVertices = new ArrayList<>(vertices);
            closedVertices.add(vertices.get(0));
            this.vertices = Collections.unmodifiableList(closedVertices);
        } else {
            this.vertices = Collections.unmodifiableList(new ArrayList<>(vertices));
        }
    }

    public static PolygonVO of(List<CoordinateVO> vertices) {
        return new PolygonVO(vertices);
    }

    /**
     * Create a polygon from latitude/longitude pairs.
     */
    public static PolygonVO of(double[][] latLonPairs) {
        if (latLonPairs == null || latLonPairs.length < MIN_VERTICES) {
            throw new IllegalArgumentException(
                "Must provide at least " + MIN_VERTICES + " coordinate pairs");
        }
        
        List<CoordinateVO> vertices = new ArrayList<>();
        for (double[] pair : latLonPairs) {
            if (pair.length != 2) {
                throw new IllegalArgumentException("Each pair must have exactly 2 values [lat, lon]");
            }
            vertices.add(CoordinateVO.of(pair[0], pair[1]));
        }
        
        return new PolygonVO(vertices);
    }

    /**
     * Create a triangle polygon.
     */
    public static PolygonVO triangle(CoordinateVO p1, CoordinateVO p2, CoordinateVO p3) {
        List<CoordinateVO> vertices = new ArrayList<>();
        vertices.add(p1);
        vertices.add(p2);
        vertices.add(p3);
        return new PolygonVO(vertices);
    }

    /**
     * Get the number of vertices (excluding the closing vertex).
     */
    public int getVertexCount() {
        return vertices.size() - 1;
    }

    /**
     * Check if a point is inside the polygon using ray casting algorithm.
     */
    public boolean contains(CoordinateVO point) {
        if (point == null) {
            return false;
        }
        
        boolean inside = false;
        int n = vertices.size() - 1; // Exclude closing vertex
        
        for (int i = 0, j = n - 1; i < n; j = i++) {
            CoordinateVO vi = vertices.get(i);
            CoordinateVO vj = vertices.get(j);
            
            double xi = vi.getLongitudeAsDouble();
            double yi = vi.getLatitudeAsDouble();
            double xj = vj.getLongitudeAsDouble();
            double yj = vj.getLatitudeAsDouble();
            
            double testX = point.getLongitudeAsDouble();
            double testY = point.getLatitudeAsDouble();
            
            if (((yi > testY) != (yj > testY)) &&
                (testX < (xj - xi) * (testY - yi) / (yj - yi) + xi)) {
                inside = !inside;
            }
        }
        
        return inside;
    }

    /**
     * Calculate the bounding box of the polygon.
     */
    public RectangleVO getBoundingBox() {
        BigDecimal minLat = vertices.get(0).getLatitude();
        BigDecimal maxLat = vertices.get(0).getLatitude();
        BigDecimal minLon = vertices.get(0).getLongitude();
        BigDecimal maxLon = vertices.get(0).getLongitude();
        
        for (CoordinateVO vertex : vertices) {
            if (vertex.getLatitude().compareTo(minLat) < 0) {
                minLat = vertex.getLatitude();
            }
            if (vertex.getLatitude().compareTo(maxLat) > 0) {
                maxLat = vertex.getLatitude();
            }
            if (vertex.getLongitude().compareTo(minLon) < 0) {
                minLon = vertex.getLongitude();
            }
            if (vertex.getLongitude().compareTo(maxLon) > 0) {
                maxLon = vertex.getLongitude();
            }
        }
        
        return RectangleVO.of(
            CoordinateVO.of(minLat, minLon),
            CoordinateVO.of(maxLat, maxLon)
        );
    }

    /**
     * Calculate the centroid (center of mass) of the polygon.
     */
    public CoordinateVO getCentroid() {
        double sumLat = 0;
        double sumLon = 0;
        int count = vertices.size() - 1; // Exclude closing vertex
        
        for (int i = 0; i < count; i++) {
            sumLat += vertices.get(i).getLatitudeAsDouble();
            sumLon += vertices.get(i).getLongitudeAsDouble();
        }
        
        return CoordinateVO.of(sumLat / count, sumLon / count);
    }

    /**
     * Calculate the perimeter in kilometers.
     */
    public double getPerimeterInKm() {
        double perimeter = 0;
        
        for (int i = 0; i < vertices.size() - 1; i++) {
            PointVO p1 = PointVO.of(vertices.get(i));
            PointVO p2 = PointVO.of(vertices.get(i + 1));
            perimeter += p1.distanceTo(p2).getKilometers();
        }
        
        return perimeter;
    }

    /**
     * Calculate approximate area using shoelace formula (in square kilometers).
     * Note: This is an approximation for small polygons.
     */
    public double getApproximateAreaInSquareKm() {
        double area = 0;
        int n = vertices.size() - 1; // Exclude closing vertex
        
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            double x1 = vertices.get(i).getLongitudeAsDouble();
            double y1 = vertices.get(i).getLatitudeAsDouble();
            double x2 = vertices.get(j).getLongitudeAsDouble();
            double y2 = vertices.get(j).getLatitudeAsDouble();
            
            area += x1 * y2 - x2 * y1;
        }
        
        area = Math.abs(area) / 2.0;
        
        // Convert to square kilometers (rough approximation)
        double kmPerDegree = 111.32;
        return area * kmPerDegree * kmPerDegree;
    }

    /**
     * Check if this is a triangle.
     */
    public boolean isTriangle() {
        return getVertexCount() == 3;
    }

    /**
     * Check if this is a quadrilateral.
     */
    public boolean isQuadrilateral() {
        return getVertexCount() == 4;
    }

    /**
     * Convert to WKT (Well-Known Text) format.
     */
    public String toWKT() {
        String coords = vertices.stream()
            .map(c -> String.format("%s %s", c.getLongitude(), c.getLatitude()))
            .collect(Collectors.joining(", "));
        
        return String.format("POLYGON((%s))", coords);
    }

    /**
     * Convert to GeoJSON coordinates array format.
     */
    public String toGeoJSON() {
        String coords = vertices.stream()
            .map(c -> String.format("[%s, %s]", 
                c.getLongitude(), c.getLatitude()))
            .collect(Collectors.joining(", "));
        
        return String.format("[[%s]]", coords);
    }

    private void validateVertices(List<CoordinateVO> vertices) {
        for (CoordinateVO vertex : vertices) {
            if (vertex == null) {
                throw new IllegalArgumentException("Polygon vertices cannot contain null values");
            }
        }
    }

    @Override
    public String toString() {
        return String.format("Polygon[vertices=%d, perimeter=%.2f km]", 
            getVertexCount(), getPerimeterInKm());
    }
}
