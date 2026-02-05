package com.cjrequena.sample.persistence.repository;

import com.cjrequena.sample.domain.model.enums.GeometryType;
import com.cjrequena.sample.persistence.entity.GeoShapeEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link GeoShapeRepository}.
 * Uses an actual database (H2 or PostgreSQL with PostGIS) to test spatial queries.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("GeoShapeRepository Integration Tests")
class GeoShapeRepositoryIT {

  @Autowired
  private GeoShapeRepository repository;

  private GeometryFactory geometryFactory;
  private ObjectMapper objectMapper;

  // Test data
  private GeoShapeEntity madridCenter;
  private GeoShapeEntity barcelonaCenter;
  private GeoShapeEntity sevillaCenter;
  private GeoShapeEntity madridCircle;
  private GeoShapeEntity spainPolygon;

  @BeforeEach
  void setUp() throws Exception {
    // SRID 4326 = WGS84
    geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    objectMapper = new ObjectMapper();

    // Clear database
    repository.deleteAll();

    // Create test data
    setupTestData();
  }

  private void setupTestData() throws Exception {
    // Madrid center point (active)
    madridCenter = createGeoShape(
      "Madrid Center",
      GeometryType.POINT,
      geometryFactory.createPoint(new Coordinate(-3.703790, 40.416775)),
      new BigDecimal("40.416775"),
      new BigDecimal("-3.703790"),
      null,
      "{\"name\":\"Madrid Center\",\"active\":true}",
      Boolean.TRUE
    );

    // Barcelona center point (active)
    barcelonaCenter = createGeoShape(
      "Barcelona Center",
      GeometryType.POINT,
      geometryFactory.createPoint(new Coordinate(2.173403, 41.385064)),
      new BigDecimal("41.385064"),
      new BigDecimal("2.173403"),
      null,
      "{\"name\":\"Barcelona Center\",\"active\":true}",
      Boolean.TRUE
    );

    // Sevilla center point (inactive)
    sevillaCenter = createGeoShape(
      "Sevilla Center",
      GeometryType.POINT,
      geometryFactory.createPoint(new Coordinate(-5.984459, 37.389092)),
      new BigDecimal("37.389092"),
      new BigDecimal("-5.984459"),
      null,
      "{\"name\":\"Sevilla Center\",\"active\":false}",
      Boolean.FALSE
    );

    // Madrid circle (10km radius)
    madridCircle = createGeoShape(
      "Madrid Circle",
      GeometryType.CIRCLE,
      geometryFactory.createPoint(new Coordinate(-3.703790, 40.416775)),
      new BigDecimal("40.416775"),
      new BigDecimal("-3.703790"),
      new BigDecimal("10000.00"),
      "{\"name\":\"Madrid Circle\",\"active\":true}",
      Boolean.TRUE
    );

    // Simplified Spain polygon
    Coordinate[] spainCoords = {
      new Coordinate(-9.5, 43.8),  // NW
      new Coordinate(3.3, 43.8),    // NE
      new Coordinate(3.3, 36.0),    // SE
      new Coordinate(-9.5, 36.0),   // SW
      new Coordinate(-9.5, 43.8)    // Close
    };
    spainPolygon = createGeoShape(
      "Spain Polygon",
      GeometryType.POLYGON,
      geometryFactory.createPolygon(spainCoords),
      new BigDecimal("40.0"),
      new BigDecimal("-3.5"),
      null,
      "{\"name\":\"Spain Bounds\",\"active\":true}",
      Boolean.TRUE
    );

    // Save all
    repository.saveAll(List.of(
      madridCenter,
      barcelonaCenter,
      sevillaCenter,
      madridCircle,
      spainPolygon
    ));
  }

