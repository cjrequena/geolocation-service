package com.cjrequena.sample.service;

import com.cjrequena.sample.configuration.CacheConfigurationProperties;
import com.cjrequena.sample.domain.mapper.AreaMapper;
import com.cjrequena.sample.domain.model.aggregate.Area;
import com.cjrequena.sample.persistence.entity.AreaEntity;
import com.cjrequena.sample.persistence.repository.AreaRepository;
import com.cjrequena.sample.persistence.repository.cache.AreaCacheRedisHashOpsRepository;
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
 * Service layer for Area aggregate operations.
 *
 * @author cjrequena
 */
@Log4j2
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AreaService {

  private final AreaRepository areaRepository;
  private final AreaCacheRedisHashOpsRepository areaCacheRedisHashOpsRepository;
  private final CacheConfigurationProperties cacheConfigurationProperties;
  private final AreaMapper areaMapper;

  @PostConstruct
  public void loadUpCache() {
    if(cacheConfigurationProperties.isFullLoadEnabled()) {
      List<Area> areas = this.areaMapper.toDomainList(areaRepository.findAll());
      this.areaCacheRedisHashOpsRepository.load(areas);
      this.areaCacheRedisHashOpsRepository.retrieve();
    }
  }

  @Transactional
  public Area create(Area area) {
    log.debug("Creating area: {}", area.getName());
    AreaEntity entity = areaMapper.toEntity(area);
    AreaEntity savedEntity = areaRepository.save(entity);
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
    Optional<Area> area = areaRepository.findById(id).map(areaMapper::toDomain);

    // Update cache on successful database hit
    if (cacheConfigurationProperties.isCacheEnabled() && area.isPresent()) {
      try {
        areaCacheRedisHashOpsRepository.save(area.get());
        log.debug("Area cached after database query: {}", id);
      } catch (Exception e) {
        log.warn("Failed to cache area after database query: {}", id, e);
      }
    }

    return area;
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
    List<Area> areas = areaRepository.findAll().stream()
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

  public List<Area> findAllActive() {
    log.debug("Finding all active areas");
    return areaRepository.findAllByActiveTrue().stream()
      .map(areaMapper::toDomain)
      .collect(Collectors.toList());
  }

  public Page<Area> findByActive(Boolean active, Pageable pageable) {
    log.debug("Finding areas by active status: {}", active);
    return areaRepository.findByActive(active, pageable).map(areaMapper::toDomain);
  }

  public List<Area> findByCityId(UUID cityId) {
    log.debug("Finding areas by city ID: {}", cityId);
    return areaRepository.findByCityId(cityId).stream()
      .map(areaMapper::toDomain)
      .collect(Collectors.toList());
  }

  public List<Area> findActiveByCityId(UUID cityId) {
    log.debug("Finding active areas by city ID: {}", cityId);
    return areaRepository.findByCityIdAndActiveTrue(cityId).stream()
      .map(areaMapper::toDomain)
      .collect(Collectors.toList());
  }

  public Page<Area> findByCityId(UUID cityId, Pageable pageable) {
    log.debug("Finding areas by city ID: {} with pagination", cityId);
    return areaRepository.findByCityId(cityId, pageable).map(areaMapper::toDomain);
  }

  public List<Area> findByAreaType(String areaType) {
    log.debug("Finding areas by type: {}", areaType);
    return areaRepository.findByAreaType(areaType).stream()
      .map(areaMapper::toDomain)
      .collect(Collectors.toList());
  }

  public List<Area> findActiveByAreaType(String areaType) {
    log.debug("Finding active areas by type: {}", areaType);
    return areaRepository.findByAreaTypeAndActiveTrue(areaType).stream()
      .map(areaMapper::toDomain)
      .collect(Collectors.toList());
  }

  public List<Area> findByCityIdAndAreaType(UUID cityId, String areaType) {
    log.debug("Finding areas by city ID: {} and type: {}", cityId, areaType);
    return areaRepository.findByCityIdAndAreaType(cityId, areaType).stream()
      .map(areaMapper::toDomain)
      .collect(Collectors.toList());
  }

  public List<Area> findByPostalCode(String postalCode) {
    log.debug("Finding areas by postal code: {}", postalCode);
    return areaRepository.findByPostalCode(postalCode).stream()
      .map(areaMapper::toDomain)
      .collect(Collectors.toList());
  }

  public List<Area> findByCityIdAndPostalCode(UUID cityId, String postalCode) {
    log.debug("Finding areas by city ID: {} and postal code: {}", cityId, postalCode);
    return areaRepository.findByCityIdAndPostalCode(cityId, postalCode).stream()
      .map(areaMapper::toDomain)
      .collect(Collectors.toList());
  }

  public Optional<Area> findByCityIdAndName(UUID cityId, String name) {
    log.debug("Finding area by city ID: {} and name: {}", cityId, name);
    return areaRepository.findByCityIdAndName(cityId, name).map(areaMapper::toDomain);
  }

  public List<Area> findByNameContaining(String namePart) {
    log.debug("Finding areas by name containing: {}", namePart);
    return areaRepository.findByNameContainingIgnoreCase(namePart).stream()
      .map(areaMapper::toDomain)
      .collect(Collectors.toList());
  }

  public List<Area> findByCityIdAndPopulationGreaterThan(UUID cityId, Long minPopulation) {
    log.debug("Finding areas in city {} with population > {}", cityId, minPopulation);
    return areaRepository.findByCityIdAndPopulationGreaterThan(cityId, minPopulation).stream()
      .map(areaMapper::toDomain)
      .collect(Collectors.toList());
  }

  public Page<Area> findByCityIdOrderByPopulationDesc(UUID cityId, Pageable pageable) {
    log.debug("Finding top areas in city {} by population", cityId);
    return areaRepository.findByCityIdOrderByPopulationDesc(cityId, pageable)
      .map(areaMapper::toDomain);
  }

  public List<Area> findByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end) {
    log.debug("Finding areas created between {} and {}", start, end);
    return areaRepository.findByCreatedAtBetween(start, end).stream()
      .map(areaMapper::toDomain)
      .collect(Collectors.toList());
  }

  @Transactional
  public Area update(UUID id, Area area) {
    log.debug("Updating area with ID: {}", id);
    AreaEntity existingEntity = areaRepository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("Area not found with ID: " + id));

    AreaEntity updatedEntity = areaMapper.toEntity(area);
    updatedEntity.setId(existingEntity.getId());
    updatedEntity.setCreatedAt(existingEntity.getCreatedAt());

    AreaEntity savedEntity = areaRepository.save(updatedEntity);
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
  }

  @Transactional
  public void deleteById(UUID id) {
    log.debug("Deleting area with ID: {}", id);
    if (!areaRepository.existsById(id)) {
      throw new IllegalArgumentException("Area not found with ID: " + id);
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

  public boolean existsByCityIdAndName(UUID cityId, String name) {
    return areaRepository.existsByCityIdAndName(cityId, name);
  }

  public boolean existsByPostalCode(String postalCode) {
    return areaRepository.existsByPostalCode(postalCode);
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
