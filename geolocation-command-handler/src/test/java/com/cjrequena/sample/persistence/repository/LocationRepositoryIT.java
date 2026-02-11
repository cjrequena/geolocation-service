package com.cjrequena.sample.persistence.repository;

import com.cjrequena.sample.persistence.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link LocationRepository}.
 * Uses PostgreSQL with PostGIS for testing spatial queries.
 *
 * Prerequisites:
 * - PostgreSQL with PostGIS extension must be running
 * - Use docker-compose-test.yml to start test database
 * - Ensure application-local.properties has correct database configuration
 */
@SpringBootTest
@ActiveProfiles("integrationTest")
@DisplayName("LocationRepository Integration Tests")
class LocationRepositoryIT {

  @Autowired
  private LocationRepository repository;

  @Autowired
  private ZoneRepository zoneRepository;

  @Autowired
  private AreaRepository areaRepository;

  @Autowired
  private CityRepository cityRepository;

  @Autowired
  private RegionRepository regionRepository;

  @Autowired
  private CountryRepository countryRepository;

  private GeometryFactory geometryFactory;

  // Test data - Geographic hierarchy
  private CountryEntity spain;
  private RegionEntity madrid;
  private CityEntity madridCity;
  private AreaEntity chamberi;
  private ZoneEntity trafalgar;

  // Test data - Locations in Madrid (around Puerta del Sol)
  private LocationEntity puertaDelSol;      // 40.4168, -3.7038
  private LocationEntity plazaMayor;        // 40.4155, -3.7074
  private LocationEntity palacioReal;       // 40.4180, -3.7144
  private LocationEntity retiroPark;        // 40.4153, -3.6844
  private LocationEntity inactiveLocation;

  @BeforeEach
  void setUp() {
    // Initialize geometry factory with SRID 4326 (WGS84)
    geometryFactory = new GeometryFactory();

    // Clear database
    repository.deleteAll();
    zoneRepository.deleteAll();
    areaRepository.deleteAll();
    cityRepository.deleteAll();
    regionRepository.deleteAll();
    countryRepository.deleteAll();

    // Create test data
    setupTestData();
  }

  private void setupTestData() {
    // Create geographic hierarchy
    spain = createCountry("Spain", "ES", "ESP", "724", "+34", "EUR", "Madrid", 47_000_000L, true);
    countryRepository.save(spain);

    madrid = createRegion(spain, "Community of Madrid", "MD", "AUTONOMOUS_COMMUNITY", 6_700_000L, true);
    regionRepository.save(madrid);

    madridCity = createCity(madrid, "Madrid", 3_200_000L, "Europe/Madrid", "28001", true, true);
    cityRepository.save(madridCity);

    chamberi = createArea(madridCity, "Chamberí", "DISTRICT", 140_000L, "28010", true);
    areaRepository.save(chamberi);

    trafalgar = createZone(chamberi, "Trafalgar", "RESIDENTIAL", "28010", true);
    zoneRepository.save(trafalgar);

    // Create locations with real Madrid coordinates
    puertaDelSol = createLocation(
      trafalgar,
      40.4168, -3.7038,
      "Puerta del Sol, 1",
      "28013",
      BigDecimal.valueOf(667),
      BigDecimal.valueOf(5),
      true
    );

    plazaMayor = createLocation(
      trafalgar,
      40.4155, -3.7074,
      "Plaza Mayor, 1",
      "28012",
      BigDecimal.valueOf(650),
      BigDecimal.valueOf(10),
      true
    );

    palacioReal = createLocation(
      trafalgar,
      40.4180, -3.7144,
      "Calle de Bailén, s/n",
      "28071",
      BigDecimal.valueOf(700),
      BigDecimal.valueOf(8),
      true
    );

    retiroPark = createLocation(
      trafalgar,
      40.4153, -3.6844,
      "Plaza de la Independencia, 7",
      "28001",
      BigDecimal.valueOf(650),
      BigDecimal.valueOf(15),
      true
    );

    inactiveLocation = createLocation(
      trafalgar,
      40.4200, -3.7000,
      "Historical Address",
      "28099",
      null,
      null,
      false
    );

    // Save all locations
    repository.saveAll(List.of(
      puertaDelSol, plazaMayor, palacioReal, retiroPark, inactiveLocation
    ));
  }

