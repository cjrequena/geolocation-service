package com.cjrequena.sample.persistence.repository;

import com.cjrequena.sample.persistence.entity.CityEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link CityEntity}.
 *
 * <p>City is a subdivision of a {@link com.cjrequena.sample.persistence.entity.RegionEntity}.</p>
 */
@Repository
public interface CityRepository extends JpaRepository<CityEntity, UUID> {

  // ================================================================
  // Active / Inactive filtering
  // ================================================================

  /**
   * Finds all active cities.
   */
  List<CityEntity> findAllByActiveTrue();

  /**
   * Finds all inactive cities.
   */
  List<CityEntity> findAllByActiveFalse();

  /**
   * Finds cities by active with pagination.
   */
  Page<CityEntity> findByActive(Boolean active, Pageable pageable);

  // ================================================================
  // Parent navigation — Region
  // ================================================================

  /**
   * Finds all cities belonging to a specific region.
   *
   * @param regionId the region UUID
   * @return all cities in that region
   */
  @Query("SELECT c FROM CityEntity c WHERE c.region.id = :regionId")
  List<CityEntity> findByRegionId(@Param("regionId") UUID regionId);

  /**
   * Finds active cities belonging to a specific region.
   */
  @Query("SELECT c FROM CityEntity c WHERE c.region.id = :regionId AND c.active = true")
  List<CityEntity> findByRegionIdAndActiveTrue(@Param("regionId") UUID regionId);

  /**
   * Finds cities in a region with pagination.
   */
  @Query("SELECT c FROM CityEntity c WHERE c.region.id = :regionId")
  Page<CityEntity> findByRegionId(@Param("regionId") UUID regionId, Pageable pageable);

  // ================================================================
  // GeoShape association
  // ================================================================

  /**
   * Finds cities associated with a specific GeoShape.
   */
  @Query("SELECT c FROM CityEntity c WHERE c.geoShape.id = :geoShapeId")
  List<CityEntity> findByGeoShapeId(@Param("geoShapeId") UUID geoShapeId);

  /**
   * Finds cities that have no associated GeoShape.
   */
  @Query("SELECT c FROM CityEntity c WHERE c.geoShape IS NULL")
  List<CityEntity> findByGeoShapeIdIsNull();

  /**
   * Finds cities that have an associated GeoShape.
   */
  @Query("SELECT c FROM CityEntity c WHERE c.geoShape IS NOT NULL")
  List<CityEntity> findByGeoShapeIdIsNotNull();

  // ================================================================
  // Capital city queries
  // ================================================================

  /**
   * Finds all cities marked as capitals.
   */
  List<CityEntity> findAllByCapitalTrue();

  /**
   * Finds all active cities marked as capitals.
   */
  List<CityEntity> findAllByCapitalTrueAndActiveTrue();

  /**
   * Finds the capital city of a specific region (if any).
   */
  @Query("SELECT c FROM CityEntity c WHERE c.region.id = :regionId AND c.capital = true")
  Optional<CityEntity> findCapitalByRegionId(@Param("regionId") UUID regionId);

  // ================================================================
  // TimeZone queries
  // ================================================================

  /**
   * Finds cities in a specific time zone.
   *
   * @param timeZone IANA time zone ID (e.g., "America/New_York")
   */
  List<CityEntity> findByTimeZone(String timeZone);

  /**
   * Finds active cities in a specific time zone.
   */
  List<CityEntity> findByTimeZoneAndActiveTrue(String timeZone);

  // ================================================================
  // Name-based queries
  // ================================================================

  /**
   * Finds a city by exact name within a specific region.
   */
  @Query("SELECT c FROM CityEntity c WHERE c.region.id = :regionId AND c.name = :name")
  Optional<CityEntity> findByRegionIdAndName(@Param("regionId") UUID regionId, @Param("name") String name);

  /**
   * Finds cities where the name contains the given substring (case-insensitive).
   */
  List<CityEntity> findByNameContainingIgnoreCase(String namePart);

  /**
   * Finds active cities where the name contains the given substring.
   */
  List<CityEntity> findByNameContainingIgnoreCaseAndActiveTrue(String namePart);

  // ================================================================
  // Population queries
  // ================================================================

  /**
   * Finds cities in a region with a population greater than the specified value.
   */
  @Query("SELECT c FROM CityEntity c WHERE c.region.id = :regionId AND c.population > :minPopulation")
  List<CityEntity> findByRegionIdAndPopulationGreaterThan(
    @Param("regionId") UUID regionId,
    @Param("minPopulation") Long minPopulation
  );

  /**
   * Finds the top N cities in a region ordered by population descending.
   */
  @Query("SELECT c FROM CityEntity c WHERE c.region.id = :regionId ORDER BY c.population DESC")
  Page<CityEntity> findByRegionIdOrderByPopulationDesc(
    @Param("regionId") UUID regionId,
    Pageable pageable
  );

  /**
   * Finds the top N cities globally ordered by population descending.
   */
  Page<CityEntity> findAllByOrderByPopulationDesc(Pageable pageable);

  // ================================================================
  // Audit / Temporal queries
  // ================================================================

  /**
   * Finds cities created within a given time range.
   */
  List<CityEntity> findByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end);

  /**
   * Finds the 10 most recently updated cities.
   */
  List<CityEntity> findTop10ByOrderByUpdatedAtDesc();

  // ================================================================
  // Existence checks
  // ================================================================

  /**
   * Checks if a city with the given name exists in the specified region.
   */
  @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM CityEntity c WHERE c.region.id = :regionId AND c.name = :name")
  boolean existsByRegionIdAndName(@Param("regionId") UUID regionId, @Param("name") String name);

  /**
   * Checks if an active city with the given name exists in the specified region.
   */
  @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM CityEntity c WHERE c.region.id = :regionId AND c.name = :name AND c.active = true")
  boolean existsByRegionIdAndNameAndActiveTrue(
    @Param("regionId") UUID regionId,
    @Param("name") String name
  );
}
