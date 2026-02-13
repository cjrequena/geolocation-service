package com.cjrequena.sample.service;

import com.cjrequena.sample.configuration.CacheConfigurationProperties;
import com.cjrequena.sample.domain.mapper.ZoneMapper;
import com.cjrequena.sample.domain.model.Zone;
import com.cjrequena.sample.persistence.entity.ZoneEntity;
import com.cjrequena.sample.persistence.repository.ZoneRepository;
import com.cjrequena.sample.persistence.repository.cache.ZoneCacheRedisHashOpsRepository;
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
 * Service layer for Zone aggregate operations.
 *
 * <p>Handles business logic for zones (subdivisions of areas) and orchestrates
 * between domain model and persistence layer.</p>
 *
 * @author cjrequena
 */
@Log4j2
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ZoneService {

  private final ZoneRepository zoneRepository;
  private final ZoneCacheRedisHashOpsRepository zoneCacheRedisHashOpsRepository;
  private final CacheConfigurationProperties cacheConfigurationProperties;
  private final ZoneMapper zoneMapper;

  @PostConstruct
  public void loadUpCache() {
    if(cacheConfigurationProperties.isFullLoadEnabled()) {
      List<Zone> zones = this.zoneMapper.toDomainList(zoneRepository.findAll());
      this.zoneCacheRedisHashOpsRepository.load(zones);
      this.zoneCacheRedisHashOpsRepository.retrieve();
    }
  }

  // ================================================================
  // CRUD Standard Operations
  // ================================================================

  @Transactional
  public Zone create(Zone zone) {
    log.debug("Creating zone: {}", zone.getName());
    ZoneEntity entity = zoneMapper.toEntity(zone);
    ZoneEntity savedEntity = zoneRepository.save(entity);
    Zone createdZone = zoneMapper.toDomain(savedEntity);

    // Update cache
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        zoneCacheRedisHashOpsRepository.save(createdZone);
        log.debug("Zone cached with ID: {}", createdZone.getId());
      } catch (Exception e) {
        log.warn("Failed to cache zone on create: {}", createdZone.getId(), e);
      }
    }

    log.info("Zone created with ID: {}", savedEntity.getId());
    return createdZone;
  }

  public Optional<Zone> findById(UUID id) {
    log.debug("Finding zone by ID: {}", id);

    // Try cache first (cache-aside pattern)
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        Optional<Zone> cachedZone = zoneCacheRedisHashOpsRepository.retrieveById(id);
        if (cachedZone.isPresent()) {
          log.debug("Zone found in cache: {}", id);
          return cachedZone;
        }
        log.debug("Zone not found in cache, querying database: {}", id);
      } catch (Exception e) {
        log.warn("Cache retrieval failed for zone: {}, falling back to database", id, e);
      }
    }

    // Cache miss or disabled - query database
    Optional<Zone> zone = zoneRepository.findById(id).map(zoneMapper::toDomain);

    // Update cache on successful database hit
    if (cacheConfigurationProperties.isCacheEnabled() && zone.isPresent()) {
      try {
        zoneCacheRedisHashOpsRepository.save(zone.get());
        log.debug("Zone cached after database query: {}", id);
      } catch (Exception e) {
        log.warn("Failed to cache zone after database query: {}", id, e);
      }
    }

    return zone;
  }

  public List<Zone> findAll() {
    log.debug("Finding all zones");

    // Try cache first for full list retrieval
    if (cacheConfigurationProperties.isCacheEnabled() && !zoneCacheRedisHashOpsRepository.isEmpty()) {
      try {
        List<Zone> cachedZones = zoneCacheRedisHashOpsRepository.retrieve();
        if (!cachedZones.isEmpty()) {
          log.debug("Retrieved {} zones from cache", cachedZones.size());
          return cachedZones;
        }
      } catch (Exception e) {
        log.warn("Cache retrieval failed for all zones, falling back to database", e);
      }
    }

    // Cache miss or disabled - query database
    List<Zone> zones = zoneRepository.findAll().stream()
      .map(zoneMapper::toDomain)
      .collect(Collectors.toList());

    // Update cache with full list
    if (cacheConfigurationProperties.isCacheEnabled() && !zones.isEmpty()) {
      try {
        zoneCacheRedisHashOpsRepository.saveAll(zones);
        log.debug("Cached {} zones after database query", zones.size());
      } catch (Exception e) {
        log.warn("Failed to cache zones after database query", e);
      }
    }

    return zones;
  }

  @Transactional
  public Zone update(UUID id, Zone zone) {
    log.debug("Updating zone with ID: {}", id);
    ZoneEntity existingEntity = zoneRepository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("Zone not found with ID: " + id));

    ZoneEntity updatedEntity = zoneMapper.toEntity(zone);
    updatedEntity.setId(existingEntity.getId());
    updatedEntity.setCreatedAt(existingEntity.getCreatedAt());

    ZoneEntity savedEntity = zoneRepository.save(updatedEntity);
    Zone updatedZone = zoneMapper.toDomain(savedEntity);

    // Update cache
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        zoneCacheRedisHashOpsRepository.save(updatedZone);
        log.debug("Zone cache updated with ID: {}", updatedZone.getId());
      } catch (Exception e) {
        log.warn("Failed to update cache for zone: {}", updatedZone.getId(), e);
      }
    }

    log.info("Zone updated with ID: {}", savedEntity.getId());
    return updatedZone;
  }

  @Transactional
  public void deleteById(UUID id) {
    log.debug("Deleting zone with ID: {}", id);
    if (!zoneRepository.existsById(id)) {
      throw new IllegalArgumentException("Zone not found with ID: " + id);
    }

    zoneRepository.deleteById(id);

    // Remove from cache
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        zoneCacheRedisHashOpsRepository.deleteById(id);
        log.debug("Zone removed from cache: {}", id);
      } catch (Exception e) {
        log.warn("Failed to remove zone from cache: {}", id, e);
      }
    }

    log.info("Zone deleted with ID: {}", id);
  }

  public boolean existsById(UUID id) {
    // Check cache first for existence
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        if (zoneCacheRedisHashOpsRepository.existsById(id)) {
          log.debug("Zone exists in cache: {}", id);
          return true;
        }
      } catch (Exception e) {
        log.warn("Cache existence check failed for zone: {}, falling back to database", id, e);
      }
    }

    return zoneRepository.existsById(id);
  }

  // ================================================================
  // Read Operations
  // ================================================================

  /**
   * Finds all active zones.
   *
   * @return list of active zones
   */
  public List<Zone> findAllActive() {
    log.debug("Finding all active zones");
    
    return zoneRepository.findAllByActiveTrue().stream()
      .map(zoneMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds zones by active status with pagination.
   *
   * @param active the active status
   * @param pageable pagination information
   * @return page of zones
   */
  public Page<Zone> findByActive(Boolean active, Pageable pageable) {
    log.debug("Finding zones by active status: {} with pagination", active);
    
    return zoneRepository.findByActive(active, pageable)
      .map(zoneMapper::toDomain);
  }

  /**
   * Finds all zones belonging to a specific area.
   *
   * @param areaId the area ID
   * @return list of zones in the area
   */
  public List<Zone> findByAreaId(UUID areaId) {
    log.debug("Finding zones by area ID: {}", areaId);
    
    return zoneRepository.findByAreaId(areaId).stream()
      .map(zoneMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds active zones belonging to a specific area.
   *
   * @param areaId the area ID
   * @return list of active zones in the area
   */
  public List<Zone> findActiveByAreaId(UUID areaId) {
    log.debug("Finding active zones by area ID: {}", areaId);
    
    return zoneRepository.findByAreaIdAndActiveTrue(areaId).stream()
      .map(zoneMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds zones in an area with pagination.
   *
   * @param areaId the area ID
   * @param pageable pagination information
   * @return page of zones
   */
  public Page<Zone> findByAreaId(UUID areaId, Pageable pageable) {
    log.debug("Finding zones by area ID: {} with pagination", areaId);
    
    return zoneRepository.findByAreaId(areaId, pageable)
      .map(zoneMapper::toDomain);
  }

  /**
   * Finds a zone by area ID and name.
   *
   * @param areaId the area ID
   * @param name the zone name
   * @return Optional containing the zone if found
   */
  public Optional<Zone> findByAreaIdAndName(UUID areaId, String name) {
    log.debug("Finding zone by area ID: {} and name: {}", areaId, name);
    
    return zoneRepository.findByAreaIdAndName(areaId, name)
      .map(zoneMapper::toDomain);
  }

  /**
   * Finds zones by zone type.
   *
   * @param zoneType the zone type (e.g., "PARK", "INDUSTRIAL")
   * @return list of zones
   */
  public List<Zone> findByZoneType(String zoneType) {
    log.debug("Finding zones by type: {}", zoneType);
    
    return zoneRepository.findByZoneType(zoneType).stream()
      .map(zoneMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds active zones by zone type.
   *
   * @param zoneType the zone type
   * @return list of active zones
   */
  public List<Zone> findActiveByZoneType(String zoneType) {
    log.debug("Finding active zones by type: {}", zoneType);
    
    return zoneRepository.findByZoneTypeAndActiveTrue(zoneType).stream()
      .map(zoneMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds zones in an area filtered by zone type.
   *
   * @param areaId the area ID
   * @param zoneType the zone type
   * @return list of zones
   */
  public List<Zone> findByAreaIdAndZoneType(UUID areaId, String zoneType) {
    log.debug("Finding zones by area ID: {} and type: {}", areaId, zoneType);
    
    return zoneRepository.findByAreaIdAndZoneType(areaId, zoneType).stream()
      .map(zoneMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds zones by postal code.
   *
   * @param postalCode the postal code
   * @return list of zones
   */
  public List<Zone> findByPostalCode(String postalCode) {
    log.debug("Finding zones by postal code: {}", postalCode);
    
    return zoneRepository.findByPostalCode(postalCode).stream()
      .map(zoneMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds active zones by postal code.
   *
   * @param postalCode the postal code
   * @return list of active zones
   */
  public List<Zone> findActiveByPostalCode(String postalCode) {
    log.debug("Finding active zones by postal code: {}", postalCode);
    
    return zoneRepository.findByPostalCodeAndActiveTrue(postalCode).stream()
      .map(zoneMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds zones in an area filtered by postal code.
   *
   * @param areaId the area ID
   * @param postalCode the postal code
   * @return list of zones
   */
  public List<Zone> findByAreaIdAndPostalCode(UUID areaId, String postalCode) {
    log.debug("Finding zones by area ID: {} and postal code: {}", areaId, postalCode);
    
    return zoneRepository.findByAreaIdAndPostalCode(areaId, postalCode).stream()
      .map(zoneMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds zones where the name contains the given substring (case-insensitive).
   *
   * @param namePart the substring to search for
   * @return list of matching zones
   */
  public List<Zone> findByNameContaining(String namePart) {
    log.debug("Finding zones by name containing: {}", namePart);
    
    return zoneRepository.findByNameContainingIgnoreCase(namePart).stream()
      .map(zoneMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds zones associated with a specific GeoShape.
   *
   * @param geoShapeId the GeoShape ID
   * @return list of zones
   */
  public List<Zone> findByGeoShapeId(UUID geoShapeId) {
    log.debug("Finding zones by GeoShape ID: {}", geoShapeId);
    
    return zoneRepository.findByGeoShapeId(geoShapeId).stream()
      .map(zoneMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds zones that have no associated GeoShape.
   *
   * @return list of zones without GeoShape
   */
  public List<Zone> findWithoutGeoShape() {
    log.debug("Finding zones without GeoShape");
    
    return zoneRepository.findByGeoShapeIdIsNull().stream()
      .map(zoneMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds zones that have an associated GeoShape.
   *
   * @return list of zones with GeoShape
   */
  public List<Zone> findWithGeoShape() {
    log.debug("Finding zones with GeoShape");
    
    return zoneRepository.findByGeoShapeIdIsNotNull().stream()
      .map(zoneMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds zones created within a time range.
   *
   * @param start start date/time
   * @param end end date/time
   * @return list of zones
   */
  public List<Zone> findByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end) {
    log.debug("Finding zones created between {} and {}", start, end);
    
    return zoneRepository.findByCreatedAtBetween(start, end).stream()
      .map(zoneMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds the 10 most recently updated zones.
   *
   * @return list of recently updated zones
   */
  public List<Zone> findRecentlyUpdated() {
    log.debug("Finding recently updated zones");
    
    return zoneRepository.findTop10ByOrderByUpdatedAtDesc().stream()
      .map(zoneMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Checks if a zone exists by area ID and name.
   *
   * @param areaId the area ID
   * @param name the zone name
   * @return true if exists, false otherwise
   */
  public boolean existsByAreaIdAndName(UUID areaId, String name) {
    return zoneRepository.existsByAreaIdAndName(areaId, name);
  }

  /**
   * Checks if an active zone exists by area ID and name.
   *
   * @param areaId the area ID
   * @param name the zone name
   * @return true if exists, false otherwise
   */
  public boolean existsActiveByAreaIdAndName(UUID areaId, String name) {
    return zoneRepository.existsByAreaIdAndNameAndActiveTrue(areaId, name);
  }

  /**
   * Checks if a zone exists by postal code.
   *
   * @param postalCode the postal code
   * @return true if exists, false otherwise
   */
  public boolean existsByPostalCode(String postalCode) {
    return zoneRepository.existsByPostalCode(postalCode);
  }

  // ================================================================
  // Count Operations
  // ================================================================

  /**
   * Counts all zones.
   *
   * @return total count of zones
   */
  public long count() {
    return zoneRepository.count();
  }
}
