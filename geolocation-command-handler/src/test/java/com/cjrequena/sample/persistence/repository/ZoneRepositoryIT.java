package com.cjrequena.sample.persistence.repository;

import com.cjrequena.sample.persistence.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ZoneRepository}.
 * Uses PostgreSQL for testing repository queries.
 *
 * Prerequisites:
 * - PostgreSQL must be running
 * - Use docker-compose-test.yml to start test database
 * - Ensure application-local.properties has correct database configuration
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ZoneRepository Integration Tests")
class ZoneRepositoryIT {

  @Autowired
  private ZoneRepository repository;

  @Autowired
  private AreaRepository areaRepository;

  @Autowired
  private CityRepository cityRepository;

  @Autowired
  private RegionRepository regionRepository;

  @Autowired
  private CountryRepository countryRepository;

  // Test data - Countries
  private CountryEntity spain;
  private CountryEntity unitedStates;

  // Test data - Regions
  private RegionEntity madrid;
  private RegionEntity newYork;

  // Test data - Cities
  private CityEntity madridCity;
  private CityEntity newYorkCity;

  // Test data - Areas
  private AreaEntity chamberi;
  private AreaEntity manhattan;

  // Test data - Zones
  private ZoneEntity trafalgar;
  private ZoneEntity almagro;
  private ZoneEntity rios;
  private ZoneEntity timesSquare;
  private ZoneEntity centralPark;
  private ZoneEntity chelseaDistrict;
  private ZoneEntity inactiveZone;

  @BeforeEach
  void setUp() {
    // Clear database
    repository.deleteAll();
    areaRepository.deleteAll();
    cityRepository.deleteAll();
    regionRepository.deleteAll();
    countryRepository.deleteAll();

    // Create test data
    setupTestData();
  }

