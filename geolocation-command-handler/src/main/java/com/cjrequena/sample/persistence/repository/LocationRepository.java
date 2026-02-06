package com.cjrequena.sample.persistence.repository;

import com.cjrequena.sample.persistence.entity.LocationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link LocationEntity}.
 *
 * <p>Location represents a specific point with precise coordinates, optionally
 * assigned to a {@link com.cjrequena.sample.persistence.entity.ZoneEntity}.</p>
 *
 * <p>Includes spatial queries using PostGIS/Hibernate Spatial for proximity
 * and containment operations.</p>
 *
 * <p><b>Note:</b> Spatial queries use WKT (Well-Known Text) format for geometry parameters
 * to ensure proper parameter binding with PostGIS. Example: "POINT(-3.7038 40.4168)"</p>
 */
@Repository
public interface LocationRepository extends JpaRepository<LocationEntity, UUID> {

  // ================================================================
  // Active / Inactive filtering
  // ================================================================

  /**
   * Finds all active locations.
   */
  List<LocationEntity> findAllByActiveTrue();

  /**
   * Finds all inactive locations.
   */
  List<LocationEntity> findAllByActiveFalse();

  /**
   * Finds locations by active status with pagination.
   */
  Page<LocationEntity> findByActive(Boolean active, Pageable pageable);

  // ================================================================
  // Parent navigation — Zone
  // ================================================================

  /**
   * Finds all locations belonging to a specific zone.
   *
   * @param zoneId the zone UUID
   * @return all locations in that zone
   */
  @Query("SELECT l FROM LocationEntity l WHERE l.zone.id = :zoneId")
  List<LocationEntity> findByZoneId(@Param("zoneId") UUID zoneId);

  /**
   * Finds active locations belonging to a specific zone.
   */
  @Query("SELECT l FROM LocationEntity l WHERE l.zone.id = :zoneId AND l.active = true")
  List<LocationEntity> findByZoneIdAndActiveTrue(@Param("zoneId") UUID zoneId);

  /**
   * Finds locations in a zone with pagination.
   */
  @Query("SELECT l FROM LocationEntity l WHERE l.zone.id = :zoneId")
  Page<LocationEntity> findByZoneId(@Param("zoneId") UUID zoneId, Pageable pageable);

  /**
   * Finds locations that are not assigned to any zone.
   */
  @Query("SELECT l FROM LocationEntity l WHERE l.zone IS NULL")
  List<LocationEntity> findByZoneIdIsNull();

  /**
   * Finds locations that are assigned to a zone.
   */
  @Query("SELECT l FROM LocationEntity l WHERE l.zone IS NOT NULL")
  List<LocationEntity> findByZoneIdIsNotNull();

  // ================================================================
  // Spatial queries — Proximity (radius search)
  // ================================================================

  /**
   * Finds all locations within a given radius (in metres) from a center point.
   *
   * @param wkt            the center point as WKT string (e.g., "POINT(-3.7038 40.4168)")
   * @param radiusMeters   radius in metres
   * @return all locations within the specified distance
   */
  @Query(
    value = """
    SELECT l.*
    FROM geo_schema.location l
    WHERE ST_DWithin(
      l.point::geography,
      ST_GeomFromText(:wkt, 4326)::geography,
      :radius
    )
  """,
    nativeQuery = true
  )
  List<LocationEntity> findWithinRadius(
    @Param("wkt") String wkt,
    @Param("radius") double radiusMeters
  );

  /**
   * Finds active locations within a given radius from a center point.
   */
  @Query(
    value = """
    SELECT l.*
    FROM geo_schema.location l
    WHERE l.active = true
      AND ST_DWithin(
        l.point::geography,
        ST_GeomFromText(:wkt, 4326)::geography,
        :radius
      )
  """,
    nativeQuery = true
  )
  List<LocationEntity> findActiveWithinRadius(
    @Param("wkt") String wkt,
    @Param("radius") double radiusMeters
  );

  /**
   * Finds locations within a radius, ordered by distance ascending.
   */
  @Query(
    value = """
    SELECT l.*
    FROM geo_schema.location l
    WHERE ST_DWithin(
      l.point::geography,
      ST_GeomFromText(:wkt, 4326)::geography,
      :radius
    )
    ORDER BY ST_Distance(
      l.point::geography,
      ST_GeomFromText(:wkt, 4326)::geography
    )
  """,
    nativeQuery = true
  )
  List<LocationEntity> findWithinRadiusOrderedByDistance(
    @Param("wkt") String wkt,
    @Param("radius") double radiusMeters
  );

  /**
   * Finds the N nearest locations to a center point (with a max search radius).
   *
   * @param wkt          the center point as WKT string
   * @param maxRadius    the maximum search radius in metres
   * @param pageable     use {@code PageRequest.of(0, n)} to limit results
   */
  @Query(
    value = """
    SELECT l.*
    FROM geo_schema.location l
    WHERE ST_DWithin(
      l.point::geography,
      ST_GeomFromText(:wkt, 4326)::geography,
      :maxRadius
    )
    ORDER BY ST_Distance(
      l.point::geography,
      ST_GeomFromText(:wkt, 4326)::geography
    )
  """,
    countQuery = """
    SELECT COUNT(*)
    FROM geo_schema.location l
    WHERE ST_DWithin(
      l.point::geography,
      ST_GeomFromText(:wkt, 4326)::geography,
      :maxRadius
    )
  """,
    nativeQuery = true
  )
  Page<LocationEntity> findNearestLocations(
    @Param("wkt") String wkt,
    @Param("maxRadius") double maxRadius,
    Pageable pageable
  );

