package com.cjrequena.sample.service;

import com.cjrequena.sample.domain.model.Location;
import com.cjrequena.sample.domain.model.Zone;
import com.cjrequena.sample.domain.model.vo.AltitudeVO;
import com.cjrequena.sample.domain.model.vo.AuditInfoVO;
import com.cjrequena.sample.domain.model.vo.PointVO;
import com.cjrequena.sample.persistence.repository.LocationRepository;
import com.cjrequena.sample.persistence.repository.ZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link LocationService}.
 *
 * @author cjrequena
 */
@SpringBootTest
@ActiveProfiles("integrationTest")
@DisplayName("LocationService Integration Tests")
class LocationServiceIT {

  @Autowired
  private LocationService locationService;

  @Autowired
  private ZoneService zoneService;

  @Autowired
  private LocationRepository locationRepository;

  @Autowired
  private ZoneRepository zoneRepository;

  private UUID zoneId;

  @BeforeEach
  void setUp() {
    locationRepository.deleteAll();
    zoneRepository.deleteAll();

    // Create parent zone (requires area setup - simplified for test)
    Zone zone = new Zone();
    zone.setName("Test Zone");
    zone.setActive(true);
    zone.setAuditInfo(AuditInfoVO.of(OffsetDateTime.now(), OffsetDateTime.now()));
    // Note: In real scenario, zone needs an area. For simplicity, we'll handle this in setup
  }

  @Test
  @DisplayName("Should create location successfully")
  void shouldCreateLocation() {
    Location location = createLocationDomain("123 Main St", 40.4168, -3.7038, true);

    Location result = locationService.create(location);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isNotNull();
    assertThat(result.getAddress()).isEqualTo("123 Main St");
  }

  @Test
  @DisplayName("Should find location by ID")
  void shouldFindById() {
    Location created = locationService.create(createLocationDomain("123 Main St", 40.4168, -3.7038, true));

    Optional<Location> result = locationService.findById(created.getId());

    assertThat(result).isPresent();
    assertThat(result.get().getAddress()).isEqualTo("123 Main St");
  }

  @Test
  @DisplayName("Should find all active locations")
  void shouldFindAllActive() {
    locationService.create(createLocationDomain("123 Main St", 40.4168, -3.7038, true));
    locationService.create(createLocationDomain("456 Oak Ave", 40.4200, -3.7100, false));

    List<Location> result = locationService.findAllActive();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getAddress()).isEqualTo("123 Main St");
  }

  @Test
  @DisplayName("Should find locations within radius")
  void shouldFindWithinRadius() {
    // Madrid center
    locationService.create(createLocationDomain("Puerta del Sol", 40.4168, -3.7038, true));
    // Nearby location (about 500m away)
    locationService.create(createLocationDomain("Plaza Mayor", 40.4155, -3.7074, true));
    // Far location
    locationService.create(createLocationDomain("Airport", 40.4719, -3.5626, true));

    String centerWkt = "POINT(-3.7038 40.4168)";
    List<Location> result = locationService.findWithinRadius(centerWkt, 1000.0);

    // Should find the two nearby locations
    assertThat(result).hasSizeGreaterThanOrEqualTo(2);
  }

  @Test
  @DisplayName("Should find locations by postal code")
  void shouldFindByPostalCode() {
    Location location = createLocationDomain("123 Main St", 40.4168, -3.7038, true);
    location.setPostalCode("28001");
    locationService.create(location);

    List<Location> result = locationService.findByPostalCode("28001");

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getAddress()).isEqualTo("123 Main St");
  }

  @Test
  @DisplayName("Should find locations by address containing")
  void shouldFindByAddressContaining() {
    locationService.create(createLocationDomain("123 Main Street", 40.4168, -3.7038, true));
    locationService.create(createLocationDomain("456 Oak Avenue", 40.4200, -3.7100, true));

    List<Location> result = locationService.findByAddressContaining("Main");

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getAddress()).isEqualTo("123 Main Street");
  }

  @Test
  @DisplayName("Should find locations by altitude greater than")
  void shouldFindByAltitudeGreaterThan() {
    Location low = createLocationDomain("Low Point", 40.4168, -3.7038, true);
    low.setAltitude(AltitudeVO.of(100.0));
    locationService.create(low);

    Location high = createLocationDomain("High Point", 40.4200, -3.7100, true);
    high.setAltitude(AltitudeVO.of(500.0));
    locationService.create(high);

    List<Location> result = locationService.findByAltitudeGreaterThan(new BigDecimal("200.0"));

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getAddress()).isEqualTo("High Point");
  }

  @Test
  @DisplayName("Should find locations with altitude")
  void shouldFindWithAltitude() {
    Location withAltitude = createLocationDomain("Mountain", 40.4168, -3.7038, true);
    withAltitude.setAltitude(AltitudeVO.of(1000.0));
    locationService.create(withAltitude);

    locationService.create(createLocationDomain("Sea Level", 40.4200, -3.7100, true));

    List<Location> result = locationService.findWithAltitude();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getAddress()).isEqualTo("Mountain");
  }

  @Test
  @DisplayName("Should update location successfully")
  void shouldUpdateLocation() {
    Location created = locationService.create(createLocationDomain("123 Main St", 40.4168, -3.7038, true));

    created.setAddress("123 Main Street");
    Location updated = locationService.update(created.getId(), created);

    assertThat(updated.getAddress()).isEqualTo("123 Main Street");
  }

  @Test
  @DisplayName("Should throw exception when updating non-existent location")
  void shouldThrowExceptionWhenUpdatingNonExistent() {
    Location location = createLocationDomain("123 Main St", 40.4168, -3.7038, true);

    assertThatThrownBy(() -> locationService.update(UUID.randomUUID(), location))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Location not found");
  }

  @Test
  @DisplayName("Should delete location by ID")
  void shouldDeleteById() {
    Location created = locationService.create(createLocationDomain("123 Main St", 40.4168, -3.7038, true));

    locationService.deleteById(created.getId());

    assertThat(locationService.findById(created.getId())).isEmpty();
  }

  @Test
  @DisplayName("Should check if active location exists near point")
  void shouldCheckExistsActiveNearPoint() {
    locationService.create(createLocationDomain("Nearby", 40.4168, -3.7038, true));

    String wkt = "POINT(-3.7038 40.4168)";
    boolean result = locationService.existsActiveNearPoint(wkt, 10.0);

    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("Should count all locations")
  void shouldCount() {
    locationService.create(createLocationDomain("Location 1", 40.4168, -3.7038, true));
    locationService.create(createLocationDomain("Location 2", 40.4200, -3.7100, true));

    long result = locationService.count();

    assertThat(result).isEqualTo(2L);
  }

  private Location createLocationDomain(String address, double latitude, double longitude, boolean active) {
    Location location = new Location();
    location.setAddress(address);
    location.setPoint(PointVO.of(latitude, longitude));
    location.setActive(active);
    location.setAuditInfo(AuditInfoVO.of(OffsetDateTime.now(), OffsetDateTime.now()));
    return location;
  }
}
