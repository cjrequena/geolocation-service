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
  // Active / Inactive filtering (JPQL)
  // ================================================================

  List<GeoShapeEntity> findByActiveTrue();

  List<GeoShapeEntity> findByActiveFalse();

  Page<GeoShapeEntity> findByActive(Boolean active, Pageable pageable);

  // ================================================================
  // Name-based queries (JPQL)
  // ================================================================

  Optional<GeoShapeEntity> findByName(String name);

  @Query("""
    SELECT g
    FROM GeoShapeEntity g
    WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :namePart, '%'))
  """)
  List<GeoShapeEntity> findByNameContainingIgnoreCase(
    @Param("namePart") String namePart
  );

  // ================================================================
  // Spatial queries — Point containment (NATIVE)
  // ================================================================

  @Query(
    value = """
      SELECT *
      FROM geo_schema.geoshape
      WHERE ST_Within(:point, geometry)
    """,
    nativeQuery = true
  )
  List<GeoShapeEntity> findContainingPoint(@Param("point") Point point);

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
  // Spatial queries — Geometry intersection (NATIVE)
  // ================================================================

  @Query(
    value = """
      SELECT *
      FROM geo_schema.geoshape
      WHERE ST_Intersects(geometry, :geometry)
    """,
    nativeQuery = true
  )
  List<GeoShapeEntity> findIntersecting(@Param("geometry") Geometry geometry);

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
  // Spatial queries — Distance-based (NATIVE, geography)
  // ================================================================

  @Query(
    value = """
      SELECT *
      FROM geo_schema.geoshape
      WHERE ST_DWithin(
        geometry::geography,
        ST_GeomFromText(:wkt, 4326)::geography,
        :distance
      )
    """,
    nativeQuery = true
  )
  List<GeoShapeEntity> findWithinDistance(
    @Param("wkt") String wkt,
    @Param("distance") double distanceMeters
  );

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
        ST_GeomFromText(:wkt, 4326)::geography
      )
    """,
    nativeQuery = true
  )
  List<GeoShapeEntity> findWithinDistanceOrderedByDistance(
    @Param("wkt") String wkt,
    @Param("distance") double distanceMeters
  );

  // ================================================================
  // Bounding box queries (NATIVE)
  // ================================================================

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
  // Audit / Temporal queries (JPQL)
  // ================================================================

  @Query("""
    SELECT g
    FROM GeoShapeEntity g
    WHERE g.createdAt BETWEEN :start AND :end
  """)
  List<GeoShapeEntity> findByCreatedAtBetween(
    @Param("start") OffsetDateTime start,
    @Param("end") OffsetDateTime end
  );

  @Query("""
    SELECT g
    FROM GeoShapeEntity g
    ORDER BY g.updatedAt DESC
  """)
  List<GeoShapeEntity> findTop10ByOrderByUpdatedAtDesc(Pageable pageable);

  // ================================================================
  // Existence checks (JPQL)
  // ================================================================

  boolean existsByName(String name);

  boolean existsByNameAndActiveTrue(String name);
}
