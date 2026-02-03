package com.cjrequena.sample.domain.model.aggregate;

import com.cjrequena.sample.domain.model.vo.AuditInfoVO;
import com.cjrequena.sample.domain.model.vo.PopulationVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.TimeZone;
import java.util.UUID;

/**
 * City Domain Aggregate.
 *
 * Represents a city or municipality within a region.
 * A city can be designated as a capital and contains multiple areas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class City {

  private UUID id;
  private UUID regionId;
  private UUID geoShapeId;
  private String name;

  private PopulationVO population;
  private TimeZone timezone;
  private String postalCode;
  private Boolean capital;
  private Boolean active;
  private AuditInfoVO auditInfo;

  /**
   * Factory method to create a new city.
   */
  public static City create(
    UUID id,
    UUID regionId,
    String name) {

    validateCreation(id, regionId, name);

    return City.builder()
      .id(id)
      .regionId(regionId)
      .name(name)
      .capital(Boolean.FALSE)
      .active(Boolean.TRUE)
      .auditInfo(AuditInfoVO.create())
      .build();
  }

  /**
   * Update city information.
   */
  public void updateInfo(String name, String postalCode, TimeZone timezone) {
    if (name != null) {
      this.name = name;
    }
    if (postalCode != null) {
      this.postalCode = postalCode;
    }
    if (timezone != null) {
      this.timezone = timezone;
    }
    this.auditInfo = this.auditInfo.update();
  }

  /**
   * Assign geographic shape.
   */
  public void assignGeoShape(UUID geoShapeId) {
    this.geoShapeId = geoShapeId;
    this.auditInfo = this.auditInfo.update();
  }

  /**
   * Update population.
   */
  public void updatePopulation(PopulationVO population) {
    if (population == null || population.getValue() < 0) {
      throw new IllegalArgumentException("PopulationVO must be non-negative");
    }
    this.population = population;
    this.auditInfo = this.auditInfo.update();
  }

  /**
   * Designate city as capital.
   */
  public void designateAsCapital() {
    this.capital = Boolean.TRUE;
    this.auditInfo = this.auditInfo.update();
  }

  /**
   * Remove capital designation.
   */
  public void removeCapitalDesignation() {
    this.capital = Boolean.FALSE;
    this.auditInfo = this.auditInfo.update();
  }

  /**
   * Activate the city.
   */
  public void activate() {
    this.active = Boolean.TRUE;
    this.auditInfo = this.auditInfo.update();
  }

  /**
   * Deactivate the city.
   */
  public void deactivate() {
    this.active = Boolean.FALSE;
    this.auditInfo = this.auditInfo.update();
  }

  /**
   * Check if city is active.
   */
  public boolean isActive() {
    return this.active != null && this.active.equals(Boolean.TRUE);
  }

  /**
   * Check if city is a capital.
   */
  public boolean isCapital() {
    return this.capital != null && this.capital.equals(Boolean.TRUE);
  }

  /**
   * Check if city has geographic shape assigned.
   */
  public boolean hasGeoShape() {
    return this.geoShapeId != null;
  }

  /**
   * Check if city has population data.
   */
  public boolean hasPopulationData() {
    return this.population != null && this.population.getValue() > 0;
  }

  /**
   * Check if city has timezone defined.
   */
  public boolean hasTimezone() {
    return this.timezone != null;
  }

  // Validation methods

  private static void validateCreation(UUID id, UUID regionId, String name) {
    if (id == null) {
      throw new IllegalArgumentException("City ID cannot be null");
    }
    if (regionId == null) {
      throw new IllegalArgumentException("Region ID cannot be null");
    }
    if (name == null) {
      throw new IllegalArgumentException("City name cannot be null");
    }
  }

  @Override
  public String toString() {
    return String.format("City{id=%s, name=%s, region=%s, capital=%s}",
      id, name, regionId, isCapital());
  }
}
