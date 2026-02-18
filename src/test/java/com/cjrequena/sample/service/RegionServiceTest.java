package com.cjrequena.sample.service;

import com.cjrequena.sample.configuration.CacheConfigurationProperties;
import com.cjrequena.sample.domain.mapper.RegionMapper;
import com.cjrequena.sample.domain.model.Region;
import com.cjrequena.sample.persistence.entity.RegionEntity;
import com.cjrequena.sample.persistence.repository.RegionRepository;
import com.cjrequena.sample.persistence.repository.cache.RegionCacheRedisHashOpsRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RegionService}.
 *
 * @author cjrequena
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegionService Unit Tests")
class RegionServiceTest {

  @Mock
  private RegionRepository regionRepository;

  @Mock
  private RegionCacheRedisHashOpsRepository regionCacheRedisHashOpsRepository;

  @Mock
  private CacheConfigurationProperties cacheConfigurationProperties;

  @Mock
  private RegionMapper regionMapper;

  @InjectMocks
  private RegionService regionService;

  private Region regionDomain;
  private RegionEntity regionEntity;
  private UUID regionId;
  private UUID countryId;

  @BeforeEach
  void setUp() {
    regionId = UUID.randomUUID();
    countryId = UUID.randomUUID();

    regionDomain = new Region();
    regionDomain.setId(regionId);
    regionDomain.setName("Catalonia");
    regionDomain.setCountryId(countryId);

    regionEntity = new RegionEntity();
    regionEntity.setId(regionId);
    regionEntity.setName("Catalonia");
  }

  @Test
  @DisplayName("Should create region successfully")
  void shouldCreateRegion() {
    when(regionMapper.toEntity(regionDomain)).thenReturn(regionEntity);
    when(regionRepository.save(regionEntity)).thenReturn(regionEntity);
    when(regionMapper.toDomain(regionEntity)).thenReturn(regionDomain);

    Region result = regionService.create(regionDomain);

    assertThat(result).isNotNull();
    verify(regionRepository).save(regionEntity);
  }

  @Test
  @DisplayName("Should find region by ID")
  void shouldFindById() {
    when(regionRepository.findById(regionId)).thenReturn(Optional.of(regionEntity));
    when(regionMapper.toDomain(regionEntity)).thenReturn(regionDomain);

    Region result = regionService.findById(regionId);
    assertThat(result).isNotNull();
  }

//  @Test
//  @DisplayName("Should find regions by country ID")
//  void shouldFindByCountryId() {
//    List<RegionEntity> entities = Arrays.asList(regionEntity);
//    when(regionRepository.findByCountryId(countryId)).thenReturn(entities);
//    when(regionMapper.toDomain(any(RegionEntity.class))).thenReturn(regionDomain);
//
//    List<Region> result = regionService.findByCountryId(countryId);
//
//    assertThat(result).hasSize(1);
//  }

//  @Test
//  @DisplayName("Should find regions by region type")
//  void shouldFindByRegionType() {
//    List<RegionEntity> entities = Arrays.asList(regionEntity);
//    when(regionRepository.findByRegionType("AUTONOMOUS_COMMUNITY")).thenReturn(entities);
//    when(regionMapper.toDomain(any(RegionEntity.class))).thenReturn(regionDomain);
//
//    List<Region> result = regionService.findByRegionType("AUTONOMOUS_COMMUNITY");
//
//    assertThat(result).hasSize(1);
//  }

  @Test
  @DisplayName("Should update region successfully")
  void shouldUpdateRegion() {
    when(regionRepository.findById(regionId)).thenReturn(Optional.of(regionEntity));
    when(regionMapper.toEntity(regionDomain)).thenReturn(regionEntity);
    when(regionRepository.save(regionEntity)).thenReturn(regionEntity);
    when(regionMapper.toDomain(regionEntity)).thenReturn(regionDomain);

    Region result = regionService.update(regionId, regionDomain);

    assertThat(result).isNotNull();
    verify(regionRepository).save(regionEntity);
  }

  @Test
  @DisplayName("Should throw exception when updating non-existent region")
  void shouldThrowExceptionWhenUpdatingNonExistent() {
    when(regionRepository.findById(regionId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> regionService.update(regionId, regionDomain))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Region not found");
  }

  @Test
  @DisplayName("Should delete region by ID")
  void shouldDeleteById() {
    when(regionRepository.existsById(regionId)).thenReturn(true);

    regionService.deleteById(regionId);

    verify(regionRepository).deleteById(regionId);
  }

//  @Test
//  @DisplayName("Should check if region exists by country ID and name")
//  void shouldCheckExistsByCountryIdAndName() {
//    when(regionRepository.existsByCountryIdAndName(countryId, "Catalonia")).thenReturn(true);
//
//    boolean result = regionService.existsByCountryIdAndName(countryId, "Catalonia");
//
//    assertThat(result).isTrue();
//  }

  @Test
  @DisplayName("Should count all regions")
  void shouldCount() {
    when(regionRepository.count()).thenReturn(5L);

    long result = regionService.count();

    assertThat(result).isEqualTo(5L);
  }
}
