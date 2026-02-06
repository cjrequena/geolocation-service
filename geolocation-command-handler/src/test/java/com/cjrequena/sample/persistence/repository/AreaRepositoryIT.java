package com.cjrequena.sample.persistence.repository;

import com.cjrequena.sample.persistence.entity.AreaEntity;
import com.cjrequena.sample.persistence.entity.CityEntity;
import com.cjrequena.sample.persistence.entity.CountryEntity;
import com.cjrequena.sample.persistence.entity.RegionEntity;
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
 * Integration tests for {@link AreaRepository}.
 * Uses PostgreSQL for testing repository queries.
 *
 * Prerequisites:
 * - PostgreSQL must be running
 * - Use docker-compose-test.yml to start test database
 * - Ensure application-local.properties has correct database configuration
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("AreaRepository Integration Tests")
class AreaRepositoryIT {

  @Autowired
  private AreaRepository repository;

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
  private AreaEntity salamanca;
  private AreaEntity retiro;
  private AreaEntity manhattan;
  private AreaEntity brooklyn;
  private AreaEntity queens;
  private AreaEntity inactiveArea;

  @BeforeEach
  void setUp() {
    // Clear database
    repository.deleteAll();
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

    // Create areas in Madrid
    chamberi = createArea(madridCity, "Chamberí", "DISTRICT", 140_000L, "28010", true);
    salamanca = createArea(madridCity, "Salamanca", "DISTRICT", 145_000L, "28006", true);
    retiro = createArea(madridCity, "Retiro", "DISTRICT", 120_000L, "28009", true);

    // Create areas in New York City
    manhattan = createArea(newYorkCity, "Manhattan", "BOROUGH", 1_630_000L, "10001", true);
    brooklyn = createArea(newYorkCity, "Brooklyn", "BOROUGH", 2_560_000L, "11201", true);
    queens = createArea(newYorkCity, "Queens", "BOROUGH", 2_270_000L, "11354", true);

    // Inactive area
    inactiveArea = createArea(madridCity, "Historical District", "HISTORICAL", 50_000L, "28099", false);

    // Save all areas
    repository.saveAll(List.of(
      chamberi, salamanca, retiro, manhattan, brooklyn, queens, inactiveArea
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

  // ================================================================
  // Basic CRUD Tests
  // ================================================================

  @Test
  @DisplayName("Should save and find area by ID")
  void shouldSaveAndFindById() {
    // Given
    UUID id = chamberi.getId();

    // When
    Optional<AreaEntity> found = repository.findById(id);

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(id);
    assertThat(found.get().getName()).isEqualTo("Chamberí");
    assertThat(found.get().getAreaType()).isEqualTo("DISTRICT");
    assertThat(found.get().getPostalCode()).isEqualTo("28010");
  }

  @Test
  @DisplayName("Should find all areas")
  void shouldFindAll() {
    // When
    List<AreaEntity> all = repository.findAll();

    // Then
    assertThat(all).hasSize(7);
  }

  @Test
  @DisplayName("Should delete area")
  void shouldDelete() {
    // Given
    UUID id = chamberi.getId();

    // When
    repository.deleteById(id);
    Optional<AreaEntity> found = repository.findById(id);

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("Should update area")
  void shouldUpdate() {
    // Given
    chamberi.setPopulation(145_000L);
    chamberi.setPostalCode("28011");

    // When
    AreaEntity updated = repository.save(chamberi);

    // Then
    assertThat(updated.getPopulation()).isEqualTo(145_000L);
    assertThat(updated.getPostalCode()).isEqualTo("28011");
  }

  // ================================================================
  // Active/Inactive Filtering Tests
  // ================================================================

  @Test
  @DisplayName("Should find all active areas")
  void shouldFindAllActiveAreas() {
    // When
    List<AreaEntity> active = repository.findAllByActiveTrue();

    // Then
    assertThat(active).hasSize(6);
    assertThat(active).allMatch(AreaEntity::getActive);
    assertThat(active).extracting(AreaEntity::getName)
      .contains("Chamberí", "Salamanca", "Retiro", "Manhattan", "Brooklyn", "Queens");
  }

  @Test
  @DisplayName("Should find all inactive areas")
  void shouldFindAllInactiveAreas() {
    // When
    List<AreaEntity> inactive = repository.findAllByActiveFalse();

    // Then
    assertThat(inactive).hasSize(1);
    assertThat(inactive).allMatch(a -> !a.getActive());
    assertThat(inactive.get(0).getName()).isEqualTo("Historical District");
  }

  @Test
  @DisplayName("Should find areas by active status with pagination")
  void shouldFindByActiveWithPagination() {
    // When
    Page<AreaEntity> page = repository.findByActive(true, PageRequest.of(0, 3));

    // Then
    assertThat(page.getContent()).hasSize(3);
    assertThat(page.getTotalElements()).isEqualTo(6);
    assertThat(page.getTotalPages()).isEqualTo(2);
    assertThat(page.getContent()).allMatch(AreaEntity::getActive);
  }

  // ================================================================
  // City Association Tests
  // ================================================================

  @Test
  @DisplayName("Should find areas by city ID")
  void shouldFindByCityId() {
    // When
    List<AreaEntity> madridAreas = repository.findByCityId(madridCity.getId());
    List<AreaEntity> nycAreas = repository.findByCityId(newYorkCity.getId());

    // Then
    assertThat(madridAreas).hasSize(4); // Chamberí, Salamanca, Retiro, Historical District
    assertThat(nycAreas).hasSize(3); // Manhattan, Brooklyn, Queens

    assertThat(madridAreas).extracting(AreaEntity::getName)
      .containsExactlyInAnyOrder("Chamberí", "Salamanca", "Retiro", "Historical District");
    assertThat(nycAreas).extracting(AreaEntity::getName)
      .containsExactlyInAnyOrder("Manhattan", "Brooklyn", "Queens");
  }

  @Test
  @DisplayName("Should find active areas by city ID")
  void shouldFindActiveAreasByCityId() {
    // When
    List<AreaEntity> activeMadridAreas = repository.findByCityIdAndActiveTrue(madridCity.getId());

    // Then
    assertThat(activeMadridAreas).hasSize(3); // Chamberí, Salamanca, Retiro (excluding Historical District)
    assertThat(activeMadridAreas).allMatch(AreaEntity::getActive);
    assertThat(activeMadridAreas).extracting(AreaEntity::getName)
      .containsExactlyInAnyOrder("Chamberí", "Salamanca", "Retiro");
  }

  @Test
  @DisplayName("Should find areas by city ID with pagination")
  void shouldFindByCityIdWithPagination() {
    // When
    Page<AreaEntity> page = repository.findByCityId(madridCity.getId(), PageRequest.of(0, 2));

    // Then
    assertThat(page.getContent()).hasSize(2);
    assertThat(page.getTotalElements()).isEqualTo(4);
    assertThat(page.getTotalPages()).isEqualTo(2);
  }

  // ================================================================
  // Area Type Filtering Tests
  // ================================================================

  @Test
  @DisplayName("Should find areas by area type")
  void shouldFindByAreaType() {
    // When
    List<AreaEntity> districts = repository.findByAreaType("DISTRICT");
    List<AreaEntity> boroughs = repository.findByAreaType("BOROUGH");

    // Then
    assertThat(districts).hasSize(3);
    assertThat(districts).extracting(AreaEntity::getName)
      .containsExactlyInAnyOrder("Chamberí", "Salamanca", "Retiro");

    assertThat(boroughs).hasSize(3);
    assertThat(boroughs).extracting(AreaEntity::getName)
      .containsExactlyInAnyOrder("Manhattan", "Brooklyn", "Queens");
  }

  @Test
  @DisplayName("Should find active areas by area type")
  void shouldFindActiveAreasByAreaType() {
    // When
    List<AreaEntity> activeDistricts = repository.findByAreaTypeAndActiveTrue("DISTRICT");
    List<AreaEntity> activeHistorical = repository.findByAreaTypeAndActiveTrue("HISTORICAL");

    // Then
    assertThat(activeDistricts).hasSize(3);
    assertThat(activeDistricts).allMatch(AreaEntity::getActive);

    assertThat(activeHistorical).isEmpty(); // Historical District is inactive
  }

  @Test
  @DisplayName("Should find areas by city ID and area type")
  void shouldFindByCityIdAndAreaType() {
    // When
    List<AreaEntity> madridDistricts = repository.findByCityIdAndAreaType(
      madridCity.getId(), "DISTRICT"
    );
    List<AreaEntity> nycBoroughs = repository.findByCityIdAndAreaType(
      newYorkCity.getId(), "BOROUGH"
    );

    // Then
    assertThat(madridDistricts).hasSize(3);
    assertThat(madridDistricts).extracting(AreaEntity::getName)
      .containsExactlyInAnyOrder("Chamberí", "Salamanca", "Retiro");

    assertThat(nycBoroughs).hasSize(3);
    assertThat(nycBoroughs).extracting(AreaEntity::getName)
      .containsExactlyInAnyOrder("Manhattan", "Brooklyn", "Queens");
  }

  // ================================================================
  // Postal Code Query Tests
  // ================================================================

  @Test
  @DisplayName("Should find areas by postal code")
  void shouldFindByPostalCode() {
    // When
    List<AreaEntity> found = repository.findByPostalCode("28010");

    // Then
    assertThat(found).hasSize(1);
    assertThat(found.get(0).getName()).isEqualTo("Chamberí");
    assertThat(found.get(0).getPostalCode()).isEqualTo("28010");
  }

  @Test
  @DisplayName("Should find active areas by postal code")
  void shouldFindActiveAreasByPostalCode() {
    // When
    List<AreaEntity> activeFound = repository.findByPostalCodeAndActiveTrue("28010");
    List<AreaEntity> inactiveFound = repository.findByPostalCodeAndActiveTrue("28099");

    // Then
    assertThat(activeFound).hasSize(1);
    assertThat(activeFound.get(0).getName()).isEqualTo("Chamberí");

    assertThat(inactiveFound).isEmpty(); // Historical District is inactive
  }

  @Test
  @DisplayName("Should find areas by city ID and postal code")
  void shouldFindByCityIdAndPostalCode() {
    // When
    List<AreaEntity> found = repository.findByCityIdAndPostalCode(
      madridCity.getId(), "28010"
    );

    // Then
    assertThat(found).hasSize(1);
    assertThat(found.get(0).getName()).isEqualTo("Chamberí");
  }

  // ================================================================
  // Name-based Query Tests
  // ================================================================

  @Test
  @DisplayName("Should find area by city ID and name")
  void shouldFindByCityIdAndName() {
    // When
    Optional<AreaEntity> found = repository.findByCityIdAndName(
      madridCity.getId(), "Chamberí"
    );
    Optional<AreaEntity> notFound = repository.findByCityIdAndName(
      madridCity.getId(), "NonExistent"
    );

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo("Chamberí");
    assertThat(found.get().getCity().getId()).isEqualTo(madridCity.getId());

    assertThat(notFound).isEmpty();
  }

  @Test
  @DisplayName("Should find areas by name containing substring (case-insensitive)")
  void shouldFindByNameContainingIgnoreCase() {
    // When
    List<AreaEntity> foundManhattan = repository.findByNameContainingIgnoreCase("manhattan");
    List<AreaEntity> foundWithA = repository.findByNameContainingIgnoreCase("a");

    // Then
    assertThat(foundManhattan).hasSize(1);
    assertThat(foundManhattan.get(0).getName()).isEqualTo("Manhattan");

    assertThat(foundWithA).hasSizeGreaterThanOrEqualTo(4); // Salamanca, Manhattan, Brooklyn, etc.
  }

  @Test
  @DisplayName("Should find active areas by name containing substring")
  void shouldFindActiveAreasByNameContaining() {
    // When
    List<AreaEntity> found = repository.findByNameContainingIgnoreCaseAndActiveTrue("historical");

    // Then
    assertThat(found).isEmpty(); // Historical District is inactive
  }

  // ================================================================
  // Population Query Tests
  // ================================================================

  @Test
  @DisplayName("Should find areas by city ID and population greater than threshold")
  void shouldFindByCityIdAndPopulationGreaterThan() {
    // When - Find NYC areas with more than 2 million people
    List<AreaEntity> largeNycAreas = repository.findByCityIdAndPopulationGreaterThan(
      newYorkCity.getId(), 2_000_000L
    );

    // Then
    assertThat(largeNycAreas).hasSize(2);
    assertThat(largeNycAreas).extracting(AreaEntity::getName)
      .containsExactlyInAnyOrder("Brooklyn", "Queens");
  }

  @Test
  @DisplayName("Should find areas ordered by population descending")
  void shouldFindByCityIdOrderByPopulationDesc() {
    // When - Get top 2 NYC areas by population
    Page<AreaEntity> page = repository.findByCityIdOrderByPopulationDesc(
      newYorkCity.getId(), PageRequest.of(0, 2)
    );

    // Then
    assertThat(page.getContent()).hasSize(2);
    assertThat(page.getContent().get(0).getName()).isEqualTo("Brooklyn"); // 2.56M
    assertThat(page.getContent().get(1).getName()).isEqualTo("Queens"); // 2.27M

    // Verify descending order
    List<Long> populations = page.getContent().stream()
      .map(AreaEntity::getPopulation)
      .toList();
    assertThat(populations).isSortedAccordingTo((a, b) -> Long.compare(b, a));
  }

  // ================================================================
  // GeoShape Association Tests
  // ================================================================

  @Test
  @DisplayName("Should find areas with no associated GeoShape")
  void shouldFindByGeoShapeIsNull() {
    // When
    List<AreaEntity> areasWithoutGeoShape = repository.findByGeoShapeIsNull();

    // Then
    assertThat(areasWithoutGeoShape).hasSize(7); // All test areas have no GeoShape
  }

  @Test
  @DisplayName("Should find areas with associated GeoShape")
  void shouldFindByGeoShapeIsNotNull() {
    // When
    List<AreaEntity> areasWithGeoShape = repository.findByGeoShapeIsNotNull();

    // Then
    assertThat(areasWithGeoShape).isEmpty(); // No test areas have GeoShape
  }

  // ================================================================
  // Temporal Query Tests
  // ================================================================

  @Test
  @DisplayName("Should find areas created within time range")
  void shouldFindByCreatedAtBetween() {
    // Given
    OffsetDateTime start = OffsetDateTime.now().minusHours(1);
    OffsetDateTime end = OffsetDateTime.now().plusHours(1);

    // When
    List<AreaEntity> found = repository.findByCreatedAtBetween(start, end);

    // Then
    assertThat(found).hasSize(7); // All test areas
  }

  @Test
  @DisplayName("Should find top 10 most recently updated areas")
  void shouldFindTop10ByOrderByUpdatedAtDesc() {
    // When
    List<AreaEntity> found = repository.findTop10ByOrderByUpdatedAtDesc(PageRequest.of(0, 10));

    // Then
    assertThat(found).hasSizeLessThanOrEqualTo(10);
    assertThat(found).hasSizeLessThanOrEqualTo(7); // We only have 7 areas
  }

  @Test
  @DisplayName("Should return most recently updated areas first")
  void shouldReturnMostRecentlyUpdatedFirst() throws InterruptedException {
    // Given - Update Manhattan
    Thread.sleep(10);
    manhattan.setPopulation(1_640_000L);
    repository.save(manhattan);

    // When
    List<AreaEntity> found = repository.findTop10ByOrderByUpdatedAtDesc(PageRequest.of(0, 10));

    // Then
    assertThat(found).isNotEmpty();
    assertThat(found.get(0).getName()).isEqualTo("Manhattan");
  }

  // ================================================================
  // Existence Check Tests
  // ================================================================

  @Test
  @DisplayName("Should check if area exists by city ID and name")
  void shouldCheckExistsByCityIdAndName() {
    // When
    boolean exists = repository.existsByCityIdAndName(madridCity.getId(), "Chamberí");
    boolean notExists = repository.existsByCityIdAndName(madridCity.getId(), "NonExistent");

    // Then
    assertThat(exists).isTrue();
    assertThat(notExists).isFalse();
  }

  @Test
  @DisplayName("Should check if active area exists by city ID and name")
  void shouldCheckExistsByCityIdAndNameAndActiveTrue() {
    // When
    boolean activeExists = repository.existsByCityIdAndNameAndActiveTrue(
      madridCity.getId(), "Chamberí"
    );
    boolean inactiveExists = repository.existsByCityIdAndNameAndActiveTrue(
      madridCity.getId(), "Historical District"
    );

    // Then
    assertThat(activeExists).isTrue();
    assertThat(inactiveExists).isFalse(); // Historical District is inactive
  }

  @Test
  @DisplayName("Should check if area exists by postal code")
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
    List<AreaEntity> found = repository.findByAreaType("NON_EXISTENT_TYPE");

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("Should handle pagination beyond available data")
  void shouldHandlePaginationBeyondData() {
    // When - Request page 100
    Page<AreaEntity> page = repository.findByActive(true, PageRequest.of(100, 10));

    // Then
    assertThat(page.getContent()).isEmpty();
    assertThat(page.getTotalElements()).isEqualTo(6);
    assertThat(page.getTotalPages()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should handle case-insensitive search correctly")
  void shouldHandleCaseInsensitiveSearch() {
    // When
    List<AreaEntity> upperCase = repository.findByNameContainingIgnoreCase("CHAMBERÍ");
    List<AreaEntity> lowerCase = repository.findByNameContainingIgnoreCase("chamberí");
    List<AreaEntity> mixedCase = repository.findByNameContainingIgnoreCase("ChAmBeRí");

    // Then
    assertThat(upperCase).hasSize(1);
    assertThat(lowerCase).hasSize(1);
    assertThat(mixedCase).hasSize(1);

    // Compare by ID to avoid lazy loading issues
    assertThat(upperCase.get(0).getId()).isEqualTo(lowerCase.get(0).getId());
    assertThat(lowerCase.get(0).getId()).isEqualTo(mixedCase.get(0).getId());
    assertThat(upperCase.get(0).getName()).isEqualTo("Chamberí");
    assertThat(lowerCase.get(0).getName()).isEqualTo("Chamberí");
    assertThat(mixedCase.get(0).getName()).isEqualTo("Chamberí");
  }

  @Test
  @DisplayName("Should handle special characters in search")
  void shouldHandleSpecialCharactersInSearch() {
    // When
    List<AreaEntity> found = repository.findByNameContainingIgnoreCase("Chamberí");

    // Then
    assertThat(found).hasSize(1);
    assertThat(found.get(0).getName()).isEqualTo("Chamberí");
  }

  @Test
  @DisplayName("Should maintain data integrity after multiple updates")
  void shouldMaintainDataIntegrityAfterUpdates() {
    // Given - Get the entity from database to have the actual persisted timestamp
    UUID originalId = chamberi.getId();
    AreaEntity fromDb = repository.findById(originalId).orElseThrow();
    OffsetDateTime originalCreatedAt = fromDb.getCreatedAt();

    // When - Multiple updates
    fromDb.setPopulation(141_000L);
    repository.save(fromDb);

    fromDb.setPopulation(142_000L);
    repository.save(fromDb);

    // Then
    AreaEntity updated = repository.findById(originalId).orElseThrow();
    assertThat(updated.getId()).isEqualTo(originalId);
    // CreatedAt should remain unchanged across updates
    assertThat(updated.getCreatedAt()).isEqualTo(originalCreatedAt);
    assertThat(updated.getPopulation()).isEqualTo(142_000L);
    // UpdatedAt should be after createdAt
    assertThat(updated.getUpdatedAt()).isAfter(originalCreatedAt);
  }

  @Test
  @DisplayName("Should enforce unique constraint on city and name")
  void shouldEnforceUniqueConstraintOnCityAndName() {
    // Given
    AreaEntity duplicate = createArea(
      madridCity,
      "Chamberí", // Duplicate name in same city
      "DUPLICATE",
      100_000L,
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
  @DisplayName("Should allow same area name in different cities")
  void shouldAllowSameAreaNameInDifferentCities() {
    // Given - Create "Downtown" in both Madrid and NYC
    AreaEntity downtownMadrid = createArea(madridCity, "Downtown", "DISTRICT", 80_000L, "28013", true);
    AreaEntity downtownNyc = createArea(newYorkCity, "Downtown", "NEIGHBORHOOD", 60_000L, "10007", true);

    // When/Then - Should not throw exception
    repository.save(downtownMadrid);
    repository.save(downtownNyc);

    // Verify both exist
    List<AreaEntity> downtownAreas = repository.findByNameContainingIgnoreCase("Downtown");
    assertThat(downtownAreas).hasSize(2);
  }

  @Test
  @DisplayName("Should handle multiple areas with same postal code in different cities")
  void shouldHandleMultipleAreasWithSamePostalCodeInDifferentCities() {
    // Given - Different cities can have overlapping postal codes
    AreaEntity area1 = createArea(madridCity, "Test Area 1", "DISTRICT", 50_000L, "10001", true);
    AreaEntity area2 = createArea(newYorkCity, "Test Area 2", "NEIGHBORHOOD", 40_000L, "10001", true);

    // When
    repository.save(area1);
    repository.save(area2);

    // Then
    List<AreaEntity> areasWithPostalCode = repository.findByPostalCode("10001");
    assertThat(areasWithPostalCode).hasSizeGreaterThanOrEqualTo(2);
  }

  @Test
  @DisplayName("Should handle areas with zero population")
  void shouldHandleAreasWithZeroPopulation() {
    // Given
    AreaEntity emptyArea = createArea(madridCity, "Empty Area", "DISTRICT", 0L, "28098", true);
    repository.save(emptyArea);

    // When
    List<AreaEntity> found = repository.findByCityIdAndPopulationGreaterThan(
      madridCity.getId(), -1L
    );

    // Then
    assertThat(found).hasSizeGreaterThanOrEqualTo(4); // Includes the empty area
  }
}
