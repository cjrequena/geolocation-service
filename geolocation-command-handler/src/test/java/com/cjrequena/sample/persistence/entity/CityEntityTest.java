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
 * Unit tests for {@link CityEntity}.
 * Tests entity behavior, lifecycle callbacks, and data integrity.
 */
@DisplayName("CityEntity Unit Tests")
class CityEntityTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
  }

  @Test
  @DisplayName("Should create entity with no-args constructor")
  void shouldCreateEntityWithNoArgsConstructor() {
    // When
    CityEntity entity = new CityEntity();

    // Then
    assertThat(entity).isNotNull();
    assertThat(entity.getId()).isNull();
    assertThat(entity.getName()).isNull();
    assertThat(entity.getRegion()).isNull();
    //    assertThat(entity.getActive()).isNull();
    //    assertThat(entity.getCapital()).isNull();
  }

  @Test
  @DisplayName("Should create entity with all-args constructor")
  void shouldCreateEntityWithAllArgsConstructor() throws Exception{
    // Given
    UUID id = UUID.randomUUID();
    RegionEntity region = new RegionEntity();
    region.setId(UUID.randomUUID());
    String name = "Madrid";
    GeoShapeEntity geoShape = new GeoShapeEntity();
    Long population = 3_200_000L;
    String timeZone = "Europe/Madrid";
    String postalCode = "28001";
    Boolean capital = true;
    Boolean active = true;
    JsonNode metadata = objectMapper.readTree("{}");
    OffsetDateTime now = OffsetDateTime.now();

    // When
    CityEntity entity = new CityEntity(
      id,
      region,
      name,
      geoShape,
      population,
      timeZone,
      postalCode,
      capital,
      active,
      metadata,
      now,
      now,
      null,
      null
    );

    // Then
    assertThat(entity.getId()).isEqualTo(id);
    assertThat(entity.getRegion()).isEqualTo(region);
    assertThat(entity.getName()).isEqualTo(name);
    assertThat(entity.getGeoShape()).isEqualTo(geoShape);
    assertThat(entity.getPopulation()).isEqualTo(population);
    assertThat(entity.getTimeZone()).isEqualTo(timeZone);
    assertThat(entity.getPostalCode()).isEqualTo(postalCode);
    assertThat(entity.getCapital()).isEqualTo(capital);
    assertThat(entity.getActive()).isEqualTo(active);
    assertThat(entity.getCreatedAt()).isEqualTo(now);
    assertThat(entity.getUpdatedAt()).isEqualTo(now);
  }

  @Test
  @DisplayName("Should set timestamps and ID on onCreate")
  void shouldSetTimestampsAndIdOnCreate() {
    // Given
    CityEntity entity = new CityEntity();
    OffsetDateTime before = OffsetDateTime.now();

    // When
    entity.onCreate();
    OffsetDateTime after = OffsetDateTime.now();

    // Then
    assertThat(entity.getId()).isNotNull();
    assertThat(entity.getCreatedAt()).isNotNull();
    assertThat(entity.getUpdatedAt()).isNotNull();
    assertThat(entity.getActive()).isTrue(); // Default value
    assertThat(entity.getCapital()).isFalse(); // Default value

    // Compare using Instant to avoid timezone issues
    assertThat(entity.getCreatedAt().toInstant())
      .isBetween(before.toInstant(), after.toInstant());
    assertThat(entity.getUpdatedAt().toInstant())
      .isBetween(before.toInstant(), after.toInstant());
    assertThat(entity.getCreatedAt().toInstant())
      .isEqualTo(entity.getUpdatedAt().toInstant());
  }

  @Test
  @DisplayName("Should not override existing ID on onCreate")
  void shouldNotOverrideExistingIdOnCreate() {
    // Given
    UUID existingId = UUID.randomUUID();
    CityEntity entity = new CityEntity();
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
    CityEntity entity = new CityEntity();
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
    CityEntity entity = new CityEntity();
    entity.setActive(false);

    // When
    entity.onCreate();

    // Then
    assertThat(entity.getActive()).isFalse();
  }

  @Test
  @DisplayName("Should set default capital to false on onCreate when null")
  void shouldSetDefaultCapitalOnCreate() {
    // Given
    CityEntity entity = new CityEntity();
    entity.setCapital(null);

    // When
    entity.onCreate();

    // Then
    assertThat(entity.getCapital()).isFalse();
  }

  @Test
  @DisplayName("Should not override existing capital value on onCreate")
  void shouldNotOverrideExistingCapitalOnCreate() {
    // Given
    CityEntity entity = new CityEntity();
    entity.setCapital(true);

    // When
    entity.onCreate();

    // Then
    assertThat(entity.getCapital()).isTrue();
  }

  @Test
  @DisplayName("Should update timestamp on onUpdate")
  void shouldUpdateTimestampOnUpdate() throws InterruptedException {
    // Given
    CityEntity entity = new CityEntity();
    entity.onCreate();
    OffsetDateTime originalUpdatedAt = entity.getUpdatedAt();
    OffsetDateTime originalCreatedAt = entity.getCreatedAt();

    // Wait to ensure time difference
    Thread.sleep(10);

    // When
    entity.onUpdate();

    // Then
    // Compare instants to avoid timezone issues
    assertThat(entity.getUpdatedAt().toInstant())
      .isAfter(originalUpdatedAt.toInstant());
    assertThat(entity.getCreatedAt().toInstant())
      .isEqualTo(originalCreatedAt.toInstant());
  }

  @Test
  @DisplayName("Should handle region association")
  void shouldHandleRegionAssociation() {
    // Given
    CityEntity city = new CityEntity();
    RegionEntity region = new RegionEntity();
    region.setId(UUID.randomUUID());
    region.setName("Community of Madrid");

    // When
    city.setRegion(region);

    // Then
    assertThat(city.getRegion()).isNotNull();
    assertThat(city.getRegion()).isEqualTo(region);
    assertThat(city.getRegion().getName()).isEqualTo("Community of Madrid");
  }

  @Test
  @DisplayName("Should handle GeoShape association")
  void shouldHandleGeoShapeAssociation() {
    // Given
    CityEntity city = new CityEntity();
    GeoShapeEntity geoShape = new GeoShapeEntity();
    geoShape.setId(UUID.randomUUID());

    // When
    city.setGeoShape(geoShape);

    // Then
    assertThat(city.getGeoShape()).isNotNull();
    assertThat(city.getGeoShape()).isEqualTo(geoShape);
  }

  @Test
  @DisplayName("Should handle null GeoShape")
  void shouldHandleNullGeoShape() {
    // Given
    CityEntity city = new CityEntity();

    // When
    city.setGeoShape(null);

    // Then
    assertThat(city.getGeoShape()).isNull();
  }

  @Test
  @DisplayName("Should handle large population values")
  void shouldHandleLargePopulationValues() {
    // Given
    CityEntity city = new CityEntity();
    Long tokyoPopulation = 37_400_000L; // Tokyo metro area

    // When
    city.setPopulation(tokyoPopulation);

    // Then
    assertThat(city.getPopulation()).isEqualTo(tokyoPopulation);
    assertThat(city.getPopulation()).isGreaterThan(10_000_000L);
  }

  @Test
  @DisplayName("Should handle IANA timezone format")
  void shouldHandleIanaTimezoneFormat() {
    // Given
    CityEntity city = new CityEntity();

    // When
    city.setTimeZone("Europe/Madrid");

    // Then
    assertThat(city.getTimeZone()).isEqualTo("Europe/Madrid");
    assertThat(city.getTimeZone()).contains("/");
  }

  @Test
  @DisplayName("Should handle various timezone formats")
  void shouldHandleVariousTimezoneFormats() {
    // Given
    CityEntity city = new CityEntity();

    // When/Then - Different timezone formats
    city.setTimeZone("America/New_York");
    assertThat(city.getTimeZone()).isEqualTo("America/New_York");

    city.setTimeZone("Asia/Tokyo");
    assertThat(city.getTimeZone()).isEqualTo("Asia/Tokyo");

    city.setTimeZone("Europe/London");
    assertThat(city.getTimeZone()).isEqualTo("Europe/London");
  }

  @Test
  @DisplayName("Should handle postal code formats")
  void shouldHandlePostalCodeFormats() {
    // Given
    CityEntity city = new CityEntity();

    // When/Then - Different postal code formats
    city.setPostalCode("28001"); // Spain
    assertThat(city.getPostalCode()).isEqualTo("28001");

    city.setPostalCode("10001"); // USA
    assertThat(city.getPostalCode()).isEqualTo("10001");

    city.setPostalCode("SW1A 1AA"); // UK
    assertThat(city.getPostalCode()).isEqualTo("SW1A 1AA");
  }

  @Test
  @DisplayName("Should mark city as capital")
  void shouldMarkCityAsCapital() {
    // Given
    CityEntity city = new CityEntity();

    // When
    city.setCapital(true);

    // Then
    assertThat(city.getCapital()).isTrue();
  }

  @Test
  @DisplayName("Should mark city as non-capital")
  void shouldMarkCityAsNonCapital() {
    // Given
    CityEntity city = new CityEntity();

    // When
    city.setCapital(false);

    // Then
    assertThat(city.getCapital()).isFalse();
  }

  @Test
  @DisplayName("Should handle typical Spanish city data")
  void shouldHandleTypicalSpanishCityData() {
    // Given
    CityEntity madrid = new CityEntity();
    RegionEntity region = new RegionEntity();
    region.setName("Community of Madrid");

    // When
    madrid.setName("Madrid");
    madrid.setRegion(region);
    madrid.setPopulation(3_200_000L);
    madrid.setTimeZone("Europe/Madrid");
    madrid.setPostalCode("28001");
    madrid.setCapital(true);
    madrid.setActive(true);

    // Then
    assertThat(madrid.getName()).isEqualTo("Madrid");
    assertThat(madrid.getRegion().getName()).isEqualTo("Community of Madrid");
    assertThat(madrid.getPopulation()).isEqualTo(3_200_000L);
    assertThat(madrid.getTimeZone()).isEqualTo("Europe/Madrid");
    assertThat(madrid.getPostalCode()).isEqualTo("28001");
    assertThat(madrid.getCapital()).isTrue();
    assertThat(madrid.getActive()).isTrue();
  }

  @Test
  @DisplayName("Should handle typical American city data")
  void shouldHandleTypicalAmericanCityData() {
    // Given
    CityEntity newYork = new CityEntity();
    RegionEntity region = new RegionEntity();
    region.setName("New York");

    // When
    newYork.setName("New York City");
    newYork.setRegion(region);
    newYork.setPopulation(8_336_000L);
    newYork.setTimeZone("America/New_York");
    newYork.setPostalCode("10001");
    newYork.setCapital(false);
    newYork.setActive(true);

    // Then
    assertThat(newYork.getName()).isEqualTo("New York City");
    assertThat(newYork.getRegion().getName()).isEqualTo("New York");
    assertThat(newYork.getPopulation()).isEqualTo(8_336_000L);
    assertThat(newYork.getTimeZone()).isEqualTo("America/New_York");
    assertThat(newYork.getPostalCode()).isEqualTo("10001");
    assertThat(newYork.getCapital()).isFalse();
    assertThat(newYork.getActive()).isTrue();
  }

  @Test
  @DisplayName("Should handle inactive city")
  void shouldHandleInactiveCity() {
    // Given
    CityEntity city = new CityEntity();

    // When
    city.setName("Historical City");
    city.setActive(false);

    // Then
    assertThat(city.getActive()).isFalse();
  }

  @Test
  @DisplayName("Should handle city with null optional fields")
  void shouldHandleCityWithNullOptionalFields() {
    // Given
    CityEntity city = new CityEntity();
    RegionEntity region = new RegionEntity();

    // When
    city.setName("Small Town");
    city.setRegion(region);
    city.setGeoShape(null);
    city.setPopulation(null);
    city.setTimeZone(null);
    city.setPostalCode(null);

    // Then
    assertThat(city.getName()).isEqualTo("Small Town");
    assertThat(city.getRegion()).isNotNull();
    assertThat(city.getGeoShape()).isNull();
    assertThat(city.getPopulation()).isNull();
    assertThat(city.getTimeZone()).isNull();
    assertThat(city.getPostalCode()).isNull();
  }

  @Test
  @DisplayName("Should implement Serializable")
  void shouldImplementSerializable() {
    // Given
    CityEntity entity = new CityEntity();

    // Then
    assertThat(entity).isInstanceOf(java.io.Serializable.class);
  }

  @Test
  @DisplayName("Should have correct serial version UID")
  void shouldHaveCorrectSerialVersionUID() throws Exception {
    // When
    java.lang.reflect.Field field = CityEntity.class.getDeclaredField("serialVersionUID");
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
    RegionEntity region = new RegionEntity();
    region.setId(UUID.randomUUID());

    CityEntity city1 = new CityEntity();
    city1.setId(id);
    city1.setName("Madrid");
    city1.setRegion(region);

    CityEntity city2 = new CityEntity();
    city2.setId(id);
    city2.setName("Madrid");
    city2.setRegion(region);

    // Then
    assertThat(city1).isEqualTo(city2);
    assertThat(city1.hashCode()).isEqualTo(city2.hashCode());
  }

  @Test
  @DisplayName("Should support toString from Lombok")
  void shouldSupportToString() {
    // Given
    CityEntity city = new CityEntity();
    city.setName("Madrid");
    city.setCapital(true);

    // When
    String toString = city.toString();

    // Then
    assertThat(toString).contains("Madrid");
    assertThat(toString).contains("CityEntity");
  }

  @Test
  @DisplayName("Should handle city name with special characters")
  void shouldHandleCityNameWithSpecialCharacters() {
    // Given
    CityEntity city = new CityEntity();

    // When
    city.setName("São Paulo");

    // Then
    assertThat(city.getName()).isEqualTo("São Paulo");
    assertThat(city.getName()).contains("ã");
  }

  @Test
  @DisplayName("Should handle zero population")
  void shouldHandleZeroPopulation() {
    // Given
    CityEntity city = new CityEntity();

    // When
    city.setPopulation(0L);

    // Then
    assertThat(city.getPopulation()).isEqualTo(0L);
  }

  @Test
  @DisplayName("Should handle very long city names")
  void shouldHandleVeryLongCityNames() {
    // Given
    CityEntity city = new CityEntity();
    String longName = "Llanfairpwllgwyngyllgogerychwyrndrobwllllantysiliogogogoch"; // Welsh town

    // When
    city.setName(longName);

    // Then
    assertThat(city.getName()).isEqualTo(longName);
    assertThat(city.getName()).hasSize(58);
  }
}
