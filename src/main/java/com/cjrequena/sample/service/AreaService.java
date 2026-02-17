package com.cjrequena.sample.service;

import com.cjrequena.sample.configuration.CacheConfigurationProperties;
import com.cjrequena.sample.domain.exception.AreaNotFoundException;
import com.cjrequena.sample.domain.exception.CityRequiredException;
import com.cjrequena.sample.domain.exception.GeoShapeNotFoundException;
import com.cjrequena.sample.domain.exception.UniqueConstraintException;
import com.cjrequena.sample.domain.mapper.AreaMapper;
import com.cjrequena.sample.domain.model.Area;
import com.cjrequena.sample.persistence.entity.AreaEntity;
import com.cjrequena.sample.persistence.repository.AreaRepository;
import com.cjrequena.sample.persistence.repository.CityRepository;
import com.cjrequena.sample.persistence.repository.GeoShapeRepository;
import com.cjrequena.sample.persistence.repository.cache.AreaCacheRedisHashOpsRepository;
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
 * Service layer for Area aggregate operations.
 *
 * @author cjrequena
 */
@Log4j2
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AreaService extends BaseService<AreaEntity, Area> {

  private final AreaRepository areaRepository;
  private final CityRepository cityRepository;
  private final GeoShapeRepository geoGeoShapeRepository;
  private final AreaCacheRedisHashOpsRepository areaCacheRedisHashOpsRepository;
  private final CacheConfigurationProperties cacheConfigurationProperties;
  private final AreaMapper areaMapper;

  // ================================================================
  // BaseService Implementation
  // ================================================================

  @Override
  protected JpaRepository<AreaEntity, ?> getRepository() {
    return areaRepository;
  }

  @Override
  protected JpaSpecificationExecutor<AreaEntity> getSpecificationExecutor() {
    return areaRepository;
  }

  @Override
  protected Function<AreaEntity, Area> getEntityToDomainMapper() {
    return areaMapper::toDomain;
  }

  @Override
  protected Class<AreaEntity> getEntityClass() {
    return AreaEntity.class;
  }

  // ================================================================
  // Cache Initialization
  // ================================================================

  @PostConstruct
  public void loadUpCache() {
    if (cacheConfigurationProperties.isFullLoadEnabled()) {
      List<Area> areas = this.areaMapper.toDomainList(areaRepository.findAll());
      this.areaCacheRedisHashOpsRepository.load(areas);
      this.areaCacheRedisHashOpsRepository.retrieve();
    }
  }

  // ================================================================
  // CRUD Standard Operations
  // ================================================================

  @Transactional
  public Area create(Area area) {
    log.debug("Creating area: {}", area.getName());

    if (area.getCityId() != null) {
      cityRepository
        .findById(area.getCityId())
        .orElseThrow(() -> new AreaNotFoundException("City not found with ID: %s".formatted(area.getCityId())));
    } else {
      throw new CityRequiredException("City ID is required for creating a zone");
    }

    if (area.getGeoShapeId() != null) {
      geoGeoShapeRepository
        .findById(area.getGeoShapeId())
        .orElseThrow(() -> new GeoShapeNotFoundException("GeoShape not found with ID: %s".formatted(area.getGeoShapeId())));
    }

    try {
      AreaEntity entity = areaMapper.toEntity(area);
      AreaEntity savedEntity = areaRepository.saveAndFlush(entity);
      Area createdArea = areaMapper.toDomain(savedEntity);

      // Update cache
      if (cacheConfigurationProperties.isCacheEnabled()) {
        try {
          areaCacheRedisHashOpsRepository.save(createdArea);
          log.debug("Area cached with ID: {}", createdArea.getId());
        } catch (Exception e) {
          log.warn("Failed to cache area on create: {}", createdArea.getId(), e);
        }
      }

      log.info("Area created with ID: {}", savedEntity.getId());
      return createdArea;
    } catch (DataIntegrityViolationException ex) {
      final String message = String.format("Unique constraint violation while creating area: %s", area.getName());
      log.warn(message);
      throw new UniqueConstraintException(message, ex);
    }
  }

  public Optional<Area> findById(UUID id) {
    log.debug("Finding area by ID: {}", id);

    // Try cache first (cache-aside pattern)
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        Optional<Area> cachedArea = areaCacheRedisHashOpsRepository.retrieveById(id);
        if (cachedArea.isPresent()) {
          log.debug("Area found in cache: {}", id);
          return cachedArea;
        }
        log.debug("Area not found in cache, querying database: {}", id);
      } catch (Exception e) {
        log.warn("Cache retrieval failed for area: {}, falling back to database", id, e);
      }
    }

    // Cache miss or disabled - query database
    Area area = areaRepository
      .findById(id)
      .map(areaMapper::toDomain)
      .orElseThrow(() -> new AreaNotFoundException("Area not found with ID: %s".formatted(id)));

    // Update cache on successful database hit
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        areaCacheRedisHashOpsRepository.save(area);
        log.debug("Area cached after database query: {}", id);
      } catch (Exception e) {
        log.warn("Failed to cache area after database query: {}", id, e);
      }
    }

    return Optional.of(area);
  }

  public List<Area> findAll() {
    log.debug("Finding all areas");

    // Try cache first for full list retrieval
    if (cacheConfigurationProperties.isCacheEnabled() && !areaCacheRedisHashOpsRepository.isEmpty()) {
      try {
        List<Area> cachedAreas = areaCacheRedisHashOpsRepository.retrieve();
        if (!cachedAreas.isEmpty()) {
          log.debug("Retrieved {} areas from cache", cachedAreas.size());
          return cachedAreas;
        }
      } catch (Exception e) {
        log.warn("Cache retrieval failed for all areas, falling back to database", e);
      }
    }

    // Cache miss or disabled - query database
    List<Area> areas = areaRepository
      .findAll()
      .stream()
      .map(areaMapper::toDomain)
      .collect(Collectors.toList());

    // Update cache with full list
    if (cacheConfigurationProperties.isCacheEnabled() && !areas.isEmpty()) {
      try {
        areaCacheRedisHashOpsRepository.saveAll(areas);
        log.debug("Cached {} areas after database query", areas.size());
      } catch (Exception e) {
        log.warn("Failed to cache areas after database query", e);
      }
    }

    return areas;
  }

  /**
   * Finds all areas with optional RSQL filtering, sorting, and pagination.
   *
   * <p>This method does NOT use cache and always queries the database to ensure
   * accurate filtering and sorting results.</p>
   *
   * @param filters RSQL filter expression (e.g., "active==true;postalCode==94102")
   * @param offset the offset for pagination (0-based)
   * @param limit the maximum number of results to return
   * @param sort the sort expression (e.g., "name,asc" or "name,desc;createdAt,asc")
   * @return list of areas matching the criteria
   */
  public List<Area> findAll(String filters, Integer offset, Integer limit, String sort) {
    return super.findAllWithFiltersAndSort(filters, offset, limit, sort);
  }

  @Transactional
  public Area update(UUID id, Area area) {
    log.debug("Updating area with ID: {}", id);

    if (area.getGeoShapeId() != null) {
      geoGeoShapeRepository
        .findById(area.getGeoShapeId())
        .orElseThrow(() -> new GeoShapeNotFoundException("GeoShape not found with ID: %s".formatted(area.getGeoShapeId())));
    }

    AreaEntity existingEntity = areaRepository
      .findById(id)
      .orElseThrow(() -> new AreaNotFoundException("Area not found with ID: %s".formatted(id)));

    try {
      AreaEntity updatedEntity = areaMapper.toEntity(area);
      updatedEntity.setId(existingEntity.getId());
      updatedEntity.setCreatedAt(existingEntity.getCreatedAt());

      AreaEntity savedEntity = areaRepository.saveAndFlush(updatedEntity);
      Area updatedArea = areaMapper.toDomain(savedEntity);

      // Update cache
      if (cacheConfigurationProperties.isCacheEnabled()) {
        try {
          areaCacheRedisHashOpsRepository.save(updatedArea);
          log.debug("Area cache updated with ID: {}", updatedArea.getId());
        } catch (Exception e) {
          log.warn("Failed to update cache for area: {}", updatedArea.getId(), e);
        }
      }

      log.info("Area updated with ID: {}", savedEntity.getId());
      return updatedArea;
    } catch (DataIntegrityViolationException ex) {
      final String message = String.format("Unique constraint violation while creating area: %s", area.getName());
      log.warn(message);
      throw new UniqueConstraintException(message, ex);
    }
  }

  @Transactional
  public void deleteById(UUID id) {
    log.debug("Deleting area with ID: {}", id);
    if (!areaRepository.existsById(id)) {
      throw new AreaNotFoundException("Area not found with ID: %s".formatted(id));
    }

    areaRepository.deleteById(id);

    // Remove from cache
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        areaCacheRedisHashOpsRepository.deleteById(id);
        log.debug("Area removed from cache: {}", id);
      } catch (Exception e) {
        log.warn("Failed to remove area from cache: {}", id, e);
      }
    }

    log.info("Area deleted with ID: {}", id);
  }

  public boolean existsById(UUID id) {
    // Check cache first for existence
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        if (areaCacheRedisHashOpsRepository.existsById(id)) {
          log.debug("Area exists in cache: {}", id);
          return true;
        }
      } catch (Exception e) {
        log.warn("Cache existence check failed for area: {}, falling back to database", id, e);
      }
    }

    return areaRepository.existsById(id);
  }

  public long count() {
    return areaRepository.count();
  }

  // ================================================================
  // Cache Management Operations
  // ================================================================

  /**
   * Manually refresh the cache from the database.
   * Useful for cache warming or recovery scenarios.
   */
  public void refreshCache() {
    if (!cacheConfigurationProperties.isCacheEnabled()) {
      log.warn("Cache is disabled, skipping refresh");
      return;
    }

    log.info("Refreshing area cache from database");
    try {
      List<Area> areas = areaRepository
        .findAll()
        .stream()
        .map(areaMapper::toDomain)
        .collect(Collectors.toList());

      areaCacheRedisHashOpsRepository.load(areas);
      log.info("Successfully refreshed cache with {} areas", areas.size());
    } catch (Exception e) {
      log.error("Failed to refresh area cache", e);
      throw new RuntimeException("Failed to refresh area cache", e);
    }
  }

  /**
   * Clear all areas from the cache.
   * Use with caution - this will force all subsequent reads to hit the database.
   */
  public void clearCache() {
    if (!cacheConfigurationProperties.isCacheEnabled()) {
      log.warn("Cache is disabled, skipping clear");
      return;
    }

    log.info("Clearing area cache");
    try {
      areaCacheRedisHashOpsRepository.clear();
      log.info("Successfully cleared area cache");
    } catch (Exception e) {
      log.error("Failed to clear area cache", e);
      throw new RuntimeException("Failed to clear area cache", e);
    }
  }

  /**
   * Get cache statistics.
   *
   * @return number of areas currently in cache
   */
  public long getCacheSize() {
    if (!cacheConfigurationProperties.isCacheEnabled()) {
      return 0L;
    }

    try {
      return areaCacheRedisHashOpsRepository.size();
    } catch (Exception e) {
      log.error("Failed to get cache size", e);
      return 0L;
    }
  }

  /**
   * Check if cache is empty.
   *
   * @return true if cache is empty or disabled
   */
  public boolean isCacheEmpty() {
    if (!cacheConfigurationProperties.isCacheEnabled()) {
      return true;
    }

    try {
      return areaCacheRedisHashOpsRepository.isEmpty();
    } catch (Exception e) {
      log.error("Failed to check if cache is empty", e);
      return true;
    }
  }

  /**
   * Warm up cache with active areas only.
   * More efficient than loading all areas when most queries are for active areas.
   */
  public void warmCacheWithActiveAreas() {
    if (!cacheConfigurationProperties.isCacheEnabled()) {
      log.warn("Cache is disabled, skipping warm-up");
      return;
    }

    log.info("Warming cache with active areas");
    try {
      List<Area> activeAreas = areaRepository.findAllByActiveTrue().stream()
        .map(areaMapper::toDomain)
        .collect(Collectors.toList());

      areaCacheRedisHashOpsRepository.saveAll(activeAreas);
      log.info("Successfully warmed cache with {} active areas", activeAreas.size());
    } catch (Exception e) {
      log.error("Failed to warm cache with active areas", e);
      throw new RuntimeException("Failed to warm cache with active areas", e);
    }
  }

  /**
   * Evict specific area from cache.
   * Useful for targeted cache invalidation.
   *
   * @param id the area ID to evict
   */
  public void evictFromCache(UUID id) {
    if (!cacheConfigurationProperties.isCacheEnabled()) {
      return;
    }

    log.debug("Evicting area from cache: {}", id);
    try {
      areaCacheRedisHashOpsRepository.deleteById(id);
      log.debug("Successfully evicted area from cache: {}", id);
    } catch (Exception e) {
      log.warn("Failed to evict area from cache: {}", id, e);
    }
  }

  /**
   * Batch evict multiple areas from cache.
   *
   * @param ids collection of area IDs to evict
   */
  public void evictMultipleFromCache(List<UUID> ids) {
    if (!cacheConfigurationProperties.isCacheEnabled() || ids == null || ids.isEmpty()) {
      return;
    }

    log.debug("Evicting {} areas from cache", ids.size());
    try {
      areaCacheRedisHashOpsRepository.deleteAll(ids);
      log.debug("Successfully evicted {} areas from cache", ids.size());
    } catch (Exception e) {
      log.warn("Failed to evict areas from cache", e);
    }
  }

}
