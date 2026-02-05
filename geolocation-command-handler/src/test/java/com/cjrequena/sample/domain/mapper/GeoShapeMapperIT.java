package com.cjrequena.sample.domain.mapper;

import com.cjrequena.sample.domain.model.aggregate.GeoShape;
import com.cjrequena.sample.domain.model.enums.GeometryType;
import com.cjrequena.sample.domain.model.vo.*;
import com.cjrequena.sample.persistence.entity.GeoShapeEntity;
import com.cjrequena.sample.shared.common.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author cjrequena
 */
@SpringBootTest
@DisplayName("GeoShapeMapper Integration Tests")
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class GeoShapeMapperIT {

  private final GeoShapeMapper geoShapeMapper;
  private GeometryFactory geometryFactory;

  @BeforeEach
  void setUp() {
    geometryFactory = new GeometryFactory();
  }

  // ==========================================
  // Entity to Domain Tests
  // ==========================================

  @Test
  @DisplayName("Should map entity to domain with Point geometry")
  void testEntityToDomain_WithPointGeometry() {
    // Given
    GeoShapeEntity entity = createTestEntity();

    // When
    GeoShape domain = geoShapeMapper.toDomain(entity);

    // Then
    assertNotNull(domain);
    assertEquals(entity.getId(), domain.getId());
    assertEquals(entity.getGeometryType(), domain.getGeometryType());

    // Verify geometry mapping
    assertNotNull(domain.getGeometry());
    assertTrue(domain.getGeometry().isPoint());

    // Verify coordinates
    CoordinateVO coords = domain.getGeometry().getPoint().getCoordinates();
    assertEquals(-73.985428, coords.getLongitudeAsDouble(), 0.000001);
    assertEquals(40.748817, coords.getLatitudeAsDouble(), 0.000001);

    // Verify center coordinates
    assertNotNull(domain.getCenterCoordinates());
    assertEquals(entity.getCenterLatitude(), domain.getCenterCoordinates().getLatitude());
    assertEquals(entity.getCenterLongitude(), domain.getCenterCoordinates().getLongitude());

    // Verify radius
    assertNotNull(domain.getRadius());
    assertEquals(entity.getRadiusMeters().toBigIntegerExact(), domain.getRadius().getMeters().toBigIntegerExact());

    // Verify bounds
    assertNotNull(domain.getBounds());

    // Verify metadata
    assertNotNull(domain.getMetadata());
    assertEquals("test-source", domain.getMetadata().getString("source"));

    // Verify audit info
    assertNotNull(domain.getAuditInfo());
    assertEquals(entity.getCreatedAt(), domain.getAuditInfo().getCreatedAt());
    assertEquals(entity.getUpdatedAt(), domain.getAuditInfo().getUpdatedAt());
  }

  @Test
  @DisplayName("Should handle null entity gracefully")
  void testEntityToDomain_WithNullEntity() {
    // When
    GeoShape domain = geoShapeMapper.toDomain(null);

    // Then
    assertNull(domain);
  }

  @Test
  @DisplayName("Should handle entity with minimal fields")
  void testEntityToDomain_WithMinimalFields() {
    // Given
    UUID id = UUID.randomUUID();
    GeoShapeEntity entity = new GeoShapeEntity();
    entity.setId(id);
    entity.setGeometryType(GeometryType.POINT);
    entity.setGeometry(geometryFactory.createPoint(new Coordinate(-74.0, 40.7)));

    // When
    GeoShape domain = geoShapeMapper.toDomain(entity);

    // Then
    assertNotNull(domain);
    assertEquals(id, domain.getId());
    assertEquals(GeometryType.POINT, domain.getGeometryType());
    assertNotNull(domain.getGeometry());
    assertNull(domain.getCenterCoordinates());
    assertNull(domain.getRadius());
    assertNull(domain.getBounds());
    assertNull(domain.getMetadata());
    assertNull(domain.getAuditInfo());
  }

  // ==========================================
  // Domain to Entity Tests
  // ==========================================

  @Test
  @DisplayName("Should map domain to entity with Point geometry")
  void testDomainToEntity_WithPointGeometry() {
    // Given
    GeoShape domain = createTestDomain();

    // When
    GeoShapeEntity entity = geoShapeMapper.toEntity(domain);

    // Then
    assertNotNull(entity);
    assertEquals(domain.getId(), entity.getId());
    assertEquals(domain.getGeometryType(), entity.getGeometryType());

    // Verify geometry mapping
    assertNotNull(entity.getGeometry());
    assertTrue(entity.getGeometry() instanceof org.locationtech.jts.geom.Point);
    Coordinate coord = entity.getGeometry().getCoordinate();
    assertEquals(-73.985428, coord.x, 0.000001); // x = longitude
    assertEquals(40.748817, coord.y, 0.000001);  // y = latitude

    // Verify center coordinates
    assertEquals(domain.getCenterCoordinates().getLatitude(), entity.getCenterLatitude());
    assertEquals(domain.getCenterCoordinates().getLongitude(), entity.getCenterLongitude());

    // Verify radius
    assertEquals(domain.getRadius().getMeters(), entity.getRadiusMeters());

    // Verify bounds (stored as JsonNode)
    assertNotNull(entity.getBounds());
    assertTrue(entity.getBounds().has("north_east"));
    assertTrue(entity.getBounds().has("south_west"));

    // Verify metadata (stored as JsonNode)
    assertNotNull(entity.getMetadata());
    assertEquals("test-source", entity.getMetadata().get("source").asText());
    assertEquals("Test location", entity.getMetadata().get("description").asText());

    // Verify audit info
    assertEquals(domain.getAuditInfo().getCreatedAt(), entity.getCreatedAt());
    assertEquals(domain.getAuditInfo().getUpdatedAt(), entity.getUpdatedAt());
  }

  @Test
  @DisplayName("Should handle null domain gracefully")
  void testDomainToEntity_WithNullDomain() {
    // When
    GeoShapeEntity entity = geoShapeMapper.toEntity(null);

    // Then
    assertNull(entity);
  }

  @Test
  @DisplayName("Should handle domain with minimal fields")
  void testDomainToEntity_WithMinimalFields() {
    // Given
    UUID id = UUID.randomUUID();
    GeoShape domain = GeoShape.builder()
      .id(id)
      .geometryType(GeometryType.POINT)
      .geometry(GeometryVO.ofCoordinates(CoordinateVO.of(40.7, -74.0)))
      .build();

    // When
    GeoShapeEntity entity = geoShapeMapper.toEntity(domain);

    // Then
    assertNotNull(entity);
    assertEquals(id, entity.getId());
    assertEquals(GeometryType.POINT, entity.getGeometryType());
    assertNotNull(entity.getGeometry());
    assertNull(entity.getCenterLatitude());
    assertNull(entity.getCenterLongitude());
    assertNull(entity.getRadiusMeters());
    assertNull(entity.getBounds());
    assertNull(entity.getMetadata());
  }

  // ==========================================
  // Round-trip Tests
  // ==========================================

  @Test
  @DisplayName("Should preserve data in round-trip: entity -> domain -> entity")
  void testRoundTrip_EntityToDomainToEntity() {
    // Given
    GeoShapeEntity originalEntity = createTestEntity();

    // When
    GeoShape domain = geoShapeMapper.toDomain(originalEntity);
    GeoShapeEntity mappedEntity = geoShapeMapper.toEntity(domain);

    // Then
    assertEquals(originalEntity.getId(), mappedEntity.getId());
    assertEquals(originalEntity.getGeometryType(), mappedEntity.getGeometryType());
    assertEquals(originalEntity.getCenterLatitude(), mappedEntity.getCenterLatitude());
    assertEquals(originalEntity.getCenterLongitude(), mappedEntity.getCenterLongitude());
    assertEquals(originalEntity.getRadiusMeters().toBigIntegerExact(), mappedEntity.getRadiusMeters().toBigIntegerExact());

    // Compare geometry coordinates
    Coordinate origCoord = originalEntity.getGeometry().getCoordinate();
    Coordinate mappedCoord = mappedEntity.getGeometry().getCoordinate();
    assertEquals(origCoord.x, mappedCoord.x, 0.000001);
    assertEquals(origCoord.y, mappedCoord.y, 0.000001);
  }

  @Test
  @DisplayName("Should preserve data in round-trip: domain -> entity -> domain")
  void testRoundTrip_DomainToEntityToDomain() {
    // Given
    GeoShape originalDomain = createTestDomain();

    // When
    GeoShapeEntity entity = geoShapeMapper.toEntity(originalDomain);
    GeoShape mappedDomain = geoShapeMapper.toDomain(entity);

    // Then
    assertEquals(originalDomain.getId(), mappedDomain.getId());
    assertEquals(originalDomain.getGeometryType(), mappedDomain.getGeometryType());
    assertEquals(originalDomain.getCenterCoordinates(), mappedDomain.getCenterCoordinates());
    assertEquals(originalDomain.getRadius(), mappedDomain.getRadius());

    // Verify geometry
    CoordinateVO origCoords = originalDomain.getGeometry().getPoint().getCoordinates();
    CoordinateVO mappedCoords = mappedDomain.getGeometry().getPoint().getCoordinates();
    assertEquals(origCoords.getLatitude(), mappedCoords.getLatitude());
    assertEquals(origCoords.getLongitude(), mappedCoords.getLongitude());

    // Verify metadata
    assertEquals(
      originalDomain.getMetadata().getString("source"),
      mappedDomain.getMetadata().getString("source")
    );
  }

  // ==========================================
  // Circle Geometry Tests
  // ==========================================

  @Test
  @DisplayName("Should map domain with Circle geometry to entity")
  void testDomainToEntity_WithCircleGeometry() {
    // Given
    UUID id = UUID.randomUUID();
    CoordinateVO center = CoordinateVO.of(40.748817, -73.985428);
    RadiusVO radius = RadiusVO.of(BigDecimal.valueOf(500));
    CircleVO circle = CircleVO.of(center, radius);

    GeoShape domain = GeoShape.builder()
      .id(id)
      .geometryType(GeometryType.CIRCLE)
      .geometry(GeometryVO.ofCircle(circle))
      .centerCoordinates(center)
      .radius(radius)
      .build();

    // When
    GeoShapeEntity entity = geoShapeMapper.toEntity(domain);

    // Then
    assertNotNull(entity);
    assertEquals(GeometryType.CIRCLE, entity.getGeometryType());

    // Should store circle's center as Point geometry
    Coordinate coord = entity.getGeometry().getCoordinate();
    assertEquals(center.getLongitudeAsDouble(), coord.x, 0.000001);
    assertEquals(center.getLatitudeAsDouble(), coord.y, 0.000001);

    // Should preserve center coordinates
    assertEquals(center.getLatitude(), entity.getCenterLatitude());
    assertEquals(center.getLongitude(), entity.getCenterLongitude());

    // Should preserve radius
    assertEquals(radius.getMeters(), entity.getRadiusMeters());
  }

  // ==========================================
  // Bounds Tests
  // ==========================================

  @Test
  @DisplayName("Should correctly map bounds from entity to domain")
  void testBoundsMapping_EntityToDomain() throws Exception {
    // Given
    UUID id = UUID.randomUUID();
    JsonNode boundsJson = JsonUtil.getObjectMapper().readTree("""
      {
        "north_east": {"latitude": 40.75, "longitude": -73.98},
        "south_west": {"latitude": 40.74, "longitude": -73.99}
      }
      """);

    GeoShapeEntity entity = new GeoShapeEntity();
    entity.setId(id);
    entity.setGeometryType(GeometryType.POINT);
    entity.setGeometry(geometryFactory.createPoint(new Coordinate(-73.985, 40.745)));
    entity.setBounds(boundsJson);

    // When
    GeoShape domain = geoShapeMapper.toDomain(entity);

    // Then
    assertNotNull(domain.getBounds());
    assertEquals(
      BigDecimal.valueOf(40.75).setScale(6, java.math.RoundingMode.HALF_UP),
      domain.getBounds().getNorthEast().getLatitude()
    );
    assertEquals(
      BigDecimal.valueOf(-73.98).setScale(6, java.math.RoundingMode.HALF_UP),
      domain.getBounds().getNorthEast().getLongitude()
    );
  }

  @Test
  @DisplayName("Should correctly map bounds from domain to entity")
  void testBoundsMapping_DomainToEntity() {
    // Given
    UUID id = UUID.randomUUID();
    CoordinateVO northEast = CoordinateVO.of(40.75, -73.98);
    CoordinateVO southWest = CoordinateVO.of(40.74, -73.99);
    BoundVO bounds = BoundVO.of(northEast, southWest);

    GeoShape domain = GeoShape.builder()
      .id(id)
      .geometryType(GeometryType.RECTANGLE)
      .geometry(GeometryVO.ofCoordinates(CoordinateVO.of(40.745, -73.985)))
      .bounds(bounds)
      .build();

    // When
    GeoShapeEntity entity = geoShapeMapper.toEntity(domain);

    // Then
    assertNotNull(entity.getBounds());
    assertTrue(entity.getBounds().has("north_east"));
    assertTrue(entity.getBounds().has("south_west"));

    JsonNode neNode = entity.getBounds().get("north_east");
    assertEquals(northEast.getLatitude().doubleValue(), neNode.get("latitude").asDouble(), 0.000001);
    assertEquals(northEast.getLongitude().doubleValue(), neNode.get("longitude").asDouble(), 0.000001);
  }

  // ==========================================
  // Metadata Tests
  // ==========================================

  @Test
  @DisplayName("Should correctly map complex metadata from entity to domain")
  void testMetadataMapping_EntityToDomain() throws Exception {
    // Given
    JsonNode metadataJson = JsonUtil.getObjectMapper().readTree("""
      {
        "source": "GPS",
        "accuracy": 10.5,
        "timestamp": 1234567890,
        "tags": ["important", "verified"],
        "nested": {
          "level1": "value1"
        }
      }
      """);
    UUID id = UUID.randomUUID();
    GeoShapeEntity entity = new GeoShapeEntity();
    entity.setId(id);
    entity.setGeometryType(GeometryType.POINT);
    entity.setGeometry(geometryFactory.createPoint(new Coordinate(-74.0, 40.7)));
    entity.setMetadata(metadataJson);

    // When
    GeoShape domain = geoShapeMapper.toDomain(entity);

    // Then
    assertNotNull(domain.getMetadata());
    assertEquals("GPS", domain.getMetadata().getString("source"));
    assertEquals(10.5, domain.getMetadata().getDouble("accuracy"), 0.01);
    assertEquals(1234567890L, domain.getMetadata().getLong("timestamp"));
    assertEquals(2, domain.getMetadata().getStringList("tags").size());

    MetadataVO nested = domain.getMetadata().getObject("nested");
    assertEquals("value1", nested.getString("level1"));
  }

  @Test
  @DisplayName("Should correctly map complex metadata from domain to entity")
  void testMetadataMapping_DomainToEntity() {
    // Given
    UUID id = UUID.randomUUID();
    MetadataVO metadata = MetadataVO.empty()
      .with("source", "Manual")
      .withDouble("confidence", 0.95)
      .withBoolean("verified", true)
      .withStringList("categories", java.util.Arrays.asList("retail", "commercial"))
      .withObject("properties", MetadataVO.empty().with("type", "store"));

    GeoShape domain = GeoShape.builder()
      .id(id)
      .geometryType(GeometryType.POINT)
      .geometry(GeometryVO.ofCoordinates(CoordinateVO.of(40.7, -74.0)))
      .metadata(metadata)
      .build();

    // When
    GeoShapeEntity entity = geoShapeMapper.toEntity(domain);

    // Then
    assertNotNull(entity.getMetadata());
    assertEquals("Manual", entity.getMetadata().get("source").asText());
    assertEquals(0.95, entity.getMetadata().get("confidence").asDouble(), 0.01);
    assertTrue(entity.getMetadata().get("verified").asBoolean());
    assertTrue(entity.getMetadata().get("categories").isArray());
    assertTrue(entity.getMetadata().get("properties").isObject());
  }

  // ==========================================
  // Helper Methods
  // ==========================================

  private GeoShapeEntity createTestEntity() {
    UUID id = UUID.randomUUID();
    GeoShapeEntity entity = new GeoShapeEntity();
    entity.setId(id);
    entity.setGeometryType(GeometryType.POINT);

    // Create JTS Point geometry
    entity.setGeometry(geometryFactory.createPoint(
      new Coordinate(-73.985428, 40.748817) // x=longitude, y=latitude
    ));

    // Set center coordinates
    entity.setCenterLatitude(BigDecimal.valueOf(40.748817));
    entity.setCenterLongitude(BigDecimal.valueOf(-73.985428));

    // Set radius
    entity.setRadiusMeters(BigDecimal.valueOf(1000));

    // Set bounds
    try {
      entity.setBounds(JsonUtil.getObjectMapper().readTree("""
        {
          "north_east": {"latitude": 40.76, "longitude": -73.97},
          "south_west": {"latitude": 40.73, "longitude": -74.00}
        }
        """));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    // Set active
    entity.setActive(Boolean.TRUE);

    // Set metadata
    try {
      entity.setMetadata(JsonUtil.getObjectMapper().readTree("""
        {
          "source": "test-source",
          "description": "Test location"
        }
        """));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    // Set audit info
    entity.setCreatedAt(OffsetDateTime.now().minusDays(1));
    entity.setUpdatedAt(OffsetDateTime.now());

    return entity;
  }

  private GeoShape createTestDomain() {
    UUID id = UUID.randomUUID();
    CoordinateVO coordinates = CoordinateVO.of(40.748817, -73.985428);
    CoordinateVO northEast = CoordinateVO.of(40.76, -73.97);
    CoordinateVO southWest = CoordinateVO.of(40.73, -74.00);

    return GeoShape.builder()
      .id(id)
      .geometryType(GeometryType.POINT)
      .geometry(GeometryVO.ofCoordinates(coordinates))
      .centerCoordinates(coordinates)
      .radius(RadiusVO.of(BigDecimal.valueOf(1000)))
      .bounds(BoundVO.of(northEast, southWest))
      .metadata(MetadataVO.empty()
        .with("source", "test-source")
        .with("description", "Test location"))
      .auditInfo(AuditInfoVO.of(
        OffsetDateTime.now().minusDays(1),
        OffsetDateTime.now()
      ))
      .build();
  }
}