  private void setupTestData() {
    // Create countries
    spain = createCountry("Spain", "ES", "ESP", "724", "+34", "EUR", "Madrid", 47_000_000L, true);
    unitedStates = createCountry("United States", "US", "USA", "840", "+1", "USD", "Washington, D.C.", 331_000_000L, true);

    countryRepository.saveAll(List.of(spain, unitedStates));

    // Create regions
    madrid = createRegion(spain, "Community of Madrid", "MD", "AUTONOMOUS_COMMUNITY", 6_700_000L, true);
    newYork = createRegion(unitedStates, "New York", "NY", "STATE", 19_500_000L, true);

    regionRepository.saveAll(List.of(madrid, newYork));

    // Create cities
    madridCity = createCity(madrid, "Madrid", 3_200_000L, "Europe/Madrid", "28001", true, true);
    newYorkCity = createCity(newYork, "New York City", 8_336_000L, "America/New_York", "10001", true, true);

    cityRepository.saveAll(List.of(madridCity, newYorkCity));

    // Create areas
    chamberi = createArea(madridCity, "Chamberí", "DISTRICT", 140_000L, "28010", true);
    manhattan = createArea(newYorkCity, "Manhattan", "BOROUGH", 1_630_000L, "10001", true);

    areaRepository.saveAll(List.of(chamberi, manhattan));

    // Create zones in Chamberí
    trafalgar = createZone(chamberi, "Trafalgar", "RESIDENTIAL", "28010", true);
    almagro = createZone(chamberi, "Almagro", "RESIDENTIAL", "28010", true);
    rios = createZone(chamberi, "Ríos Rosas", "COMMERCIAL", "28003", true);

    // Create zones in Manhattan
    timesSquare = createZone(manhattan, "Times Square", "COMMERCIAL", "10036", true);
    centralPark = createZone(manhattan, "Central Park", "PARK", "10024", true);
    chelseaDistrict = createZone(manhattan, "Chelsea", "RESIDENTIAL", "10001", true);

    // Inactive zone
    inactiveZone = createZone(chamberi, "Historical Zone", "HISTORICAL", "28099", false);

    // Save all zones
    repository.saveAll(List.of(
      trafalgar, almagro, rios, timesSquare, centralPark, chelseaDistrict, inactiveZone
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

  // ================================================================
  // Basic CRUD Tests
  // ================================================================

  @Test
  @DisplayName("Should save and find zone by ID")
  void shouldSaveAndFindById() {
    // Given
    UUID id = trafalgar.getId();

    // When
    Optional<ZoneEntity> found = repository.findById(id);

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(id);
    assertThat(found.get().getName()).isEqualTo("Trafalgar");
    assertThat(found.get().getZoneType()).isEqualTo("RESIDENTIAL");
    assertThat(found.get().getPostalCode()).isEqualTo("28010");
  }

  @Test
  @DisplayName("Should find all zones")
  void shouldFindAll() {
    // When
    List<ZoneEntity> all = repository.findAll();

    // Then
    assertThat(all).hasSize(7);
  }

  @Test
  @DisplayName("Should delete zone")
  void shouldDelete() {
    // Given
    UUID id = trafalgar.getId();

    // When
    repository.deleteById(id);
    Optional<ZoneEntity> found = repository.findById(id);

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("Should update zone")
  void shouldUpdate() {
    // Given
    trafalgar.setZoneType("MIXED_USE");
    trafalgar.setPostalCode("28011");

    // When
    ZoneEntity updated = repository.save(trafalgar);

    // Then
    assertThat(updated.getZoneType()).isEqualTo("MIXED_USE");
    assertThat(updated.getPostalCode()).isEqualTo("28011");
  }

  // ================================================================
  // Active/Inactive Filtering Tests
  // ================================================================

  @Test
  @DisplayName("Should find all active zones")
  void shouldFindAllActiveZones() {
    // When
    List<ZoneEntity> active = repository.findAllByActiveTrue();

    // Then
    assertThat(active).hasSize(6);
    assertThat(active).allMatch(ZoneEntity::getActive);
    assertThat(active).extracting(ZoneEntity::getName)
      .contains("Trafalgar", "Almagro", "Ríos Rosas", "Times Square", "Central Park", "Chelsea");
  }

  @Test
  @DisplayName("Should find all inactive zones")
  void shouldFindAllInactiveZones() {
    // When
    List<ZoneEntity> inactive = repository.findAllByActiveFalse();

    // Then
    assertThat(inactive).hasSize(1);
    assertThat(inactive).allMatch(z -> !z.getActive());
    assertThat(inactive.get(0).getName()).isEqualTo("Historical Zone");
  }

  @Test
  @DisplayName("Should find zones by active status with pagination")
  void shouldFindByActiveWithPagination() {
    // When
    Page<ZoneEntity> page = repository.findByActive(true, PageRequest.of(0, 3));

    // Then
    assertThat(page.getContent()).hasSize(3);
    assertThat(page.getTotalElements()).isEqualTo(6);
    assertThat(page.getTotalPages()).isEqualTo(2);
    assertThat(page.getContent()).allMatch(ZoneEntity::getActive);
  }

  // ================================================================
  // Area Association Tests
  // ================================================================

  @Test
  @DisplayName("Should find zones by area ID")
  void shouldFindByAreaId() {
    // When
    List<ZoneEntity> chamberiZones = repository.findByAreaId(chamberi.getId());
    List<ZoneEntity> manhattanZones = repository.findByAreaId(manhattan.getId());

    // Then
    assertThat(chamberiZones).hasSize(4); // Trafalgar, Almagro, Ríos Rosas, Historical Zone
    assertThat(manhattanZones).hasSize(3); // Times Square, Central Park, Chelsea

    assertThat(chamberiZones).extracting(ZoneEntity::getName)
      .containsExactlyInAnyOrder("Trafalgar", "Almagro", "Ríos Rosas", "Historical Zone");
    assertThat(manhattanZones).extracting(ZoneEntity::getName)
      .containsExactlyInAnyOrder("Times Square", "Central Park", "Chelsea");
  }

  @Test
  @DisplayName("Should find active zones by area ID")
  void shouldFindActiveZonesByAreaId() {
    // When
    List<ZoneEntity> activeChamberiZones = repository.findByAreaIdAndActiveTrue(chamberi.getId());

    // Then
    assertThat(activeChamberiZones).hasSize(3); // Trafalgar, Almagro, Ríos Rosas (excluding Historical Zone)
    assertThat(activeChamberiZones).allMatch(ZoneEntity::getActive);
    assertThat(activeChamberiZones).extracting(ZoneEntity::getName)
      .containsExactlyInAnyOrder("Trafalgar", "Almagro", "Ríos Rosas");
  }

  @Test
  @DisplayName("Should find zones by area ID with pagination")
  void shouldFindByAreaIdWithPagination() {
    // When
    Page<ZoneEntity> page = repository.findByAreaId(chamberi.getId(), PageRequest.of(0, 2));

    // Then
    assertThat(page.getContent()).hasSize(2);
    assertThat(page.getTotalElements()).isEqualTo(4);
    assertThat(page.getTotalPages()).isEqualTo(2);
  }

  // ================================================================
  // Zone Type Filtering Tests
  // ================================================================

  @Test
  @DisplayName("Should find zones by zone type")
  void shouldFindByZoneType() {
    // When
    List<ZoneEntity> residential = repository.findByZoneType("RESIDENTIAL");
    List<ZoneEntity> commercial = repository.findByZoneType("COMMERCIAL");
    List<ZoneEntity> parks = repository.findByZoneType("PARK");

    // Then
    assertThat(residential).hasSize(3);
    assertThat(residential).extracting(ZoneEntity::getName)
      .containsExactlyInAnyOrder("Trafalgar", "Almagro", "Chelsea");

    assertThat(commercial).hasSize(2);
    assertThat(commercial).extracting(ZoneEntity::getName)
      .containsExactlyInAnyOrder("Ríos Rosas", "Times Square");

    assertThat(parks).hasSize(1);
    assertThat(parks.get(0).getName()).isEqualTo("Central Park");
  }

  @Test
  @DisplayName("Should find active zones by zone type")
  void shouldFindActiveZonesByZoneType() {
    // When
    List<ZoneEntity> activeResidential = repository.findByZoneTypeAndActiveTrue("RESIDENTIAL");
    List<ZoneEntity> activeHistorical = repository.findByZoneTypeAndActiveTrue("HISTORICAL");

    // Then
    assertThat(activeResidential).hasSize(3);
    assertThat(activeResidential).allMatch(ZoneEntity::getActive);

    assertThat(activeHistorical).isEmpty(); // Historical Zone is inactive
  }

  @Test
  @DisplayName("Should find zones by area ID and zone type")
  void shouldFindByAreaIdAndZoneType() {
    // When
    List<ZoneEntity> chamberiResidential = repository.findByAreaIdAndZoneType(
      chamberi.getId(), "RESIDENTIAL"
    );
    List<ZoneEntity> manhattanCommercial = repository.findByAreaIdAndZoneType(
      manhattan.getId(), "COMMERCIAL"
    );

    // Then
    assertThat(chamberiResidential).hasSize(2);
    assertThat(chamberiResidential).extracting(ZoneEntity::getName)
      .containsExactlyInAnyOrder("Trafalgar", "Almagro");

    assertThat(manhattanCommercial).hasSize(1);
    assertThat(manhattanCommercial.get(0).getName()).isEqualTo("Times Square");
  }

  // ================================================================
  // Postal Code Query Tests
  // ================================================================

  @Test
  @DisplayName("Should find zones by postal code")
  void shouldFindByPostalCode() {
    // When
    List<ZoneEntity> found = repository.findByPostalCode("28010");

    // Then
    assertThat(found).hasSize(2);
    assertThat(found).extracting(ZoneEntity::getName)
      .containsExactlyInAnyOrder("Trafalgar", "Almagro");
  }

  @Test
  @DisplayName("Should find active zones by postal code")
  void shouldFindActiveZonesByPostalCode() {
    // When
    List<ZoneEntity> activeFound = repository.findByPostalCodeAndActiveTrue("28010");
    List<ZoneEntity> inactiveFound = repository.findByPostalCodeAndActiveTrue("28099");

    // Then
    assertThat(activeFound).hasSize(2);
    assertThat(activeFound).extracting(ZoneEntity::getName)
      .containsExactlyInAnyOrder("Trafalgar", "Almagro");

    assertThat(inactiveFound).isEmpty(); // Historical Zone is inactive
  }

  @Test
  @DisplayName("Should find zones by area ID and postal code")
  void shouldFindByAreaIdAndPostalCode() {
    // When
    List<ZoneEntity> found = repository.findByAreaIdAndPostalCode(
      chamberi.getId(), "28010"
    );

    // Then
    assertThat(found).hasSize(2);
    assertThat(found).extracting(ZoneEntity::getName)
      .containsExactlyInAnyOrder("Trafalgar", "Almagro");
  }

  // ================================================================
  // Name-based Query Tests
  // ================================================================

  @Test
  @DisplayName("Should find zone by area ID and name")
  void shouldFindByAreaIdAndName() {
    // When
    Optional<ZoneEntity> found = repository.findByAreaIdAndName(
      chamberi.getId(), "Trafalgar"
    );
    Optional<ZoneEntity> notFound = repository.findByAreaIdAndName(
      chamberi.getId(), "NonExistent"
    );

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo("Trafalgar");
    assertThat(found.get().getArea().getId()).isEqualTo(chamberi.getId());

    assertThat(notFound).isEmpty();
  }

  @Test
  @DisplayName("Should find zones by name containing substring (case-insensitive)")
  void shouldFindByNameContainingIgnoreCase() {
    // When
    List<ZoneEntity> foundCentral = repository.findByNameContainingIgnoreCase("central");
    List<ZoneEntity> foundWithA = repository.findByNameContainingIgnoreCase("a");

    // Then
    assertThat(foundCentral).hasSize(1);
    assertThat(foundCentral.get(0).getName()).isEqualTo("Central Park");

    assertThat(foundWithA).hasSizeGreaterThanOrEqualTo(4); // Trafalgar, Almagro, Times Square, etc.
  }

  @Test
  @DisplayName("Should find active zones by name containing substring")
  void shouldFindActiveZonesByNameContaining() {
    // When
    List<ZoneEntity> found = repository.findByNameContainingIgnoreCaseAndActiveTrue("historical");

    // Then
    assertThat(found).isEmpty(); // Historical Zone is inactive
  }

  // ================================================================
  // GeoShape Association Tests
  // ================================================================

  @Test
  @DisplayName("Should find zones with no associated GeoShape")
  void shouldFindByGeoShapeIdIsNull() {
    // When
    List<ZoneEntity> zonesWithoutGeoShape = repository.findByGeoShapeIdIsNull();

    // Then
    assertThat(zonesWithoutGeoShape).hasSize(7); // All test zones have no GeoShape
  }

  @Test
  @DisplayName("Should find zones with associated GeoShape")
  void shouldFindByGeoShapeIdIsNotNull() {
    // When
    List<ZoneEntity> zonesWithGeoShape = repository.findByGeoShapeIdIsNotNull();

    // Then
    assertThat(zonesWithGeoShape).isEmpty(); // No test zones have GeoShape
  }

  // ================================================================
  // Temporal Query Tests
  // ================================================================

  @Test
  @DisplayName("Should find zones created within time range")
  void shouldFindByCreatedAtBetween() {
    // Given
    OffsetDateTime start = OffsetDateTime.now().minusHours(1);
    OffsetDateTime end = OffsetDateTime.now().plusHours(1);

    // When
    List<ZoneEntity> found = repository.findByCreatedAtBetween(start, end);

    // Then
    assertThat(found).hasSize(7); // All test zones
  }

  @Test
  @DisplayName("Should find top 10 most recently updated zones")
  void shouldFindTop10ByOrderByUpdatedAtDesc() {
    // When
    List<ZoneEntity> found = repository.findTop10ByOrderByUpdatedAtDesc();

    // Then
    assertThat(found).hasSizeLessThanOrEqualTo(10);
    assertThat(found).hasSizeLessThanOrEqualTo(7); // We only have 7 zones
  }

  @Test
  @DisplayName("Should return most recently updated zones first")
  void shouldReturnMostRecentlyUpdatedFirst() throws InterruptedException {
    // Given - Update Times Square
    Thread.sleep(10);
    timesSquare.setZoneType("ENTERTAINMENT");
    repository.save(timesSquare);

    // When
    List<ZoneEntity> found = repository.findTop10ByOrderByUpdatedAtDesc();

    // Then
    assertThat(found).isNotEmpty();
    assertThat(found.get(0).getName()).isEqualTo("Times Square");
  }

  // ================================================================
  // Existence Check Tests
  // ================================================================

  @Test
  @DisplayName("Should check if zone exists by area ID and name")
  void shouldCheckExistsByAreaIdAndName() {
    // When
    boolean exists = repository.existsByAreaIdAndName(chamberi.getId(), "Trafalgar");
    boolean notExists = repository.existsByAreaIdAndName(chamberi.getId(), "NonExistent");

    // Then
    assertThat(exists).isTrue();
    assertThat(notExists).isFalse();
  }

  @Test
  @DisplayName("Should check if active zone exists by area ID and name")
  void shouldCheckExistsByAreaIdAndNameAndActiveTrue() {
    // When
    boolean activeExists = repository.existsByAreaIdAndNameAndActiveTrue(
      chamberi.getId(), "Trafalgar"
    );
    boolean inactiveExists = repository.existsByAreaIdAndNameAndActiveTrue(
      chamberi.getId(), "Historical Zone"
    );

    // Then
    assertThat(activeExists).isTrue();
    assertThat(inactiveExists).isFalse(); // Historical Zone is inactive
  }

  @Test
  @DisplayName("Should check if zone exists by postal code")
  void shouldCheckExistsByPostalCode() {
    // When
    boolean exists = repository.existsByPostalCode("28010");
    boolean notExists = repository.existsByPostalCode("99999");

    // Then
    assertThat(exists).isTrue();
    assertThat(notExists).isFalse();
  }

  // ================================================================
  // Edge Case Tests
  // ================================================================

  @Test
  @DisplayName("Should handle empty result sets")
  void shouldHandleEmptyResults() {
    // When
    List<ZoneEntity> found = repository.findByZoneType("NON_EXISTENT_TYPE");

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("Should handle pagination beyond available data")
  void shouldHandlePaginationBeyondData() {
    // When - Request page 100
    Page<ZoneEntity> page = repository.findByActive(true, PageRequest.of(100, 10));

    // Then
    assertThat(page.getContent()).isEmpty();
    assertThat(page.getTotalElements()).isEqualTo(6);
    assertThat(page.getTotalPages()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should handle case-insensitive search correctly")
  void shouldHandleCaseInsensitiveSearch() {
    // When
    List<ZoneEntity> upperCase = repository.findByNameContainingIgnoreCase("TRAFALGAR");
    List<ZoneEntity> lowerCase = repository.findByNameContainingIgnoreCase("trafalgar");
    List<ZoneEntity> mixedCase = repository.findByNameContainingIgnoreCase("TrAfAlGaR");

    // Then
    assertThat(upperCase).hasSize(1);
    assertThat(lowerCase).hasSize(1);
    assertThat(mixedCase).hasSize(1);

    // Compare by ID to avoid lazy loading issues
    assertThat(upperCase.get(0).getId()).isEqualTo(lowerCase.get(0).getId());
    assertThat(lowerCase.get(0).getId()).isEqualTo(mixedCase.get(0).getId());
    assertThat(upperCase.get(0).getName()).isEqualTo("Trafalgar");
    assertThat(lowerCase.get(0).getName()).isEqualTo("Trafalgar");
    assertThat(mixedCase.get(0).getName()).isEqualTo("Trafalgar");
  }

  @Test
  @DisplayName("Should handle special characters in search")
  void shouldHandleSpecialCharactersInSearch() {
    // When
    List<ZoneEntity> found = repository.findByNameContainingIgnoreCase("Ríos");

    // Then
    assertThat(found).hasSize(1);
    assertThat(found.get(0).getName()).isEqualTo("Ríos Rosas");
  }

  @Test
  @DisplayName("Should maintain data integrity after multiple updates")
  void shouldMaintainDataIntegrityAfterUpdates() {
    // Given - Get the entity from database to have the actual persisted timestamp
    UUID originalId = trafalgar.getId();
    ZoneEntity fromDb = repository.findById(originalId).orElseThrow();
    OffsetDateTime originalCreatedAt = fromDb.getCreatedAt();

    // When - Multiple updates
    fromDb.setZoneType("MIXED_USE");
    repository.save(fromDb);

    fromDb.setZoneType("COMMERCIAL");
    repository.save(fromDb);

    // Then
    ZoneEntity updated = repository.findById(originalId).orElseThrow();
    assertThat(updated.getId()).isEqualTo(originalId);
    // CreatedAt should remain unchanged across updates
    assertThat(updated.getCreatedAt()).isEqualTo(originalCreatedAt);
    assertThat(updated.getZoneType()).isEqualTo("COMMERCIAL");
    // UpdatedAt should be after createdAt
    assertThat(updated.getUpdatedAt()).isAfter(originalCreatedAt);
  }

  @Test
  @DisplayName("Should enforce unique constraint on area and name")
  void shouldEnforceUniqueConstraintOnAreaAndName() {
    // Given
    ZoneEntity duplicate = createZone(
      chamberi,
      "Trafalgar", // Duplicate name in same area
      "DUPLICATE",
      "28099",
      true
    );

    // When/Then - Should throw exception
    try {
      repository.saveAndFlush(duplicate);
      assertThat(false).as("Should have thrown constraint violation").isTrue();
    } catch (Exception e) {
      assertThat(e.getMessage()).containsAnyOf("unique", "constraint", "duplicate");
    }
  }

  @Test
  @DisplayName("Should allow same zone name in different areas")
  void shouldAllowSameZoneNameInDifferentAreas() {
    // Given - Create "Park Zone" in both Chamberí and Manhattan
    ZoneEntity parkChamberi = createZone(chamberi, "Park Zone", "PARK", "28013", true);
    ZoneEntity parkManhattan = createZone(manhattan, "Park Zone", "PARK", "10019", true);

    // When/Then - Should not throw exception
    repository.save(parkChamberi);
    repository.save(parkManhattan);

    // Verify both exist
    List<ZoneEntity> parkZones = repository.findByNameContainingIgnoreCase("Park Zone");
    assertThat(parkZones).hasSize(2);
  }

  @Test
  @DisplayName("Should handle multiple zones with same postal code in same area")
  void shouldHandleMultipleZonesWithSamePostalCodeInSameArea() {
    // Given - Multiple zones can share the same postal code
    ZoneEntity zone1 = createZone(chamberi, "Zone A", "RESIDENTIAL", "28010", true);
    ZoneEntity zone2 = createZone(chamberi, "Zone B", "COMMERCIAL", "28010", true);

    // When
    repository.save(zone1);
    repository.save(zone2);

    // Then
    List<ZoneEntity> zonesWithPostalCode = repository.findByPostalCode("28010");
    assertThat(zonesWithPostalCode).hasSizeGreaterThanOrEqualTo(4); // Including Trafalgar and Almagro
  }
}
