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
 * Entity representing a country with ISO codes and basic metadata.
 */
@Entity
@Table(
  name = "country",
  schema = "geo_schema",
  uniqueConstraints = {
    @UniqueConstraint(name = "uk_country_iso_alpha2", columnNames = "iso_code_alpha2"),
    @UniqueConstraint(name = "uk_country_iso_alpha3", columnNames = "iso_code_alpha3")
  },
  indexes = {
    @Index(name = "idx_country_name", columnList = "name"),
    @Index(name = "idx_country_active", columnList = "is_active")
  }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CountryEntity implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @Id
  @Column(columnDefinition = "uuid", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "name", nullable = false, length = 255)
  private String name;

  @Column(name = "iso_code_alpha2", nullable = false, length = 2)
  private String isoCodeAlpha2;

  @Column(name = "iso_code_alpha3", length = 3)
  private String isoCodeAlpha3;

  @Column(name = "iso_code_numeric", length = 3)
  private String isoCodeNumeric;

  @Column(name = "phone_code", length = 10)
  private String phoneCode;

  @Column(name = "currency_code", length = 3)
  private String currencyCode;

  @Column(name = "capital", length = 255)
  private String capital;

  @Column(name = "population")
  private Long population;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

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
    if (isActive == null) {
      isActive = true;
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = OffsetDateTime.now();
  }
}
