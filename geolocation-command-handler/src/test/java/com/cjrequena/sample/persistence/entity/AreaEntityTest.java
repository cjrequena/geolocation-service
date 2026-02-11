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
 * Unit tests for {@link AreaEntity}.
 * Tests entity behavior, lifecycle callbacks, and data integrity.
 */
@DisplayName("AreaEntity Unit Tests")
class AreaEntityTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
  }

  @Test
  @DisplayName("Should create entity with no-args constructor")
  void shouldCreateEntityWithNoArgsConstructor() {
    // When
    AreaEntity entity = new AreaEntity();

    // Then
    assertThat(entity).isNotNull();
    assertThat(entity.getId()).isNull();
    assertThat(entity.getName()).isNull();
    assertThat(entity.getCity()).isNull();
    //    assertThat(entity.getActive()).isNull();
  }

  @Test
  @DisplayName("Should create entity with all-args constructor")
  void shouldCreateEntityWithAllArgsConstructor() throws Exception {
    // Given
    UUID id = UUID.randomUUID();
    CityEntity city = new CityEntity();
    city.setId(UUID.randomUUID());
    String name = "Chamberí";
    String areaType = "DISTRICT";
    GeoShapeEntity geoShape = new GeoShapeEntity();
    Long population = 150_000L;
    String postalCode = "28010";
    Boolean active = true;
    JsonNode metadata = objectMapper.readTree("{}");
    OffsetDateTime now = OffsetDateTime.now();

    // When
    AreaEntity entity = new AreaEntity(
      id,
      city,
      name,
      areaType,
      geoShape,
      population,
      postalCode,
      active,
      metadata,
      now,
      now,
      null,
      null
    );

    // Then
    assertThat(entity.getId()).isEqualTo(id);
    assertThat(entity.getCity()).isEqualTo(city);
    assertThat(entity.getName()).isEqualTo(name);
    assertThat(entity.getAreaType()).isEqualTo(areaType);
    assertThat(entity.getGeoShape()).isEqualTo(geoShape);
    assertThat(entity.getPopulation()).isEqualTo(population);
    assertThat(entity.getPostalCode()).isEqualTo(postalCode);
    assertThat(entity.getActive()).isEqualTo(active);
    assertThat(entity.getCreatedAt()).isEqualTo(now);
    assertThat(entity.getUpdatedAt()).isEqualTo(now);
  }

  @Test
  @DisplayName("Should set timestamps and ID on onCreate")
  void shouldSetTimestampsAndIdOnCreate() {
    // Given
    AreaEntity entity = new AreaEntity();
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
    AreaEntity entity = new AreaEntity();
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
    AreaEntity entity = new AreaEntity();
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
    AreaEntity entity = new AreaEntity();
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
    AreaEntity entity = new AreaEntity();
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
  @DisplayName("Should handle city association")
  void shouldHandleCityAssociation() {
    // Given
    AreaEntity area = new AreaEntity();
    CityEntity city = new CityEntity();
    city.setId(UUID.randomUUID());
    city.setName("Madrid");

    // When
    area.setCity(city);

    // Then
    assertThat(area.getCity()).isNotNull();
    assertThat(area.getCity()).isEqualTo(city);
    assertThat(area.getCity().getName()).isEqualTo("Madrid");
  }

  @Test
  @DisplayName("Should handle GeoShape association")
  void shouldHandleGeoShapeAssociation() {
    // Given
    AreaEntity area = new AreaEntity();
    GeoShapeEntity geoShape = new GeoShapeEntity();
    geoShape.setId(UUID.randomUUID());

    // When
    area.setGeoShape(geoShape);

    // Then
    assertThat(area.getGeoShape()).isNotNull();
    assertThat(area.getGeoShape()).isEqualTo(geoShape);
  }

  @Test
  @DisplayName("Should handle null GeoShape")
  void shouldHandleNullGeoShape() {
    // Given
    AreaEntity area = new AreaEntity();

    // When
    area.setGeoShape(null);

    // Then
    assertThat(area.getGeoShape()).isNull();
  }

  @Test
  @DisplayName("Should handle different area types")
  void shouldHandleDifferentAreaTypes() {
    // Given
    AreaEntity area = new AreaEntity();

    // When/Then - Different area types
    area.setAreaType("DISTRICT");
    assertThat(area.getAreaType()).isEqualTo("DISTRICT");

    area.setAreaType("NEIGHBORHOOD");
    assertThat(area.getAreaType()).isEqualTo("NEIGHBORHOOD");

    area.setAreaType("BOROUGH");
    assertThat(area.getAreaType()).isEqualTo("BOROUGH");

    area.setAreaType("WARD");
    assertThat(area.getAreaType()).isEqualTo("WARD");
  }

  @Test
  @DisplayName("Should handle null area type")
  void shouldHandleNullAreaType() {
    // Given
    AreaEntity area = new AreaEntity();

    // When
    area.setAreaType(null);

    // Then
    assertThat(area.getAreaType()).isNull();
  }

  @Test
  @DisplayName("Should handle population values")
  void shouldHandlePopulationValues() {
    // Given
    AreaEntity area = new AreaEntity();
    Long manhattanPopulation = 1_630_000L;

    // When
    area.setPopulation(manhattanPopulation);

    // Then
    assertThat(area.getPopulation()).isEqualTo(manhattanPopulation);
    assertThat(area.getPopulation()).isGreaterThan(1_000_000L);
  }

  @Test
  @DisplayName("Should handle postal code formats")
  void shouldHandlePostalCodeFormats() {
    // Given
    AreaEntity area = new AreaEntity();

    // When/Then - Different postal code formats
    area.setPostalCode("28010"); // Spain
    assertThat(area.getPostalCode()).isEqualTo("28010");

    area.setPostalCode("10001"); // USA
    assertThat(area.getPostalCode()).isEqualTo("10001");

    area.setPostalCode("SW1A 1AA"); // UK
    assertThat(area.getPostalCode()).isEqualTo("SW1A 1AA");
  }

  @Test
  @DisplayName("Should handle typical Madrid district data")
  void shouldHandleTypicalMadridDistrictData() {
    // Given
    AreaEntity chamberi = new AreaEntity();
    CityEntity madrid = new CityEntity();
    madrid.setName("Madrid");

    // When
    chamberi.setName("Chamberí");
    chamberi.setCity(madrid);
    chamberi.setAreaType("DISTRICT");
    chamberi.setPopulation(140_000L);
    chamberi.setPostalCode("28010");
    chamberi.setActive(true);

    // Then
    assertThat(chamberi.getName()).isEqualTo("Chamberí");
    assertThat(chamberi.getCity().getName()).isEqualTo("Madrid");
    assertThat(chamberi.getAreaType()).isEqualTo("DISTRICT");
    assertThat(chamberi.getPopulation()).isEqualTo(140_000L);
    assertThat(chamberi.getPostalCode()).isEqualTo("28010");
    assertThat(chamberi.getActive()).isTrue();
  }

  @Test
  @DisplayName("Should handle typical NYC borough data")
  void shouldHandleTypicalNycBoroughData() {
    // Given
    AreaEntity manhattan = new AreaEntity();
    CityEntity nyc = new CityEntity();
    nyc.setName("New York City");

    // When
    manhattan.setName("Manhattan");
    manhattan.setCity(nyc);
    manhattan.setAreaType("BOROUGH");
    manhattan.setPopulation(1_630_000L);
    manhattan.setPostalCode("10001");
    manhattan.setActive(true);

    // Then
    assertThat(manhattan.getName()).isEqualTo("Manhattan");
    assertThat(manhattan.getCity().getName()).isEqualTo("New York City");
    assertThat(manhattan.getAreaType()).isEqualTo("BOROUGH");
    assertThat(manhattan.getPopulation()).isEqualTo(1_630_000L);
    assertThat(manhattan.getPostalCode()).isEqualTo("10001");
    assertThat(manhattan.getActive()).isTrue();
  }

  @Test
  @DisplayName("Should handle typical London borough data")
  void shouldHandleTypicalLondonBoroughData() {
    // Given
    AreaEntity westminster = new AreaEntity();
    CityEntity london = new CityEntity();
    london.setName("London");

    // When
    westminster.setName("Westminster");
    westminster.setCity(london);
    westminster.setAreaType("BOROUGH");
    westminster.setPopulation(250_000L);
    westminster.setPostalCode("SW1A");
    westminster.setActive(true);

    // Then
    assertThat(westminster.getName()).isEqualTo("Westminster");
    assertThat(westminster.getCity().getName()).isEqualTo("London");
    assertThat(westminster.getAreaType()).isEqualTo("BOROUGH");
    assertThat(westminster.getPopulation()).isEqualTo(250_000L);
    assertThat(westminster.getPostalCode()).isEqualTo("SW1A");
    assertThat(westminster.getActive()).isTrue();
  }

  @Test
  @DisplayName("Should handle inactive area")
  void shouldHandleInactiveArea() {
    // Given
    AreaEntity area = new AreaEntity();

    // When
    area.setName("Historical District");
    area.setActive(false);

    // Then
    assertThat(area.getActive()).isFalse();
  }

  @Test
  @DisplayName("Should handle area with null optional fields")
  void shouldHandleAreaWithNullOptionalFields() {
    // Given
    AreaEntity area = new AreaEntity();
    CityEntity city = new CityEntity();

    // When
    area.setName("Small Neighborhood");
    area.setCity(city);
    area.setAreaType(null);
    area.setGeoShape(null);
    area.setPopulation(null);
    area.setPostalCode(null);

    // Then
    assertThat(area.getName()).isEqualTo("Small Neighborhood");
    assertThat(area.getCity()).isNotNull();
    assertThat(area.getAreaType()).isNull();
    assertThat(area.getGeoShape()).isNull();
    assertThat(area.getPopulation()).isNull();
    assertThat(area.getPostalCode()).isNull();
  }

  @Test
  @DisplayName("Should implement Serializable")
  void shouldImplementSerializable() {
    // Given
    AreaEntity entity = new AreaEntity();

    // Then
    assertThat(entity).isInstanceOf(java.io.Serializable.class);
  }

  @Test
  @DisplayName("Should have correct serial version UID")
  void shouldHaveCorrectSerialVersionUID() throws Exception {
    // When
    java.lang.reflect.Field field = AreaEntity.class.getDeclaredField("serialVersionUID");
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
    CityEntity city = new CityEntity();
    city.setId(UUID.randomUUID());

    AreaEntity area1 = new AreaEntity();
    area1.setId(id);
    area1.setName("Chamberí");
    area1.setCity(city);

    AreaEntity area2 = new AreaEntity();
    area2.setId(id);
    area2.setName("Chamberí");
    area2.setCity(city);

    // Then
    assertThat(area1).isEqualTo(area2);
    assertThat(area1.hashCode()).isEqualTo(area2.hashCode());
  }

  @Test
  @DisplayName("Should support toString from Lombok")
  void shouldSupportToString() {
    // Given
    AreaEntity area = new AreaEntity();
    area.setName("Chamberí");
    area.setAreaType("DISTRICT");

    // When
    String toString = area.toString();

    // Then
    assertThat(toString).contains("Chamberí");
    assertThat(toString).contains("AreaEntity");
  }

  @Test
  @DisplayName("Should handle area name with special characters")
  void shouldHandleAreaNameWithSpecialCharacters() {
    // Given
    AreaEntity area = new AreaEntity();

    // When
    area.setName("Montmartre - 18ème");

    // Then
    assertThat(area.getName()).isEqualTo("Montmartre - 18ème");
    assertThat(area.getName()).contains("è");
    assertThat(area.getName()).contains("-");
  }

  @Test
  @DisplayName("Should handle zero population")
  void shouldHandleZeroPopulation() {
    // Given
    AreaEntity area = new AreaEntity();

    // When
    area.setPopulation(0L);

    // Then
    assertThat(area.getPopulation()).isEqualTo(0L);
  }

  @Test
  @DisplayName("Should handle very long area names")
  void shouldHandleVeryLongAreaNames() {
    // Given
    AreaEntity area = new AreaEntity();
    String longName = "Saint-Remy-en-Bouzemont-Saint-Genest-et-Isson";

    // When
    area.setName(longName);

    // Then
    assertThat(area.getName()).isEqualTo(longName);
    assertThat(area.getName().length()).isGreaterThan(40);
  }

  @Test
  @DisplayName("Should handle numeric area names")
  void shouldHandleNumericAreaNames() {
    // Given
    AreaEntity area = new AreaEntity();

    // When
    area.setName("5th Ward");

    // Then
    assertThat(area.getName()).isEqualTo("5th Ward");
    assertThat(area.getName()).matches(".*\\d+.*"); // Contains digit
  }

  @Test
  @DisplayName("Should handle area types with different cases")
  void shouldHandleAreaTypesWithDifferentCases() {
    // Given
    AreaEntity area = new AreaEntity();

    // When/Then
    area.setAreaType("district");
    assertThat(area.getAreaType()).isEqualTo("district");

    area.setAreaType("NEIGHBORHOOD");
    assertThat(area.getAreaType()).isEqualTo("NEIGHBORHOOD");

    area.setAreaType("Borough");
    assertThat(area.getAreaType()).isEqualTo("Borough");
  }

  @Test
  @DisplayName("Should handle small neighborhood populations")
  void shouldHandleSmallNeighborhoodPopulations() {
    // Given
    AreaEntity area = new AreaEntity();

    // When
    area.setPopulation(5_000L);

    // Then
    assertThat(area.getPopulation()).isEqualTo(5_000L);
    assertThat(area.getPopulation()).isLessThan(10_000L);
  }

  @Test
  @DisplayName("Should handle large district populations")
  void shouldHandleLargeDistrictPopulations() {
    // Given
    AreaEntity area = new AreaEntity();

    // When
    area.setPopulation(2_000_000L);

    // Then
    assertThat(area.getPopulation()).isEqualTo(2_000_000L);
    assertThat(area.getPopulation()).isGreaterThan(1_000_000L);
  }
}
