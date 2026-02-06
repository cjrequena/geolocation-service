package com.cjrequena.sample.service;

import com.cjrequena.sample.domain.mapper.GeoShapeMapper;
import com.cjrequena.sample.domain.model.aggregate.GeoShape;
import com.cjrequena.sample.domain.model.enums.GeometryType;
import com.cjrequena.sample.persistence.entity.GeoShapeEntity;
import com.cjrequena.sample.persistence.repository.GeoShapeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GeoShapeService}.
 *
 * @author cjrequena
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GeoShapeService Unit Tests")
class GeoShapeServiceTest {

  @Mock
  private GeoShapeRepository geoShapeRepository;

  @Mock
  private GeoShapeMapper geoShapeMapper;

  @InjectMocks
  private GeoShapeService geoShapeService;

  private GeoShape geoShapeDomain;
  private GeoShapeEntity geoShapeEntity;
  private UUID geoShapeId;
  private GeometryFactory geometryFactory;

  @BeforeEach
  void setUp() {
    geoShapeId = UUID.randomUUID();
    geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    geoShapeDomain = new GeoShape();
    geoShapeDomain.setId(geoShapeId);
    geoShapeDomain.setGeometryType(GeometryType.POLYGON);

    geoShapeEntity = new GeoShapeEntity();
    geoShapeEntity.setId(geoShapeId);
    geoShapeEntity.setGeometryType(GeometryType.POLYGON);
  }

  @Test
  @DisplayName("Should create GeoShape successfully")
  void shouldCreateGeoShape() {
    when(geoShapeMapper.toEntity(geoShapeDomain)).thenReturn(geoShapeEntity);
    when(geoShapeRepository.save(geoShapeEntity)).thenReturn(geoShapeEntity);
    when(geoShapeMapper.toDomain(geoShapeEntity)).thenReturn(geoShapeDomain);

    GeoShape result = geoShapeService.create(geoShapeDomain);

    assertThat(result).isNotNull();
    verify(geoShapeRepository).save(geoShapeEntity);
  }

  @Test
  @DisplayName("Should find GeoShape by ID")
  void shouldFindById() {
    when(geoShapeRepository.findById(geoShapeId)).thenReturn(Optional.of(geoShapeEntity));
    when(geoShapeMapper.toDomain(geoShapeEntity)).thenReturn(geoShapeDomain);

    Optional<GeoShape> result = geoShapeService.findById(geoShapeId);

    assertThat(result).isPresent();
  }

  @Test
  @DisplayName("Should find GeoShape by name")
  void shouldFindByName() {
    when(geoShapeRepository.findByName("Test Shape")).thenReturn(Optional.of(geoShapeEntity));
    when(geoShapeMapper.toDomain(geoShapeEntity)).thenReturn(geoShapeDomain);

    Optional<GeoShape> result = geoShapeService.findByName("Test Shape");

    assertThat(result).isPresent();
  }

  @Test
  @DisplayName("Should find all active GeoShapes")
  void shouldFindAllActive() {
    List<GeoShapeEntity> entities = Arrays.asList(geoShapeEntity);
    when(geoShapeRepository.findByActiveTrue()).thenReturn(entities);
    when(geoShapeMapper.toDomain(any(GeoShapeEntity.class))).thenReturn(geoShapeDomain);

    List<GeoShape> result = geoShapeService.findAllActive();

    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("Should find GeoShapes by name containing")
  void shouldFindByNameContaining() {
    List<GeoShapeEntity> entities = Arrays.asList(geoShapeEntity);
    when(geoShapeRepository.findByNameContainingIgnoreCase("test")).thenReturn(entities);
    when(geoShapeMapper.toDomain(any(GeoShapeEntity.class))).thenReturn(geoShapeDomain);

    List<GeoShape> result = geoShapeService.findByNameContaining("test");

    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("Should find GeoShapes containing point")
  void shouldFindContainingPoint() {
    Point point = geometryFactory.createPoint(new Coordinate(-3.7038, 40.4168));
    List<GeoShapeEntity> entities = Arrays.asList(geoShapeEntity);
    when(geoShapeRepository.findContainingPoint(point)).thenReturn(entities);
    when(geoShapeMapper.toDomain(any(GeoShapeEntity.class))).thenReturn(geoShapeDomain);

    List<GeoShape> result = geoShapeService.findContainingPoint(point);

    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("Should find GeoShapes within distance")
  void shouldFindWithinDistance() {
    String wkt = "POINT(-3.7038 40.4168)";
    List<GeoShapeEntity> entities = Arrays.asList(geoShapeEntity);
    when(geoShapeRepository.findWithinDistance(wkt, 1000.0)).thenReturn(entities);
    when(geoShapeMapper.toDomain(any(GeoShapeEntity.class))).thenReturn(geoShapeDomain);

    List<GeoShape> result = geoShapeService.findWithinDistance(wkt, 1000.0);

    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("Should update GeoShape successfully")
  void shouldUpdateGeoShape() {
    when(geoShapeRepository.findById(geoShapeId)).thenReturn(Optional.of(geoShapeEntity));
    when(geoShapeMapper.toEntity(geoShapeDomain)).thenReturn(geoShapeEntity);
    when(geoShapeRepository.save(geoShapeEntity)).thenReturn(geoShapeEntity);
    when(geoShapeMapper.toDomain(geoShapeEntity)).thenReturn(geoShapeDomain);

    GeoShape result = geoShapeService.update(geoShapeId, geoShapeDomain);

    assertThat(result).isNotNull();
    verify(geoShapeRepository).save(geoShapeEntity);
  }

  @Test
  @DisplayName("Should throw exception when updating non-existent GeoShape")
  void shouldThrowExceptionWhenUpdatingNonExistent() {
    when(geoShapeRepository.findById(geoShapeId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> geoShapeService.update(geoShapeId, geoShapeDomain))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("GeoShape not found");
  }

  @Test
  @DisplayName("Should delete GeoShape by ID")
  void shouldDeleteById() {
    when(geoShapeRepository.existsById(geoShapeId)).thenReturn(true);

    geoShapeService.deleteById(geoShapeId);

    verify(geoShapeRepository).deleteById(geoShapeId);
  }

  @Test
  @DisplayName("Should check if GeoShape exists by name")
  void shouldCheckExistsByName() {
    when(geoShapeRepository.existsByName("Test Shape")).thenReturn(true);

    boolean result = geoShapeService.existsByName("Test Shape");

    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("Should count all GeoShapes")
  void shouldCount() {
    when(geoShapeRepository.count()).thenReturn(5L);

    long result = geoShapeService.count();

    assertThat(result).isEqualTo(5L);
  }
}
