package com.cjrequena.sample.service;

import com.cjrequena.sample.configuration.CacheConfigurationProperties;
import com.cjrequena.sample.domain.mapper.CityMapper;
import com.cjrequena.sample.domain.model.aggregate.City;
import com.cjrequena.sample.persistence.entity.CityEntity;
import com.cjrequena.sample.persistence.repository.CityRepository;
import com.cjrequena.sample.persistence.repository.cache.CityCacheRedisHashOpsRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for City aggregate operations.
 *
 * @author cjrequena
 */
@Log4j2
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CityService {

  private final CityRepository cityRepository;
  private final CityCacheRedisHashOpsRepository cityCacheRedisHashOpsRepository;
  private final CacheConfigurationProperties cacheConfigurationProperties;
  private final CityMapper cityMapper;

  @PostConstruct
  public void loadUpCache() {
    if(cacheConfigurationProperties.isFullLoadEnabled()) {
      List<City> cities = this.cityMapper.toDomainList(cityRepository.findAll());
      this.cityCacheRedisHashOpsRepository.load(cities);
      this.cityCacheRedisHashOpsRepository.retrieve();
    }
  }

  // ================================================================
  // CRUD Standard Operations
  // ================================================================

  @Transactional
  public City create(City city) {
    log.debug("Creating city: {}", city.getName());
    CityEntity entity = cityMapper.toEntity(city);
    CityEntity savedEntity = cityRepository.save(entity);
    City createdCity = cityMapper.toDomain(savedEntity);

    // Update cache
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        cityCacheRedisHashOpsRepository.save(createdCity);
        log.debug("City cached with ID: {}", createdCity.getId());
      } catch (Exception e) {
        log.warn("Failed to cache city on create: {}", createdCity.getId(), e);
      }
    }

    log.info("City created with ID: {}", savedEntity.getId());
    return createdCity;
  }

  public Optional<City> findById(UUID id) {
    log.debug("Finding city by ID: {}", id);

    // Try cache first (cache-aside pattern)
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        Optional<City> cachedCity = cityCacheRedisHashOpsRepository.retrieveById(id);
        if (cachedCity.isPresent()) {
          log.debug("City found in cache: {}", id);
          return cachedCity;
        }
        log.debug("City not found in cache, querying database: {}", id);
      } catch (Exception e) {
        log.warn("Cache retrieval failed for city: {}, falling back to database", id, e);
      }
    }

    // Cache miss or disabled - query database
    Optional<City> city = cityRepository.findById(id).map(cityMapper::toDomain);

    // Update cache on successful database hit
    if (cacheConfigurationProperties.isCacheEnabled() && city.isPresent()) {
      try {
        cityCacheRedisHashOpsRepository.save(city.get());
        log.debug("City cached after database query: {}", id);
      } catch (Exception e) {
        log.warn("Failed to cache city after database query: {}", id, e);
      }
    }

    return city;
  }

  public List<City> findAll() {
    log.debug("Finding all cities");

    // Try cache first for full list retrieval
    if (cacheConfigurationProperties.isCacheEnabled() && !cityCacheRedisHashOpsRepository.isEmpty()) {
      try {
        List<City> cachedCities = cityCacheRedisHashOpsRepository.retrieve();
        if (!cachedCities.isEmpty()) {
          log.debug("Retrieved {} cities from cache", cachedCities.size());
          return cachedCities;
        }
      } catch (Exception e) {
        log.warn("Cache retrieval failed for all cities, falling back to database", e);
      }
    }

    // Cache miss or disabled - query database
    List<City> cities = cityRepository.findAll().stream()
      .map(cityMapper::toDomain)
      .collect(Collectors.toList());

    // Update cache with full list
    if (cacheConfigurationProperties.isCacheEnabled() && !cities.isEmpty()) {
      try {
        cityCacheRedisHashOpsRepository.saveAll(cities);
        log.debug("Cached {} cities after database query", cities.size());
      } catch (Exception e) {
        log.warn("Failed to cache cities after database query", e);
      }
    }

    return cities;
  }

  @Transactional
  public City update(UUID id, City city) {
    log.debug("Updating city with ID: {}", id);
    CityEntity existingEntity = cityRepository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("City not found with ID: " + id));

    CityEntity updatedEntity = cityMapper.toEntity(city);
    updatedEntity.setId(existingEntity.getId());
    updatedEntity.setCreatedAt(existingEntity.getCreatedAt());

    CityEntity savedEntity = cityRepository.save(updatedEntity);
    City updatedCity = cityMapper.toDomain(savedEntity);

    // Update cache
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        cityCacheRedisHashOpsRepository.save(updatedCity);
        log.debug("City cache updated with ID: {}", updatedCity.getId());
      } catch (Exception e) {
        log.warn("Failed to update cache for city: {}", updatedCity.getId(), e);
      }
    }

    log.info("City updated with ID: {}", savedEntity.getId());
    return updatedCity;
  }

  @Transactional
  public void deleteById(UUID id) {
    log.debug("Deleting city with ID: {}", id);
    if (!cityRepository.existsById(id)) {
      throw new IllegalArgumentException("City not found with ID: " + id);
    }

    cityRepository.deleteById(id);

    // Remove from cache
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        cityCacheRedisHashOpsRepository.deleteById(id);
        log.debug("City removed from cache: {}", id);
      } catch (Exception e) {
        log.warn("Failed to remove city from cache: {}", id, e);
      }
    }

    log.info("City deleted with ID: {}", id);
  }

  public boolean existsById(UUID id) {
    // Check cache first for existence
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        if (cityCacheRedisHashOpsRepository.existsById(id)) {
          log.debug("City exists in cache: {}", id);
          return true;
        }
      } catch (Exception e) {
        log.warn("Cache existence check failed for city: {}, falling back to database", id, e);
      }
    }

    return cityRepository.existsById(id);
  }
  
