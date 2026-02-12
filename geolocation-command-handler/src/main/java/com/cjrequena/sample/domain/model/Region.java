package com.cjrequena.sample.domain.model;

import com.cjrequena.sample.domain.model.enums.RegionType;
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
 * Region Domain Aggregate.
 *
 * Represents a first-level administrative division (state, province, territory, etc.).
 * A region belongs to a country and can contain multiple cities.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Region {

  private UUID id;
  private UUID countryId;
  private UUID geoShapeId;
  private String name;
  private String code;
  private RegionType type;
  private PopulationVO population;
  private TimeZone timeZone;
  private Boolean active;
  private MetadataVO metadata;
  private AuditInfoVO auditInfo;

  /**
   * Factory method to create a new region.
   */
  public static Region create(
    UUID id,
    UUID countryId,
    UUID geoShapeId,
    String name,
    String code,
    RegionType regionType,
    PopulationVO population,
    TimeZone timeZone,
    Boolean active,
    MetadataVO metadata
  ) {

    validateCreation(id, countryId, name);

    return Region.builder()
      .id(id)
      .countryId(countryId)
      .geoShapeId(geoShapeId)
      .name(name)
      .code(code)
      .type(regionType != null ? regionType : RegionType.defaultType())
      .population(population)
      .timeZone(timeZone != null ? timeZone : TimeZone.getDefault())
      .active(active != null ? active : Boolean.TRUE)
      .metadata(metadata != null ? metadata : MetadataVO.empty())
      .auditInfo(AuditInfoVO.create())
      .build();
  }

  /**
   * Update region information.
   */
  public void updateInfo(String name, String code, RegionType type) {
    if (name != null) {
      this.name = name;
    }
    if (code != null) {
      this.code = code;
    }
    if (type != null) {
      this.type = type;
    }
    this.auditInfo = this.auditInfo.update();
  }

  /**
   * Update metadata.
   */
  public void updateMetadata(MetadataVO metadata) {
    if (metadata == null) {
      throw new IllegalArgumentException("Metadata cannot be null");
    }
    this.metadata = metadata;
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
   * Set timeZone.
   */
  public void setTimeZone(TimeZone timeZone) {
    this.timeZone = timeZone;
    this.auditInfo = this.auditInfo.update();
  }

  /**
   * Activate the region.
   */
  public void activate() {
    this.active = Boolean.TRUE;
    this.auditInfo = this.auditInfo.update();
  }

  /**
   * Deactivate the region.
   */
  public void deactivate() {
    this.active = Boolean.FALSE;
    this.auditInfo = this.auditInfo.update();
  }

  /**
   * Check if region is active.
   */
  public boolean isActive() {
    return this.active != null && this.active.equals(Boolean.TRUE);
  }

  /**
   * Check if region has geographic shape assigned.
   */
  public boolean hasGeoShape() {
    return this.geoShapeId != null;
  }

  /**
   * Check if region has population data.
   */
  public boolean hasPopulationData() {
    return this.population != null && this.population.getValue() > 0;
  }

  /**
   * Get region type as string.
   */
  public String getTypeAsString() {
    return this.type != null ? this.type.getValue() : null;
  }

  // Validation methods

  private static void validateCreation(UUID id, UUID countryId, String name) {
    if (id == null) {
      throw new IllegalArgumentException("Region ID cannot be null");
    }
    if (countryId == null) {
      throw new IllegalArgumentException("Country ID cannot be null");
    }
    if (name == null) {
      throw new IllegalArgumentException("Region name cannot be null");
    }
  }

  @Override
  public String toString() {
    return String.format("Region{id=%s, name=%s, country=%s, type=%s}",
      id, name, countryId, type);
  }
}
