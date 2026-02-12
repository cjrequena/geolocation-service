package com.cjrequena.sample.domain.model.vo;

import com.cjrequena.sample.domain.model.enums.GeometryType;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Enhanced Geometry value object that can represent any geometric shape.
 * Acts as a wrapper for Point, CircleVO, RectangleVO, PolygonVO, or LineVO shapes.
 *
 * This follows the Composite pattern to provide a unified interface for all geometry types.
 */
@Getter
@Builder
@Jacksonized
@EqualsAndHashCode
public class GeometryVO implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  private final GeometryType type;
  private final PointVO point;
  private final CircleVO circle;
  private final RectangleVO rectangle;
  private final PolygonVO polygon;
  private final LineVO line;

  @JsonCreator
  private GeometryVO(
    GeometryType type,
    PointVO point,
    CircleVO circle,
    RectangleVO rectangle,
    PolygonVO polygon,
    LineVO line) {

    this.type = type;
    this.point = point;
    this.circle = circle;
    this.rectangle = rectangle;
    this.polygon = polygon;
    this.line = line;

    validateGeometry();
  }

  /**
   * Create geometry of a Point.
   */
  public static GeometryVO ofPoint(PointVO point) {
    if (point == null) {
      throw new IllegalArgumentException("Point cannot be null");
    }

    return GeometryVO
      .builder()
      .type(GeometryType.POINT)
      .point(point)
      .build();
  }

  /**
   * Create geometry of CoordinateVO (convenience method).
   */
  public static GeometryVO ofCoordinates(CoordinateVO coordinates) {
    if (coordinates == null) {
      throw new IllegalArgumentException("CoordinateVO cannot be null");
    }
    return ofPoint(PointVO.of(coordinates));
  }

  /**
   * Create geometry of a CircleVO.
   */
  public static GeometryVO ofCircle(CircleVO circle) {
    if (circle == null) {
      throw new IllegalArgumentException("CircleVO cannot be null");
    }

    return GeometryVO
      .builder()
      .type(GeometryType.CIRCLE)
      .circle(circle)
      .build();
  }

  /**
   * Create geometry of center and radius (convenience method).
   */
  public static GeometryVO ofCircle(CoordinateVO center, RadiusVO radius) {
    if (center == null || radius == null) {
      throw new IllegalArgumentException("Center and radius cannot be null");
    }
    return ofCircle(CircleVO.of(center, radius));
  }

  /**
   * Create geometry of a RectangleVO.
   */
  public static GeometryVO ofRectangle(RectangleVO rectangle) {
    if (rectangle == null) {
      throw new IllegalArgumentException("RectangleVO cannot be null");
    }

    return GeometryVO
      .builder()
      .type(GeometryType.RECTANGLE)
      .rectangle(rectangle)
      .build();
  }

  /**
   * Create geometry of a PolygonVO.
   */
  public static GeometryVO ofPolygon(PolygonVO polygon) {
    if (polygon == null) {
      throw new IllegalArgumentException("PolygonVO cannot be null");
    }
    return GeometryVO
      .builder()
      .type(GeometryType.POLYGON)
      .polygon(polygon)
      .build();
  }

  /**
   * Create geometry of a LineVO.
   */
  public static GeometryVO ofLine(LineVO line) {
    if (line == null) {
      throw new IllegalArgumentException("LineVO cannot be null");
    }
    return GeometryVO
      .builder()
      .type(GeometryType.LINE)
      .line(line)
      .build();
  }

  /**
   * Create geometry of WKT (Well-Known Text) string.
   */
  public static GeometryVO ofWKT(String wkt) {
    if (wkt == null || wkt.trim().isEmpty()) {
      throw new IllegalArgumentException("WKT cannot be null or empty");
    }

    String upperWkt = wkt.trim().toUpperCase();

    if (upperWkt.startsWith("POINT")) {
      return parseWKTPoint(wkt);
    } else if (upperWkt.startsWith("LINESTRING")) {
      return parseWKTLineVOString(wkt);
    } else if (upperWkt.startsWith("POLYGON")) {
      return parseWKTPolygonVO(wkt);
    } else {
      throw new IllegalArgumentException("Unsupported WKT type: " + wkt);
    }
  }

  /**
   * Check if this is a Point geometry.
   */
  public boolean isPoint() {
    return type == GeometryType.POINT;
  }

  /**
   * Check if this is a Circle geometry.
   */
  public boolean isCircle() {
    return type == GeometryType.CIRCLE;
  }

  /**
   * Check if this is a Rectangle geometry.
   */
  public boolean isRectangle() {
    return type == GeometryType.RECTANGLE;
  }

  /**
   * Check if this is a Polygon geometry.
   */
  public boolean isPolygon() {
    return type == GeometryType.POLYGON;
  }

  /**
   * Check if this is a Line geometry.
   */
  public boolean isLine() {
    return type == GeometryType.LINE;
  }

  /**
   * Get the underlying geometry object (type-safe casting required).
   */
  public Object getGeometry() {
    switch (type) {
      case POINT:
        return point;
      case CIRCLE:
        return circle;
      case RECTANGLE:
        return rectangle;
      case POLYGON:
        return polygon;
      case LINE:
        return line;
      default:
        throw new IllegalStateException("Unknown geometry type: " + type);
    }
  }

  /**
   * Get the bounding box for this geometry.
   */
  public RectangleVO getBoundingBox() {
    switch (type) {
      case POINT:
        // Create a tiny rectangle around the point
        CoordinateVO coords = point.getCoordinates();
        return RectangleVO.of(
          coords.getLatitudeAsDouble() - 0.0001,
          coords.getLongitudeAsDouble() - 0.0001,
          coords.getLatitudeAsDouble() + 0.0001,
          coords.getLongitudeAsDouble() + 0.0001
        );
      case CIRCLE:
        // Create bounding box of circle's extent
        double latDelta = (circle.getRadius().getMeters().doubleValue() / 111320.0);
        double lonDelta = (circle.getRadius().getMeters().doubleValue() /
          (111320.0 * Math.cos(Math.toRadians(circle.getCenterLatitude()))));
        return RectangleVO.of(
          circle.getCenterLatitude() - latDelta,
          circle.getCenterLongitude() - lonDelta,
          circle.getCenterLatitude() + latDelta,
          circle.getCenterLongitude() + lonDelta
        );
      case RECTANGLE:
        return rectangle;
      case POLYGON:
        return polygon.getBoundingBox();
      case LINE:
        return line.getBoundingBox();
      default:
        throw new IllegalStateException("Unknown geometry type: " + type);
    }
  }

  /**
   * Check if this geometry contains a given point.
   */
  public boolean contains(CoordinateVO coordinates) {
    if (coordinates == null) {
      return false;
    }

    switch (type) {
      case POINT:
        return point.getCoordinates().equals(coordinates);
      case CIRCLE:
        return circle.contains(coordinates);
      case RECTANGLE:
        return rectangle.contains(coordinates);
      case POLYGON:
        return polygon.contains(coordinates);
      case LINE:
        // LineVOs don't contain points in 2D space
        return false;
      default:
        return false;
    }
  }

  /**
   * Calculate the area of this geometry in square kilometers.
   * Returns null for geometries that don't have area (Point, LineVO).
   */
  public Double getAreaInSquareKm() {
    switch (type) {
      case POINT:
        return null; // Points have no area
      case CIRCLE:
        return circle.getAreaInSquareKm();
      case RECTANGLE:
        return rectangle.getAreaInSquareKm();
      case POLYGON:
        return polygon.getApproximateAreaInSquareKm();
      case LINE:
        return null; // LineVOs have no area
      default:
        return null;
    }
  }

  /**
   * Calculate the perimeter/length of this geometry in kilometers.
   * For Point, returns 0. For LineVO, returns length. For areas, returns perimeter.
   */
  public Double getPerimeterOrLengthInKm() {
    switch (type) {
      case POINT:
        return 0.0;
      case CIRCLE:
        return circle.getCircumferenceInKm();
      case RECTANGLE:
        return 2 * (rectangle.getWidthInKm() + rectangle.getHeightInKm());
      case POLYGON:
        return polygon.getPerimeterInKm();
      case LINE:
        return line.getLengthInKm();
      default:
        return null;
    }
  }

  /**
   * Get the centroid (center point) of this geometry.
   */
  public CoordinateVO getCentroid() {
    switch (type) {
      case POINT:
        return point.getCoordinates();
      case CIRCLE:
        return circle.getCenter();
      case RECTANGLE:
        return rectangle.getCenter();
      case POLYGON:
        return polygon.getCentroid();
      case LINE:
        // For line, return midpoint
        int midIndex = line.getPointCount() / 2;
        return line.getPoint(midIndex);
      default:
        throw new IllegalStateException("Unknown geometry type: " + type);
    }
  }

  /**
   * Convert to WKT (Well-Known Text) format.
   */
  public String toWKT() {
    switch (type) {
      case POINT:
        return point.toWKT();
      case CIRCLE:
        // WKT doesn't have native circle, use point + comment
        return circle.toWKT() + " /* CIRCLE radius=" + circle.getRadius() + " */";
      case RECTANGLE:
        return rectangle.toWKT();
      case POLYGON:
        return polygon.toWKT();
      case LINE:
        return line.toWKT();
      default:
        throw new IllegalStateException("Unknown geometry type: " + type);
    }
  }

  /**
   * Convert to GeoJSON coordinates format (where applicable).
   */
  public String toGeoJSON() {
    switch (type) {
      case POINT:
        CoordinateVO coords = point.getCoordinates();
        return String.format("[%s, %s]",
          coords.getLongitude(), coords.getLatitude());
      case CIRCLE:
        // GeoJSON doesn't have native circle representation
        CoordinateVO center = circle.getCenter();
        return String.format("{\"type\":\"Point\",\"coordinates\":[%s,%s],\"radius\":%s}",
          center.getLongitude(), center.getLatitude(),
          circle.getRadius().getMeters());
      case RECTANGLE:
        // Convert rectangle to polygon for GeoJSON
        return convertRectangleVOToGeoJSONPolygonVO();
      case POLYGON:
        return polygon.toGeoJSON();
      case LINE:
        return line.toGeoJSON();
      default:
        throw new IllegalStateException("Unknown geometry type: " + type);
    }
  }

  /**
   * Get a human-readable description of this geometry.
   */
  public String getDescription() {
    switch (type) {
      case POINT:
        return String.format("Point at %s", point.getCoordinates());
      case CIRCLE:
        return String.format("CircleVO centered at %s with radius %s",
          circle.getCenter(), circle.getRadius());
      case RECTANGLE:
        return String.format("RectangleVO with area %.2f km²",
          rectangle.getAreaInSquareKm());
      case POLYGON:
        return String.format("PolygonVO with %d vertices and area %.2f km²",
          polygon.getVertexCount(), polygon.getApproximateAreaInSquareKm());
      case LINE:
        return String.format("LineVO with %d points and length %.2f km",
          line.getPointCount(), line.getLengthInKm());
      default:
        return "Unknown geometry";
    }
  }

  // Helper methods for WKT parsing

  private static GeometryVO parseWKTPoint(String wkt) {
    // Simple WKT point parser: "POINT(lon lat)"
    String coords = wkt.substring(wkt.indexOf('(') + 1, wkt.indexOf(')'));
    String[] parts = coords.trim().split("\\s+");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Invalid POINT WKT: " + wkt);
    }
    double lon = Double.parseDouble(parts[0]);
    double lat = Double.parseDouble(parts[1]);
    return ofPoint(PointVO.of(lat, lon));
  }

  private static GeometryVO parseWKTLineVOString(String wkt) {
    // Simple WKT linestring parser: "LINESTRING(lon1 lat1, lon2 lat2, ...)"
    String coords = wkt.substring(wkt.indexOf('(') + 1, wkt.indexOf(')'));
    String[] points = coords.trim().split(",");

    java.util.List<CoordinateVO> coordsList = new java.util.ArrayList<>();
    for (String point : points) {
      String[] parts = point.trim().split("\\s+");
      if (parts.length != 2) {
        throw new IllegalArgumentException("Invalid LINESTRING WKT: " + wkt);
      }
      double lon = Double.parseDouble(parts[0]);
      double lat = Double.parseDouble(parts[1]);
      coordsList.add(CoordinateVO.of(lat, lon));
    }

    return ofLine(LineVO.of(coordsList));
  }

  private static GeometryVO parseWKTPolygonVO(String wkt) {
    // Simple WKT polygon parser: "POLYGON((lon1 lat1, lon2 lat2, ...))"
    int start = wkt.indexOf("((") + 2;
    int end = wkt.indexOf("))");
    String coords = wkt.substring(start, end);
    String[] points = coords.trim().split(",");

    java.util.List<CoordinateVO> coordsList = new java.util.ArrayList<>();
    for (String point : points) {
      String[] parts = point.trim().split("\\s+");
      if (parts.length != 2) {
        throw new IllegalArgumentException("Invalid POLYGON WKT: " + wkt);
      }
      double lon = Double.parseDouble(parts[0]);
      double lat = Double.parseDouble(parts[1]);
      coordsList.add(CoordinateVO.of(lat, lon));
    }

    return ofPolygon(PolygonVO.of(coordsList));
  }

  private String convertRectangleVOToGeoJSONPolygonVO() {
    List<CoordinateVO> corners = rectangle.getCorners();
    StringBuilder sb = new StringBuilder("[[");
    for (int i = 0; i < corners.size(); i++) {
      if (i > 0)
        sb.append(", ");
      CoordinateVO c = corners.get(i);
      sb.append(String.format("[%s, %s]", c.getLongitude(), c.getLatitude()));
    }
    // Close the polygon
    CoordinateVO first = corners.get(0);
    sb.append(String.format(", [%s, %s]", first.getLongitude(), first.getLatitude()));
    sb.append("]]");
    return sb.toString();
  }

  private void validateGeometry() {
    int nonNullCount = 0;
    if (point != null)
      nonNullCount++;
    if (circle != null)
      nonNullCount++;
    if (rectangle != null)
      nonNullCount++;
    if (polygon != null)
      nonNullCount++;
    if (line != null)
      nonNullCount++;

    if (nonNullCount != 1) {
      throw new IllegalStateException("GeometryVO must contain exactly one geometry type");
    }
  }

  @Override
  public String toString() {
    return String.format("GeometryVO[type=%s, %s]", type, getDescription());
  }

}
