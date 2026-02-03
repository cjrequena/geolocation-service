package com.cjrequena.sample.domain.mapper;

import com.cjrequena.sample.domain.model.aggregate.Country;
import com.cjrequena.sample.domain.model.vo.AuditInfoVO;
import com.cjrequena.sample.domain.model.vo.IsoCodeVO;
import com.cjrequena.sample.domain.model.vo.PopulationVO;
import com.cjrequena.sample.persistence.entity.CountryEntity;
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

/**
 *
 * @author cjrequena
 */
@ExtendWith(MockitoExtension.class)
class CountryMapperTest {

  // ── shared fixtures ────────────────────────────────────────────────────────

  private static final UUID            COUNTRY_ID     = UUID.randomUUID();
  private static final String          NAME           = "United States";
  private static final String          ALPHA_2        = "US";
  private static final String          ALPHA_3        = "USA";
  private static final String          NUMERIC        = "840";
  private static final String          PHONE_CODE     = "+1";
  private static final String          CURRENCY_CODE  = "USD";
  private static final String          CAPITAL        = "Washington D.C.";
  private static final long            POPULATION_VAL = 331_002_651L;
  private static final OffsetDateTime  CREATED_AT     = OffsetDateTime.of(2024, 2, 1, 9, 0, 0, 0, ZoneOffset.UTC);
  private static final OffsetDateTime  UPDATED_AT     = OffsetDateTime.of(2024, 8, 15, 17, 45, 0, 0, ZoneOffset.UTC);

