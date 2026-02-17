package com.cjrequena.sample.service;

import com.cjrequena.sample.configuration.CacheConfigurationProperties;
import com.cjrequena.sample.domain.exception.*;
import com.cjrequena.sample.domain.mapper.ZoneMapper;
import com.cjrequena.sample.domain.model.Zone;
import com.cjrequena.sample.persistence.entity.ZoneEntity;
import com.cjrequena.sample.persistence.repository.AreaRepository;
import com.cjrequena.sample.persistence.repository.GeoShapeRepository;
import com.cjrequena.sample.persistence.repository.ZoneRepository;
import com.cjrequena.sample.persistence.repository.cache.ZoneCacheRedisHashOpsRepository;
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
public class ZoneService extends BaseService<ZoneEntity, Zone> {

  private final ZoneRepository zoneRepository;
  private final AreaRepository areaRepository;
  private final GeoShapeRepository geoShapeRepository;

  private final ZoneCacheRedisHashOpsRepository zoneCacheRedisHashOpsRepository;
  private final CacheConfigurationProperties cacheConfigurationProperties;
  private final ZoneMapper zoneMapper;

  // ================================================================
  // BaseService Implementation
  // ================================================================

  @Override
  protected JpaRepository<ZoneEntity, ?> getRepository() {
    return null;
  }

  @Override
  protected JpaSpecificationExecutor<ZoneEntity> getSpecificationExecutor() {
    return null;
  }

  @Override
  protected Function<ZoneEntity, Zone> getEntityToDomainMapper() {
    return null;
  }

  @Override
  protected Class<ZoneEntity> getEntityClass() {
    return null;
  }

  // ================================================================
  // Cache Initialization
  // ================================================================

  @PostConstruct
  public void loadUpCache() {
    if (cacheConfigurationProperties.isFullLoadEnabled()) {
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

    if (zone.getAreaId() != null) {
      areaRepository
        .findById(zone.getAreaId())
        .orElseThrow(() -> new AreaNotFoundException("Area not found with ID: %s".formatted(zone.getAreaId())));
    } else {
      throw new AreaRequiredException("Area ID is required for creating a zone");
    }

    try {
      if (zone.getGeoShapeId() != null) {
        geoShapeRepository
          .findById(zone.getGeoShapeId())
          .orElseThrow(() -> new GeoShapeNotFoundException("GeoShape not found with ID: %s".formatted(zone.getGeoShapeId())));
      }

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
    } catch (DataIntegrityViolationException ex) {
      final String message = String.format("Unique constraint violation while creating zone: %s", zone.getName());
      log.warn(message);
      throw new UniqueConstraintException(message, ex);
    }
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
    Zone zone = zoneRepository
      .findById(id)
      .map(zoneMapper::toDomain)
      .orElseThrow(() -> new ZoneNotFoundException("Zone not found with ID: %s".formatted(id)));

    // Update cache on successful database hit
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        zoneCacheRedisHashOpsRepository.save(zone);
        log.debug("Zone cached after database query: {}", id);
      } catch (Exception e) {
        log.warn("Failed to cache zone after database query: {}", id, e);
      }
    }

    return Optional.of(zone);
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

  /**
   * Finds all zones with optional RSQL filtering, sorting, and pagination.
   *
   * <p>This method does NOT use cache and always queries the database to ensure
   * accurate filtering and sorting results.</p>
   *
   * @param filters RSQL filter expression (e.g., "active==true;postalCode==94102")
   * @param offset the offset for pagination (0-based)
   * @param limit the maximum number of results to return
   * @param sort the sort expression (e.g., "name,asc" or "name,desc;createdAt,asc")
   * @return list of zones matching the criteria
   */
  public List<Zone> findAll(String filters, Integer offset, Integer limit, String sort) {
    return super.findAllWithFiltersAndSort(filters, offset, limit, sort);
  }

  @Transactional
  public Zone update(UUID id, Zone zone) {
    log.debug("Updating zone with ID: {}", id);

    if (zone.getGeoShapeId() != null) {
      geoShapeRepository
        .findById(zone.getGeoShapeId())
        .orElseThrow(() -> new GeoShapeNotFoundException("GeoShape not found with ID: %s".formatted(zone.getGeoShapeId())));
    }

    ZoneEntity existingEntity = zoneRepository
      .findById(id)
      .orElseThrow(() -> new ZoneNotFoundException("Zone not found with ID: %s ".formatted(id)));

    try {
      ZoneEntity updatedEntity = zoneMapper.toEntity(zone);
      updatedEntity.setId(existingEntity.getId());
      updatedEntity.setCreatedAt(existingEntity.getCreatedAt());

      ZoneEntity savedEntity = zoneRepository.saveAndFlush(updatedEntity);
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
    } catch (DataIntegrityViolationException ex) {
      final String message = String.format("Unique constraint violation while creating zone: %s", zone.getName());
      log.warn(message);
      throw new UniqueConstraintException(message, ex);
    }
  }

  @Transactional
  public void deleteById(UUID id) {
    log.debug("Deleting zone with ID: {}", id);
    if (!zoneRepository.existsById(id)) {
      throw new ZoneNotFoundException("Zone not found with ID: %s".formatted(id));
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