  private GeoShapeEntity createGeoShape(
    String name,
    GeometryType type,
    Geometry geometry,
    BigDecimal lat,
    BigDecimal lng,
    BigDecimal radius,
    String metadataJson,
    boolean active
  ) throws Exception {
    return GeoShapeEntity.builder()
      .id(UUID.randomUUID())
      .name(name)
      .geometryType(type)
      .geometry(geometry)
      .centerLatitude(lat)
      .centerLongitude(lng)
      .radiusMeters(radius)
      .active(active)
      .metadata(objectMapper.readTree(metadataJson))
      .createdAt(OffsetDateTime.now())
      .updatedAt(OffsetDateTime.now())
      .build();
  }

  // ================================================================
  // Basic CRUD Tests
  // ================================================================

  @Test
  @DisplayName("Should save and find GeoShape by ID")
  void shouldSaveAndFindById() {
    // Given
    UUID id = madridCenter.getId();

    // When
    Optional<GeoShapeEntity> found = repository.findById(id);

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(id);
    assertThat(found.get().getGeometryType()).isEqualTo(GeometryType.POINT);
  }

  @Test
  @DisplayName("Should find all GeoShapes")
  void shouldFindAll() {
    // When
    List<GeoShapeEntity> all = repository.findAll();

    // Then
    assertThat(all).hasSize(5);
  }

  @Test
  @DisplayName("Should delete GeoShape")
  void shouldDelete() {
    // Given
    UUID id = madridCenter.getId();

    // When
    repository.deleteById(id);
    Optional<GeoShapeEntity> found = repository.findById(id);

    // Then
    assertThat(found).isEmpty();
  }

  // ================================================================
  // Active/Inactive Filtering Tests
  // ================================================================

  @Test
  @DisplayName("Should find all active GeoShapes")
  void shouldFindAllActive() {
    // When
    List<GeoShapeEntity> active = repository.findByActiveTrue();

    // Then
    assertThat(active).hasSize(4);
  }

    @Test
    @DisplayName("Should find all inactive GeoShapes")
    void shouldFindAllInactive() {
      // When
      List<GeoShapeEntity> inactive = repository.findByActiveFalse();

      // Then
      assertThat(inactive).hasSize(1);
    }

    @Test
    @DisplayName("Should find active GeoShapes with pagination")
    void shouldFindByActiveWithPagination() {
      // When
      Page<GeoShapeEntity> page = repository.findByActive(true, PageRequest.of(0, 2));

      // Then
      assertThat(page.getContent()).hasSize(2);
      assertThat(page.getTotalElements()).isEqualTo(4);
      assertThat(page.getTotalPages()).isEqualTo(2);
    }

    // ================================================================
    // Name-based Query Tests
    // ================================================================

    @Test
    @DisplayName("Should find GeoShape by exact name")
    void shouldFindByName() {
      // When
      Optional<GeoShapeEntity> found = repository.findByName("Madrid Center");

      // Then
      assertThat(found).isPresent();
      assertThat(found.get().getMetadata().get("name").asText()).isEqualTo("Madrid Center");
    }

