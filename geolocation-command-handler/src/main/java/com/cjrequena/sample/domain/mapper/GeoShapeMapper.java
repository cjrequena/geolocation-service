package com.cjrequena.sample.domain.mapper;

import com.cjrequena.sample.domain.model.aggregate.GeoShape;
import com.cjrequena.sample.domain.model.vo.*;
import com.cjrequena.sample.persistence.entity.GeoShapeEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Improved GeoShapeMapper with bidirectional mapping support.
 * Handles conversion between domain aggregates and persistence entities,
 * including complex nested value objects like GeometryVO and BoundVO.
 */
@Mapper(
  componentModel = "spring",
  unmappedTargetPolicy = ReportingPolicy.IGNORE,
  nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public abstract class GeoShapeMapper {

  @Autowired
  private ObjectMapper objectMapper;

  private final GeometryFactory geometryFactory = new GeometryFactory();

  // ==========================================
  // Domain -> Entity mapping
  // ==========================================

  /**
   * Converts a GeoShape domain aggregate to a GeoShapeEntity.
   *
   * @param domain the GeoShape domain object
   * @return the corresponding GeoShapeEntity
   */
  public GeoShapeEntity toEntity(GeoShape domain) {
    if (domain == null) {
      return null;
    }

    GeoShapeEntity entity = new GeoShapeEntity();

    // Map simple fields
    entity.setId(domain.getId());
    entity.setGeometryType(domain.getGeometryType());

    // Map geometry (GeometryVO -> JTS Point)
    if (domain.getGeometry() != null) {
      entity.setGeometry(mapGeometryVOToJTSPoint(domain.getGeometry()));
    }

    // Map center coordinates
    if (domain.getCenterCoordinates() != null) {
      entity.setCenterLatitude(domain.getCenterCoordinates().getLatitude());
      entity.setCenterLongitude(domain.getCenterCoordinates().getLongitude());
    }

    // Map radius
    if (domain.getRadius() != null) {
      entity.setRadiusMeters(domain.getRadius().getMeters());
    }

    // Map bounds (BoundVO -> JsonNode)
    if (domain.getBounds() != null) {
      entity.setBounds(domain.getBounds().toJsonNode(objectMapper));
    }

    // Map metadata (MetadataVO -> JsonNode)
    if (domain.getMetadata() != null) {
      entity.setMetadata(domain.getMetadata().getValue());
    }

    // Map audit info
    if (domain.getAuditInfo() != null) {
      entity.setCreatedAt(domain.getAuditInfo().getCreatedAt());
      entity.setUpdatedAt(domain.getAuditInfo().getUpdatedAt());
    }

    return entity;
  }

  // ==========================================
  // Entity -> Domain mapping
  // ==========================================

  /**
   * Converts a GeoShapeEntity to a GeoShape domain aggregate.
   *
   * @param entity the GeoShapeEntity
   * @return the corresponding GeoShape domain object
   */
  public GeoShape toDomain(GeoShapeEntity entity) {
    if (entity == null) {
      return null;
    }

    return GeoShape
      .builder()
      .id(entity.getId())
      .geometryType(entity.getGeometryType())
      .geometry(mapJTSPointToGeometryVO(entity.getGeometry()))
      .centerCoordinates(mapCenterCoordinates(entity))
      .radius(mapRadius(entity))
      .bounds(mapBounds(entity))
      .metadata(mapMetadata(entity))
      .auditInfo(mapAuditInfo(entity))
      .build();
  }

  // ==========================================
  // Helper methods for Entity -> Domain
  // ==========================================

  /**
   * Maps JTS Geometry to GeometryVO.
   * Handles both Point and other geometry types from JTS.
   */
  private GeometryVO mapJTSPointToGeometryVO(org.locationtech.jts.geom.Geometry geometry) {
    if (geometry == null) {
      return null;
    }

    // Get the coordinate from the geometry (works for Point and other types)
    Coordinate coordinate = geometry.getCoordinate();
    if (coordinate == null) {
      return null;
    }

    /*
     * JTS Coordinate convention:
     * x = longitude
     * y = latitude
     */
    CoordinateVO coordinateVO = CoordinateVO.of(coordinate.y, coordinate.x);
    return GeometryVO.ofCoordinates(coordinateVO);
  }

  /**
   * Maps center latitude/longitude to CoordinateVO.
   */
  private CoordinateVO mapCenterCoordinates(GeoShapeEntity entity) {
    if (entity.getCenterLatitude() == null || entity.getCenterLongitude() == null) {
      return null;
    }
    return CoordinateVO.of(entity.getCenterLatitude(), entity.getCenterLongitude());
  }

  /**
   * Maps radius meters to RadiusVO.
   */
  private RadiusVO mapRadius(GeoShapeEntity entity) {
    if (entity.getRadiusMeters() == null) {
      return null;
    }
    return RadiusVO.of(entity.getRadiusMeters());
  }

  /**
   * Maps JsonNode bounds to BoundVO.
   */
  private BoundVO mapBounds(GeoShapeEntity entity) {
    if (entity.getBounds() == null) {
      return null;
    }
    return BoundVO.ofJsonNode(entity.getBounds());
  }

  /**
   * Maps JsonNode metadata to MetadataVO.
   */
  private MetadataVO mapMetadata(GeoShapeEntity entity) {
    if (entity.getMetadata() == null) {
      return null;
    }
    return MetadataVO.of(entity.getMetadata());
  }

  /**
   * Maps audit timestamps to AuditInfoVO.
   */
  private AuditInfoVO mapAuditInfo(GeoShapeEntity entity) {
    if (entity.getCreatedAt() == null && entity.getUpdatedAt() == null) {
      return null;
    }
    return AuditInfoVO.of(entity.getCreatedAt(), entity.getUpdatedAt());
  }

  // ==========================================
  // Helper methods for Domain -> Entity
  // ==========================================

  /**
   * Maps GeometryVO to JTS Geometry (Point).
   * Extracts the centroid from the GeometryVO and creates a JTS Point.
   */
  private org.locationtech.jts.geom.Geometry mapGeometryVOToJTSPoint(GeometryVO geometryVO) {
    if (geometryVO == null) {
      return null;
    }

    CoordinateVO centroid = geometryVO.getCentroid();
    if (centroid == null) {
      return null;
    }

    // JTS uses (x, y) which corresponds to (longitude, latitude)
    Coordinate coordinate = new Coordinate(
      centroid.getLongitudeAsDouble(),
      centroid.getLatitudeAsDouble()
    );

    return geometryFactory.createPoint(coordinate);
  }

  /**
   * Alternative method to map specific geometry types to JTS Geometry (Point).
   * This method handles different geometry types explicitly.
   */
  private org.locationtech.jts.geom.Geometry mapGeometryVOToJTSPointDetailed(GeometryVO geometryVO) {
    if (geometryVO == null) {
      return null;
    }

    CoordinateVO coordinateVO;

    switch (geometryVO.getType()) {
      case POINT:
        coordinateVO = geometryVO.getPoint().getCoordinates();
        break;
      case CIRCLE:
        coordinateVO = geometryVO.getCircle().getCenter();
        break;
      case RECTANGLE:
        coordinateVO = geometryVO.getRectangle().getCenter();
        break;
      case POLYGON:
        coordinateVO = geometryVO.getPolygon().getCentroid();
        break;
      case LINE:
        // For lines, use the midpoint
        LineVO line = geometryVO.getLine();
        int midIndex = line.getPointCount() / 2;
        coordinateVO = line.getPoint(midIndex);
        break;
      default:
        throw new IllegalStateException("Unknown geometry type: " + geometryVO.getType());
    }

    Coordinate coordinate = new Coordinate(
      coordinateVO.getLongitudeAsDouble(),
      coordinateVO.getLatitudeAsDouble()
    );

    return geometryFactory.createPoint(coordinate);
  }
}
