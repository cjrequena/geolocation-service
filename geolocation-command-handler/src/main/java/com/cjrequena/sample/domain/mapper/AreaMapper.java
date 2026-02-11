package com.cjrequena.sample.domain.mapper;

import com.cjrequena.sample.domain.model.Area;
import com.cjrequena.sample.domain.model.enums.AreaType;
import com.cjrequena.sample.domain.model.vo.AuditInfoVO;
import com.cjrequena.sample.domain.model.vo.MetadataVO;
import com.cjrequena.sample.domain.model.vo.PopulationVO;
import com.cjrequena.sample.persistence.entity.AreaEntity;
import com.cjrequena.sample.persistence.entity.CityEntity;
import com.cjrequena.sample.persistence.entity.GeoShapeEntity;
import org.mapstruct.*;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author cjrequena
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AreaMapper {

  // ================================================================
  // Domain  →  Entity
  // ================================================================

  /**
   * Converts an {@link Area} domain aggregate into an {@link AreaEntity}.
   *
   * <p>{@code active} shares the same name on both sides so it is wired
   * declaratively alongside the other direct scalars.  Everything that needs
   * a helper lives in {@link #populateEntityFields}.</p>
   */
  @Mapping(target = "id", source = "id")
  @Mapping(target = "name", source = "name")
  @Mapping(target = "postalCode", source = "postalCode")
  @Mapping(target = "active", source = "active")
  // Handled in @AfterMapping:
  @Mapping(target = "city", ignore = true)   // UUID       → shell CityEntity
  @Mapping(target = "geoShape", ignore = true)   // UUID       → shell GeoShapeEntity
  @Mapping(target = "areaType", ignore = true)   // AreaType   → String
  @Mapping(target = "population", ignore = true)   // PopulationVO → Long
  @Mapping(target = "metadata",        ignore = true)   // MetadataVO     → JsonNode
  @Mapping(target = "createdAt", ignore = true)   // AuditInfoVO  → OffsetDateTime
  @Mapping(target = "updatedAt", ignore = true)
  AreaEntity toEntity(Area domain);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "metadata",  ignore = true)   // MetadataVO     → JsonNode
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateEntity(Area source, @MappingTarget AreaEntity target);

  /** Fills every field on {@link AreaEntity} that requires a helper conversion. */
  @AfterMapping
  default void populateEntityFields(Area domain, @MappingTarget AreaEntity entity) {
    if (domain == null) {
      return;
    }

    // ── FK shell entities ──────────────────────────────────────────
    entity.setCity(uuidToCityEntity(domain.getCityId()));
    entity.setGeoShape(uuidToGeoShapeEntity(domain.getGeoShapeId()));

    // ── AreaType enum  →  String ───────────────────────────────────
    entity.setAreaType(areaTypeToString(domain.getType()));

    // ── PopulationVO  →  Long ──────────────────────────────────────
    entity.setPopulation(populationVOToLong(domain.getPopulation()));

    // ── MetadataVO  →  JsonNode ────────────────────────────────────
    if (domain.getMetadata() != null) {
      entity.setMetadata(domain.getMetadata().getJsonNode());
    }

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
   * Converts an {@link AreaEntity} into an {@link Area} domain aggregate.
   */
  @Mapping(target = "id", source = "id")
  @Mapping(target = "name", source = "name")
  @Mapping(target = "postalCode", source = "postalCode")
  @Mapping(target = "active", source = "active")
  // Handled in @AfterMapping:
  @Mapping(target = "cityId", ignore = true)   // CityEntity      → UUID
  @Mapping(target = "geoShapeId", ignore = true)   // GeoShapeEntity  → UUID
  @Mapping(target = "type", ignore = true)   // String          → AreaType
  @Mapping(target = "population", ignore = true)   // Long            → PopulationVO
  @Mapping(target = "metadata",   ignore = true)   // JsonNode       → MetadataVO
  @Mapping(target = "auditInfo", ignore = true)
  // timestamps      → AuditInfoVO
  Area toDomain(AreaEntity entity);

  /**
   * Converts a list of {@link AreaEntity} into a list of {@link Area} domain aggregates.
   * Each entity is converted using {@link #toDomain(AreaEntity)}.
   */
  List<Area> toDomainList(List<AreaEntity> entityList);

  /** Fills every value-object / derived field on {@link Area} that requires a helper. */
  @AfterMapping
  default void populateDomainFields(AreaEntity entity, @MappingTarget Area domain) {
    if (entity == null) {
      return;
    }

    // ── CityEntity  →  UUID ────────────────────────────────────────
    domain.setCityId(cityEntityToUuid(entity.getCity()));

    // ── GeoShapeEntity  →  UUID ────────────────────────────────────
    domain.setGeoShapeId(geoShapeEntityToUuid(entity.getGeoShape()));

    // ── String  →  AreaType enum ───────────────────────────────────
    domain.setType(stringToAreaType(entity.getAreaType()));

    // ── Long  →  PopulationVO ──────────────────────────────────────
    domain.setPopulation(longToPopulationVO(entity.getPopulation()));

    // ── createdAt / updatedAt  →  AuditInfoVO ──────────────────────
    if (entity.getCreatedAt() != null || entity.getUpdatedAt() != null) {
      domain.setAuditInfo(AuditInfoVO.of(entity.getCreatedAt(), entity.getUpdatedAt()));
    }

    // ── JsonNode  →  MetadataVO ────────────────────────────────────
    if (entity.getMetadata() != null) {
      domain.setMetadata(MetadataVO.of(entity.getMetadata()));
    }
  }

  // ================================================================
  // Static conversion helpers
  // ================================================================

  // ── FK shell entities ──────────────────────────────────────────────────────

  /**
   * Wraps a bare {@link UUID} into a {@link CityEntity} shell.
   * JPA uses the shell to persist the FK column without loading the full row.
   */
  static CityEntity uuidToCityEntity(UUID cityId) {
    if (cityId == null) {
      return null;
    }
    CityEntity shell = new CityEntity();
    shell.setId(cityId);
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

  /** Extracts the {@code id} from a {@link CityEntity}, null-safe. */
  static UUID cityEntityToUuid(CityEntity city) {
    return city != null ? city.getId() : null;
  }

  /** Extracts the {@code id} from a {@link GeoShapeEntity}, null-safe. */
  static UUID geoShapeEntityToUuid(GeoShapeEntity geoShape) {
    return geoShape != null ? geoShape.getId() : null;
  }

  // ── AreaType ↔ String ─────────────────────────────────────────────────────

  /**
   * Converts an {@link AreaType} enum to its persistence string via
   * {@link AreaType#getValue()}.
   */
  static String areaTypeToString(AreaType type) {
    return type != null ? type.getValue() : null;
  }

  /**
   * Recovers an {@link AreaType} from its persistence string via
   * {@link AreaType#valueOf(String)}.
   *
   * <p>Replace with {@code AreaType.valueOf(string)} if your enum does not
   * expose a {@code fromValue} factory.</p>
   */
  static AreaType stringToAreaType(String areaType) {
    return areaType != null ? AreaType.valueOf(areaType) : null;
  }

  // ── PopulationVO ↔ Long ────────────────────────────────────────────────────

  /** Extracts the numeric value from a {@link PopulationVO}, null-safe. */
  static Long populationVOToLong(PopulationVO population) {
    return population != null ? population.getValue() : null;
  }

  /**
   * Wraps a {@code Long} into a {@link PopulationVO}.
   * {@code null} input produces {@code null} — population is optional.
   */
  static PopulationVO longToPopulationVO(Long population) {
    return population != null ? PopulationVO.of(population) : null;
  }
}
