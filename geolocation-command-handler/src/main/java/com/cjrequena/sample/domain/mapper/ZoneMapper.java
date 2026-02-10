package com.cjrequena.sample.domain.mapper;

import com.cjrequena.sample.domain.model.aggregate.Zone;
import com.cjrequena.sample.domain.model.enums.ZoneType;
import com.cjrequena.sample.domain.model.vo.AuditInfoVO;
import com.cjrequena.sample.persistence.entity.AreaEntity;
import com.cjrequena.sample.persistence.entity.GeoShapeEntity;
import com.cjrequena.sample.persistence.entity.ZoneEntity;
import org.mapstruct.*;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author cjrequena
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ZoneMapper {

  // ================================================================
  // Domain  →  Entity
  // ================================================================

  /**
   * Converts an {@link Zone} domain aggregate into an {@link ZoneEntity}.
   *
   * <p>{@code active} shares the same name on both sides so it is wired
   * declaratively alongside the other direct scalars.  Everything that needs
   * a helper lives in {@link #populateEntityFields}.</p>
   */
  @Mapping(target = "id",         source = "id")
  @Mapping(target = "name",       source = "name")
  @Mapping(target = "postalCode", source = "postalCode")
  @Mapping(target = "active",     source = "active")
  // Handled in @AfterMapping:
  @Mapping(target = "area",   ignore = true)   // UUID       → shell Area
  @Mapping(target = "geoShape",   ignore = true)   // UUID       → shell GeoShapeEntity
  @Mapping(target = "zoneType",   ignore = true)   // ZoneType   → String
  @Mapping(target = "createdAt",  ignore = true)   // AuditInfoVO  → OffsetDateTime
  @Mapping(target = "updatedAt",  ignore = true)
  ZoneEntity toEntity(Zone domain);

  /** Fills every field on {@link ZoneEntity} that requires a helper conversion. */
  @AfterMapping
  default void populateEntityFields(Zone domain, @MappingTarget ZoneEntity entity) {
    if (domain == null) {
      return;
    }

    // ── FK shell entities ──────────────────────────────────────────
    entity.setArea(uuidToAreaEntity(domain.getAreaId()));
    entity.setGeoShape(uuidToGeoShapeEntity(domain.getGeoShapeId()));

    // ── ZoneType enum  →  String ───────────────────────────────────
    entity.setZoneType(zoneTypeToString(domain.getType()));

    // ── AuditInfoVO  →  createdAt / updatedAt ──────────────────────
    if (domain.getAuditInfo() != null) {
      entity.setCreatedAt(domain.getAuditInfo().getCreatedAt());
      entity.setUpdatedAt(domain.getAuditInfo().getUpdatedAt());
    }
  }

  // ================================================================
  // Entity  →  Domain
  // ================================================================

  /**
   * Converts an {@link ZoneEntity} into an {@link Zone} domain aggregate.
   */
  @Mapping(target = "id",         source = "id")
  @Mapping(target = "name",       source = "name")
  @Mapping(target = "postalCode", source = "postalCode")
  @Mapping(target = "active",     source = "active")
  // Handled in @AfterMapping:
  @Mapping(target = "areaId",     ignore = true)   // AreaEntity      → UUID
  @Mapping(target = "geoShapeId", ignore = true)   // GeoShapeEntity  → UUID
  @Mapping(target = "type",       ignore = true)   // String          → ZoneType
  @Mapping(target = "auditInfo",  ignore = true)   // timestamps      → AuditInfoVO
  Zone toDomain(ZoneEntity entity);

  /**
   * Converts a list of {@link ZoneEntity} into a list of {@link Zone} domain aggregates.
   * Each entity is converted using {@link #toDomain(ZoneEntity)}.
   */
  List<Zone> toDomainList(List<ZoneEntity> entityList);
  
  /** Fills every value-object / derived field on {@link Zone} that requires a helper. */
  @AfterMapping
  default void populateDomainFields(ZoneEntity entity, @MappingTarget Zone domain) {
    if (entity == null) {
      return;
    }

    // ── AreaEntity  →  UUID ────────────────────────────────────────
    domain.setAreaId(areaEntityToUuid(entity.getArea()));

    // ── GeoShapeEntity  →  UUID ────────────────────────────────────
    domain.setGeoShapeId(geoShapeEntityToUuid(entity.getGeoShape()));

    // ── String  →  ZoneType enum ───────────────────────────────────
    domain.setType(stringToZoneType(entity.getZoneType()));

    // ── createdAt / updatedAt  →  AuditInfoVO ──────────────────────
    if (entity.getCreatedAt() != null || entity.getUpdatedAt() != null) {
      domain.setAuditInfo(AuditInfoVO.of(entity.getCreatedAt(), entity.getUpdatedAt()));
    }
  }

  // ================================================================
  // Static conversion helpers
  // ================================================================

  // ── FK shell entities ──────────────────────────────────────────────────────

  /**
   * Wraps a bare {@link UUID} into a {@link AreaEntity} shell.
   * JPA uses the shell to persist the FK column without loading the full row.
   */
  static AreaEntity uuidToAreaEntity(UUID areaId) {
    if (areaId == null) {
      return null;
    }
    AreaEntity shell = new AreaEntity();
    shell.setId(areaId);
    return shell;
  }

  /**
   * Wraps a bare {@link UUID} into a {@link GeoShapeEntity} shell.
   * {@code geoShapeId} is optional, so {@code null} is a valid input.
   */
  static GeoShapeEntity uuidToGeoShapeEntity(UUID geoShapeId) {
    if (geoShapeId == null) {
      return null;
    }
    GeoShapeEntity shell = new GeoShapeEntity();
    shell.setId(geoShapeId);
    return shell;
  }

  /** Extracts the {@code id} from a {@link AreaEntity}, null-safe. */
  static UUID areaEntityToUuid(AreaEntity area) {
    return area != null ? area.getId() : null;
  }

  /** Extracts the {@code id} from a {@link GeoShapeEntity}, null-safe. */
  static UUID geoShapeEntityToUuid(GeoShapeEntity geoShape) {
    return geoShape != null ? geoShape.getId() : null;
  }

  // ── ZoneType ↔ String ─────────────────────────────────────────────────────

  /**
   * Converts an {@link ZoneType} enum to its persistence string via
   * {@link ZoneType#getValue()}.
   */
  static String zoneTypeToString(ZoneType type) {
    return type != null ? type.getValue() : null;
  }

  /**
   * Recovers an {@link ZoneType} from its persistence string via
   * {@link ZoneType#valueOf(String)}.
   *
   * <p>Replace with {@code ZoneType.valueOf(string)} if your enum does not
   * expose a {@code fromValue} factory.</p>
   */
  static ZoneType stringToZoneType(String zoneType) {
    return zoneType != null ? ZoneType.valueOf(zoneType) : null;
  }

}
