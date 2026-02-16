package com.cjrequena.sample.persistence.repository;

import com.cjrequena.sample.persistence.entity.ZoneEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link ZoneEntity}.
 *
 * <p>Zone is a subdivision of an {@link com.cjrequena.sample.persistence.entity.AreaEntity}.</p>
 */
@Repository
public interface ZoneRepository extends JpaRepository<ZoneEntity, UUID>, JpaSpecificationExecutor<ZoneEntity>, QuerydslPredicateExecutor<ZoneEntity> {

  // ================================================================
  // Active / Inactive filtering
  // ================================================================

  /**
   * Finds all active zones.
   */
  List<ZoneEntity> findAllByActiveTrue();

  /**
   * Finds all inactive zones.
   */
  List<ZoneEntity> findAllByActiveFalse();

  /**
   * Finds zones by active status with pagination.
   */
  Page<ZoneEntity> findByActive(Boolean active, Pageable pageable);

  // ================================================================
  // Parent navigation — Area
  // ================================================================

  /**
   * Finds all zones belonging to a specific area.
   *
   * @param areaId the area UUID
   * @return all zones in that area
   */
  @Query("SELECT z FROM ZoneEntity z WHERE z.area.id = :areaId")
  List<ZoneEntity> findByAreaId(@Param("areaId") UUID areaId);

  /**
   * Finds active zones belonging to a specific area.
   */
  @Query("SELECT z FROM ZoneEntity z WHERE z.area.id = :areaId AND z.active = true")
  List<ZoneEntity> findByAreaIdAndActiveTrue(@Param("areaId") UUID areaId);

  /**
   * Finds zones in an area with pagination.
   */
  @Query("SELECT z FROM ZoneEntity z WHERE z.area.id = :areaId")
  Page<ZoneEntity> findByAreaId(@Param("areaId") UUID areaId, Pageable pageable);

  // ================================================================
  // GeoShape association
  // ================================================================

  /**
   * Finds zones associated with a specific GeoShape.
   */
  @Query("SELECT z FROM ZoneEntity z WHERE z.geoShape.id = :geoShapeId")
  List<ZoneEntity> findByGeoShapeId(@Param("geoShapeId") UUID geoShapeId);

  /**
   * Finds zones that have no associated GeoShape.
   */
  @Query("SELECT z FROM ZoneEntity z WHERE z.geoShape IS NULL")
  List<ZoneEntity> findByGeoShapeIdIsNull();

  /**
   * Finds zones that have an associated GeoShape.
   */
  @Query("SELECT z FROM ZoneEntity z WHERE z.geoShape IS NOT NULL")
  List<ZoneEntity> findByGeoShapeIdIsNotNull();

  // ================================================================
  // Zone type filtering
  // ================================================================

  /**
   * Finds zones by zone type (e.g., "PARK", "INDUSTRIAL", "RESIDENTIAL").
   *
   * @param zoneType the string value of the zone type enum
   */
  List<ZoneEntity> findByZoneType(String zoneType);

  /**
   * Finds active zones by zone type.
   */
  List<ZoneEntity> findByZoneTypeAndActiveTrue(String zoneType);

  /**
   * Finds zones in an area filtered by zone type.
   */
  @Query("SELECT z FROM ZoneEntity z WHERE z.area.id = :areaId AND z.zoneType = :zoneType")
  List<ZoneEntity> findByAreaIdAndZoneType(
    @Param("areaId") UUID areaId,
    @Param("zoneType") String zoneType
  );

  // ================================================================
  // Postal code queries
  // ================================================================

  /**
   * Finds zones by postal code.
   */
  List<ZoneEntity> findByPostalCode(String postalCode);

  /**
   * Finds active zones by postal code.
   */
  List<ZoneEntity> findByPostalCodeAndActiveTrue(String postalCode);

  /**
   * Finds zones in an area filtered by postal code.
   */
  @Query("SELECT z FROM ZoneEntity z WHERE z.area.id = :areaId AND z.postalCode = :postalCode")
  List<ZoneEntity> findByAreaIdAndPostalCode(
    @Param("areaId") UUID areaId,
    @Param("postalCode") String postalCode
  );

  // ================================================================
  // Name-based queries
  // ================================================================

  /**
   * Finds a zone by exact name within a specific area.
   */
  @Query("SELECT z FROM ZoneEntity z WHERE z.area.id = :areaId AND z.name = :name")
  Optional<ZoneEntity> findByAreaIdAndName(@Param("areaId") UUID areaId, @Param("name") String name);

  /**
   * Finds zones where the name contains the given substring (case-insensitive).
   */
  List<ZoneEntity> findByNameContainingIgnoreCase(String namePart);

  /**
   * Finds active zones where the name contains the given substring.
   */
  List<ZoneEntity> findByNameContainingIgnoreCaseAndActiveTrue(String namePart);

  // ================================================================
  // Audit / Temporal queries
  // ================================================================

  /**
   * Finds zones created within a given time range.
   */
  List<ZoneEntity> findByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end);

  /**
   * Finds the 10 most recently updated zones.
   */
  List<ZoneEntity> findTop10ByOrderByUpdatedAtDesc();

  // ================================================================
  // Existence checks
  // ================================================================

  /**
   * Checks if a zone with the given name exists in the specified area.
   */
  @Query("SELECT CASE WHEN COUNT(z) > 0 THEN true ELSE false END FROM ZoneEntity z WHERE z.area.id = :areaId AND z.name = :name")
  boolean existsByAreaIdAndName(@Param("areaId") UUID areaId, @Param("name") String name);

  /**
   * Checks if an active zone with the given name exists in the specified area.
   */
  @Query("SELECT CASE WHEN COUNT(z) > 0 THEN true ELSE false END FROM ZoneEntity z WHERE z.area.id = :areaId AND z.name = :name AND z.active = true")
  boolean existsByAreaIdAndNameAndActiveTrue(
    @Param("areaId") UUID areaId,
    @Param("name") String name
  );

  /**
   * Checks if a zone with the given postal code exists.
   */
  boolean existsByPostalCode(String postalCode);
}
