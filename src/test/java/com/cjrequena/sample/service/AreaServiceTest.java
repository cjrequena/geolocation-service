package com.cjrequena.sample.service;

import com.cjrequena.sample.configuration.CacheConfigurationProperties;
import com.cjrequena.sample.domain.mapper.AreaMapper;
import com.cjrequena.sample.domain.model.Area;
import com.cjrequena.sample.persistence.entity.AreaEntity;
import com.cjrequena.sample.persistence.entity.CityEntity;
import com.cjrequena.sample.persistence.repository.AreaRepository;
import com.cjrequena.sample.persistence.repository.CityRepository;
import com.cjrequena.sample.persistence.repository.GeoShapeRepository;
import com.cjrequena.sample.persistence.repository.cache.AreaCacheRedisHashOpsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AreaService Unit Tests")
class AreaServiceTest {

  @Mock
  private AreaRepository areaRepository;

  @Mock
  private CityRepository cityRepository;

  @Mock
  private GeoShapeRepository geoShapeRepository;

  @Mock
  private AreaCacheRedisHashOpsRepository areaCacheRedisHashOpsRepository;

  @Mock
  private CacheConfigurationProperties cacheConfigurationProperties;

  @Mock
  private AreaMapper areaMapper;

  @InjectMocks
  private AreaService areaService;

  private Area areaDomain;
  private AreaEntity areaEntity;
  private UUID areaId;
  private UUID cityId;

  @BeforeEach
  void setUp() {
    areaId = UUID.randomUUID();
    cityId = UUID.randomUUID();

    areaDomain = new Area();
    areaDomain.setId(areaId);
    areaDomain.setName("Eixample");
    areaDomain.setCityId(cityId);

    areaEntity = new AreaEntity();
    areaEntity.setId(areaId);
    areaEntity.setName("Eixample");
  }

  @Test
  @DisplayName("Should create area successfully")
  void shouldCreateArea() {
    when(areaMapper.toEntity(areaDomain)).thenReturn(areaEntity);
    when(areaRepository.saveAndFlush(areaEntity)).thenReturn(areaEntity);
    when(areaMapper.toDomain(areaEntity)).thenReturn(areaDomain);
    when(cityRepository.findById(cityId)).thenReturn(Optional.of(new CityEntity()));

    Area result = areaService.create(areaDomain);

    assertThat(result).isNotNull();
    verify(areaRepository).saveAndFlush(areaEntity);
  }

  @Test
  @DisplayName("Should find area by ID")
  void shouldFindById() {
    when(areaRepository.findById(areaId)).thenReturn(Optional.of(areaEntity));
    when(areaMapper.toDomain(areaEntity)).thenReturn(areaDomain);

    Area result = areaService.findById(areaId);
    assertThat(result).isNotNull();
  }

  @Test
  @DisplayName("Should update area successfully")
  void shouldUpdateArea() {
    when(areaRepository.findById(areaId)).thenReturn(Optional.of(areaEntity));
    when(areaMapper.toEntity(areaDomain)).thenReturn(areaEntity);
    when(areaRepository.saveAndFlush(areaEntity)).thenReturn(areaEntity);
    when(areaMapper.toDomain(areaEntity)).thenReturn(areaDomain);

    Area result = areaService.update(areaId, areaDomain);

    assertThat(result).isNotNull();
    verify(areaRepository).saveAndFlush(areaEntity);
  }

  @Test
  @DisplayName("Should delete area by ID")
  void shouldDeleteById() {
    when(areaRepository.existsById(areaId)).thenReturn(true);

    areaService.deleteById(areaId);

    verify(areaRepository).deleteById(areaId);
  }

  @Test
  @DisplayName("Should count all areas")
  void shouldCount() {
    when(areaRepository.count()).thenReturn(5L);

    long result = areaService.count();

    assertThat(result).isEqualTo(5L);
  }
}
