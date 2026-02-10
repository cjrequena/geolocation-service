package com.cjrequena.sample.domain.mapper;

import com.cjrequena.sample.domain.model.Region;
import com.cjrequena.sample.domain.model.enums.RegionType;
import com.cjrequena.sample.domain.model.vo.AuditInfoVO;
import com.cjrequena.sample.domain.model.vo.PopulationVO;
import com.cjrequena.sample.persistence.entity.CountryEntity;
import com.cjrequena.sample.persistence.entity.GeoShapeEntity;
import com.cjrequena.sample.persistence.entity.RegionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.TimeZone;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author cjrequena
 */
@ExtendWith(MockitoExtension.class)
class RegionMapperTest {

  // ── shared fixtures ────────────────────────────────────────────────────────

  private static final UUID        REGION_ID      = UUID.randomUUID();
  private static final UUID        COUNTRY_ID     = UUID.randomUUID();
  private static final UUID        GEOSHAPE_ID    = UUID.randomUUID();
  private static final String      NAME           = "California";
  private static final String      CODE           = "CA";
  private static final RegionType  TYPE           = RegionType.STATE;           // adjust to an actual enum constant
  private static final String      TYPE_STRING    = TYPE.getValue();
  private static final long        POPULATION_VAL = 39_538_223L;
  private static final TimeZone    TZ             = TimeZone.getTimeZone("America/Los_Angeles");
  private static final OffsetDateTime CREATED_AT  = OffsetDateTime.of(2024, 1, 15, 10, 30, 0, 0, ZoneOffset.UTC);
  private static final OffsetDateTime UPDATED_AT  = OffsetDateTime.of(2024, 6, 20, 14, 0, 0, 0, ZoneOffset.UTC);

