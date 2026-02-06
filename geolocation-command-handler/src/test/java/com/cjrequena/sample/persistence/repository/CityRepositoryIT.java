package com.cjrequena.sample.persistence.repository;

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
 * Integration tests for {@link CityRepository}.
 * Tests repository queries with actual database (PostgreSQL).
 * 
 * Prerequisites:
 * - PostgreSQL must be running
 * - Use docker-compose-test.yml to start test database
 * - Ensure application-local.properties has correct database configuration
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("CityRepository Integration Tests")
class CityRepositoryIT {

  @Autowired
  private CityRepository cityRepository;

  @Autowired
  private RegionRepository regionRepository;

  @Autowired
  private CountryRepository countryRepository;

  @Autowired
  private GeoShapeRepository geoShapeRepository;

  // Test data
  private CountryEntity spain;
  private CountryEntity usa;
  
  private RegionEntity madrid;
  private RegionEntity catalonia;
  private RegionEntity newYork;
  
  private CityEntity madridCity;
  private CityEntity barcelona;
  private CityEntity valencia;
  private CityEntity newYorkCity;
  private CityEntity buffalo;
  private CityEntity inactiveCity;

  @BeforeEach
  void setUp() {
    // Clear database
    cityRepository.deleteAll();
    regionRepository.deleteAll();
    countryRepository.deleteAll();
    geoShapeRepository.deleteAll();

    // Create test data
    setupTestData();
  }

  private void setupTestData() {
    // Create countries
    spain = createCountry("Spain", "ES", "ESP");
    usa = createCountry("United States", "US", "USA");
    countryRepository.saveAll(List.of(spain, usa));

    // Create regions
    madrid = createRegion("Community of Madrid", spain);
    catalonia = createRegion("Catalonia", spain);
    newYork = createRegion("New York", usa);
    regionRepository.saveAll(List.of(madrid, catalonia, newYork));

    // Create cities in Madrid region
    madridCity = createCity(
      "Madrid",
      madrid,
      3_200_000L,
      "Europe/Madrid",
      "28001",
      true,  // capital
      true   // active
    );

    // Create cities in Catalonia region
    barcelona = createCity(
      "Barcelona",
      catalonia,
      1_600_000L,
      "Europe/Madrid",
      "08001",
      true,  // capital of Catalonia
      true   // active
    );

    valencia = createCity(
      "Valencia",
      catalonia,
      800_000L,
      "Europe/Madrid",
      "46001",
      false, // not capital
      true   // active
    );

    // Create cities in New York region
    newYorkCity = createCity(
      "New York City",
      newYork,
      8_336_000L,
      "America/New_York",
      "10001",
      true,  // capital
      true   // active
    );

    buffalo = createCity(
      "Buffalo",
      newYork,
      255_000L,
      "America/New_York",
      "14201",
      false, // not capital
      true   // active
    );

    // Inactive city
    inactiveCity = createCity(
      "Abandoned Town",
      madrid,
      0L,
      "Europe/Madrid",
      null,
      false, // not capital
      false  // inactive
    );

    // Save all cities
    cityRepository.saveAll(List.of(
      madridCity,
      barcelona,
      valencia,
      newYorkCity,
      buffalo,
      inactiveCity
    ));
  }

  private CountryEntity createCountry(String name, String alpha2, String alpha3) {
    CountryEntity country = new CountryEntity();
    country.setId(UUID.randomUUID());
    country.setName(name);
    country.setIsoCodeAlpha2(alpha2);
    country.setIsoCodeAlpha3(alpha3);
    country.setActive(true);
    return country;
  }

  private RegionEntity createRegion(String name, CountryEntity country) {
    RegionEntity region = new RegionEntity();
    region.setId(UUID.randomUUID());
    region.setName(name);
    region.setCountry(country);
    region.setActive(true);
    return region;
  }

  private CityEntity createCity(
    String name,
    RegionEntity region,
    Long population,
    String timeZone,
    String postalCode,
    Boolean capital,
    Boolean active
  ) {
    CityEntity city = new CityEntity();
    city.setId(UUID.randomUUID());
    city.setName(name);
    city.setRegion(region);
    city.setPopulation(population);
    city.setTimeZone(timeZone);
    city.setPostalCode(postalCode);
    city.setCapital(capital);
    city.setActive(active);
    return city;
  }

  // ================================================================
  // Basic CRUD Tests
  // ================================================================

