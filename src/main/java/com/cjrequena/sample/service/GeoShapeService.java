package com.cjrequena.sample.service;

import com.cjrequena.sample.configuration.CacheConfigurationProperties;
import com.cjrequena.sample.domain.mapper.GeoShapeMapper;
import com.cjrequena.sample.domain.model.GeoShape;
import com.cjrequena.sample.persistence.entity.GeoShapeEntity;
import com.cjrequena.sample.persistence.repository.GeoShapeRepository;
import com.cjrequena.sample.persistence.repository.cache.GeoShapeCacheRedisHashOpsRepository;
import com.cjrequena.sample.service.base.BaseService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
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
 * Service layer for GeoShape aggregate operations.
 *
 * <p>Handles business logic for geographic shapes (polygons, circles, etc.)
 * and orchestrates between domain model and persistence layer. Includes spatial
 * query operations using PostGIS/Hibernate Spatial.</p>
 *
 * @author cjrequena
 */
@Log4j2
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GeoShapeService extends BaseService<GeoShapeEntity, GeoShape> {

  private final GeoShapeRepository geoShapeRepository;
  private final GeoShapeCacheRedisHashOpsRepository geoShapeCacheRedisHashOpsRepository;
  private final CacheConfigurationProperties cacheConfigurationProperties;
  private final GeoShapeMapper geoShapeMapper;

  // ================================================================
  // BaseService Implementation
  // ================================================================

  @Override
  protected JpaRepository<GeoShapeEntity, ?> getRepository() {
    return geoShapeRepository;
  }

  @Override
  protected JpaSpecificationExecutor<GeoShapeEntity> getSpecificationExecutor() {
    return geoShapeRepository;
  }

  @Override
  protected Function<GeoShapeEntity, GeoShape> getEntityToDomainMapper() {
    return geoShapeMapper::toDomain;
  }

  @Override
  protected Class<GeoShapeEntity> getEntityClass() {
    return GeoShapeEntity.class;
  }

  // ================================================================
  // Cache Initialization
  // ================================================================

  @PostConstruct
  public void loadUpCache() {
    if(cacheConfigurationProperties.isFullLoadEnabled()) {
      List<GeoShape> geoShapes = this.geoShapeMapper.toDomainList(geoShapeRepository.findAll());
      this.geoShapeCacheRedisHashOpsRepository.load(geoShapes);
      this.geoShapeCacheRedisHashOpsRepository.retrieve();
    }
  }

  // ================================================================
  // CRUD Standard Operations
  // ================================================================

  @Transactional
  public GeoShape create(GeoShape geoShape) {
    log.debug("Creating geoShape: {}", geoShape.getName());
    GeoShapeEntity entity = geoShapeMapper.toEntity(geoShape);
    GeoShapeEntity savedEntity = geoShapeRepository.save(entity);
    GeoShape createdGeoShape = geoShapeMapper.toDomain(savedEntity);

    // Update cache
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        geoShapeCacheRedisHashOpsRepository.save(createdGeoShape);
        log.debug("GeoShape cached with ID: {}", createdGeoShape.getId());
      } catch (Exception e) {
        log.warn("Failed to cache geoShape on create: {}", createdGeoShape.getId(), e);
      }
    }

    log.info("GeoShape created with ID: {}", savedEntity.getId());
    return createdGeoShape;
  }

  public Optional<GeoShape> findById(UUID id) {
    log.debug("Finding geoShape by ID: {}", id);

    // Try cache first (cache-aside pattern)
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        Optional<GeoShape> cachedGeoShape = geoShapeCacheRedisHashOpsRepository.retrieveById(id);
        if (cachedGeoShape.isPresent()) {
          log.debug("GeoShape found in cache: {}", id);
          return cachedGeoShape;
        }
        log.debug("GeoShape not found in cache, querying database: {}", id);
      } catch (Exception e) {
        log.warn("Cache retrieval failed for geoShape: {}, falling back to database", id, e);
      }
    }

    // Cache miss or disabled - query database
    Optional<GeoShape> geoShape = geoShapeRepository.findById(id).map(geoShapeMapper::toDomain);

    // Update cache on successful database hit
    if (cacheConfigurationProperties.isCacheEnabled() && geoShape.isPresent()) {
      try {
        geoShapeCacheRedisHashOpsRepository.save(geoShape.get());
        log.debug("GeoShape cached after database query: {}", id);
      } catch (Exception e) {
        log.warn("Failed to cache geoShape after database query: {}", id, e);
      }
    }

    return geoShape;
  }

  public List<GeoShape> findAll() {
    log.debug("Finding all geoShapes");

    // Try cache first for full list retrieval
    if (cacheConfigurationProperties.isCacheEnabled() && !geoShapeCacheRedisHashOpsRepository.isEmpty()) {
      try {
        List<GeoShape> cachedGeoShapes = geoShapeCacheRedisHashOpsRepository.retrieve();
        if (!cachedGeoShapes.isEmpty()) {
          log.debug("Retrieved {} geoShapes from cache", cachedGeoShapes.size());
          return cachedGeoShapes;
        }
      } catch (Exception e) {
        log.warn("Cache retrieval failed for all geoShapes, falling back to database", e);
      }
    }

    // Cache miss or disabled - query database
    List<GeoShape> geoShapes = geoShapeRepository.findAll().stream()
      .map(geoShapeMapper::toDomain)
      .collect(Collectors.toList());

    // Update cache with full list
    if (cacheConfigurationProperties.isCacheEnabled() && !geoShapes.isEmpty()) {
      try {
        geoShapeCacheRedisHashOpsRepository.saveAll(geoShapes);
        log.debug("Cached {} geoShapes after database query", geoShapes.size());
      } catch (Exception e) {
        log.warn("Failed to cache geoShapes after database query", e);
      }
    }

    return geoShapes;
  }

  /**
   * Finds all geoShapes with optional RSQL filtering, sorting, and pagination.
   *
   * <p>This method does NOT use cache and always queries the database to ensure
   * accurate filtering and sorting results.</p>
   *
   * @param filters RSQL filter expression (e.g., "active==true;postalCode==94102")
   * @param offset the offset for pagination (0-based)
   * @param limit the maximum number of results to return
   * @param sort the sort expression (e.g., "name,asc" or "name,desc;createdAt,asc")
   * @return list of geoShapes matching the criteria
   */
  public List<GeoShape> findAll(String filters, Integer offset, Integer limit, String sort) {
    return super.findAllWithFiltersAndSort(filters, offset, limit, sort);
  }

  @Transactional
  public GeoShape update(UUID id, GeoShape geoShape) {
    log.debug("Updating geoShape with ID: {}", id);
    GeoShapeEntity existingEntity = geoShapeRepository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("GeoShape not found with ID: " + id));

    GeoShapeEntity updatedEntity = geoShapeMapper.toEntity(geoShape);
    updatedEntity.setId(existingEntity.getId());
    updatedEntity.setCreatedAt(existingEntity.getCreatedAt());

    GeoShapeEntity savedEntity = geoShapeRepository.save(updatedEntity);
    GeoShape updatedGeoShape = geoShapeMapper.toDomain(savedEntity);

    // Update cache
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        geoShapeCacheRedisHashOpsRepository.save(updatedGeoShape);
        log.debug("GeoShape cache updated with ID: {}", updatedGeoShape.getId());
      } catch (Exception e) {
        log.warn("Failed to update cache for geoShape: {}", updatedGeoShape.getId(), e);
      }
    }

    log.info("GeoShape updated with ID: {}", savedEntity.getId());
    return updatedGeoShape;
  }

  @Transactional
  public void deleteById(UUID id) {
    log.debug("Deleting geoShape with ID: {}", id);
    if (!geoShapeRepository.existsById(id)) {
      throw new IllegalArgumentException("GeoShape not found with ID: " + id);
    }

    geoShapeRepository.deleteById(id);

    // Remove from cache
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        geoShapeCacheRedisHashOpsRepository.deleteById(id);
        log.debug("GeoShape removed from cache: {}", id);
      } catch (Exception e) {
        log.warn("Failed to remove geoShape from cache: {}", id, e);
      }
    }

    log.info("GeoShape deleted with ID: {}", id);
  }



  // ================================================================
  // Existence Checks
  // ================================================================

  public boolean existsById(UUID id) {
    // Check cache first for existence
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        if (geoShapeCacheRedisHashOpsRepository.existsById(id)) {
          log.debug("GeoShape exists in cache: {}", id);
          return true;
        }
      } catch (Exception e) {
        log.warn("Cache existence check failed for geoShape: {}, falling back to database", id, e);
      }
    }

    return geoShapeRepository.existsById(id);
  }

  /**
   * Checks if a GeoShape exists by name.
   *
   * @param name the GeoShape name
   * @return true if exists, false otherwise
   */
  public boolean existsByName(String name) {
    return geoShapeRepository.existsByName(name);
  }



  // ================================================================
  // Count Operations
  // ================================================================

  /**
   * Counts all GeoShapes.
   *
   * @return total count of GeoShapes
   */
  public long count() {
    return geoShapeRepository.count();
  }


  // ================================================================
  // Spatial Queries — Point Containment
  // ================================================================

  /**
   * Finds all GeoShapes that contain the given point.
   *
   * @param point the JTS Point to test
   * @return list of GeoShapes containing the point
   */
  public List<GeoShape> findContainingPoint(Point point) {
    log.debug("Finding GeoShapes containing point: {}", point);
    
    return geoShapeRepository.findContainingPoint(point).stream()
      .map(geoShapeMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds active GeoShapes that contain the given point.
   *
   * @param point the JTS Point to test
   * @return list of active GeoShapes containing the point
   */
  public List<GeoShape> findActiveContainingPoint(Point point) {
    log.debug("Finding active GeoShapes containing point: {}", point);
    
    return geoShapeRepository.findActiveContainingPoint(point).stream()
      .map(geoShapeMapper::toDomain)
      .collect(Collectors.toList());
  }

  // ================================================================
  // Spatial Queries — Geometry Intersection
  // ================================================================

  /**
   * Finds all GeoShapes that intersect with the given geometry.
   *
   * @param geometry the JTS Geometry to test
   * @return list of intersecting GeoShapes
   */
  public List<GeoShape> findIntersecting(Geometry geometry) {
    log.debug("Finding GeoShapes intersecting with geometry: {}", geometry.getGeometryType());
    
    return geoShapeRepository.findIntersecting(geometry).stream()
      .map(geoShapeMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds active GeoShapes that intersect with the given geometry.
   *
   * @param geometry the JTS Geometry to test
   * @return list of active intersecting GeoShapes
   */
  public List<GeoShape> findActiveIntersecting(Geometry geometry) {
    log.debug("Finding active GeoShapes intersecting with geometry: {}", geometry.getGeometryType());
    
    return geoShapeRepository.findActiveIntersecting(geometry).stream()
      .map(geoShapeMapper::toDomain)
      .collect(Collectors.toList());
  }

  // ================================================================
  // Spatial Queries — Distance-Based
  // ================================================================

  /**
   * Finds all GeoShapes within a given distance from a point.
   *
   * @param wkt the center point as WKT string (e.g., "POINT(-3.7038 40.4168)")
   * @param distanceMeters distance in metres
   * @return list of GeoShapes within the distance
   */
  public List<GeoShape> findWithinDistance(String wkt, double distanceMeters) {
    log.debug("Finding GeoShapes within {} metres of {}", distanceMeters, wkt);
    
    return geoShapeRepository.findWithinDistance(wkt, distanceMeters).stream()
      .map(geoShapeMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds GeoShapes within a distance, ordered by distance ascending.
   *
   * @param wkt the center point as WKT string
   * @param distanceMeters distance in metres
   * @return list of GeoShapes ordered by distance
   */
  public List<GeoShape> findWithinDistanceOrderedByDistance(String wkt, double distanceMeters) {
    log.debug("Finding GeoShapes within {} metres of {}, ordered by distance", distanceMeters, wkt);
    
    return geoShapeRepository.findWithinDistanceOrderedByDistance(wkt, distanceMeters).stream()
      .map(geoShapeMapper::toDomain)
      .collect(Collectors.toList());
  }

  // ================================================================
  // Spatial Queries — Bounding Box
  // ================================================================

  /**
   * Finds all GeoShapes that intersect with the given bounding box.
   *
   * @param boundingBox the JTS Polygon representing the bounding box
   * @return list of GeoShapes in the bounding box
   */
  public List<GeoShape> findInBoundingBox(Polygon boundingBox) {
    log.debug("Finding GeoShapes in bounding box");
    
    return geoShapeRepository.findInBoundingBox(boundingBox).stream()
      .map(geoShapeMapper::toDomain)
      .collect(Collectors.toList());
  }

}
