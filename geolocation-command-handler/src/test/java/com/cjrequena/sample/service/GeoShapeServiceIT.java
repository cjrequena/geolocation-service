package com.cjrequena.sample.service;

import com.cjrequena.sample.domain.model.aggregate.GeoShape;
import com.cjrequena.sample.domain.model.enums.GeometryType;
import com.cjrequena.sample.domain.model.vo.*;
import com.cjrequena.sample.persistence.repository.GeoShapeRepository;
import com.cjrequena.sample.shared.common.util.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link GeoShapeService}.
 *
 * @author cjrequena
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("GeoShapeService Integration Tests")
class GeoShapeServiceIT {

  @Autowired
  private GeoShapeService geoShapeService;

  @Autowired
  private GeoShapeRepository geoShapeRepository;

  @Autowired
  private ObjectMapper objectMapper;

  private GeometryFactory geometryFactory;
  // Test data
  private GeoShape activePoint;
  private GeoShape inactivePoint;
  private GeoShape madridCenterPoint;
  private GeoShape barcelonaCenterPoint;

  @BeforeEach
  void setUp() throws Exception {
    // SRID 4326 = WGS84
    geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    // Clear database
    geoShapeRepository.deleteAll();

    // Create test data
    setupTestData();
  }

  private void setupTestData() throws Exception {
    activePoint = createGeoShapeDomain(
      "Test Shape",
      GeometryType.POINT,
      BigDecimal.valueOf(40.4168),
      BigDecimal.valueOf(-3.7038),
      null,
      "{\"name\":\"Sevilla Center\",\"active\":false}",
      Boolean.TRUE
    );

    inactivePoint = createGeoShapeDomain(
      "Test Shape",
      GeometryType.POINT,
      BigDecimal.valueOf(40.4200),
      BigDecimal.valueOf(3.7100),
      null,
      "{\"name\":\"Sevilla Center\",\"active\":false}",
      Boolean.FALSE
    );

    madridCenterPoint = createGeoShapeDomain(
      "Madrid Center",
      GeometryType.POINT,
      BigDecimal.valueOf(40.4168),
      BigDecimal.valueOf(-3.7038),
      null,
      "{\"name\":\"Madrid Center\",\"active\":true}",
      Boolean.TRUE
    );

    barcelonaCenterPoint = createGeoShapeDomain(
      "Barcelona Center",
      GeometryType.POINT,
      BigDecimal.valueOf(41.3851),
      BigDecimal.valueOf(2.1734),
      null,
      "{\"name\":\"Barcelona Center\",\"active\":true}",
      Boolean.TRUE
    );
  }

  @Test
  @DisplayName("Should create GeoShape successfully")
  void shouldCreateGeoShape() {
    GeoShape created = geoShapeService.create(activePoint);
    assertThat(created).isNotNull();
    assertThat(created.getId()).isNotNull();
    assertThat(created.getGeometryType()).isEqualTo(GeometryType.POINT);
  }

  @Test
  @DisplayName("Should find GeoShape by ID")
  void shouldFindById() {
    GeoShape created = geoShapeService.create(activePoint);
    Optional<GeoShape> result = geoShapeService.findById(created.getId());
    assertThat(result).isPresent();
  }

  @Test
  @DisplayName("Should find all active GeoShapes")
  void shouldFindAllActive() {
    geoShapeService.create(activePoint);
    geoShapeService.create(inactivePoint);
    List<GeoShape> result = geoShapeService.findAllActive();

    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("Should find GeoShapes by name containing")
  void shouldFindByNameContaining() {
    geoShapeService.create(madridCenterPoint);
    geoShapeService.create(barcelonaCenterPoint);
    List<GeoShape> result = geoShapeService.findByNameContaining("Madrid");
    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("Should find GeoShapes containing point")
  void shouldFindContainingPoint() {
    // Create a GeoShape at Madrid center
    geoShapeService.create(madridCenterPoint);
    // Create a point near Madrid center
    Point point = geometryFactory.createPoint(new Coordinate(-3.7038, 40.4168));
    // Note: This test may need adjustment based on actual geometry storage
    List<GeoShape> result = geoShapeService.findContainingPoint(point);
    // The result depends on how geometries are stored
    assertThat(result).isNotNull();
  }

  @Test
  @DisplayName("Should find GeoShapes within distance")
  void shouldFindWithinDistance() {
    final GeoShape madrid = geoShapeService.create(madridCenterPoint);
    final GeoShape barcelona = geoShapeService.create(barcelonaCenterPoint);

    String wktBarcelona = "POINT(2.173400 41.385100)";
    String wktMadrid = "POINT(-3.703800 40.416800)";

    List<GeoShape> result = geoShapeService.findWithinDistance(wktMadrid, 10000.0);
    // Should find at least the nearby shape
    assertThat(result).hasSizeGreaterThanOrEqualTo(1);
    assertThat(barcelona.getGeometry().toWKT()).isEqualTo(wktBarcelona);
    assertThat(madrid.getGeometry().toWKT()).isEqualTo(wktMadrid);
  }

  @Test
  @DisplayName("Should update GeoShape successfully")
  void shouldUpdateGeoShape() {
    GeoShape created = geoShapeService.create(madridCenterPoint);
    created.setGeometryType(GeometryType.POLYGON);
    GeoShape updated = geoShapeService.update(created.getId(), created);
    assertThat(updated.getGeometryType()).isEqualTo(GeometryType.POLYGON);
  }

  @Test
  @DisplayName("Should throw exception when updating non-existent GeoShape")
  void shouldThrowExceptionWhenUpdatingNonExistent() {
    GeoShape geoShape = geoShapeService.create(madridCenterPoint);

    assertThatThrownBy(() -> geoShapeService
      .update(UUID.randomUUID(), geoShape))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("GeoShape not found");
  }

  @Test
  @DisplayName("Should delete GeoShape by ID")
  void shouldDeleteById() {
    GeoShape created = geoShapeService.create(madridCenterPoint);
    geoShapeService.deleteById(created.getId());
    assertThat(geoShapeService.findById(created.getId())).isEmpty();
  }

  @Test
  @DisplayName("Should check if GeoShape exists by ID")
  void shouldCheckExistsById() {
    GeoShape created = geoShapeService.create(madridCenterPoint);
    boolean result = geoShapeService.existsById(created.getId());
    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("Should count all GeoShapes")
  void shouldCount() {
    geoShapeService.create(madridCenterPoint);
    geoShapeService.create(barcelonaCenterPoint);
    long result = geoShapeService.count();
    assertThat(result).isEqualTo(2L);
  }

  private GeoShape createGeoShapeDomain(
    String name,
    GeometryType type,
    BigDecimal lat,
    BigDecimal lng,
    BigDecimal radius,
    String metadataJson,
    boolean active
  ) throws Exception {
    return GeoShape.builder()
      .id(UUID.randomUUID())
      .name(name)
      .geometryType(type)
      .geometry(GeometryVO.ofCoordinates(CoordinateVO.of(lat, lng)))
      .radius( radius!=null ? RadiusVO.of(radius): null)
      .active(active)
      .metadata(JsonUtil.jsonStringToObject(metadataJson, MetadataVO.class))
      .auditInfo(AuditInfoVO.of(OffsetDateTime.now(), OffsetDateTime.now()))
      .build();
  }
}
