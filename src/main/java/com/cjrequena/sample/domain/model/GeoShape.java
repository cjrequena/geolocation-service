package com.cjrequena.sample.domain.model;

import com.cjrequena.sample.domain.model.enums.GeometryType;
import com.cjrequena.sample.domain.model.vo.*;
import lombok.*;

import java.util.UUID;

/**
 * GeoShape Domain 
 *
 * Represents a geographic shape that can be a point, circle, rectangle, polygon, or line.
 * This is a rich domain model with business logic and validation.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeoShape extends Domain {

  private UUID id;
  private String name;
  private GeometryType geometryType;
  private GeometryVO geometry;
  private CoordinateVO centerCoordinates;
  private RadiusVO radius;
  private BoundVO bounds;
  private Boolean active;
  private MetadataVO metadata;
  private AuditInfoVO auditInfo;

  /**
   * Factory method to create a point shape.
   */
  public static GeoShape createPoint(UUID id, String name, CoordinateVO coordinates, MetadataVO metadata) {
    validatePointCreation(coordinates);

    return GeoShape.builder()
      .id(id)
      .name(name)
      .geometryType(GeometryType.POINT)
      .geometry(GeometryVO.ofCoordinates(coordinates))
      .centerCoordinates(null)
      .radius(null)
      .bounds(null)
      .active(Boolean.TRUE)
      .metadata(metadata != null ? metadata : MetadataVO.empty())
      .auditInfo(AuditInfoVO.create())
      .build();
  }

  /**
   * Factory method to create a circle shape.
   */
  public static GeoShape createCircle(UUID id, String name, CoordinateVO center, RadiusVO radius, MetadataVO metadata) {
    validateCircleCreation(center, radius);

    return GeoShape.builder()
      .id(id)
      .name(name)
      .geometryType(GeometryType.CIRCLE)
      .geometry(GeometryVO.ofCircle(center, radius))
      .centerCoordinates(center)
      .radius(radius)
      .bounds(null)
      .active(Boolean.TRUE)
      .metadata(metadata != null ? metadata : MetadataVO.empty())
      .auditInfo(AuditInfoVO.create())
      .build();
  }

  /**
   * Factory method to create a polygon shape.
   */
  public static GeoShape createPolygon(UUID id, String name, GeometryVO geometry, BoundVO bounds, MetadataVO metadata) {
    validatePolygonCreation(geometry);

    return GeoShape.builder()
      .id(id)
      .name(name)
      .geometryType(GeometryType.POLYGON)
      .geometry(geometry)
      .centerCoordinates(null)
      .radius(null)
      .bounds(bounds)
      .active(Boolean.TRUE)
      .metadata(metadata != null ? metadata : MetadataVO.empty())
      .auditInfo(AuditInfoVO.create())
      .build();
  }

  /**
   * Factory method to create a rectangle shape.
   */
  public static GeoShape createRectangle(UUID id, String name, GeometryVO geometry, BoundVO bounds, MetadataVO metadata) {
    validateRectangleCreation(geometry, bounds);

    return GeoShape.builder()
      .id(id)
      .name(name)
      .geometryType(GeometryType.RECTANGLE)
      .geometry(geometry)
      .centerCoordinates(null)
      .radius(null)
      .bounds(bounds)
      .active(Boolean.TRUE)
      .metadata(metadata != null ? metadata : MetadataVO.empty())
      .auditInfo(AuditInfoVO.create())
      .build();
  }

  /**
   * Factory method to create a line shape.
   */
  public static GeoShape createLine(UUID id, String name, GeometryVO geometry, MetadataVO metadata) {
    validateLineCreation(geometry);

    return GeoShape.builder()
      .id(id)
      .name(name)
      .geometryType(GeometryType.LINE)
      .geometry(geometry)
      .centerCoordinates(null)
      .radius(null)
      .bounds(null)
      .active(Boolean.TRUE)
      .metadata(metadata != null ? metadata : MetadataVO.empty())
      .auditInfo(AuditInfoVO.create())
      .build();
  }

  /**
   * Update the shape's geometry.
   */
  public void updateGeometry(GeometryVO newGeometry) {
    validateGeometryUpdate(newGeometry);
    this.geometry = newGeometry;
  }

  /**
   * Update metadata.
   */
  public void updateMetadata(MetadataVO newMetadata) {
    if (newMetadata == null) {
      throw new IllegalArgumentException("Metadata cannot be null");
    }
    this.metadata = newMetadata;
  }

  /**
   * Check if this is a circle shape.
   */
  public boolean isCircle() {
    return GeometryType.CIRCLE.equals(this.geometryType);
  }

  /**
   * Check if this is a point shape.
   */
  public boolean isPoint() {
    return GeometryType.POINT.equals(this.geometryType);
  }

  /**
   * Check if this is a polygon shape.
   */
  public boolean isPolygon() {
    return GeometryType.POLYGON.equals(this.geometryType);
  }

  /**
   * Check if this is a rectangle shape.
   */
  public boolean isRectangle() {
    return GeometryType.RECTANGLE.equals(this.geometryType);
  }

  /**
   * Check if this is a line shape.
   */
  public boolean isLine() {
    return GeometryType.LINE.equals(this.geometryType);
  }

  /**
   * Get the area covered by this shape (if applicable).
   */
  public Double getAreaInSquareKm() {
    if (isCircle() && radius != null) {
      return Math.PI * Math.pow(radius.getKilometers(), 2);
    }
    // For polygons, would need actual geometry calculation
    return null;
  }

  // Validation methods

  private static void validatePointCreation(CoordinateVO coordinates) {
    if (coordinates == null) {
      throw new IllegalArgumentException("Point must have coordinates");
    }
  }

  private static void validateCircleCreation(CoordinateVO center, RadiusVO radius) {
    if (center == null) {
      throw new IllegalArgumentException("Circle must have center coordinates");
    }
    if (radius == null || radius.getMeters() == null || radius.getMeters().longValue() <= 0) {
      throw new IllegalArgumentException("Circle must have a positive radius");
    }
  }

  private static void validatePolygonCreation(GeometryVO geometry) {
    if (geometry == null) {
      throw new IllegalArgumentException("Polygon must have geometry");
    }
  }

  private void validateGeometryUpdate(GeometryVO newGeometry) {
    if (newGeometry == null) {
      throw new IllegalArgumentException("Geometry cannot be null");
    }
  }

  private static void validateRectangleCreation(GeometryVO geometry, BoundVO bounds) {
    if (geometry == null) {
      throw new IllegalArgumentException("Rectangle must have geometry");
    }
    if (bounds == null) {
      throw new IllegalArgumentException("Rectangle must have bounds");
    }
  }

  private static void validateLineCreation(GeometryVO geometry) {
    if (geometry == null) {
      throw new IllegalArgumentException("Line must have geometry");
    }
  }

  /**
   * Activate the area.
   */
  public void activate() {
    this.active = Boolean.TRUE;
  }

  /**
   * Deactivate the area.
   */
  public void deactivate() {
    this.active = Boolean.FALSE;
  }

  /**
   * Check if area is active.
   */
  public boolean isActive() {
    return this.active != null && this.active.equals(Boolean.TRUE);
  }
}
