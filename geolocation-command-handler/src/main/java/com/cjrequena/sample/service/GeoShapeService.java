package com.cjrequena.sample.service;

import com.cjrequena.sample.domain.mapper.GeoShapeMapper;
import com.cjrequena.sample.domain.model.aggregate.GeoShape;
import com.cjrequena.sample.persistence.entity.GeoShapeEntity;
import com.cjrequena.sample.persistence.repository.GeoShapeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
public class GeoShapeService {

  private final GeoShapeRepository geoShapeRepository;
  private final GeoShapeMapper geoShapeMapper;

  // ================================================================
  // Create Operations
  // ================================================================

  /**
   * Creates a new GeoShape.
   *
   * @param geoShape the GeoShape domain aggregate to create
   * @return the created GeoShape with generated ID
   */
  @Transactional
  public GeoShape create(GeoShape geoShape) {
    log.debug("Creating GeoShape with geometry type: {}", geoShape.getGeometryType());
    
    GeoShapeEntity entity = geoShapeMapper.toEntity(geoShape);
    GeoShapeEntity savedEntity = geoShapeRepository.save(entity);
    
    log.info("GeoShape created with ID: {}", savedEntity.getId());
    return geoShapeMapper.toDomain(savedEntity);
  }

  // ================================================================
  // Read Operations
  // ================================================================

  /**
   * Finds a GeoShape by ID.
   *
   * @param id the GeoShape ID
   * @return Optional containing the GeoShape if found
   */
  public Optional<GeoShape> findById(UUID id) {
    log.debug("Finding GeoShape by ID: {}", id);
    
    return geoShapeRepository.findById(id)
      .map(geoShapeMapper::toDomain);
  }

  /**
   * Finds a GeoShape by name.
   *
   * @param name the GeoShape name
   * @return Optional containing the GeoShape if found
   */
  public Optional<GeoShape> findByName(String name) {
    log.debug("Finding GeoShape by name: {}", name);
    
    return geoShapeRepository.findByName(name)
      .map(geoShapeMapper::toDomain);
  }

  /**
   * Finds all GeoShapes.
   *
   * @return list of all GeoShapes
   */
  public List<GeoShape> findAll() {
    log.debug("Finding all GeoShapes");
    
    return geoShapeRepository.findAll().stream()
      .map(geoShapeMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds all active GeoShapes.
   *
   * @return list of active GeoShapes
   */
  public List<GeoShape> findAllActive() {
    log.debug("Finding all active GeoShapes");
    
    return geoShapeRepository.findByActiveTrue().stream()
      .map(geoShapeMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds all inactive GeoShapes.
   *
   * @return list of inactive GeoShapes
   */
  public List<GeoShape> findAllInactive() {
    log.debug("Finding all inactive GeoShapes");
    
    return geoShapeRepository.findByActiveFalse().stream()
      .map(geoShapeMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds GeoShapes by active status with pagination.
   *
   * @param active the active status
   * @param pageable pagination information
   * @return page of GeoShapes
   */
  public Page<GeoShape> findByActive(Boolean active, Pageable pageable) {
    log.debug("Finding GeoShapes by active status: {} with pagination", active);
    
    return geoShapeRepository.findByActive(active, pageable)
      .map(geoShapeMapper::toDomain);
  }

  /**
   * Finds GeoShapes where the name contains the given substring (case-insensitive).
   *
   * @param namePart the substring to search for
   * @return list of matching GeoShapes
   */
  public List<GeoShape> findByNameContaining(String namePart) {
    log.debug("Finding GeoShapes by name containing: {}", namePart);
    
    return geoShapeRepository.findByNameContainingIgnoreCase(namePart).stream()
      .map(geoShapeMapper::toDomain)
      .collect(Collectors.toList());
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

  // ================================================================
  // Temporal Queries
  // ================================================================

  /**
   * Finds GeoShapes created within a time range.
   *
   * @param start start date/time
   * @param end end date/time
   * @return list of GeoShapes
   */
  public List<GeoShape> findByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end) {
    log.debug("Finding GeoShapes created between {} and {}", start, end);
    
    return geoShapeRepository.findByCreatedAtBetween(start, end).stream()
      .map(geoShapeMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds the 10 most recently updated GeoShapes.
   *
   * @return list of recently updated GeoShapes
   */
  public List<GeoShape> findRecentlyUpdated() {
    log.debug("Finding recently updated GeoShapes");
    
    return geoShapeRepository.findTop10ByOrderByUpdatedAtDesc(PageRequest.of(0, 10)).stream()
      .map(geoShapeMapper::toDomain)
      .collect(Collectors.toList());
  }

  // ================================================================
  // Update Operations
  // ================================================================

  /**
   * Updates an existing GeoShape.
   *
   * @param id the GeoShape ID
   * @param geoShape the updated GeoShape data
   * @return the updated GeoShape
   * @throws IllegalArgumentException if GeoShape not found
   */
  @Transactional
  public GeoShape update(UUID id, GeoShape geoShape) {
    log.debug("Updating GeoShape with ID: {}", id);
    
    GeoShapeEntity existingEntity = geoShapeRepository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("GeoShape not found with ID: " + id));
    
    GeoShapeEntity updatedEntity = geoShapeMapper.toEntity(geoShape);
    updatedEntity.setId(existingEntity.getId());
    updatedEntity.setCreatedAt(existingEntity.getCreatedAt());
    
    GeoShapeEntity savedEntity = geoShapeRepository.save(updatedEntity);
    
    log.info("GeoShape updated with ID: {}", savedEntity.getId());
    return geoShapeMapper.toDomain(savedEntity);
  }

  // ================================================================
  // Delete Operations
  // ================================================================

  /**
   * Deletes a GeoShape by ID.
   *
   * @param id the GeoShape ID
   */
  @Transactional
  public void deleteById(UUID id) {
    log.debug("Deleting GeoShape with ID: {}", id);
    
    if (!geoShapeRepository.existsById(id)) {
      throw new IllegalArgumentException("GeoShape not found with ID: " + id);
    }
    
    geoShapeRepository.deleteById(id);
    log.info("GeoShape deleted with ID: {}", id);
  }

  // ================================================================
  // Existence Checks
  // ================================================================

  /**
   * Checks if a GeoShape exists by ID.
   *
   * @param id the GeoShape ID
   * @return true if exists, false otherwise
   */
  public boolean existsById(UUID id) {
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

  /**
   * Checks if an active GeoShape exists by name.
   *
   * @param name the GeoShape name
   * @return true if exists, false otherwise
   */
  public boolean existsActiveByName(String name) {
    return geoShapeRepository.existsByNameAndActiveTrue(name);
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
}
