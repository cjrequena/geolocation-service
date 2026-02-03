package com.cjrequena.sample.domain.mapper;

import com.cjrequena.sample.domain.model.aggregate.Zone;
import com.cjrequena.sample.domain.model.enums.ZoneType;
import com.cjrequena.sample.domain.model.vo.AuditInfoVO;
import com.cjrequena.sample.persistence.entity.AreaEntity;
import com.cjrequena.sample.persistence.entity.GeoShapeEntity;
import com.cjrequena.sample.persistence.entity.ZoneEntity;
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
class ZoneMapperTest {

  // ── shared fixtures ────────────────────────────────────────────────────────

  private static final UUID            ZONE_ID        = UUID.randomUUID();
  private static final UUID            AREA_ID        = UUID.randomUUID();
  private static final UUID            GEOSHAPE_ID    = UUID.randomUUID();
  private static final String          NAME           = "Golden Gate Park";
  private static final String          POSTAL_CODE    = "94121";
  private static final ZoneType        TYPE           = ZoneType.SECTOR;                // adjust to an actual enum constant
  private static final String          TYPE_STRING    = TYPE.getValue();
  private static final OffsetDateTime  CREATED_AT     = OffsetDateTime.of(2024, 5, 8, 11, 15, 0, 0, ZoneOffset.UTC);
  private static final OffsetDateTime  UPDATED_AT     = OffsetDateTime.of(2024, 10, 22, 9, 0, 0, 0, ZoneOffset.UTC);

