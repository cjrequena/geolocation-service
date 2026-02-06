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
 * Entity representing a sub-city area (district, borough, neighborhood).
 */
@Entity
@Table(
  name = "area",
  schema = "geo_schema",
  uniqueConstraints = {
    @UniqueConstraint(name = "uq_area_city_name", columnNames = {"city_id", "name"})
  },
  indexes = {
    @Index(name = "idx_area_city", columnList = "city_id"),
    @Index(name = "idx_area_geoshape", columnList = "geoshape_id"),
    @Index(name = "idx_area_active", columnList = "active"),
    @Index(name = "idx_area_name", columnList = "name")
  }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AreaEntity implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "id", columnDefinition = "uuid", nullable = false, updatable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "city_id", nullable = false, foreignKey = @ForeignKey(name = "fk_area_city"))
  private CityEntity city;

  @Column(name = "name", nullable = false, length = 255)
  private String name;

  @Column(name = "area_type", length = 50)
  private String areaType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "geoshape_id", foreignKey = @ForeignKey(name = "fk_area_geoshape"))
  private GeoShapeEntity geoShape;

  @Column(name = "population")
  private Long population;

  @Column(name = "postal_code", length = 20)
  private String postalCode;

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
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = OffsetDateTime.now();
  }
}
