package com.cjrequena.sample.service;

import com.cjrequena.sample.configuration.CacheConfigurationProperties;
import com.cjrequena.sample.domain.exception.RegionNotFoundException;
import com.cjrequena.sample.domain.mapper.RegionMapper;
import com.cjrequena.sample.domain.model.Region;
import com.cjrequena.sample.persistence.entity.RegionEntity;
import com.cjrequena.sample.persistence.repository.RegionRepository;
import com.cjrequena.sample.persistence.repository.cache.RegionCacheRedisHashOpsRepository;
import com.cjrequena.sample.service.base.BaseService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
 * Service layer for Region aggregate operations.
 * 
 * <p>Handles business logic for regions (states, provinces, autonomous communities, etc.)
 * and orchestrates between domain model and persistence layer.</p>
 *
 * @author cjrequena
 */
@Log4j2
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionService extends BaseService<RegionEntity, Region> {

  private final RegionRepository regionRepository;
  private final RegionCacheRedisHashOpsRepository regionCacheRedisHashOpsRepository;
  private final CacheConfigurationProperties cacheConfigurationProperties;
  private final RegionMapper regionMapper;

  // ================================================================
  // BaseService Implementation
  // ================================================================

  @Override
  protected JpaRepository<RegionEntity, ?> getRepository() {
    return null;
  }

  @Override
  protected JpaSpecificationExecutor<RegionEntity> getSpecificationExecutor() {
    return null;
  }

  @Override
  protected Function<RegionEntity, Region> getEntityToDomainMapper() {
    return null;
  }

  @Override
  protected Class<RegionEntity> getEntityClass() {
    return null;
  }

  // ================================================================
  // Cache Initialization
  // ================================================================
  
  @PostConstruct
  public void loadUpCache() {
    if(cacheConfigurationProperties.isFullLoadEnabled()) {
      List<Region> regions = this.regionMapper.toDomainList(regionRepository.findAll());
      this.regionCacheRedisHashOpsRepository.load(regions);
      this.regionCacheRedisHashOpsRepository.retrieve();
    }
  }

  // ================================================================
  // CRUD Standard Operations
  // ================================================================

  @Transactional
  public Region create(Region region) {
    log.debug("Creating region: {}", region.getName());
    RegionEntity entity = regionMapper.toEntity(region);
    RegionEntity savedEntity = regionRepository.save(entity);
    Region createdRegion = regionMapper.toDomain(savedEntity);

    // Update cache
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        regionCacheRedisHashOpsRepository.save(createdRegion);
        log.debug("Region cached with ID: {}", createdRegion.getId());
      } catch (Exception e) {
        log.warn("Failed to cache region on create: {}", createdRegion.getId(), e);
      }
    }

    log.info("Region created with ID: {}", savedEntity.getId());
    return createdRegion;
  }

  public Region findById(UUID id) {
    log.debug("Finding region by ID: {}", id);

    // Try cache first (cache-aside pattern)
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        Optional<Region> cachedRegion = regionCacheRedisHashOpsRepository.retrieveById(id);
        if (cachedRegion.isPresent()) {
          log.debug("Region found in cache: {}", id);
          return cachedRegion.get();
        }
        log.debug("Region not found in cache, querying database: {}", id);
      } catch (Exception e) {
        log.warn("Cache retrieval failed for region: {}, falling back to database", id, e);
      }
    }

    // Cache miss or disabled - query database
    Region region = regionRepository
      .findById(id)
      .map(regionMapper::toDomain)
      .orElseThrow(() -> new RegionNotFoundException("Region not found with ID: %s".formatted(id)));

    // Update cache on successful database hit
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        regionCacheRedisHashOpsRepository.save(region);
        log.debug("Region cached after database query: {}", id);
      } catch (Exception e) {
        log.warn("Failed to cache region after database query: {}", id, e);
      }
    }

    return region;
  }

  public List<Region> findAll() {
    log.debug("Finding all regions");

    // Try cache first for full list retrieval
    if (cacheConfigurationProperties.isCacheEnabled() && !regionCacheRedisHashOpsRepository.isEmpty()) {
      try {
        List<Region> cachedRegions = regionCacheRedisHashOpsRepository.retrieve();
        if (!cachedRegions.isEmpty()) {
          log.debug("Retrieved {} regions from cache", cachedRegions.size());
          return cachedRegions;
        }
      } catch (Exception e) {
        log.warn("Cache retrieval failed for all regions, falling back to database", e);
      }
    }

    // Cache miss or disabled - query database
    List<Region> regions = regionRepository.findAll().stream()
      .map(regionMapper::toDomain)
      .collect(Collectors.toList());

    // Update cache with full list
    if (cacheConfigurationProperties.isCacheEnabled() && !regions.isEmpty()) {
      try {
        regionCacheRedisHashOpsRepository.saveAll(regions);
        log.debug("Cached {} regions after database query", regions.size());
      } catch (Exception e) {
        log.warn("Failed to cache regions after database query", e);
      }
    }

    return regions;
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
  public List<Region> findAll(String filters, Integer offset, Integer limit, String sort) {
    return super.findAllWithFiltersAndSort(filters, offset, limit, sort);
  }


  @Transactional
  public Region update(UUID id, Region region) {
    log.debug("Updating region with ID: {}", id);
    RegionEntity existingEntity = regionRepository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("Region not found with ID: " + id));

    RegionEntity updatedEntity = regionMapper.toEntity(region);
    updatedEntity.setId(existingEntity.getId());
    updatedEntity.setCreatedAt(existingEntity.getCreatedAt());

    RegionEntity savedEntity = regionRepository.save(updatedEntity);
    Region updatedRegion = regionMapper.toDomain(savedEntity);

    // Update cache
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        regionCacheRedisHashOpsRepository.save(updatedRegion);
        log.debug("Region cache updated with ID: {}", updatedRegion.getId());
      } catch (Exception e) {
        log.warn("Failed to update cache for region: {}", updatedRegion.getId(), e);
      }
    }

    log.info("Region updated with ID: {}", savedEntity.getId());
    return updatedRegion;
  }

  @Transactional
  public void deleteById(UUID id) {
    log.debug("Deleting region with ID: {}", id);
    if (!regionRepository.existsById(id)) {
      throw new IllegalArgumentException("Region not found with ID: " + id);
    }

    regionRepository.deleteById(id);

    // Remove from cache
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        regionCacheRedisHashOpsRepository.deleteById(id);
        log.debug("Region removed from cache: {}", id);
      } catch (Exception e) {
        log.warn("Failed to remove region from cache: {}", id, e);
      }
    }

    log.info("Region deleted with ID: {}", id);
  }

  public boolean existsById(UUID id) {
    // Check cache first for existence
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        if (regionCacheRedisHashOpsRepository.existsById(id)) {
          log.debug("Region exists in cache: {}", id);
          return true;
        }
      } catch (Exception e) {
        log.warn("Cache existence check failed for region: {}, falling back to database", id, e);
      }
    }

    return regionRepository.existsById(id);
  }

  // ================================================================
  // Count Operations
  // ================================================================

  /**
   * Counts all regions.
   *
   * @return total count of regions
   */
  public long count() {
    return regionRepository.count();
  }

}