  private ZoneMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(ZoneMapper.class);
  }

  // ── fixture builders ───────────────────────────────────────────────────────

  /** Fully populated {@link Zone} domain object. */
  private static Zone fullDomain() {
    return Zone.builder()
      .id(ZONE_ID)
      .areaId(AREA_ID)
      .geoShapeId(GEOSHAPE_ID)
      .name(NAME)
      .postalCode(POSTAL_CODE)
      .type(TYPE)
      .active(Boolean.TRUE)
      .auditInfo(AuditInfoVO.of(CREATED_AT, UPDATED_AT))
      .build();
  }

  /** Fully populated {@link ZoneEntity}. */
  private static ZoneEntity fullEntity() {
    AreaEntity      area     = new AreaEntity();
    area.setId(AREA_ID);

    GeoShapeEntity  geoShape = new GeoShapeEntity();
    geoShape.setId(GEOSHAPE_ID);

    ZoneEntity entity = new ZoneEntity();
    entity.setId(ZONE_ID);
    entity.setArea(area);
    entity.setGeoShape(geoShape);
    entity.setName(NAME);
    entity.setPostalCode(POSTAL_CODE);
    entity.setZoneType(TYPE_STRING);
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
      Zone domain = mapper.toDomain(fullEntity());

      assertThat(domain).isNotNull();
      assertThat(domain.getId())                          .isEqualTo(ZONE_ID);
      assertThat(domain.getAreaId())                      .isEqualTo(AREA_ID);
      assertThat(domain.getGeoShapeId())                  .isEqualTo(GEOSHAPE_ID);
      assertThat(domain.getName())                        .isEqualTo(NAME);
      assertThat(domain.getPostalCode())                  .isEqualTo(POSTAL_CODE);
      assertThat(domain.getType())                        .isEqualTo(TYPE);
      assertThat(domain.getActive())                      .isTrue();
      assertThat(domain.getAuditInfo())                   .isNotNull();
      assertThat(domain.getAuditInfo().getCreatedAt())    .isEqualTo(CREATED_AT);
      assertThat(domain.getAuditInfo().getUpdatedAt())    .isEqualTo(UPDATED_AT);
    }

    @Test
    @DisplayName("maps active=false correctly")
    void inactiveFlag() {
      ZoneEntity entity = fullEntity();
      entity.setActive(Boolean.FALSE);

      assertThat(mapper.toDomain(entity).getActive()).isFalse();
    }

    @Test
    @DisplayName("extracts areaId from the nested AreaEntity")
    void areaIdExtraction() {
      assertThat(mapper.toDomain(fullEntity()).getAreaId()).isEqualTo(AREA_ID);
    }

    @Test
    @DisplayName("extracts geoShapeId from the nested GeoShapeEntity")
    void geoShapeIdExtraction() {
      assertThat(mapper.toDomain(fullEntity()).getGeoShapeId()).isEqualTo(GEOSHAPE_ID);
    }

    @Test
    @DisplayName("converts the zoneType string back to the matching ZoneType enum")
    void zoneTypeConversion() {
      assertThat(mapper.toDomain(fullEntity()).getType()).isEqualTo(TYPE);
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
      ZoneEntity entity = mapper.toEntity(fullDomain());

      assertThat(entity).isNotNull();
      assertThat(entity.getId())                     .isEqualTo(ZONE_ID);
      assertThat(entity.getArea())                   .isNotNull();
      assertThat(entity.getArea().getId())           .isEqualTo(AREA_ID);
      assertThat(entity.getGeoShape())               .isNotNull();
      assertThat(entity.getGeoShape().getId())       .isEqualTo(GEOSHAPE_ID);
      assertThat(entity.getName())                   .isEqualTo(NAME);
      assertThat(entity.getPostalCode())             .isEqualTo(POSTAL_CODE);
      assertThat(entity.getZoneType())               .isEqualTo(TYPE_STRING);
      assertThat(entity.getActive())                 .isTrue();
      assertThat(entity.getCreatedAt())              .isEqualTo(CREATED_AT);
      assertThat(entity.getUpdatedAt())              .isEqualTo(UPDATED_AT);
    }

    @Test
    @DisplayName("creates an AreaEntity shell that contains only the id")
    void areaShellContainsOnlyId() {
      ZoneEntity entity = mapper.toEntity(fullDomain());

      AreaEntity area = entity.getArea();
      assertThat(area)            .isNotNull();
      assertThat(area.getId())    .isEqualTo(AREA_ID);
      assertThat(area.getName())  .isNull();   // nothing else populated on the shell
    }

    @Test
    @DisplayName("creates a GeoShapeEntity shell that contains only the id")
    void geoShapeShellContainsOnlyId() {
      ZoneEntity entity = mapper.toEntity(fullDomain());

      GeoShapeEntity geoShape = entity.getGeoShape();
      assertThat(geoShape)            .isNotNull();
      assertThat(geoShape.getId())    .isEqualTo(GEOSHAPE_ID);
    }

    @Test
    @DisplayName("maps active=false correctly")
    void inactiveFlag() {
      Zone domain = fullDomain();
      domain.setActive(Boolean.FALSE);

      assertThat(mapper.toEntity(domain).getActive()).isFalse();
    }

    @Test
    @DisplayName("converts ZoneType enum to its getValue() string")
    void zoneTypeConversion() {
      assertThat(mapper.toEntity(fullDomain()).getZoneType()).isEqualTo(TYPE_STRING);
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
      ZoneEntity entity = fullEntity();
      entity.setGeoShape(null);

      assertThat(mapper.toDomain(entity).getGeoShapeId()).isNull();
    }

    @Test
    @DisplayName("toDomain: zoneType=null  →  type=null")
    void toDomain_zoneTypeNull() {
      ZoneEntity entity = fullEntity();
      entity.setZoneType(null);

      assertThat(mapper.toDomain(entity).getType()).isNull();
    }

    @Test
    @DisplayName("toDomain: postalCode=null  →  postalCode=null")
    void toDomain_postalCodeNull() {
      ZoneEntity entity = fullEntity();
      entity.setPostalCode(null);

      assertThat(mapper.toDomain(entity).getPostalCode()).isNull();
    }

    @Test
    @DisplayName("toDomain: both timestamps null  →  auditInfo=null")
    void toDomain_auditTimestampsNull() {
      ZoneEntity entity = fullEntity();
      entity.setCreatedAt(null);
      entity.setUpdatedAt(null);

      assertThat(mapper.toDomain(entity).getAuditInfo()).isNull();
    }

    @Test
    @DisplayName("toDomain: area=null  →  areaId=null  (defensive, no NPE)")
    void toDomain_areaNull() {
      ZoneEntity entity = fullEntity();
      entity.setArea(null);

      assertThat(mapper.toDomain(entity).getAreaId()).isNull();
    }

    // ── toEntity direction ─────────────────────────────────────────

    @Test
    @DisplayName("toEntity: geoShapeId=null  →  geoShape=null")
    void toEntity_geoShapeIdNull() {
      Zone domain = fullDomain();
      domain.setGeoShapeId(null);

      assertThat(mapper.toEntity(domain).getGeoShape()).isNull();
    }

    @Test
    @DisplayName("toEntity: type=null  →  zoneType=null")
    void toEntity_typeNull() {
      Zone domain = fullDomain();
      domain.setType(null);

      assertThat(mapper.toEntity(domain).getZoneType()).isNull();
    }

    @Test
    @DisplayName("toEntity: postalCode=null  →  postalCode=null")
    void toEntity_postalCodeNull() {
      Zone domain = fullDomain();
      domain.setPostalCode(null);

      assertThat(mapper.toEntity(domain).getPostalCode()).isNull();
    }

    @Test
    @DisplayName("toEntity: auditInfo=null  →  createdAt and updatedAt=null")
    void toEntity_auditInfoNull() {
      Zone domain = fullDomain();
      domain.setAuditInfo(null);

      ZoneEntity entity = mapper.toEntity(domain);

      assertThat(entity.getCreatedAt()).isNull();
      assertThat(entity.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("toEntity: areaId=null  →  area shell=null  (defensive)")
    void toEntity_areaIdNull() {
      Zone domain = fullDomain();
      domain.setAreaId(null);

      assertThat(mapper.toEntity(domain).getArea()).isNull();
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
      Zone original = fullDomain();

      Zone recovered = mapper.toDomain(mapper.toEntity(original));

      assertThat(recovered.getId())                          .isEqualTo(original.getId());
      assertThat(recovered.getAreaId())                      .isEqualTo(original.getAreaId());
      assertThat(recovered.getGeoShapeId())                  .isEqualTo(original.getGeoShapeId());
      assertThat(recovered.getName())                        .isEqualTo(original.getName());
      assertThat(recovered.getPostalCode())                  .isEqualTo(original.getPostalCode());
      assertThat(recovered.getType())                        .isEqualTo(original.getType());
      assertThat(recovered.getActive())                      .isEqualTo(original.getActive());
      assertThat(recovered.getAuditInfo().getCreatedAt())    .isEqualTo(original.getAuditInfo().getCreatedAt());
      assertThat(recovered.getAuditInfo().getUpdatedAt())    .isEqualTo(original.getAuditInfo().getUpdatedAt());
    }

    @Test
    @DisplayName("minimal domain (only mandatory fields) survives round-trip")
    void minimalRoundTrip() {
      Zone minimal = Zone.builder()
        .id(ZONE_ID)
        .areaId(AREA_ID)
        .name(NAME)
        .active(Boolean.TRUE)
        .build();

      Zone recovered = mapper.toDomain(mapper.toEntity(minimal));

      assertThat(recovered.getId())         .isEqualTo(ZONE_ID);
      assertThat(recovered.getAreaId())     .isEqualTo(AREA_ID);
      assertThat(recovered.getName())       .isEqualTo(NAME);
      assertThat(recovered.getActive())     .isTrue();
      // optional fields stay null
      assertThat(recovered.getGeoShapeId()) .isNull();
      assertThat(recovered.getPostalCode()) .isNull();
      assertThat(recovered.getType())       .isNull();
      assertThat(recovered.getAuditInfo())  .isNull();
    }
  }

  // ================================================================
  // Static helper methods exercised directly
  // ================================================================

  @Nested
  @DisplayName("Static conversion helpers")
  class StaticHelpers {

    // ── uuidToAreaEntity ───────────────────────────────────────────

    @Test
    @DisplayName("uuidToAreaEntity: valid UUID  →  shell with that id")
    void uuidToAreaEntity_valid() {
      AreaEntity shell = ZoneMapper.uuidToAreaEntity(AREA_ID);

      assertThat(shell)            .isNotNull();
      assertThat(shell.getId())    .isEqualTo(AREA_ID);
    }

    @Test
    @DisplayName("uuidToAreaEntity: null  →  null")
    void uuidToAreaEntity_null() {
      assertThat(ZoneMapper.uuidToAreaEntity(null)).isNull();
    }

    // ── uuidToGeoShapeEntity ───────────────────────────────────────

    @Test
    @DisplayName("uuidToGeoShapeEntity: valid UUID  →  shell with that id")
    void uuidToGeoShapeEntity_valid() {
      GeoShapeEntity shell = ZoneMapper.uuidToGeoShapeEntity(GEOSHAPE_ID);

      assertThat(shell)            .isNotNull();
      assertThat(shell.getId())    .isEqualTo(GEOSHAPE_ID);
    }

    @Test
    @DisplayName("uuidToGeoShapeEntity: null  →  null")
    void uuidToGeoShapeEntity_null() {
      assertThat(ZoneMapper.uuidToGeoShapeEntity(null)).isNull();
    }

    // ── areaEntityToUuid ───────────────────────────────────────────

    @Test
    @DisplayName("areaEntityToUuid: populated entity  →  its id")
    void areaEntityToUuid_valid() {
      AreaEntity entity = new AreaEntity();
      entity.setId(AREA_ID);

      assertThat(ZoneMapper.areaEntityToUuid(entity)).isEqualTo(AREA_ID);
    }

    @Test
    @DisplayName("areaEntityToUuid: null entity  →  null")
    void areaEntityToUuid_null() {
      assertThat(ZoneMapper.areaEntityToUuid(null)).isNull();
    }

    // ── geoShapeEntityToUuid ───────────────────────────────────────

    @Test
    @DisplayName("geoShapeEntityToUuid: populated entity  →  its id")
    void geoShapeEntityToUuid_valid() {
      GeoShapeEntity entity = new GeoShapeEntity();
      entity.setId(GEOSHAPE_ID);

      assertThat(ZoneMapper.geoShapeEntityToUuid(entity)).isEqualTo(GEOSHAPE_ID);
    }

    @Test
    @DisplayName("geoShapeEntityToUuid: null entity  →  null")
    void geoShapeEntityToUuid_null() {
      assertThat(ZoneMapper.geoShapeEntityToUuid(null)).isNull();
    }

    // ── zoneTypeToString / stringToZoneType ────────────────────────

    @Test
    @DisplayName("zoneTypeToString: converts enum to its getValue() string")
    void zoneTypeToString_valid() {
      assertThat(ZoneMapper.zoneTypeToString(TYPE)).isEqualTo(TYPE_STRING);
    }

    @Test
    @DisplayName("zoneTypeToString: null enum  →  null")
    void zoneTypeToString_null() {
      assertThat(ZoneMapper.zoneTypeToString(null)).isNull();
    }

    @Test
    @DisplayName("stringToZoneType: recovers the original enum constant")
    void stringToZoneType_valid() {
      // stringToZoneType delegates to ZoneType.valueOf — input must be the
      // declared constant name.  Adjust if getValue() != name().
      assertThat(ZoneMapper.stringToZoneType(TYPE.name())).isEqualTo(TYPE);
    }

    @Test
    @DisplayName("stringToZoneType: null string  →  null")
    void stringToZoneType_null() {
      assertThat(ZoneMapper.stringToZoneType(null)).isNull();
    }

    /**
     * {@link ZoneType#valueOf(String)} throws {@link IllegalArgumentException}
     * for any string that does not match a declared constant name — identical
     * contract to {@code AreaType}.  Bad persistence data will blow up here
     * rather than silently becoming {@code null}.
     */
    @Test
    @DisplayName("stringToZoneType: unrecognised string  →  throws IllegalArgumentException")
    void stringToZoneType_invalidString_throws() {
      assertThatThrownBy(() -> ZoneMapper.stringToZoneType("NOT_A_REAL_TYPE"))
        .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Iterates every declared {@link ZoneType} constant through
     * {@code name() → valueOf()}.  If {@code getValue()} ever diverges from
     * {@code name()} this is the first test to surface it and flag that the
     * helper needs a {@code fromValue} factory instead.
     */
    @Test
    @DisplayName("ZoneType round-trip: every enum constant survives name → valueOf")
    void zoneTypeRoundTrip() {
      for (ZoneType type : ZoneType.values()) {
        assertThat(ZoneMapper.stringToZoneType(type.name())).isEqualTo(type);
      }
    }
  }
}
