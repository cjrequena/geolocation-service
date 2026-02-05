package com.cjrequena.sample.persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.TimeZone;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RegionEntity}.
 * Tests entity behavior, lifecycle callbacks, and data integrity.
 */
@DisplayName("RegionEntity Unit Tests")
class RegionEntityTest {

  @Test
  @DisplayName("Should create entity with no-args constructor")
  void shouldCreateEntityWithNoArgsConstructor() {
    // When
    RegionEntity entity = new RegionEntity();

    // Then
    assertThat(entity).isNotNull();
    assertThat(entity.getId()).isNull();
    assertThat(entity.getName()).isNull();
    assertThat(entity.getCountry()).isNull();
    assertThat(entity.getCode()).isNull();
    assertThat(entity.getRegionType()).isNull();
    assertThat(entity.getGeoShape()).isNull();
    assertThat(entity.getPopulation()).isNull();
    assertThat(entity.getTimeZone()).isNull();
  }

  @Test
  @DisplayName("Should create entity with all-args constructor")
  void shouldCreateEntityWithAllArgsConstructor() {
    // Given
    UUID id = UUID.randomUUID();
    CountryEntity country = new CountryEntity();
    country.setId(UUID.randomUUID());
    String name = "Catalonia";
    String code = "CT";
    String regionType = "AUTONOMOUS_COMMUNITY";
    GeoShapeEntity geoShape = new GeoShapeEntity();
    Long population = 7_700_000L;
    TimeZone timeZone = TimeZone.getTimeZone("Europe/Madrid");
    Boolean active = true;
    OffsetDateTime now = OffsetDateTime.now();

    // When
    RegionEntity entity = new RegionEntity(
      id, country, name, code, regionType, geoShape,
      population, timeZone, active, now, now
    );

    // Then
    assertThat(entity.getId()).isEqualTo(id);
    assertThat(entity.getCountry()).isEqualTo(country);
    assertThat(entity.getName()).isEqualTo(name);
    assertThat(entity.getCode()).isEqualTo(code);
    assertThat(entity.getRegionType()).isEqualTo(regionType);
    assertThat(entity.getGeoShape()).isEqualTo(geoShape);
    assertThat(entity.getPopulation()).isEqualTo(population);
    assertThat(entity.getTimeZone()).isEqualTo(timeZone);
    assertThat(entity.getActive()).isEqualTo(active);
    assertThat(entity.getCreatedAt()).isEqualTo(now);
    assertThat(entity.getUpdatedAt()).isEqualTo(now);
  }

