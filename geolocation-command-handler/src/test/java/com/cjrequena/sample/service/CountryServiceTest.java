package com.cjrequena.sample.service;

import com.cjrequena.sample.domain.mapper.CountryMapper;
import com.cjrequena.sample.domain.model.aggregate.Country;
import com.cjrequena.sample.persistence.entity.CountryEntity;
import com.cjrequena.sample.persistence.repository.CountryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CountryService}.
 * Uses Mockito to mock dependencies.
 *
 * @author cjrequena
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CountryService Unit Tests")
class CountryServiceTest {

  @Mock
  private CountryRepository countryRepository;

  @Mock
  private CountryMapper countryMapper;

  @InjectMocks
  private CountryService countryService;

  private Country countryDomain;
  private CountryEntity countryEntity;
  private UUID countryId;

  @BeforeEach
  void setUp() {
    countryId = UUID.randomUUID();

    countryDomain = new Country();
    countryDomain.setId(countryId);
    countryDomain.setName("Spain");

    countryEntity = new CountryEntity();
    countryEntity.setId(countryId);
    countryEntity.setName("Spain");
  }

  // ================================================================
  // Create Operations
  // ================================================================

  @Test
  @DisplayName("Should create country successfully")
  void shouldCreateCountry() {
    when(countryMapper.toEntity(countryDomain)).thenReturn(countryEntity);
    when(countryRepository.save(countryEntity)).thenReturn(countryEntity);
    when(countryMapper.toDomain(countryEntity)).thenReturn(countryDomain);

    Country result = countryService.create(countryDomain);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(countryId);
    verify(countryRepository).save(countryEntity);
  }

  // ================================================================
  // Read Operations
  // ================================================================

  @Test
  @DisplayName("Should find country by ID")
  void shouldFindById() {
    when(countryRepository.findById(countryId)).thenReturn(Optional.of(countryEntity));
    when(countryMapper.toDomain(countryEntity)).thenReturn(countryDomain);

    Optional<Country> result = countryService.findById(countryId);

    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(countryId);
  }

