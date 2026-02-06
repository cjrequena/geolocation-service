package com.cjrequena.sample.service;

import com.cjrequena.sample.domain.mapper.CountryMapper;
import com.cjrequena.sample.domain.model.aggregate.Country;
import com.cjrequena.sample.persistence.entity.CountryEntity;
import com.cjrequena.sample.persistence.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for Country aggregate operations.
 * 
 * <p>Handles business logic and orchestrates between domain model and persistence layer.
 * Uses CountryMapper to convert between domain aggregates and entities.</p>
 *
 * @author cjrequena
 */
@Log4j2
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CountryService {

  private final CountryRepository countryRepository;
  private final CountryMapper countryMapper;

  // ================================================================
  // Create Operations
  // ================================================================

  /**
   * Creates a new country.
   *
   * @param country the country domain aggregate to create
   * @return the created country with generated ID
   */
  @Transactional
  public Country create(Country country) {
    log.debug("Creating country: {}", country.getName());
    
    CountryEntity entity = countryMapper.toEntity(country);
    CountryEntity savedEntity = countryRepository.save(entity);
    
    log.info("Country created with ID: {}", savedEntity.getId());
    return countryMapper.toDomain(savedEntity);
  }

  // ================================================================
  // Read Operations
  // ================================================================

  /**
   * Finds a country by ID.
   *
   * @param id the country ID
   * @return Optional containing the country if found
   */
  public Optional<Country> findById(UUID id) {
    log.debug("Finding country by ID: {}", id);
    
    return countryRepository.findById(id)
      .map(countryMapper::toDomain);
  }

  /**
   * Finds a country by ISO Alpha-2 code.
   *
   * @param alpha2 the ISO Alpha-2 code (e.g., "ES", "US")
   * @return Optional containing the country if found
   */
  public Optional<Country> findByIsoAlpha2(String alpha2) {
    log.debug("Finding country by ISO Alpha-2: {}", alpha2);
    
    return countryRepository.findByIsoCodeAlpha2(alpha2)
      .map(countryMapper::toDomain);
  }

  /**
   * Finds a country by ISO Alpha-3 code.
   *
   * @param alpha3 the ISO Alpha-3 code (e.g., "ESP", "USA")
   * @return Optional containing the country if found
   */
  public Optional<Country> findByIsoAlpha3(String alpha3) {
    log.debug("Finding country by ISO Alpha-3: {}", alpha3);
    
    return countryRepository.findByIsoCodeAlpha3(alpha3)
      .map(countryMapper::toDomain);
  }

  /**
   * Finds a country by name.
   *
   * @param name the country name
   * @return Optional containing the country if found
   */
  public Optional<Country> findByName(String name) {
    log.debug("Finding country by name: {}", name);
    
    return countryRepository.findByName(name)
      .map(countryMapper::toDomain);
  }

  /**
   * Finds all countries.
   *
   * @return list of all countries
   */
  public List<Country> findAll() {
    log.debug("Finding all countries");
    
    return countryRepository.findAll().stream()
      .map(countryMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds all active countries.
   *
   * @return list of active countries
   */
  public List<Country> findAllActive() {
    log.debug("Finding all active countries");
    
    return countryRepository.findAllByActiveTrue().stream()
      .map(countryMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds countries by active status with pagination.
   *
   * @param active the active status
   * @param pageable pagination information
   * @return page of countries
   */
  public Page<Country> findByActive(Boolean active, Pageable pageable) {
    log.debug("Finding countries by active status: {} with pagination", active);
    
    return countryRepository.findByActive(active, pageable)
      .map(countryMapper::toDomain);
  }

  /**
   * Finds countries by name containing substring (case-insensitive).
   *
   * @param namePart the substring to search for
   * @return list of matching countries
   */
  public List<Country> findByNameContaining(String namePart) {
    log.debug("Finding countries by name containing: {}", namePart);
    
    return countryRepository.findByNameContainingIgnoreCase(namePart).stream()
      .map(countryMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds countries by currency code.
   *
   * @param currencyCode the currency code (e.g., "EUR", "USD")
   * @return list of countries using the currency
   */
  public List<Country> findByCurrencyCode(String currencyCode) {
    log.debug("Finding countries by currency code: {}", currencyCode);
    
    return countryRepository.findByCurrencyCode(currencyCode).stream()
      .map(countryMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds countries with population greater than threshold.
   *
   * @param minPopulation the minimum population
   * @return list of countries
   */
  public List<Country> findByPopulationGreaterThan(Long minPopulation) {
    log.debug("Finding countries with population > {}", minPopulation);
    
    return countryRepository.findByPopulationGreaterThan(minPopulation).stream()
      .map(countryMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds countries created within a time range.
   *
   * @param start start date/time
   * @param end end date/time
   * @return list of countries
   */
  public List<Country> findByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end) {
    log.debug("Finding countries created between {} and {}", start, end);
    
    return countryRepository.findByCreatedAtBetween(start, end).stream()
      .map(countryMapper::toDomain)
      .collect(Collectors.toList());
  }

  // ================================================================
  // Update Operations
  // ================================================================

  /**
   * Updates an existing country.
   *
   * @param id the country ID
   * @param country the updated country data
   * @return the updated country
   * @throws IllegalArgumentException if country not found
   */
  @Transactional
  public Country update(UUID id, Country country) {
    log.debug("Updating country with ID: {}", id);
    
    CountryEntity existingEntity = countryRepository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("Country not found with ID: " + id));
    
    // Map updated domain to entity
    CountryEntity updatedEntity = countryMapper.toEntity(country);
    updatedEntity.setId(existingEntity.getId());
    updatedEntity.setCreatedAt(existingEntity.getCreatedAt());
    
    CountryEntity savedEntity = countryRepository.save(updatedEntity);
    
    log.info("Country updated with ID: {}", savedEntity.getId());
    return countryMapper.toDomain(savedEntity);
  }

  // ================================================================
  // Delete Operations
  // ================================================================

  /**
   * Deletes a country by ID.
   *
   * @param id the country ID
   */
  @Transactional
  public void deleteById(UUID id) {
    log.debug("Deleting country with ID: {}", id);
    
    if (!countryRepository.existsById(id)) {
      throw new IllegalArgumentException("Country not found with ID: " + id);
    }
    
    countryRepository.deleteById(id);
    log.info("Country deleted with ID: {}", id);
  }

  // ================================================================
  // Existence Checks
  // ================================================================

  /**
   * Checks if a country exists by ID.
   *
   * @param id the country ID
   * @return true if exists, false otherwise
   */
  public boolean existsById(UUID id) {
    return countryRepository.existsById(id);
  }

  /**
   * Checks if a country exists by ISO Alpha-2 code.
   *
   * @param alpha2 the ISO Alpha-2 code
   * @return true if exists, false otherwise
   */
  public boolean existsByIsoAlpha2(String alpha2) {
    return countryRepository.existsByIsoCodeAlpha2(alpha2);
  }

  /**
   * Checks if a country exists by name.
   *
   * @param name the country name
   * @return true if exists, false otherwise
   */
  public boolean existsByName(String name) {
    return countryRepository.existsByName(name);
  }

  // ================================================================
  // Count Operations
  // ================================================================

  /**
   * Counts all countries.
   *
   * @return total count of countries
   */
  public long count() {
    return countryRepository.count();
  }
}
