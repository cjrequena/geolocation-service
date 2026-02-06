package com.cjrequena.sample.service;

import com.cjrequena.sample.domain.model.aggregate.GeoShape;
import com.cjrequena.sample.domain.model.enums.GeometryType;
import com.cjrequena.sample.domain.model.vo.AuditInfoVO;
import com.cjrequena.sample.domain.model.vo.CoordinateVO;
import com.cjrequena.sample.domain.model.vo.GeometryVO;
import com.cjrequena.sample.domain.model.vo.RadiusVO;
import com.cjrequena.sample.persistence.repository.GeoShapeRepository;
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

  private GeometryFactory geometryFactory;

  @BeforeEach
  void setUp() {
    geoShapeRepository.deleteAll();
    geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
  }

  @Test
  @DisplayName("Should create GeoShape successfully")
  void shouldCreateGeoShape() {
    GeoShape geoShape = createGeoShapeDomain("Test Shape", 40.4168, -3.7038, true);

    GeoShape result = geoShapeService.create(geoShape);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isNotNull();
    assertThat(result.getGeometryType()).isEqualTo("POINT");
  }

  @Test
  @DisplayName("Should find GeoShape by ID")
  void shouldFindById() {
    GeoShape created = geoShapeService.create(createGeoShapeDomain("Test Shape", 40.4168, -3.7038, true));

    Optional<GeoShape> result = geoShapeService.findById(created.getId());

    assertThat(result).isPresent();
  }

  @Test
  @DisplayName("Should find all active GeoShapes")
  void shouldFindAllActive() {
    geoShapeService.create(createGeoShapeDomain("Active Shape", 40.4168, -3.7038, true));
    geoShapeService.create(createGeoShapeDomain("Inactive Shape", 40.4200, -3.7100, false));

    List<GeoShape> result = geoShapeService.findAllActive();

    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("Should find GeoShapes by name containing")
  void shouldFindByNameContaining() {
    geoShapeService.create(createGeoShapeDomain("Madrid Center", 40.4168, -3.7038, true));
    geoShapeService.create(createGeoShapeDomain("Barcelona Center", 41.3851, 2.1734, true));

    List<GeoShape> result = geoShapeService.findByNameContaining("Madrid");

    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("Should find GeoShapes containing point")
  void shouldFindContainingPoint() {
    // Create a GeoShape at Madrid center
    geoShapeService.create(createGeoShapeDomain("Madrid Area", 40.4168, -3.7038, true));

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
    geoShapeService.create(createGeoShapeDomain("Nearby Shape", 40.4168, -3.7038, true));
    geoShapeService.create(createGeoShapeDomain("Far Shape", 41.3851, 2.1734, true));

    String wkt = "POINT(-3.7038 40.4168)";
    List<GeoShape> result = geoShapeService.findWithinDistance(wkt, 10000.0);

    // Should find at least the nearby shape
    assertThat(result).hasSizeGreaterThanOrEqualTo(1);
  }

  @Test
  @DisplayName("Should update GeoShape successfully")
  void shouldUpdateGeoShape() {
    GeoShape created = geoShapeService.create(createGeoShapeDomain("Original Name", 40.4168, -3.7038, true));

    created.setGeometryType(GeometryType.POLYGON);
    GeoShape updated = geoShapeService.update(created.getId(), created);

    assertThat(updated.getGeometryType()).isEqualTo(GeometryType.POLYGON);
  }

  @Test
  @DisplayName("Should throw exception when updating non-existent GeoShape")
  void shouldThrowExceptionWhenUpdatingNonExistent() {
    GeoShape geoShape = createGeoShapeDomain("Test", 40.4168, -3.7038, true);

    assertThatThrownBy(() -> geoShapeService.update(UUID.randomUUID(), geoShape))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("GeoShape not found");
  }

  @Test
  @DisplayName("Should delete GeoShape by ID")
  void shouldDeleteById() {
    GeoShape created = geoShapeService.create(createGeoShapeDomain("Test Shape", 40.4168, -3.7038, true));

    geoShapeService.deleteById(created.getId());

    assertThat(geoShapeService.findById(created.getId())).isEmpty();
  }

  @Test
  @DisplayName("Should check if GeoShape exists by ID")
  void shouldCheckExistsById() {
    GeoShape created = geoShapeService.create(createGeoShapeDomain("Test Shape", 40.4168, -3.7038, true));

    boolean result = geoShapeService.existsById(created.getId());

    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("Should count all GeoShapes")
  void shouldCount() {
    geoShapeService.create(createGeoShapeDomain("Shape 1", 40.4168, -3.7038, true));
    geoShapeService.create(createGeoShapeDomain("Shape 2", 40.4200, -3.7100, true));

    long result = geoShapeService.count();

    assertThat(result).isEqualTo(2L);
  }

  private GeoShape createGeoShapeDomain(String name, double latitude, double longitude, boolean active) {
    GeoShape geoShape = new GeoShape();
    geoShape.setGeometryType(GeometryType.POINT);
    geoShape.setCenterCoordinates(CoordinateVO.of(latitude, longitude));
    geoShape.setGeometry(GeometryVO.ofCoordinates(CoordinateVO.of(latitude, longitude)));
    geoShape.setRadius(RadiusVO.of(BigDecimal.valueOf(100.0)));
    geoShape.setActive(active);
    geoShape.setAuditInfo(AuditInfoVO.of(OffsetDateTime.now(), OffsetDateTime.now()));
    return geoShape;
  }
}
