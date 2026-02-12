package com.cjrequena.sample.domain.model;

import com.cjrequena.sample.domain.model.enums.LocationType;
import com.cjrequena.sample.domain.model.vo.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Location Domain Aggregate.
 *
 * Represents a specific point location with precise coordinates.
 * This is the finest-grained geographic entity in the model.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {

  private UUID id;
  private UUID zoneId;
  private String name;
  private LocationType locationType;
  private PointVO point;
  private AltitudeVO altitude;
  private GpsAccuracyVO accuracy;
  private String address;
  private String postalCode;
  private Boolean active;
  private MetadataVO metadata;
  private AuditInfoVO auditInfo;

  /**
   * Factory method to create a new location.
   */
  public static Location create(
    UUID id,
    UUID zoneId,
    String name,
    LocationType locationType,
    PointVO point,
    AltitudeVO altitude,
    GpsAccuracyVO accuracy,
    String address,
    String postalCode,
    Boolean active,
    MetadataVO metadata
  ) {

    validateCreation(id, point);

    return Location.builder()
      .id(id)
      .zoneId(zoneId)
      .name(name != null ? name : "GENERIC_LOCATION")
      .locationType(locationType!= null ? locationType : LocationType.GENERIC)
      .point(point)
      .altitude(altitude)
      .accuracy(accuracy)
      .address(address)
      .postalCode(postalCode)
      .active(active != null ? active : Boolean.TRUE)
      .metadata(metadata!=null ? metadata : MetadataVO.empty())
      .auditInfo(AuditInfoVO.create())
      .build();
  }

  /**
   * Factory method to create a location with zone assignment.
   */
  public static Location createWithZone(
    UUID id,
    UUID zoneId,
    PointVO point,
    String address) {

    validateCreation(id, point);

    return Location.builder()
      .id(id)
      .zoneId(zoneId)
      .point(point)
      .address(address)
      .active(Boolean.TRUE)
      .metadata(MetadataVO.empty())
      .auditInfo(AuditInfoVO.create())
      .build();
  }

  /**
   * Update coordinates.
   */
  public void updateCoordinates(PointVO newPoint) {
    if (newPoint == null) {
      throw new IllegalArgumentException("Point cannot be null");
    }
    this.point = newPoint;
  }

  /**
   * Update address information.
   */
  public void updateAddress(String address, String postalCode) {
    if (address != null) {
      this.address = address;
    }
    if (postalCode != null) {
      this.postalCode = postalCode;
    }
  }

  /**
   * Assign to a zone.
   */
  public void assignToZone(UUID zoneId) {
    this.zoneId = zoneId;
  }

  /**
   * Remove zone assignment.
   */
  public void removeZoneAssignment() {
    this.zoneId = null;
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
   * Activate the location.
   */
  public void activate() {
    this.active = Boolean.TRUE;
  }

  /**
   * Deactivate the location.
   */
  public void deactivate() {
    this.active = Boolean.FALSE;
  }

  /**
   * Check if location is active.
   */
  public boolean isActive() {
    return this.active != null && this.active.equals(Boolean.TRUE);
  }

  /**
   * Check if location is assigned to a zone.
   */
  public boolean isAssignedToZone() {
    return this.zoneId != null;
  }

  /**
   * Check if location has altitude information.
   */
  public boolean hasAltitude() {
    return this.altitude != null;
  }

  /**
   * Check if location has accuracy information.
   */
  public boolean hasGpsAccuracy() {
    return this.accuracy != null;
  }

  /**
   * Get latitude.
   */
  public Double getLatitude() {
    return this.point != null ? this.point.getLatitude() : null;
  }

  /**
   * Get longitude.
   */
  public Double getLongitude() {
    return this.point != null ? this.point.getLongitude() : null;
  }

  /**
   * Calculate distance to another location.
   */
  public DistanceVO distanceTo(Location other) {
    if (other == null || other.point == null) {
      throw new IllegalArgumentException("Cannot calculate distance to null location");
    }
    return this.point.distanceTo(other.point);
  }

  /**
   * Check if location is within radius of another point.
   */
  public boolean isWithinRadius(PointVO center, DistanceVO radius) {
    if (center == null || radius == null) {
      return false;
    }
    DistanceVO distance = this.point.distanceTo(center);
    return distance.getMeters() <= radius.getMeters();
  }

  // Validation methods

  private static void validateCreation(UUID id, PointVO point) {
    if (id == null) {
      throw new IllegalArgumentException("Location ID cannot be null");
    }
    if (point == null) {
      throw new IllegalArgumentException("Point cannot be null");
    }
  }

  @Override
  public String toString() {
    return String.format("Location{id=%s, coordinates=(%s, %s), zone=%s}",
      id, getLatitude(), getLongitude(), zoneId);
  }
}
