package com.cjrequena.sample.service;

import com.cjrequena.sample.configuration.CacheConfigurationProperties;
import com.cjrequena.sample.domain.mapper.LocationMapper;
import com.cjrequena.sample.domain.model.Location;
import com.cjrequena.sample.persistence.entity.LocationEntity;
import com.cjrequena.sample.persistence.repository.LocationRepository;
import com.cjrequena.sample.persistence.repository.cache.LocationCacheRedisHashOpsRepository;
import com.cjrequena.sample.service.base.BaseService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
 * Service layer for Location aggregate operations.
 *
 * <p>Handles business logic for locations (specific points with precise coordinates)
 * and orchestrates between domain model and persistence layer. Includes spatial
 * query operations using WKT (Well-Known Text) format for geometry parameters.</p>
 *
 * @author cjrequena
 */
@Log4j2
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationService extends BaseService<LocationEntity, Location> {

  private final LocationRepository locationRepository;
  private final LocationCacheRedisHashOpsRepository locationCacheRedisHashOpsRepository;
  private final CacheConfigurationProperties cacheConfigurationProperties;
  private final LocationMapper locationMapper;

  // ================================================================
  // BaseService Implementation
  // ================================================================

  @Override
  protected JpaRepository<LocationEntity, ?> getRepository() {
    return locationRepository;
  }

  @Override
  protected JpaSpecificationExecutor<LocationEntity> getSpecificationExecutor() {
    return locationRepository;
  }

  @Override
  protected Function<LocationEntity, Location> getEntityToDomainMapper() {
    return locationMapper::toDomain;
  }

  @Override
  protected Class<LocationEntity> getEntityClass() {
    return LocationEntity.class;
  }

  // ================================================================
  // Cache Initialization
  // ================================================================

  @PostConstruct
  public void loadUpCache() {
    if (cacheConfigurationProperties.isFullLoadEnabled()) {
      List<Location> locations = this.locationMapper.toDomainList(locationRepository.findAll());
      this.locationCacheRedisHashOpsRepository.load(locations);
    }
  }

  // ================================================================
  // CRUD Standard Operations
  // ================================================================

  @Transactional
  public Location create(Location location) {
    log.debug("Creating location: {}", location.getName());

    LocationEntity entity = locationMapper.toEntity(location);
    LocationEntity savedEntity = locationRepository.save(entity);
    Location createdLocation = locationMapper.toDomain(savedEntity);

    // Update cache
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        locationCacheRedisHashOpsRepository.save(createdLocation);
        log.debug("Location cached with ID: {}", createdLocation.getId());
      } catch (Exception e) {
        log.warn("Failed to cache location on create: {}", createdLocation.getId(), e);
      }
    }

    log.info("Location created with ID: {}", savedEntity.getId());
    return createdLocation;
  }

  public Optional<Location> findById(UUID id) {
    log.debug("Finding location by ID: {}", id);

    // Try cache first (cache-aside pattern)
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        Optional<Location> cachedLocation = locationCacheRedisHashOpsRepository.retrieveById(id);
        if (cachedLocation.isPresent()) {
          log.debug("Location found in cache: {}", id);
          return cachedLocation;
        }
        log.debug("Location not found in cache, querying database: {}", id);
      } catch (Exception e) {
        log.warn("Cache retrieval failed for location: {}, falling back to database", id, e);
      }
    }

    // Cache miss or disabled - query database
    Optional<Location> location = locationRepository.findById(id).map(locationMapper::toDomain);

    // Update cache on successful database hit
    if (cacheConfigurationProperties.isCacheEnabled() && location.isPresent()) {
      try {
        locationCacheRedisHashOpsRepository.save(location.get());
        log.debug("Location cached after database query: {}", id);
      } catch (Exception e) {
        log.warn("Failed to cache location after database query: {}", id, e);
      }
    }

    return location;
  }

  /**
   * Finds all locations without any filtering or pagination.
   *
   * <p>This method tries cache first, then falls back to database if cache is disabled or empty.</p>
   *
   * @return list of all locations
   */
  public List<Location> findAll() {
    log.debug("Finding all locations");

    // Try cache first for full list retrieval
    if (cacheConfigurationProperties.isCacheEnabled() && !locationCacheRedisHashOpsRepository.isEmpty()) {
      try {
        List<Location> cachedLocations = locationCacheRedisHashOpsRepository.retrieve();
        if (!cachedLocations.isEmpty()) {
          log.debug("Retrieved {} locations from cache", cachedLocations.size());
          return cachedLocations;
        }
      } catch (Exception e) {
        log.warn("Cache retrieval failed for all locations, falling back to database", e);
      }
    }

    // Cache miss or disabled - query database
    List<Location> locations = locationRepository.findAll().stream()
      .map(locationMapper::toDomain)
      .collect(Collectors.toList());

    // Update cache with full list
    if (cacheConfigurationProperties.isCacheEnabled() && !locations.isEmpty()) {
      try {
        locationCacheRedisHashOpsRepository.saveAll(locations);
        log.debug("Cached {} locations after database query", locations.size());
      } catch (Exception e) {
        log.warn("Failed to cache locations after database query", e);
      }
    }

    return locations;
  }

  /**
   * Finds all locations with optional RSQL filtering, sorting, and pagination.
   *
   * <p>This method does NOT use cache and always queries the database to ensure
   * accurate filtering and sorting results.</p>
   *
   * @param filters RSQL filter expression (e.g., "active==true;postalCode==94102")
   * @param offset the offset for pagination (0-based)
   * @param limit the maximum number of results to return
   * @param sort the sort expression (e.g., "name,asc" or "name,desc;createdAt,asc")
   * @return list of locations matching the criteria
   */
  public List<Location> findAll(String filters, Integer offset, Integer limit, String sort) {
    return super.findAllWithFiltersAndSort(filters, offset, limit, sort);
  }

  @Transactional
  public Location update(UUID id, Location location) {
    log.debug("Updating location with ID: {}", id);
    LocationEntity existingEntity = locationRepository
      .findById(id)
      .orElseThrow(() -> new IllegalArgumentException("Location not found with ID: " + id));

    LocationEntity updatedEntity = locationMapper.toEntity(location);
    updatedEntity.setId(existingEntity.getId());
    updatedEntity.setCreatedAt(existingEntity.getCreatedAt());

    LocationEntity savedEntity = locationRepository.save(updatedEntity);
    Location updatedLocation = locationMapper.toDomain(savedEntity);

    // Update cache
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        locationCacheRedisHashOpsRepository.save(updatedLocation);
        log.debug("Location cache updated with ID: {}", updatedLocation.getId());
      } catch (Exception e) {
        log.warn("Failed to update cache for location: {}", updatedLocation.getId(), e);
      }
    }

    log.info("Location updated with ID: {}", savedEntity.getId());
    return updatedLocation;
  }

  @Transactional
  public void deleteById(UUID id) {
    log.debug("Deleting location with ID: {}", id);
    if (!locationRepository.existsById(id)) {
      throw new IllegalArgumentException("Location not found with ID: " + id);
    }

    locationRepository.deleteById(id);

    // Remove from cache
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        locationCacheRedisHashOpsRepository.deleteById(id);
        log.debug("Location removed from cache: {}", id);
      } catch (Exception e) {
        log.warn("Failed to remove location from cache: {}", id, e);
      }
    }

    log.info("Location deleted with ID: {}", id);
  }

  // ================================================================
  // Read Operations
  // ================================================================

  public boolean existsById(UUID id) {
    // Check cache first for existence
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        if (locationCacheRedisHashOpsRepository.existsById(id)) {
          log.debug("Location exists in cache: {}", id);
          return true;
        }
      } catch (Exception e) {
        log.warn("Cache existence check failed for location: {}, falling back to database", id, e);
      }
    }

    return locationRepository.existsById(id);
  }

  // ================================================================
  // Count Operations
  // ================================================================

  /**
   * Counts all locations.
   *
   * @return total count of locations
   */
  public long count() {
    return locationRepository.count();
  }

  // ================================================================
  // Spatial Queries — Proximity
  // ================================================================

  /**
   * Finds all locations within a given radius from a center point.
   *
   * @param wkt the center point as WKT string (e.g., "POINT(-3.7038 40.4168)")
   * @param radiusMeters radius in metres
   * @return list of locations within the radius
   */
  public List<Location> findWithinRadius(String wkt, double radiusMeters) {
    log.debug("Finding locations within {} metres of {}", radiusMeters, wkt);

    return locationRepository.findWithinRadius(wkt, radiusMeters).stream()
      .map(locationMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds active locations within a given radius from a center point.
   *
   * @param wkt the center point as WKT string
   * @param radiusMeters radius in metres
   * @return list of active locations within the radius
   */
  public List<Location> findActiveWithinRadius(String wkt, double radiusMeters) {
    log.debug("Finding active locations within {} metres of {}", radiusMeters, wkt);

    return locationRepository.findActiveWithinRadius(wkt, radiusMeters).stream()
      .map(locationMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds locations within a radius, ordered by distance ascending.
   *
   * @param wkt the center point as WKT string
   * @param radiusMeters radius in metres
   * @return list of locations ordered by distance
   */
  public List<Location> findWithinRadiusOrderedByDistance(String wkt, double radiusMeters) {
    log.debug("Finding locations within {} metres of {}, ordered by distance", radiusMeters, wkt);

    return locationRepository.findWithinRadiusOrderedByDistance(wkt, radiusMeters).stream()
      .map(locationMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds the N nearest locations to a center point.
   *
   * @param wkt the center point as WKT string
   * @param maxRadius the maximum search radius in metres
   * @param pageable pagination information (use PageRequest.of(0, n) to limit results)
   * @return page of nearest locations
   */
  public Page<Location> findNearestLocations(String wkt, double maxRadius, Pageable pageable) {
    log.debug("Finding nearest locations to {} within {} metres", wkt, maxRadius);

    return locationRepository.findNearestLocations(wkt, maxRadius, pageable)
      .map(locationMapper::toDomain);
  }

  /**
   * Checks if an active location exists near the given point (useful for duplicate detection).
   *
   * @param wkt the point as WKT string
   * @param thresholdMeters the proximity threshold in metres
   * @return true if an active location exists within the threshold
   */
  public boolean existsActiveNearPoint(String wkt, double thresholdMeters) {
    return locationRepository.existsActiveNearPoint(wkt, thresholdMeters);
  }

  // ================================================================
  // Spatial Queries — Polygon Containment
  // ================================================================

  /**
   * Finds all locations within a given polygon.
   *
   * @param wkt the bounding polygon as WKT string (e.g., "POLYGON((-3.72 40.41, -3.68 40.41, -3.68 40.42, -3.72 40.42, -3.72 40.41))")
   * @return list of locations within the polygon
   */
  public List<Location> findWithinPolygon(String wkt) {
    log.debug("Finding locations within polygon: {}", wkt);

    return locationRepository.findWithinPolygon(wkt).stream()
      .map(locationMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds active locations within a given polygon.
   *
   * @param wkt the bounding polygon as WKT string
   * @return list of active locations within the polygon
   */
  public List<Location> findActiveWithinPolygon(String wkt) {
    log.debug("Finding active locations within polygon: {}", wkt);

    return locationRepository.findActiveWithinPolygon(wkt).stream()
      .map(locationMapper::toDomain)
      .collect(Collectors.toList());
  }

  // ================================================================
  // Combined Spatial + Zone Queries
  // ================================================================

  /**
   * Finds locations within a zone that are also within a given radius from a point.
   *
   * @param zoneId the zone ID
   * @param wkt the center point as WKT string
   * @param radiusMeters radius in metres
   * @return list of locations
   */
  public List<Location> findByZoneIdAndWithinRadius(UUID zoneId, String wkt, double radiusMeters) {
    log.debug("Finding locations in zone {} within {} metres of {}", zoneId, radiusMeters, wkt);

    return locationRepository
      .findByZoneIdAndWithinRadius(zoneId, wkt, radiusMeters).stream()
      .map(locationMapper::toDomain)
      .collect(Collectors.toList());
  }

}
