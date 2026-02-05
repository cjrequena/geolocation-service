package com.cjrequena.sample.persistence.repository;

import com.cjrequena.sample.persistence.entity.AreaEntity;
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
 * Spring Data JPA repository for {@link AreaEntity}.
 *
 * <p>Area is a subdivision of a {@link com.cjrequena.sample.persistence.entity.CityEntity}.</p>
 */
@Repository
public interface AreaRepository extends JpaRepository<AreaEntity, UUID> {

  // ================================================================
  // Active / Inactive filtering
  // ================================================================

  /**
   * Finds all active areas.
   */
  @Query(
    value = "SELECT * FROM area WHERE active = true",
    nativeQuery = true
  )
  List<AreaEntity> findAllActive();

  /**
   * Finds all inactive areas.
   */
  @Query(
    value = "SELECT * FROM area WHERE active = false",
    nativeQuery = true
  )
  List<AreaEntity> findAllInactive();

  /**
   * Finds areas by active status with pagination.
   */
  @Query(
    value = "SELECT * FROM area WHERE active = :active",
    countQuery = "SELECT COUNT(*) FROM area WHERE active = :active",
    nativeQuery = true
  )
  Page<AreaEntity> findByActive(
    @Param("active") Boolean active,
    Pageable pageable
  );

  // ================================================================
  // Parent navigation — City
  // ================================================================

  /**
   * Finds all areas belonging to a specific city.
   *
   * @param cityId the city UUID
   * @return all areas in that city
   */
  @Query("SELECT a FROM AreaEntity a WHERE a.city.id = :cityId")
  List<AreaEntity> findByCityId(@Param("cityId") UUID cityId);

  /**
   * Finds active areas belonging to a specific city.
   */
  @Query("SELECT a FROM AreaEntity a WHERE a.city.id = :cityId AND a.active = true")
  List<AreaEntity> findByCityIdAndActiveTrue(@Param("cityId") UUID cityId);

  /**
   * Finds areas in a city with pagination.
   */
  @Query("SELECT a FROM AreaEntity a WHERE a.city.id = :cityId")
  Page<AreaEntity> findByCityId(@Param("cityId") UUID cityId, Pageable pageable);

  // ================================================================
  // GeoShape association
  // ================================================================

  /**
   * Finds areas associated with a specific GeoShape.
   */
  @Query(
    value = "SELECT * FROM geo_schema.area WHERE geoshape_id = :geoShapeId",
    nativeQuery = true
  )
  List<AreaEntity> findByGeoShapeId(@Param("geoShapeId") UUID geoShapeId);

  /**
   * Finds areas that have no associated GeoShape.
   */
  @Query(
    value = "SELECT * FROM geo_schema.area WHERE geoshape_id IS NULL",
    nativeQuery = true
  )
  List<AreaEntity> findByGeoShapeIdIsNull();

  /**
   * Finds areas that have an associated GeoShape.
   */
  @Query(
    value = "SELECT * FROM geo_schema.area WHERE geoshape_id IS NOT NULL",
    nativeQuery = true
  )
  List<AreaEntity> findByGeoShapeIdIsNotNull();

  // ================================================================
  // Area type filtering
  // ================================================================

  /**
   * Finds areas by area type (e.g., "DISTRICT", "NEIGHBORHOOD").
   */
  @Query(
    value = "SELECT * FROM geo_schema.area WHERE area_type = :areaType",
    nativeQuery = true
  )
  List<AreaEntity> findByAreaType(@Param("areaType") String areaType);

  /**
   * Finds active areas by area type.
   */
  @Query(
    value = "SELECT * FROM geo_schema.area WHERE area_type = :areaType AND active = true",
    nativeQuery = true
  )
  List<AreaEntity> findByAreaTypeAndActiveTrue(@Param("areaType") String areaType);

  /**
   * Finds areas in a city filtered by area type.
   */
  @Query("SELECT a FROM AreaEntity a WHERE a.city.id = :cityId AND a.areaType = :areaType")
  List<AreaEntity> findByCityIdAndAreaType(
    @Param("cityId") UUID cityId,
    @Param("areaType") String areaType
  );

  // ================================================================
  // Postal code queries
  // ================================================================

  /**
   * Finds areas by postal code.
   */
  @Query(
    value = "SELECT * FROM geo_schema.area WHERE postal_code = :postalCode",
    nativeQuery = true
  )
  List<AreaEntity> findByPostalCode(@Param("postalCode") String postalCode);