  // ================================================================
  // Spatial queries — Polygon containment
  // ================================================================

  /**
   * Finds all locations within a given polygon.
   *
   * @param wkt the bounding polygon as WKT string (e.g., "POLYGON((-3.72 40.41, -3.68 40.41, -3.68 40.42, -3.72 40.42, -3.72 40.41))")
   * @return all locations whose point falls within the polygon
   */
  @Query(
    value = """
    SELECT l.*
    FROM geo_schema.location l
    WHERE ST_Within(l.point, ST_GeomFromText(:wkt, 4326))
  """,
    nativeQuery = true
  )
  List<LocationEntity> findWithinPolygon(@Param("wkt") String wkt);

  /**
   * Finds active locations within a given polygon.
   */
  @Query(
    value = """
    SELECT l.*
    FROM geo_schema.location l
    WHERE l.active = true
      AND ST_Within(l.point, ST_GeomFromText(:wkt, 4326))
  """,
    nativeQuery = true
  )
  List<LocationEntity> findActiveWithinPolygon(@Param("wkt") String wkt);

  // ================================================================
  // Postal code queries
  // ================================================================

  /**
   * Finds locations by postal code.
   */
  List<LocationEntity> findByPostalCode(String postalCode);

  /**
   * Finds active locations by postal code.
   */
  List<LocationEntity> findByPostalCodeAndActiveTrue(String postalCode);

  // ================================================================
  // Address-based queries
  // ================================================================

  /**
   * Finds locations where the address contains the given substring (case-insensitive).
   */
  List<LocationEntity> findByAddressContainingIgnoreCase(String addressPart);

  /**
   * Finds active locations where the address contains the given substring.
   */
  List<LocationEntity> findByAddressContainingIgnoreCaseAndActiveTrue(String addressPart);

  // ================================================================
  // Altitude / Accuracy filtering
  // ================================================================

  /**
   * Finds locations with an altitude greater than the specified value (in metres).
   */
  @Query("SELECT l FROM LocationEntity l WHERE l.altitudeMeters > :minAltitude")
  List<LocationEntity> findByAltitudeGreaterThan(@Param("minAltitude") BigDecimal minAltitude);

  /**
   * Finds locations with an altitude between the specified range (in metres).
   */
  @Query("SELECT l FROM LocationEntity l WHERE l.altitudeMeters BETWEEN :minAltitude AND :maxAltitude")
  List<LocationEntity> findByAltitudeBetween(
    @Param("minAltitude") BigDecimal minAltitude,
    @Param("maxAltitude") BigDecimal maxAltitude
  );

  /**
   * Finds locations with GPS accuracy better than (less than or equal to) the specified value (in metres).
   */
  @Query("SELECT l FROM LocationEntity l WHERE l.accuracyMeters <= :maxAccuracy")
  List<LocationEntity> findByAccuracyBetterThan(@Param("maxAccuracy") BigDecimal maxAccuracy);

  /**
   * Finds locations that have altitude information.
   */
  @Query("SELECT l FROM LocationEntity l WHERE l.altitudeMeters IS NOT NULL")
  List<LocationEntity> findWithAltitude();

  /**
   * Finds locations that have GPS accuracy information.
   */
  @Query("SELECT l FROM LocationEntity l WHERE l.accuracyMeters IS NOT NULL")
  List<LocationEntity> findWithAccuracy();

  // ================================================================
  // Combined spatial + zone queries
  // ================================================================

  /**
   * Finds locations within a zone that are also within a given radius from a point.
   */
  @Query(
    value = """
    SELECT l.*
    FROM geo_schema.location l
    WHERE l.zone_id = :zoneId
      AND ST_DWithin(
        l.point::geography,
        ST_GeomFromText(:wkt, 4326)::geography,
        :radius
      )
  """,
    nativeQuery = true
  )
  List<LocationEntity> findByZoneIdAndWithinRadius(
    @Param("zoneId") UUID zoneId,
    @Param("wkt") String wkt,
    @Param("radius") double radiusMeters
  );

  // ================================================================
  // Audit / Temporal queries
  // ================================================================

  /**
   * Finds locations created within a given time range.
   */
  List<LocationEntity> findByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end);

  /**
   * Finds the 10 most recently updated locations.
   */
  List<LocationEntity> findTop10ByOrderByUpdatedAtDesc();

  /**
   * Finds the most recently created locations.
   */
  List<LocationEntity> findTop10ByOrderByCreatedAtDesc();

  // ================================================================
  // Existence checks
  // ================================================================

  /**
   * Checks if an active location exists within a very small radius of the given point
   * (useful for duplicate detection).
   */
  @Query(
    value = """
    SELECT EXISTS (
      SELECT 1
      FROM geo_schema.location l
      WHERE l.active = true
        AND ST_DWithin(
          l.point::geography,
          ST_GeomFromText(:wkt, 4326)::geography,
          :threshold
        )
    )
  """,
    nativeQuery = true
  )
  boolean existsActiveNearPoint(
    @Param("wkt") String wkt,
    @Param("threshold") double thresholdMeters
  );
}