  @Test
  @DisplayName("Should return empty when country not found by ID")
  void shouldReturnEmptyWhenNotFoundById() {
    when(countryRepository.findById(countryId)).thenReturn(Optional.empty());

    Optional<Country> result = countryService.findById(countryId);

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Should find country by ISO Alpha-2")
  void shouldFindByIsoAlpha2() {
    when(countryRepository.findByIsoCodeAlpha2("ES")).thenReturn(Optional.of(countryEntity));
    when(countryMapper.toDomain(countryEntity)).thenReturn(countryDomain);

    Optional<Country> result = countryService.findByIsoAlpha2("ES");

    assertThat(result).isPresent();
  }

  @Test
  @DisplayName("Should find country by ISO Alpha-3")
  void shouldFindByIsoAlpha3() {
    when(countryRepository.findByIsoCodeAlpha3("ESP")).thenReturn(Optional.of(countryEntity));
    when(countryMapper.toDomain(countryEntity)).thenReturn(countryDomain);

    Optional<Country> result = countryService.findByIsoAlpha3("ESP");

    assertThat(result).isPresent();
  }

  @Test
  @DisplayName("Should find country by name")
  void shouldFindByName() {
    when(countryRepository.findByName("Spain")).thenReturn(Optional.of(countryEntity));
    when(countryMapper.toDomain(countryEntity)).thenReturn(countryDomain);

    Optional<Country> result = countryService.findByName("Spain");

    assertThat(result).isPresent();
  }

  @Test
  @DisplayName("Should find all countries")
  void shouldFindAll() {
    List<CountryEntity> entities = Arrays.asList(countryEntity);
    when(countryRepository.findAll()).thenReturn(entities);
    when(countryMapper.toDomain(any(CountryEntity.class))).thenReturn(countryDomain);

    List<Country> result = countryService.findAll();

    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("Should find all active countries")
  void shouldFindAllActive() {
    List<CountryEntity> entities = Arrays.asList(countryEntity);
    when(countryRepository.findAllByActiveTrue()).thenReturn(entities);
    when(countryMapper.toDomain(any(CountryEntity.class))).thenReturn(countryDomain);

    List<Country> result = countryService.findAllActive();

    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("Should find countries by active status with pagination")
  void shouldFindByActiveWithPagination() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<CountryEntity> entityPage = new PageImpl<>(Arrays.asList(countryEntity));
    when(countryRepository.findByActive(true, pageable)).thenReturn(entityPage);
    when(countryMapper.toDomain(any(CountryEntity.class))).thenReturn(countryDomain);

    Page<Country> result = countryService.findByActive(true, pageable);

    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("Should find countries by name containing")
  void shouldFindByNameContaining() {
    List<CountryEntity> entities = Arrays.asList(countryEntity);
    when(countryRepository.findByNameContainingIgnoreCase("spa")).thenReturn(entities);
    when(countryMapper.toDomain(any(CountryEntity.class))).thenReturn(countryDomain);

    List<Country> result = countryService.findByNameContaining("spa");

    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("Should find countries by currency code")
  void shouldFindByCurrencyCode() {
    List<CountryEntity> entities = Arrays.asList(countryEntity);
    when(countryRepository.findByCurrencyCode("EUR")).thenReturn(entities);
    when(countryMapper.toDomain(any(CountryEntity.class))).thenReturn(countryDomain);

    List<Country> result = countryService.findByCurrencyCode("EUR");

    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("Should find countries by population greater than")
  void shouldFindByPopulationGreaterThan() {
    List<CountryEntity> entities = Arrays.asList(countryEntity);
    when(countryRepository.findByPopulationGreaterThan(1000000L)).thenReturn(entities);
    when(countryMapper.toDomain(any(CountryEntity.class))).thenReturn(countryDomain);

    List<Country> result = countryService.findByPopulationGreaterThan(1000000L);

    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("Should find countries created between dates")
  void shouldFindByCreatedAtBetween() {
    OffsetDateTime start = OffsetDateTime.now().minusDays(7);
    OffsetDateTime end = OffsetDateTime.now();
    List<CountryEntity> entities = Arrays.asList(countryEntity);
    when(countryRepository.findByCreatedAtBetween(start, end)).thenReturn(entities);
    when(countryMapper.toDomain(any(CountryEntity.class))).thenReturn(countryDomain);

    List<Country> result = countryService.findByCreatedAtBetween(start, end);

    assertThat(result).hasSize(1);
  }

  // ================================================================
  // Update Operations
  // ================================================================

  @Test
  @DisplayName("Should update country successfully")
  void shouldUpdateCountry() {
    when(countryRepository.findById(countryId)).thenReturn(Optional.of(countryEntity));
    when(countryMapper.toEntity(countryDomain)).thenReturn(countryEntity);
    when(countryRepository.save(countryEntity)).thenReturn(countryEntity);
    when(countryMapper.toDomain(countryEntity)).thenReturn(countryDomain);

    Country result = countryService.update(countryId, countryDomain);

    assertThat(result).isNotNull();
    verify(countryRepository).save(countryEntity);
  }

  @Test
  @DisplayName("Should throw exception when updating non-existent country")
  void shouldThrowExceptionWhenUpdatingNonExistent() {
    when(countryRepository.findById(countryId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> countryService.update(countryId, countryDomain))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Country not found");
  }

  // ================================================================
  // Delete Operations
  // ================================================================

  @Test
  @DisplayName("Should delete country by ID")
  void shouldDeleteById() {
    when(countryRepository.existsById(countryId)).thenReturn(true);

    countryService.deleteById(countryId);

    verify(countryRepository).deleteById(countryId);
  }

  @Test
  @DisplayName("Should throw exception when deleting non-existent country")
  void shouldThrowExceptionWhenDeletingNonExistent() {
    when(countryRepository.existsById(countryId)).thenReturn(false);

    assertThatThrownBy(() -> countryService.deleteById(countryId))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Country not found");
  }

  // ================================================================
  // Existence Checks
  // ================================================================

  @Test
  @DisplayName("Should check if country exists by ID")
  void shouldCheckExistsById() {
    when(countryRepository.existsById(countryId)).thenReturn(true);

    boolean result = countryService.existsById(countryId);

    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("Should check if country exists by ISO Alpha-2")
  void shouldCheckExistsByIsoAlpha2() {
    when(countryRepository.existsByIsoCodeAlpha2("ES")).thenReturn(true);

    boolean result = countryService.existsByIsoAlpha2("ES");

    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("Should check if country exists by name")
  void shouldCheckExistsByName() {
    when(countryRepository.existsByName("Spain")).thenReturn(true);

    boolean result = countryService.existsByName("Spain");

    assertThat(result).isTrue();
  }

  // ================================================================
  // Count Operations
  // ================================================================

  @Test
  @DisplayName("Should count all countries")
  void shouldCount() {
    when(countryRepository.count()).thenReturn(5L);

    long result = countryService.count();

    assertThat(result).isEqualTo(5L);
  }
}