    @Test
    @DisplayName("Should not find GeoShape by non-existent name")
    void shouldNotFindByNonExistentName() {
      // When
      Optional<GeoShapeEntity> found = repository.findByName("Valencia Center");

      // Then
      assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find GeoShapes by name containing substring (case-insensitive)")
    void shouldFindByNameContainingIgnoreCase() {
      // When
      List<GeoShapeEntity> found = repository.findByNameContainingIgnoreCase("center");

      // Then
      assertThat(found).hasSize(3); // Madrid, Barcelona, Sevilla
    }

    // ================================================================
    // Spatial Query Tests - Point Containment
    // ================================================================

    @Test
    @DisplayName("Should find GeoShapes containing a point")
    void shouldFindContainingPoint() {
      // Given - point in Madrid
      Point madridPoint = geometryFactory.createPoint(new Coordinate(-3.70, 40.42));

      // When
      List<GeoShapeEntity> found = repository.findContainingPoint(madridPoint);

      // Then
      assertThat(found).isNotEmpty();
      assertThat(found).anyMatch(e ->
        e.getMetadata().get("name").asText().equals("Spain Bounds")
      );
    }

    @Test
    @DisplayName("Should find active GeoShapes containing a point")
    void shouldFindActiveContainingPoint() {
      // Given - point in Madrid
      Point madridPoint = geometryFactory.createPoint(new Coordinate(-3.70, 40.42));

      // When
      List<GeoShapeEntity> found = repository.findActiveContainingPoint(madridPoint);

      // Then
      assertThat(found).isNotEmpty();
      assertThat(found).allMatch(e -> e.getMetadata().get("active").asBoolean());
    }

    // ================================================================
    // Spatial Query Tests - Intersection
    // ================================================================

    @Test
    @DisplayName("Should find GeoShapes intersecting with geometry")
    void shouldFindIntersecting() {
      // Given - small polygon around Madrid
      Coordinate[] coords = {
        new Coordinate(-4.0, 40.0),
        new Coordinate(-3.0, 40.0),
        new Coordinate(-3.0, 41.0),
        new Coordinate(-4.0, 41.0),
        new Coordinate(-4.0, 40.0)
      };
      Polygon madridArea = geometryFactory.createPolygon(coords);

      // When
      List<GeoShapeEntity> found = repository.findIntersecting(madridArea);

      // Then
      assertThat(found).isNotEmpty();
      assertThat(found).anyMatch(e ->
        e.getMetadata().get("name").asText().contains("Madrid")
      );
    }

    @Test
    @DisplayName("Should find active GeoShapes intersecting with geometry")
    void shouldFindActiveIntersecting() {
      // Given - polygon covering Spain
      Coordinate[] coords = {
        new Coordinate(-10.0, 35.0),
        new Coordinate(5.0, 35.0),
        new Coordinate(5.0, 45.0),
        new Coordinate(-10.0, 45.0),
        new Coordinate(-10.0, 35.0)
      };
      Polygon largeArea = geometryFactory.createPolygon(coords);

      // When
      List<GeoShapeEntity> found = repository.findActiveIntersecting(largeArea);

      // Then
      assertThat(found).isNotEmpty();
      assertThat(found).allMatch(e -> e.getMetadata().get("active").asBoolean());
    }

    // ================================================================
    // Spatial Query Tests - Distance-based
    // ================================================================

    @Test
    @DisplayName("Should find GeoShapes within distance from point (WKT)")
    void shouldFindWithinDistance() {
      // Given - Madrid center point, 50km radius
      String wkt = "POINT(-3.703790 40.416775)";
      double distance = 50000; // 50km in meters

      // When
      List<GeoShapeEntity> found = repository.findWithinDistance(wkt, distance);

      // Then
      assertThat(found).isNotEmpty();
      assertThat(found).anyMatch(e -> e.getMetadata().get("name").asText().contains("Madrid")
      );
    }

    @Test
    @DisplayName("Should find GeoShapes within distance ordered by distance")
    void shouldFindWithinDistanceOrderedByDistance() {
      // Given - Madrid center point, 1000km radius
      Point madridPoint = geometryFactory.createPoint(new Coordinate(-3.703790, 40.416775));
      String madridPointWKT = madridPoint.toText();

      double distance = 1000000; // 1000km

      // When
      List<GeoShapeEntity> found = repository.findWithinDistanceOrderedByDistance(
        madridPointWKT,
        distance
      );

      // Then
      assertThat(found).isNotEmpty();
      // First result should be closest (Madrid itself)
      if (!found.isEmpty()) {
        assertThat(found.get(0).getMetadata().get("name").asText())
          .containsIgnoringCase("Madrid");
      }
    }

    // ================================================================
    // Bounding Box Query Tests
    // ================================================================

    @Test
    @DisplayName("Should find GeoShapes in bounding box")
    void shouldFindInBoundingBox() {
      // Given - bounding box around Madrid
      Coordinate[] coords = {
        new Coordinate(-4.5, 40.0),
        new Coordinate(-3.0, 40.0),
        new Coordinate(-3.0, 41.0),
        new Coordinate(-4.5, 41.0),
        new Coordinate(-4.5, 40.0)
      };
      Polygon boundingBox = geometryFactory.createPolygon(coords);

      // When
      List<GeoShapeEntity> found = repository.findInBoundingBox(boundingBox);

      // Then
      assertThat(found).isNotEmpty();
      assertThat(found).anyMatch(e ->
        e.getMetadata().get("name").asText().contains("Madrid")
      );
    }

    // ================================================================
    // Temporal Query Tests
    // ================================================================

    @Test
    @DisplayName("Should find GeoShapes created within time range")
    void shouldFindByCreatedAtBetween() {
      // Given
      OffsetDateTime start = OffsetDateTime.now().minusHours(1);
      OffsetDateTime end = OffsetDateTime.now().plusHours(1);

      // When
      List<GeoShapeEntity> found = repository.findByCreatedAtBetween(start, end);

      // Then
      assertThat(found).hasSize(5);
    }

    @Test
    @DisplayName("Should find top 10 most recently updated GeoShapes")
    void shouldFindTop10ByOrderByUpdatedAtDesc() {
      // When
      List<GeoShapeEntity> found = repository.findTop10ByOrderByUpdatedAtDesc(PageRequest.of(0, 10));

      // Then
      assertThat(found).hasSizeLessThanOrEqualTo(10);
      assertThat(found).hasSizeLessThanOrEqualTo(5); // We only have 5 entities
    }

    // ================================================================
    // Existence Check Tests
    // ================================================================

    @Test
    @DisplayName("Should check if GeoShape exists by name")
    void shouldCheckExistsByName() {
      // When
      boolean exists = repository.existsByName("Madrid Center");
      boolean notExists = repository.existsByName("Valencia Center");

      // Then
      assertThat(exists).isTrue();
      assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should check if active GeoShape exists by name")
    void shouldCheckExistsByNameAndActiveTrue() {
      // When
      boolean activeExists = repository.existsByNameAndActiveTrue("Madrid Center");
      boolean inactiveExists = repository.existsByNameAndActiveTrue("Sevilla Center");

      // Then
      assertThat(activeExists).isTrue();
      assertThat(inactiveExists).isFalse(); // Sevilla is inactive
    }

    // ================================================================
    // Edge Case Tests
    // ================================================================

    @Test
    @DisplayName("Should handle empty result sets")
    void shouldHandleEmptyResults() {
      // Given - point far from all stored geometries
      Point oceanPoint = geometryFactory.createPoint(new Coordinate(0, 0));
      String oceanPointWKT = oceanPoint.toText();


      // When
      List<GeoShapeEntity> found = repository.findWithinDistanceOrderedByDistance(
        oceanPointWKT,
        1000 // 1km radius
      );

      // Then
      assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should handle very large distances")
    void shouldHandleVeryLargeDistances() {
      // Given - Madrid point with global radius
      Point madridPoint = geometryFactory.createPoint(new Coordinate(-3.703790, 40.416775));
      String madridPointWKT = madridPoint.toText();

      double globalDistance = 20000000; // 20,000 km

      // When
      List<GeoShapeEntity> found = repository.findWithinDistanceOrderedByDistance(
        madridPointWKT,
        globalDistance
      );

      // Then
      assertThat(found).hasSize(5); // Should find all entities
    }

    @Test
    @DisplayName("Should handle pagination edge cases")
    void shouldHandlePaginationEdgeCases() {
      // When - request page beyond available data
      Page<GeoShapeEntity> page = repository.findByActive(true, PageRequest.of(10, 10));

      // Then
      assertThat(page.getContent()).isEmpty();
      assertThat(page.getTotalElements()).isEqualTo(4);
    }
}
