package com.cjrequena.sample.domain.mapper;

import com.cjrequena.sample.domain.model.Area;
import com.cjrequena.sample.domain.model.enums.AreaType;
import com.cjrequena.sample.domain.model.vo.AuditInfoVO;
import com.cjrequena.sample.domain.model.vo.PopulationVO;
import com.cjrequena.sample.persistence.entity.AreaEntity;
import com.cjrequena.sample.persistence.entity.CityEntity;
import com.cjrequena.sample.persistence.entity.GeoShapeEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 *
 * @author cjrequena
 */
@ExtendWith(MockitoExtension.class)
class AreaMapperTest {

  // ── shared fixtures ────────────────────────────────────────────────────────

  private static final UUID            AREA_ID        = UUID.randomUUID();
  private static final UUID            CITY_ID        = UUID.randomUUID();
  private static final UUID            GEOSHAPE_ID    = UUID.randomUUID();
  private static final String          NAME           = "Mission District";
  private static final String          POSTAL_CODE    = "94110";
  private static final AreaType        TYPE           = AreaType.DISTRICT;       // adjust to an actual enum constant
  private static final String          TYPE_STRING    = TYPE.getValue();
  private static final long            POPULATION_VAL = 48_000L;
  private static final OffsetDateTime  CREATED_AT     = OffsetDateTime.of(2024, 4, 12, 7, 30, 0, 0, ZoneOffset.UTC);
  private static final OffsetDateTime  UPDATED_AT     = OffsetDateTime.of(2024, 11, 1, 16, 0, 0, 0, ZoneOffset.UTC);

