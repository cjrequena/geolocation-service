package com.cjrequena.sample.service;

import com.cjrequena.sample.configuration.CacheConfigurationProperties;
import com.cjrequena.sample.domain.mapper.RegionMapper;
import com.cjrequena.sample.domain.model.Region;
import com.cjrequena.sample.persistence.entity.RegionEntity;
import com.cjrequena.sample.persistence.repository.RegionRepository;
import com.cjrequena.sample.persistence.repository.cache.RegionCacheRedisHashOpsRepository;
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
public class RegionService {

  private final RegionRepository regionRepository;
  private final RegionCacheRedisHashOpsRepository regionCacheRedisHashOpsRepository;
  private final CacheConfigurationProperties cacheConfigurationProperties;
  private final RegionMapper regionMapper;

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

  public Optional<Region> findById(UUID id) {
    log.debug("Finding region by ID: {}", id);

    // Try cache first (cache-aside pattern)
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        Optional<Region> cachedRegion = regionCacheRedisHashOpsRepository.retrieveById(id);
        if (cachedRegion.isPresent()) {
          log.debug("Region found in cache: {}", id);
          return cachedRegion;
        }
        log.debug("Region not found in cache, querying database: {}", id);
      } catch (Exception e) {
        log.warn("Cache retrieval failed for region: {}, falling back to database", id, e);
      }
    }

    // Cache miss or disabled - query database
    Optional<Region> region = regionRepository.findById(id).map(regionMapper::toDomain);

    // Update cache on successful database hit
    if (cacheConfigurationProperties.isCacheEnabled() && region.isPresent()) {
      try {
        regionCacheRedisHashOpsRepository.save(region.get());
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
  // Read Operations
  // ================================================================

  /**
   * Finds all active regions.
   *
   * @return list of active regions
   */
  public List<Region> findAllActive() {
    log.debug("Finding all active regions");
    
    return regionRepository.findAllByActiveTrue().stream()
      .map(regionMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds regions by active status with pagination.
   *
   * @param active the active status
   * @param pageable pagination information
   * @return page of regions
   */
  public Page<Region> findByActive(Boolean active, Pageable pageable) {
    log.debug("Finding regions by active status: {} with pagination", active);
    
    return regionRepository.findByActive(active, pageable)
      .map(regionMapper::toDomain);
  }

  /**
   * Finds all regions belonging to a specific country.
   *
   * @param countryId the country ID
   * @return list of regions in the country
   */
  public List<Region> findByCountryId(UUID countryId) {
    log.debug("Finding regions by country ID: {}", countryId);
    
    return regionRepository.findByCountryId(countryId).stream()
      .map(regionMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds active regions belonging to a specific country.
   *
   * @param countryId the country ID
   * @return list of active regions in the country
   */
  public List<Region> findActiveByCountryId(UUID countryId) {
    log.debug("Finding active regions by country ID: {}", countryId);
    
    return regionRepository.findByCountryIdAndActiveTrue(countryId).stream()
      .map(regionMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds regions in a country with pagination.
   *
   * @param countryId the country ID
   * @param pageable pagination information
   * @return page of regions
   */
  public Page<Region> findByCountryId(UUID countryId, Pageable pageable) {
    log.debug("Finding regions by country ID: {} with pagination", countryId);
    
    return regionRepository.findByCountryId(countryId, pageable)
      .map(regionMapper::toDomain);
  }

  /**
   * Finds a region by country ID and name.
   *
   * @param countryId the country ID
   * @param name the region name
   * @return Optional containing the region if found
   */
  public Optional<Region> findByCountryIdAndName(UUID countryId, String name) {
    log.debug("Finding region by country ID: {} and name: {}", countryId, name);
    
    return regionRepository.findByCountryIdAndName(countryId, name)
      .map(regionMapper::toDomain);
  }

  /**
   * Finds regions by region type.
   *
   * @param regionType the region type (e.g., "STATE", "PROVINCE")
   * @return list of regions
   */
  public List<Region> findByRegionType(String regionType) {
    log.debug("Finding regions by type: {}", regionType);
    
    return regionRepository.findByRegionType(regionType).stream()
      .map(regionMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds active regions by region type.
   *
   * @param regionType the region type
   * @return list of active regions
   */
  public List<Region> findActiveByRegionType(String regionType) {
    log.debug("Finding active regions by type: {}", regionType);
    
    return regionRepository.findByRegionTypeAndActiveTrue(regionType).stream()
      .map(regionMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds regions by name containing substring (case-insensitive).
   *
   * @param namePart the substring to search for
   * @return list of matching regions
   */
  public List<Region> findByNameContaining(String namePart) {
    log.debug("Finding regions by name containing: {}", namePart);
    
    return regionRepository.findByNameContainingIgnoreCase(namePart).stream()
      .map(regionMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds regions in a country with population greater than threshold.
   *
   * @param countryId the country ID
   * @param minPopulation the minimum population
   * @return list of regions
   */
  public List<Region> findByCountryIdAndPopulationGreaterThan(UUID countryId, Long minPopulation) {
    log.debug("Finding regions in country {} with population > {}", countryId, minPopulation);
    
    return regionRepository.findByCountryIdAndPopulationGreaterThan(countryId, minPopulation).stream()
      .map(regionMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds top regions in a country ordered by population descending.
   *
   * @param countryId the country ID
   * @param pageable pagination information
   * @return page of regions ordered by population
   */
  public Page<Region> findByCountryIdOrderByPopulationDesc(UUID countryId, Pageable pageable) {
    log.debug("Finding top regions in country {} by population", countryId);
    
    return regionRepository.findByCountryIdOrderByPopulationDesc(countryId, pageable)
      .map(regionMapper::toDomain);
  }

  /**
   * Finds regions created within a time range.
   *
   * @param start start date/time
   * @param end end date/time
   * @return list of regions
   */
  public List<Region> findByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end) {
    log.debug("Finding regions created between {} and {}", start, end);
    
    return regionRepository.findByCreatedAtBetween(start, end).stream()
      .map(regionMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Checks if a region exists by country ID and name.
   *
   * @param countryId the country ID
   * @param name the region name
   * @return true if exists, false otherwise
   */
  public boolean existsByCountryIdAndName(UUID countryId, String name) {
    return regionRepository.existsByCountryIdAndName(countryId, name);
  }

  /**
   * Checks if an active region exists by country ID and name.
   *
   * @param countryId the country ID
   * @param name the region name
   * @return true if exists, false otherwise
   */
  public boolean existsActiveByCountryIdAndName(UUID countryId, String name) {
    return regionRepository.existsByCountryIdAndNameAndActiveTrue(countryId, name);
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
