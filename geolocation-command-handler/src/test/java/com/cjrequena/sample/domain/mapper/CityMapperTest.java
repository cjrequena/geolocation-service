package com.cjrequena.sample.domain.mapper;

import com.cjrequena.sample.domain.model.aggregate.City;
import com.cjrequena.sample.domain.model.vo.AuditInfoVO;
import com.cjrequena.sample.domain.model.vo.PopulationVO;
import com.cjrequena.sample.persistence.entity.CityEntity;
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
 * Unit tests for {@link CityMapper}.
 *
 * <p>Uses the MapStruct-generated implementation obtained via
 * {@link Mappers#getMapper(Class)} — no Spring context required.</p>
 *
 * <h3>Structure</h3>
 * <ul>
 *   <li>{@link ToDomain}       – Entity → Domain happy-path.</li>
 *   <li>{@link ToEntity}       – Domain → Entity happy-path + shell verification.</li>
 *   <li>{@link NullInputs}     – Both directions with a {@code null} source.</li>
 *   <li>{@link NullableFields} – Every optional field {@code null} in isolation.</li>
 *   <li>{@link RoundTrip}      – Domain → Entity → Domain fidelity (full + minimal).</li>
 *   <li>{@link StaticHelpers}  – Every public static helper exercised directly,
 *                                 including the {@code TimeZone} edge cases.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class CityMapperTest {

  // ── shared fixtures ────────────────────────────────────────────────────────

  private static final UUID            CITY_ID        = UUID.randomUUID();
  private static final UUID            REGION_ID      = UUID.randomUUID();
  private static final UUID            GEOSHAPE_ID    = UUID.randomUUID();
  private static final String          NAME           = "San Francisco";
  private static final String          POSTAL_CODE    = "94102";
  private static final long            POPULATION_VAL = 873_965L;
  private static final TimeZone        TZ             = TimeZone.getTimeZone("America/Los_Angeles");
  private static final String          TZ_ID          = "America/Los_Angeles";
  private static final OffsetDateTime  CREATED_AT     = OffsetDateTime.of(2024, 3, 10, 8, 0, 0, 0, ZoneOffset.UTC);
  private static final OffsetDateTime  UPDATED_AT     = OffsetDateTime.of(2024, 9, 5, 12, 30, 0, 0, ZoneOffset.UTC);

  private CityMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(CityMapper.class);
  }

  // ── fixture builders ───────────────────────────────────────────────────────

  /** Fully populated {@link City} domain object. */
  private static City fullDomain() {
    return City.builder()
      .id(CITY_ID)
      .regionId(REGION_ID)
      .geoShapeId(GEOSHAPE_ID)
      .name(NAME)
      .postalCode(POSTAL_CODE)
      .population(PopulationVO.of(POPULATION_VAL))
      .timezone(TZ)
      .capital(Boolean.TRUE)
      .status(Boolean.TRUE)
      .auditInfo(AuditInfoVO.of(CREATED_AT, UPDATED_AT))
      .build();
  }

  /** Fully populated {@link CityEntity}. */
  private static CityEntity fullEntity() {
    RegionEntity    region   = new RegionEntity();
    region.setId(REGION_ID);

    GeoShapeEntity  geoShape = new GeoShapeEntity();
    geoShape.setId(GEOSHAPE_ID);

    CityEntity entity = new CityEntity();
    entity.setId(CITY_ID);
    entity.setRegion(region);
    entity.setGeoShape(geoShape);
    entity.setName(NAME);
    entity.setPostalCode(POSTAL_CODE);
    entity.setPopulation(POPULATION_VAL);
    entity.setTimezone(TZ_ID);
    entity.setIsCapital(Boolean.TRUE);
    entity.setIsActive(Boolean.TRUE);
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
      City domain = mapper.toDomain(fullEntity());

      assertThat(domain).isNotNull();
      assertThat(domain.getId())                          .isEqualTo(CITY_ID);
      assertThat(domain.getRegionId())                    .isEqualTo(REGION_ID);
      assertThat(domain.getGeoShapeId())                  .isEqualTo(GEOSHAPE_ID);
      assertThat(domain.getName())                        .isEqualTo(NAME);
      assertThat(domain.getPostalCode())                  .isEqualTo(POSTAL_CODE);
      assertThat(domain.getPopulation())                  .isNotNull();
      assertThat(domain.getPopulation().getValue())       .isEqualTo(POPULATION_VAL);
      assertThat(domain.getTimezone())                    .isEqualTo(TZ);
      assertThat(domain.getCapital())                     .isTrue();
      assertThat(domain.getStatus())                      .isTrue();
      assertThat(domain.getAuditInfo())                   .isNotNull();
      assertThat(domain.getAuditInfo().getCreatedAt())    .isEqualTo(CREATED_AT);
      assertThat(domain.getAuditInfo().getUpdatedAt())    .isEqualTo(UPDATED_AT);
    }

    @Test
    @DisplayName("maps isActive=false  →  status=false")
    void inactiveStatus() {
      CityEntity entity = fullEntity();
      entity.setIsActive(Boolean.FALSE);

      assertThat(mapper.toDomain(entity).getStatus()).isFalse();
    }

    @Test
    @DisplayName("maps isCapital=false  →  capital=false")
    void notCapital() {
      CityEntity entity = fullEntity();
      entity.setIsCapital(Boolean.FALSE);

      assertThat(mapper.toDomain(entity).getCapital()).isFalse();
    }

    @Test
    @DisplayName("extracts regionId from the nested RegionEntity")
    void regionIdExtraction() {
      City domain = mapper.toDomain(fullEntity());

      assertThat(domain.getRegionId()).isEqualTo(REGION_ID);
    }

    @Test
    @DisplayName("extracts geoShapeId from the nested GeoShapeEntity")
    void geoShapeIdExtraction() {
      City domain = mapper.toDomain(fullEntity());

      assertThat(domain.getGeoShapeId()).isEqualTo(GEOSHAPE_ID);
    }

    @Test
    @DisplayName("converts the IANA zone-id string back to the matching TimeZone")
    void timezoneConversion() {
      City domain = mapper.toDomain(fullEntity());

      assertThat(domain.getTimezone().getID()).isEqualTo(TZ_ID);
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
      CityEntity entity = mapper.toEntity(fullDomain());

      assertThat(entity).isNotNull();
      assertThat(entity.getId())                     .isEqualTo(CITY_ID);
      assertThat(entity.getRegion())                 .isNotNull();
      assertThat(entity.getRegion().getId())         .isEqualTo(REGION_ID);
      assertThat(entity.getGeoShape())               .isNotNull();
      assertThat(entity.getGeoShape().getId())       .isEqualTo(GEOSHAPE_ID);
      assertThat(entity.getName())                   .isEqualTo(NAME);
      assertThat(entity.getPostalCode())             .isEqualTo(POSTAL_CODE);
      assertThat(entity.getPopulation())             .isEqualTo(POPULATION_VAL);
      assertThat(entity.getTimezone())               .isEqualTo(TZ_ID);
      assertThat(entity.getIsCapital())              .isTrue();
      assertThat(entity.getIsActive())               .isTrue();
      assertThat(entity.getCreatedAt())              .isEqualTo(CREATED_AT);
      assertThat(entity.getUpdatedAt())              .isEqualTo(UPDATED_AT);
    }

    @Test
    @DisplayName("creates a RegionEntity shell that contains only the id")
    void regionShellContainsOnlyId() {
      CityEntity entity = mapper.toEntity(fullDomain());

      RegionEntity region = entity.getRegion();
      assertThat(region).isNotNull();
      assertThat(region.getId())  .isEqualTo(REGION_ID);
      assertThat(region.getName()).isNull();   // nothing else populated
    }

    @Test
    @DisplayName("creates a GeoShapeEntity shell that contains only the id")
    void geoShapeShellContainsOnlyId() {
      CityEntity entity = mapper.toEntity(fullDomain());

      GeoShapeEntity geoShape = entity.getGeoShape();
      assertThat(geoShape).isNotNull();
      assertThat(geoShape.getId()).isEqualTo(GEOSHAPE_ID);
    }

    @Test
    @DisplayName("maps status=false  →  isActive=false")
    void inactiveStatus() {
      City domain = fullDomain();
      domain.setStatus(Boolean.FALSE);

      assertThat(mapper.toEntity(domain).getIsActive()).isFalse();
    }

    @Test
    @DisplayName("maps capital=false  →  isCapital=false")
    void notCapital() {
      City domain = fullDomain();
      domain.setCapital(Boolean.FALSE);

      assertThat(mapper.toEntity(domain).getIsCapital()).isFalse();
    }

    @Test
    @DisplayName("converts TimeZone to its IANA zone-id string")
    void timezoneConversion() {
      CityEntity entity = mapper.toEntity(fullDomain());

      assertThat(entity.getTimezone()).isEqualTo(TZ_ID);
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
    void toEntityReturnsNull() {
      assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    @DisplayName("toDomain returns null when source entity is null")
    void toDomainReturnsNull() {
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
      CityEntity entity = fullEntity();
      entity.setGeoShape(null);

      assertThat(mapper.toDomain(entity).getGeoShapeId()).isNull();
    }

    @Test
    @DisplayName("toDomain: population=null  →  population VO=null")
    void toDomain_populationNull() {
      CityEntity entity = fullEntity();
      entity.setPopulation(null);

      assertThat(mapper.toDomain(entity).getPopulation()).isNull();
    }

    @Test
    @DisplayName("toDomain: timezone=null  →  timezone=null")
    void toDomain_timezoneNull() {
      CityEntity entity = fullEntity();
      entity.setTimezone(null);

      assertThat(mapper.toDomain(entity).getTimezone()).isNull();
    }

    @Test
    @DisplayName("toDomain: postalCode=null  →  postalCode=null")
    void toDomain_postalCodeNull() {
      CityEntity entity = fullEntity();
      entity.setPostalCode(null);

      assertThat(mapper.toDomain(entity).getPostalCode()).isNull();
    }

    @Test
    @DisplayName("toDomain: isCapital=null  →  capital=null")
    void toDomain_isCapitalNull() {
      CityEntity entity = fullEntity();
      entity.setIsCapital(null);

      assertThat(mapper.toDomain(entity).getCapital()).isNull();
    }

    @Test
    @DisplayName("toDomain: both timestamps null  →  auditInfo=null")
    void toDomain_auditTimestampsNull() {
      CityEntity entity = fullEntity();
      entity.setCreatedAt(null);
      entity.setUpdatedAt(null);

      assertThat(mapper.toDomain(entity).getAuditInfo()).isNull();
    }

    @Test
    @DisplayName("toDomain: region=null  →  regionId=null  (defensive, no NPE)")
    void toDomain_regionNull() {
      CityEntity entity = fullEntity();
      entity.setRegion(null);

      assertThat(mapper.toDomain(entity).getRegionId()).isNull();
    }

    // ── toEntity direction ─────────────────────────────────────────

    @Test
    @DisplayName("toEntity: geoShapeId=null  →  geoShape=null")
    void toEntity_geoShapeIdNull() {
      City domain = fullDomain();
      domain.setGeoShapeId(null);

      assertThat(mapper.toEntity(domain).getGeoShape()).isNull();
    }

    @Test
    @DisplayName("toEntity: population=null  →  population column=null")
    void toEntity_populationNull() {
      City domain = fullDomain();
      domain.setPopulation(null);

      assertThat(mapper.toEntity(domain).getPopulation()).isNull();
    }

    @Test
    @DisplayName("toEntity: timezone=null  →  timezone column=null")
    void toEntity_timezoneNull() {
      City domain = fullDomain();
      domain.setTimezone(null);

      assertThat(mapper.toEntity(domain).getTimezone()).isNull();
    }

    @Test
    @DisplayName("toEntity: postalCode=null  →  postalCode=null")
    void toEntity_postalCodeNull() {
      City domain = fullDomain();
      domain.setPostalCode(null);

      assertThat(mapper.toEntity(domain).getPostalCode()).isNull();
    }

    @Test
    @DisplayName("toEntity: capital=null  →  isCapital=null")
    void toEntity_capitalNull() {
      City domain = fullDomain();
      domain.setCapital(null);

      assertThat(mapper.toEntity(domain).getIsCapital()).isNull();
    }

    @Test
    @DisplayName("toEntity: auditInfo=null  →  createdAt and updatedAt=null")
    void toEntity_auditInfoNull() {
      City domain = fullDomain();
      domain.setAuditInfo(null);

      CityEntity entity = mapper.toEntity(domain);

      assertThat(entity.getCreatedAt()).isNull();
      assertThat(entity.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("toEntity: regionId=null  →  region shell=null  (defensive)")
    void toEntity_regionIdNull() {
      City domain = fullDomain();
      domain.setRegionId(null);

      assertThat(mapper.toEntity(domain).getRegion()).isNull();
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
      City original = fullDomain();

      City recovered = mapper.toDomain(mapper.toEntity(original));

      assertThat(recovered.getId())                          .isEqualTo(original.getId());
      assertThat(recovered.getRegionId())                    .isEqualTo(original.getRegionId());
      assertThat(recovered.getGeoShapeId())                  .isEqualTo(original.getGeoShapeId());
      assertThat(recovered.getName())                        .isEqualTo(original.getName());
      assertThat(recovered.getPostalCode())                  .isEqualTo(original.getPostalCode());
      assertThat(recovered.getCapital())                     .isEqualTo(original.getCapital());
      assertThat(recovered.getStatus())                      .isEqualTo(original.getStatus());
      assertThat(recovered.getTimezone())                    .isEqualTo(original.getTimezone());
      assertThat(recovered.getPopulation().getValue())       .isEqualTo(original.getPopulation().getValue());
      assertThat(recovered.getAuditInfo().getCreatedAt())    .isEqualTo(original.getAuditInfo().getCreatedAt());
      assertThat(recovered.getAuditInfo().getUpdatedAt())    .isEqualTo(original.getAuditInfo().getUpdatedAt());
    }

    @Test
    @DisplayName("minimal domain (only mandatory fields) survives round-trip")
    void minimalRoundTrip() {
      City minimal = City.builder()
        .id(CITY_ID)
        .regionId(REGION_ID)
        .name(NAME)
        .status(Boolean.TRUE)
        .capital(Boolean.FALSE)
        .build();

      City recovered = mapper.toDomain(mapper.toEntity(minimal));

      assertThat(recovered.getId())         .isEqualTo(CITY_ID);
      assertThat(recovered.getRegionId())   .isEqualTo(REGION_ID);
      assertThat(recovered.getName())       .isEqualTo(NAME);
      assertThat(recovered.getStatus())     .isTrue();
      assertThat(recovered.getCapital())    .isFalse();
      // optional fields stay null
      assertThat(recovered.getGeoShapeId()) .isNull();
      assertThat(recovered.getPostalCode()) .isNull();
      assertThat(recovered.getPopulation()) .isNull();
      assertThat(recovered.getTimezone())   .isNull();
      assertThat(recovered.getAuditInfo())  .isNull();
    }
  }

  // ================================================================
  // Static helper methods exercised directly
  // ================================================================

  @Nested
  @DisplayName("Static conversion helpers")
  class StaticHelpers {

    // ── uuidToRegionEntity ─────────────────────────────────────────

    @Test
    @DisplayName("uuidToRegionEntity: valid UUID  →  shell with that id")
    void uuidToRegionEntity_valid() {
      RegionEntity shell = CityMapper.uuidToRegionEntity(REGION_ID);

      assertThat(shell).isNotNull();
      assertThat(shell.getId()).isEqualTo(REGION_ID);
    }

    @Test
    @DisplayName("uuidToRegionEntity: null  →  null")
    void uuidToRegionEntity_null() {
      assertThat(CityMapper.uuidToRegionEntity(null)).isNull();
    }

    // ── uuidToGeoShapeEntity ───────────────────────────────────────

    @Test
    @DisplayName("uuidToGeoShapeEntity: valid UUID  →  shell with that id")
    void uuidToGeoShapeEntity_valid() {
      GeoShapeEntity shell = CityMapper.uuidToGeoShapeEntity(GEOSHAPE_ID);

      assertThat(shell).isNotNull();
      assertThat(shell.getId()).isEqualTo(GEOSHAPE_ID);
    }

    @Test
    @DisplayName("uuidToGeoShapeEntity: null  →  null")
    void uuidToGeoShapeEntity_null() {
      assertThat(CityMapper.uuidToGeoShapeEntity(null)).isNull();
    }

    // ── regionEntityToUuid ─────────────────────────────────────────

    @Test
    @DisplayName("regionEntityToUuid: populated entity  →  its id")
    void regionEntityToUuid_valid() {
      RegionEntity entity = new RegionEntity();
      entity.setId(REGION_ID);

      assertThat(CityMapper.regionEntityToUuid(entity)).isEqualTo(REGION_ID);
    }

    @Test
    @DisplayName("regionEntityToUuid: null entity  →  null")
    void regionEntityToUuid_null() {
      assertThat(CityMapper.regionEntityToUuid(null)).isNull();
    }

    // ── geoShapeEntityToUuid ───────────────────────────────────────

    @Test
    @DisplayName("geoShapeEntityToUuid: populated entity  →  its id")
    void geoShapeEntityToUuid_valid() {
      GeoShapeEntity entity = new GeoShapeEntity();
      entity.setId(GEOSHAPE_ID);

      assertThat(CityMapper.geoShapeEntityToUuid(entity)).isEqualTo(GEOSHAPE_ID);
    }

    @Test
    @DisplayName("geoShapeEntityToUuid: null entity  →  null")
    void geoShapeEntityToUuid_null() {
      assertThat(CityMapper.geoShapeEntityToUuid(null)).isNull();
    }

    // ── populationVOToLong / longToPopulationVO ────────────────────

    @Test
    @DisplayName("populationVOToLong: extracts the numeric value")
    void populationVOToLong_valid() {
      assertThat(CityMapper.populationVOToLong(PopulationVO.of(POPULATION_VAL)))
        .isEqualTo(POPULATION_VAL);
    }

    @Test
    @DisplayName("populationVOToLong: null VO  →  null")
    void populationVOToLong_null() {
      assertThat(CityMapper.populationVOToLong(null)).isNull();
    }

    @Test
    @DisplayName("longToPopulationVO: wraps the Long into a VO")
    void longToPopulationVO_valid() {
      PopulationVO vo = CityMapper.longToPopulationVO(POPULATION_VAL);

      assertThat(vo).isNotNull();
      assertThat(vo.getValue()).isEqualTo(POPULATION_VAL);
    }

    @Test
    @DisplayName("longToPopulationVO: null Long  →  null")
    void longToPopulationVO_null() {
      assertThat(CityMapper.longToPopulationVO(null)).isNull();
    }

    @Test
    @DisplayName("Population round-trip: Long → VO → Long recovers original value")
    void populationRoundTrip() {
      Long recovered = CityMapper.populationVOToLong(
        CityMapper.longToPopulationVO(POPULATION_VAL)
      );

      assertThat(recovered).isEqualTo(POPULATION_VAL);
    }

    // ── timeZoneToString / stringToTimeZone ────────────────────────

    @Test
    @DisplayName("timeZoneToString: converts TimeZone to its IANA zone-id")
    void timeZoneToString_valid() {
      assertThat(CityMapper.timeZoneToString(TZ)).isEqualTo(TZ_ID);
    }

    @Test
    @DisplayName("timeZoneToString: null TimeZone  →  null")
    void timeZoneToString_null() {
      assertThat(CityMapper.timeZoneToString(null)).isNull();
    }

    @Test
    @DisplayName("stringToTimeZone: recovers a TimeZone whose id matches the input")
    void stringToTimeZone_valid() {
      TimeZone tz = CityMapper.stringToTimeZone(TZ_ID);

      assertThat(tz).isNotNull();
      assertThat(tz.getID()).isEqualTo(TZ_ID);
    }

    @Test
    @DisplayName("stringToTimeZone: null string  →  null")
    void stringToTimeZone_null() {
      assertThat(CityMapper.stringToTimeZone(null)).isNull();
    }

    /**
     * {@link TimeZone#getTimeZone(String)} never returns {@code null};
     * an unrecognised id silently falls back to GMT.  This test documents
     * that behaviour so the team is aware if validation is ever added.
     */
    @Test
    @DisplayName("stringToTimeZone: unrecognised zone-id  →  falls back to GMT (JDK behaviour)")
    void stringToTimeZone_unknownIdFallsBackToGmt() {
      TimeZone tz = CityMapper.stringToTimeZone("Not/A/Real/Zone");

      assertThat(tz).isNotNull();
      assertThat(tz.getID()).isEqualTo("GMT");
    }

    @Test
    @DisplayName("TimeZone round-trip: TimeZone → String → TimeZone preserves zone-id")
    void timeZoneRoundTrip() {
      TimeZone recovered = CityMapper.stringToTimeZone(
        CityMapper.timeZoneToString(TZ)
      );

      assertThat(recovered).isNotNull();
      assertThat(recovered.getID()).isEqualTo(TZ.getID());
    }
  }
}