  private CountryEntity createCountry(
    String name, String alpha2, String alpha3, String numeric,
    String phoneCode, String currencyCode, String capital, Long population, Boolean active
  ) {
    CountryEntity country = new CountryEntity();
    country.setId(UUID.randomUUID());
    country.setName(name);
    country.setIsoCodeAlpha2(alpha2);
    country.setIsoCodeAlpha3(alpha3);
    country.setIsoCodeNumeric(numeric);
    country.setPhoneCode(phoneCode);
    country.setCurrencyCode(currencyCode);
    country.setCapital(capital);
    country.setPopulation(population);
    country.setActive(active);
    return country;
  }

  private RegionEntity createRegion(
    CountryEntity country, String name, String code, String regionType,
    Long population, Boolean active
  ) {
    RegionEntity region = new RegionEntity();
    region.setId(UUID.randomUUID());
    region.setCountry(country);
    region.setName(name);
    region.setCode(code);
    region.setRegionType(regionType);
    region.setPopulation(population);
    region.setActive(active);
    return region;
  }

  private CityEntity createCity(
    RegionEntity region, String name, Long population, String timeZone,
    String postalCode, Boolean capital, Boolean active
  ) {
    CityEntity city = new CityEntity();
    city.setId(UUID.randomUUID());
    city.setRegion(region);
    city.setName(name);
    city.setPopulation(population);
    city.setTimeZone(timeZone);
    city.setPostalCode(postalCode);
    city.setCapital(capital);
    city.setActive(active);
    return city;
  }

  private AreaEntity createArea(
    CityEntity city, String name, String areaType, Long population,
    String postalCode, Boolean active
  ) {
    AreaEntity area = new AreaEntity();
    area.setId(UUID.randomUUID());
    area.setCity(city);
    area.setName(name);
    area.setAreaType(areaType);
    area.setPopulation(population);
    area.setPostalCode(postalCode);
    area.setActive(active);
    return area;
  }

  private ZoneEntity createZone(
    AreaEntity area, String name, String zoneType, String postalCode, Boolean active
  ) {
    ZoneEntity zone = new ZoneEntity();
    zone.setId(UUID.randomUUID());
    zone.setArea(area);
    zone.setName(name);
    zone.setZoneType(zoneType);
    zone.setPostalCode(postalCode);
    zone.setActive(active);
    return zone;
  }

  private LocationEntity createLocation(
    ZoneEntity zone, double latitude, double longitude, String address,
    String postalCode, BigDecimal altitude, BigDecimal accuracy, Boolean active
  ) {
    Random random = new Random();
    LocationEntity location = new LocationEntity();
    location.setId(UUID.randomUUID());
    location.setZone(zone);
    location.setName("Test Location-" + random.nextInt(100));
    
    // Create Point geometry with SRID 4326
    Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
    point.setSRID(4326);
    location.setPoint(point);
    
    location.setAddress(address);
    location.setPostalCode(postalCode);
    location.setAltitudeMeters(altitude);
    location.setAccuracyMeters(accuracy);
    location.setActive(active);
    return location;
  }

  // ================================================================
  // Basic CRUD Tests
  // ================================================================

  @Test
  @DisplayName("Should save and find location by ID")
  void shouldSaveAndFindById() {
    // Given
    UUID id = puertaDelSol.getId();

    // When
    Optional<LocationEntity> found = repository.findById(id);

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(id);
    assertThat(found.get().getAddress()).isEqualTo("Puerta del Sol, 1");
    assertThat(found.get().getPostalCode()).isEqualTo("28013");
    assertThat(found.get().getLatitude()).isCloseTo(40.4168, org.assertj.core.data.Offset.offset(0.0001));
    assertThat(found.get().getLongitude()).isCloseTo(-3.7038, org.assertj.core.data.Offset.offset(0.0001));
  }

  @Test
  @DisplayName("Should find all locations")
  void shouldFindAll() {
    // When
    List<LocationEntity> all = repository.findAll();

    // Then
    assertThat(all).hasSize(5);
  }

