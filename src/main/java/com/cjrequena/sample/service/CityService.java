package com.cjrequena.sample.service;

import com.cjrequena.sample.configuration.CacheConfigurationProperties;
import com.cjrequena.sample.domain.exception.*;
import com.cjrequena.sample.domain.mapper.CityMapper;
import com.cjrequena.sample.domain.model.City;
import com.cjrequena.sample.persistence.entity.CityEntity;
import com.cjrequena.sample.persistence.repository.CityRepository;
import com.cjrequena.sample.persistence.repository.GeoShapeRepository;
import com.cjrequena.sample.persistence.repository.RegionRepository;
import com.cjrequena.sample.persistence.repository.cache.CityCacheRedisHashOpsRepository;
import com.cjrequena.sample.service.base.BaseService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
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
public class CityService extends BaseService<CityEntity, City> {

  private final CityRepository cityRepository;
  private final RegionRepository regionRepository;
  private final GeoShapeRepository geoGeoShapeRepository;
  private final CityCacheRedisHashOpsRepository cityCacheRedisHashOpsRepository;
  private final CacheConfigurationProperties cacheConfigurationProperties;
  private final CityMapper cityMapper;

  // ================================================================
  // BaseService Implementation
  // ================================================================

  @Override
  protected JpaRepository<CityEntity, ?> getRepository() {
    return cityRepository;
  }

  @Override
  protected JpaSpecificationExecutor<CityEntity> getSpecificationExecutor() {
    return cityRepository;
  }

  @Override
  protected Function<CityEntity, City> getEntityToDomainMapper() {
    return cityMapper::toDomain;
  }

  @Override
  protected Class<CityEntity> getEntityClass() {
    return CityEntity.class;
  }

  // ================================================================
  // Cache Initialization
  // ================================================================
  @PostConstruct
  public void loadUpCache() {
    if (cacheConfigurationProperties.isFullLoadEnabled()) {
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

    if (city.getRegionId() != null) {
      regionRepository
        .findById(city.getRegionId())
        .orElseThrow(() -> new RegionNotFoundException("Region not found with ID: %s".formatted(city.getRegionId())));
    } else {
      throw new RegionRequiredException("Region ID is required for creating a zone");
    }

    if (city.getGeoShapeId() != null) {
      geoGeoShapeRepository
        .findById(city.getGeoShapeId())
        .orElseThrow(() -> new GeoShapeNotFoundException("GeoShape not found with ID: %s".formatted(city.getGeoShapeId())));
    }

    try {
      CityEntity entity = cityMapper.toEntity(city);
      CityEntity savedEntity = cityRepository.saveAndFlush(entity);
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
    } catch (DataIntegrityViolationException ex) {
      final String message = String.format("Unique constraint violation while creating city: %s", city.getName());
      log.warn(message);
      throw new UniqueConstraintException(message, ex);
    }
  }

  public City findById(UUID id) {
    log.debug("Finding city by ID: {}", id);

    // Try cache first (cache-aside pattern)
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        Optional<City> cachedCity = cityCacheRedisHashOpsRepository.retrieveById(id);
        if (cachedCity.isPresent()) {
          log.debug("City found in cache: {}", id);
          return cachedCity.get();
        }
        log.debug("City not found in cache, querying database: {}", id);
      } catch (Exception e) {
        log.warn("Cache retrieval failed for city: {}, falling back to database", id, e);
      }
    }

    // Cache miss or disabled - query database
    City city = cityRepository
      .findById(id)
      .map(cityMapper::toDomain)
      .orElseThrow(() -> new CityNotFoundException("City not found with ID: %s".formatted(id)));

    // Update cache on successful database hit
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        cityCacheRedisHashOpsRepository.save(city);
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
    List<City> cities = cityRepository
      .findAll()
      .stream()
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

  /**
   * Finds all cities with optional RSQL filtering, sorting, and pagination.
   *
   * <p>This method does NOT use cache and always queries the database to ensure
   * accurate filtering and sorting results.</p>
   *
   * @param filters RSQL filter expression (e.g., "active==true;name=like='United'")
   * @param offset the offset for pagination (0-based)
   * @param limit the maximum number of results to return
   * @param sort the sort expression (e.g., "name,asc" or "name,desc;population,desc")
   * @return list of cities matching the criteria
   */
  public List<City> findAll(String filters, Integer offset, Integer limit, String sort) {
    return super.findAllWithFiltersAndSort(filters, offset, limit, sort);
  }

  @Transactional
  public City update(UUID id, City city) {
    log.debug("Updating city with ID: {}", id);

    if (city.getGeoShapeId() != null) {
      geoGeoShapeRepository
        .findById(city.getGeoShapeId())
        .orElseThrow(() -> new GeoShapeNotFoundException("GeoShape not found with ID: %s".formatted(city.getGeoShapeId())));
    }

    CityEntity existingEntity = cityRepository
      .findById(id)
      .orElseThrow(() -> new CityNotFoundException("City not found with ID: %s".formatted(id)));

    try {
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
        } catch (Exception ex) {
          log.warn("Failed to update cache for city: {}", updatedCity.getId(), ex);
        }
      }

      log.info("City updated with ID: {}", savedEntity.getId());
      return updatedCity;
    } catch (DataIntegrityViolationException ex) {
      final String message = String.format("Unique constraint violation while creating city: %s", city.getName());
      log.warn(message);
      throw new UniqueConstraintException(message, ex);
    }
  }

  @Transactional
  public void deleteById(UUID id) {
    log.debug("Deleting city with ID: {}", id);
    if (!cityRepository.existsById(id)) {
      throw new CityNotFoundException("City not found with ID: %s".formatted(id));
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

  public long count() {
    return cityRepository.count();
  }

}
