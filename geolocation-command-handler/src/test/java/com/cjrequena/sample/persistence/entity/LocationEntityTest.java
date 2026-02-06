package com.cjrequena.sample.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link LocationEntity}.
 * Tests entity behavior, lifecycle callbacks, and data integrity.
 */
@DisplayName("LocationEntity Unit Tests")
class LocationEntityTest {

  private GeometryFactory geometryFactory;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    geometryFactory = new GeometryFactory();
    objectMapper = new ObjectMapper();
  }

  @Test
  @DisplayName("Should create entity with no-args constructor")
  void shouldCreateEntityWithNoArgsConstructor() {
    // When
    LocationEntity entity = new LocationEntity();

    // Then
    assertThat(entity).isNotNull();
    assertThat(entity.getId()).isNull();
    assertThat(entity.getPoint()).isNull();
    assertThat(entity.getZone()).isNull();
  }

  @Test
  @DisplayName("Should create entity with all-args constructor")
  void shouldCreateEntityWithAllArgsConstructor() throws Exception {
    // Given
    UUID id = UUID.randomUUID();
    ZoneEntity zone = new ZoneEntity();
    zone.setId(UUID.randomUUID());
    Point point = geometryFactory.createPoint(new Coordinate(-3.7038, 40.4168));
    point.setSRID(4326);
    BigDecimal altitude = BigDecimal.valueOf(667);
    BigDecimal accuracy = BigDecimal.valueOf(5);
    String address = "Puerta del Sol, 1";
    String postalCode = "28013";
    JsonNode metadata = objectMapper.readTree("{}");
    Boolean active = true;
    OffsetDateTime now = OffsetDateTime.now();

    // When
    LocationEntity entity = new LocationEntity(
      id, zone, point, altitude, accuracy, address,
      postalCode, metadata, active, now, now
    );

    // Then
    assertThat(entity.getId()).isEqualTo(id);
    assertThat(entity.getZone()).isEqualTo(zone);
    assertThat(entity.getPoint()).isEqualTo(point);
    assertThat(entity.getAltitudeMeters()).isEqualTo(altitude);
    assertThat(entity.getAccuracyMeters()).isEqualTo(accuracy);
    assertThat(entity.getAddress()).isEqualTo(address);
    assertThat(entity.getPostalCode()).isEqualTo(postalCode);
    assertThat(entity.getMetadata()).isEqualTo(metadata);
    assertThat(entity.getActive()).isEqualTo(active);
    assertThat(entity.getCreatedAt()).isEqualTo(now);
    assertThat(entity.getUpdatedAt()).isEqualTo(now);
  }

  @Test
  @DisplayName("Should set timestamps and ID on onCreate")
  void shouldSetTimestampsAndIdOnCreate() {
    // Given
    LocationEntity entity = new LocationEntity();
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
    LocationEntity entity = new LocationEntity();
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
    LocationEntity entity = new LocationEntity();
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
    LocationEntity entity = new LocationEntity();
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
    LocationEntity entity = new LocationEntity();
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
  @DisplayName("Should handle zone association")
  void shouldHandleZoneAssociation() {
    // Given
    LocationEntity location = new LocationEntity();
    ZoneEntity zone = new ZoneEntity();
    zone.setId(UUID.randomUUID());
    zone.setName("Trafalgar");

    // When
    location.setZone(zone);

    // Then
    assertThat(location.getZone()).isNotNull();
    assertThat(location.getZone()).isEqualTo(zone);
    assertThat(location.getZone().getName()).isEqualTo("Trafalgar");
  }

  @Test
  @DisplayName("Should handle null zone")
  void shouldHandleNullZone() {
    // Given
    LocationEntity location = new LocationEntity();

    // When
    location.setZone(null);

    // Then
    assertThat(location.getZone()).isNull();
  }

  @Test
  @DisplayName("Should handle Point geometry")
  void shouldHandlePointGeometry() {
    // Given
    LocationEntity location = new LocationEntity();
    Point point = geometryFactory.createPoint(new Coordinate(-3.7038, 40.4168));
    point.setSRID(4326);

    // When
    location.setPoint(point);

    // Then
    assertThat(location.getPoint()).isNotNull();
    assertThat(location.getPoint()).isEqualTo(point);
    assertThat(location.getPoint().getSRID()).isEqualTo(4326);
    assertThat(location.getPoint().getX()).isCloseTo(-3.7038, org.assertj.core.data.Offset.offset(0.0001));
    assertThat(location.getPoint().getY()).isCloseTo(40.4168, org.assertj.core.data.Offset.offset(0.0001));
  }

  @Test
  @DisplayName("Should get latitude from point")
  void shouldGetLatitudeFromPoint() {
    // Given
    LocationEntity location = new LocationEntity();
    Point point = geometryFactory.createPoint(new Coordinate(-3.7038, 40.4168));
    point.setSRID(4326);
    location.setPoint(point);

    // When
    Double latitude = location.getLatitude();

    // Then
    assertThat(latitude).isNotNull();
    assertThat(latitude).isCloseTo(40.4168, org.assertj.core.data.Offset.offset(0.0001));
  }

  @Test
  @DisplayName("Should get longitude from point")
  void shouldGetLongitudeFromPoint() {
    // Given
    LocationEntity location = new LocationEntity();
    Point point = geometryFactory.createPoint(new Coordinate(-3.7038, 40.4168));
    point.setSRID(4326);
    location.setPoint(point);

    // When
    Double longitude = location.getLongitude();

    // Then
    assertThat(longitude).isNotNull();
    assertThat(longitude).isCloseTo(-3.7038, org.assertj.core.data.Offset.offset(0.0001));
  }

  @Test
  @DisplayName("Should return null latitude when point is null")
  void shouldReturnNullLatitudeWhenPointIsNull() {
    // Given
    LocationEntity location = new LocationEntity();
    location.setPoint(null);

    // When
    Double latitude = location.getLatitude();

    // Then
    assertThat(latitude).isNull();
  }

  @Test
  @DisplayName("Should return null longitude when point is null")
  void shouldReturnNullLongitudeWhenPointIsNull() {
    // Given
    LocationEntity location = new LocationEntity();
    location.setPoint(null);

    // When
    Double longitude = location.getLongitude();

    // Then
    assertThat(longitude).isNull();
  }

  @Test
  @DisplayName("Should handle altitude values")
  void shouldHandleAltitudeValues() {
    // Given
    LocationEntity location = new LocationEntity();
    BigDecimal altitude = BigDecimal.valueOf(667.50);

    // When
    location.setAltitudeMeters(altitude);

    // Then
    assertThat(location.getAltitudeMeters()).isEqualTo(altitude);
    assertThat(location.getAltitudeMeters()).isGreaterThan(BigDecimal.valueOf(600));
  }

  @Test
  @DisplayName("Should handle GPS accuracy values")
  void shouldHandleGpsAccuracyValues() {
    // Given
    LocationEntity location = new LocationEntity();
    BigDecimal accuracy = BigDecimal.valueOf(5.0);

    // When
    location.setAccuracyMeters(accuracy);

    // Then
    assertThat(location.getAccuracyMeters()).isEqualTo(accuracy);
    assertThat(location.getAccuracyMeters()).isLessThan(BigDecimal.valueOf(10));
  }

  @Test
  @DisplayName("Should handle address")
  void shouldHandleAddress() {
    // Given
    LocationEntity location = new LocationEntity();
    String address = "Puerta del Sol, 1, 28013 Madrid, Spain";

    // When
    location.setAddress(address);

    // Then
    assertThat(location.getAddress()).isEqualTo(address);
    assertThat(location.getAddress()).contains("Madrid");
  }

  @Test
  @DisplayName("Should handle postal code")
  void shouldHandlePostalCode() {
    // Given
    LocationEntity location = new LocationEntity();

    // When/Then - Different postal code formats
    location.setPostalCode("28013"); // Spain
    assertThat(location.getPostalCode()).isEqualTo("28013");

    location.setPostalCode("10001"); // USA
    assertThat(location.getPostalCode()).isEqualTo("10001");

    location.setPostalCode("SW1A 1AA"); // UK
    assertThat(location.getPostalCode()).isEqualTo("SW1A 1AA");
  }

  @Test
  @DisplayName("Should handle JSON metadata")
  void shouldHandleJsonMetadata() throws Exception {
    // Given
    LocationEntity location = new LocationEntity();
    JsonNode metadata = objectMapper.readTree("{\"source\":\"GPS\",\"device\":\"iPhone\"}");

    // When
    location.setMetadata(metadata);

    // Then
    assertThat(location.getMetadata()).isNotNull();
    assertThat(location.getMetadata().get("source").asText()).isEqualTo("GPS");
    assertThat(location.getMetadata().get("device").asText()).isEqualTo("iPhone");
  }

  @Test
  @DisplayName("Should handle null metadata")
  void shouldHandleNullMetadata() {
    // Given
    LocationEntity location = new LocationEntity();

    // When
    location.setMetadata(null);

    // Then
    assertThat(location.getMetadata()).isNull();
  }

  @Test
  @DisplayName("Should handle typical Madrid location data")
  void shouldHandleTypicalMadridLocationData() {
    // Given
    LocationEntity puertaDelSol = new LocationEntity();
    ZoneEntity trafalgar = new ZoneEntity();
    trafalgar.setName("Trafalgar");
    Point point = geometryFactory.createPoint(new Coordinate(-3.7038, 40.4168));
    point.setSRID(4326);

    // When
    puertaDelSol.setZone(trafalgar);
    puertaDelSol.setPoint(point);
    puertaDelSol.setAltitudeMeters(BigDecimal.valueOf(667));
    puertaDelSol.setAccuracyMeters(BigDecimal.valueOf(5));
    puertaDelSol.setAddress("Puerta del Sol, 1");
    puertaDelSol.setPostalCode("28013");
    puertaDelSol.setActive(true);

    // Then
    assertThat(puertaDelSol.getZone().getName()).isEqualTo("Trafalgar");
    assertThat(puertaDelSol.getLatitude()).isCloseTo(40.4168, org.assertj.core.data.Offset.offset(0.0001));
    assertThat(puertaDelSol.getLongitude()).isCloseTo(-3.7038, org.assertj.core.data.Offset.offset(0.0001));
    assertThat(puertaDelSol.getAltitudeMeters()).isEqualByComparingTo(BigDecimal.valueOf(667));
    assertThat(puertaDelSol.getAccuracyMeters()).isEqualByComparingTo(BigDecimal.valueOf(5));
    assertThat(puertaDelSol.getAddress()).isEqualTo("Puerta del Sol, 1");
    assertThat(puertaDelSol.getPostalCode()).isEqualTo("28013");
    assertThat(puertaDelSol.getActive()).isTrue();
  }

  @Test
  @DisplayName("Should handle typical NYC location data")
  void shouldHandleTypicalNycLocationData() {
    // Given
    LocationEntity timesSquare = new LocationEntity();
    ZoneEntity zone = new ZoneEntity();
    zone.setName("Times Square");
    Point point = geometryFactory.createPoint(new Coordinate(-73.9855, 40.7580));
    point.setSRID(4326);

    // When
    timesSquare.setZone(zone);
    timesSquare.setPoint(point);
    timesSquare.setAltitudeMeters(BigDecimal.valueOf(14));
    timesSquare.setAccuracyMeters(BigDecimal.valueOf(10));
    timesSquare.setAddress("Times Square, New York, NY 10036");
    timesSquare.setPostalCode("10036");
    timesSquare.setActive(true);

    // Then
    assertThat(timesSquare.getZone().getName()).isEqualTo("Times Square");
    assertThat(timesSquare.getLatitude()).isCloseTo(40.7580, org.assertj.core.data.Offset.offset(0.0001));
    assertThat(timesSquare.getLongitude()).isCloseTo(-73.9855, org.assertj.core.data.Offset.offset(0.0001));
    assertThat(timesSquare.getAltitudeMeters()).isEqualByComparingTo(BigDecimal.valueOf(14));
    assertThat(timesSquare.getAddress()).contains("Times Square");
    assertThat(timesSquare.getActive()).isTrue();
  }

  @Test
  @DisplayName("Should handle inactive location")
  void shouldHandleInactiveLocation() {
    // Given
    LocationEntity location = new LocationEntity();

    // When
    location.setAddress("Historical Address");
    location.setActive(false);

    // Then
    assertThat(location.getActive()).isFalse();
  }

  @Test
  @DisplayName("Should handle location with null optional fields")
  void shouldHandleLocationWithNullOptionalFields() {
    // Given
    LocationEntity location = new LocationEntity();
    Point point = geometryFactory.createPoint(new Coordinate(-3.7038, 40.4168));
    point.setSRID(4326);

    // When
    location.setPoint(point);
    location.setZone(null);
    location.setAltitudeMeters(null);
    location.setAccuracyMeters(null);
    location.setAddress(null);
    location.setPostalCode(null);
    location.setMetadata(null);

    // Then
    assertThat(location.getPoint()).isNotNull();
    assertThat(location.getZone()).isNull();
    assertThat(location.getAltitudeMeters()).isNull();
    assertThat(location.getAccuracyMeters()).isNull();
    assertThat(location.getAddress()).isNull();
    assertThat(location.getPostalCode()).isNull();
    assertThat(location.getMetadata()).isNull();
  }

  @Test
  @DisplayName("Should implement Serializable")
  void shouldImplementSerializable() {
    // Given
    LocationEntity entity = new LocationEntity();

    // Then
    assertThat(entity).isInstanceOf(java.io.Serializable.class);
  }

  @Test
  @DisplayName("Should have correct serial version UID")
  void shouldHaveCorrectSerialVersionUID() throws Exception {
    // When
    java.lang.reflect.Field field = LocationEntity.class.getDeclaredField("serialVersionUID");
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
    Point point = geometryFactory.createPoint(new Coordinate(-3.7038, 40.4168));
    point.setSRID(4326);

    LocationEntity location1 = new LocationEntity();
    location1.setId(id);
    location1.setPoint(point);
    location1.setAddress("Puerta del Sol");

    LocationEntity location2 = new LocationEntity();
    location2.setId(id);
    location2.setPoint(point);
    location2.setAddress("Puerta del Sol");

    // Then
    assertThat(location1).isEqualTo(location2);
    assertThat(location1.hashCode()).isEqualTo(location2.hashCode());
  }

  @Test
  @DisplayName("Should support toString from Lombok")
  void shouldSupportToString() {
    // Given
    LocationEntity location = new LocationEntity();
    location.setAddress("Puerta del Sol");

    // When
    String toString = location.toString();

    // Then
    assertThat(toString).contains("Puerta del Sol");
    assertThat(toString).contains("LocationEntity");
  }

  @Test
  @DisplayName("Should handle address with special characters")
  void shouldHandleAddressWithSpecialCharacters() {
    // Given
    LocationEntity location = new LocationEntity();

    // When
    location.setAddress("Calle de José Ortega y Gasset, 29");

    // Then
    assertThat(location.getAddress()).isEqualTo("Calle de José Ortega y Gasset, 29");
    assertThat(location.getAddress()).contains("é");
  }

  @Test
  @DisplayName("Should handle negative altitude values")
  void shouldHandleNegativeAltitudeValues() {
    // Given
    LocationEntity location = new LocationEntity();
    BigDecimal belowSeaLevel = BigDecimal.valueOf(-10.5);

    // When
    location.setAltitudeMeters(belowSeaLevel);

    // Then
    assertThat(location.getAltitudeMeters()).isEqualByComparingTo(belowSeaLevel);
    assertThat(location.getAltitudeMeters()).isLessThan(BigDecimal.ZERO);
  }

  @Test
  @DisplayName("Should handle high altitude values")
  void shouldHandleHighAltitudeValues() {
    // Given
    LocationEntity location = new LocationEntity();
    BigDecimal highAltitude = BigDecimal.valueOf(3500); // Mountain location

    // When
    location.setAltitudeMeters(highAltitude);

    // Then
    assertThat(location.getAltitudeMeters()).isEqualByComparingTo(highAltitude);
    assertThat(location.getAltitudeMeters()).isGreaterThan(BigDecimal.valueOf(3000));
  }

  @Test
  @DisplayName("Should handle poor GPS accuracy")
  void shouldHandlePoorGpsAccuracy() {
    // Given
    LocationEntity location = new LocationEntity();
    BigDecimal poorAccuracy = BigDecimal.valueOf(100); // 100 meters

    // When
    location.setAccuracyMeters(poorAccuracy);

    // Then
    assertThat(location.getAccuracyMeters()).isEqualByComparingTo(poorAccuracy);
    assertThat(location.getAccuracyMeters()).isGreaterThan(BigDecimal.valueOf(50));
  }

  @Test
  @DisplayName("Should handle excellent GPS accuracy")
  void shouldHandleExcellentGpsAccuracy() {
    // Given
    LocationEntity location = new LocationEntity();
    BigDecimal excellentAccuracy = BigDecimal.valueOf(1); // 1 meter

    // When
    location.setAccuracyMeters(excellentAccuracy);

    // Then
    assertThat(location.getAccuracyMeters()).isEqualByComparingTo(excellentAccuracy);
    assertThat(location.getAccuracyMeters()).isLessThan(BigDecimal.valueOf(5));
  }

  @Test
  @DisplayName("Should handle very long addresses")
  void shouldHandleVeryLongAddresses() {
    // Given
    LocationEntity location = new LocationEntity();
    String longAddress = "Calle de la Princesa, 31, Edificio España, Planta 15, Oficina 1502, 28008 Madrid, Spain";

    // When
    location.setAddress(longAddress);

    // Then
    assertThat(location.getAddress()).isEqualTo(longAddress);
    assertThat(location.getAddress().length()).isGreaterThan(50);
  }

  @Test
  @DisplayName("Should handle coordinates at equator")
  void shouldHandleCoordinatesAtEquator() {
    // Given
    LocationEntity location = new LocationEntity();
    Point equatorPoint = geometryFactory.createPoint(new Coordinate(0.0, 0.0));
    equatorPoint.setSRID(4326);

    // When
    location.setPoint(equatorPoint);

    // Then
    assertThat(location.getLatitude()).isEqualTo(0.0);
    assertThat(location.getLongitude()).isEqualTo(0.0);
  }

  @Test
  @DisplayName("Should handle coordinates at prime meridian")
  void shouldHandleCoordinatesAtPrimeMeridian() {
    // Given
    LocationEntity location = new LocationEntity();
    Point meridianPoint = geometryFactory.createPoint(new Coordinate(0.0, 51.4778)); // Greenwich
    meridianPoint.setSRID(4326);

    // When
    location.setPoint(meridianPoint);

    // Then
    assertThat(location.getLongitude()).isEqualTo(0.0);
    assertThat(location.getLatitude()).isCloseTo(51.4778, org.assertj.core.data.Offset.offset(0.0001));
  }

  @Test
  @DisplayName("Should handle coordinates in southern hemisphere")
  void shouldHandleCoordinatesInSouthernHemisphere() {
    // Given
    LocationEntity location = new LocationEntity();
    Point sydneyPoint = geometryFactory.createPoint(new Coordinate(151.2093, -33.8688)); // Sydney
    sydneyPoint.setSRID(4326);

    // When
    location.setPoint(sydneyPoint);

    // Then
    assertThat(location.getLatitude()).isCloseTo(-33.8688, org.assertj.core.data.Offset.offset(0.0001));
    assertThat(location.getLongitude()).isCloseTo(151.2093, org.assertj.core.data.Offset.offset(0.0001));
    assertThat(location.getLatitude()).isLessThan(0.0); // Southern hemisphere
  }
}