//  @Transactional
//  public City create(City city) {
//    log.debug("Creating city: {}", city.getName());
//    CityEntity entity = cityMapper.toEntity(city);
//    CityEntity savedEntity = cityRepository.save(entity);
//    log.info("City created with ID: {}", savedEntity.getId());
//    return cityMapper.toDomain(savedEntity);
//  }
//
//  public Optional<City> findById(UUID id) {
//    log.debug("Finding city by ID: {}", id);
//    return cityRepository.findById(id).map(cityMapper::toDomain);
//  }
//
//  public List<City> findAll() {
//    log.debug("Finding all cities");
//    return cityRepository.findAll().stream()
//      .map(cityMapper::toDomain)
//      .collect(Collectors.toList());
//  }

  public List<City> findAllActive() {
    log.debug("Finding all active cities");
    return cityRepository.findAllByActiveTrue().stream()
      .map(cityMapper::toDomain)
      .collect(Collectors.toList());
  }

  public Page<City> findByActive(Boolean active, Pageable pageable) {
    log.debug("Finding cities by active status: {}", active);
    return cityRepository.findByActive(active, pageable).map(cityMapper::toDomain);
  }

  public List<City> findByRegionId(UUID regionId) {
    log.debug("Finding cities by region ID: {}", regionId);
    return cityRepository.findByRegionId(regionId).stream()
      .map(cityMapper::toDomain)
      .collect(Collectors.toList());
  }

  public List<City> findActiveByRegionId(UUID regionId) {
    log.debug("Finding active cities by region ID: {}", regionId);
    return cityRepository.findByRegionIdAndActiveTrue(regionId).stream()
      .map(cityMapper::toDomain)
      .collect(Collectors.toList());
  }

  public Page<City> findByRegionId(UUID regionId, Pageable pageable) {
    log.debug("Finding cities by region ID: {} with pagination", regionId);
    return cityRepository.findByRegionId(regionId, pageable).map(cityMapper::toDomain);
  }

  public Optional<City> findCapitalByRegionId(UUID regionId) {
    log.debug("Finding capital city by region ID: {}", regionId);
    return cityRepository.findCapitalByRegionId(regionId).map(cityMapper::toDomain);
  }

  public List<City> findAllCapitals() {
    log.debug("Finding all capital cities");
    return cityRepository.findAllByCapitalTrue().stream()
      .map(cityMapper::toDomain)
      .collect(Collectors.toList());
  }

  public List<City> findByTimeZone(String timeZone) {
    log.debug("Finding cities by timezone: {}", timeZone);
    return cityRepository.findByTimeZone(timeZone).stream()
      .map(cityMapper::toDomain)
      .collect(Collectors.toList());
  }

  public Optional<City> findByRegionIdAndName(UUID regionId, String name) {
    log.debug("Finding city by region ID: {} and name: {}", regionId, name);
    return cityRepository.findByRegionIdAndName(regionId, name).map(cityMapper::toDomain);
  }

  public List<City> findByNameContaining(String namePart) {
    log.debug("Finding cities by name containing: {}", namePart);
    return cityRepository.findByNameContainingIgnoreCase(namePart).stream()
      .map(cityMapper::toDomain)
      .collect(Collectors.toList());
  }

  public List<City> findByRegionIdAndPopulationGreaterThan(UUID regionId, Long minPopulation) {
    log.debug("Finding cities in region {} with population > {}", regionId, minPopulation);
    return cityRepository.findByRegionIdAndPopulationGreaterThan(regionId, minPopulation).stream()
      .map(cityMapper::toDomain)
      .collect(Collectors.toList());
  }

  public Page<City> findByRegionIdOrderByPopulationDesc(UUID regionId, Pageable pageable) {
    log.debug("Finding top cities in region {} by population", regionId);
    return cityRepository.findByRegionIdOrderByPopulationDesc(regionId, pageable)
      .map(cityMapper::toDomain);
  }

  public Page<City> findAllOrderByPopulationDesc(Pageable pageable) {
    log.debug("Finding all cities ordered by population");
    return cityRepository.findAllByOrderByPopulationDesc(pageable).map(cityMapper::toDomain);
  }

  public List<City> findByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end) {
    log.debug("Finding cities created between {} and {}", start, end);
    return cityRepository.findByCreatedAtBetween(start, end).stream()
      .map(cityMapper::toDomain)
      .collect(Collectors.toList());
  }

