package com.cjrequena.sample.persistence.repository;

import com.cjrequena.sample.persistence.entity.CountryEntity;
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
 * Integration tests for {@link CountryRepository}.
 * Uses PostgreSQL for testing repository queries.
 *
 * Prerequisites:
 * - PostgreSQL must be running
 * - Use docker-compose-test.yml to start test database
 * - Ensure application-local.properties has correct database configuration
 */
@SpringBootTest
@ActiveProfiles("integrationTest")
@DisplayName("CountryRepository Integration Tests")
class CountryRepositoryIT {

  @Autowired
  private CountryRepository repository;

  // Test data
  private CountryEntity spain;
  private CountryEntity unitedStates;
  private CountryEntity france;
  private CountryEntity germany;
  private CountryEntity china;
  private CountryEntity inactiveCountry;

  @BeforeEach
  void setUp() {
    // Clear database
    repository.deleteAll();

    // Create test data
    setupTestData();
  }

  private void setupTestData() {
    // Spain - Active
    spain = createCountry(
      "Spain",
      "ES",
      "ESP",
      "724",
      "+34",
      "EUR",
      "Madrid",
      47_000_000L,
      true
    );

    // United States - Active
    unitedStates = createCountry(
      "United States",
      "US",
      "USA",
      "840",
      "+1",
      "USD",
      "Washington, D.C.",
      331_000_000L,
      true
    );

    // France - Active
    france = createCountry(
      "France",
      "FR",
      "FRA",
      "250",
      "+33",
      "EUR",
      "Paris",
      67_000_000L,
      true
    );

    // Germany - Active
    germany = createCountry(
      "Germany",
      "DE",
      "DEU",
      "276",
      "+49",
      "EUR",
      "Berlin",
      83_000_000L,
      true
    );

    // China - Active (largest population)
    china = createCountry(
      "China",
      "CN",
      "CHN",
      "156",
      "+86",
      "CNY",
      "Beijing",
      1_400_000_000L,
      true
    );

    // Inactive country
    inactiveCountry = createCountry(
      "Historical Country",
      "XX",
      "XXX",
      "999",
      "+99",
      "XXX",
      "Old Capital",
      1_000_000L,
      false
    );

    // Save all
    repository.saveAll(List.of(
      spain,
      unitedStates,
      france,
      germany,
      china,
      inactiveCountry
    ));
  }

