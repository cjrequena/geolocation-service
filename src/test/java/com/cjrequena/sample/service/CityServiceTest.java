package com.cjrequena.sample.service;

import com.cjrequena.sample.configuration.CacheConfigurationProperties;
import com.cjrequena.sample.domain.mapper.CityMapper;
import com.cjrequena.sample.domain.model.City;
import com.cjrequena.sample.persistence.entity.CityEntity;
import com.cjrequena.sample.persistence.entity.RegionEntity;
import com.cjrequena.sample.persistence.repository.CityRepository;
import com.cjrequena.sample.persistence.repository.RegionRepository;
import com.cjrequena.sample.persistence.repository.cache.CityCacheRedisHashOpsRepository;
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
@DisplayName("CityService Unit Tests")
class CityServiceTest {

  @Mock
  private CityRepository cityRepository;

  @Mock
  private RegionRepository regionRepository;

  @Mock
  private CityCacheRedisHashOpsRepository cityCacheRedisHashOpsRepository;

  @Mock
  private CacheConfigurationProperties cacheConfigurationProperties;
  
  @Mock
  private CityMapper cityMapper;

  @InjectMocks
  private CityService cityService;

  private City cityDomain;
  private CityEntity cityEntity;
  private UUID cityId;
  private UUID regionId;

  @BeforeEach
  void setUp() {
    cityId = UUID.randomUUID();
    regionId = UUID.randomUUID();

    cityDomain = new City();
    cityDomain.setId(cityId);
    cityDomain.setName("Barcelona");
    cityDomain.setRegionId(regionId);

    cityEntity = new CityEntity();
    cityEntity.setId(cityId);
    cityEntity.setName("Barcelona");
  }

  @Test
  @DisplayName("Should create city successfully")
  void shouldCreateCity() {
    when(cityMapper.toEntity(cityDomain)).thenReturn(cityEntity);
    when(cityRepository.saveAndFlush(cityEntity)).thenReturn(cityEntity);
    when(cityMapper.toDomain(cityEntity)).thenReturn(cityDomain);
    when(regionRepository.findById(regionId)).thenReturn(Optional.of(new RegionEntity()));

    City result = cityService.create(cityDomain);

    assertThat(result).isNotNull();
    verify(cityRepository).saveAndFlush(cityEntity);
  }

  @Test
  @DisplayName("Should find city by ID")
  void shouldFindById() {
    when(cityRepository.findById(cityId)).thenReturn(Optional.of(cityEntity));
    when(cityMapper.toDomain(cityEntity)).thenReturn(cityDomain);

    City result = cityService.findById(cityId);
    assertThat(result).isNotNull();
  }


  @Test
  @DisplayName("Should update city successfully")
  void shouldUpdateCity() {
    when(cityRepository.findById(cityId)).thenReturn(Optional.of(cityEntity));
    when(cityMapper.toEntity(cityDomain)).thenReturn(cityEntity);
    when(cityRepository.save(cityEntity)).thenReturn(cityEntity);
    when(cityMapper.toDomain(cityEntity)).thenReturn(cityDomain);

    City result = cityService.update(cityId, cityDomain);

    assertThat(result).isNotNull();
    verify(cityRepository).save(cityEntity);
  }

  @Test
  @DisplayName("Should delete city by ID")
  void shouldDeleteById() {
    when(cityRepository.existsById(cityId)).thenReturn(true);

    cityService.deleteById(cityId);

    verify(cityRepository).deleteById(cityId);
  }

  @Test
  @DisplayName("Should count all cities")
  void shouldCount() {
    when(cityRepository.count()).thenReturn(5L);

    long result = cityService.count();

    assertThat(result).isEqualTo(5L);
  }
}
