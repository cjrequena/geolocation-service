package com.cjrequena.sample.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entity representing a city or municipality.
 */
@Entity
@Table(
  name = "city",
  schema = "geo_schema",
  uniqueConstraints = {
    @UniqueConstraint(name = "uq_city_region_name", columnNames = {"region_id", "name"})
  },
  indexes = {
    @Index(name = "idx_city_region", columnList = "region_id"),
    @Index(name = "idx_city_geoshape", columnList = "geoshape_id"),
    @Index(name = "idx_city_active", columnList = "active"),
    @Index(name = "idx_city_name", columnList = "name")
  }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CityEntity implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @Id
  @Column(columnDefinition = "uuid", nullable = false, updatable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "region_id", nullable = false, foreignKey = @ForeignKey(name = "fk_city_region"))
  private RegionEntity region;

  @Column(name = "name", nullable = false, length = 255)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "geoshape_id", foreignKey = @ForeignKey(name = "fk_city_geoshape"))
  private GeoShapeEntity geoShape;

  @Column(name = "population")
  private Long population;

  @Column(name = "timezone", length = 50)
  private String timeZone;

  @Column(name = "postal_code", length = 20)
  private String postalCode;

  @Column(name = "capital")
  private Boolean capital = false;

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
    createdAt = updatedAt = OffsetDateTime.now();
    if (active == null) {
      active = true;
    }
    if (capital == null) {
      capital = false;
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = OffsetDateTime.now();
  }
}
