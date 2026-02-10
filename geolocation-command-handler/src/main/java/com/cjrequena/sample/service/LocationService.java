package com.cjrequena.sample.service;

import com.cjrequena.sample.configuration.CacheConfigurationProperties;
import com.cjrequena.sample.domain.mapper.LocationMapper;
import com.cjrequena.sample.domain.model.aggregate.Location;
import com.cjrequena.sample.persistence.entity.LocationEntity;
import com.cjrequena.sample.persistence.repository.LocationRepository;
import com.cjrequena.sample.persistence.repository.cache.LocationCacheRedisHashOpsRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
public class LocationService {

  private final LocationRepository locationRepository;
  private final LocationCacheRedisHashOpsRepository locationCacheRedisHashOpsRepository;
  private final CacheConfigurationProperties cacheConfigurationProperties;
  private final LocationMapper locationMapper;

  @PostConstruct
  public void loadUpCache() {
    if(cacheConfigurationProperties.isFullLoadEnabled()) {
      List<Location> locations = this.locationMapper.toDomainList(locationRepository.findAll());
      this.locationCacheRedisHashOpsRepository.load(locations);
      this.locationCacheRedisHashOpsRepository.retrieve();
    }
  }

  // ================================================================
  // CRUD Standard Operations
  // ================================================================

