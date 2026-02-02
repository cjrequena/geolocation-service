package com.cjrequena.sample.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing fine-grained zones within areas (block, sector, precinct, etc.).
 */
@Entity
@Table(
  name = "zone",
  schema = "geo_schema",
  uniqueConstraints = {
    @UniqueConstraint(name = "uq_zone_area_name", columnNames = {"area_id", "name"})
  },
  indexes = {
    @Index(name = "idx_zone_area", columnList = "area_id"),
    @Index(name = "idx_zone_geoshape", columnList = "geoshape_id"),
    @Index(name = "idx_zone_active", columnList = "is_active"),
    @Index(name = "idx_zone_name", columnList = "name")
  }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZoneEntity implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "id", columnDefinition = "uuid", nullable = false, updatable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "area_id", nullable = false, foreignKey = @ForeignKey(name = "fk_zone_area"))
  private AreaEntity area;

  @Column(name = "name", nullable = false, length = 255)
  private String name;

  @Column(name = "zone_type", length = 50)
  private String zoneType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "geoshape_id", foreignKey = @ForeignKey(name = "fk_zone_geoshape"))
  private GeoShapeEntity geoShape;

  @Column(name = "postal_code", length = 20)
  private String postalCode;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    if (id == null) {
      id = com.github.f4b6a3.uuid.UuidCreator.getTimeOrdered();
    }
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
    if (isActive == null) {
      isActive = true;
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
