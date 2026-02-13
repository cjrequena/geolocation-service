package com.cjrequena.sample.service;

import com.cjrequena.sample.configuration.CacheConfigurationProperties;
import com.cjrequena.sample.domain.mapper.ZoneMapper;
import com.cjrequena.sample.domain.model.Zone;
import com.cjrequena.sample.persistence.entity.ZoneEntity;
import com.cjrequena.sample.persistence.repository.ZoneRepository;
import com.cjrequena.sample.persistence.repository.cache.ZoneCacheRedisHashOpsRepository;
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
 * Unit tests for {@link ZoneService}.
 *
 * @author cjrequena
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ZoneService Unit Tests")
class ZoneServiceTest {

  @Mock
  private ZoneRepository zoneRepository;

  @Mock
  private ZoneCacheRedisHashOpsRepository zoneCacheRedisHashOpsRepository;

  @Mock
  private CacheConfigurationProperties cacheConfigurationProperties;

  @Mock
  private ZoneMapper zoneMapper;

  @InjectMocks
  private ZoneService zoneService;

  private Zone zoneDomain;
  private ZoneEntity zoneEntity;
  private UUID zoneId;
  private UUID areaId;

  @BeforeEach
  void setUp() {
    zoneId = UUID.randomUUID();
    areaId = UUID.randomUUID();

    zoneDomain = new Zone();
    zoneDomain.setId(zoneId);
    zoneDomain.setName("Downtown");
    zoneDomain.setAreaId(areaId);

    zoneEntity = new ZoneEntity();
    zoneEntity.setId(zoneId);
    zoneEntity.setName("Downtown");
  }

  @Test
  @DisplayName("Should create zone successfully")
  void shouldCreateZone() {
    when(zoneMapper.toEntity(zoneDomain)).thenReturn(zoneEntity);
    when(zoneRepository.save(zoneEntity)).thenReturn(zoneEntity);
    when(zoneMapper.toDomain(zoneEntity)).thenReturn(zoneDomain);

    Zone result = zoneService.create(zoneDomain);

    assertThat(result).isNotNull();
    verify(zoneRepository).save(zoneEntity);
  }

  @Test
  @DisplayName("Should find zone by ID")
  void shouldFindById() {
    when(zoneRepository.findById(zoneId)).thenReturn(Optional.of(zoneEntity));
    when(zoneMapper.toDomain(zoneEntity)).thenReturn(zoneDomain);

    Optional<Zone> result = zoneService.findById(zoneId);

    assertThat(result).isPresent();
  }

  @Test
  @DisplayName("Should find zones by area ID")
  void shouldFindByAreaId() {
    List<ZoneEntity> entities = Arrays.asList(zoneEntity);
    when(zoneRepository.findByAreaId(areaId)).thenReturn(entities);
    when(zoneMapper.toDomain(any(ZoneEntity.class))).thenReturn(zoneDomain);

    List<Zone> result = zoneService.findByAreaId(areaId);

    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("Should find zones by zone type")
  void shouldFindByZoneType() {
    List<ZoneEntity> entities = Arrays.asList(zoneEntity);
    when(zoneRepository.findByZoneType("RESIDENTIAL")).thenReturn(entities);
    when(zoneMapper.toDomain(any(ZoneEntity.class))).thenReturn(zoneDomain);

    List<Zone> result = zoneService.findByZoneType("RESIDENTIAL");

    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("Should find zones by postal code")
  void shouldFindByPostalCode() {
    List<ZoneEntity> entities = Arrays.asList(zoneEntity);
    when(zoneRepository.findByPostalCode("28001")).thenReturn(entities);
    when(zoneMapper.toDomain(any(ZoneEntity.class))).thenReturn(zoneDomain);

    List<Zone> result = zoneService.findByPostalCode("28001");

    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("Should update zone successfully")
  void shouldUpdateZone() {
    when(zoneRepository.findById(zoneId)).thenReturn(Optional.of(zoneEntity));
    when(zoneMapper.toEntity(zoneDomain)).thenReturn(zoneEntity);
    when(zoneRepository.save(zoneEntity)).thenReturn(zoneEntity);
    when(zoneMapper.toDomain(zoneEntity)).thenReturn(zoneDomain);

    Zone result = zoneService.update(zoneId, zoneDomain);

    assertThat(result).isNotNull();
    verify(zoneRepository).save(zoneEntity);
  }

  @Test
  @DisplayName("Should throw exception when updating non-existent zone")
  void shouldThrowExceptionWhenUpdatingNonExistent() {
    when(zoneRepository.findById(zoneId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> zoneService.update(zoneId, zoneDomain))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Zone not found");
  }

  @Test
  @DisplayName("Should delete zone by ID")
  void shouldDeleteById() {
    when(zoneRepository.existsById(zoneId)).thenReturn(true);

    zoneService.deleteById(zoneId);

    verify(zoneRepository).deleteById(zoneId);
  }

  @Test
  @DisplayName("Should check if zone exists by area ID and name")
  void shouldCheckExistsByAreaIdAndName() {
    when(zoneRepository.existsByAreaIdAndName(areaId, "Downtown")).thenReturn(true);

    boolean result = zoneService.existsByAreaIdAndName(areaId, "Downtown");

    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("Should count all zones")
  void shouldCount() {
    when(zoneRepository.count()).thenReturn(5L);

    long result = zoneService.count();

    assertThat(result).isEqualTo(5L);
  }
}
