package com.cjrequena.sample.domain.mapper;

import com.cjrequena.sample.domain.model.Location;
import com.cjrequena.sample.domain.model.vo.*;
import com.cjrequena.sample.persistence.entity.LocationEntity;
import com.cjrequena.sample.persistence.entity.ZoneEntity;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author cjrequena
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class LocationMapper {

  private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

  // ================================================================
  // Domain  →  Entity
  // ================================================================

  /**
   * Converts a {@link Location} domain aggregate into a {@link LocationEntity}.
   */
  @Mapping(target = "id",         source = "id")
  @Mapping(target = "address",    source = "address")
  @Mapping(target = "postalCode", source = "postalCode")
  @Mapping(target = "active",     source = "active")
  // Handled in @AfterMapping:
  @Mapping(target = "zone",            ignore = true)   // UUID           → shell ZoneEntity
  @Mapping(target = "point",        ignore = true)   // PointVO        → JTS Point
  @Mapping(target = "altitudeMeters",  ignore = true)   // AltitudeVO     → BigDecimal
  @Mapping(target = "accuracyMeters",  ignore = true)   // GpsAccuracyVO  → BigDecimal
  @Mapping(target = "metadata",        ignore = true)   // MetadataVO     → JsonNode
  @Mapping(target = "createdAt",       ignore = true)   // AuditInfoVO    → OffsetDateTime
  @Mapping(target = "updatedAt",       ignore = true)
  public abstract LocationEntity toEntity(Location domain);

  /**
   * Fills every field on {@link LocationEntity} that requires a helper conversion.
   */
  @AfterMapping
  protected void populateEntityFields(Location domain, @MappingTarget LocationEntity entity) {
    if (domain == null) {
      return;
    }

    // ── AuditInfoVO  →  createdAt / updatedAt ──────────────────────
    if (domain.getAuditInfo() != null) {
      entity.setCreatedAt(domain.getAuditInfo().getCreatedAt());
      entity.setUpdatedAt(domain.getAuditInfo().getUpdatedAt());
    }

    // ── FK shell ───────────────────────────────────────────────────
    entity.setZone(uuidToZoneEntity(domain.getZoneId()));

    // ── PointVO  →  JTS Point ──────────────────────────────────────
    entity.setPoint(pointVOToJTS(domain.getPoint()));

    // ── AltitudeVO  →  BigDecimal ──────────────────────────────────
    entity.setAltitudeMeters(altitudeVOToBigDecimal(domain.getAltitude()));

    // ── GpsAccuracyVO  →  BigDecimal ───────────────────────────────
    entity.setAccuracyMeters(gpsAccuracyVOToBigDecimal(domain.getAccuracy()));

    // ── MetadataVO  →  JsonNode ────────────────────────────────────
    if (domain.getMetadata() != null) {
      entity.setMetadata(domain.getMetadata().getJsonNode());
    }

  }

  // ================================================================
  // Entity  →  Domain
  // ================================================================

  /**
   * Converts a {@link LocationEntity} into a {@link Location} domain aggregate.
   */
  @Mapping(target = "id",         source = "id")
  @Mapping(target = "address",    source = "address")
  @Mapping(target = "postalCode", source = "postalCode")
  @Mapping(target = "active",     source = "active")
  // Handled in @AfterMapping:
  @Mapping(target = "zoneId",     ignore = true)   // ZoneEntity     → UUID
  @Mapping(target = "point",      ignore = true)   // JTS Point      → PointVO
  @Mapping(target = "altitude",   ignore = true)   // BigDecimal     → AltitudeVO
  @Mapping(target = "accuracy",   ignore = true)   // BigDecimal     → GpsAccuracyVO
  @Mapping(target = "metadata",   ignore = true)   // JsonNode       → MetadataVO
  @Mapping(target = "auditInfo",  ignore = true)   // timestamps     → AuditInfoVO
  public abstract Location toDomain(LocationEntity entity);

  /**
   * Converts a list of {@link LocationEntity} into a list of {@link Location} domain aggregates.
   * Each entity is converted using {@link #toDomain(LocationEntity)}.
   */
  public abstract List<Location> toDomainList(List<LocationEntity> entityList);

  /**
   * Fills every value-object field on {@link Location} that requires assembly
   * from an entity column or a factory call.
   */
  @AfterMapping
  protected void populateDomainFields(LocationEntity entity, @MappingTarget Location domain) {
    if (entity == null) {
      return;
    }

    // ── createdAt / updatedAt  →  AuditInfoVO ──────────────────────
    if (entity.getCreatedAt() != null || entity.getUpdatedAt() != null) {
      domain.setAuditInfo(AuditInfoVO.of(entity.getCreatedAt(), entity.getUpdatedAt()));
    }

    // ── ZoneEntity  →  UUID ────────────────────────────────────────
    domain.setZoneId(zoneEntityToUuid(entity.getZone()));

    // ── JTS Point  →  PointVO ──────────────────────────────────────
    domain.setPoint(jtsToPointVO(entity.getPoint()));

    // ── BigDecimal  →  AltitudeVO ──────────────────────────────────
    domain.setAltitude(bigDecimalToAltitudeVO(entity.getAltitudeMeters()));

    // ── BigDecimal  →  GpsAccuracyVO ───────────────────────────────
    domain.setAccuracy(bigDecimalToGpsAccuracyVO(entity.getAccuracyMeters()));

    // ── JsonNode  →  MetadataVO ────────────────────────────────────
    if (entity.getMetadata() != null) {
      domain.setMetadata(MetadataVO.of(entity.getMetadata()));
    }

  }

  // ================================================================
  // Conversion helpers
  // ================================================================

  // ── FK shell ───────────────────────────────────────────────────────────────

  /**
   * Wraps a bare {@link UUID} into a {@link ZoneEntity} shell.
   * {@code zoneId} is optional on Location, so {@code null} is valid.
   */
  static ZoneEntity uuidToZoneEntity(UUID zoneId) {
    if (zoneId == null) {
      return null;
    }
    ZoneEntity shell = new ZoneEntity();
    shell.setId(zoneId);
    return shell;
  }

  /** Extracts the {@code id} from a {@link ZoneEntity}, null-safe. */
  static UUID zoneEntityToUuid(ZoneEntity zone) {
    return zone != null ? zone.getId() : null;
  }

  // ── PointVO ↔ JTS Point ────────────────────────────────────────────────────

  /**
   * Converts a {@link PointVO} to a JTS {@link Point}.
   *
   * <p>JTS convention: {@code x = longitude, y = latitude}.
   * {@link PointVO} exposes {@code getLatitude()} and {@code getLongitude()}.</p>
   *
   * <p>This method is {@code protected} (not {@code static}) because it needs
   * the instance-level {@link GeometryFactory}.</p>
   */
  protected Point pointVOToJTS(PointVO pointVO) {
    if (pointVO == null) {
      return null;
    }
    return geometryFactory.createPoint(
      new Coordinate(pointVO.getLongitude(), pointVO.getLatitude())
    );
  }

  /**
   * Converts a JTS {@link Point} back to a {@link PointVO}.
   *
   * <p>JTS convention: {@code x = longitude, y = latitude}.</p>
   */
  static PointVO jtsToPointVO(Point point) {
    if (point == null) {
      return null;
    }
    // y = latitude, x = longitude
    return PointVO.of(point.getY(), point.getX());
  }

  // ── AltitudeVO ↔ BigDecimal ────────────────────────────────────────────────

  /**
   * Extracts the metres value from an {@link AltitudeVO} as a {@link BigDecimal}.
   * If the VO stores metres as a {@code double}, the conversion is done via
   * {@link BigDecimal#valueOf(double)} to preserve the entity column's
   * {@code precision(8), scale(2)} semantics.
   */
  static BigDecimal altitudeVOToBigDecimal(AltitudeVO altitude) {
    if (altitude == null) {
      return null;
    }
    return altitude.getMeters();
  }

  /**
   * Wraps a {@link BigDecimal} column value back into an {@link AltitudeVO}.
   * Converts to {@code double} via {@link BigDecimal#doubleValue()} before
   * handing to the VO factory — matches the inverse of
   * {@link #altitudeVOToBigDecimal}.
   */
  static AltitudeVO bigDecimalToAltitudeVO(BigDecimal altitudeMeters) {
    if (altitudeMeters == null) {
      return null;
    }
    return AltitudeVO.of(altitudeMeters.doubleValue());
  }

  // ── GpsAccuracyVO ↔ BigDecimal ─────────────────────────────────────────────

  /**
   * Extracts the metres value from a {@link GpsAccuracyVO} as a {@link BigDecimal}.
   * Same {@link BigDecimal#valueOf(double)} rationale as {@link #altitudeVOToBigDecimal}.
   */
  static BigDecimal gpsAccuracyVOToBigDecimal(GpsAccuracyVO accuracy) {
    if (accuracy == null) {
      return null;
    }
    return accuracy.getMeters();
  }

  /**
   * Wraps a {@link BigDecimal} column value back into a {@link GpsAccuracyVO}.
   */
  static GpsAccuracyVO bigDecimalToGpsAccuracyVO(BigDecimal accuracyMeters) {
    if (accuracyMeters == null) {
      return null;
    }
    return GpsAccuracyVO.of(accuracyMeters.doubleValue());
  }
}
