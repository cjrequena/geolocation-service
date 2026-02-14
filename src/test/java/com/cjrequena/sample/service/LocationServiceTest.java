package com.cjrequena.sample.service;

import com.cjrequena.sample.configuration.CacheConfigurationProperties;
import com.cjrequena.sample.domain.mapper.LocationMapper;
import com.cjrequena.sample.domain.model.Location;
import com.cjrequena.sample.persistence.entity.LocationEntity;
import com.cjrequena.sample.persistence.repository.LocationRepository;
import com.cjrequena.sample.persistence.repository.cache.LocationCacheRedisHashOpsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
 * Unit tests for {@link LocationService}.
 *
 * @author cjrequena
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LocationService Unit Tests")
class LocationServiceTest {

  @Mock
  private LocationRepository locationRepository;

  @Mock
  private LocationCacheRedisHashOpsRepository locationCacheRedisHashOpsRepository;

  @Mock
  private CacheConfigurationProperties cacheConfigurationProperties;

  @Mock
  private LocationMapper locationMapper;

  @InjectMocks
  private LocationService locationService;

  private Location locationDomain;
  private LocationEntity locationEntity;
  private UUID locationId;
  private UUID zoneId;

  @BeforeEach
  void setUp() {
    locationId = UUID.randomUUID();
    zoneId = UUID.randomUUID();

    locationDomain = new Location();
    locationDomain.setId(locationId);
    locationDomain.setAddress("123 Main St");
    locationDomain.setZoneId(zoneId);

    locationEntity = new LocationEntity();
    locationEntity.setId(locationId);
    locationEntity.setAddress("123 Main St");
  }

  @Test
  @DisplayName("Should create location successfully")
  void shouldCreateLocation() {
    when(locationMapper.toEntity(locationDomain)).thenReturn(locationEntity);
    when(locationRepository.save(locationEntity)).thenReturn(locationEntity);
    when(locationMapper.toDomain(locationEntity)).thenReturn(locationDomain);

    Location result = locationService.create(locationDomain);

    assertThat(result).isNotNull();
    verify(locationRepository).save(locationEntity);
  }

  @Test
  @DisplayName("Should find location by ID")
  void shouldFindById() {
    when(locationRepository.findById(locationId)).thenReturn(Optional.of(locationEntity));
    when(locationMapper.toDomain(locationEntity)).thenReturn(locationDomain);

    Optional<Location> result = locationService.findById(locationId);

    assertThat(result).isPresent();
  }

//  @Test
//  @DisplayName("Should find locations by zone ID")
//  void shouldFindByZoneId() {
//    List<LocationEntity> entities = Arrays.asList(locationEntity);
//    when(locationRepository.findByZoneId(zoneId)).thenReturn(entities);
//    when(locationMapper.toDomain(any(LocationEntity.class))).thenReturn(locationDomain);
//
//    List<Location> result = locationService.findByZoneId(zoneId);
//
//    assertThat(result).hasSize(1);
//  }

  @Test
  @DisplayName("Should find locations within radius")
  void shouldFindWithinRadius() {
    String wkt = "POINT(-3.7038 40.4168)";
    List<LocationEntity> entities = Arrays.asList(locationEntity);
    when(locationRepository.findWithinRadius(wkt, 1000.0)).thenReturn(entities);
    when(locationMapper.toDomain(any(LocationEntity.class))).thenReturn(locationDomain);

    List<Location> result = locationService.findWithinRadius(wkt, 1000.0);

    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("Should find locations within polygon")
  void shouldFindWithinPolygon() {
    String wkt = "POLYGON((-3.72 40.41, -3.68 40.41, -3.68 40.42, -3.72 40.42, -3.72 40.41))";
    List<LocationEntity> entities = Arrays.asList(locationEntity);
    when(locationRepository.findWithinPolygon(wkt)).thenReturn(entities);
    when(locationMapper.toDomain(any(LocationEntity.class))).thenReturn(locationDomain);

    List<Location> result = locationService.findWithinPolygon(wkt);

    assertThat(result).hasSize(1);
  }

//  @Test
//  @DisplayName("Should find locations by postal code")
//  void shouldFindByPostalCode() {
//    List<LocationEntity> entities = Arrays.asList(locationEntity);
//    when(locationRepository.findByPostalCode("28001")).thenReturn(entities);
//    when(locationMapper.toDomain(any(LocationEntity.class))).thenReturn(locationDomain);
//
//    List<Location> result = locationService.findByPostalCode("28001");
//
//    assertThat(result).hasSize(1);
//  }
//
//  @Test
//  @DisplayName("Should find locations by address containing")
//  void shouldFindByAddressContaining() {
//    List<LocationEntity> entities = Arrays.asList(locationEntity);
//    when(locationRepository.findByAddressContainingIgnoreCase("Main")).thenReturn(entities);
//    when(locationMapper.toDomain(any(LocationEntity.class))).thenReturn(locationDomain);
//
//    List<Location> result = locationService.findByAddressContaining("Main");
//
//    assertThat(result).hasSize(1);
//  }
//
//  @Test
//  @DisplayName("Should find locations by altitude greater than")
//  void shouldFindByAltitudeGreaterThan() {
//    BigDecimal minAltitude = new BigDecimal("100.0");
//    List<LocationEntity> entities = Arrays.asList(locationEntity);
//    when(locationRepository.findByAltitudeGreaterThan(minAltitude)).thenReturn(entities);
//    when(locationMapper.toDomain(any(LocationEntity.class))).thenReturn(locationDomain);
//
//    List<Location> result = locationService.findByAltitudeGreaterThan(minAltitude);
//
//    assertThat(result).hasSize(1);
//  }

  @Test
  @DisplayName("Should update location successfully")
  void shouldUpdateLocation() {
    when(locationRepository.findById(locationId)).thenReturn(Optional.of(locationEntity));
    when(locationMapper.toEntity(locationDomain)).thenReturn(locationEntity);
    when(locationRepository.save(locationEntity)).thenReturn(locationEntity);
    when(locationMapper.toDomain(locationEntity)).thenReturn(locationDomain);

    Location result = locationService.update(locationId, locationDomain);

    assertThat(result).isNotNull();
    verify(locationRepository).save(locationEntity);
  }

  @Test
  @DisplayName("Should throw exception when updating non-existent location")
  void shouldThrowExceptionWhenUpdatingNonExistent() {
    when(locationRepository.findById(locationId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> locationService.update(locationId, locationDomain))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Location not found");
  }

  @Test
  @DisplayName("Should delete location by ID")
  void shouldDeleteById() {
    when(locationRepository.existsById(locationId)).thenReturn(true);

    locationService.deleteById(locationId);

    verify(locationRepository).deleteById(locationId);
  }

  @Test
  @DisplayName("Should check if active location exists near point")
  void shouldCheckExistsActiveNearPoint() {
    String wkt = "POINT(-3.7038 40.4168)";
    when(locationRepository.existsActiveNearPoint(wkt, 10.0)).thenReturn(true);

    boolean result = locationService.existsActiveNearPoint(wkt, 10.0);

    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("Should count all locations")
  void shouldCount() {
    when(locationRepository.count()).thenReturn(5L);

    long result = locationService.count();

    assertThat(result).isEqualTo(5L);
  }
}
