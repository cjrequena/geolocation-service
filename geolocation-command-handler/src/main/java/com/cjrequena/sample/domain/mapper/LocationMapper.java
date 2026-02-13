package com.cjrequena.sample.domain.mapper;

import com.cjrequena.sample.controller.dto.CreateLocationRequestDTO;
import com.cjrequena.sample.controller.dto.LocationResponseDTO;
import com.cjrequena.sample.domain.model.Location;
import com.cjrequena.sample.domain.model.enums.LocationType;
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
  @Mapping(target = "id", source = "id")
  @Mapping(target = "name", source = "name")
  @Mapping(target = "locationType", source = "locationType")
  @Mapping(target = "address", source = "address")
  @Mapping(target = "postalCode", source = "postalCode")
  @Mapping(target = "active", source = "active")
  // Handled in @AfterMapping:
  @Mapping(target = "zone", ignore = true)   // UUID           → shell ZoneEntity
  @Mapping(target = "point", ignore = true)   // PointVO        → JTS Point
  @Mapping(target = "altitudeMeters", ignore = true)   // AltitudeVO     → BigDecimal
  @Mapping(target = "accuracyMeters", ignore = true)   // GpsAccuracyVO  → BigDecimal
  @Mapping(target = "metadata", ignore = true)   // MetadataVO     → JsonNode
  @Mapping(target = "createdBy", ignore = true)   // AuditInfoVO
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  public abstract LocationEntity toEntity(Location domain);

  /**
   * Fills every field on {@link LocationEntity} that requires a helper conversion.
   */
  @AfterMapping
  protected void populateEntityFields(Location domain, @MappingTarget LocationEntity entity) {
    if (domain == null) {
      return;
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

    // ── AuditInfoVO  ──────────────────────
    if (domain.getAuditInfo() != null) {
      entity.setCreatedBy(domain.getAuditInfo().getCreatedBy());
      entity.setUpdatedBy(domain.getAuditInfo().getUpdatedBy());
      entity.setCreatedAt(domain.getAuditInfo().getCreatedAt());
      entity.setUpdatedAt(domain.getAuditInfo().getUpdatedAt());
    }
  }

  // ================================================================
  // Entity  →  Domain
  // ================================================================

  /**
   * Converts a {@link LocationEntity} into a {@link Location} domain 
   */
  @Mapping(target = "id", source = "id")
  @Mapping(target = "name", source = "name")
  @Mapping(target = "locationType", source = "locationType")
  @Mapping(target = "address", source = "address")
  @Mapping(target = "postalCode", source = "postalCode")
  @Mapping(target = "active", source = "active")
  // Handled in @AfterMapping:
  @Mapping(target = "zoneId", ignore = true)   // ZoneEntity     → UUID
  @Mapping(target = "point", ignore = true)   // JTS Point      → PointVO
  @Mapping(target = "altitude", ignore = true)   // BigDecimal     → AltitudeVO
  @Mapping(target = "accuracy", ignore = true)   // BigDecimal     → GpsAccuracyVO
  @Mapping(target = "metadata", ignore = true)   // JsonNode       → MetadataVO
  @Mapping(target = "auditInfo", ignore = true)   // timestamps     → AuditInfoVO
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
    // ── AuditInfoVO ──────────────────────
    if (entity.getCreatedAt() != null || entity.getUpdatedAt() != null) {
      domain.setAuditInfo(AuditInfoVO.of(entity.getCreatedAt(), entity.getUpdatedAt(), entity.getCreatedBy(), entity.getUpdatedBy()));
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
  // Domain  →  DTO
  // ================================================================

  /**
   * Converts a {@link Location} domain aggregate into a {@link LocationResponseDTO}.
   */
  @Mapping(target = "id", expression = "java(domain.getId() != null ? domain.getId().toString() : null)")
  //@Mapping(target = "zoneId", expression = "java(domain.getZoneId() != null ? domain.getZoneId().toString() : null)")
  @Mapping(target = "name", source = "name")
  @Mapping(target = "locationType", source = "locationType")
  @Mapping(target = "address", source = "address")
  @Mapping(target = "postalCode", source = "postalCode")
  @Mapping(target = "active", source = "active")
  @Mapping(target = "latitude", ignore = true)
  @Mapping(target = "longitude", ignore = true)
  @Mapping(target = "altitudeMeters", ignore = true)
  @Mapping(target = "accuracyMeters", ignore = true)
  @Mapping(target = "metadata", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  public abstract LocationResponseDTO domainToResponseDTO(Location domain);

  /**
   * Fills the flattened fields on {@link LocationResponseDTO}.
   */
  @AfterMapping
  protected void populateResponseDTOFields(Location domain, @MappingTarget LocationResponseDTO dto) {
    if (domain == null) {
      return;
    }

    // ── PointVO  →  latitude/longitude ──────────────────────────
    if (domain.getPoint() != null) {
      dto.setLatitude(domain.getPoint().getLatitude());
      dto.setLongitude(domain.getPoint().getLongitude());
    }

    // ── AltitudeVO  →  BigDecimal ──────────────────────────────────────
    dto.setAltitudeMeters(altitudeVOToDouble(domain.getAltitude()));

    // ── GpsAccuracyVO  →  BigDecimal ───────────────────────────────
    dto.setAccuracyMeters(gpsAccuracyVOToDouble(domain.getAccuracy()));

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

  public Location requestDTOtoDomain(CreateLocationRequestDTO requestDTO) {
    return Location.create(
      UUID.randomUUID(),
      requestDTO.getZoneId() != null ? UUID.fromString(requestDTO.getZoneId()) : null,
      requestDTO.getName(),
      requestDTO.getLocationType() != null ? requestDTO.getLocationType() : LocationType.GENERIC,
      PointVO.of(requestDTO.getLatitude(), requestDTO.getLongitude()),
      requestDTO.getAltitudeMeters() != null ? AltitudeVO.of(requestDTO.getAltitudeMeters()) : null,
      requestDTO.getAccuracyMeters() != null ? GpsAccuracyVO.of(requestDTO.getAccuracyMeters()) : null,
      requestDTO.getAddress(),
      requestDTO.getPostalCode(),
      requestDTO.getActive() != null ? requestDTO.getActive() : Boolean.TRUE,
      requestDTO.getMetadata() != null ? MetadataVO.of(requestDTO.getMetadata()) : MetadataVO.empty()
    );
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

  static Double altitudeVOToDouble(AltitudeVO altitude) {
    if (altitude == null || altitude.getMeters() == null) {
      return null;
    }
    return altitude.getMeters().doubleValue();
  }

  static AltitudeVO doubleToAltitudeVO(BigDecimal altitudeMeters) {
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

  static Double gpsAccuracyVOToDouble(GpsAccuracyVO accuracy) {
    if (accuracy == null || accuracy.getMeters() == null) {
      return null;
    }
    return accuracy.getMeters().doubleValue();
  }

  static GpsAccuracyVO doubleToGpsAccuracyVO(Double accuracyMeters) {
    if (accuracyMeters == null) {
      return null;
    }
    return GpsAccuracyVO.of(accuracyMeters);
  }
}