//  @Transactional
//  public City update(UUID id, City city) {
//    log.debug("Updating city with ID: {}", id);
//    CityEntity existingEntity = cityRepository.findById(id)
//      .orElseThrow(() -> new IllegalArgumentException("City not found with ID: " + id));
//
//    CityEntity updatedEntity = cityMapper.toEntity(city);
//    updatedEntity.setId(existingEntity.getId());
//    updatedEntity.setCreatedAt(existingEntity.getCreatedAt());
//
//    CityEntity savedEntity = cityRepository.save(updatedEntity);
//    log.info("City updated with ID: {}", savedEntity.getId());
//    return cityMapper.toDomain(savedEntity);
//  }

//  @Transactional
//  public void deleteById(UUID id) {
//    log.debug("Deleting city with ID: {}", id);
//    if (!cityRepository.existsById(id)) {
//      throw new IllegalArgumentException("City not found with ID: " + id);
//    }
//    cityRepository.deleteById(id);
//    log.info("City deleted with ID: {}", id);
//  }
//
//  public boolean existsById(UUID id) {
//    return cityRepository.existsById(id);
//  }

  public boolean existsByRegionIdAndName(UUID regionId, String name) {
    return cityRepository.existsByRegionIdAndName(regionId, name);
  }

  public long count() {
    return cityRepository.count();
  }
}
