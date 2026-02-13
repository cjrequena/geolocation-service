package com.cjrequena.sample.domain.mapper;

import com.cjrequena.sample.controller.dto.GeoShapeRequestDTO;
import com.cjrequena.sample.controller.dto.GeoShapeResponseDTO;
import com.cjrequena.sample.controller.dto.LocationResponseDTO;
import com.cjrequena.sample.domain.model.GeoShape;
import com.cjrequena.sample.domain.model.Location;
import com.cjrequena.sample.domain.model.enums.GeometryType;
import com.cjrequena.sample.domain.model.vo.*;
import com.cjrequena.sample.persistence.entity.GeoShapeEntity;
import com.cjrequena.sample.shared.common.util.WKTParserUtil;
import org.locationtech.jts.geom.*;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author cjrequena
 */
@Mapper(
  componentModel = "spring",
  unmappedTargetPolicy = ReportingPolicy.IGNORE,
  nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public abstract class GeoShapeMapper {

  private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

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
  @Mapping(target = "id", source = "id")
  @Mapping(target = "geometryType", source = "geometryType")
  @Mapping(target = "centerLatitude", source = "centerCoordinates.latitude")
  @Mapping(target = "centerLongitude", source = "centerCoordinates.longitude")
  @Mapping(target = "radiusMeters", source = "radius.meters")
  @Mapping(target = "active", source = "active")
  @Mapping(target = "createdBy", source = "auditInfo.createdBy")   // AuditInfoVO
  @Mapping(target = "updatedBy", source = "auditInfo.updatedBy")
  @Mapping(target = "createdAt", source = "auditInfo.createdAt")
  @Mapping(target = "updatedAt", source = "auditInfo.updatedAt")
  // These three fields have no direct path mapping; the @AfterMapping hook handles them.
  @Mapping(target = "geometry", ignore = true)
  @Mapping(target = "bounds", ignore = true)
  @Mapping(target = "metadata", ignore = true)
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


    // GeometryVO  ->  JTS Geometry
    if (domain.getGeometry() != null) {
      final String wkt = domain.getGeometry().toWKT();
      final Geometry geometry = WKTParserUtil.fromWKT(wkt, domain.getGeometryType());
      entity.setGeometry(geometry);
    }

    // BoundVO  ->  JsonNode
    if (domain.getBounds() != null) {
      entity.setBounds(domain.getBounds().toJsonNode());
    }

    // MetadataVO  ->  JsonNode
    if (domain.getMetadata() != null) {
      entity.setMetadata(domain.getMetadata().getJsonNode());
    }
  }

  // ==========================================
  // Entity -> Domain
  // ==========================================

  /**
   * Maps a {@link GeoShapeEntity} to a {@link GeoShape} domain 
   *
   * <p>Simple scalar fields are wired by the annotations below.  The
   * {@link #populateDomainComplexFields} hook runs afterwards and fills every
   * value-object field that requires a custom conversion.</p>
   */
  @Mapping(target = "id", source = "id")
  @Mapping(target = "geometryType", source = "geometryType")
  @Mapping(target = "active", source = "active")
  // These fields have no direct 1-to-1 target path; the @AfterMapping hook builds them.
  @Mapping(target = "geometry", ignore = true)
  @Mapping(target = "centerCoordinates", ignore = true)
  @Mapping(target = "radius", ignore = true)
  @Mapping(target = "bounds", ignore = true)
  @Mapping(target = "metadata", ignore = true)
  @Mapping(target = "auditInfo", ignore = true)
  public abstract GeoShape toDomain(GeoShapeEntity entity);

  /**
   * Converts a list of {@link GeoShapeEntity} into a list of {@link GeoShape} domain aggregates.
   * Each entity is converted using {@link #toDomain(GeoShapeEntity)}.
   */
  public abstract List<GeoShape> toDomainList(List<GeoShapeEntity> entityList);

  /**
   * Fills the value-object fields on {@link GeoShape} that require
   * factory-method or custom construction.
   */
  @AfterMapping
  protected void populateDomainComplexFields(GeoShapeEntity entity, @MappingTarget GeoShape domain) {
    if (entity == null) {
      return;
    }

    // JTS Geometry  ->  GeometryVO
    if (entity.getGeometry() != null) {
      final String wkt = entity.getGeometry().toText();
      domain.setGeometry(GeometryVO.ofWKT(wkt));
    }

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

    // ── AuditInfoVO ──────────────────────
    if (entity.getCreatedAt() != null || entity.getUpdatedAt() != null) {
      domain.setAuditInfo(AuditInfoVO.of(entity.getCreatedAt(), entity.getUpdatedAt(), entity.getCreatedBy(), entity.getUpdatedBy()));
    }

  }

  // ================================================================
  // Domain  →  DTO
  // ================================================================

  /**
   * Converts a {@link Location} domain aggregate into a {@link LocationResponseDTO}.
   */
  @Mapping(target = "id", expression = "java(domain.getId() != null ? domain.getId().toString() : null)")
  @Mapping(target = "name", source = "name")
  @Mapping(target = "geometryType", source = "geometryType")
  @Mapping(target = "geometryWKT", ignore = true)
  @Mapping(target = "active", source = "active")
  @Mapping(target = "metadata", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  public abstract GeoShapeResponseDTO domainToResponseDTO(GeoShape domain);

  /**
   * Fills the flattened fields on {@link LocationResponseDTO}.
   */
  @AfterMapping
  protected void populateResponseDTOFields(GeoShape domain, @MappingTarget GeoShapeResponseDTO dto) {
    if (domain == null) {
      return;
    }

    if (domain.getGeometry() != null && domain.getGeometry().toWKT() != null) {
      dto.setGeometryWKT(domain.getGeometry().toWKT());
    }

    // MetadataVO → Map<String, Object> ───────────────────────────────
    if (domain.getMetadata() != null) {
      dto.setMetadata(domain.getMetadata().toMap());
    }

    // ── AuditInfoVO  →  timestamps ──────────────────────
    if (domain.getAuditInfo() != null) {
      dto.setCreatedAt(domain.getAuditInfo().getCreatedAt() != null
        ? domain.getAuditInfo().getCreatedAt().toString()
        : null);
      dto.setUpdatedAt(domain.getAuditInfo().getUpdatedAt() != null
        ? domain.getAuditInfo().getUpdatedAt().toString()
        : null);
    }
  }

  // ================================================================
  // DTO  →  domain
  // ================================================================

  public GeoShape requestDTOtoDomain(GeoShapeRequestDTO requestDTO) {
    UUID id = UUID.randomUUID();
    final GeoShape geoShape;

    if (requestDTO.getGeometryType() == null) {
      throw new IllegalArgumentException("GeometryType cannot be null");
    }
    if (requestDTO.getGeometryWKT() == null || requestDTO.getGeometryWKT().isBlank()) {
      throw new IllegalArgumentException("GeometryWKT cannot be null or blank");
    }

    switch (requestDTO.getGeometryType()) {
      case POINT -> {
        final Geometry geometry = WKTParserUtil.fromWKT(requestDTO.getGeometryWKT(), GeometryType.POINT);
        final CoordinateVO coordinateVO = CoordinateVO.of(geometry.getCoordinate().y, geometry.getCoordinate().x);
        geoShape = GeoShape.createPoint(
          id,
          requestDTO.getName(),
          coordinateVO,
          requestDTO.getMetadata() != null ? MetadataVO.of(requestDTO.getMetadata()) : MetadataVO.empty()
        );
      }
      case CIRCLE -> {
        final Geometry geometry = WKTParserUtil.fromWKT(requestDTO.getGeometryWKT(), GeometryType.CIRCLE);
        final CoordinateVO coordinateVO = CoordinateVO.of(geometry.getCentroid().getY(), geometry.getCentroid().getX());
        final Point centroid = geometry.getCentroid();
        Coordinate boundaryPoint = geometry.getCoordinates()[0];
        double radius = centroid.getCoordinate().distance(boundaryPoint);
        geoShape = GeoShape.createCircle(
          id,
          requestDTO.getName(),
          coordinateVO,
          RadiusVO.of(radius),
          requestDTO.getMetadata() != null ? MetadataVO.of(requestDTO.getMetadata()) : MetadataVO.empty()
        );

      }
      case RECTANGLE -> {
        final Geometry geometry = WKTParserUtil.fromWKT(requestDTO.getGeometryWKT(), GeometryType.RECTANGLE);
        final CoordinateVO coordinateVO = CoordinateVO.of(geometry.getCentroid().getY(), geometry.getCentroid().getX());
        final GeometryVO geometryVO = GeometryVO.ofCoordinates(coordinateVO);
        geoShape = GeoShape.createRectangle(
          id,
          requestDTO.getName(),
          geometryVO,
          geometryVO.getBoundingBox().toBounds(),
          requestDTO.getMetadata() != null ? MetadataVO.of(requestDTO.getMetadata()) : MetadataVO.empty()
        );
      }
      case POLYGON -> {
        final Geometry geometry = WKTParserUtil.fromWKT(requestDTO.getGeometryWKT(), GeometryType.POLYGON);
        final List<CoordinateVO> coordinateVOList = Arrays.stream(geometry.getCoordinates()).map(x -> CoordinateVO.of(x.y, x.x)).toList();
        final PolygonVO polygonVO = PolygonVO.of(coordinateVOList);
        //final CoordinateVO coordinateVO = CoordinateVO.of(geometry.getCentroid().getY(), geometry.getCentroid().getX());
        final GeometryVO geometryVO = GeometryVO.ofPolygon(polygonVO);
        geoShape = GeoShape.createPolygon(
          id,
          requestDTO.getName(),
          geometryVO,
          geometryVO.getBoundingBox().toBounds(),
          requestDTO.getMetadata() != null ? MetadataVO.of(requestDTO.getMetadata()) : MetadataVO.empty()
        );
      }
      case LINE -> {
        final Geometry geometry = WKTParserUtil.fromWKT(requestDTO.getGeometryWKT(), GeometryType.LINE);
        final CoordinateVO coordinateVO = CoordinateVO.of(geometry.getCentroid().getY(), geometry.getCentroid().getX());
        final GeometryVO geometryVO = GeometryVO.ofCoordinates(coordinateVO);
        geoShape = GeoShape.createLine(
          id,
          requestDTO.getName(),
          geometryVO,
          requestDTO.getMetadata() != null ? MetadataVO.of(requestDTO.getMetadata()) : MetadataVO.empty()
        );
      }
      default -> throw new IllegalArgumentException("Unsupported geometry type: " + requestDTO.getGeometryType());
    }
    return geoShape;
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
