package com.cjrequena.sample.persistence.repository;

import com.cjrequena.sample.persistence.entity.GeoShapeEntity;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
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
 * Spring Data JPA repository for {@link GeoShapeEntity}.
 *
 * <p>Provides CRUD operations plus spatial queries using PostGIS/Hibernate Spatial.</p>
 *
 * @see <a href="https://docs.jboss.org/hibernate/orm/6.0/userguide/html_single/Hibernate_User_Guide.html#spatial">Hibernate Spatial</a>
 */
@Repository
public interface GeoShapeRepository extends JpaRepository<GeoShapeEntity, UUID> {

  // ================================================================
  // Active / Inactive filtering
  // ================================================================

  /**
   * Finds all active GeoShapes.
   */
  @Query(
    value = "SELECT * FROM geo_schema.geoshape WHERE active = true",
    nativeQuery = true
  )
  List<GeoShapeEntity> findAllActive();

  /**
   * Finds all inactive GeoShapes.
   */
  @Query(
    value = "SELECT * FROM geo_schema.geoshape WHERE active = false",
    nativeQuery = true
  )
  List<GeoShapeEntity> findAllInactive();

  /**
   * Finds all active GeoShapes with pagination.
   */
  @Query(
    value = "SELECT * FROM geo_schema.geoshape WHERE active = :active",
    countQuery = "SELECT COUNT(*) FROM geo_schema.geoshape WHERE active = :active",
    nativeQuery = true
  )
  Page<GeoShapeEntity> findByActive(
    @Param("active") Boolean active,
    Pageable pageable
  );

  // ================================================================
  // Name-based queries
  // ================================================================

  /**
   * Finds a GeoShape by exact name (case-sensitive).
   */
  @Query(
    value = "SELECT * FROM geo_schema.geoshape WHERE name = :name LIMIT 1",
    nativeQuery = true
  )
  Optional<GeoShapeEntity> findByName(@Param("name") String name);

  /**
   * Finds GeoShapes where the name contains the given substring (case-insensitive).
   */
  @Query(
    value = """
      SELECT *
      FROM geo_schema.geoshape
      WHERE LOWER(name) LIKE LOWER(CONCAT('%', :namePart, '%'))
    """,
    nativeQuery = true
  )
  List<GeoShapeEntity> findByNameContainingIgnoreCase(
    @Param("namePart") String namePart
  );

  // ================================================================
  // Spatial queries — Point containment
  // ================================================================

  /**
   * Finds all GeoShapes whose geometry contains the given point.
   *
   * @param point the point to test (SRID must match the entity geometry, typically 4326)
   * @return all GeoShapes that contain the point
   */
  @Query(
    value = """
      SELECT *
      FROM geo_schema.geoshape
      WHERE ST_Within(:point, geometry)
    """,
    nativeQuery = true
  )
  List<GeoShapeEntity> findContainingPoint(@Param("point") Point point);

  /**
   * Finds active GeoShapes containing the given point.
   */
  @Query(
    value = """
      SELECT *
      FROM geo_schema.geoshape
      WHERE active = true
        AND ST_Within(:point, geometry)
    """,
    nativeQuery = true
  )
  List<GeoShapeEntity> findActiveContainingPoint(@Param("point") Point point);

  // ================================================================
  // Spatial queries — Geometry intersection
  // ================================================================

  /**
   * Finds all GeoShapes that intersect with the given geometry.
   *
   * @param geometry the geometry to test against (Polygon, MultiPolygon, etc.)
   * @return all GeoShapes whose geometry intersects the input
   */
  @Query(
    value = """
      SELECT *
      FROM geo_schema.geoshape
      WHERE ST_Intersects(geometry, :geometry)
    """,
    nativeQuery = true
  )
  List<GeoShapeEntity> findIntersecting(@Param("geometry") Geometry geometry);

  /**
   * Finds active GeoShapes that intersect with the given geometry.
   */
  @Query(
    value = """
      SELECT *
      FROM geo_schema.geoshape
      WHERE active = true
        AND ST_Intersects(geometry, :geometry)
    """,
    nativeQuery = true
  )
  List<GeoShapeEntity> findActiveIntersecting(
    @Param("geometry") Geometry geometry
  );

  // ================================================================
  // Spatial queries — Distance-based
  // ================================================================

  /**
   * Finds all GeoShapes within a given distance (in metres) from a point.
   *
   */
  @Query(
    value = """
      SELECT *
      FROM geo_schema.geoshape
      WHERE ST_DWithin(
        geometry::geography,
        ST_GeomFromText(:wkt)::geography,
        :distance
      )
    """,
    nativeQuery = true
  )
  List<GeoShapeEntity> findWithinDistance(
    @Param("wkt") String wkt,
    @Param("distance") double distanceMeters
  );

  /**
   * Finds all GeoShapes within a given distance, ordered by actual distance ascending.
   *
   */
  @Query(
    value = """
    SELECT *
    FROM geo_schema.geoshape
    WHERE ST_DWithin(
      geometry::geography,
      ST_GeomFromText(:wkt, 4326)::geography,
      :distance
    )
    ORDER BY ST_Distance(
      geometry::geography,
      ST_GeomFromText(:wkt)::geography
    )
  """,
    nativeQuery = true
  )
  List<GeoShapeEntity> findWithinDistanceOrderedByDistance(
    @Param("wkt") String wkt,
    @Param("distance") double distanceMeters
  );

  // ================================================================
  // Bounding box queries
  // ================================================================

  /**
   * Finds all GeoShapes that intersect with the given bounding box (envelope).
   *
   * <p>The bounding box is represented as a {@link Polygon} — typically a
   * rectangle constructed from min/max lat/lng.</p>
   */
  @Query(
    value = """
      SELECT *
      FROM geo_schema.geoshape
      WHERE ST_Intersects(geometry, :boundingBox)
    """,
    nativeQuery = true
  )
  List<GeoShapeEntity> findInBoundingBox(
    @Param("boundingBox") Polygon boundingBox
  );

  // ================================================================
  // Audit / Temporal queries
  // ================================================================

  /**
   * Finds GeoShapes created within a given time range.
   */
  @Query(
    value = """
      SELECT *
      FROM geo_schema.geoshape
      WHERE created_at BETWEEN :start AND :end
    """,
    nativeQuery = true
  )
  List<GeoShapeEntity> findByCreatedAtBetween(
    @Param("start") OffsetDateTime start,
    @Param("end") OffsetDateTime end
  );

  /**
   * Finds the 10 most recently updated GeoShapes.
   */
  @Query(
    value = """
      SELECT *
      FROM geo_schema.geoshape
      ORDER BY updated_at DESC
      LIMIT 10
    """,
    nativeQuery = true
  )
  List<GeoShapeEntity> findTop10ByOrderByUpdatedAtDesc();

  // ================================================================
  // Existence checks
  // ================================================================

  /**
   * Checks if a GeoShape with the given name exists.
   */
  @Query(
    value = """
      SELECT EXISTS(
        SELECT 1
        FROM geo_schema.geoshape
        WHERE name = :name
      )
    """,
    nativeQuery = true
  )
  boolean existsByName(@Param("name") String name);

  /**
   * Checks if an active GeoShape with the given name exists.
   */
  @Query(
    value = """
      SELECT EXISTS(
        SELECT 1
        FROM geo_schema.geoshape
        WHERE name = :name
          AND active = true
      )
    """,
    nativeQuery = true
  )
  boolean existsByNameAndActiveTrue(@Param("name") String name);
}
