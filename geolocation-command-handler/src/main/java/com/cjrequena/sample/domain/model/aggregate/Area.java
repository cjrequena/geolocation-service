package com.cjrequena.sample.domain.model.aggregate;

import com.cjrequena.sample.domain.model.enums.AreaType;
import com.cjrequena.sample.domain.model.vo.AuditInfo;
import com.cjrequena.sample.domain.model.vo.PopulationVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Area Domain Aggregate.
 *
 * Represents a sub-city area such as a district, borough, or neighborhood.
 * An area belongs to a city and contains multiple zones.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Area {

  private UUID id;
  private UUID cityId;
  private UUID geoShapeId;
  private String name;
  private AreaType type;

  private PopulationVO population;
  private String postalCode;
  private Boolean status;
  private AuditInfo auditInfo;

  /**
   * Factory method to create a new area.
   */
  public static Area create(
    UUID id,
    UUID cityId,
    String name,
    AreaType type) {

    validateCreation(id, cityId, name);

    return Area.builder()
      .id(id)
      .cityId(cityId)
      .name(name)
      .type(type != null ? type : AreaType.defaultType())
      .status(Boolean.TRUE)
      .auditInfo(AuditInfo.create())
      .build();
  }

  /**
   * Update area information.
   */
  public void updateInfo(String name, AreaType type, String postalCode) {
    if (name != null) {
      this.name = name;
    }
    if (type != null) {
      this.type = type;
    }
    if (postalCode != null) {
      this.postalCode = postalCode;
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
   * Activate the area.
   */
  public void activate() {
    this.status = Boolean.TRUE;
    this.auditInfo = this.auditInfo.update();
  }

  /**
   * Deactivate the area.
   */
  public void deactivate() {
    this.status = Boolean.FALSE;
    this.auditInfo = this.auditInfo.update();
  }

  /**
   * Check if area is active.
   */
  public boolean isActive() {
    return this.status != null && this.status.equals(Boolean.TRUE);
  }

  /**
   * Check if area has geographic shape assigned.
   */
  public boolean hasGeoShape() {
    return this.geoShapeId != null;
  }

  /**
   * Check if area has population data.
   */
  public boolean hasPopulationData() {
    return this.population != null && this.population.getValue() > 0;
  }

  /**
   * Get area type as string.
   */
  public String getTypeAsString() {
    return this.type != null ? this.type.getValue() : null;
  }

  // Validation methods

  private static void validateCreation(UUID id, UUID cityId, String name) {
    if (id == null) {
      throw new IllegalArgumentException("Area ID cannot be null");
    }
    if (cityId == null) {
      throw new IllegalArgumentException("City ID cannot be null");
    }
    if (name == null) {
      throw new IllegalArgumentException("Area name cannot be null");
    }
  }

  @Override
  public String toString() {
    return String.format("Area{id=%s, name=%s, city=%s, type=%s}",
      id, name, cityId, type);
  }
}
