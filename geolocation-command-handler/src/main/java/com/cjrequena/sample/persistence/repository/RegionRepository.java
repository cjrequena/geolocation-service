package com.cjrequena.sample.persistence.repository;

import com.cjrequena.sample.persistence.entity.RegionEntity;
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
 * Spring Data JPA repository for {@link RegionEntity}.
 *
 * <p>Region is a subdivision of a {@link com.cjrequena.sample.persistence.entity.CountryEntity}.</p>
 */
@Repository
public interface RegionRepository extends JpaRepository<RegionEntity, UUID> {

  // ================================================================
  // Active / Inactive filtering
  // ================================================================

  /**
   * Finds all active regions.
   */
  List<RegionEntity> findAllByActiveTrue();

  /**
   * Finds all inactive regions.
   */
  List<RegionEntity> findAllByActiveFalse();

  /**
   * Finds regions by active  with pagination.
   */
  Page<RegionEntity> findByActive(Boolean active, Pageable pageable);

  // ================================================================
  // Parent navigation — Country
  // ================================================================

  /**
   * Finds all regions belonging to a specific country.
   *
   * @param countryId the country UUID
   * @return all regions in that country
   */
  @Query("SELECT r FROM RegionEntity r WHERE r.country.id = :countryId")
  List<RegionEntity> findByCountryId(@Param("countryId") UUID countryId);

  /**
   * Finds active regions belonging to a specific country.
   */
  @Query("SELECT r FROM RegionEntity r WHERE r.country.id = :countryId AND r.active = true")
  List<RegionEntity> findByCountryIdAndActiveTrue(@Param("countryId") UUID countryId);

  /**
   * Finds regions in a country with pagination.
   */
  @Query("SELECT r FROM RegionEntity r WHERE r.country.id = :countryId")
  Page<RegionEntity> findByCountryId(@Param("countryId") UUID countryId, Pageable pageable);

  // ================================================================
  // GeoShape association
  // ================================================================

  /**
   * Finds regions associated with a specific GeoShape.
   */
  @Query("SELECT r FROM RegionEntity r WHERE r.geoShape.id = :geoShapeId")
  List<RegionEntity> findByGeoShapeId(@Param("geoShapeId") UUID geoShapeId);

  /**
   * Finds regions that have no associated GeoShape.
   */
  @Query("SELECT r FROM RegionEntity r WHERE r.geoShape IS NULL")
  List<RegionEntity> findByGeoShapeIdIsNull();

  /**
   * Finds regions that have an associated GeoShape.
   */
  @Query("SELECT r FROM RegionEntity r WHERE r.geoShape IS NOT NULL")
  List<RegionEntity> findByGeoShapeIdIsNotNull();

  // ================================================================
  // Region type filtering
  // ================================================================

  /**
   * Finds regions by region type (e.g., "STATE", "PROVINCE").
   *
   * @param regionType the string value of the region type enum
   */
  List<RegionEntity> findByRegionType(String regionType);

  /**
   * Finds active regions by region type.
   */
  List<RegionEntity> findByRegionTypeAndActiveTrue(String regionType);

  // ================================================================
  // Name-based queries
  // ================================================================

  /**
   * Finds a region by exact name within a specific country.
   */
  @Query("SELECT r FROM RegionEntity r WHERE r.country.id = :countryId AND r.name = :name")
  Optional<RegionEntity> findByCountryIdAndName(@Param("countryId") UUID countryId, @Param("name") String name);

  /**
   * Finds regions where the name contains the given substring (case-insensitive).
   */
  List<RegionEntity> findByNameContainingIgnoreCase(String namePart);

  /**
   * Finds active regions where the name contains the given substring.
   */
  List<RegionEntity> findByNameContainingIgnoreCaseAndActiveTrue(String namePart);

  // ================================================================
  // Population queries
  // ================================================================

  /**
   * Finds regions in a country with a population greater than the specified value.
   */
  @Query("SELECT r FROM RegionEntity r WHERE r.country.id = :countryId AND r.population > :minPopulation")
  List<RegionEntity> findByCountryIdAndPopulationGreaterThan(
    @Param("countryId") UUID countryId,
    @Param("minPopulation") Long minPopulation
  );

  /**
   * Finds the top N regions in a country ordered by population descending.
   */
  @Query("SELECT r FROM RegionEntity r WHERE r.country.id = :countryId ORDER BY r.population DESC")
  Page<RegionEntity> findByCountryIdOrderByPopulationDesc(
    @Param("countryId") UUID countryId,
    Pageable pageable
  );

  // ================================================================
  // Audit / Temporal queries
  // ================================================================

  /**
   * Finds regions created within a given time range.
   */
  List<RegionEntity> findByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end);

  /**
   * Finds the 10 most recently updated regions.
   */
  List<RegionEntity> findTop10ByOrderByUpdatedAtDesc();

  // ================================================================
  // Existence checks
  // ================================================================

  /**
   * Checks if a region with the given name exists in the specified country.
   */
  @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM RegionEntity r WHERE r.country.id = :countryId AND r.name = :name")
  boolean existsByCountryIdAndName(@Param("countryId") UUID countryId, @Param("name") String name);

  /**
   * Checks if an active region with the given name exists in the specified country.
   */
  @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM RegionEntity r WHERE r.country.id = :countryId AND r.name = :name AND r.active = true")
  boolean existsByCountryIdAndNameAndActiveTrue(
    @Param("countryId") UUID countryId,
    @Param("name") String name
  );
}
