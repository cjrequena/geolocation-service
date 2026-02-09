package com.cjrequena.sample.domain.mapper;

import com.cjrequena.sample.domain.model.aggregate.Country;
import com.cjrequena.sample.domain.model.vo.AuditInfoVO;
import com.cjrequena.sample.domain.model.vo.IsoCodeVO;
import com.cjrequena.sample.domain.model.vo.PopulationVO;
import com.cjrequena.sample.persistence.entity.CountryEntity;
import org.mapstruct.*;

import java.util.List;

/**
 *
 * @author cjrequena
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CountryMapper {

  // ================================================================
  // Domain  →  Entity
  // ================================================================

  /**
   * Converts a {@link Country} domain aggregate into a {@link CountryEntity}.
   *
   * <p>The five simple scalar fields (plus the boolean rename) are wired
   * declaratively.  The three fan-out / flatten conversions are handled by
   * {@link #populateEntityFields}.</p>
   */
  @Mapping(target = "id",           source = "id")
  @Mapping(target = "name",         source = "name")
  @Mapping(target = "phoneCode",    source = "phoneCode")
  @Mapping(target = "currencyCode", source = "currencyCode")
  @Mapping(target = "capital",      source = "capital")
  @Mapping(target = "active",     source = "active")
  // Handled in @AfterMapping:
  @Mapping(target = "isoCodeAlpha2",   ignore = true)   // IsoCodeVO  →  three columns
  @Mapping(target = "isoCodeAlpha3",   ignore = true)
  @Mapping(target = "isoCodeNumeric",  ignore = true)
  @Mapping(target = "population",      ignore = true)   // PopulationVO  →  Long
  @Mapping(target = "createdAt",       ignore = true)   // AuditInfoVO   →  OffsetDateTime
  @Mapping(target = "updatedAt",       ignore = true)
  CountryEntity toEntity(Country domain);

  /**
   * Fills the fields on {@link CountryEntity} that require helper conversions.
   */
  @AfterMapping
  default void populateEntityFields(Country domain, @MappingTarget CountryEntity entity) {
    if (domain == null) {
      return;
    }

    // ── IsoCodeVO  →  three flat columns ──────────────────────────
    if (domain.getIsoCode() != null) {
      entity.setIsoCodeAlpha2(domain.getIsoCode().getAlpha2());
      entity.setIsoCodeAlpha3(domain.getIsoCode().getAlpha3());
      entity.setIsoCodeNumeric(domain.getIsoCode().getNumeric());
    }

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
   * Converts a {@link CountryEntity} into a {@link Country} domain aggregate.
   */
  @Mapping(target = "id",           source = "id")
  @Mapping(target = "name",         source = "name")
  @Mapping(target = "phoneCode",    source = "phoneCode")
  @Mapping(target = "currencyCode", source = "currencyCode")
  @Mapping(target = "capital",      source = "capital")
  @Mapping(target = "active",       source = "active")
  // Handled in @AfterMapping:
  @Mapping(target = "isoCode",      ignore = true)   // three columns  →  IsoCodeVO
  @Mapping(target = "population",   ignore = true)   // Long           →  PopulationVO
  @Mapping(target = "auditInfo",    ignore = true)   // timestamps     →  AuditInfoVO
  Country toDomain(CountryEntity entity);

  /**
   * Converts a list of {@link CountryEntity} into a list of {@link Country} domain aggregates.
   * Each entity is converted using {@link #toDomain(CountryEntity)}.
   */
  List<Country> toDomainList(List<CountryEntity> entityList);

  /**
   * Fills the value-object fields on {@link Country} that require assembly
   * from multiple entity columns or a factory call.
   */
  @AfterMapping
  default void populateDomainFields(CountryEntity entity, @MappingTarget Country domain) {
    if (entity == null) {
      return;
    }

    // ── three flat columns  →  IsoCodeVO ───────────────────────────
    domain.setIsoCode(isoCodeVOFrom(
      entity.getIsoCodeAlpha2(),
      entity.getIsoCodeAlpha3(),
      entity.getIsoCodeNumeric()
    ));

    // ── Long  →  PopulationVO ──────────────────────────────────────
    domain.setPopulation(longToPopulationVO(entity.getPopulation()));

    // ── createdAt / updatedAt  →  AuditInfoVO ──────────────────────
    if (entity.getCreatedAt() != null || entity.getUpdatedAt() != null) {
      domain.setAuditInfo(AuditInfoVO.of(entity.getCreatedAt(), entity.getUpdatedAt()));
    }
  }

  // ================================================================
  // Static conversion helpers
  // ================================================================

  // ── IsoCodeVO ──────────────────────────────────────────────────────────────

  /**
   * Assembles an {@link IsoCodeVO} from the three flat column values.
   * Returns {@code null} when all three inputs are {@code null} (i.e. no ISO
   * data exists on the entity at all).  A partially-populated set of codes
   * is still assembled — the VO's own validation is responsible for deciding
   * whether that combination is legal.
   */
  static IsoCodeVO isoCodeVOFrom(String alpha2, String alpha3, String numeric) {
    if (alpha2 == null && alpha3 == null && numeric == null) {
      return null;
    }
    return IsoCodeVO.of(alpha2, alpha3, numeric);
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