  @Test
  @DisplayName("Should delete location")
  void shouldDelete() {
    // Given
    UUID id = puertaDelSol.getId();

    // When
    repository.deleteById(id);
    Optional<LocationEntity> found = repository.findById(id);

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("Should update location")
  void shouldUpdate() {
    // Given
    puertaDelSol.setAddress("Puerta del Sol, 2");
    puertaDelSol.setPostalCode("28014");

    // When
    LocationEntity updated = repository.save(puertaDelSol);

    // Then
    assertThat(updated.getAddress()).isEqualTo("Puerta del Sol, 2");
    assertThat(updated.getPostalCode()).isEqualTo("28014");
  }

  // ================================================================
  // Active/Inactive Filtering Tests
  // ================================================================

  @Test
  @DisplayName("Should find all active locations")
  void shouldFindAllActiveLocations() {
    // When
    List<LocationEntity> active = repository.findAllByActiveTrue();

    // Then
    assertThat(active).hasSize(4);
    assertThat(active).allMatch(LocationEntity::getActive);
  }

  @Test
  @DisplayName("Should find all inactive locations")
  void shouldFindAllInactiveLocations() {
    // When
    List<LocationEntity> inactive = repository.findAllByActiveFalse();

    // Then
    assertThat(inactive).hasSize(1);
    assertThat(inactive).allMatch(l -> !l.getActive());
  }

  @Test
  @DisplayName("Should find locations by active status with pagination")
  void shouldFindByActiveWithPagination() {
    // When
    Page<LocationEntity> page = repository.findByActive(true, PageRequest.of(0, 2));

    // Then
    assertThat(page.getContent()).hasSize(2);
    assertThat(page.getTotalElements()).isEqualTo(4);
    assertThat(page.getTotalPages()).isEqualTo(2);
    assertThat(page.getContent()).allMatch(LocationEntity::getActive);
  }

  // ================================================================
  // Zone Association Tests
  // ================================================================

  @Test
  @DisplayName("Should find locations by zone ID")
  void shouldFindByZoneId() {
    // When
    List<LocationEntity> locations = repository.findByZoneId(trafalgar.getId());

    // Then
    assertThat(locations).hasSize(5);
  }

  @Test
  @DisplayName("Should find active locations by zone ID")
  void shouldFindActiveLocationsByZoneId() {
    // When
    List<LocationEntity> activeLocations = repository.findByZoneIdAndActiveTrue(trafalgar.getId());

    // Then
    assertThat(activeLocations).hasSize(4);
    assertThat(activeLocations).allMatch(LocationEntity::getActive);
  }

  @Test
  @DisplayName("Should find locations by zone ID with pagination")
  void shouldFindByZoneIdWithPagination() {
    // When
    Page<LocationEntity> page = repository.findByZoneId(trafalgar.getId(), PageRequest.of(0, 2));

    // Then
    assertThat(page.getContent()).hasSize(2);
    assertThat(page.getTotalElements()).isEqualTo(5);
    assertThat(page.getTotalPages()).isEqualTo(3);
  }

  @Test
  @DisplayName("Should find locations without zone")
  void shouldFindByZoneIdIsNull() {
    // Given - Create a location without zone
    LocationEntity noZoneLocation = createLocation(
      null, 40.4200, -3.7100, "No Zone Address", "28015", null, null, true
    );
    repository.save(noZoneLocation);

    // When
    List<LocationEntity> locationsWithoutZone = repository.findByZoneIdIsNull();

    // Then
    assertThat(locationsWithoutZone).hasSize(1);
    assertThat(locationsWithoutZone.get(0).getZone()).isNull();
  }

  @Test
  @DisplayName("Should find locations with zone")
  void shouldFindByZoneIdIsNotNull() {
    // When
    List<LocationEntity> locationsWithZone = repository.findByZoneIdIsNotNull();

    // Then
    assertThat(locationsWithZone).hasSize(5);
    assertThat(locationsWithZone).allMatch(l -> l.getZone() != null);
  }

  // ================================================================
  // Spatial Query Tests - Radius Search
  // ================================================================

  @Test
  @DisplayName("Should find locations within radius")
  void shouldFindWithinRadius() {
    // Given - Center point at Puerta del Sol as WKT
    String centerWkt = "POINT(-3.7038 40.4168)";
    double radius = 500; // 500 meters

    // When
    List<LocationEntity> found = repository.findWithinRadius(centerWkt, radius);

    // Then
    assertThat(found).hasSizeGreaterThanOrEqualTo(2); // At least Puerta del Sol and Plaza Mayor
  }

  @Test
  @DisplayName("Should find active locations within radius")
  void shouldFindActiveWithinRadius() {
    // Given - Center point at Puerta del Sol as WKT
    String centerWkt = "POINT(-3.7038 40.4168)";
    double radius = 1000; // 1 km

    // When
    List<LocationEntity> found = repository.findActiveWithinRadius(centerWkt, radius);

    // Then
    assertThat(found).isNotEmpty();
    assertThat(found).allMatch(LocationEntity::getActive);
  }

  @Test
  @DisplayName("Should find locations within radius ordered by distance")
  void shouldFindWithinRadiusOrderedByDistance() {
    // Given - Center point at Puerta del Sol as WKT
    String centerWkt = "POINT(-3.7038 40.4168)";
    double radius = 2000; // 2 km

    // When
    List<LocationEntity> found = repository.findWithinRadiusOrderedByDistance(centerWkt, radius);

    // Then
    assertThat(found).isNotEmpty();
    // First result should be Puerta del Sol itself (distance ~0)
    assertThat(found.get(0).getAddress()).contains("Puerta del Sol");
  }

  @Test
  @DisplayName("Should find nearest locations with pagination")
  void shouldFindNearestLocations() {
    // Given - Center point at Puerta del Sol as WKT
    String centerWkt = "POINT(-3.7038 40.4168)";
    double maxRadius = 5000; // 5 km

    // When - Get 2 nearest locations
    Page<LocationEntity> page = repository.findNearestLocations(centerWkt, maxRadius, PageRequest.of(0, 2));

    // Then
    assertThat(page.getContent()).hasSize(2);
    // First should be Puerta del Sol (closest to itself)
    assertThat(page.getContent().get(0).getAddress()).contains("Puerta del Sol");
  }

  // ================================================================
  // Spatial Query Tests - Polygon Containment
  // ================================================================

  @Test
  @DisplayName("Should find locations within polygon")
  void shouldFindWithinPolygon() {
    // Given - Create a polygon around central Madrid as WKT
    String polygonWkt = "POLYGON((-3.72 40.41, -3.68 40.41, -3.68 40.42, -3.72 40.42, -3.72 40.41))";

    // When
    List<LocationEntity> found = repository.findWithinPolygon(polygonWkt);

    // Then
    assertThat(found).isNotEmpty();
  }

  @Test
  @DisplayName("Should find active locations within polygon")
  void shouldFindActiveWithinPolygon() {
    // Given - Create a polygon around central Madrid as WKT
    String polygonWkt = "POLYGON((-3.72 40.41, -3.68 40.41, -3.68 40.42, -3.72 40.42, -3.72 40.41))";

    // When
    List<LocationEntity> found = repository.findActiveWithinPolygon(polygonWkt);

    // Then
    assertThat(found).isNotEmpty();
    assertThat(found).allMatch(LocationEntity::getActive);
  }

  // ================================================================
  // Postal Code Query Tests
  // ================================================================

  @Test
  @DisplayName("Should find locations by postal code")
  void shouldFindByPostalCode() {
    // When
    List<LocationEntity> found = repository.findByPostalCode("28013");

    // Then
    assertThat(found).hasSize(1);
    assertThat(found.get(0).getAddress()).contains("Puerta del Sol");
  }

  @Test
  @DisplayName("Should find active locations by postal code")
  void shouldFindActiveLocationsByPostalCode() {
    // When
    List<LocationEntity> activeFound = repository.findByPostalCodeAndActiveTrue("28013");
    List<LocationEntity> inactiveFound = repository.findByPostalCodeAndActiveTrue("28099");

    // Then
    assertThat(activeFound).hasSize(1);
    assertThat(inactiveFound).isEmpty(); // Historical location is inactive
  }

  // ================================================================
  // Address-based Query Tests
  // ================================================================

  @Test
  @DisplayName("Should find locations by address containing substring")
  void shouldFindByAddressContainingIgnoreCase() {
    // When
    List<LocationEntity> found = repository.findByAddressContainingIgnoreCase("plaza");

    // Then
    assertThat(found).hasSizeGreaterThanOrEqualTo(2); // Plaza Mayor and Plaza de la Independencia
  }

  @Test
  @DisplayName("Should find active locations by address containing substring")
  void shouldFindActiveLocationsByAddressContaining() {
    // When
    List<LocationEntity> found = repository.findByAddressContainingIgnoreCaseAndActiveTrue("plaza");

    // Then
    assertThat(found).isNotEmpty();
    assertThat(found).allMatch(LocationEntity::getActive);
  }

  // ================================================================
  // Altitude / Accuracy Filtering Tests
  // ================================================================

  @Test
  @DisplayName("Should find locations by altitude greater than threshold")
  void shouldFindByAltitudeGreaterThan() {
    // When
    List<LocationEntity> found = repository.findByAltitudeGreaterThan(BigDecimal.valueOf(660));

    // Then
    assertThat(found).hasSizeGreaterThanOrEqualTo(2); // Puerta del Sol (667m) and Palacio Real (700m)
  }

  @Test
  @DisplayName("Should find locations by altitude between range")
  void shouldFindByAltitudeBetween() {
    // When
    List<LocationEntity> found = repository.findByAltitudeBetween(
      BigDecimal.valueOf(650),
      BigDecimal.valueOf(680)
    );

    // Then
    assertThat(found).hasSizeGreaterThanOrEqualTo(3); // Puerta del Sol, Plaza Mayor, Retiro Park
  }

  @Test
  @DisplayName("Should find locations with good GPS accuracy")
  void shouldFindByAccuracyBetterThan() {
    // When - Find locations with accuracy better than 10 meters
    List<LocationEntity> found = repository.findByAccuracyBetterThan(BigDecimal.valueOf(10));

    // Then
    assertThat(found).hasSizeGreaterThanOrEqualTo(2); // Puerta del Sol (5m) and Palacio Real (8m)
  }

  @Test
  @DisplayName("Should find locations with altitude information")
  void shouldFindWithAltitude() {
    // When
    List<LocationEntity> found = repository.findWithAltitude();

    // Then
    assertThat(found).hasSize(4); // All active locations have altitude
    assertThat(found).allMatch(l -> l.getAltitudeMeters() != null);
  }

  @Test
  @DisplayName("Should find locations with accuracy information")
  void shouldFindWithAccuracy() {
    // When
    List<LocationEntity> found = repository.findWithAccuracy();

    // Then
    assertThat(found).hasSize(4); // All active locations have accuracy
    assertThat(found).allMatch(l -> l.getAccuracyMeters() != null);
  }

  // ================================================================
  // Combined Spatial + Zone Query Tests
  // ================================================================

  @Test
  @DisplayName("Should find locations by zone ID and within radius")
  void shouldFindByZoneIdAndWithinRadius() {
    // Given - Center point at Puerta del Sol as WKT
    String centerWkt = "POINT(-3.7038 40.4168)";
    double radius = 1000; // 1 km

    // When
    List<LocationEntity> found = repository.findByZoneIdAndWithinRadius(
      trafalgar.getId(), centerWkt, radius
    );

    // Then
    assertThat(found).isNotEmpty();
    assertThat(found).allMatch(l -> l.getZone().getId().equals(trafalgar.getId()));
  }

  // ================================================================
  // Temporal Query Tests
  // ================================================================

  @Test
  @DisplayName("Should find locations created within time range")
  void shouldFindByCreatedAtBetween() {
    // Given
    OffsetDateTime start = OffsetDateTime.now().minusHours(1);
    OffsetDateTime end = OffsetDateTime.now().plusHours(1);

    // When
    List<LocationEntity> found = repository.findByCreatedAtBetween(start, end);

    // Then
    assertThat(found).hasSize(5); // All test locations
  }

  @Test
  @DisplayName("Should find top 10 most recently updated locations")
  void shouldFindTop10ByOrderByUpdatedAtDesc() {
    // When
    List<LocationEntity> found = repository.findTop10ByOrderByUpdatedAtDesc();

    // Then
    assertThat(found).hasSizeLessThanOrEqualTo(10);
    assertThat(found).hasSizeLessThanOrEqualTo(5); // We only have 5 locations
  }

  @Test
  @DisplayName("Should find top 10 most recently created locations")
  void shouldFindTop10ByOrderByCreatedAtDesc() {
    // When
    List<LocationEntity> found = repository.findTop10ByOrderByCreatedAtDesc();

    // Then
    assertThat(found).hasSizeLessThanOrEqualTo(10);
    assertThat(found).hasSizeLessThanOrEqualTo(5); // We only have 5 locations
  }

  @Test
  @DisplayName("Should return most recently updated locations first")
  void shouldReturnMostRecentlyUpdatedFirst() throws InterruptedException {
    // Given - Update Palacio Real
    Thread.sleep(10);
    palacioReal.setAddress("Calle de Bailén, 2");
    repository.save(palacioReal);

    // When
    List<LocationEntity> found = repository.findTop10ByOrderByUpdatedAtDesc();

    // Then
    assertThat(found).isNotEmpty();
    assertThat(found.get(0).getAddress()).contains("Bailén");
  }

  // ================================================================
  // Existence Check Tests
  // ================================================================

  @Test
  @DisplayName("Should check if active location exists near point")
  void shouldCheckExistsActiveNearPoint() {
    // Given - Point very close to Puerta del Sol as WKT
    String nearPointWkt = "POINT(-3.7039 40.4169)";
    double threshold = 50; // 50 meters

    String farPointWkt = "POINT(-3.8000 40.5000)";

    // When
    boolean existsNear = repository.existsActiveNearPoint(nearPointWkt, threshold);
    boolean notExistsFar = repository.existsActiveNearPoint(farPointWkt, threshold);

    // Then
    assertThat(existsNear).isTrue();
    assertThat(notExistsFar).isFalse();
  }

  // ================================================================
  // Edge Case Tests
  // ================================================================

  @Test
  @DisplayName("Should handle empty result sets")
  void shouldHandleEmptyResults() {
    // When
    List<LocationEntity> found = repository.findByPostalCode("99999");

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("Should handle pagination beyond available data")
  void shouldHandlePaginationBeyondData() {
    // When - Request page 100
    Page<LocationEntity> page = repository.findByActive(true, PageRequest.of(100, 10));

    // Then
    assertThat(page.getContent()).isEmpty();
    assertThat(page.getTotalElements()).isEqualTo(4);
    assertThat(page.getTotalPages()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should handle locations with null altitude")
  void shouldHandleLocationsWithNullAltitude() {
    // When
    List<LocationEntity> withAltitude = repository.findWithAltitude();

    // Then
    assertThat(withAltitude).hasSize(4); // Inactive location has null altitude
  }

  @Test
  @DisplayName("Should handle locations with null accuracy")
  void shouldHandleLocationsWithNullAccuracy() {
    // When
    List<LocationEntity> withAccuracy = repository.findWithAccuracy();

    // Then
    assertThat(withAccuracy).hasSize(4); // Inactive location has null accuracy
  }

  @Test
  @DisplayName("Should maintain data integrity after multiple updates")
  void shouldMaintainDataIntegrityAfterUpdates() {
    // Given - Get the entity from database to have the actual persisted timestamp
    UUID originalId = puertaDelSol.getId();
    LocationEntity fromDb = repository.findById(originalId).orElseThrow();
    OffsetDateTime originalCreatedAt = fromDb.getCreatedAt();
    Point originalPoint = fromDb.getPoint();

    // When - Multiple updates
    fromDb.setAddress("Puerta del Sol, Updated 1");
    repository.save(fromDb);

    fromDb.setAddress("Puerta del Sol, Updated 2");
    repository.save(fromDb);

    // Then
    LocationEntity updated = repository.findById(originalId).orElseThrow();
    assertThat(updated.getId()).isEqualTo(originalId);
    // CreatedAt should remain unchanged across updates
    assertThat(updated.getCreatedAt()).isEqualTo(originalCreatedAt);
    assertThat(updated.getAddress()).isEqualTo("Puerta del Sol, Updated 2");
    // UpdatedAt should be after createdAt
    assertThat(updated.getUpdatedAt()).isAfter(originalCreatedAt);
    // Point should remain unchanged
    assertThat(updated.getPoint().getX()).isEqualTo(originalPoint.getX());
    assertThat(updated.getPoint().getY()).isEqualTo(originalPoint.getY());
  }

  @Test
  @DisplayName("Should handle latitude and longitude helper methods")
  void shouldHandleLatitudeLongitudeHelperMethods() {
    // When
    LocationEntity location = repository.findById(puertaDelSol.getId()).orElseThrow();

    // Then
    assertThat(location.getLatitude()).isNotNull();
    assertThat(location.getLongitude()).isNotNull();
    assertThat(location.getLatitude()).isCloseTo(40.4168, org.assertj.core.data.Offset.offset(0.0001));
    assertThat(location.getLongitude()).isCloseTo(-3.7038, org.assertj.core.data.Offset.offset(0.0001));
  }

  @Test
  @DisplayName("Should handle very small radius searches")
  void shouldHandleVerySmallRadiusSearches() {
    // Given - Center point at Puerta del Sol as WKT
    String centerWkt = "POINT(-3.7038 40.4168)";
    double radius = 10; // 10 meters - very small

    // When
    List<LocationEntity> found = repository.findWithinRadius(centerWkt, radius);

    // Then
    assertThat(found).hasSize(1); // Only Puerta del Sol itself
  }

  @Test
  @DisplayName("Should handle very large radius searches")
  void shouldHandleVeryLargeRadiusSearches() {
    // Given - Center point at Puerta del Sol as WKT
    String centerWkt = "POINT(-3.7038 40.4168)";
    double radius = 10000; // 10 km

    // When
    List<LocationEntity> found = repository.findWithinRadius(centerWkt, radius);

    // Then
    assertThat(found).hasSize(5); // All locations
  }
}
