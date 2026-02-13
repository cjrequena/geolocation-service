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
 * <p>Area is a subdivision of a City.</p>
 */
@Repository
public interface AreaRepository extends JpaRepository<AreaEntity, UUID> {

  // ================================================================
  // Active / Inactive filtering
  // ================================================================

  List<AreaEntity> findAllByActiveTrue();

  List<AreaEntity> findAllByActiveFalse();

  Page<AreaEntity> findByActive(Boolean active, Pageable pageable);

  // ================================================================
  // Parent navigation — City
  // ================================================================

  @Query("SELECT a FROM AreaEntity a WHERE a.city.id = :cityId")
  List<AreaEntity> findByCityId(@Param("cityId") UUID cityId);

  @Query("SELECT a FROM AreaEntity a WHERE a.city.id = :cityId AND a.active = true")
  List<AreaEntity> findByCityIdAndActiveTrue(@Param("cityId") UUID cityId);

  @Query("SELECT a FROM AreaEntity a WHERE a.city.id = :cityId")
  Page<AreaEntity> findByCityId(@Param("cityId") UUID cityId, Pageable pageable);

  // ================================================================
  // GeoShape association
  // ================================================================

  @Query("SELECT a FROM AreaEntity a WHERE a.geoShape.id = :geoShapeId")
  List<AreaEntity> findByGeoShapeId(@Param("geoShapeId") UUID geoShapeId);

  @Query("SELECT a FROM AreaEntity a WHERE a.geoShape IS NULL")
  List<AreaEntity> findByGeoShapeIsNull();

  @Query("SELECT a FROM AreaEntity a WHERE a.geoShape IS NOT NULL")
  List<AreaEntity> findByGeoShapeIsNotNull();

  // ================================================================
  // Area type filtering
  // ================================================================

  @Query("SELECT a FROM AreaEntity a WHERE a.areaType = :areaType")
  List<AreaEntity> findByAreaType(@Param("areaType") String areaType);

  @Query("SELECT a FROM AreaEntity a WHERE a.areaType = :areaType AND a.active = true")
  List<AreaEntity> findByAreaTypeAndActiveTrue(@Param("areaType") String areaType);

  @Query("""
    SELECT a
    FROM AreaEntity a
    WHERE a.city.id = :cityId
      AND a.areaType = :areaType
  """)
  List<AreaEntity> findByCityIdAndAreaType(
    @Param("cityId") UUID cityId,
    @Param("areaType") String areaType
  );

  // ================================================================
  // Postal code queries
  // ================================================================

  @Query("SELECT a FROM AreaEntity a WHERE a.postalCode = :postalCode")
  List<AreaEntity> findByPostalCode(@Param("postalCode") String postalCode);

  @Query("SELECT a FROM AreaEntity a WHERE a.postalCode = :postalCode AND a.active = true")
  List<AreaEntity> findByPostalCodeAndActiveTrue(@Param("postalCode") String postalCode);

  @Query("""
    SELECT a
    FROM AreaEntity a
    WHERE a.city.id = :cityId
      AND a.postalCode = :postalCode
  """)
  List<AreaEntity> findByCityIdAndPostalCode(
    @Param("cityId") UUID cityId,
    @Param("postalCode") String postalCode
  );

  // ================================================================
  // Name-based queries
  // ================================================================

  @Query("""
    SELECT a
    FROM AreaEntity a
    WHERE a.city.id = :cityId
      AND a.name = :name
  """)
  Optional<AreaEntity> findByCityIdAndName(
    @Param("cityId") UUID cityId,
    @Param("name") String name
  );

  @Query("""
    SELECT a
    FROM AreaEntity a
    WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :namePart, '%'))
  """)
  List<AreaEntity> findByNameContainingIgnoreCase(@Param("namePart") String namePart);

  @Query("""
    SELECT a
    FROM AreaEntity a
    WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :namePart, '%'))
      AND a.active = true
  """)
  List<AreaEntity> findByNameContainingIgnoreCaseAndActiveTrue(
    @Param("namePart") String namePart
  );

  // ================================================================
  // Population queries
  // ================================================================

  @Query("""
    SELECT a
    FROM AreaEntity a
    WHERE a.city.id = :cityId
      AND a.population > :minPopulation
  """)
  List<AreaEntity> findByCityIdAndPopulationGreaterThan(
    @Param("cityId") UUID cityId,
    @Param("minPopulation") Long minPopulation
  );

  @Query("""
    SELECT a
    FROM AreaEntity a
    WHERE a.city.id = :cityId
    ORDER BY a.population DESC
  """)
  Page<AreaEntity> findByCityIdOrderByPopulationDesc(
    @Param("cityId") UUID cityId,
    Pageable pageable
  );

  // ================================================================
  // Audit / Temporal queries
  // ================================================================

  @Query("""
    SELECT a
    FROM AreaEntity a
    WHERE a.createdAt BETWEEN :start AND :end
  """)
  List<AreaEntity> findByCreatedAtBetween(
    @Param("start") OffsetDateTime start,
    @Param("end") OffsetDateTime end
  );

  @Query("""
    SELECT a
    FROM AreaEntity a
    ORDER BY a.updatedAt DESC
  """)
  List<AreaEntity> findTop10ByOrderByUpdatedAtDesc(Pageable pageable);

  // ================================================================
  // Existence checks
  // ================================================================

  @Query("""
    SELECT COUNT(a) > 0
    FROM AreaEntity a
    WHERE a.city.id = :cityId
      AND a.name = :name
  """)
  boolean existsByCityIdAndName(
    @Param("cityId") UUID cityId,
    @Param("name") String name
  );

  @Query("""
    SELECT COUNT(a) > 0
    FROM AreaEntity a
    WHERE a.city.id = :cityId
      AND a.name = :name
      AND a.active = true
  """)
  boolean existsByCityIdAndNameAndActiveTrue(
    @Param("cityId") UUID cityId,
    @Param("name") String name
  );

  @Query("""
    SELECT COUNT(a) > 0
    FROM AreaEntity a
    WHERE a.postalCode = :postalCode
  """)
  boolean existsByPostalCode(@Param("postalCode") String postalCode);
}
