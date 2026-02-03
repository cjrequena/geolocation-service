package com.cjrequena.sample.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entity representing a specific point location with coordinates.
 * Uses PostGIS Point geometry as single source of truth for coordinates.
 */
@Entity
@Table(
  name = "location",
  schema = "geo_schema",
  indexes = {
    @Index(name = "idx_location_zone", columnList = "zone_id"),
    @Index(name = "idx_location_geopoint", columnList = "geo_point"),
    @Index(name = "idx_location_active", columnList = "active")
  }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationEntity implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "id", columnDefinition = "uuid", nullable = false, updatable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "zone_id", foreignKey = @ForeignKey(name = "fk_location_zone"))
  private ZoneEntity zone;

  @Column(name = "geo_point", nullable = false, columnDefinition = "geometry(Point, 4326)")
  private Point geoPoint;

  @Column(name = "altitude_meters", precision = 8, scale = 2)
  private BigDecimal altitudeMeters;

  @Column(name = "accuracy_meters", precision = 8, scale = 2)
  private BigDecimal accuracyMeters;

  @Column(name = "address", columnDefinition = "TEXT")
  private String address;

  @Column(name = "postal_code", length = 20)
  private String postalCode;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "metadata", columnDefinition = "jsonb")
  private JsonNode metadata;

  @Column(name = "active", nullable = false)
  private Boolean active = true;

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
    if (active == null) {
      active = true;
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = OffsetDateTime.now();
  }

  /**
   * Helper method to get latitude from the geo_point.
   * @return latitude value
   */
  public Double getLatitude() {
    return geoPoint != null ? geoPoint.getY() : null;
  }

  /**
   * Helper method to get longitude from the geo_point.
   * @return longitude value
   */
  public Double getLongitude() {
    return geoPoint != null ? geoPoint.getX() : null;
  }
}
