package com.cjrequena.sample.persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ZoneEntity}.
 * Tests entity behavior, lifecycle callbacks, and data integrity.
 */
@DisplayName("ZoneEntity Unit Tests")
class ZoneEntityTest {

  @Test
  @DisplayName("Should create entity with no-args constructor")
  void shouldCreateEntityWithNoArgsConstructor() {
    // When
    ZoneEntity entity = new ZoneEntity();

    // Then
    assertThat(entity).isNotNull();
    assertThat(entity.getId()).isNull();
    assertThat(entity.getName()).isNull();
    assertThat(entity.getArea()).isNull();
  }

  @Test
  @DisplayName("Should create entity with all-args constructor")
  void shouldCreateEntityWithAllArgsConstructor() {
    // Given
    UUID id = UUID.randomUUID();
    AreaEntity area = new AreaEntity();
    area.setId(UUID.randomUUID());
    String name = "Trafalgar";
    String zoneType = "RESIDENTIAL";
    GeoShapeEntity geoShape = new GeoShapeEntity();
    String postalCode = "28010";
    Boolean active = true;
    OffsetDateTime now = OffsetDateTime.now();

    // When
    ZoneEntity entity = new ZoneEntity(
      id, area, name, zoneType, geoShape, postalCode, active, now, now
    );

    // Then
    assertThat(entity.getId()).isEqualTo(id);
    assertThat(entity.getArea()).isEqualTo(area);
    assertThat(entity.getName()).isEqualTo(name);
    assertThat(entity.getZoneType()).isEqualTo(zoneType);
    assertThat(entity.getGeoShape()).isEqualTo(geoShape);
    assertThat(entity.getPostalCode()).isEqualTo(postalCode);
    assertThat(entity.getActive()).isEqualTo(active);
    assertThat(entity.getCreatedAt()).isEqualTo(now);
    assertThat(entity.getUpdatedAt()).isEqualTo(now);
  }

