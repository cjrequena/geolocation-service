package com.cjrequena.sample.domain.mapper;

import com.cjrequena.sample.domain.model.aggregate.Region;
import com.cjrequena.sample.domain.model.enums.RegionType;
import com.cjrequena.sample.domain.model.vo.AuditInfoVO;
import com.cjrequena.sample.domain.model.vo.PopulationVO;
import com.cjrequena.sample.persistence.entity.CountryEntity;
import com.cjrequena.sample.persistence.entity.GeoShapeEntity;
import com.cjrequena.sample.persistence.entity.RegionEntity;
import org.mapstruct.*;

import java.util.UUID;

/**
 *
 * @author cjrequena
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RegionMapper {

  // ================================================================
  // Domain  →  Entity
  // ================================================================

  /**
   * Converts a {@link Region} domain aggregate into a {@link RegionEntity}.
   *
   * <p>Fields that require non-trivial conversion (FK shell entities, enum →
   * String, VO → primitive, audit flattening) are declared as {@code ignore = true}
   * here and populated by the {@link #populateEntityFields} {@code @AfterMapping}
   * hook.</p>
   */
  @Mapping(target = "id", source = "id")
  @Mapping(target = "name", source = "name")
  @Mapping(target = "code", source = "code")
  @Mapping(target = "timezone", source = "timezone")
  @Mapping(target = "active", source = "status")
  // Handled in @AfterMapping – no direct path available:
  @Mapping(target = "country", ignore = true)   // UUID  → shell CountryEntity
  @Mapping(target = "geoShape", ignore = true)   // UUID  → shell GeoShapeEntity
  @Mapping(target = "regionType", ignore = true)   // enum  → String
  @Mapping(target = "population", ignore = true)   // VO    → Long
  @Mapping(target = "createdAt", ignore = true)   // VO    → OffsetDateTime
  @Mapping(target = "updatedAt", ignore = true)
  // VO    → OffsetDateTime
  RegionEntity toEntity(Region domain);

  /**
   * Populates every field on {@link RegionEntity} that could not be expressed
   * as a simple {@code source} path in the main mapping method.
   */
  @AfterMapping
  default void populateEntityFields(Region domain, @MappingTarget RegionEntity entity) {
    if (domain == null) {
      return;
    }

    // ── FK shell entities ──────────────────────────────────────────
    entity.setCountry(uuidToCountryEntity(domain.getCountryId()));
    entity.setGeoShape(uuidToGeoShapeEntity(domain.getGeoShapeId()));

    // ── RegionType enum  →  String ─────────────────────────────────
    entity.setRegionType(regionTypeToString(domain.getType()));

    // ── PopulationVO  →  Long ──────────────────────────────────────
    entity.setPopulation(populationVOToLong(domain.getPopulation()));

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
   * Converts a {@link RegionEntity} into a {@link Region} domain aggregate.
   *
   * <p>Same pattern as {@link #toEntity}: scalar fields are wired declaratively;
   * everything else lands in the {@link #populateDomainFields} hook.</p>
   */
  @Mapping(target = "id", source = "id")
  @Mapping(target = "name", source = "name")
  @Mapping(target = "code", source = "code")
  @Mapping(target = "timezone", source = "timezone")
  @Mapping(target = "status", source = "active")
  // Handled in @AfterMapping:
  @Mapping(target = "countryId", ignore = true)   // CountryEntity  → UUID
  @Mapping(target = "geoShapeId", ignore = true)   // GeoShapeEntity → UUID
  @Mapping(target = "type", ignore = true)   // String         → enum
  @Mapping(target = "population", ignore = true)   // Long           → VO
  @Mapping(target = "auditInfo", ignore = true)
  // timestamps     → VO
  Region toDomain(RegionEntity entity);

  /**
   * Populates every value-object / derived field on {@link Region} that could
   * not be expressed as a simple {@code source} path.
   */
  @AfterMapping
  default void populateDomainFields(RegionEntity entity, @MappingTarget Region domain) {
    if (entity == null) {
      return;
    }

    // ── CountryEntity  →  UUID ─────────────────────────────────────
    domain.setCountryId(countryEntityToUuid(entity.getCountry()));

    // ── GeoShapeEntity  →  UUID ────────────────────────────────────
    domain.setGeoShapeId(geoShapeEntityToUuid(entity.getGeoShape()));

    // ── String  →  RegionType enum ─────────────────────────────────
    domain.setType(stringToRegionType(entity.getRegionType()));

    // ── Long  →  PopulationVO ──────────────────────────────────────
    domain.setPopulation(longToPopulationVO(entity.getPopulation()));

    // ── createdAt / updatedAt  →  AuditInfoVO ──────────────────────
    if (entity.getCreatedAt() != null || entity.getUpdatedAt() != null) {
      domain.setAuditInfo(AuditInfoVO.of(entity.getCreatedAt(), entity.getUpdatedAt()));
    }
  }

  // ================================================================
  // Scalar conversion helpers  (package-private default methods)
  // ================================================================

  /**
   * Wraps a bare {@link UUID} into a {@link CountryEntity} shell that JPA
   * can use to persist the foreign-key column without loading the full row.
   * Returns {@code null} when the UUID is {@code null}.
   */
  static CountryEntity uuidToCountryEntity(UUID countryId) {
    if (countryId == null) {
      return null;
    }
    CountryEntity shell = new CountryEntity();
    shell.setId(countryId);
    return shell;
  }

  /**
   * Wraps a bare {@link UUID} into a {@link GeoShapeEntity} shell.
   * {@code geoShapeId} is optional in the domain, so {@code null} is a valid input.
   */
  static GeoShapeEntity uuidToGeoShapeEntity(UUID geoShapeId) {
    if (geoShapeId == null) {
      return null;
    }
    GeoShapeEntity shell = new GeoShapeEntity();
    shell.setId(geoShapeId);
    return shell;
  }

  /** Extracts the {@code id} from a {@link CountryEntity}, null-safe. */
  static UUID countryEntityToUuid(CountryEntity country) {
    return country != null ? country.getId() : null;
  }

  /** Extracts the {@code id} from a {@link GeoShapeEntity}, null-safe. */
  static UUID geoShapeEntityToUuid(GeoShapeEntity geoShape) {
    return geoShape != null ? geoShape.getId() : null;
  }

  /**
   * Converts a {@link RegionType} enum to its persistence string via
   * {@link RegionType#getValue()}.
   *
   * <p>If your enum uses a different accessor (e.g. {@code name()}) adjust here.</p>
   */
  static String regionTypeToString(RegionType type) {
    return type != null ? type.getValue() : null;
  }

  /**
   * Converts a persistence string back to a {@link RegionType}.
   *
   * enum (mirrors the {@code getValue()} convention).  Replace with
   * {@code RegionType.valueOf(string)} if your enum does not have a custom factory.</p>
   */
  static RegionType stringToRegionType(String regionType) {
    return regionType != null ? RegionType.valueOf(regionType) : null;
  }

  /** Extracts the numeric value from a {@link PopulationVO}, null-safe. */
  static Long populationVOToLong(PopulationVO population) {
    return population != null ? population.getValue() : null;
  }

  /**
   * Wraps a {@code Long} into a {@link PopulationVO} via its factory method.
   * {@code null} input produces {@code null} output (population is optional).
   */
  static PopulationVO longToPopulationVO(Long population) {
    return population != null ? PopulationVO.of(population) : null;
  }
}
