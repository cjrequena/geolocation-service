package com.cjrequena.sample.domain.model;

import com.cjrequena.sample.domain.model.enums.ZoneType;
import com.cjrequena.sample.domain.model.vo.AuditInfoVO;
import com.cjrequena.sample.domain.model.vo.MetadataVO;
import lombok.*;

import java.util.UUID;

/**
 * Zone Domain
 *
 * Represents a fine-grained zone within an area (block, sector, precinct).
 * A zone belongs to an area and contains multiple specific locations.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Zone extends Domain {

  private UUID id;
  private UUID areaId;
  private UUID geoShapeId;
  private String name;
  private ZoneType type;
  private String postalCode;
  private Boolean active;
  private MetadataVO metadata;
  private AuditInfoVO auditInfo;

  /**
   * Factory method to create a new zone.
   */
  public static Zone create(
    UUID id,
    UUID areaId,
    UUID geoShapeId,
    String name,
    ZoneType type,
    String postalCode,
    Boolean active,
    MetadataVO metadata
  ) {

    validateCreation(id, areaId, name);

    return Zone.builder()
      .id(id)
      .areaId(areaId)
      .geoShapeId(geoShapeId)
      .name(name)
      .type(type != null ? type : ZoneType.defaultType())
      .active(active != null ? active : Boolean.TRUE)
      .postalCode(postalCode)
      .metadata(metadata != null ? metadata : MetadataVO.empty())
      .auditInfo(AuditInfoVO.create())
      .build();
  }

  /**
   * Update zone information.
   */
  public void updateInfo(String name, ZoneType type, String postalCode) {
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
   * Activate the zone.
   */
  public void activate() {
    this.active = Boolean.TRUE;
    this.auditInfo = this.auditInfo.update();
  }

  /**
   * Deactivate the zone.
   */
  public void deactivate() {
    this.active = Boolean.FALSE;
    this.auditInfo = this.auditInfo.update();
  }

  /**
   * Check if zone is active.
   */
  public boolean isActive() {
    return this.active != null && this.active.equals(Boolean.TRUE);
  }

  /**
   * Check if zone has geographic shape assigned.
   */
  public boolean hasGeoShape() {
    return this.geoShapeId != null;
  }

  /**
   * Get zone type as string.
   */
  public String getTypeAsString() {
    return this.type != null ? this.type.getValue() : null;
  }

  // Validation methods

  private static void validateCreation(UUID id, UUID areaId, String name) {
    if (id == null) {
      throw new IllegalArgumentException("Zone ID cannot be null");
    }
    if (areaId == null) {
      throw new IllegalArgumentException("Area ID cannot be null");
    }
    if (name == null) {
      throw new IllegalArgumentException("Zone name cannot be null");
    }
  }

  @Override
  public String toString() {
    return String.format("Zone{id=%s, name=%s, area=%s, type=%s}",
      id, name, areaId, type);
  }
}
