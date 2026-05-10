package com.cjrequena.sample.persistence.repository;

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
import java.util.TimeZone;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link RegionRepository}.
 * Uses Testcontainers with PostGIS for testing repository queries.
 */
@SpringBootTest
@ActiveProfiles("integrationTest")
@DisplayName("RegionRepository Integration Tests")
class RegionRepositoryIT extends com.cjrequena.sample.configuration.TestcontainersConfiguration {

  @Autowired
  private RegionRepository repository;

  @Autowired
  private CountryRepository countryRepository;

  // Test data - Countries
  private CountryEntity spain;
  private CountryEntity unitedStates;
  private CountryEntity france;

  // Test data - Regions
  private RegionEntity madrid;
  private RegionEntity catalonia;
  private RegionEntity california;
  private RegionEntity texas;
  private RegionEntity ileDefrance;
  private RegionEntity provence;
  private RegionEntity inactiveRegion;

  @BeforeEach
  void setUp() {
    // Clear database
    repository.deleteAll();
    countryRepository.deleteAll();

    // Create test data
    setupTestData();
  }

  private void setupTestData() {
    // Create countries first
    spain = createCountry("Spain", "ES", "ESP", "724", "+34", "EUR", "Madrid", 47_000_000L, true);
    unitedStates = createCountry("United States", "US", "USA", "840", "+1", "USD", "Washington, D.C.", 331_000_000L, true);
    france = createCountry("France", "FR", "FRA", "250", "+33", "EUR", "Paris", 67_000_000L, true);

    countryRepository.saveAll(List.of(spain, unitedStates, france));

    // Create regions
    madrid = createRegion(spain, "Madrid", "MD", "AUTONOMOUS_COMMUNITY", 6_700_000L, TimeZone.getTimeZone("Europe/Madrid"), true);
    catalonia = createRegion(spain, "Catalonia", "CT", "AUTONOMOUS_COMMUNITY", 7_700_000L, TimeZone.getTimeZone("Europe/Madrid"), true);
    
    california = createRegion(unitedStates, "California", "CA", "STATE", 39_500_000L, TimeZone.getTimeZone("America/Los_Angeles"), true);
    texas = createRegion(unitedStates, "Texas", "TX", "STATE", 29_000_000L, TimeZone.getTimeZone("America/Chicago"), true);
    
    ileDefrance = createRegion(france, "Île-de-France", "IDF", "REGION", 12_300_000L, TimeZone.getTimeZone("Europe/Paris"), true);
    provence = createRegion(france, "Provence-Alpes-Côte d'Azur", "PACA", "REGION", 5_100_000L, TimeZone.getTimeZone("Europe/Paris"), true);
    
    inactiveRegion = createRegion(spain, "Historical Region", "HR", "HISTORICAL", 1_000_000L, TimeZone.getTimeZone("Europe/Madrid"), false);

    // Save all regions
    repository.saveAll(List.of(
      madrid, catalonia, california, texas, ileDefrance, provence, inactiveRegion
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
    Long population, TimeZone timeZone, Boolean active
  ) {
    RegionEntity region = new RegionEntity();
    region.setId(UUID.randomUUID());
    region.setCountry(country);
    region.setName(name);
    region.setCode(code);
    region.setRegionType(regionType);
    region.setPopulation(population);
    region.setTimeZone(timeZone);
    region.setActive(active);
    return region;
  }

  // ================================================================
  // Basic CRUD Tests
  // ================================================================

  @Test
  @DisplayName("Should save and find region by ID")
  void shouldSaveAndFindById() {
    // Given
    UUID id = madrid.getId();

    // When
    Optional<RegionEntity> found = repository.findById(id);

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(id);
    assertThat(found.get().getName()).isEqualTo("Madrid");
    assertThat(found.get().getCode()).isEqualTo("MD");
    assertThat(found.get().getRegionType()).isEqualTo("AUTONOMOUS_COMMUNITY");
  }

  @Test
  @DisplayName("Should find all regions")
  void shouldFindAll() {
    // When
    List<RegionEntity> all = repository.findAll();

    // Then
    assertThat(all).hasSize(7);
  }

  @Test
  @DisplayName("Should delete region")
  void shouldDelete() {
    // Given
    UUID id = madrid.getId();

    // When
    repository.deleteById(id);
    Optional<RegionEntity> found = repository.findById(id);

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("Should update region")
  void shouldUpdate() {
    // Given
    madrid.setPopulation(6_800_000L);
    madrid.setCode("MAD");

    // When
    RegionEntity updated = repository.save(madrid);

    // Then
    assertThat(updated.getPopulation()).isEqualTo(6_800_000L);
    assertThat(updated.getCode()).isEqualTo("MAD");
  }

  // ================================================================
  // Active/Inactive Filtering Tests
  // ================================================================

  @Test
  @DisplayName("Should find all active regions")
  void shouldFindAllActiveRegions() {
    // When
    List<RegionEntity> active = repository.findAllByActiveTrue();

    // Then
    assertThat(active).hasSize(6);
    assertThat(active).allMatch(RegionEntity::getActive);
    assertThat(active).extracting(RegionEntity::getName)
      .contains("Madrid", "Catalonia", "California", "Texas", "Île-de-France", "Provence-Alpes-Côte d'Azur");
  }

  @Test
  @DisplayName("Should find all inactive regions")
  void shouldFindAllInactiveRegions() {
    // When
    List<RegionEntity> inactive = repository.findAllByActiveFalse();

    // Then
    assertThat(inactive).hasSize(1);
    assertThat(inactive).allMatch(r -> !r.getActive());
    assertThat(inactive.get(0).getName()).isEqualTo("Historical Region");
  }

  @Test
  @DisplayName("Should find regions by active status with pagination")
  void shouldFindByActiveWithPagination() {
    // When
    Page<RegionEntity> page = repository.findByActive(true, PageRequest.of(0, 3));

    // Then
    assertThat(page.getContent()).hasSize(3);
    assertThat(page.getTotalElements()).isEqualTo(6);
    assertThat(page.getTotalPages()).isEqualTo(2);
    assertThat(page.getContent()).allMatch(RegionEntity::getActive);
  }

  // ================================================================
  // Country Association Tests
  // ================================================================

  @Test
  @DisplayName("Should find regions by country ID")
  void shouldFindByCountryId() {
    // When
    List<RegionEntity> spanishRegions = repository.findByCountryId(spain.getId());
    List<RegionEntity> usRegions = repository.findByCountryId(unitedStates.getId());
    List<RegionEntity> frenchRegions = repository.findByCountryId(france.getId());

    // Then
    assertThat(spanishRegions).hasSize(3); // Madrid, Catalonia, Historical Region
    assertThat(usRegions).hasSize(2); // California, Texas
    assertThat(frenchRegions).hasSize(2); // Île-de-France, Provence

    assertThat(spanishRegions).extracting(RegionEntity::getName)
      .containsExactlyInAnyOrder("Madrid", "Catalonia", "Historical Region");
    assertThat(usRegions).extracting(RegionEntity::getName)
      .containsExactlyInAnyOrder("California", "Texas");
  }

  @Test
  @DisplayName("Should find active regions by country ID")
  void shouldFindActiveRegionsByCountryId() {
    // When
    List<RegionEntity> activeSpanishRegions = repository.findByCountryIdAndActiveTrue(spain.getId());

    // Then
    assertThat(activeSpanishRegions).hasSize(2); // Madrid, Catalonia (excluding Historical Region)
    assertThat(activeSpanishRegions).allMatch(RegionEntity::getActive);
    assertThat(activeSpanishRegions).extracting(RegionEntity::getName)
      .containsExactlyInAnyOrder("Madrid", "Catalonia");
  }

  @Test
  @DisplayName("Should find regions by country ID with pagination")
  void shouldFindByCountryIdWithPagination() {
    // When
    Page<RegionEntity> page = repository.findByCountryId(spain.getId(), PageRequest.of(0, 2));

    // Then
    assertThat(page.getContent()).hasSize(2);
    assertThat(page.getTotalElements()).isEqualTo(3);
    assertThat(page.getTotalPages()).isEqualTo(2);
  }

  // ================================================================
  // Region Type Filtering Tests
  // ================================================================

  @Test
  @DisplayName("Should find regions by region type")
  void shouldFindByRegionType() {
    // When
    List<RegionEntity> states = repository.findByRegionType("STATE");
    List<RegionEntity> autonomousCommunities = repository.findByRegionType("AUTONOMOUS_COMMUNITY");
    List<RegionEntity> regions = repository.findByRegionType("REGION");

    // Then
    assertThat(states).hasSize(2);
    assertThat(states).extracting(RegionEntity::getName)
      .containsExactlyInAnyOrder("California", "Texas");

    assertThat(autonomousCommunities).hasSize(2);
    assertThat(autonomousCommunities).extracting(RegionEntity::getName)
      .containsExactlyInAnyOrder("Madrid", "Catalonia");

    assertThat(regions).hasSize(2);
    assertThat(regions).extracting(RegionEntity::getName)
      .containsExactlyInAnyOrder("Île-de-France", "Provence-Alpes-Côte d'Azur");
  }

  @Test
  @DisplayName("Should find active regions by region type")
  void shouldFindActiveRegionsByRegionType() {
    // When
    List<RegionEntity> activeStates = repository.findByRegionTypeAndActiveTrue("STATE");
    List<RegionEntity> activeHistorical = repository.findByRegionTypeAndActiveTrue("HISTORICAL");

    // Then
    assertThat(activeStates).hasSize(2);
    assertThat(activeStates).allMatch(RegionEntity::getActive);
    
    assertThat(activeHistorical).isEmpty(); // Historical region is inactive
  }

  // ================================================================
  // Name-based Query Tests
  // ================================================================

  @Test
  @DisplayName("Should find region by country ID and name")
  void shouldFindByCountryIdAndName() {
    // When
    Optional<RegionEntity> found = repository.findByCountryIdAndName(spain.getId(), "Madrid");
    Optional<RegionEntity> notFound = repository.findByCountryIdAndName(spain.getId(), "NonExistent");

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo("Madrid");
    assertThat(found.get().getCountry().getId()).isEqualTo(spain.getId());
    
    assertThat(notFound).isEmpty();
  }

  @Test
  @DisplayName("Should find regions by name containing substring (case-insensitive)")
  void shouldFindByNameContainingIgnoreCase() {
    // When
    List<RegionEntity> foundCalifornia = repository.findByNameContainingIgnoreCase("california");
    List<RegionEntity> foundWithA = repository.findByNameContainingIgnoreCase("a");

    // Then
    assertThat(foundCalifornia).hasSize(1);
    assertThat(foundCalifornia.get(0).getName()).isEqualTo("California");

    assertThat(foundWithA).hasSizeGreaterThanOrEqualTo(4); // Madrid, Catalonia, California, Texas
  }

  @Test
  @DisplayName("Should find active regions by name containing substring")
  void shouldFindActiveRegionsByNameContaining() {
    // When
    List<RegionEntity> found = repository.findByNameContainingIgnoreCaseAndActiveTrue("historical");

    // Then
    assertThat(found).isEmpty(); // Historical Region is inactive
  }

  // ================================================================
  // Population Query Tests
  // ================================================================

  @Test
  @DisplayName("Should find regions by country ID and population greater than threshold")
  void shouldFindByCountryIdAndPopulationGreaterThan() {
    // When - Find US regions with more than 30 million people
    List<RegionEntity> largeUsRegions = repository.findByCountryIdAndPopulationGreaterThan(
      unitedStates.getId(), 30_000_000L
    );

    // Then
    assertThat(largeUsRegions).hasSize(1);
    assertThat(largeUsRegions.get(0).getName()).isEqualTo("California");
  }

  @Test
  @DisplayName("Should find regions ordered by population descending")
  void shouldFindByCountryIdOrderByPopulationDesc() {
    // When - Get top 2 Spanish regions by population
    Page<RegionEntity> page = repository.findByCountryIdOrderByPopulationDesc(
      spain.getId(), PageRequest.of(0, 2)
    );

    // Then
    assertThat(page.getContent()).hasSize(2);
    assertThat(page.getContent().get(0).getName()).isEqualTo("Catalonia"); // 7.7M
    assertThat(page.getContent().get(1).getName()).isEqualTo("Madrid"); // 6.7M

    // Verify descending order
    List<Long> populations = page.getContent().stream()
      .map(RegionEntity::getPopulation)
      .toList();
    assertThat(populations).isSortedAccordingTo((a, b) -> Long.compare(b, a));
  }

  // ================================================================
  // GeoShape Association Tests
  // ================================================================

  @Test
  @DisplayName("Should find regions with no associated GeoShape")
  void shouldFindByGeoShapeIdIsNull() {
    // When
    List<RegionEntity> regionsWithoutGeoShape = repository.findByGeoShapeIdIsNull();

    // Then
    assertThat(regionsWithoutGeoShape).hasSize(7); // All test regions have no GeoShape
  }

  @Test
  @DisplayName("Should find regions with associated GeoShape")
  void shouldFindByGeoShapeIdIsNotNull() {
    // When
    List<RegionEntity> regionsWithGeoShape = repository.findByGeoShapeIdIsNotNull();

    // Then
    assertThat(regionsWithGeoShape).isEmpty(); // No test regions have GeoShape
  }

  // ================================================================
  // Temporal Query Tests
  // ================================================================

  @Test
  @DisplayName("Should find regions created within time range")
  void shouldFindByCreatedAtBetween() {
    // Given
    OffsetDateTime start = OffsetDateTime.now().minusHours(1);
    OffsetDateTime end = OffsetDateTime.now().plusHours(1);

    // When
    List<RegionEntity> found = repository.findByCreatedAtBetween(start, end);

    // Then
    assertThat(found).hasSize(7); // All test regions
  }

  @Test
  @DisplayName("Should find top 10 most recently updated regions")
  void shouldFindTop10ByOrderByUpdatedAtDesc() {
    // When
    List<RegionEntity> found = repository.findTop10ByOrderByUpdatedAtDesc();

    // Then
    assertThat(found).hasSizeLessThanOrEqualTo(10);
    assertThat(found).hasSizeLessThanOrEqualTo(7); // We only have 7 regions
  }

  @Test
  @DisplayName("Should return most recently updated regions first")
  void shouldReturnMostRecentlyUpdatedFirst() throws InterruptedException {
    // Given - Update California
    Thread.sleep(10);
    california.setPopulation(40_000_000L);
    repository.save(california);

    // When
    List<RegionEntity> found = repository.findTop10ByOrderByUpdatedAtDesc();

    // Then
    assertThat(found).isNotEmpty();
    assertThat(found.get(0).getName()).isEqualTo("California");
  }

  // ================================================================
  // Existence Check Tests
  // ================================================================

  @Test
  @DisplayName("Should check if region exists by country ID and name")
  void shouldCheckExistsByCountryIdAndName() {
    // When
    boolean exists = repository.existsByCountryIdAndName(spain.getId(), "Madrid");
    boolean notExists = repository.existsByCountryIdAndName(spain.getId(), "NonExistent");

    // Then
    assertThat(exists).isTrue();
    assertThat(notExists).isFalse();
  }

  @Test
  @DisplayName("Should check if active region exists by country ID and name")
  void shouldCheckExistsByCountryIdAndNameAndActiveTrue() {
    // When
    boolean activeExists = repository.existsByCountryIdAndNameAndActiveTrue(spain.getId(), "Madrid");
    boolean inactiveExists = repository.existsByCountryIdAndNameAndActiveTrue(spain.getId(), "Historical Region");

    // Then
    assertThat(activeExists).isTrue();
    assertThat(inactiveExists).isFalse(); // Historical Region is inactive
  }

  // ================================================================
  // Edge Case Tests
  // ================================================================

  @Test
  @DisplayName("Should handle empty result sets")
  void shouldHandleEmptyResults() {
    // When
    List<RegionEntity> found = repository.findByRegionType("NON_EXISTENT_TYPE");

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("Should handle pagination beyond available data")
  void shouldHandlePaginationBeyondData() {
    // When - Request page 100
    Page<RegionEntity> page = repository.findByActive(true, PageRequest.of(100, 10));

    // Then
    assertThat(page.getContent()).isEmpty();
    assertThat(page.getTotalElements()).isEqualTo(6);
    assertThat(page.getTotalPages()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should handle case-insensitive search correctly")
  void shouldHandleCaseInsensitiveSearch() {
    // When
    List<RegionEntity> upperCase = repository.findByNameContainingIgnoreCase("MADRID");
    List<RegionEntity> lowerCase = repository.findByNameContainingIgnoreCase("madrid");
    List<RegionEntity> mixedCase = repository.findByNameContainingIgnoreCase("MaDrId");

    // Then
    assertThat(upperCase).hasSize(1);
    assertThat(lowerCase).hasSize(1);
    assertThat(mixedCase).hasSize(1);
    
    // Compare by ID to avoid lazy loading issues
    assertThat(upperCase.get(0).getId()).isEqualTo(lowerCase.get(0).getId());
    assertThat(lowerCase.get(0).getId()).isEqualTo(mixedCase.get(0).getId());
    assertThat(upperCase.get(0).getName()).isEqualTo("Madrid");
    assertThat(lowerCase.get(0).getName()).isEqualTo("Madrid");
    assertThat(mixedCase.get(0).getName()).isEqualTo("Madrid");
  }

  @Test
  @DisplayName("Should handle special characters in search")
  void shouldHandleSpecialCharactersInSearch() {
    // When
    List<RegionEntity> found = repository.findByNameContainingIgnoreCase("Île");

    // Then
    assertThat(found).hasSize(1);
    assertThat(found.get(0).getName()).isEqualTo("Île-de-France");
  }

  @Test
  @DisplayName("Should maintain data integrity after multiple updates")
  void shouldMaintainDataIntegrityAfterUpdates() {
    // Given - Get the entity from database to have the actual persisted timestamp
    UUID originalId = madrid.getId();
    RegionEntity fromDb = repository.findById(originalId).orElseThrow();
    OffsetDateTime originalCreatedAt = fromDb.getCreatedAt();

    // When - Multiple updates
    fromDb.setPopulation(6_750_000L);
    repository.save(fromDb);

    fromDb.setPopulation(6_800_000L);
    repository.save(fromDb);

    // Then
    RegionEntity updated = repository.findById(originalId).orElseThrow();
    assertThat(updated.getId()).isEqualTo(originalId);
    // CreatedAt should remain unchanged across updates
    assertThat(updated.getCreatedAt()).isEqualTo(originalCreatedAt);
    assertThat(updated.getPopulation()).isEqualTo(6_800_000L);
    // UpdatedAt should be after createdAt
    assertThat(updated.getUpdatedAt()).isAfter(originalCreatedAt);
  }

  @Test
  @DisplayName("Should enforce unique constraint on country and name")
  void shouldEnforceUniqueConstraintOnCountryAndName() {
    // Given
    RegionEntity duplicate = createRegion(
      spain, 
      "Madrid", // Duplicate name in same country
      "DUP", 
      "DUPLICATE", 
      1_000_000L, 
      TimeZone.getTimeZone("Europe/Madrid"), 
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
  @DisplayName("Should allow same region name in different countries")
  void shouldAllowSameRegionNameInDifferentCountries() {
    // Given - Create "Valencia" in both Spain and a hypothetical country
    CountryEntity venezuela = createCountry("Venezuela", "VE", "VEN", "862", "+58", "VES", "Caracas", 28_000_000L, true);
    countryRepository.save(venezuela);

    RegionEntity valenciaSpain = createRegion(spain, "Valencia", "VAL", "AUTONOMOUS_COMMUNITY", 2_500_000L, TimeZone.getTimeZone("Europe/Madrid"), true);
    RegionEntity valenciaVenezuela = createRegion(venezuela, "Valencia", "VAL", "STATE", 2_200_000L, TimeZone.getTimeZone("America/Caracas"), true);

    // When/Then - Should not throw exception
    repository.save(valenciaSpain);
    repository.save(valenciaVenezuela);

    // Verify both exist
    List<RegionEntity> valenciaRegions = repository.findByNameContainingIgnoreCase("Valencia");
    assertThat(valenciaRegions).hasSize(2);
  }
}
