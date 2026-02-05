package com.cjrequena.sample.persistence.entity;

import com.cjrequena.sample.domain.model.enums.GeometryType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link GeoShapeEntity}.
 * Tests entity behavior, lifecycle callbacks, and data integrity.
 */
@DisplayName("GeoShapeEntity Unit Tests")
class GeoShapeEntityTest {

  private GeometryFactory geometryFactory;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    // SRID 4326 = WGS84 (standard GPS coordinates)
    geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    objectMapper = new ObjectMapper();
  }

  @Test
  @DisplayName("Should create entity with builder pattern")
  void shouldCreateEntityWithBuilder() throws Exception {
    // Given
    UUID id = UUID.randomUUID();
    Point point = geometryFactory.createPoint(new Coordinate(-3.60667, 37.16389));
    JsonNode metadata = objectMapper.readTree("{\"name\":\"Test Location\"}");

    // When
    GeoShapeEntity entity = GeoShapeEntity.builder()
      .id(id)
      .geometryType(GeometryType.POINT)
      .geometry(point)
      .centerLatitude(new BigDecimal("37.163890"))
      .centerLongitude(new BigDecimal("-3.606670"))
      .metadata(metadata)
      .build();

    // Then
    assertThat(entity).isNotNull();
    assertThat(entity.getId()).isEqualTo(id);
    assertThat(entity.getGeometryType()).isEqualTo(GeometryType.POINT);
    assertThat(entity.getGeometry()).isEqualTo(point);
    assertThat(entity.getCenterLatitude()).isEqualByComparingTo(new BigDecimal("37.163890"));
    assertThat(entity.getCenterLongitude()).isEqualByComparingTo(new BigDecimal("-3.606670"));
    assertThat(entity.getMetadata()).isEqualTo(metadata);
  }

  @Test
  @DisplayName("Should create entity with no-args constructor")
  void shouldCreateEntityWithNoArgsConstructor() {
    // When
    GeoShapeEntity entity = new GeoShapeEntity();

    // Then
    assertThat(entity).isNotNull();
    assertThat(entity.getId()).isNull();
    assertThat(entity.getGeometryType()).isNull();
    assertThat(entity.getGeometry()).isNull();
  }

  @Test
  @DisplayName("Should create entity with all-args constructor")
  void shouldCreateEntityWithAllArgsConstructor() throws Exception {
    Random r= new Random();

    // Given
    UUID id = UUID.randomUUID();
    String name = "GEOSHAME-" + r.nextInt(1000);
    GeometryType type = GeometryType.CIRCLE;
    Point point = geometryFactory.createPoint(new Coordinate(0, 0));
    BigDecimal lat = new BigDecimal("40.416775");
    BigDecimal lng = new BigDecimal("-3.703790");
    BigDecimal radius = new BigDecimal("1000.00");
    JsonNode bounds = objectMapper.readTree("{}");
    JsonNode metadata = objectMapper.readTree("{\"city\":\"Madrid\"}");
    OffsetDateTime now = OffsetDateTime.now();

    // When
    GeoShapeEntity entity = new GeoShapeEntity(
      id, name, type, point, lat, lng, radius, bounds, metadata,Boolean.TRUE, now, now
    );

    // Then
    assertThat(entity.getId()).isEqualTo(id);
    assertThat(entity.getName()).isEqualTo(name);
    assertThat(entity.getGeometryType()).isEqualTo(type);
    assertThat(entity.getGeometry()).isEqualTo(point);
    assertThat(entity.getCenterLatitude()).isEqualTo(lat);
    assertThat(entity.getCenterLongitude()).isEqualTo(lng);
    assertThat(entity.getRadiusMeters()).isEqualTo(radius);
    assertThat(entity.getBounds()).isEqualTo(bounds);
    assertThat(entity.getMetadata()).isEqualTo(metadata);
    assertThat(entity.getCreatedAt()).isEqualTo(now);
    assertThat(entity.getUpdatedAt()).isEqualTo(now);
  }

  @Test
  @DisplayName("Should set timestamps on onCreate")
  void shouldSetTimestampsOnCreate() {
    // Given
    GeoShapeEntity entity = new GeoShapeEntity();
    OffsetDateTime before = OffsetDateTime.now();

    // When
    entity.onCreate();
    OffsetDateTime after = OffsetDateTime.now();

    // Then
    assertThat(entity.getId()).isNotNull();
    assertThat(entity.getCreatedAt()).isNotNull();
    assertThat(entity.getUpdatedAt()).isNotNull();
    assertThat(entity.getCreatedAt()).isBetween(before, after);
    assertThat(entity.getUpdatedAt()).isBetween(before, after);
    assertThat(entity.getCreatedAt()).isEqualTo(entity.getUpdatedAt());
  }

  @Test
  @DisplayName("Should not override existing ID on onCreate")
  void shouldNotOverrideExistingIdOnCreate() {
    // Given
    UUID existingId = UUID.randomUUID();
    GeoShapeEntity entity = new GeoShapeEntity();
    entity.setId(existingId);

    // When
    entity.onCreate();

    // Then
    assertThat(entity.getId()).isEqualTo(existingId);
  }

  @Test
  @DisplayName("Should update timestamp on onUpdate")
  void shouldUpdateTimestampOnUpdate() throws InterruptedException {
    // Given
    GeoShapeEntity entity = new GeoShapeEntity();
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
  @DisplayName("Should support different geometry types")
  void shouldSupportDifferentGeometryTypes() {
    // Test Point
    GeoShapeEntity pointEntity = GeoShapeEntity.builder()
      .geometryType(GeometryType.POINT)
      .geometry(geometryFactory.createPoint(new Coordinate(0, 0)))
      .build();
    assertThat(pointEntity.getGeometryType()).isEqualTo(GeometryType.POINT);

    // Test Circle
    GeoShapeEntity circleEntity = GeoShapeEntity.builder()
      .geometryType(GeometryType.CIRCLE)
      .geometry(geometryFactory.createPoint(new Coordinate(0, 0)))
      .radiusMeters(new BigDecimal("500.00"))
      .build();
    assertThat(circleEntity.getGeometryType()).isEqualTo(GeometryType.CIRCLE);
    assertThat(circleEntity.getRadiusMeters()).isEqualByComparingTo(new BigDecimal("500.00"));
  }

  @Test
  @DisplayName("Should handle JSON metadata")
  void shouldHandleJsonMetadata() throws Exception {
    // Given
    JsonNode metadata = objectMapper.readTree(
      "{\"name\":\"Test\",\"tags\":[\"geo\",\"test\"],\"properties\":{\"color\":\"red\"}}"
    );

    // When
    GeoShapeEntity entity = GeoShapeEntity.builder()
      .metadata(metadata)
      .build();

    // Then
    assertThat(entity.getMetadata()).isNotNull();
    assertThat(entity.getMetadata().get("name").asText()).isEqualTo("Test");
    assertThat(entity.getMetadata().get("tags").isArray()).isTrue();
    assertThat(entity.getMetadata().get("tags")).hasSize(2);
    assertThat(entity.getMetadata().get("properties").get("color").asText()).isEqualTo("red");
  }

  @Test
  @DisplayName("Should handle JSON bounds")
  void shouldHandleJsonBounds() throws Exception {
    // Given
    JsonNode bounds = objectMapper.readTree(
      "{\"minLat\":37.0,\"maxLat\":38.0,\"minLng\":-4.0,\"maxLng\":-3.0}"
    );

    // When
    GeoShapeEntity entity = GeoShapeEntity.builder()
      .bounds(bounds)
      .build();

    // Then
    assertThat(entity.getBounds()).isNotNull();
    assertThat(entity.getBounds().get("minLat").asDouble()).isEqualTo(37.0);
    assertThat(entity.getBounds().get("maxLat").asDouble()).isEqualTo(38.0);
  }

  @Test
  @DisplayName("Should handle precision for coordinates")
  void shouldHandlePrecisionForCoordinates() {
    // Given - precision 9, scale 6 allows values like 123.456789
    BigDecimal latitude = new BigDecimal("37.163890");
    BigDecimal longitude = new BigDecimal("-3.606670");

    // When
    GeoShapeEntity entity = GeoShapeEntity.builder()
      .centerLatitude(latitude)
      .centerLongitude(longitude)
      .build();

    // Then
    assertThat(entity.getCenterLatitude()).isEqualByComparingTo(latitude);
    assertThat(entity.getCenterLongitude()).isEqualByComparingTo(longitude);
  }

  @Test
  @DisplayName("Should handle precision for radius")
  void shouldHandlePrecisionForRadius() {
    // Given - precision 10, scale 2 allows values like 12345678.90
    BigDecimal radius = new BigDecimal("12345678.90");

    // When
    GeoShapeEntity entity = GeoShapeEntity.builder()
      .radiusMeters(radius)
      .build();

    // Then
    assertThat(entity.getRadiusMeters()).isEqualByComparingTo(radius);
  }

  @Test
  @DisplayName("Should implement Serializable")
  void shouldImplementSerializable() {
    // Given
    GeoShapeEntity entity = new GeoShapeEntity();

    // Then
    assertThat(entity).isInstanceOf(java.io.Serializable.class);
  }

  @Test
  @DisplayName("Should have correct serial version UID")
  void shouldHaveCorrectSerialVersionUID() throws Exception {
    // When
    java.lang.reflect.Field field = GeoShapeEntity.class.getDeclaredField("serialVersionUID");
    field.setAccessible(true);
    long serialVersionUID = field.getLong(null);

    // Then
    assertThat(serialVersionUID).isEqualTo(1L);
  }
}
