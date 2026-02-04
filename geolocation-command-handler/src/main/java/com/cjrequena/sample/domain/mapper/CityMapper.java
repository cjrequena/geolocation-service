package com.cjrequena.sample.domain.mapper;

import com.cjrequena.sample.domain.model.aggregate.City;
import com.cjrequena.sample.domain.model.vo.AuditInfoVO;
import com.cjrequena.sample.domain.model.vo.PopulationVO;
import com.cjrequena.sample.persistence.entity.CityEntity;
import com.cjrequena.sample.persistence.entity.GeoShapeEntity;
import com.cjrequena.sample.persistence.entity.RegionEntity;
import org.mapstruct.*;

import java.util.TimeZone;
import java.util.UUID;

/**
 *
 * @author cjrequena
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CityMapper {

  // ================================================================
  // Domain  →  Entity
  // ================================================================

  /**
   * Converts a {@link City} domain aggregate into a {@link CityEntity}.
   *
   * <p>Scalar fields that map 1-to-1 (including the two boolean renames) are
   * wired declaratively.  Everything that needs a helper conversion is
   * {@code ignore = true} and filled by {@link #populateEntityFields}.</p>
   */
  @Mapping(target = "id",         source = "id")
  @Mapping(target = "name",       source = "name")
  @Mapping(target = "postalCode", source = "postalCode")
  @Mapping(target = "active",   source = "active")
  @Mapping(target = "capital",  source = "capital")
  // Handled in @AfterMapping:
  @Mapping(target = "region",     ignore = true)   // UUID          → shell RegionEntity
  @Mapping(target = "geoShape",   ignore = true)   // UUID          → shell GeoShapeEntity
  @Mapping(target = "population", ignore = true)   // PopulationVO  → Long
  @Mapping(target = "timeZone",   ignore = true)   // TimeZone      → String (IANA id)
  @Mapping(target = "createdAt",  ignore = true)   // AuditInfoVO   → OffsetDateTime
  @Mapping(target = "updatedAt",  ignore = true)   // AuditInfoVO   → OffsetDateTime
  CityEntity toEntity(City domain);

  /**
   * Fills every field on {@link CityEntity} that requires a helper conversion.
   */
  @AfterMapping
  default void populateEntityFields(City domain, @MappingTarget CityEntity entity) {
    if (domain == null) {
      return;
    }

    // ── FK shell entities ──────────────────────────────────────────
    entity.setRegion(uuidToRegionEntity(domain.getRegionId()));
    entity.setGeoShape(uuidToGeoShapeEntity(domain.getGeoShapeId()));

    // ── PopulationVO  →  Long ──────────────────────────────────────
    entity.setPopulation(populationVOToLong(domain.getPopulation()));

    // ── TimeZone  →  String ────────────────────────────────────────
    entity.setTimeZone(timeZoneToString(domain.getTimeZone()));

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
   * Converts a {@link CityEntity} into a {@link City} domain aggregate.
   */
  @Mapping(target = "id",         source = "id")
  @Mapping(target = "name",       source = "name")
  @Mapping(target = "postalCode", source = "postalCode")
  @Mapping(target = "active",     source = "active")
  @Mapping(target = "capital",    source = "capital")
  // Handled in @AfterMapping:
  @Mapping(target = "regionId",      ignore = true)   // RegionEntity   → UUID
  @Mapping(target = "geoShapeId",    ignore = true)   // GeoShapeEntity → UUID
  @Mapping(target = "population",    ignore = true)   // Long           → PopulationVO
  @Mapping(target = "timeZone",      ignore = true)   // String         → TimeZone
  @Mapping(target = "auditInfo",     ignore = true)   // timestamps     → AuditInfoVO
  City toDomain(CityEntity entity);

  /**
   * Fills every value-object / derived field on {@link City} that requires
   * a helper conversion.
   */
  @AfterMapping
  default void populateDomainFields(CityEntity entity, @MappingTarget City domain) {
    if (entity == null) {
      return;
    }

    // ── RegionEntity  →  UUID ──────────────────────────────────────
    domain.setRegionId(regionEntityToUuid(entity.getRegion()));

    // ── GeoShapeEntity  →  UUID ────────────────────────────────────
    domain.setGeoShapeId(geoShapeEntityToUuid(entity.getGeoShape()));

    // ── Long  →  PopulationVO ──────────────────────────────────────
    domain.setPopulation(longToPopulationVO(entity.getPopulation()));

    // ── String  →  TimeZone ────────────────────────────────────────
    domain.setTimeZone(stringToTimeZone(entity.getTimeZone()));

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
   * Wraps a bare {@link UUID} into a {@link RegionEntity} shell.
   * JPA uses the shell to persist the FK column without loading the full row.
   */
  static RegionEntity uuidToRegionEntity(UUID regionId) {
    if (regionId == null) {
      return null;
    }
    RegionEntity shell = new RegionEntity();
    shell.setId(regionId);
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

  /** Extracts the {@code id} from a {@link RegionEntity}, null-safe. */
  static UUID regionEntityToUuid(RegionEntity region) {
    return region != null ? region.getId() : null;
  }

  /** Extracts the {@code id} from a {@link GeoShapeEntity}, null-safe. */
  static UUID geoShapeEntityToUuid(GeoShapeEntity geoShape) {
    return geoShape != null ? geoShape.getId() : null;
  }

  // ── PopulationVO ↔ Long ────────────────────────────────────────────────────

  /** Extracts the numeric value from a {@link PopulationVO}, null-safe. */
  static Long populationVOToLong(PopulationVO population) {
    return population != null ? population.getValue() : null;
  }

  /**
   * Wraps a {@code Long} into a {@link PopulationVO} via its factory method.
   * {@code null} input produces {@code null} — population is optional.
   */
  static PopulationVO longToPopulationVO(Long population) {
    return population != null ? PopulationVO.of(population) : null;
  }

  // ── TimeZone ↔ String ─────────────────────────────────────────────────────

  /**
   * Converts a {@link TimeZone} to its IANA zone-id string
   * (e.g. {@code "America/Los_Angeles"}) for persistence.
   *
   * @return the zone-id, or {@code null} when the input is {@code null}
   */
  static String timeZoneToString(TimeZone timeZone) {
    return timeZone != null ? timeZone.getID() : null;
  }

  /**
   * Recovers a {@link TimeZone} from its IANA zone-id string.
   *
   * <p><b>Note:</b> {@link TimeZone#getTimeZone(String)} never returns {@code null};
   * it falls back to {@code GMT} for unrecognised ids.  If you need strict
   * validation, add a check against {@link java.util.Arrays#asList}
   * {@link TimeZone#getAvailableIDs()} before calling this method.</p>
   *
   * @return the {@link TimeZone}, or {@code null} when the input is {@code null}
   */
  static TimeZone stringToTimeZone(String zoneId) {
    return zoneId != null ? TimeZone.getTimeZone(zoneId) : null;
  }
}