  @Test
  @DisplayName("Should set timestamps and ID on onCreate")
  void shouldSetTimestampsAndIdOnCreate() {
    // Given
    RegionEntity entity = new RegionEntity();
    OffsetDateTime before = OffsetDateTime.now();

    // When
    entity.onCreate();
    OffsetDateTime after = OffsetDateTime.now();

    // Then
    assertThat(entity.getId()).isNotNull();
    assertThat(entity.getCreatedAt()).isNotNull();
    assertThat(entity.getUpdatedAt()).isNotNull();
    assertThat(entity.getActive()).isTrue(); // Default value

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
    RegionEntity entity = new RegionEntity();
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
    RegionEntity entity = new RegionEntity();
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
    RegionEntity entity = new RegionEntity();
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
    RegionEntity entity = new RegionEntity();
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
  @DisplayName("Should handle country association")
  void shouldHandleCountryAssociation() {
    // Given
    RegionEntity region = new RegionEntity();
    CountryEntity country = new CountryEntity();
    country.setId(UUID.randomUUID());
    country.setName("Spain");
    country.setIsoCodeAlpha2("ES");

    // When
    region.setCountry(country);

    // Then
    assertThat(region.getCountry()).isNotNull();
    assertThat(region.getCountry()).isEqualTo(country);
    assertThat(region.getCountry().getName()).isEqualTo("Spain");
    assertThat(region.getCountry().getIsoCodeAlpha2()).isEqualTo("ES");
  }

  @Test
  @DisplayName("Should handle GeoShape association")
  void shouldHandleGeoShapeAssociation() {
    // Given
    RegionEntity region = new RegionEntity();
    GeoShapeEntity geoShape = new GeoShapeEntity();
    geoShape.setId(UUID.randomUUID());

    // When
    region.setGeoShape(geoShape);

    // Then
    assertThat(region.getGeoShape()).isNotNull();
    assertThat(region.getGeoShape()).isEqualTo(geoShape);
  }

  @Test
  @DisplayName("Should handle null GeoShape")
  void shouldHandleNullGeoShape() {
    // Given
    RegionEntity region = new RegionEntity();

    // When
    region.setGeoShape(null);

    // Then
    assertThat(region.getGeoShape()).isNull();
  }

  @Test
  @DisplayName("Should handle large population values")
  void shouldHandleLargePopulationValues() {
    // Given
    RegionEntity region = new RegionEntity();
    Long californiaPopulation = 39_500_000L; // California population

    // When
    region.setPopulation(californiaPopulation);

    // Then
    assertThat(region.getPopulation()).isEqualTo(californiaPopulation);
    assertThat(region.getPopulation()).isGreaterThan(30_000_000L);
  }

  @Test
  @DisplayName("Should handle TimeZone objects")
  void shouldHandleTimeZoneObjects() {
    // Given
    RegionEntity region = new RegionEntity();
    TimeZone madridTimeZone = TimeZone.getTimeZone("Europe/Madrid");

    // When
    region.setTimeZone(madridTimeZone);

    // Then
    assertThat(region.getTimeZone()).isEqualTo(madridTimeZone);
    assertThat(region.getTimeZone().getID()).isEqualTo("Europe/Madrid");
  }

  @Test
  @DisplayName("Should handle various timezone formats")
  void shouldHandleVariousTimezoneFormats() {
    // Given
    RegionEntity region = new RegionEntity();

    // When/Then - Different timezone formats
    region.setTimeZone(TimeZone.getTimeZone("America/New_York"));
    assertThat(region.getTimeZone().getID()).isEqualTo("America/New_York");

    region.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
    assertThat(region.getTimeZone().getID()).isEqualTo("Asia/Tokyo");

    region.setTimeZone(TimeZone.getTimeZone("Europe/London"));
    assertThat(region.getTimeZone().getID()).isEqualTo("Europe/London");
  }

  @Test
  @DisplayName("Should handle region type constants")
  void shouldHandleRegionTypeConstants() {
    // Given
    RegionEntity region = new RegionEntity();

    // When/Then - Different region types
    region.setRegionType("STATE");
    assertThat(region.getRegionType()).isEqualTo("STATE");

    region.setRegionType("PROVINCE");
    assertThat(region.getRegionType()).isEqualTo("PROVINCE");

    region.setRegionType("AUTONOMOUS_COMMUNITY");
    assertThat(region.getRegionType()).isEqualTo("AUTONOMOUS_COMMUNITY");

    region.setRegionType("REGION");
    assertThat(region.getRegionType()).isEqualTo("REGION");
  }

  @Test
  @DisplayName("Should handle region codes")
  void shouldHandleRegionCodes() {
    // Given
    RegionEntity region = new RegionEntity();

    // When/Then - Different code formats
    region.setCode("CA"); // California
    assertThat(region.getCode()).isEqualTo("CA");

    region.setCode("TX"); // Texas
    assertThat(region.getCode()).isEqualTo("TX");

    region.setCode("MD"); // Madrid
    assertThat(region.getCode()).isEqualTo("MD");

    region.setCode("CT"); // Catalonia
    assertThat(region.getCode()).isEqualTo("CT");
  }

  @Test
  @DisplayName("Should handle typical Spanish autonomous community data")
  void shouldHandleTypicalSpanishAutonomousCommunityData() {
    // Given
    RegionEntity catalonia = new RegionEntity();
    CountryEntity spain = new CountryEntity();
    spain.setName("Spain");
    spain.setIsoCodeAlpha2("ES");

    // When
    catalonia.setName("Catalonia");
    catalonia.setCountry(spain);
    catalonia.setCode("CT");
    catalonia.setRegionType("AUTONOMOUS_COMMUNITY");
    catalonia.setPopulation(7_700_000L);
    catalonia.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
    catalonia.setActive(true);

    // Then
    assertThat(catalonia.getName()).isEqualTo("Catalonia");
    assertThat(catalonia.getCountry().getName()).isEqualTo("Spain");
    assertThat(catalonia.getCode()).isEqualTo("CT");
    assertThat(catalonia.getRegionType()).isEqualTo("AUTONOMOUS_COMMUNITY");
    assertThat(catalonia.getPopulation()).isEqualTo(7_700_000L);
    assertThat(catalonia.getTimeZone().getID()).isEqualTo("Europe/Madrid");
    assertThat(catalonia.getActive()).isTrue();
  }

  @Test
  @DisplayName("Should handle typical American state data")
  void shouldHandleTypicalAmericanStateData() {
    // Given
    RegionEntity california = new RegionEntity();
    CountryEntity usa = new CountryEntity();
    usa.setName("United States");
    usa.setIsoCodeAlpha2("US");

    // When
    california.setName("California");
    california.setCountry(usa);
    california.setCode("CA");
    california.setRegionType("STATE");
    california.setPopulation(39_500_000L);
    california.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
    california.setActive(true);

    // Then
    assertThat(california.getName()).isEqualTo("California");
    assertThat(california.getCountry().getName()).isEqualTo("United States");
    assertThat(california.getCode()).isEqualTo("CA");
    assertThat(california.getRegionType()).isEqualTo("STATE");
    assertThat(california.getPopulation()).isEqualTo(39_500_000L);
    assertThat(california.getTimeZone().getID()).isEqualTo("America/Los_Angeles");
    assertThat(california.getActive()).isTrue();
  }

  @Test
  @DisplayName("Should handle typical French region data")
  void shouldHandleTypicalFrenchRegionData() {
    // Given
    RegionEntity ileDefrance = new RegionEntity();
    CountryEntity france = new CountryEntity();
    france.setName("France");
    france.setIsoCodeAlpha2("FR");

    // When
    ileDefrance.setName("Île-de-France");
    ileDefrance.setCountry(france);
    ileDefrance.setCode("IDF");
    ileDefrance.setRegionType("REGION");
    ileDefrance.setPopulation(12_300_000L);
    ileDefrance.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
    ileDefrance.setActive(true);

    // Then
    assertThat(ileDefrance.getName()).isEqualTo("Île-de-France");
    assertThat(ileDefrance.getCountry().getName()).isEqualTo("France");
    assertThat(ileDefrance.getCode()).isEqualTo("IDF");
    assertThat(ileDefrance.getRegionType()).isEqualTo("REGION");
    assertThat(ileDefrance.getPopulation()).isEqualTo(12_300_000L);
    assertThat(ileDefrance.getTimeZone().getID()).isEqualTo("Europe/Paris");
    assertThat(ileDefrance.getActive()).isTrue();
  }

  @Test
  @DisplayName("Should handle inactive region")
  void shouldHandleInactiveRegion() {
    // Given
    RegionEntity region = new RegionEntity();

    // When
    region.setName("Historical Region");
    region.setActive(false);

    // Then
    assertThat(region.getActive()).isFalse();
  }

  @Test
  @DisplayName("Should handle region with null optional fields")
  void shouldHandleRegionWithNullOptionalFields() {
    // Given
    RegionEntity region = new RegionEntity();
    CountryEntity country = new CountryEntity();

    // When
    region.setName("Basic Region");
    region.setCountry(country);
    region.setCode(null);
    region.setRegionType(null);
    region.setGeoShape(null);
    region.setPopulation(null);
    region.setTimeZone(null);

    // Then
    assertThat(region.getName()).isEqualTo("Basic Region");
    assertThat(region.getCountry()).isNotNull();
    assertThat(region.getCode()).isNull();
    assertThat(region.getRegionType()).isNull();
    assertThat(region.getGeoShape()).isNull();
    assertThat(region.getPopulation()).isNull();
    assertThat(region.getTimeZone()).isNull();
  }

  @Test
  @DisplayName("Should implement Serializable")
  void shouldImplementSerializable() {
    // Given
    RegionEntity entity = new RegionEntity();

    // Then
    assertThat(entity).isInstanceOf(java.io.Serializable.class);
  }

  @Test
  @DisplayName("Should have correct serial version UID")
  void shouldHaveCorrectSerialVersionUID() throws Exception {
    // When
    java.lang.reflect.Field field = RegionEntity.class.getDeclaredField("serialVersionUID");
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
    CountryEntity country = new CountryEntity();
    country.setId(UUID.randomUUID());

    RegionEntity region1 = new RegionEntity();
    region1.setId(id);
    region1.setName("Catalonia");
    region1.setCountry(country);

    RegionEntity region2 = new RegionEntity();
    region2.setId(id);
    region2.setName("Catalonia");
    region2.setCountry(country);

    // Then
    assertThat(region1).isEqualTo(region2);
    assertThat(region1.hashCode()).isEqualTo(region2.hashCode());
  }

  @Test
  @DisplayName("Should support toString from Lombok")
  void shouldSupportToString() {
    // Given
    RegionEntity region = new RegionEntity();
    region.setName("Catalonia");
    region.setRegionType("AUTONOMOUS_COMMUNITY");

    // When
    String toString = region.toString();

    // Then
    assertThat(toString).contains("Catalonia");
    assertThat(toString).contains("RegionEntity");
  }

  @Test
  @DisplayName("Should handle region name with special characters")
  void shouldHandleRegionNameWithSpecialCharacters() {
    // Given
    RegionEntity region = new RegionEntity();

    // When
    region.setName("Île-de-France");

    // Then
    assertThat(region.getName()).isEqualTo("Île-de-France");
    assertThat(region.getName()).contains("Î");
    assertThat(region.getName()).contains("-");
  }

  @Test
  @DisplayName("Should handle zero population")
  void shouldHandleZeroPopulation() {
    // Given
    RegionEntity region = new RegionEntity();

    // When
    region.setPopulation(0L);

    // Then
    assertThat(region.getPopulation()).isEqualTo(0L);
  }

  @Test
  @DisplayName("Should handle very long region names")
  void shouldHandleVeryLongRegionNames() {
    // Given
    RegionEntity region = new RegionEntity();
    String longName = "Provincia Autonoma di Bolzano - Alto Adige / Südtirol"; // Italian region

    // When
    region.setName(longName);

    // Then
    assertThat(region.getName()).isEqualTo(longName);
    assertThat(region.getName()).contains("/");
    assertThat(region.getName()).contains("-");
  }

  @Test
  @DisplayName("Should handle GMT timezone")
  void shouldHandleGmtTimezone() {
    // Given
    RegionEntity region = new RegionEntity();

    // When
    region.setTimeZone(TimeZone.getTimeZone("GMT"));

    // Then
    assertThat(region.getTimeZone().getID()).isEqualTo("GMT");
  }

  @Test
  @DisplayName("Should handle UTC timezone")
  void shouldHandleUtcTimezone() {
    // Given
    RegionEntity region = new RegionEntity();

    // When
    region.setTimeZone(TimeZone.getTimeZone("UTC"));

    // Then
    assertThat(region.getTimeZone().getID()).isEqualTo("UTC");
  }

  @Test
  @DisplayName("Should handle region with complex administrative structure")
  void shouldHandleRegionWithComplexAdministrativeStructure() {
    // Given - German state with complex name
    RegionEntity region = new RegionEntity();
    CountryEntity germany = new CountryEntity();
    germany.setName("Germany");
    germany.setIsoCodeAlpha2("DE");

    // When
    region.setName("Baden-Württemberg");
    region.setCountry(germany);
    region.setCode("BW");
    region.setRegionType("STATE");
    region.setPopulation(11_100_000L);
    region.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
    region.setActive(true);

    // Then
    assertThat(region.getName()).isEqualTo("Baden-Württemberg");
    assertThat(region.getName()).contains("ü");
    assertThat(region.getCode()).isEqualTo("BW");
    assertThat(region.getRegionType()).isEqualTo("STATE");
    assertThat(region.getCountry().getIsoCodeAlpha2()).isEqualTo("DE");
  }

  @Test
  @DisplayName("Should handle region type case sensitivity")
  void shouldHandleRegionTypeCaseSensitivity() {
    // Given
    RegionEntity region = new RegionEntity();

    // When/Then - Region types should be stored as provided
    region.setRegionType("state");
    assertThat(region.getRegionType()).isEqualTo("state");

    region.setRegionType("STATE");
    assertThat(region.getRegionType()).isEqualTo("STATE");

    region.setRegionType("State");
    assertThat(region.getRegionType()).isEqualTo("State");
  }

  @Test
  @DisplayName("Should handle short region codes")
  void shouldHandleShortRegionCodes() {
    // Given
    RegionEntity region = new RegionEntity();

    // When/Then - Single character codes
    region.setCode("A");
    assertThat(region.getCode()).isEqualTo("A");
    assertThat(region.getCode()).hasSize(1);
  }

  @Test
  @DisplayName("Should handle long region codes")
  void shouldHandleLongRegionCodes() {
    // Given
    RegionEntity region = new RegionEntity();

    // When/Then - Longer codes
    region.setCode("AUTONOMOUS_COMMUNITY_01");
    assertThat(region.getCode()).isEqualTo("AUTONOMOUS_COMMUNITY_01");
    assertThat(region.getCode()).hasSize(23);
  }
}