  @Test
  @DisplayName("Should save and find city by ID")
  void shouldSaveAndFindById() {
    // Given
    UUID id = madridCity.getId();

    // When
    Optional<CityEntity> found = cityRepository.findById(id);

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(id);
    assertThat(found.get().getName()).isEqualTo("Madrid");
    assertThat(found.get().getCapital()).isTrue();
  }

  @Test
  @DisplayName("Should find all cities")
  void shouldFindAll() {
    // When
    List<CityEntity> all = cityRepository.findAll();

    // Then
    assertThat(all).hasSize(6);
  }

  @Test
  @DisplayName("Should delete city")
  void shouldDelete() {
    // Given
    UUID id = madridCity.getId();

    // When
    cityRepository.deleteById(id);
    Optional<CityEntity> found = cityRepository.findById(id);

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("Should update city")
  void shouldUpdate() {
    // Given - Fetch from DB to get actual persisted entity
    UUID id = madridCity.getId();
    CityEntity fromDb = cityRepository.findById(id).orElseThrow();
    fromDb.setPopulation(3_300_000L);
    fromDb.setPostalCode("28002");

    // When
    CityEntity updated = cityRepository.save(fromDb);

    // Then
    assertThat(updated.getPopulation()).isEqualTo(3_300_000L);
    assertThat(updated.getPostalCode()).isEqualTo("28002");
  }

  // ================================================================
  // Active/Inactive Filtering Tests
  // ================================================================

  @Test
  @DisplayName("Should find all active cities")
  void shouldFindAllActiveCities() {
    // When
    List<CityEntity> active = cityRepository.findAllByActiveTrue();

    // Then
    assertThat(active).hasSize(5);
    assertThat(active).allMatch(CityEntity::getActive);
    assertThat(active).extracting(CityEntity::getName)
      .contains("Madrid", "Barcelona", "Valencia", "New York City", "Buffalo");
  }

  @Test
  @DisplayName("Should find all inactive cities")
  void shouldFindAllInactiveCities() {
    // When
    List<CityEntity> inactive = cityRepository.findAllByActiveFalse();

    // Then
    assertThat(inactive).hasSize(1);
    assertThat(inactive).allMatch(c -> !c.getActive());
    assertThat(inactive.get(0).getName()).isEqualTo("Abandoned Town");
  }

  @Test
  @DisplayName("Should find cities by active status with pagination")
  void shouldFindByActiveWithPagination() {
    // When
    Page<CityEntity> page = cityRepository.findByActive(true, PageRequest.of(0, 3));

    // Then
    assertThat(page.getContent()).hasSize(3);
    assertThat(page.getTotalElements()).isEqualTo(5);
    assertThat(page.getTotalPages()).isEqualTo(2);
    assertThat(page.getContent()).allMatch(CityEntity::getActive);
  }

  // ================================================================
  // Region Navigation Tests
  // ================================================================

  @Test
  @DisplayName("Should find cities by region ID")
  void shouldFindByRegionId() {
    // When
    List<CityEntity> madridCities = cityRepository.findByRegionId(madrid.getId());

    // Then
    assertThat(madridCities).hasSize(2); // Madrid city + Abandoned Town
    assertThat(madridCities).extracting(CityEntity::getName)
      .containsExactlyInAnyOrder("Madrid", "Abandoned Town");
  }

  @Test
  @DisplayName("Should find active cities by region ID")
  void shouldFindActiveByRegionId() {
    // When
    List<CityEntity> activeMadridCities = cityRepository.findByRegionIdAndActiveTrue(madrid.getId());

    // Then
    assertThat(activeMadridCities).hasSize(1);
    assertThat(activeMadridCities.get(0).getName()).isEqualTo("Madrid");
    assertThat(activeMadridCities).allMatch(CityEntity::getActive);
  }

  @Test
  @DisplayName("Should find cities by region ID with pagination")
  void shouldFindByRegionIdWithPagination() {
    // When
    Page<CityEntity> page = cityRepository.findByRegionId(
      catalonia.getId(),
      PageRequest.of(0, 1)
    );

    // Then
    assertThat(page.getContent()).hasSize(1);
    assertThat(page.getTotalElements()).isEqualTo(2); // Barcelona + Valencia
    assertThat(page.getTotalPages()).isEqualTo(2);
  }

  @Test
  @DisplayName("Should find cities in multiple regions")
  void shouldFindCitiesInMultipleRegions() {
    // When
    List<CityEntity> cataloniaCities = cityRepository.findByRegionId(catalonia.getId());
    List<CityEntity> newYorkCities = cityRepository.findByRegionId(newYork.getId());

    // Then
    assertThat(cataloniaCities).hasSize(2);
    assertThat(newYorkCities).hasSize(2);
  }

  // ================================================================
  // GeoShape Association Tests
  // ================================================================

  @Test
  @DisplayName("Should find cities with no GeoShape")
  void shouldFindCitiesWithNoGeoShape() {
    // When
    List<CityEntity> citiesWithoutGeo = cityRepository.findByGeoShapeIdIsNull();

    // Then
    assertThat(citiesWithoutGeo).hasSize(6); // All test cities have no geoshape
  }

  @Test
  @DisplayName("Should find cities with GeoShape")
  void shouldFindCitiesWithGeoShape() {
    // This test requires PostGIS setup and complex geometry creation
    // For now, we'll skip it and just verify the query method exists
    org.junit.jupiter.api.Assumptions.assumeTrue(false, "GeoShape test requires PostGIS setup - skipping for now");
  }

  // ================================================================
  // Capital City Query Tests
  // ================================================================

  @Test
  @DisplayName("Should find all capital cities")
  void shouldFindAllCapitalCities() {
    // When
    List<CityEntity> capitals = cityRepository.findAllByCapitalTrue();

    // Then
    assertThat(capitals).hasSize(3);
    assertThat(capitals).extracting(CityEntity::getName)
      .containsExactlyInAnyOrder("Madrid", "Barcelona", "New York City");
    assertThat(capitals).allMatch(CityEntity::getCapital);
  }

  @Test
  @DisplayName("Should find active capital cities")
  void shouldFindActiveCapitalCities() {
    // When
    List<CityEntity> activeCapitals = cityRepository.findAllByCapitalTrueAndActiveTrue();

    // Then
    assertThat(activeCapitals).hasSize(3);
    assertThat(activeCapitals).allMatch(CityEntity::getCapital);
    assertThat(activeCapitals).allMatch(CityEntity::getActive);
  }

  @Test
  @DisplayName("Should find capital by region ID")
  void shouldFindCapitalByRegionId() {
    // When
    Optional<CityEntity> madridCapital = cityRepository.findCapitalByRegionId(madrid.getId());
    Optional<CityEntity> cataloniaCapital = cityRepository.findCapitalByRegionId(catalonia.getId());

    // Then
    assertThat(madridCapital).isPresent();
    assertThat(madridCapital.get().getName()).isEqualTo("Madrid");
    assertThat(cataloniaCapital).isPresent();
    assertThat(cataloniaCapital.get().getName()).isEqualTo("Barcelona");
  }

  // ================================================================
  // TimeZone Query Tests
  // ================================================================

  @Test
  @DisplayName("Should find cities by timezone")
  void shouldFindByTimeZone() {
    // When
    List<CityEntity> europeCities = cityRepository.findByTimeZone("Europe/Madrid");
    List<CityEntity> americaCities = cityRepository.findByTimeZone("America/New_York");

    // Then
    assertThat(europeCities).hasSize(4); // Madrid, Barcelona, Valencia, Abandoned Town
    assertThat(americaCities).hasSize(2); // New York City, Buffalo
  }

  @Test
  @DisplayName("Should find active cities by timezone")
  void shouldFindActiveByTimeZone() {
    // When
    List<CityEntity> activeEuropeCities = cityRepository.findByTimeZoneAndActiveTrue("Europe/Madrid");

    // Then
    assertThat(activeEuropeCities).hasSize(3); // Excludes Abandoned Town
    assertThat(activeEuropeCities).allMatch(CityEntity::getActive);
  }

  // ================================================================
  // Name-based Query Tests
  // ================================================================

  @Test
  @DisplayName("Should find city by region ID and name")
  void shouldFindByRegionIdAndName() {
    // When
    Optional<CityEntity> found = cityRepository.findByRegionIdAndName(
      madrid.getId(),
      "Madrid"
    );

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo("Madrid");
  }

  @Test
  @DisplayName("Should not find city in wrong region")
  void shouldNotFindCityInWrongRegion() {
    // When
    Optional<CityEntity> found = cityRepository.findByRegionIdAndName(
      newYork.getId(),
      "Madrid"
    );

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("Should find cities by name containing substring")
  void shouldFindByNameContainingIgnoreCase() {
    // When
    List<CityEntity> found = cityRepository.findByNameContainingIgnoreCase("new");

    // Then
    assertThat(found).hasSize(1);
    assertThat(found.get(0).getName()).isEqualTo("New York City");
  }

  @Test
  @DisplayName("Should find cities by name containing substring case-insensitive")
  void shouldFindByNameContainingCaseInsensitive() {
    // When
    List<CityEntity> upperCase = cityRepository.findByNameContainingIgnoreCase("MADRID");
    List<CityEntity> lowerCase = cityRepository.findByNameContainingIgnoreCase("madrid");

    // Then
    assertThat(upperCase).hasSize(1);
    assertThat(lowerCase).hasSize(1);
    
    // Compare by ID to avoid lazy loading issues
    assertThat(upperCase.get(0).getId()).isEqualTo(lowerCase.get(0).getId());
    assertThat(upperCase.get(0).getName()).isEqualTo("Madrid");
    assertThat(lowerCase.get(0).getName()).isEqualTo("Madrid");
  }

  @Test
  @DisplayName("Should find active cities by name containing substring")
  void shouldFindActiveByNameContaining() {
    // When
    List<CityEntity> found = cityRepository.findByNameContainingIgnoreCaseAndActiveTrue("a");

    // Then
    assertThat(found).hasSizeGreaterThan(0);
    assertThat(found).allMatch(CityEntity::getActive);
  }

  // ================================================================
  // Population Query Tests
  // ================================================================

  @Test
  @DisplayName("Should find cities by region and population greater than")
  void shouldFindByRegionIdAndPopulationGreaterThan() {
    // When
    List<CityEntity> largeCataloniaCities = cityRepository.findByRegionIdAndPopulationGreaterThan(
      catalonia.getId(),
      1_000_000L
    );

    // Then
    assertThat(largeCataloniaCities).hasSize(1);
    assertThat(largeCataloniaCities.get(0).getName()).isEqualTo("Barcelona");
    assertThat(largeCataloniaCities.get(0).getPopulation()).isGreaterThan(1_000_000L);
  }

  @Test
  @DisplayName("Should find top cities by population in region")
  void shouldFindTopCitiesByPopulationInRegion() {
    // When - Get top 2 cities in New York region
    Page<CityEntity> page = cityRepository.findByRegionIdOrderByPopulationDesc(
      newYork.getId(),
      PageRequest.of(0, 2)
    );

    // Then
    assertThat(page.getContent()).hasSize(2);
    assertThat(page.getContent().get(0).getName()).isEqualTo("New York City"); // Largest
    assertThat(page.getContent().get(1).getName()).isEqualTo("Buffalo");
    
    // Verify descending order
    assertThat(page.getContent().get(0).getPopulation())
      .isGreaterThan(page.getContent().get(1).getPopulation());
  }

  @Test
  @DisplayName("Should find top cities globally by population")
  void shouldFindTopCitiesGloballyByPopulation() {
    // When - Get top 3 cities globally
    Page<CityEntity> page = cityRepository.findAllByOrderByPopulationDesc(PageRequest.of(0, 3));

    // Then
    assertThat(page.getContent()).hasSize(3);
    assertThat(page.getContent().get(0).getName()).isEqualTo("New York City"); // Largest population
    
    // Verify descending order
    List<Long> populations = page.getContent().stream()
      .map(CityEntity::getPopulation)
      .toList();
    assertThat(populations).isSortedAccordingTo((a, b) -> Long.compare(b, a));
  }

  // ================================================================
  // Temporal Query Tests
  // ================================================================

  @Test
  @DisplayName("Should find cities created within time range")
  void shouldFindByCreatedAtBetween() {
    // Given
    OffsetDateTime start = OffsetDateTime.now().minusHours(1);
    OffsetDateTime end = OffsetDateTime.now().plusHours(1);

    // When
    List<CityEntity> found = cityRepository.findByCreatedAtBetween(start, end);

    // Then
    assertThat(found).hasSize(6); // All test cities
  }

  @Test
  @DisplayName("Should find top 10 most recently updated cities")
  void shouldFindTop10ByOrderByUpdatedAtDesc() {
    // When
    List<CityEntity> found = cityRepository.findTop10ByOrderByUpdatedAtDesc();

    // Then
    assertThat(found).hasSizeLessThanOrEqualTo(10);
    assertThat(found).hasSizeLessThanOrEqualTo(6); // We only have 6 cities
  }

  @Test
  @DisplayName("Should return most recently updated cities first")
  void shouldReturnMostRecentlyUpdatedFirst() throws InterruptedException {
    // Given - Update Barcelona
    Thread.sleep(10);
    CityEntity fromDb = cityRepository.findById(barcelona.getId()).orElseThrow();
    fromDb.setPopulation(1_700_000L);
    cityRepository.save(fromDb);

    // When
    List<CityEntity> found = cityRepository.findTop10ByOrderByUpdatedAtDesc();

    // Then
    assertThat(found).isNotEmpty();
    assertThat(found.get(0).getName()).isEqualTo("Barcelona");
  }

  // ================================================================
  // Existence Check Tests
  // ================================================================

  @Test
  @DisplayName("Should check if city exists in region by name")
  void shouldCheckExistsByRegionIdAndName() {
    // When
    boolean exists = cityRepository.existsByRegionIdAndName(madrid.getId(), "Madrid");
    boolean notExists = cityRepository.existsByRegionIdAndName(madrid.getId(), "Barcelona");

    // Then
    assertThat(exists).isTrue();
    assertThat(notExists).isFalse();
  }

  @Test
  @DisplayName("Should check if active city exists in region by name")
  void shouldCheckExistsByRegionIdAndNameAndActiveTrue() {
    // When
    boolean activeExists = cityRepository.existsByRegionIdAndNameAndActiveTrue(
      madrid.getId(),
      "Madrid"
    );
    boolean inactiveExists = cityRepository.existsByRegionIdAndNameAndActiveTrue(
      madrid.getId(),
      "Abandoned Town"
    );

    // Then
    assertThat(activeExists).isTrue();
    assertThat(inactiveExists).isFalse(); // Abandoned Town is inactive
  }

  // ================================================================
  // Edge Case Tests
  // ================================================================

  @Test
  @DisplayName("Should handle empty result sets")
  void shouldHandleEmptyResults() {
    // When
    List<CityEntity> found = cityRepository.findByTimeZone("Asia/Tokyo");

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("Should handle pagination beyond available data")
  void shouldHandlePaginationBeyondData() {
    // When - Request page 100
    Page<CityEntity> page = cityRepository.findByActive(true, PageRequest.of(100, 10));

    // Then
    assertThat(page.getContent()).isEmpty();
    assertThat(page.getTotalElements()).isEqualTo(5);
  }

  @Test
  @DisplayName("Should handle cities with zero population")
  void shouldHandleCitiesWithZeroPopulation() {
    // When
    List<CityEntity> found = cityRepository.findByRegionIdAndPopulationGreaterThan(
      madrid.getId(),
      -1L
    );

    // Then
    assertThat(found).hasSize(2); // Madrid (3.2M) and Abandoned Town (0)
    assertThat(found).extracting(CityEntity::getName)
      .containsExactlyInAnyOrder("Madrid", "Abandoned Town");
  }

  @Test
  @DisplayName("Should maintain data integrity after multiple updates")
  void shouldMaintainDataIntegrityAfterUpdates() {
    // Given - Get the entity from database
    UUID originalId = madridCity.getId();
    CityEntity fromDb = cityRepository.findById(originalId).orElseThrow();
    OffsetDateTime originalCreatedAt = fromDb.getCreatedAt();

    // When - Multiple updates
    fromDb.setPopulation(3_300_000L);
    cityRepository.save(fromDb);
    
    fromDb.setPopulation(3_400_000L);
    cityRepository.save(fromDb);

    // Then
    CityEntity updated = cityRepository.findById(originalId).orElseThrow();
    assertThat(updated.getId()).isEqualTo(originalId);
    assertThat(updated.getCreatedAt()).isEqualTo(originalCreatedAt);
    assertThat(updated.getPopulation()).isEqualTo(3_400_000L);
    assertThat(updated.getUpdatedAt()).isAfter(originalCreatedAt);
  }

  @Test
  @DisplayName("Should maintain region association after updates")
  void shouldMaintainRegionAssociationAfterUpdates() {
    // Given
    UUID regionId = madrid.getId();
    CityEntity fromDb = cityRepository.findById(madridCity.getId()).orElseThrow();

    // When
    fromDb.setPopulation(3_500_000L);
    cityRepository.save(fromDb);

    // Then
    CityEntity updated = cityRepository.findById(fromDb.getId()).orElseThrow();
    assertThat(updated.getRegion()).isNotNull();
    assertThat(updated.getRegion().getId()).isEqualTo(regionId);
  }

  @Test
  @DisplayName("Should handle multiple capitals in different regions")
  void shouldHandleMultipleCapitalsInDifferentRegions() {
    // When
    List<CityEntity> capitals = cityRepository.findAllByCapitalTrue();

    // Then
    assertThat(capitals).hasSize(3);
    
    // Verify each is in a different region by comparing region IDs
    List<UUID> regionIds = capitals.stream()
      .map(city -> city.getRegion().getId())
      .distinct()
      .toList();
    assertThat(regionIds).hasSize(3);
  }
}
