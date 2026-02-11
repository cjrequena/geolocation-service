package com.cjrequena.sample.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CountryEntity}.
 * Tests entity behavior, lifecycle callbacks, and data integrity.
 */
@DisplayName("CountryEntity Unit Tests")
class CountryEntityTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
  }

  @Test
  @DisplayName("Should create entity with no-args constructor")
  void shouldCreateEntityWithNoArgsConstructor() {
    // When
    CountryEntity entity = new CountryEntity();

    // Then
    assertThat(entity).isNotNull();
    assertThat(entity.getId()).isNull();
    assertThat(entity.getName()).isNull();
  }

  @Test
  @DisplayName("Should create entity with all-args constructor")
  void shouldCreateEntityWithAllArgsConstructor() throws Exception{
    // Given
    UUID id = UUID.randomUUID();
    String name = "Spain";
    String alpha2 = "ES";
    String alpha3 = "ESP";
    String numeric = "724";
    String phoneCode = "+34";
    String currencyCode = "EUR";
    String capital = "Madrid";
    Long population = 47000000L;
    Boolean active = true;
    JsonNode metadata = objectMapper.readTree("{}");
    OffsetDateTime now = OffsetDateTime.now();

    // When
    CountryEntity entity = new CountryEntity(
      id,
      name,
      alpha2,
      alpha3,
      numeric,
      phoneCode,
      currencyCode,
      capital,
      population,
      active,
      metadata,
      now,
      now,
      null,
      null
    );

    // Then
    assertThat(entity.getId()).isEqualTo(id);
    assertThat(entity.getName()).isEqualTo(name);
    assertThat(entity.getIsoCodeAlpha2()).isEqualTo(alpha2);
    assertThat(entity.getIsoCodeAlpha3()).isEqualTo(alpha3);
    assertThat(entity.getIsoCodeNumeric()).isEqualTo(numeric);
    assertThat(entity.getPhoneCode()).isEqualTo(phoneCode);
    assertThat(entity.getCurrencyCode()).isEqualTo(currencyCode);
    assertThat(entity.getCapital()).isEqualTo(capital);
    assertThat(entity.getPopulation()).isEqualTo(population);
    assertThat(entity.getActive()).isEqualTo(active);
    assertThat(entity.getCreatedAt()).isEqualTo(now);
    assertThat(entity.getUpdatedAt()).isEqualTo(now);
  }

  @Test
  @DisplayName("Should set timestamps and ID on onCreate")
  void shouldSetTimestampsAndIdOnCreate() {
    // Given
    CountryEntity entity = new CountryEntity();
    OffsetDateTime before = OffsetDateTime.now();

    // When
    entity.onCreate();
    OffsetDateTime after = OffsetDateTime.now();

    // Then
    assertThat(entity.getId()).isNotNull();
    assertThat(entity.getCreatedAt()).isNotNull();
    assertThat(entity.getUpdatedAt()).isNotNull();
    assertThat(entity.getActive()).isTrue(); // Default value
    assertThat(entity.getCreatedAt()).isBetween(before, after);
    assertThat(entity.getUpdatedAt()).isBetween(before, after);
    assertThat(entity.getCreatedAt()).isEqualTo(entity.getUpdatedAt());
  }

  @Test
  @DisplayName("Should not override existing ID on onCreate")
  void shouldNotOverrideExistingIdOnCreate() {
    // Given
    UUID existingId = UUID.randomUUID();
    CountryEntity entity = new CountryEntity();
    entity.setId(existingId);

    // When
    entity.onCreate();

    // Then
    assertThat(entity.getId()).isEqualTo(existingId);
  }

  @Test
  @DisplayName("Should set default active to true on onCreate when null")
  void shouldSetDefaultActiveOnCreate() {
    // Given
    CountryEntity entity = new CountryEntity();
    entity.setActive(null);

    // When
    entity.onCreate();

    // Then
    assertThat(entity.getActive()).isTrue();
  }

  @Test
  @DisplayName("Should not override existing active value on onCreate")
  void shouldNotOverrideExistingActiveOnCreate() {
    // Given
    CountryEntity entity = new CountryEntity();
    entity.setActive(false);

    // When
    entity.onCreate();

    // Then
    assertThat(entity.getActive()).isFalse();
  }

  @Test
  @DisplayName("Should update timestamp on onUpdate")
  void shouldUpdateTimestampOnUpdate() throws InterruptedException {
    // Given
    CountryEntity entity = new CountryEntity();
    entity.onCreate();
    OffsetDateTime originalUpdatedAt = entity.getUpdatedAt();
    OffsetDateTime originalCreatedAt = entity.getCreatedAt();

    // Wait to ensure time difference
    Thread.sleep(10);

    // When
    entity.onUpdate();

    // Then
    assertThat(entity.getUpdatedAt()).isAfter(originalUpdatedAt);
    assertThat(entity.getCreatedAt()).isEqualTo(originalCreatedAt);
  }

  @Test
  @DisplayName("Should handle ISO codes correctly")
  void shouldHandleIsoCodesCorrectly() {
    // Given
    CountryEntity entity = new CountryEntity();

    // When
    entity.setIsoCodeAlpha2("US");
    entity.setIsoCodeAlpha3("USA");
    entity.setIsoCodeNumeric("840");

    // Then
    assertThat(entity.getIsoCodeAlpha2()).isEqualTo("US");
    assertThat(entity.getIsoCodeAlpha2()).hasSize(2);
    assertThat(entity.getIsoCodeAlpha3()).isEqualTo("USA");
    assertThat(entity.getIsoCodeAlpha3()).hasSize(3);
    assertThat(entity.getIsoCodeNumeric()).isEqualTo("840");
    assertThat(entity.getIsoCodeNumeric()).hasSize(3);
  }

  @Test
  @DisplayName("Should handle phone code with plus sign")
  void shouldHandlePhoneCodeWithPlusSign() {
    // Given
    CountryEntity entity = new CountryEntity();

    // When
    entity.setPhoneCode("+1");

    // Then
    assertThat(entity.getPhoneCode()).isEqualTo("+1");
    assertThat(entity.getPhoneCode()).startsWith("+");
  }

  @Test
  @DisplayName("Should handle currency code in ISO 4217 format")
  void shouldHandleCurrencyCode() {
    // Given
    CountryEntity entity = new CountryEntity();

    // When
    entity.setCurrencyCode("EUR");

    // Then
    assertThat(entity.getCurrencyCode()).isEqualTo("EUR");
    assertThat(entity.getCurrencyCode()).hasSize(3);
  }

  @Test
  @DisplayName("Should handle large population values")
  void shouldHandleLargePopulationValues() {
    // Given
    CountryEntity entity = new CountryEntity();
    Long chinaPopulation = 1_400_000_000L;

    // When
    entity.setPopulation(chinaPopulation);

    // Then
    assertThat(entity.getPopulation()).isEqualTo(chinaPopulation);
    assertThat(entity.getPopulation()).isGreaterThan(1_000_000_000L);
  }

  @Test
  @DisplayName("Should handle country with all optional fields null")
  void shouldHandleOptionalFieldsNull() {
    // Given
    CountryEntity entity = new CountryEntity();

    // When
    entity.setName("Test Country");
    entity.setIsoCodeAlpha2("TC");
    entity.setIsoCodeAlpha3(null);
    entity.setIsoCodeNumeric(null);
    entity.setPhoneCode(null);
    entity.setCurrencyCode(null);
    entity.setCapital(null);
    entity.setPopulation(null);

    // Then
    assertThat(entity.getName()).isEqualTo("Test Country");
    assertThat(entity.getIsoCodeAlpha2()).isEqualTo("TC");
    assertThat(entity.getIsoCodeAlpha3()).isNull();
    assertThat(entity.getIsoCodeNumeric()).isNull();
    assertThat(entity.getPhoneCode()).isNull();
    assertThat(entity.getCurrencyCode()).isNull();
    assertThat(entity.getCapital()).isNull();
    assertThat(entity.getPopulation()).isNull();
  }

  @Test
  @DisplayName("Should implement Serializable")
  void shouldImplementSerializable() {
    // Given
    CountryEntity entity = new CountryEntity();

    // Then
    assertThat(entity).isInstanceOf(java.io.Serializable.class);
  }

  @Test
  @DisplayName("Should have correct serial version UID")
  void shouldHaveCorrectSerialVersionUID() throws Exception {
    // When
    java.lang.reflect.Field field = CountryEntity.class.getDeclaredField("serialVersionUID");
    field.setAccessible(true);
    long serialVersionUID = field.getLong(null);

    // Then
    assertThat(serialVersionUID).isEqualTo(1L);
  }

  @Test
  @DisplayName("Should support equals and hashCode from Lombok")
  void shouldSupportEqualsAndHashCode() {
    // Given
    UUID id = UUID.randomUUID();
    CountryEntity entity1 = new CountryEntity();
    entity1.setId(id);
    entity1.setName("Spain");
    entity1.setIsoCodeAlpha2("ES");

    CountryEntity entity2 = new CountryEntity();
    entity2.setId(id);
    entity2.setName("Spain");
    entity2.setIsoCodeAlpha2("ES");

    // Then
    assertThat(entity1).isEqualTo(entity2);
    assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
  }

  @Test
  @DisplayName("Should support toString from Lombok")
  void shouldSupportToString() {
    // Given
    CountryEntity entity = new CountryEntity();
    entity.setName("Spain");
    entity.setIsoCodeAlpha2("ES");

    // When
    String toString = entity.toString();

    // Then
    assertThat(toString).contains("Spain");
    assertThat(toString).contains("ES");
    assertThat(toString).contains("CountryEntity");
  }

  @Test
  @DisplayName("Should handle typical European country data")
  void shouldHandleTypicalEuropeanCountryData() {
    // Given
    CountryEntity spain = new CountryEntity();
    
    // When
    spain.setName("Spain");
    spain.setIsoCodeAlpha2("ES");
    spain.setIsoCodeAlpha3("ESP");
    spain.setIsoCodeNumeric("724");
    spain.setPhoneCode("+34");
    spain.setCurrencyCode("EUR");
    spain.setCapital("Madrid");
    spain.setPopulation(47_000_000L);
    spain.setActive(true);

    // Then
    assertThat(spain.getName()).isEqualTo("Spain");
    assertThat(spain.getIsoCodeAlpha2()).isEqualTo("ES");
    assertThat(spain.getIsoCodeAlpha3()).isEqualTo("ESP");
    assertThat(spain.getIsoCodeNumeric()).isEqualTo("724");
    assertThat(spain.getPhoneCode()).isEqualTo("+34");
    assertThat(spain.getCurrencyCode()).isEqualTo("EUR");
    assertThat(spain.getCapital()).isEqualTo("Madrid");
    assertThat(spain.getPopulation()).isEqualTo(47_000_000L);
    assertThat(spain.getActive()).isTrue();
  }

  @Test
  @DisplayName("Should handle typical American country data")
  void shouldHandleTypicalAmericanCountryData() {
    // Given
    CountryEntity usa = new CountryEntity();
    
    // When
    usa.setName("United States");
    usa.setIsoCodeAlpha2("US");
    usa.setIsoCodeAlpha3("USA");
    usa.setIsoCodeNumeric("840");
    usa.setPhoneCode("+1");
    usa.setCurrencyCode("USD");
    usa.setCapital("Washington, D.C.");
    usa.setPopulation(331_000_000L);
    usa.setActive(true);

    // Then
    assertThat(usa.getName()).isEqualTo("United States");
    assertThat(usa.getIsoCodeAlpha2()).isEqualTo("US");
    assertThat(usa.getIsoCodeAlpha3()).isEqualTo("USA");
    assertThat(usa.getIsoCodeNumeric()).isEqualTo("840");
    assertThat(usa.getPhoneCode()).isEqualTo("+1");
    assertThat(usa.getCurrencyCode()).isEqualTo("USD");
    assertThat(usa.getCapital()).isEqualTo("Washington, D.C.");
    assertThat(usa.getPopulation()).isEqualTo(331_000_000L);
    assertThat(usa.getActive()).isTrue();
  }

  @Test
  @DisplayName("Should handle inactive country")
  void shouldHandleInactiveCountry() {
    // Given
    CountryEntity entity = new CountryEntity();

    // When
    entity.setName("Historical Country");
    entity.setIsoCodeAlpha2("XX");
    entity.setActive(false);

    // Then
    assertThat(entity.getActive()).isFalse();
  }

  @Test
  @DisplayName("Should handle country name with special characters")
  void shouldHandleCountryNameWithSpecialCharacters() {
    // Given
    CountryEntity entity = new CountryEntity();

    // When
    entity.setName("Côte d'Ivoire");

    // Then
    assertThat(entity.getName()).isEqualTo("Côte d'Ivoire");
    assertThat(entity.getName()).contains("'");
    assertThat(entity.getName()).contains("ô");
  }

  @Test
  @DisplayName("Should handle country with multiple phone codes")
  void shouldHandleMultiplePhoneCodes() {
    // Given - Some countries have multiple codes, stored as one string
    CountryEntity entity = new CountryEntity();

    // When
    entity.setPhoneCode("+1");

    // Then
    assertThat(entity.getPhoneCode()).isEqualTo("+1");
  }

  @Test
  @DisplayName("Should handle zero population")
  void shouldHandleZeroPopulation() {
    // Given
    CountryEntity entity = new CountryEntity();

    // When
    entity.setPopulation(0L);

    // Then
    assertThat(entity.getPopulation()).isEqualTo(0L);
  }
}