  private CountryMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(CountryMapper.class);
  }

  // ── fixture builders ───────────────────────────────────────────────────────

  /** Fully populated {@link Country} domain object. */
  private static Country fullDomain() {
    return Country.builder()
      .id(COUNTRY_ID)
      .name(NAME)
      .isoCode(IsoCodeVO.of(ALPHA_2, ALPHA_3, NUMERIC))
      .phoneCode(PHONE_CODE)
      .currencyCode(CURRENCY_CODE)
      .capital(CAPITAL)
      .population(PopulationVO.of(POPULATION_VAL))
      .active(Boolean.TRUE)
      .auditInfo(AuditInfoVO.of(CREATED_AT, UPDATED_AT))
      .build();
  }

  /** Fully populated {@link CountryEntity}. */
  private static CountryEntity fullEntity() {
    CountryEntity entity = new CountryEntity();
    entity.setId(COUNTRY_ID);
    entity.setName(NAME);
    entity.setIsoCodeAlpha2(ALPHA_2);
    entity.setIsoCodeAlpha3(ALPHA_3);
    entity.setIsoCodeNumeric(NUMERIC);
    entity.setPhoneCode(PHONE_CODE);
    entity.setCurrencyCode(CURRENCY_CODE);
    entity.setCapital(CAPITAL);
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
      Country domain = mapper.toDomain(fullEntity());

      assertThat(domain).isNotNull();
      assertThat(domain.getId())                          .isEqualTo(COUNTRY_ID);
      assertThat(domain.getName())                        .isEqualTo(NAME);
      assertThat(domain.getPhoneCode())                   .isEqualTo(PHONE_CODE);
      assertThat(domain.getCurrencyCode())                .isEqualTo(CURRENCY_CODE);
      assertThat(domain.getCapital())                     .isEqualTo(CAPITAL);
      assertThat(domain.getActive())                      .isTrue();
      assertThat(domain.getPopulation())                  .isNotNull();
      assertThat(domain.getPopulation().getValue())       .isEqualTo(POPULATION_VAL);
      assertThat(domain.getAuditInfo())                   .isNotNull();
      assertThat(domain.getAuditInfo().getCreatedAt())    .isEqualTo(CREATED_AT);
      assertThat(domain.getAuditInfo().getUpdatedAt())    .isEqualTo(UPDATED_AT);
    }

    @Test
    @DisplayName("assembles IsoCodeVO correctly from the three flat columns")
    void isoCodeAssembly() {
      Country domain = mapper.toDomain(fullEntity());

      assertThat(domain.getIsoCode())                     .isNotNull();
      assertThat(domain.getIsoCode().getAlpha2())         .isEqualTo(ALPHA_2);
      assertThat(domain.getIsoCode().getAlpha3())         .isEqualTo(ALPHA_3);
      assertThat(domain.getIsoCode().getNumeric())        .isEqualTo(NUMERIC);
    }

    @Test
    @DisplayName("maps isActive=false  →  status=false")
    void inactiveStatus() {
      CountryEntity entity = fullEntity();
      entity.setActive(Boolean.FALSE);

      assertThat(mapper.toDomain(entity).getActive()).isFalse();
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
      CountryEntity entity = mapper.toEntity(fullDomain());

      assertThat(entity).isNotNull();
      assertThat(entity.getId())              .isEqualTo(COUNTRY_ID);
      assertThat(entity.getName())            .isEqualTo(NAME);
      assertThat(entity.getIsoCodeAlpha2())   .isEqualTo(ALPHA_2);
      assertThat(entity.getIsoCodeAlpha3())   .isEqualTo(ALPHA_3);
      assertThat(entity.getIsoCodeNumeric())  .isEqualTo(NUMERIC);
      assertThat(entity.getPhoneCode())       .isEqualTo(PHONE_CODE);
      assertThat(entity.getCurrencyCode())    .isEqualTo(CURRENCY_CODE);
      assertThat(entity.getCapital())         .isEqualTo(CAPITAL);
      assertThat(entity.getPopulation())      .isEqualTo(POPULATION_VAL);
      assertThat(entity.getActive())        .isTrue();
      assertThat(entity.getCreatedAt())       .isEqualTo(CREATED_AT);
      assertThat(entity.getUpdatedAt())       .isEqualTo(UPDATED_AT);
    }

    @Test
    @DisplayName("fans IsoCodeVO out to the three flat columns")
    void isoCodeFanOut() {
      CountryEntity entity = mapper.toEntity(fullDomain());

      assertThat(entity.getIsoCodeAlpha2())   .isEqualTo(ALPHA_2);
      assertThat(entity.getIsoCodeAlpha3())   .isEqualTo(ALPHA_3);
      assertThat(entity.getIsoCodeNumeric())  .isEqualTo(NUMERIC);
    }

    @Test
    @DisplayName("maps status=false  →  isActive=false")
    void inactiveStatus() {
      Country domain = fullDomain();
      domain.setActive(Boolean.FALSE);

      assertThat(mapper.toEntity(domain).getActive()).isFalse();
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
    @DisplayName("toDomain: population=null  →  population VO=null")
    void toDomain_populationNull() {
      CountryEntity entity = fullEntity();
      entity.setPopulation(null);

      assertThat(mapper.toDomain(entity).getPopulation()).isNull();
    }

    @Test
    @DisplayName("toDomain: capital=null  →  capital=null")
    void toDomain_capitalNull() {
      CountryEntity entity = fullEntity();
      entity.setCapital(null);

      assertThat(mapper.toDomain(entity).getCapital()).isNull();
    }

    @Test
    @DisplayName("toDomain: phoneCode=null  →  phoneCode=null")
    void toDomain_phoneCodeNull() {
      CountryEntity entity = fullEntity();
      entity.setPhoneCode(null);

      assertThat(mapper.toDomain(entity).getPhoneCode()).isNull();
    }

    @Test
    @DisplayName("toDomain: currencyCode=null  →  currencyCode=null")
    void toDomain_currencyCodeNull() {
      CountryEntity entity = fullEntity();
      entity.setCurrencyCode(null);

      assertThat(mapper.toDomain(entity).getCurrencyCode()).isNull();
    }

    @Test
    @DisplayName("toDomain: all three ISO columns null  →  isoCode=null")
    void toDomain_allIsoColumnsNull() {
      CountryEntity entity = fullEntity();
      entity.setIsoCodeAlpha2(null);
      entity.setIsoCodeAlpha3(null);
      entity.setIsoCodeNumeric(null);

      assertThat(mapper.toDomain(entity).getIsoCode()).isNull();
    }

    @Test
    @DisplayName("toDomain: only alpha2 set  →  isoCode assembled with alpha3 and numeric null")
    void toDomain_isoCodeOnlyAlpha2() {
      CountryEntity entity = fullEntity();
      entity.setIsoCodeAlpha3(null);
      entity.setIsoCodeNumeric(null);

      IsoCodeVO isoCode = mapper.toDomain(entity).getIsoCode();

      assertThat(isoCode)                .isNotNull();
      assertThat(isoCode.getAlpha2())    .isEqualTo(ALPHA_2);
      assertThat(isoCode.getAlpha3())    .isNull();
      assertThat(isoCode.getNumeric())   .isNull();
    }

    @Test
    @DisplayName("toDomain: alpha2+alpha3 set, numeric null  →  isoCode assembled with numeric null")
    void toDomain_isoCodeNoNumeric() {
      CountryEntity entity = fullEntity();
      entity.setIsoCodeNumeric(null);

      IsoCodeVO isoCode = mapper.toDomain(entity).getIsoCode();

      assertThat(isoCode)                .isNotNull();
      assertThat(isoCode.getAlpha2())    .isEqualTo(ALPHA_2);
      assertThat(isoCode.getAlpha3())    .isEqualTo(ALPHA_3);
      assertThat(isoCode.getNumeric())   .isNull();
    }

    @Test
    @DisplayName("toDomain: both timestamps null  →  auditInfo=null")
    void toDomain_auditTimestampsNull() {
      CountryEntity entity = fullEntity();
      entity.setCreatedAt(null);
      entity.setUpdatedAt(null);

      assertThat(mapper.toDomain(entity).getAuditInfo()).isNull();
    }

    // ── toEntity direction ─────────────────────────────────────────

    @Test
    @DisplayName("toEntity: population=null  →  population column=null")
    void toEntity_populationNull() {
      Country domain = fullDomain();
      domain.setPopulation(null);

      assertThat(mapper.toEntity(domain).getPopulation()).isNull();
    }

    @Test
    @DisplayName("toEntity: capital=null  →  capital=null")
    void toEntity_capitalNull() {
      Country domain = fullDomain();
      domain.setCapital(null);

      assertThat(mapper.toEntity(domain).getCapital()).isNull();
    }

    @Test
    @DisplayName("toEntity: phoneCode=null  →  phoneCode=null")
    void toEntity_phoneCodeNull() {
      Country domain = fullDomain();
      domain.setPhoneCode(null);

      assertThat(mapper.toEntity(domain).getPhoneCode()).isNull();
    }

    @Test
    @DisplayName("toEntity: currencyCode=null  →  currencyCode=null")
    void toEntity_currencyCodeNull() {
      Country domain = fullDomain();
      domain.setCurrencyCode(null);

      assertThat(mapper.toEntity(domain).getCurrencyCode()).isNull();
    }

    @Test
    @DisplayName("toEntity: isoCode=null  →  all three ISO columns=null")
    void toEntity_isoCodeNull() {
      Country domain = fullDomain();
      domain.setIsoCode(null);

      CountryEntity entity = mapper.toEntity(domain);

      assertThat(entity.getIsoCodeAlpha2())   .isNull();
      assertThat(entity.getIsoCodeAlpha3())   .isNull();
      assertThat(entity.getIsoCodeNumeric())  .isNull();
    }

    @Test
    @DisplayName("toEntity: isoCode with only alpha2  →  alpha3 and numeric columns=null")
    void toEntity_isoCodeOnlyAlpha2() {
      Country domain = fullDomain();
      domain.setIsoCode(IsoCodeVO.of(ALPHA_2, null, null));

      CountryEntity entity = mapper.toEntity(domain);

      assertThat(entity.getIsoCodeAlpha2())   .isEqualTo(ALPHA_2);
      assertThat(entity.getIsoCodeAlpha3())   .isNull();
      assertThat(entity.getIsoCodeNumeric())  .isNull();
    }

    @Test
    @DisplayName("toEntity: auditInfo=null  →  createdAt and updatedAt=null")
    void toEntity_auditInfoNull() {
      Country domain = fullDomain();
      domain.setAuditInfo(null);

      CountryEntity entity = mapper.toEntity(domain);

      assertThat(entity.getCreatedAt()).isNull();
      assertThat(entity.getUpdatedAt()).isNull();
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
      Country original = fullDomain();

      Country recovered = mapper.toDomain(mapper.toEntity(original));

      assertThat(recovered.getId())                          .isEqualTo(original.getId());
      assertThat(recovered.getName())                        .isEqualTo(original.getName());
      assertThat(recovered.getPhoneCode())                   .isEqualTo(original.getPhoneCode());
      assertThat(recovered.getCurrencyCode())                .isEqualTo(original.getCurrencyCode());
      assertThat(recovered.getCapital())                     .isEqualTo(original.getCapital());
      assertThat(recovered.getActive())                      .isEqualTo(original.getActive());
      assertThat(recovered.getIsoCode().getAlpha2())         .isEqualTo(original.getIsoCode().getAlpha2());
      assertThat(recovered.getIsoCode().getAlpha3())         .isEqualTo(original.getIsoCode().getAlpha3());
      assertThat(recovered.getIsoCode().getNumeric())        .isEqualTo(original.getIsoCode().getNumeric());
      assertThat(recovered.getPopulation().getValue())       .isEqualTo(original.getPopulation().getValue());
      assertThat(recovered.getAuditInfo().getCreatedAt())    .isEqualTo(original.getAuditInfo().getCreatedAt());
      assertThat(recovered.getAuditInfo().getUpdatedAt())    .isEqualTo(original.getAuditInfo().getUpdatedAt());
    }

    @Test
    @DisplayName("minimal domain (only mandatory fields) survives round-trip")
    void minimalRoundTrip() {
      Country minimal = Country.builder()
        .id(COUNTRY_ID)
        .name(NAME)
        .isoCode(IsoCodeVO.of(ALPHA_2, ALPHA_3, NUMERIC))
        .active(Boolean.TRUE)
        .build();

      Country recovered = mapper.toDomain(mapper.toEntity(minimal));

      assertThat(recovered.getId())                  .isEqualTo(COUNTRY_ID);
      assertThat(recovered.getName())                .isEqualTo(NAME);
      assertThat(recovered.getActive())              .isTrue();
      assertThat(recovered.getIsoCode().getAlpha2()) .isEqualTo(ALPHA_2);
      assertThat(recovered.getIsoCode().getAlpha3()) .isEqualTo(ALPHA_3);
      assertThat(recovered.getIsoCode().getNumeric()).isEqualTo(NUMERIC);
      // optional fields stay null
      assertThat(recovered.getPhoneCode())           .isNull();
      assertThat(recovered.getCurrencyCode())        .isNull();
      assertThat(recovered.getCapital())             .isNull();
      assertThat(recovered.getPopulation())          .isNull();
      assertThat(recovered.getAuditInfo())           .isNull();
    }

    @Test
    @DisplayName("domain with partial IsoCodeVO (only alpha2) survives round-trip")
    void partialIsoCodeRoundTrip() {
      Country original = Country.builder()
        .id(COUNTRY_ID)
        .name(NAME)
        .isoCode(IsoCodeVO.of(ALPHA_2, null, null))
        .active(Boolean.TRUE)
        .build();

      Country recovered = mapper.toDomain(mapper.toEntity(original));

      assertThat(recovered.getIsoCode())                .isNotNull();
      assertThat(recovered.getIsoCode().getAlpha2())    .isEqualTo(ALPHA_2);
      assertThat(recovered.getIsoCode().getAlpha3())    .isNull();
      assertThat(recovered.getIsoCode().getNumeric())   .isNull();
    }
  }

  // ================================================================
  // Static helper methods exercised directly
  // ================================================================

  @Nested
  @DisplayName("Static conversion helpers")
  class StaticHelpers {

    // ── isoCodeVOFrom ──────────────────────────────────────────────

    @Test
    @DisplayName("isoCodeVOFrom: all three values present  →  fully populated VO")
    void isoCodeVOFrom_allPresent() {
      IsoCodeVO vo = CountryMapper.isoCodeVOFrom(ALPHA_2, ALPHA_3, NUMERIC);

      assertThat(vo)                .isNotNull();
      assertThat(vo.getAlpha2())    .isEqualTo(ALPHA_2);
      assertThat(vo.getAlpha3())    .isEqualTo(ALPHA_3);
      assertThat(vo.getNumeric())   .isEqualTo(NUMERIC);
    }

    @Test
    @DisplayName("isoCodeVOFrom: only alpha2  →  VO with alpha3 and numeric null")
    void isoCodeVOFrom_onlyAlpha2() {
      IsoCodeVO vo = CountryMapper.isoCodeVOFrom(ALPHA_2, null, null);

      assertThat(vo)                .isNotNull();
      assertThat(vo.getAlpha2())    .isEqualTo(ALPHA_2);
      assertThat(vo.getAlpha3())    .isNull();
      assertThat(vo.getNumeric())   .isNull();
    }

    @Test
    @DisplayName("isoCodeVOFrom: alpha2+alpha3, no numeric  →  VO with numeric null")
    void isoCodeVOFrom_noNumeric() {
      IsoCodeVO vo = CountryMapper.isoCodeVOFrom(ALPHA_2, ALPHA_3, null);

      assertThat(vo)                .isNotNull();
      assertThat(vo.getAlpha2())    .isEqualTo(ALPHA_2);
      assertThat(vo.getAlpha3())    .isEqualTo(ALPHA_3);
      assertThat(vo.getNumeric())   .isNull();
    }

    @Test
    @DisplayName("isoCodeVOFrom: all three null  →  null  (no empty VO created)")
    void isoCodeVOFrom_allNull() {
      assertThat(CountryMapper.isoCodeVOFrom(null, null, null)).isNull();
    }

    @Test
    @DisplayName("IsoCode round-trip: VO → three strings → VO preserves every component")
    void isoCodeRoundTrip() {
      IsoCodeVO original = IsoCodeVO.of(ALPHA_2, ALPHA_3, NUMERIC);

      IsoCodeVO recovered = CountryMapper.isoCodeVOFrom(
        original.getAlpha2(),
        original.getAlpha3(),
        original.getNumeric()
      );

      assertThat(recovered)                .isNotNull();
      assertThat(recovered.getAlpha2())    .isEqualTo(original.getAlpha2());
      assertThat(recovered.getAlpha3())    .isEqualTo(original.getAlpha3());
      assertThat(recovered.getNumeric())   .isEqualTo(original.getNumeric());
    }

    // ── populationVOToLong / longToPopulationVO ────────────────────

    @Test
    @DisplayName("populationVOToLong: extracts the numeric value")
    void populationVOToLong_valid() {
      assertThat(CountryMapper.populationVOToLong(PopulationVO.of(POPULATION_VAL)))
        .isEqualTo(POPULATION_VAL);
    }

    @Test
    @DisplayName("populationVOToLong: null VO  →  null")
    void populationVOToLong_null() {
      assertThat(CountryMapper.populationVOToLong(null)).isNull();
    }

    @Test
    @DisplayName("longToPopulationVO: wraps the Long into a VO")
    void longToPopulationVO_valid() {
      PopulationVO vo = CountryMapper.longToPopulationVO(POPULATION_VAL);

      assertThat(vo)             .isNotNull();
      assertThat(vo.getValue())  .isEqualTo(POPULATION_VAL);
    }

    @Test
    @DisplayName("longToPopulationVO: null Long  →  null")
    void longToPopulationVO_null() {
      assertThat(CountryMapper.longToPopulationVO(null)).isNull();
    }

    @Test
    @DisplayName("Population round-trip: Long → VO → Long recovers original value")
    void populationRoundTrip() {
      Long recovered = CountryMapper.populationVOToLong(
        CountryMapper.longToPopulationVO(POPULATION_VAL)
      );

      assertThat(recovered).isEqualTo(POPULATION_VAL);
    }
  }
}
