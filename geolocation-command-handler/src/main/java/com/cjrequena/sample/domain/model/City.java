package com.cjrequena.sample.domain.model;

import com.cjrequena.sample.domain.model.vo.AuditInfoVO;
import com.cjrequena.sample.domain.model.vo.MetadataVO;
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
  private TimeZone timeZone;
  private String postalCode;
  private Boolean capital;
  private Boolean active;
  private MetadataVO metadata;
  private AuditInfoVO auditInfo;

  /**
   * Factory method to create a new city.
   */
  public static City create(
    UUID id,
    UUID regionId,
    UUID geoShapeId,
    String name,
    PopulationVO population,
    TimeZone timeZone,
    String postalCode,
    Boolean capital,
    Boolean active,
    MetadataVO metadata
  ) {

    validateCreation(id, regionId, name);

    return City.builder()
      .id(id)
      .regionId(regionId)
      .geoShapeId(geoShapeId)
      .name(name)
      .population(population)
      .timeZone(timeZone)
      .postalCode(postalCode)
      .capital(capital != null ? capital : Boolean.FALSE)
      .active(active != null ? active : Boolean.TRUE)
      .metadata(metadata!=null ? metadata : MetadataVO.empty())
      .auditInfo(AuditInfoVO.create())
      .build();
  }

  /**
   * Update metadata.
   */
  public void updateMetadata(MetadataVO metadata) {
    if (metadata == null) {
      throw new IllegalArgumentException("Metadata cannot be null");
    }
    this.metadata = metadata;
  }

  /**
   * Assign geographic shape.
   */
  public void assignGeoShapeId(UUID geoShapeId) {
    this.geoShapeId = geoShapeId;
  }

  /**
   * Update population.
   */
  public void updatePopulation(PopulationVO population) {
    if (population == null || population.getValue() < 0) {
      throw new IllegalArgumentException("PopulationVO must be non-negative");
    }
    this.population = population;
  }

  /**
   * Designate city as capital.
   */
  public void designateAsCapital() {
    this.capital = Boolean.TRUE;
  }

  /**
   * Remove capital designation.
   */
  public void removeCapitalDesignation() {
    this.capital = Boolean.FALSE;
  }

  /**
   * Activate the city.
   */
  public void activate() {
    this.active = Boolean.TRUE;
  }

  /**
   * Deactivate the city.
   */
  public void deactivate() {
    this.active = Boolean.FALSE;
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
   * Check if city has timeZone defined.
   */
  public boolean hasTimezone() {
    return this.timeZone != null;
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
