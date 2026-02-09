package com.cjrequena.sample.service;

import com.cjrequena.sample.domain.model.aggregate.Country;
import com.cjrequena.sample.domain.model.vo.AuditInfoVO;
import com.cjrequena.sample.domain.model.vo.IsoCodeVO;
import com.cjrequena.sample.domain.model.vo.PopulationVO;
import com.cjrequena.sample.persistence.repository.CountryRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link CountryService}.
 * Tests the service layer with real database and mapper interactions.
 *
 * @author cjrequena
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("CountryService Integration Tests")
class CountryServiceIT {

  @Autowired
  private CountryService countryService;

  @Autowired
  private CountryRepository countryRepository;

  @BeforeEach
  void setUp() {
    countryRepository.deleteAll();
  }

  // ================================================================
  // Create Operations
  // ================================================================

  @Test
  @DisplayName("Should create country successfully")
  void shouldCreateCountry() {
    Country country = createCountryDomain("Spain", "ES", "ESP", true);

    Country result = countryService.create(country);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isNotNull();
    assertThat(result.getName()).isEqualTo("Spain");
    assertThat(result.getIsoCode().getAlpha2()).isEqualTo("ES");
  }

  // ================================================================
  // Read Operations
  // ================================================================

  @Test
  @DisplayName("Should find country by ID")
  void shouldFindById() {
    Country created = countryService.create(createCountryDomain("Spain", "ES", "ESP", true));

    Optional<Country> result = countryService.findById(created.getId());

    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo("Spain");
  }

  @Test
  @DisplayName("Should return empty when country not found by ID")
  void shouldReturnEmptyWhenNotFoundById() {
    Optional<Country> result = countryService.findById(UUID.randomUUID());

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Should find country by ISO Alpha-2")
  void shouldFindByIsoAlpha2() {
    countryService.create(createCountryDomain("Spain", "ES", "ESP", true));

    Optional<Country> result = countryService.findByIsoAlpha2("ES");

    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo("Spain");
  }

  @Test
  @DisplayName("Should find country by ISO Alpha-3")
  void shouldFindByIsoAlpha3() {
    countryService.create(createCountryDomain("Spain", "ES", "ESP", true));

    Optional<Country> result = countryService.findByIsoAlpha3("ESP");

    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo("Spain");
  }

  @Test
  @DisplayName("Should find country by name")
  void shouldFindByName() {
    countryService.create(createCountryDomain("Spain", "ES", "ESP", true));

    Optional<Country> result = countryService.findByName("Spain");

    assertThat(result).isPresent();
  }

  @Test
  @DisplayName("Should find all countries")
  void shouldFindAll() {
    countryService.create(createCountryDomain("Spain", "ES", "ESP", true));
    countryService.create(createCountryDomain("France", "FR", "FRA", true));

    List<Country> result = countryService.findAll();

    assertThat(result).hasSizeGreaterThan(0);
  }

  @Test
  @DisplayName("Should find all active countries")
  void shouldFindAllActive() {
    countryService.create(createCountryDomain("Spain", "ES", "ESP", true));
    countryService.create(createCountryDomain("France", "FR", "FRA", false));

    List<Country> result = countryService.findAllActive();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getName()).isEqualTo("Spain");
  }

  @Test
  @DisplayName("Should find countries by active status with pagination")
  void shouldFindByActiveWithPagination() {
    countryService.create(createCountryDomain("Spain", "ES", "ESP", true));
    countryService.create(createCountryDomain("France", "FR", "FRA", true));

    Page<Country> result = countryService.findByActive(true, PageRequest.of(0, 10));

    assertThat(result.getContent()).hasSize(2);
  }

  @Test
  @DisplayName("Should find countries by name containing")
  void shouldFindByNameContaining() {
    countryService.create(createCountryDomain("Spain", "ES", "ESP", true));
    countryService.create(createCountryDomain("France", "FR", "FRA", true));

    List<Country> result = countryService.findByNameContaining("spa");

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getName()).isEqualTo("Spain");
  }

  @Test
  @DisplayName("Should find countries by population greater than")
  void shouldFindByPopulationGreaterThan() {
    Country spain = createCountryDomain("Spain", "ES", "ESP", true);
    spain.setPopulation(PopulationVO.of(47_000_000L));
    countryService.create(spain);

    Country france = createCountryDomain("France", "FR", "FRA", true);
    france.setPopulation(PopulationVO.of(67_000_000L));
    countryService.create(france);

    List<Country> result = countryService.findByPopulationGreaterThan(50_000_000L);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getName()).isEqualTo("France");
  }

  // ================================================================
  // Update Operations
  // ================================================================

  @Test
  @DisplayName("Should update country successfully")
  void shouldUpdateCountry() {
    Country created = countryService.create(createCountryDomain("Spain", "ES", "ESP", true));

    created.setName("Kingdom of Spain");
    Country updated = countryService.update(created.getId(), created);

    assertThat(updated.getName()).isEqualTo("Kingdom of Spain");
  }

  @Test
  @DisplayName("Should throw exception when updating non-existent country")
  void shouldThrowExceptionWhenUpdatingNonExistent() {
    Country country = createCountryDomain("Spain", "ES", "ESP", true);

    assertThatThrownBy(() -> countryService.update(UUID.randomUUID(), country))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Country not found");
  }

  // ================================================================
  // Delete Operations
  // ================================================================

  @Test
  @DisplayName("Should delete country by ID")
  void shouldDeleteById() {
    Country created = countryService.create(createCountryDomain("Spain", "ES", "ESP", true));

    countryService.deleteById(created.getId());

    assertThat(countryService.findById(created.getId())).isEmpty();
  }

  @Test
  @DisplayName("Should throw exception when deleting non-existent country")
  void shouldThrowExceptionWhenDeletingNonExistent() {
    assertThatThrownBy(() -> countryService.deleteById(UUID.randomUUID()))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Country not found");
  }

  // ================================================================
  // Existence Checks
  // ================================================================

  @Test
  @DisplayName("Should check if country exists by ID")
  void shouldCheckExistsById() {
    Country created = countryService.create(createCountryDomain("Spain", "ES", "ESP", true));

    boolean result = countryService.existsById(created.getId());

    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("Should check if country exists by ISO Alpha-2")
  void shouldCheckExistsByIsoAlpha2() {
    countryService.create(createCountryDomain("Spain", "ES", "ESP", true));

    boolean result = countryService.existsByIsoAlpha2("ES");

    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("Should check if country exists by name")
  void shouldCheckExistsByName() {
    countryService.create(createCountryDomain("Spain", "ES", "ESP", true));

    boolean result = countryService.existsByName("Spain");

    assertThat(result).isTrue();
  }

  // ================================================================
  // Count Operations
  // ================================================================

  @Test
  @DisplayName("Should count all countries")
  void shouldCount() {
    countryService.create(createCountryDomain("Spain", "ES", "ESP", true));
    countryService.create(createCountryDomain("France", "FR", "FRA", true));

    long result = countryService.count();

    assertThat(result).isEqualTo(2L);
  }

  // ================================================================
  // Helper Methods
  // ================================================================

  private Country createCountryDomain(String name, String alpha2, String alpha3, boolean active) {
    Country country = new Country();
    country.setName(name);
    country.setIsoCode(IsoCodeVO.of(alpha2, alpha3, "000"));
    country.setActive(active);
    country.setAuditInfo(AuditInfoVO.of(OffsetDateTime.now(), OffsetDateTime.now()));
    return country;
  }
}
