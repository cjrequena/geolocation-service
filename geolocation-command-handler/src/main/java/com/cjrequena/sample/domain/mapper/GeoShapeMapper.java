package com.cjrequena.sample.domain.mapper;

import com.cjrequena.sample.domain.model.aggregate.GeoShape;
import com.cjrequena.sample.domain.model.vo.*;
import com.cjrequena.sample.persistence.entity.GeoShapeEntity;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.mapstruct.*;

import java.math.BigDecimal;

/**
 * Pure MapStruct interface mapper for bidirectional conversion between
 * {@link GeoShape} domain aggregates and {@link GeoShapeEntity} persistence entities.
 *
 * <p>All field-level mappings are declared via {@code @Mapping}. Conversions that
 * cannot be expressed as simple source→target path expressions (e.g. JTS geometry
 * construction, JsonNode serialization) are handled by {@code @AfterMapping} hooks
 * that receive the partially-built target and fill in the remaining fields.</p>
 */
@Mapper(
  componentModel = "spring",
  unmappedTargetPolicy = ReportingPolicy.IGNORE,
  nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public abstract class GeoShapeMapper {

//  @Autowired
//  private ObjectMapper objectMapper;

  private final GeometryFactory geometryFactory = new GeometryFactory();

  // ==========================================
  // Domain -> Entity
  // ==========================================

  /**
   * Maps a {@link GeoShape} domain aggregate to a {@link GeoShapeEntity}.
   *
   * <p>Simple scalar fields are wired by the annotations below.  The
   * {@link #populateEntityComplexFields} hook runs afterwards and fills every
   * field that requires a custom conversion (geometry, bounds, metadata).</p>
   */
  @Mapping(target = "id",              source = "id")
  @Mapping(target = "geometryType",    source = "geometryType")
  @Mapping(target = "centerLatitude",  source = "centerCoordinates.latitude")
  @Mapping(target = "centerLongitude", source = "centerCoordinates.longitude")
  @Mapping(target = "radiusMeters",    source = "radius.meters")
  @Mapping(target = "createdAt",       source = "auditInfo.createdAt")
  @Mapping(target = "updatedAt",       source = "auditInfo.updatedAt")
  // These three fields have no direct path mapping; the @AfterMapping hook handles them.
  @Mapping(target = "geometry",        ignore = true)
  @Mapping(target = "bounds",          ignore = true)
  @Mapping(target = "metadata",        ignore = true)
  public abstract GeoShapeEntity toEntity(GeoShape domain);

  /**
   * Fills the fields on {@link GeoShapeEntity} that require helper-level
   * conversions and cannot be expressed as a simple source path.
   */
  @AfterMapping
  protected void populateEntityComplexFields(GeoShape domain, @MappingTarget GeoShapeEntity entity) {
    if (domain == null) {
      return;
    }

    // GeometryVO  ->  JTS Point
    if (domain.getGeometry() != null) {
      entity.setGeometry(geometryVOToJTSPoint(domain.getGeometry()));
    }

    // BoundVO  ->  JsonNode
    if (domain.getBounds() != null) {
      entity.setBounds(domain.getBounds().toJsonNode());
    }

    // MetadataVO  ->  JsonNode
    if (domain.getMetadata() != null) {
      entity.setMetadata(domain.getMetadata().getValue());
    }
  }

  // ==========================================
  // Entity -> Domain
  // ==========================================

  /**
   * Maps a {@link GeoShapeEntity} to a {@link GeoShape} domain aggregate.
   *
   * <p>Simple scalar fields are wired by the annotations below.  The
   * {@link #populateDomainComplexFields} hook runs afterwards and fills every
   * value-object field that requires a custom conversion.</p>
   */
  @Mapping(target = "id",              source = "id")
  @Mapping(target = "geometryType",    source = "geometryType")
  // These fields have no direct 1-to-1 target path; the @AfterMapping hook builds them.
  @Mapping(target = "geometry",           ignore = true)
  @Mapping(target = "centerCoordinates",  ignore = true)
  @Mapping(target = "radius",             ignore = true)
  @Mapping(target = "bounds",             ignore = true)
  @Mapping(target = "metadata",           ignore = true)
  @Mapping(target = "auditInfo",          ignore = true)
  public abstract GeoShape toDomain(GeoShapeEntity entity);

  /**
   * Fills the value-object fields on {@link GeoShape} that require
   * factory-method or custom construction.
   */
  @AfterMapping
  protected void populateDomainComplexFields(GeoShapeEntity entity, @MappingTarget GeoShape domain) {
    if (entity == null) {
      return;
    }

    // JTS Point  ->  GeometryVO
    domain.setGeometry(jtsPointToGeometryVO(entity.getGeometry()));

    // centerLatitude + centerLongitude  ->  CoordinateVO
    domain.setCenterCoordinates(coordinateVOFrom(entity.getCenterLatitude(), entity.getCenterLongitude()));

    // radiusMeters  ->  RadiusVO
    if (entity.getRadiusMeters() != null) {
      domain.setRadius(RadiusVO.of(entity.getRadiusMeters()));
    }

    // JsonNode  ->  BoundVO
    if (entity.getBounds() != null) {
      domain.setBounds(BoundVO.ofJsonNode(entity.getBounds()));
    }

    // JsonNode  ->  MetadataVO
    if (entity.getMetadata() != null) {
      domain.setMetadata(MetadataVO.of(entity.getMetadata()));
    }

    // createdAt + updatedAt  ->  AuditInfoVO
    if (entity.getCreatedAt() != null || entity.getUpdatedAt() != null) {
      domain.setAuditInfo(AuditInfoVO.of(entity.getCreatedAt(), entity.getUpdatedAt()));
    }
  }

  // ==========================================
  // Private conversion helpers
  // ==========================================

  /**
   * Converts a {@link GeometryVO} to a JTS {@link org.locationtech.jts.geom.Point}
   * using the VO's centroid as the representative coordinate.
   *
   * <p>JTS convention: {@code x = longitude, y = latitude}.</p>
   */
  private org.locationtech.jts.geom.Geometry geometryVOToJTSPoint(GeometryVO geometryVO) {
    if (geometryVO == null) {
      return null;
    }

    CoordinateVO centroid = geometryVO.getCentroid();
    if (centroid == null) {
      return null;
    }

    return geometryFactory.createPoint(
      new Coordinate(centroid.getLongitudeAsDouble(), centroid.getLatitudeAsDouble())
    );
  }

  /**
   * Converts a JTS {@link org.locationtech.jts.geom.Geometry} back to a
   * {@link GeometryVO} by reading its first coordinate.
   *
   * <p>JTS convention: {@code x = longitude, y = latitude}.</p>
   */
  private GeometryVO jtsPointToGeometryVO(org.locationtech.jts.geom.Geometry geometry) {
    if (geometry == null) {
      return null;
    }

    Coordinate coordinate = geometry.getCoordinate();
    if (coordinate == null) {
      return null;
    }

    // x = longitude, y = latitude
    return GeometryVO.ofCoordinates(CoordinateVO.of(coordinate.y, coordinate.x));
  }

  /**
   * Builds a {@link CoordinateVO} from separate latitude / longitude values.
   * Returns {@code null} when either value is missing.
   */
  private CoordinateVO coordinateVOFrom(Double latitude, Double longitude) {
    if (latitude == null || longitude == null) {
      return null;
    }
    return CoordinateVO.of(latitude, longitude);
  }

  /**
   * Builds a {@link CoordinateVO} from separate latitude / longitude values.
   * Returns {@code null} when either value is missing.
   */
  private CoordinateVO coordinateVOFrom(BigDecimal latitude, BigDecimal longitude) {
    if (latitude == null || longitude == null) {
      return null;
    }
    return CoordinateVO.of(latitude, longitude);
  }
}