  private RegionMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(RegionMapper.class);
  }

  // ── helpers that build fully-populated fixtures ───────────────────────────

  /** Builds a {@link Region} with every field populated. */
  private static Region fullDomain() {
    return Region.builder()
      .id(REGION_ID)
      .countryId(COUNTRY_ID)
      .geoShapeId(GEOSHAPE_ID)
      .name(NAME)
      .code(CODE)
      .type(TYPE)
      .population(PopulationVO.of(POPULATION_VAL))
      .timeZone(TZ)
      .active(Boolean.TRUE)
      .auditInfo(AuditInfoVO.of(CREATED_AT, UPDATED_AT))
      .build();
  }

  /** Builds a {@link RegionEntity} with every field populated. */
  private static RegionEntity fullEntity() {
    CountryEntity  country  = new CountryEntity();
    country.setId(COUNTRY_ID);

    GeoShapeEntity geoShape = new GeoShapeEntity();
    geoShape.setId(GEOSHAPE_ID);

    RegionEntity entity = new RegionEntity();
    entity.setId(REGION_ID);
    entity.setCountry(country);
    entity.setGeoShape(geoShape);
    entity.setName(NAME);
    entity.setCode(CODE);
    entity.setRegionType(TYPE_STRING);
    entity.setPopulation(POPULATION_VAL);
    entity.setTimeZone(TZ);
    entity.setActive(Boolean.TRUE);
    entity.setCreatedAt(CREATED_AT);
    entity.setUpdatedAt(UPDATED_AT);
    return entity;
  }

  // ================================================================
  // Entity  →  Domain
  // ================================================================

  @Nested
  @DisplayName("toDomain – Entity → Domain")
  class ToDomain {

    @Test
    @DisplayName("maps every field correctly from a fully populated entity")
    void fullMapping() {
      Region domain = mapper.toDomain(fullEntity());

      assertThat(domain).isNotNull();
      assertThat(domain.getId())            .isEqualTo(REGION_ID);
      assertThat(domain.getCountryId())     .isEqualTo(COUNTRY_ID);
      assertThat(domain.getGeoShapeId())    .isEqualTo(GEOSHAPE_ID);
      assertThat(domain.getName())          .isEqualTo(NAME);
      assertThat(domain.getCode())          .isEqualTo(CODE);
      assertThat(domain.getType())          .isEqualTo(TYPE);
      assertThat(domain.getPopulation())    .isNotNull();
      assertThat(domain.getPopulation().getValue()).isEqualTo(POPULATION_VAL);
      assertThat(domain.getTimeZone())      .isEqualTo(TZ);
      assertThat(domain.isActive())        .isTrue();
      assertThat(domain.getAuditInfo())     .isNotNull();
      assertThat(domain.getAuditInfo().getCreatedAt()).isEqualTo(CREATED_AT);
      assertThat(domain.getAuditInfo().getUpdatedAt()).isEqualTo(UPDATED_AT);
    }

    @Test
    @DisplayName("maps isActive=false to active=false")
    void inactiveStatus() {
      RegionEntity entity = fullEntity();
      entity.setActive(Boolean.FALSE);

      Region domain = mapper.toDomain(entity);

      assertThat(domain.isActive()).isFalse();
    }

    @Test
    @DisplayName("extracts countryId from the nested CountryEntity")
    void countryIdExtraction() {
      RegionEntity entity = fullEntity();
      // Verify the id comes from the nested object, not a flat column
      entity.getCountry().setId(COUNTRY_ID);

      Region domain = mapper.toDomain(entity);

      assertThat(domain.getCountryId()).isEqualTo(COUNTRY_ID);
    }

    @Test
    @DisplayName("extracts geoShapeId from the nested GeoShapeEntity")
    void geoShapeIdExtraction() {
      RegionEntity entity = fullEntity();
      entity.getGeoShape().setId(GEOSHAPE_ID);

      Region domain = mapper.toDomain(entity);

      assertThat(domain.getGeoShapeId()).isEqualTo(GEOSHAPE_ID);
    }
  }

  // ================================================================
  // Domain  →  Entity
  // ================================================================

  @Nested
  @DisplayName("toEntity – Domain → Entity")
  class ToEntity {

    @Test
    @DisplayName("maps every field correctly from a fully populated domain object")
    void fullMapping() {
      RegionEntity entity = mapper.toEntity(fullDomain());

      assertThat(entity).isNotNull();
      assertThat(entity.getId())                        .isEqualTo(REGION_ID);
      assertThat(entity.getCountry())                   .isNotNull();
      assertThat(entity.getCountry().getId())           .isEqualTo(COUNTRY_ID);
      assertThat(entity.getGeoShape())                  .isNotNull();
      assertThat(entity.getGeoShape().getId())          .isEqualTo(GEOSHAPE_ID);
      assertThat(entity.getName())                      .isEqualTo(NAME);
      assertThat(entity.getCode())                      .isEqualTo(CODE);
      assertThat(entity.getRegionType())                .isEqualTo(TYPE_STRING);
      assertThat(entity.getPopulation())                .isEqualTo(POPULATION_VAL);
      assertThat(entity.getTimeZone())                  .isEqualTo(TZ);
      assertThat(entity.getActive())                  .isTrue();
      assertThat(entity.getCreatedAt())                 .isEqualTo(CREATED_AT);
      assertThat(entity.getUpdatedAt())                 .isEqualTo(UPDATED_AT);
    }

    @Test
    @DisplayName("creates a CountryEntity shell that contains only the id")
    void countryShellContainsOnlyId() {
      RegionEntity entity = mapper.toEntity(fullDomain());

      CountryEntity country = entity.getCountry();
      assertThat(country).isNotNull();
      assertThat(country.getId()).isEqualTo(COUNTRY_ID);
      // Shell should have no other state loaded — name is the most obvious indicator
      assertThat(country.getName()).isNull();
    }

    @Test
    @DisplayName("creates a GeoShapeEntity shell that contains only the id")
    void geoShapeShellContainsOnlyId() {
      RegionEntity entity = mapper.toEntity(fullDomain());

      GeoShapeEntity geoShape = entity.getGeoShape();
      assertThat(geoShape).isNotNull();
      assertThat(geoShape.getId()).isEqualTo(GEOSHAPE_ID);
    }

    @Test
    @DisplayName("maps active=false to isActive=false")
    void inactiveStatus() {
      Region domain = fullDomain();
      domain.setActive(Boolean.FALSE);

      RegionEntity entity = mapper.toEntity(domain);

      assertThat(entity.getActive()).isFalse();
    }
  }

  // ================================================================
  // Null source objects
  // ================================================================

  @Nested
  @DisplayName("Null input handling")
  class NullInputs {

    @Test
    @DisplayName("toEntity returns null when source domain is null")
    void toEntityReturnsNullForNullDomain() {
      assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    @DisplayName("toDomain returns null when source entity is null")
    void toDomainReturnsNullForNullEntity() {
      assertThat(mapper.toDomain(null)).isNull();
    }
  }

  // ================================================================
  // Each optional field set to null individually
  // ================================================================

  @Nested
  @DisplayName("Nullable fields – each optional field null in isolation")
  class NullableFields {

    // ── toDomain direction ─────────────────────────────────────────

    @Test
    @DisplayName("toDomain: geoShape=null  →  geoShapeId=null")
    void toDomain_geoShapeNull() {
      RegionEntity entity = fullEntity();
      entity.setGeoShape(null);

      Region domain = mapper.toDomain(entity);

      assertThat(domain.getGeoShapeId()).isNull();
    }

    @Test
    @DisplayName("toDomain: population=null  →  population VO=null")
    void toDomain_populationNull() {
      RegionEntity entity = fullEntity();
      entity.setPopulation(null);

      Region domain = mapper.toDomain(entity);

      assertThat(domain.getPopulation()).isNull();
    }

    @Test
    @DisplayName("toDomain: regionType=null  →  type=null")
    void toDomain_regionTypeNull() {
      RegionEntity entity = fullEntity();
      entity.setRegionType(null);

      Region domain = mapper.toDomain(entity);

      assertThat(domain.getType()).isNull();
    }

    @Test
    @DisplayName("toDomain: both timestamps null  →  auditInfo=null")
    void toDomain_auditTimestampsNull() {
      RegionEntity entity = fullEntity();
      entity.setCreatedAt(null);
      entity.setUpdatedAt(null);

      Region domain = mapper.toDomain(entity);

      assertThat(domain.getAuditInfo()).isNull();
    }

    @Test
    @DisplayName("toDomain: timeZone=null  →  timeZone=null")
    void toDomain_timeZoneNull() {
      RegionEntity entity = fullEntity();
      entity.setTimeZone(null);

      Region domain = mapper.toDomain(entity);

      assertThat(domain.getTimeZone()).isNull();
    }

    @Test
    @DisplayName("toDomain: country=null  →  countryId=null  (defensive, no NPE)")
    void toDomain_countryNull() {
      RegionEntity entity = fullEntity();
      entity.setCountry(null);

      Region domain = mapper.toDomain(entity);

      assertThat(domain.getCountryId()).isNull();
    }

    // ── toEntity direction ─────────────────────────────────────────

    @Test
    @DisplayName("toEntity: geoShapeId=null  →  geoShape=null")
    void toEntity_geoShapeIdNull() {
      Region domain = fullDomain();
      domain.setGeoShapeId(null);

      RegionEntity entity = mapper.toEntity(domain);

      assertThat(entity.getGeoShape()).isNull();
    }

    @Test
    @DisplayName("toEntity: population=null  →  population column=null")
    void toEntity_populationNull() {
      Region domain = fullDomain();
      domain.setPopulation(null);

      RegionEntity entity = mapper.toEntity(domain);

      assertThat(entity.getPopulation()).isNull();
    }

    @Test
    @DisplayName("toEntity: type=null  →  regionType=null")
    void toEntity_typeNull() {
      Region domain = fullDomain();
      domain.setType(null);

      RegionEntity entity = mapper.toEntity(domain);

      assertThat(entity.getRegionType()).isNull();
    }

    @Test
    @DisplayName("toEntity: auditInfo=null  →  createdAt and updatedAt=null")
    void toEntity_auditInfoNull() {
      Region domain = fullDomain();
      domain.setAuditInfo(null);

      RegionEntity entity = mapper.toEntity(domain);

      assertThat(entity.getCreatedAt()).isNull();
      assertThat(entity.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("toEntity: timeZone=null  →  timeZone=null")
    void toEntity_timeZoneNull() {
      Region domain = fullDomain();
      domain.setTimeZone(null);

      RegionEntity entity = mapper.toEntity(domain);

      assertThat(entity.getTimeZone()).isNull();
    }

    @Test
    @DisplayName("toEntity: countryId=null  →  country shell=null  (defensive)")
    void toEntity_countryIdNull() {
      Region domain = fullDomain();
      domain.setCountryId(null);

      RegionEntity entity = mapper.toEntity(domain);

      assertThat(entity.getCountry()).isNull();
    }
  }

  // ================================================================
  // Round-trip: Domain → Entity → Domain
  // ================================================================

  @Nested
  @DisplayName("Round-trip fidelity")
  class RoundTrip {

    @Test
    @DisplayName("full domain survives Domain → Entity → Domain without data loss")
    void fullRoundTrip() {
      Region original = fullDomain();

      Region recovered = mapper.toDomain(mapper.toEntity(original));

      assertThat(recovered.getId())            .isEqualTo(original.getId());
      assertThat(recovered.getCountryId())     .isEqualTo(original.getCountryId());
      assertThat(recovered.getGeoShapeId())    .isEqualTo(original.getGeoShapeId());
      assertThat(recovered.getName())          .isEqualTo(original.getName());
      assertThat(recovered.getCode())          .isEqualTo(original.getCode());
      assertThat(recovered.getType())          .isEqualTo(original.getType());
      assertThat(recovered.getTimeZone())      .isEqualTo(original.getTimeZone());
      assertThat(recovered.isActive())        .isEqualTo(original.isActive());
      assertThat(recovered.getPopulation().getValue())
                                               .isEqualTo(original.getPopulation().getValue());
      assertThat(recovered.getAuditInfo().getCreatedAt())
                                               .isEqualTo(original.getAuditInfo().getCreatedAt());
      assertThat(recovered.getAuditInfo().getUpdatedAt())
                                               .isEqualTo(original.getAuditInfo().getUpdatedAt());
    }

    @Test
    @DisplayName("minimal domain (only mandatory fields) survives round-trip")
    void minimalRoundTrip() {
      Region minimal = Region.builder()
        .id(REGION_ID)
        .countryId(COUNTRY_ID)
        .name(NAME)
        .active(Boolean.TRUE)
        .build();

      Region recovered = mapper.toDomain(mapper.toEntity(minimal));

      assertThat(recovered.getId())         .isEqualTo(REGION_ID);
      assertThat(recovered.getCountryId())  .isEqualTo(COUNTRY_ID);
      assertThat(recovered.getName())       .isEqualTo(NAME);
      assertThat(recovered.isActive())     .isTrue();
      // optional fields stay null
      assertThat(recovered.getGeoShapeId()) .isNull();
      assertThat(recovered.getCode())       .isNull();
      assertThat(recovered.getType())       .isNull();
      assertThat(recovered.getPopulation()) .isNull();
      assertThat(recovered.getAuditInfo())  .isNull();
      assertThat(recovered.getTimeZone())   .isNull();
    }
  }

  // ================================================================
  // Static helper methods exercised directly
  // ================================================================

  @Nested
  @DisplayName("Static conversion helpers")
  class StaticHelpers {

    // ── uuidToCountryEntity ────────────────────────────────────────

    @Test
    @DisplayName("uuidToCountryEntity: valid UUID produces shell with that id")
    void uuidToCountryEntity_valid() {
      CountryEntity shell = RegionMapper.uuidToCountryEntity(COUNTRY_ID);

      assertThat(shell).isNotNull();
      assertThat(shell.getId()).isEqualTo(COUNTRY_ID);
    }

    @Test
    @DisplayName("uuidToCountryEntity: null UUID returns null")
    void uuidToCountryEntity_null() {
      assertThat(RegionMapper.uuidToCountryEntity(null)).isNull();
    }

    // ── uuidToGeoShapeEntity ───────────────────────────────────────

    @Test
    @DisplayName("uuidToGeoShapeEntity: valid UUID produces shell with that id")
    void uuidToGeoShapeEntity_valid() {
      GeoShapeEntity shell = RegionMapper.uuidToGeoShapeEntity(GEOSHAPE_ID);

      assertThat(shell).isNotNull();
      assertThat(shell.getId()).isEqualTo(GEOSHAPE_ID);
    }

    @Test
    @DisplayName("uuidToGeoShapeEntity: null UUID returns null")
    void uuidToGeoShapeEntity_null() {
      assertThat(RegionMapper.uuidToGeoShapeEntity(null)).isNull();
    }

    // ── countryEntityToUuid ────────────────────────────────────────

    @Test
    @DisplayName("countryEntityToUuid: extracts id from populated entity")
    void countryEntityToUuid_valid() {
      CountryEntity entity = new CountryEntity();
      entity.setId(COUNTRY_ID);

      assertThat(RegionMapper.countryEntityToUuid(entity)).isEqualTo(COUNTRY_ID);
    }

    @Test
    @DisplayName("countryEntityToUuid: returns null when entity is null")
    void countryEntityToUuid_null() {
      assertThat(RegionMapper.countryEntityToUuid(null)).isNull();
    }

    // ── geoShapeEntityToUuid ───────────────────────────────────────

    @Test
    @DisplayName("geoShapeEntityToUuid: extracts id from populated entity")
    void geoShapeEntityToUuid_valid() {
      GeoShapeEntity entity = new GeoShapeEntity();
      entity.setId(GEOSHAPE_ID);

      assertThat(RegionMapper.geoShapeEntityToUuid(entity)).isEqualTo(GEOSHAPE_ID);
    }

    @Test
    @DisplayName("geoShapeEntityToUuid: returns null when entity is null")
    void geoShapeEntityToUuid_null() {
      assertThat(RegionMapper.geoShapeEntityToUuid(null)).isNull();
    }

    // ── regionTypeToString / stringToRegionType ────────────────────

    @Test
    @DisplayName("regionTypeToString: converts enum to its getValue() string")
    void regionTypeToString_valid() {
      assertThat(RegionMapper.regionTypeToString(TYPE)).isEqualTo(TYPE_STRING);
    }

    @Test
    @DisplayName("regionTypeToString: null enum returns null")
    void regionTypeToString_null() {
      assertThat(RegionMapper.regionTypeToString(null)).isNull();
    }

    @Test
    @DisplayName("stringToRegionType: recovers the original enum constant")
    void stringToRegionType_valid() {
      assertThat(RegionMapper.stringToRegionType(TYPE_STRING)).isEqualTo(TYPE);
    }

    @Test
    @DisplayName("stringToRegionType: null string returns null")
    void stringToRegionType_null() {
      assertThat(RegionMapper.stringToRegionType(null)).isNull();
    }

    @Test
    @DisplayName("RegionType round-trip: getValue → fromValue recovers original")
    void regionTypeRoundTrip() {
      for (RegionType type : RegionType.values()) {
        assertThat(RegionMapper.stringToRegionType(RegionMapper.regionTypeToString(type)))
          .isEqualTo(type);
      }
    }

    // ── populationVOToLong / longToPopulationVO ────────────────────

    @Test
    @DisplayName("populationVOToLong: extracts the numeric value")
    void populationVOToLong_valid() {
      assertThat(RegionMapper.populationVOToLong(PopulationVO.of(POPULATION_VAL)))
        .isEqualTo(POPULATION_VAL);
    }

    @Test
    @DisplayName("populationVOToLong: null VO returns null")
    void populationVOToLong_null() {
      assertThat(RegionMapper.populationVOToLong(null)).isNull();
    }

    @Test
    @DisplayName("longToPopulationVO: wraps the Long into a VO")
    void longToPopulationVO_valid() {
      PopulationVO vo = RegionMapper.longToPopulationVO(POPULATION_VAL);

      assertThat(vo).isNotNull();
      assertThat(vo.getValue()).isEqualTo(POPULATION_VAL);
    }

    @Test
    @DisplayName("longToPopulationVO: null Long returns null")
    void longToPopulationVO_null() {
      assertThat(RegionMapper.longToPopulationVO(null)).isNull();
    }

    @Test
    @DisplayName("Population round-trip: Long → VO → Long recovers original value")
    void populationRoundTrip() {
      Long recovered = RegionMapper.populationVOToLong(
        RegionMapper.longToPopulationVO(POPULATION_VAL)
      );
      assertThat(recovered).isEqualTo(POPULATION_VAL);
    }
  }
}