  /**
   * Finds active areas by postal code.
   */
  @Query(
    value = "SELECT * FROM geo_schema.area WHERE postal_code = :postalCode AND active = true",
    nativeQuery = true
  )
  List<AreaEntity> findByPostalCodeAndActiveTrue(@Param("postalCode") String postalCode);

  /**
   * Finds areas in a city filtered by postal code.
   */
  @Query("SELECT a FROM AreaEntity a WHERE a.city.id = :cityId AND a.postalCode = :postalCode")
  List<AreaEntity> findByCityIdAndPostalCode(
    @Param("cityId") UUID cityId,
    @Param("postalCode") String postalCode
  );

  // ================================================================
  // Name-based queries
  // ================================================================

  /**
   * Finds an area by exact name within a specific city.
   */
  @Query("SELECT a FROM AreaEntity a WHERE a.city.id = :cityId AND a.name = :name")
  Optional<AreaEntity> findByCityIdAndName(@Param("cityId") UUID cityId, @Param("name") String name);

  /**
   * Finds areas where the name contains the given substring (case-insensitive).
   */
  @Query(
    value = """
    SELECT *
    FROM geo_schema.area
    WHERE LOWER(name) LIKE LOWER(CONCAT('%', :namePart, '%'))
  """,
    nativeQuery = true
  )
  List<AreaEntity> findByNameContainingIgnoreCase(@Param("namePart") String namePart);

  /**
   * Finds active areas where the name contains the given substring.
   */
  @Query(
    value = """
    SELECT *
    FROM geo_schema.area
    WHERE LOWER(name) LIKE LOWER(CONCAT('%', :namePart, '%'))
      AND active = true
  """,
    nativeQuery = true
  )
  List<AreaEntity> findByNameContainingIgnoreCaseAndActiveTrue(@Param("namePart") String namePart);

  // ================================================================
  // Population queries
  // ================================================================

  /**
   * Finds areas in a city with a population greater than the specified value.
   */
  @Query("SELECT a FROM AreaEntity a WHERE a.city.id = :cityId AND a.population > :minPopulation")
  List<AreaEntity> findByCityIdAndPopulationGreaterThan(
    @Param("cityId") UUID cityId,
    @Param("minPopulation") Long minPopulation
  );

  /**
   * Finds the top N areas in a city ordered by population descending.
   */
  @Query("SELECT a FROM AreaEntity a WHERE a.city.id = :cityId ORDER BY a.population DESC")
  Page<AreaEntity> findByCityIdOrderByPopulationDesc(
    @Param("cityId") UUID cityId,
    Pageable pageable
  );

  // ================================================================
  // Audit / Temporal queries
  // ================================================================

  /**
   * Finds areas created within a given time range.
   */
  @Query(
    value = """
    SELECT *
    FROM geo_schema.area
    WHERE created_at BETWEEN :start AND :end
  """,
    nativeQuery = true
  )
  List<AreaEntity> findByCreatedAtBetween(
    @Param("start") OffsetDateTime start,
    @Param("end") OffsetDateTime end
  );

  /**
   * Finds the 10 most recently updated areas.
   */
  @Query(
    value = """
    SELECT *
    FROM geo_schema.area
    ORDER BY updated_at DESC
    LIMIT 10
  """,
    nativeQuery = true
  )
  List<AreaEntity> findTop10ByOrderByUpdatedAtDesc();

  // ================================================================
  // Existence checks
  // ================================================================

  /**
   * Checks if an area with the given name exists in the specified city.
   */
  @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM AreaEntity a WHERE a.city.id = :cityId AND a.name = :name")
  boolean existsByCityIdAndName(@Param("cityId") UUID cityId, @Param("name") String name);

  /**
   * Checks if an active area with the given name exists in the specified city.
   */
  @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM AreaEntity a WHERE a.city.id = :cityId AND a.name = :name AND a.active = true")
  boolean existsByCityIdAndNameAndActiveTrue(
    @Param("cityId") UUID cityId,
    @Param("name") String name
  );

  /**
   * Checks if an area with the given postal code exists.
   */
  @Query(
    value = """
    SELECT EXISTS(
      SELECT 1
      FROM geo_schema.area
      WHERE postal_code = :postalCode
    )
  """,
    nativeQuery = true
  )
  boolean existsByPostalCode(@Param("postalCode") String postalCode);
}
