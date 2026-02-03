package com.cjrequena.sample.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Entity representing a first-level administrative division (state, province, etc.).
 */
@Entity
@Table(
  name = "region",
  schema = "geo_schema",
  uniqueConstraints = {
    @UniqueConstraint(name = "uq_region_country_name", columnNames = {"country_id", "name"})
  },
  indexes = {
    @Index(name = "idx_region_country", columnList = "country_id"),
    @Index(name = "idx_region_geoshape", columnList = "geoshape_id"),
    @Index(name = "idx_region_active", columnList = "active"),
    @Index(name = "idx_region_name", columnList = "name")
  }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegionEntity implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @Id
  @Column(columnDefinition = "uuid", nullable = false, updatable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "country_id", nullable = false, foreignKey = @ForeignKey(name = "fk_region_country"))
  private CountryEntity country;

  @Column(name = "name", nullable = false, length = 255)
  private String name;

  @Column(name = "code", length = 50)
  private String code;

  @Column(name = "region_type", length = 50)
  private String regionType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "geoshape_id", foreignKey = @ForeignKey(name = "fk_region_geoshape"))
  private GeoShapeEntity geoShape;

  @Column(name = "population")
  private Long population;

  @Column(name = "timezone", length = 50)
  private TimeZone timezone;

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
}