  @Test
  @DisplayName("Should set timestamps and ID on onCreate")
  void shouldSetTimestampsAndIdOnCreate() {
    // Given
    ZoneEntity entity = new ZoneEntity();
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
    ZoneEntity entity = new ZoneEntity();
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
    ZoneEntity entity = new ZoneEntity();
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
    ZoneEntity entity = new ZoneEntity();
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
    ZoneEntity entity = new ZoneEntity();
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
  @DisplayName("Should handle area association")
  void shouldHandleAreaAssociation() {
    // Given
    ZoneEntity zone = new ZoneEntity();
    AreaEntity area = new AreaEntity();
    area.setId(UUID.randomUUID());
    area.setName("Chamberí");

    // When
    zone.setArea(area);

    // Then
    assertThat(zone.getArea()).isNotNull();
    assertThat(zone.getArea()).isEqualTo(area);
    assertThat(zone.getArea().getName()).isEqualTo("Chamberí");
  }

  @Test
  @DisplayName("Should handle GeoShape association")
  void shouldHandleGeoShapeAssociation() {
    // Given
    ZoneEntity zone = new ZoneEntity();
    GeoShapeEntity geoShape = new GeoShapeEntity();
    geoShape.setId(UUID.randomUUID());

    // When
    zone.setGeoShape(geoShape);

    // Then
    assertThat(zone.getGeoShape()).isNotNull();
    assertThat(zone.getGeoShape()).isEqualTo(geoShape);
  }

  @Test
  @DisplayName("Should handle null GeoShape")
  void shouldHandleNullGeoShape() {
    // Given
    ZoneEntity zone = new ZoneEntity();

    // When
    zone.setGeoShape(null);

    // Then
    assertThat(zone.getGeoShape()).isNull();
  }

  @Test
  @DisplayName("Should handle different zone types")
  void shouldHandleDifferentZoneTypes() {
    // Given
    ZoneEntity zone = new ZoneEntity();

    // When/Then - Different zone types
    zone.setZoneType("RESIDENTIAL");
    assertThat(zone.getZoneType()).isEqualTo("RESIDENTIAL");

    zone.setZoneType("COMMERCIAL");
    assertThat(zone.getZoneType()).isEqualTo("COMMERCIAL");

    zone.setZoneType("INDUSTRIAL");
    assertThat(zone.getZoneType()).isEqualTo("INDUSTRIAL");

    zone.setZoneType("PARK");
    assertThat(zone.getZoneType()).isEqualTo("PARK");

    zone.setZoneType("MIXED_USE");
    assertThat(zone.getZoneType()).isEqualTo("MIXED_USE");
  }

  @Test
  @DisplayName("Should handle null zone type")
  void shouldHandleNullZoneType() {
    // Given
    ZoneEntity zone = new ZoneEntity();

    // When
    zone.setZoneType(null);

    // Then
    assertThat(zone.getZoneType()).isNull();
  }

  @Test
  @DisplayName("Should handle postal code formats")
  void shouldHandlePostalCodeFormats() {
    // Given
    ZoneEntity zone = new ZoneEntity();

    // When/Then - Different postal code formats
    zone.setPostalCode("28010"); // Spain
    assertThat(zone.getPostalCode()).isEqualTo("28010");

    zone.setPostalCode("10036"); // USA
    assertThat(zone.getPostalCode()).isEqualTo("10036");

    zone.setPostalCode("SW1A 1AA"); // UK
    assertThat(zone.getPostalCode()).isEqualTo("SW1A 1AA");
  }

  @Test
  @DisplayName("Should handle typical Madrid zone data")
  void shouldHandleTypicalMadridZoneData() {
    // Given
    ZoneEntity trafalgar = new ZoneEntity();
    AreaEntity chamberi = new AreaEntity();
    chamberi.setName("Chamberí");

    // When
    trafalgar.setName("Trafalgar");
    trafalgar.setArea(chamberi);
    trafalgar.setZoneType("RESIDENTIAL");
    trafalgar.setPostalCode("28010");
    trafalgar.setActive(true);

    // Then
    assertThat(trafalgar.getName()).isEqualTo("Trafalgar");
    assertThat(trafalgar.getArea().getName()).isEqualTo("Chamberí");
    assertThat(trafalgar.getZoneType()).isEqualTo("RESIDENTIAL");
    assertThat(trafalgar.getPostalCode()).isEqualTo("28010");
    assertThat(trafalgar.getActive()).isTrue();
  }

  @Test
  @DisplayName("Should handle typical NYC zone data")
  void shouldHandleTypicalNycZoneData() {
    // Given
    ZoneEntity timesSquare = new ZoneEntity();
    AreaEntity manhattan = new AreaEntity();
    manhattan.setName("Manhattan");

    // When
    timesSquare.setName("Times Square");
    timesSquare.setArea(manhattan);
    timesSquare.setZoneType("COMMERCIAL");
    timesSquare.setPostalCode("10036");
    timesSquare.setActive(true);

    // Then
    assertThat(timesSquare.getName()).isEqualTo("Times Square");
    assertThat(timesSquare.getArea().getName()).isEqualTo("Manhattan");
    assertThat(timesSquare.getZoneType()).isEqualTo("COMMERCIAL");
    assertThat(timesSquare.getPostalCode()).isEqualTo("10036");
    assertThat(timesSquare.getActive()).isTrue();
  }

  @Test
  @DisplayName("Should handle park zone data")
  void shouldHandleParkZoneData() {
    // Given
    ZoneEntity centralPark = new ZoneEntity();
    AreaEntity manhattan = new AreaEntity();
    manhattan.setName("Manhattan");

    // When
    centralPark.setName("Central Park");
    centralPark.setArea(manhattan);
    centralPark.setZoneType("PARK");
    centralPark.setPostalCode("10024");
    centralPark.setActive(true);

    // Then
    assertThat(centralPark.getName()).isEqualTo("Central Park");
    assertThat(centralPark.getArea().getName()).isEqualTo("Manhattan");
    assertThat(centralPark.getZoneType()).isEqualTo("PARK");
    assertThat(centralPark.getPostalCode()).isEqualTo("10024");
    assertThat(centralPark.getActive()).isTrue();
  }

  @Test
  @DisplayName("Should handle inactive zone")
  void shouldHandleInactiveZone() {
    // Given
    ZoneEntity zone = new ZoneEntity();

    // When
    zone.setName("Historical Zone");
    zone.setActive(false);

    // Then
    assertThat(zone.getActive()).isFalse();
  }

  @Test
  @DisplayName("Should handle zone with null optional fields")
  void shouldHandleZoneWithNullOptionalFields() {
    // Given
    ZoneEntity zone = new ZoneEntity();
    AreaEntity area = new AreaEntity();

    // When
    zone.setName("Basic Zone");
    zone.setArea(area);
    zone.setZoneType(null);
    zone.setGeoShape(null);
    zone.setPostalCode(null);

    // Then
    assertThat(zone.getName()).isEqualTo("Basic Zone");
    assertThat(zone.getArea()).isNotNull();
    assertThat(zone.getZoneType()).isNull();
    assertThat(zone.getGeoShape()).isNull();
    assertThat(zone.getPostalCode()).isNull();
  }

  @Test
  @DisplayName("Should implement Serializable")
  void shouldImplementSerializable() {
    // Given
    ZoneEntity entity = new ZoneEntity();

    // Then
    assertThat(entity).isInstanceOf(java.io.Serializable.class);
  }

  @Test
  @DisplayName("Should have correct serial version UID")
  void shouldHaveCorrectSerialVersionUID() throws Exception {
    // When
    java.lang.reflect.Field field = ZoneEntity.class.getDeclaredField("serialVersionUID");
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
    AreaEntity area = new AreaEntity();
    area.setId(UUID.randomUUID());

    ZoneEntity zone1 = new ZoneEntity();
    zone1.setId(id);
    zone1.setName("Trafalgar");
    zone1.setArea(area);

    ZoneEntity zone2 = new ZoneEntity();
    zone2.setId(id);
    zone2.setName("Trafalgar");
    zone2.setArea(area);

    // Then
    assertThat(zone1).isEqualTo(zone2);
    assertThat(zone1.hashCode()).isEqualTo(zone2.hashCode());
  }

  @Test
  @DisplayName("Should support toString from Lombok")
  void shouldSupportToString() {
    // Given
    ZoneEntity zone = new ZoneEntity();
    zone.setName("Trafalgar");
    zone.setZoneType("RESIDENTIAL");

    // When
    String toString = zone.toString();

    // Then
    assertThat(toString).contains("Trafalgar");
    assertThat(toString).contains("ZoneEntity");
  }

  @Test
  @DisplayName("Should handle zone name with special characters")
  void shouldHandleZoneNameWithSpecialCharacters() {
    // Given
    ZoneEntity zone = new ZoneEntity();

    // When
    zone.setName("Ríos Rosas");

    // Then
    assertThat(zone.getName()).isEqualTo("Ríos Rosas");
    assertThat(zone.getName()).contains("í");
  }

  @Test
  @DisplayName("Should handle very long zone names")
  void shouldHandleVeryLongZoneNames() {
    // Given
    ZoneEntity zone = new ZoneEntity();
    String longName = "Saint-Germain-des-Prés Cultural District";

    // When
    zone.setName(longName);

    // Then
    assertThat(zone.getName()).isEqualTo(longName);
    assertThat(zone.getName().length()).isGreaterThan(30);
  }

  @Test
  @DisplayName("Should handle zone types with different cases")
  void shouldHandleZoneTypesWithDifferentCases() {
    // Given
    ZoneEntity zone = new ZoneEntity();

    // When/Then
    zone.setZoneType("residential");
    assertThat(zone.getZoneType()).isEqualTo("residential");

    zone.setZoneType("COMMERCIAL");
    assertThat(zone.getZoneType()).isEqualTo("COMMERCIAL");

    zone.setZoneType("Park");
    assertThat(zone.getZoneType()).isEqualTo("Park");
  }

  @Test
  @DisplayName("Should handle industrial zone")
  void shouldHandleIndustrialZone() {
    // Given
    ZoneEntity zone = new ZoneEntity();
    AreaEntity area = new AreaEntity();
    area.setName("Industrial District");

    // When
    zone.setName("Factory Zone A");
    zone.setArea(area);
    zone.setZoneType("INDUSTRIAL");
    zone.setPostalCode("28050");
    zone.setActive(true);

    // Then
    assertThat(zone.getName()).isEqualTo("Factory Zone A");
    assertThat(zone.getZoneType()).isEqualTo("INDUSTRIAL");
    assertThat(zone.getActive()).isTrue();
  }

  @Test
  @DisplayName("Should handle mixed-use zone")
  void shouldHandleMixedUseZone() {
    // Given
    ZoneEntity zone = new ZoneEntity();
    AreaEntity area = new AreaEntity();
    area.setName("Downtown");

    // When
    zone.setName("Mixed Development Zone");
    zone.setArea(area);
    zone.setZoneType("MIXED_USE");
    zone.setPostalCode("10001");
    zone.setActive(true);

    // Then
    assertThat(zone.getName()).isEqualTo("Mixed Development Zone");
    assertThat(zone.getZoneType()).isEqualTo("MIXED_USE");
    assertThat(zone.getActive()).isTrue();
  }

  @Test
  @DisplayName("Should handle zone with numeric name")
  void shouldHandleZoneWithNumericName() {
    // Given
    ZoneEntity zone = new ZoneEntity();

    // When
    zone.setName("Block 42");

    // Then
    assertThat(zone.getName()).isEqualTo("Block 42");
    assertThat(zone.getName()).matches(".*\\d+.*"); // Contains digit
  }

  @Test
  @DisplayName("Should handle multiple zones with same postal code")
  void shouldHandleMultipleZonesWithSamePostalCode() {
    // Given
    ZoneEntity zone1 = new ZoneEntity();
    ZoneEntity zone2 = new ZoneEntity();
    String sharedPostalCode = "28010";

    // When
    zone1.setName("Zone A");
    zone1.setPostalCode(sharedPostalCode);

    zone2.setName("Zone B");
    zone2.setPostalCode(sharedPostalCode);

    // Then
    assertThat(zone1.getPostalCode()).isEqualTo(zone2.getPostalCode());
    assertThat(zone1.getName()).isNotEqualTo(zone2.getName());
  }
}