  private AreaMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(AreaMapper.class);
  }

  // ── fixture builders ───────────────────────────────────────────────────────

  /** Fully populated {@link Area} domain object. */
  private static Area fullDomain() {
    return Area.builder()
      .id(AREA_ID)
      .cityId(CITY_ID)
      .geoShapeId(GEOSHAPE_ID)
      .name(NAME)
      .postalCode(POSTAL_CODE)
      .type(TYPE)
      .population(PopulationVO.of(POPULATION_VAL))
      .active(Boolean.TRUE)
      .auditInfo(AuditInfoVO.of(CREATED_AT, UPDATED_AT))
      .build();
  }

  /** Fully populated {@link AreaEntity}. */
  private static AreaEntity fullEntity() {
    CityEntity      city     = new CityEntity();
    city.setId(CITY_ID);

    GeoShapeEntity  geoShape = new GeoShapeEntity();
    geoShape.setId(GEOSHAPE_ID);

    AreaEntity entity = new AreaEntity();
    entity.setId(AREA_ID);
    entity.setCity(city);
    entity.setGeoShape(geoShape);
    entity.setName(NAME);
    entity.setPostalCode(POSTAL_CODE);
    entity.setAreaType(TYPE_STRING);
    entity.setPopulation(POPULATION_VAL);
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
      Area domain = mapper.toDomain(fullEntity());

      assertThat(domain).isNotNull();
      assertThat(domain.getId())                          .isEqualTo(AREA_ID);
      assertThat(domain.getCityId())                      .isEqualTo(CITY_ID);
      assertThat(domain.getGeoShapeId())                  .isEqualTo(GEOSHAPE_ID);
      assertThat(domain.getName())                        .isEqualTo(NAME);
      assertThat(domain.getPostalCode())                  .isEqualTo(POSTAL_CODE);
      assertThat(domain.getType())                        .isEqualTo(TYPE);
      assertThat(domain.getPopulation())                  .isNotNull();
      assertThat(domain.getPopulation().getValue())       .isEqualTo(POPULATION_VAL);
      assertThat(domain.getActive())                      .isTrue();
      assertThat(domain.getAuditInfo())                   .isNotNull();
      assertThat(domain.getAuditInfo().getCreatedAt())    .isEqualTo(CREATED_AT);
      assertThat(domain.getAuditInfo().getUpdatedAt())    .isEqualTo(UPDATED_AT);
    }

    @Test
    @DisplayName("maps active=false correctly")
    void inactiveFlag() {
      AreaEntity entity = fullEntity();
      entity.setActive(Boolean.FALSE);

      assertThat(mapper.toDomain(entity).getActive()).isFalse();
    }

    @Test
    @DisplayName("extracts cityId from the nested CityEntity")
    void cityIdExtraction() {
      Area domain = mapper.toDomain(fullEntity());

      assertThat(domain.getCityId()).isEqualTo(CITY_ID);
    }

    @Test
    @DisplayName("extracts geoShapeId from the nested GeoShapeEntity")
    void geoShapeIdExtraction() {
      Area domain = mapper.toDomain(fullEntity());

      assertThat(domain.getGeoShapeId()).isEqualTo(GEOSHAPE_ID);
    }

    @Test
    @DisplayName("converts the areaType string back to the matching AreaType enum")
    void areaTypeConversion() {
      Area domain = mapper.toDomain(fullEntity());

      assertThat(domain.getType()).isEqualTo(TYPE);
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
      AreaEntity entity = mapper.toEntity(fullDomain());

      assertThat(entity).isNotNull();
      assertThat(entity.getId())                     .isEqualTo(AREA_ID);
      assertThat(entity.getCity())                   .isNotNull();
      assertThat(entity.getCity().getId())           .isEqualTo(CITY_ID);
      assertThat(entity.getGeoShape())               .isNotNull();
      assertThat(entity.getGeoShape().getId())       .isEqualTo(GEOSHAPE_ID);
      assertThat(entity.getName())                   .isEqualTo(NAME);
      assertThat(entity.getPostalCode())             .isEqualTo(POSTAL_CODE);
      assertThat(entity.getAreaType())               .isEqualTo(TYPE_STRING);
      assertThat(entity.getPopulation())             .isEqualTo(POPULATION_VAL);
      assertThat(entity.getActive())                 .isTrue();
      assertThat(entity.getCreatedAt())              .isEqualTo(CREATED_AT);
      assertThat(entity.getUpdatedAt())              .isEqualTo(UPDATED_AT);
    }

    @Test
    @DisplayName("creates a CityEntity shell that contains only the id")
    void cityShellContainsOnlyId() {
      AreaEntity entity = mapper.toEntity(fullDomain());

      CityEntity city = entity.getCity();
      assertThat(city)            .isNotNull();
      assertThat(city.getId())    .isEqualTo(CITY_ID);
      assertThat(city.getName())  .isNull();   // nothing else populated on the shell
    }

    @Test
    @DisplayName("creates a GeoShapeEntity shell that contains only the id")
    void geoShapeShellContainsOnlyId() {
      AreaEntity entity = mapper.toEntity(fullDomain());

      GeoShapeEntity geoShape = entity.getGeoShape();
      assertThat(geoShape)            .isNotNull();
      assertThat(geoShape.getId())    .isEqualTo(GEOSHAPE_ID);
    }

    @Test
    @DisplayName("maps active=false correctly")
    void inactiveFlag() {
      Area domain = fullDomain();
      domain.setActive(Boolean.FALSE);

      assertThat(mapper.toEntity(domain).getActive()).isFalse();
    }

    @Test
    @DisplayName("converts AreaType enum to its getValue() string")
    void areaTypeConversion() {
      AreaEntity entity = mapper.toEntity(fullDomain());

      assertThat(entity.getAreaType()).isEqualTo(TYPE_STRING);
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
      AreaEntity entity = fullEntity();
      entity.setGeoShape(null);

      assertThat(mapper.toDomain(entity).getGeoShapeId()).isNull();
    }

    @Test
    @DisplayName("toDomain: population=null  →  population VO=null")
    void toDomain_populationNull() {
      AreaEntity entity = fullEntity();
      entity.setPopulation(null);

      assertThat(mapper.toDomain(entity).getPopulation()).isNull();
    }

    @Test
    @DisplayName("toDomain: areaType=null  →  type=null")
    void toDomain_areaTypeNull() {
      AreaEntity entity = fullEntity();
      entity.setAreaType(null);

      assertThat(mapper.toDomain(entity).getType()).isNull();
    }

    @Test
    @DisplayName("toDomain: postalCode=null  →  postalCode=null")
    void toDomain_postalCodeNull() {
      AreaEntity entity = fullEntity();
      entity.setPostalCode(null);

      assertThat(mapper.toDomain(entity).getPostalCode()).isNull();
    }

    @Test
    @DisplayName("toDomain: both timestamps null  →  auditInfo=null")
    void toDomain_auditTimestampsNull() {
      AreaEntity entity = fullEntity();
      entity.setCreatedAt(null);
      entity.setUpdatedAt(null);

      assertThat(mapper.toDomain(entity).getAuditInfo()).isNull();
    }

    @Test
    @DisplayName("toDomain: city=null  →  cityId=null  (defensive, no NPE)")
    void toDomain_cityNull() {
      AreaEntity entity = fullEntity();
      entity.setCity(null);

      assertThat(mapper.toDomain(entity).getCityId()).isNull();
    }

    // ── toEntity direction ─────────────────────────────────────────

    @Test
    @DisplayName("toEntity: geoShapeId=null  →  geoShape=null")
    void toEntity_geoShapeIdNull() {
      Area domain = fullDomain();
      domain.setGeoShapeId(null);

      assertThat(mapper.toEntity(domain).getGeoShape()).isNull();
    }

    @Test
    @DisplayName("toEntity: population=null  →  population column=null")
    void toEntity_populationNull() {
      Area domain = fullDomain();
      domain.setPopulation(null);

      assertThat(mapper.toEntity(domain).getPopulation()).isNull();
    }

    @Test
    @DisplayName("toEntity: type=null  →  areaType=null")
    void toEntity_typeNull() {
      Area domain = fullDomain();
      domain.setType(null);

      assertThat(mapper.toEntity(domain).getAreaType()).isNull();
    }

    @Test
    @DisplayName("toEntity: postalCode=null  →  postalCode=null")
    void toEntity_postalCodeNull() {
      Area domain = fullDomain();
      domain.setPostalCode(null);

      assertThat(mapper.toEntity(domain).getPostalCode()).isNull();
    }

    @Test
    @DisplayName("toEntity: auditInfo=null  →  createdAt and updatedAt=null")
    void toEntity_auditInfoNull() {
      Area domain = fullDomain();
      domain.setAuditInfo(null);

      AreaEntity entity = mapper.toEntity(domain);

      assertThat(entity.getCreatedAt()).isNull();
      assertThat(entity.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("toEntity: cityId=null  →  city shell=null  (defensive)")
    void toEntity_cityIdNull() {
      Area domain = fullDomain();
      domain.setCityId(null);

      assertThat(mapper.toEntity(domain).getCity()).isNull();
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
      Area original = fullDomain();

      Area recovered = mapper.toDomain(mapper.toEntity(original));

      assertThat(recovered.getId())                          .isEqualTo(original.getId());
      assertThat(recovered.getCityId())                      .isEqualTo(original.getCityId());
      assertThat(recovered.getGeoShapeId())                  .isEqualTo(original.getGeoShapeId());
      assertThat(recovered.getName())                        .isEqualTo(original.getName());
      assertThat(recovered.getPostalCode())                  .isEqualTo(original.getPostalCode());
      assertThat(recovered.getType())                        .isEqualTo(original.getType());
      assertThat(recovered.getActive())                      .isEqualTo(original.getActive());
      assertThat(recovered.getPopulation().getValue())       .isEqualTo(original.getPopulation().getValue());
      assertThat(recovered.getAuditInfo().getCreatedAt())    .isEqualTo(original.getAuditInfo().getCreatedAt());
      assertThat(recovered.getAuditInfo().getUpdatedAt())    .isEqualTo(original.getAuditInfo().getUpdatedAt());
    }

    @Test
    @DisplayName("minimal domain (only mandatory fields) survives round-trip")
    void minimalRoundTrip() {
      Area minimal = Area.builder()
        .id(AREA_ID)
        .cityId(CITY_ID)
        .name(NAME)
        .active(Boolean.TRUE)
        .build();

      Area recovered = mapper.toDomain(mapper.toEntity(minimal));

      assertThat(recovered.getId())         .isEqualTo(AREA_ID);
      assertThat(recovered.getCityId())     .isEqualTo(CITY_ID);
      assertThat(recovered.getName())       .isEqualTo(NAME);
      assertThat(recovered.getActive())     .isTrue();
      // optional fields stay null
      assertThat(recovered.getGeoShapeId()) .isNull();
      assertThat(recovered.getPostalCode()) .isNull();
      assertThat(recovered.getType())       .isNull();
      assertThat(recovered.getPopulation()) .isNull();
      assertThat(recovered.getAuditInfo())  .isNull();
    }
  }

  // ================================================================
  // Static helper methods exercised directly
  // ================================================================

  @Nested
  @DisplayName("Static conversion helpers")
  class StaticHelpers {

    // ── uuidToCityEntity ───────────────────────────────────────────

    @Test
    @DisplayName("uuidToCityEntity: valid UUID  →  shell with that id")
    void uuidToCityEntity_valid() {
      CityEntity shell = AreaMapper.uuidToCityEntity(CITY_ID);

      assertThat(shell)            .isNotNull();
      assertThat(shell.getId())    .isEqualTo(CITY_ID);
    }

    @Test
    @DisplayName("uuidToCityEntity: null  →  null")
    void uuidToCityEntity_null() {
      assertThat(AreaMapper.uuidToCityEntity(null)).isNull();
    }

    // ── uuidToGeoShapeEntity ───────────────────────────────────────

    @Test
    @DisplayName("uuidToGeoShapeEntity: valid UUID  →  shell with that id")
    void uuidToGeoShapeEntity_valid() {
      GeoShapeEntity shell = AreaMapper.uuidToGeoShapeEntity(GEOSHAPE_ID);

      assertThat(shell)            .isNotNull();
      assertThat(shell.getId())    .isEqualTo(GEOSHAPE_ID);
    }

    @Test
    @DisplayName("uuidToGeoShapeEntity: null  →  null")
    void uuidToGeoShapeEntity_null() {
      assertThat(AreaMapper.uuidToGeoShapeEntity(null)).isNull();
    }

    // ── cityEntityToUuid ───────────────────────────────────────────

    @Test
    @DisplayName("cityEntityToUuid: populated entity  →  its id")
    void cityEntityToUuid_valid() {
      CityEntity entity = new CityEntity();
      entity.setId(CITY_ID);

      assertThat(AreaMapper.cityEntityToUuid(entity)).isEqualTo(CITY_ID);
    }

    @Test
    @DisplayName("cityEntityToUuid: null entity  →  null")
    void cityEntityToUuid_null() {
      assertThat(AreaMapper.cityEntityToUuid(null)).isNull();
    }

    // ── geoShapeEntityToUuid ───────────────────────────────────────

    @Test
    @DisplayName("geoShapeEntityToUuid: populated entity  →  its id")
    void geoShapeEntityToUuid_valid() {
      GeoShapeEntity entity = new GeoShapeEntity();
      entity.setId(GEOSHAPE_ID);

      assertThat(AreaMapper.geoShapeEntityToUuid(entity)).isEqualTo(GEOSHAPE_ID);
    }

    @Test
    @DisplayName("geoShapeEntityToUuid: null entity  →  null")
    void geoShapeEntityToUuid_null() {
      assertThat(AreaMapper.geoShapeEntityToUuid(null)).isNull();
    }

    // ── areaTypeToString / stringToAreaType ────────────────────────

    @Test
    @DisplayName("areaTypeToString: converts enum to its getValue() string")
    void areaTypeToString_valid() {
      assertThat(AreaMapper.areaTypeToString(TYPE)).isEqualTo(TYPE_STRING);
    }

    @Test
    @DisplayName("areaTypeToString: null enum  →  null")
    void areaTypeToString_null() {
      assertThat(AreaMapper.areaTypeToString(null)).isNull();
    }

    @Test
    @DisplayName("stringToAreaType: recovers the original enum constant")
    void stringToAreaType_valid() {
      // stringToAreaType delegates to AreaType.valueOf — input must be the
      // declared constant name.  Adjust if getValue() != name().
      assertThat(AreaMapper.stringToAreaType(TYPE.name())).isEqualTo(TYPE);
    }

    @Test
    @DisplayName("stringToAreaType: null string  →  null")
    void stringToAreaType_null() {
      assertThat(AreaMapper.stringToAreaType(null)).isNull();
    }

    /**
     * {@link AreaType#valueOf(String)} throws {@link IllegalArgumentException}
     * for any string that does not match a declared constant name.  This is
     * different from the {@code fromValue} pattern used by {@code RegionType}:
     * bad data blows up at mapping time rather than silently becoming {@code null}.
     * This test documents the contract so that if the team later switches to a
     * lenient factory the change is deliberate.
     */
    @Test
    @DisplayName("stringToAreaType: unrecognised string  →  throws IllegalArgumentException")
    void stringToAreaType_invalidString_throws() {
      assertThatThrownBy(() -> AreaMapper.stringToAreaType("NOT_A_REAL_TYPE"))
        .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Iterates every declared {@link AreaType} constant through
     * {@code areaTypeToString → stringToAreaType} and verifies the original
     * value is recovered.  If {@code getValue()} ever diverges from
     * {@code name()} this test is the first to surface it, because
     * {@code valueOf} only recognises {@code name()}.
     */
    @Test
    @DisplayName("AreaType round-trip: every enum constant survives name → valueOf")
    void areaTypeRoundTrip() {
      for (AreaType type : AreaType.values()) {
        assertThat(AreaMapper.stringToAreaType(type.name())).isEqualTo(type);
      }
    }

    // ── populationVOToLong / longToPopulationVO ────────────────────

    @Test
    @DisplayName("populationVOToLong: extracts the numeric value")
    void populationVOToLong_valid() {
      assertThat(AreaMapper.populationVOToLong(PopulationVO.of(POPULATION_VAL)))
        .isEqualTo(POPULATION_VAL);
    }

    @Test
    @DisplayName("populationVOToLong: null VO  →  null")
    void populationVOToLong_null() {
      assertThat(AreaMapper.populationVOToLong(null)).isNull();
    }

    @Test
    @DisplayName("longToPopulationVO: wraps the Long into a VO")
    void longToPopulationVO_valid() {
      PopulationVO vo = AreaMapper.longToPopulationVO(POPULATION_VAL);

      assertThat(vo)             .isNotNull();
      assertThat(vo.getValue())  .isEqualTo(POPULATION_VAL);
    }

    @Test
    @DisplayName("longToPopulationVO: null Long  →  null")
    void longToPopulationVO_null() {
      assertThat(AreaMapper.longToPopulationVO(null)).isNull();
    }

    @Test
    @DisplayName("Population round-trip: Long → VO → Long recovers original value")
    void populationRoundTrip() {
      Long recovered = AreaMapper.populationVOToLong(
        AreaMapper.longToPopulationVO(POPULATION_VAL)
      );

      assertThat(recovered).isEqualTo(POPULATION_VAL);
    }
  }
}