  @Transactional
  public Location create(Location location) {
    //log.debug("Creating location: {}", location.getName());
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

  @Transactional
  public Location update(UUID id, Location location) {
    log.debug("Updating location with ID: {}", id);
    LocationEntity existingEntity = locationRepository.findById(id)
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
  // Read Operations
  // ================================================================

  /**
   * Finds all active locations.
   *
   * @return list of active locations
   */
  public List<Location> findAllActive() {
    log.debug("Finding all active locations");
    
    return locationRepository.findAllByActiveTrue().stream()
      .map(locationMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds locations by active status with pagination.
   *
   * @param active the active status
   * @param pageable pagination information
   * @return page of locations
   */
  public Page<Location> findByActive(Boolean active, Pageable pageable) {
    log.debug("Finding locations by active status: {} with pagination", active);
    
    return locationRepository.findByActive(active, pageable)
      .map(locationMapper::toDomain);
  }

  /**
   * Finds all locations belonging to a specific zone.
   *
   * @param zoneId the zone ID
   * @return list of locations in the zone
   */
  public List<Location> findByZoneId(UUID zoneId) {
    log.debug("Finding locations by zone ID: {}", zoneId);
    
    return locationRepository.findByZoneId(zoneId).stream()
      .map(locationMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds active locations belonging to a specific zone.
   *
   * @param zoneId the zone ID
   * @return list of active locations in the zone
   */
  public List<Location> findActiveByZoneId(UUID zoneId) {
    log.debug("Finding active locations by zone ID: {}", zoneId);
    
    return locationRepository.findByZoneIdAndActiveTrue(zoneId).stream()
      .map(locationMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds locations in a zone with pagination.
   *
   * @param zoneId the zone ID
   * @param pageable pagination information
   * @return page of locations
   */
  public Page<Location> findByZoneId(UUID zoneId, Pageable pageable) {
    log.debug("Finding locations by zone ID: {} with pagination", zoneId);
    
    return locationRepository.findByZoneId(zoneId, pageable)
      .map(locationMapper::toDomain);
  }

  /**
   * Finds locations that are not assigned to any zone.
   *
   * @return list of unassigned locations
   */
  public List<Location> findUnassigned() {
    log.debug("Finding unassigned locations");
    
    return locationRepository.findByZoneIdIsNull().stream()
      .map(locationMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds locations that are assigned to a zone.
   *
   * @return list of assigned locations
   */
  public List<Location> findAssigned() {
    log.debug("Finding assigned locations");
    
    return locationRepository.findByZoneIdIsNotNull().stream()
      .map(locationMapper::toDomain)
      .collect(Collectors.toList());
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
    
    return locationRepository.findByZoneIdAndWithinRadius(zoneId, wkt, radiusMeters).stream()
      .map(locationMapper::toDomain)
      .collect(Collectors.toList());
  }

  // ================================================================
  // Postal Code Queries
  // ================================================================

  /**
   * Finds locations by postal code.
   *
   * @param postalCode the postal code
   * @return list of locations
   */
  public List<Location> findByPostalCode(String postalCode) {
    log.debug("Finding locations by postal code: {}", postalCode);
    
    return locationRepository.findByPostalCode(postalCode).stream()
      .map(locationMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds active locations by postal code.
   *
   * @param postalCode the postal code
   * @return list of active locations
   */
  public List<Location> findActiveByPostalCode(String postalCode) {
    log.debug("Finding active locations by postal code: {}", postalCode);
    
    return locationRepository.findByPostalCodeAndActiveTrue(postalCode).stream()
      .map(locationMapper::toDomain)
      .collect(Collectors.toList());
  }

  // ================================================================
  // Address-Based Queries
  // ================================================================

  /**
   * Finds locations where the address contains the given substring (case-insensitive).
   *
   * @param addressPart the substring to search for
   * @return list of matching locations
   */
  public List<Location> findByAddressContaining(String addressPart) {
    log.debug("Finding locations by address containing: {}", addressPart);
    
    return locationRepository.findByAddressContainingIgnoreCase(addressPart).stream()
      .map(locationMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds active locations where the address contains the given substring.
   *
   * @param addressPart the substring to search for
   * @return list of matching active locations
   */
  public List<Location> findActiveByAddressContaining(String addressPart) {
    log.debug("Finding active locations by address containing: {}", addressPart);
    
    return locationRepository.findByAddressContainingIgnoreCaseAndActiveTrue(addressPart).stream()
      .map(locationMapper::toDomain)
      .collect(Collectors.toList());
  }

  // ================================================================
  // Altitude / Accuracy Queries
  // ================================================================

  /**
   * Finds locations with an altitude greater than the specified value.
   *
   * @param minAltitude minimum altitude in metres
   * @return list of locations
   */
  public List<Location> findByAltitudeGreaterThan(BigDecimal minAltitude) {
    log.debug("Finding locations with altitude > {} metres", minAltitude);
    
    return locationRepository.findByAltitudeGreaterThan(minAltitude).stream()
      .map(locationMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds locations with an altitude between the specified range.
   *
   * @param minAltitude minimum altitude in metres
   * @param maxAltitude maximum altitude in metres
   * @return list of locations
   */
  public List<Location> findByAltitudeBetween(BigDecimal minAltitude, BigDecimal maxAltitude) {
    log.debug("Finding locations with altitude between {} and {} metres", minAltitude, maxAltitude);
    
    return locationRepository.findByAltitudeBetween(minAltitude, maxAltitude).stream()
      .map(locationMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds locations with GPS accuracy better than the specified value.
   *
   * @param maxAccuracy maximum accuracy in metres (lower is better)
   * @return list of locations
   */
  public List<Location> findByAccuracyBetterThan(BigDecimal maxAccuracy) {
    log.debug("Finding locations with accuracy <= {} metres", maxAccuracy);
    
    return locationRepository.findByAccuracyBetterThan(maxAccuracy).stream()
      .map(locationMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds locations that have altitude information.
   *
   * @return list of locations with altitude
   */
  public List<Location> findWithAltitude() {
    log.debug("Finding locations with altitude information");
    
    return locationRepository.findWithAltitude().stream()
      .map(locationMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds locations that have GPS accuracy information.
   *
   * @return list of locations with accuracy
   */
  public List<Location> findWithAccuracy() {
    log.debug("Finding locations with accuracy information");
    
    return locationRepository.findWithAccuracy().stream()
      .map(locationMapper::toDomain)
      .collect(Collectors.toList());
  }

  // ================================================================
  // Temporal Queries
  // ================================================================

  /**
   * Finds locations created within a time range.
   *
   * @param start start date/time
   * @param end end date/time
   * @return list of locations
   */
  public List<Location> findByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end) {
    log.debug("Finding locations created between {} and {}", start, end);
    
    return locationRepository.findByCreatedAtBetween(start, end).stream()
      .map(locationMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds the 10 most recently updated locations.
   *
   * @return list of recently updated locations
   */
  public List<Location> findRecentlyUpdated() {
    log.debug("Finding recently updated locations");
    
    return locationRepository.findTop10ByOrderByUpdatedAtDesc().stream()
      .map(locationMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds the 10 most recently created locations.
   *
   * @return list of recently created locations
   */
  public List<Location> findRecentlyCreated() {
    log.debug("Finding recently created locations");
    
    return locationRepository.findTop10ByOrderByCreatedAtDesc().stream()
      .map(locationMapper::toDomain)
      .collect(Collectors.toList());
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
}
