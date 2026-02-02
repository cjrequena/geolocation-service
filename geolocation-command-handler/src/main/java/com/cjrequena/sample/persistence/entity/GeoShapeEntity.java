package com.cjrequena.sample.persistence.entity;

import com.cjrequena.sample.domain.model.enums.GeometryType;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Geometry;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entity representing a geographic shape.
 * Stores geometric shapes (point, circle, rectangle, polygon, line) with PostGIS geometry support.
 */
@Entity
@Table(
  name = "geoshape",
  schema = "geo_schema",
  indexes = {
    @Index(name = "idx_geoshape_type", columnList = "shape_type"),
    @Index(name = "idx_geoshape_geometry", columnList = "geometry")
  }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeoShapeEntity implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @Id
  @Column(columnDefinition = "uuid", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "shape_type", nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private GeometryType geometryType;

  @Column(name = "geometry", nullable = false, columnDefinition = "geometry")
  private Geometry geometry;

  @Column(name = "center_latitude", precision = 9, scale = 6)
  private BigDecimal centerLatitude;

  @Column(name = "center_longitude", precision = 9, scale = 6)
  private BigDecimal centerLongitude;

  @Column(name = "radius_meters", precision = 10, scale = 2)
  private BigDecimal radiusMeters;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "bounds", columnDefinition = "jsonb")
  private JsonNode bounds;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "metadata", nullable = false, columnDefinition = "jsonb")
  private JsonNode metadata;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    if (id == null) {
      id = com.github.f4b6a3.uuid.UuidCreator.getTimeOrdered();
    }
    createdAt = OffsetDateTime.now();
    updatedAt = OffsetDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = OffsetDateTime.now();
  }

}