  private CountryEntity createCountry(
    String name,
    String alpha2,
    String alpha3,
    String numeric,
    String phoneCode,
    String currencyCode,
    String capital,
    Long population,
    Boolean active
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

  // ================================================================
  // Basic CRUD Tests
  // ================================================================

  @Test
  @DisplayName("Should save and find country by ID")
  void shouldSaveAndFindById() {
    // Given
    UUID id = spain.getId();

    // When
    Optional<CountryEntity> found = repository.findById(id);

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(id);
    assertThat(found.get().getName()).isEqualTo("Spain");
    assertThat(found.get().getIsoCodeAlpha2()).isEqualTo("ES");
  }

  @Test
  @DisplayName("Should find all countries")
  void shouldFindAll() {
    // When
    List<CountryEntity> all = repository.findAll();

    // Then
    assertThat(all).hasSize(6);
  }

  @Test
  @DisplayName("Should delete country")
  void shouldDelete() {
    // Given
    UUID id = spain.getId();

    // When
    repository.deleteById(id);
    Optional<CountryEntity> found = repository.findById(id);

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("Should update country")
  void shouldUpdate() {
    // Given
    spain.setCapital("Nueva Madrid");
    spain.setPopulation(48_000_000L);

    // When
    CountryEntity updated = repository.save(spain);

    // Then
    assertThat(updated.getCapital()).isEqualTo("Nueva Madrid");
    assertThat(updated.getPopulation()).isEqualTo(48_000_000L);
  }

  // ================================================================
  // Active/Inactive Filtering Tests
  // ================================================================

  @Test
  @DisplayName("Should find all active countries")
  void shouldFindAllActiveCountries() {
    // When
    List<CountryEntity> active = repository.findAllByActiveTrue();

    // Then
    assertThat(active).hasSize(5);
    assertThat(active).allMatch(CountryEntity::getActive);
    assertThat(active).extracting(CountryEntity::getName)
      .contains("Spain", "United States", "France", "Germany", "China");
  }

  @Test
  @DisplayName("Should find all inactive countries")
  void shouldFindAllInactiveCountries() {
    // When
    List<CountryEntity> inactive = repository.findAllByActiveFalse();

    // Then
    assertThat(inactive).hasSize(1);
    assertThat(inactive).allMatch(c -> !c.getActive());
    assertThat(inactive.get(0).getName()).isEqualTo("Historical Country");
  }

  @Test
  @DisplayName("Should find countries by active status with pagination")
  void shouldFindByActiveWithPagination() {
    // When
    Page<CountryEntity> page = repository.findByActive(true, PageRequest.of(0, 3));

    // Then
    assertThat(page.getContent()).hasSize(3);
    assertThat(page.getTotalElements()).isEqualTo(5);
    assertThat(page.getTotalPages()).isEqualTo(2);
    assertThat(page.getContent()).allMatch(CountryEntity::getActive);
  }

  // ================================================================
  // ISO Code Query Tests
  // ================================================================

  @Test
  @DisplayName("Should find country by ISO alpha-2 code")
  void shouldFindByIsoCodeAlpha2() {
    // When
    Optional<CountryEntity> found = repository.findByIsoCodeAlpha2("ES");

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo("Spain");
    assertThat(found.get().getIsoCodeAlpha2()).isEqualTo("ES");
  }

  @Test
  @DisplayName("Should not find country by non-existent alpha-2 code")
  void shouldNotFindByNonExistentAlpha2() {
    // When
    Optional<CountryEntity> found = repository.findByIsoCodeAlpha2("ZZ");

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("Should find country by ISO alpha-3 code")
  void shouldFindByIsoCodeAlpha3() {
    // When
    Optional<CountryEntity> found = repository.findByIsoCodeAlpha3("USA");

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo("United States");
    assertThat(found.get().getIsoCodeAlpha3()).isEqualTo("USA");
  }

  @Test
  @DisplayName("Should find country by ISO numeric code")
  void shouldFindByIsoCodeNumeric() {
    // When
    Optional<CountryEntity> found = repository.findByIsoCodeNumeric("724");

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo("Spain");
    assertThat(found.get().getIsoCodeNumeric()).isEqualTo("724");
  }

  @Test
  @DisplayName("Should find active country by alpha-2 code")
  void shouldFindActiveCountryByAlpha2() {
    // When
    Optional<CountryEntity> active = repository.findByIsoCodeAlpha2AndActiveTrue("ES");
    Optional<CountryEntity> inactive = repository.findByIsoCodeAlpha2AndActiveTrue("XX");

    // Then
    assertThat(active).isPresent();
    assertThat(active.get().getName()).isEqualTo("Spain");
    assertThat(inactive).isEmpty(); // XX is inactive
  }

  @Test
  @DisplayName("Should find active country by alpha-3 code")
  void shouldFindActiveCountryByAlpha3() {
    // When
    Optional<CountryEntity> active = repository.findByIsoCodeAlpha3AndActiveTrue("USA");
    Optional<CountryEntity> inactive = repository.findByIsoCodeAlpha3AndActiveTrue("XXX");

    // Then
    assertThat(active).isPresent();
    assertThat(active.get().getName()).isEqualTo("United States");
    assertThat(inactive).isEmpty(); // XXX is inactive
  }

  // ================================================================
  // Name-based Query Tests
  // ================================================================

  @Test
  @DisplayName("Should find country by exact name")
  void shouldFindByName() {
    // When
    Optional<CountryEntity> found = repository.findByName("Spain");

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo("Spain");
  }

  @Test
  @DisplayName("Should not find country by case-mismatched name")
  void shouldNotFindByCaseMismatchedName() {
    // When - findByName is case-sensitive
    Optional<CountryEntity> found = repository.findByName("spain");

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("Should find countries by name containing substring (case-insensitive)")
  void shouldFindByNameContainingIgnoreCase() {
    // When
    List<CountryEntity> found = repository.findByNameContainingIgnoreCase("united");

    // Then
    assertThat(found).hasSize(1);
    assertThat(found.get(0).getName()).isEqualTo("United States");
  }

  @Test
  @DisplayName("Should find multiple countries by name substring")
  void shouldFindMultipleCountriesByNameSubstring() {
    // When - Both Germany and China contain "n"
    List<CountryEntity> found = repository.findByNameContainingIgnoreCase("a");

    // Then
    assertThat(found).hasSizeGreaterThanOrEqualTo(3);
    assertThat(found).extracting(CountryEntity::getName)
      .contains("Spain", "France", "Germany");
  }

  @Test
  @DisplayName("Should find active countries by name containing substring")
  void shouldFindActiveCountriesByNameContaining() {
    // When
    List<CountryEntity> found = repository.findByNameContainingIgnoreCaseAndActiveTrue("country");

    // Then
    assertThat(found).isEmpty(); // "Historical Country" is inactive
  }

  // ================================================================
  // Phone/Currency Code Query Tests
  // ================================================================

  @Test
  @DisplayName("Should find countries by phone code")
  void shouldFindByPhoneCode() {
    // When
    List<CountryEntity> found = repository.findByPhoneCode("+1");

    // Then
    assertThat(found).hasSize(1);
    assertThat(found.get(0).getName()).isEqualTo("United States");
  }

  @Test
  @DisplayName("Should find multiple countries with same currency code")
  void shouldFindCountriesByCurrencyCode() {
    // When - Spain, France, Germany all use EUR
    List<CountryEntity> found = repository.findByCurrencyCode("EUR");

    // Then
    assertThat(found).hasSize(3);
    assertThat(found).extracting(CountryEntity::getName)
      .containsExactlyInAnyOrder("Spain", "France", "Germany");
  }

  @Test
  @DisplayName("Should find country with unique currency code")
  void shouldFindCountryWithUniqueCurrency() {
    // When
    List<CountryEntity> found = repository.findByCurrencyCode("USD");

    // Then
    assertThat(found).hasSize(1);
    assertThat(found.get(0).getName()).isEqualTo("United States");
  }

  // ================================================================
  // Population Query Tests
  // ================================================================

  @Test
  @DisplayName("Should find countries with population greater than threshold")
  void shouldFindByPopulationGreaterThan() {
    // When - Find countries with more than 100 million people
    List<CountryEntity> found = repository.findByPopulationGreaterThan(100_000_000L);

    // Then
    assertThat(found).hasSize(2);
    assertThat(found).extracting(CountryEntity::getName)
      .containsExactlyInAnyOrder("United States", "China");
  }

  @Test
  @DisplayName("Should find countries with population greater than 50 million")
  void shouldFindCountriesWithPopulationGreaterThan50Million() {
    // When
    List<CountryEntity> found = repository.findByPopulationGreaterThan(50_000_000L);

    // Then
    assertThat(found).hasSize(4);
    assertThat(found).extracting(CountryEntity::getName)
      .contains("United States", "France", "Germany", "China");
  }

  @Test
  @DisplayName("Should find top countries ordered by population descending")
  void shouldFindTopCountriesByPopulation() {
    // When - Get top 3 countries by population
    Page<CountryEntity> page = repository.findAllByOrderByPopulationDesc(PageRequest.of(0, 3));

    // Then
    assertThat(page.getContent()).hasSize(3);
    assertThat(page.getContent().get(0).getName()).isEqualTo("China"); // Largest
    assertThat(page.getContent().get(1).getName()).isEqualTo("United States");
    assertThat(page.getContent().get(2).getName()).isEqualTo("Germany");

    // Verify descending order
    List<Long> populations = page.getContent().stream()
      .map(CountryEntity::getPopulation)
      .toList();
    assertThat(populations).isSortedAccordingTo((a, b) -> Long.compare(b, a));
  }

  // ================================================================
  // Temporal Query Tests
  // ================================================================

  @Test
  @DisplayName("Should find countries created within time range")
  void shouldFindByCreatedAtBetween() {
    // Given
    OffsetDateTime start = OffsetDateTime.now().minusHours(1);
    OffsetDateTime end = OffsetDateTime.now().plusHours(1);

    // When
    List<CountryEntity> found = repository.findByCreatedAtBetween(start, end);

    // Then
    assertThat(found).hasSize(6); // All test countries
  }

  @Test
  @DisplayName("Should find countries created after specific time")
  void shouldFindCountriesCreatedAfterSpecificTime() {
    // Given
    OffsetDateTime cutoff = OffsetDateTime.now().minusMinutes(5);

    // When
    List<CountryEntity> found = repository.findByCreatedAtBetween(
      cutoff,
      OffsetDateTime.now().plusHours(1)
    );

    // Then
    assertThat(found).hasSizeGreaterThanOrEqualTo(6);
  }

  @Test
  @DisplayName("Should find top 10 most recently updated countries")
  void shouldFindTop10ByOrderByUpdatedAtDesc() {
    // When
    List<CountryEntity> found = repository.findTop10ByOrderByUpdatedAtDesc();

    // Then
    assertThat(found).hasSizeLessThanOrEqualTo(10);
    assertThat(found).hasSizeLessThanOrEqualTo(6); // We only have 6 countries
  }

  @Test
  @DisplayName("Should return most recently updated countries first")
  void shouldReturnMostRecentlyUpdatedFirst() throws InterruptedException {
    // Given - Update France
    Thread.sleep(10);
    france.setPopulation(68_000_000L);
    repository.save(france);

    // When
    List<CountryEntity> found = repository.findTop10ByOrderByUpdatedAtDesc();

    // Then
    assertThat(found).isNotEmpty();
    assertThat(found.get(0).getName()).isEqualTo("France");
  }

  // ================================================================
  // Existence Check Tests
  // ================================================================

  @Test
  @DisplayName("Should check if country exists by alpha-2 code")
  void shouldCheckExistsByIsoCodeAlpha2() {
    // When
    boolean exists = repository.existsByIsoCodeAlpha2("ES");
    boolean notExists = repository.existsByIsoCodeAlpha2("ZZ");

    // Then
    assertThat(exists).isTrue();
    assertThat(notExists).isFalse();
  }

  @Test
  @DisplayName("Should check if country exists by alpha-3 code")
  void shouldCheckExistsByIsoCodeAlpha3() {
    // When
    boolean exists = repository.existsByIsoCodeAlpha3("USA");
    boolean notExists = repository.existsByIsoCodeAlpha3("ZZZ");

    // Then
    assertThat(exists).isTrue();
    assertThat(notExists).isFalse();
  }

  @Test
  @DisplayName("Should check if country exists by name")
  void shouldCheckExistsByName() {
    // When
    boolean exists = repository.existsByName("Spain");
    boolean notExists = repository.existsByName("Atlantis");

    // Then
    assertThat(exists).isTrue();
    assertThat(notExists).isFalse();
  }

  @Test
  @DisplayName("Should check if active country exists by alpha-2 code")
  void shouldCheckExistsByIsoCodeAlpha2AndActiveTrue() {
    // When
    boolean activeExists = repository.existsByIsoCodeAlpha2AndActiveTrue("ES");
    boolean inactiveExists = repository.existsByIsoCodeAlpha2AndActiveTrue("XX");

    // Then
    assertThat(activeExists).isTrue();
    assertThat(inactiveExists).isFalse(); // XX is inactive
  }

  // ================================================================
  // Edge Case Tests
  // ================================================================

  @Test
  @DisplayName("Should handle empty result sets")
  void shouldHandleEmptyResults() {
    // When
    List<CountryEntity> found = repository.findByPhoneCode("+999");

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("Should handle pagination beyond available data")
  void shouldHandlePaginationBeyondData() {
    // When - Request page 100
    Page<CountryEntity> page = repository.findByActive(true, PageRequest.of(100, 10));

    // Then
    assertThat(page.getContent()).isEmpty();
    assertThat(page.getTotalElements()).isEqualTo(5);
    assertThat(page.getTotalPages()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should handle case-insensitive search correctly")
  void shouldHandleCaseInsensitiveSearch() {
    // When
    List<CountryEntity> upperCase = repository.findByNameContainingIgnoreCase("SPAIN");
    List<CountryEntity> lowerCase = repository.findByNameContainingIgnoreCase("spain");
    List<CountryEntity> mixedCase = repository.findByNameContainingIgnoreCase("SpAiN");

    // Then
    assertThat(upperCase).hasSize(1);
    assertThat(lowerCase).hasSize(1);
    assertThat(mixedCase).hasSize(1);
    assertThat(upperCase).isEqualTo(lowerCase);
    assertThat(lowerCase).isEqualTo(mixedCase);
  }

  @Test
  @DisplayName("Should handle special characters in search")
  void shouldHandleSpecialCharactersInSearch() {
    // Given - Create country with special characters
    CountryEntity ivoryCoast = createCountry(
      "Côte d'Ivoire",
      "CI",
      "CIV",
      "384",
      "+225",
      "XOF",
      "Yamoussoukro",
      26_000_000L,
      true
    );
    repository.save(ivoryCoast);

    // When
    List<CountryEntity> found = repository.findByNameContainingIgnoreCase("Côte");

    // Then
    assertThat(found).hasSize(1);
    assertThat(found.get(0).getName()).isEqualTo("Côte d'Ivoire");
  }

  @Test
  @DisplayName("Should maintain data integrity after multiple updates")
  void shouldMaintainDataIntegrityAfterUpdates() {
    // Given - Get the entity from database to have the actual persisted timestamp
    UUID originalId = spain.getId();
    CountryEntity fromDb = repository.findById(originalId).orElseThrow();
    OffsetDateTime originalCreatedAt = fromDb.getCreatedAt();

    // When - Multiple updates
    fromDb.setPopulation(47_500_000L);
    repository.save(fromDb);

    fromDb.setPopulation(48_000_000L);
    repository.save(fromDb);

    // Then
    CountryEntity updated = repository.findById(originalId).orElseThrow();
    assertThat(updated.getId()).isEqualTo(originalId);
    // CreatedAt should remain unchanged across updates
    assertThat(updated.getCreatedAt()).isEqualTo(originalCreatedAt);
    assertThat(updated.getPopulation()).isEqualTo(48_000_000L);
    // UpdatedAt should be after createdAt
    assertThat(updated.getUpdatedAt()).isAfter(originalCreatedAt);
  }

  @Test
  @DisplayName("Should enforce unique constraint on alpha-2 code")
  void shouldEnforceUniqueConstraintOnAlpha2() {
    // Given
    CountryEntity duplicate = createCountry(
      "Duplicate Spain",
      "ES", // Duplicate alpha-2
      "DSP",
      "999",
      "+999",
      "EUR",
      "Duplicate Madrid",
      1_000_000L,
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
  @DisplayName("Should enforce unique constraint on alpha-3 code")
  void shouldEnforceUniqueConstraintOnAlpha3() {
    // Given
    CountryEntity duplicate = createCountry(
      "Duplicate USA",
      "DU",
      "USA", // Duplicate alpha-3
      "999",
      "+999",
      "USD",
      "Duplicate DC",
      1_000_000L,
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
}
