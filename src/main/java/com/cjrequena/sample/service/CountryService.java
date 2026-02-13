package com.cjrequena.sample.service;

import com.cjrequena.sample.configuration.CacheConfigurationProperties;
import com.cjrequena.sample.domain.mapper.CountryMapper;
import com.cjrequena.sample.domain.model.Country;
import com.cjrequena.sample.persistence.entity.CountryEntity;
import com.cjrequena.sample.persistence.repository.CountryRepository;
import com.cjrequena.sample.persistence.repository.cache.CountryCacheRedisHashOpsRepository;
import jakarta.annotation.PostConstruct;
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
  private final CountryCacheRedisHashOpsRepository countryCacheRedisHashOpsRepository;
  private final CacheConfigurationProperties cacheConfigurationProperties;
  private final CountryMapper countryMapper;

  @PostConstruct
  public void loadUpCache() {
    if(cacheConfigurationProperties.isFullLoadEnabled()) {
      List<Country> coutries = this.countryMapper.toDomainList(countryRepository.findAll());
      this.countryCacheRedisHashOpsRepository.load(coutries);
    }
  }

  // ================================================================
  // CRUD Standard Operations
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
    Country createdCountry = countryMapper.toDomain(savedEntity);

    // Update cache
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        countryCacheRedisHashOpsRepository.save(createdCountry);
        log.debug("Country cached with ID: {}", createdCountry.getId());
      } catch (Exception e) {
        log.warn("Failed to cache country on create: {}", createdCountry.getId(), e);
      }
    }

    log.info("Country created with ID: {}", savedEntity.getId());
    return createdCountry;
  }

  /**
   * Finds a country by ID.
   *
   * @param id the country ID
   * @return Optional containing the country if found
   */
  public Optional<Country> findById(UUID id) {
    log.debug("Finding country by ID: {}", id);

    // Try cache first (cache-aside pattern)
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        Optional<Country> cachedCountry = this.countryCacheRedisHashOpsRepository.retrieveById(id);
        if (cachedCountry.isPresent()) {
          log.debug("Country found in cache: {}", id);
          return cachedCountry;
        }
        log.debug("Country not found in cache, querying database: {}", id);
      } catch (Exception e) {
        log.warn("Cache retrieval failed for country: {}, falling back to database", id, e);
      }
    }

    // Cache miss or disabled - query database
    Optional<Country> country = countryRepository.findById(id).map(countryMapper::toDomain);

    // Update cache on successful database hit
    if (cacheConfigurationProperties.isCacheEnabled() && country.isPresent()) {
      try {
        countryCacheRedisHashOpsRepository.save(country.get());
        log.debug("Country cached after database query: {}", id);
      } catch (Exception e) {
        log.warn("Failed to cache country after database query: {}", id, e);
      }
    }

    return country;
  }

  /**
   * Finds all countries.
   *
   * @return list of all countries
   */
  public List<Country> findAll() {
    log.debug("Finding all countries");

    // Try cache first for full list retrieval
    if (cacheConfigurationProperties.isCacheEnabled() && !countryCacheRedisHashOpsRepository.isEmpty()) {
      try {
        List<Country> cachedCountries = countryCacheRedisHashOpsRepository.retrieve();
        if (!cachedCountries.isEmpty()) {
          log.debug("Retrieved {} countries from cache", cachedCountries.size());
          return cachedCountries;
        }
      } catch (Exception e) {
        log.warn("Cache retrieval failed for all countries, falling back to database", e);
      }
    }

    // Cache miss or disabled - query database
    List<Country> countries = countryRepository.findAll().stream()
      .map(countryMapper::toDomain)
      .collect(Collectors.toList());

    // Update cache with full list
    if (cacheConfigurationProperties.isCacheEnabled() && !countries.isEmpty()) {
      try {
        countryCacheRedisHashOpsRepository.saveAll(countries);
        log.debug("Cached {} countries after database query", countries.size());
      } catch (Exception e) {
        log.warn("Failed to cache countries after database query", e);
      }
    }

    return countries;
  }

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
    CountryEntity existingEntity = countryRepository
      .findById(id)
      .orElseThrow(() -> new IllegalArgumentException("Country not found with ID: " + id));

    CountryEntity updatedEntity = countryMapper.toEntity(country);
    updatedEntity.setId(existingEntity.getId());
    updatedEntity.setCreatedAt(existingEntity.getCreatedAt());

    CountryEntity savedEntity = countryRepository.save(updatedEntity);
    Country updatedCountry = countryMapper.toDomain(savedEntity);

    // Update cache
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        countryCacheRedisHashOpsRepository.save(updatedCountry);
        log.debug("Country cache updated with ID: {}", updatedCountry.getId());
      } catch (Exception e) {
        log.warn("Failed to update cache for country: {}", updatedCountry.getId(), e);
      }
    }

    log.info("Country updated with ID: {}", savedEntity.getId());
    return updatedCountry;
  }

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

    // Remove from cache
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        countryCacheRedisHashOpsRepository.deleteById(id);
        log.debug("Country removed from cache: {}", id);
      } catch (Exception e) {
        log.warn("Failed to remove country from cache: {}", id, e);
      }
    }

    log.info("Country deleted with ID: {}", id);
  }

  /**
   * Checks if a country exists by ID.
   *
   * @param id the country ID
   * @return true if exists, false otherwise
   */
  public boolean existsById(UUID id) {
    // Check cache first for existence
    if (cacheConfigurationProperties.isCacheEnabled()) {
      try {
        if (countryCacheRedisHashOpsRepository.existsById(id)) {
          log.debug("Country exists in cache: {}", id);
          return true;
        }
      } catch (Exception e) {
        log.warn("Cache existence check failed for country: {}, falling back to database", id, e);
      }
    }

    return countryRepository.existsById(id);
  }

  // ================================================================
  // Read Operations
  // ================================================================


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

  // ================================================================
  // Cache Management Operations
  // ================================================================

  /**
   * Manually refresh the cache from the database.
   * Useful for cache warming or recovery scenarios.
   */
  public void refreshCache() {
    if (!cacheConfigurationProperties.isCacheEnabled()) {
      log.warn("Cache is disabled, skipping refresh");
      return;
    }

    log.info("Refreshing country cache from database");
    try {
      List<Country> countries = countryRepository
        .findAll()
        .stream()
        .map(countryMapper::toDomain)
        .collect(Collectors.toList());

      countryCacheRedisHashOpsRepository.load(countries);
      log.info("Successfully refreshed cache with {} countries", countries.size());
    } catch (Exception e) {
      log.error("Failed to refresh country cache", e);
      throw new RuntimeException("Failed to refresh country cache", e);
    }
  }

  /**
   * Clear all countries from the cache.
   * Use with caution - this will force all subsequent reads to hit the database.
   */
  public void clearCache() {
    if (!cacheConfigurationProperties.isCacheEnabled()) {
      log.warn("Cache is disabled, skipping clear");
      return;
    }

    log.info("Clearing country cache");
    try {
      countryCacheRedisHashOpsRepository.clear();
      log.info("Successfully cleared country cache");
    } catch (Exception e) {
      log.error("Failed to clear country cache", e);
      throw new RuntimeException("Failed to clear country cache", e);
    }
  }

  /**
   * Get cache statistics.
   *
   * @return number of countries currently in cache
   */
  public long getCacheSize() {
    if (!cacheConfigurationProperties.isCacheEnabled()) {
      return 0L;
    }

    try {
      return countryCacheRedisHashOpsRepository.size();
    } catch (Exception e) {
      log.error("Failed to get cache size", e);
      return 0L;
    }
  }

  /**
   * Check if cache is empty.
   *
   * @return true if cache is empty or disabled
   */
  public boolean isCacheEmpty() {
    if (!cacheConfigurationProperties.isCacheEnabled()) {
      return true;
    }

    try {
      return countryCacheRedisHashOpsRepository.isEmpty();
    } catch (Exception e) {
      log.error("Failed to check if cache is empty", e);
      return true;
    }
  }

  /**
   * Warm up cache with active countries only.
   * More efficient than loading all countries when most queries are for active countries.
   */
  public void warmCacheWithActiveCountries() {
    if (!cacheConfigurationProperties.isCacheEnabled()) {
      log.warn("Cache is disabled, skipping warm-up");
      return;
    }

    log.info("Warming cache with active countries");
    try {
      List<Country> activeCountries = countryRepository.findAllByActiveTrue().stream()
        .map(countryMapper::toDomain)
        .collect(Collectors.toList());

      countryCacheRedisHashOpsRepository.saveAll(activeCountries);
      log.info("Successfully warmed cache with {} active countries", activeCountries.size());
    } catch (Exception e) {
      log.error("Failed to warm cache with active countries", e);
      throw new RuntimeException("Failed to warm cache with active countries", e);
    }
  }

  /**
   * Evict specific country from cache.
   * Useful for targeted cache invalidation.
   *
   * @param id the country ID to evict
   */
  public void evictFromCache(UUID id) {
    if (!cacheConfigurationProperties.isCacheEnabled()) {
      return;
    }

    log.debug("Evicting country from cache: {}", id);
    try {
      countryCacheRedisHashOpsRepository.deleteById(id);
      log.debug("Successfully evicted country from cache: {}", id);
    } catch (Exception e) {
      log.warn("Failed to evict country from cache: {}", id, e);
    }
  }

  /**
   * Batch evict multiple countries from cache.
   *
   * @param ids collection of country IDs to evict
   */
  public void evictMultipleFromCache(List<UUID> ids) {
    if (!cacheConfigurationProperties.isCacheEnabled() || ids == null || ids.isEmpty()) {
      return;
    }

    log.debug("Evicting {} countries from cache", ids.size());
    try {
      countryCacheRedisHashOpsRepository.deleteAll(ids);
      log.debug("Successfully evicted {} countries from cache", ids.size());
    } catch (Exception e) {
      log.warn("Failed to evict countries from cache", e);
    }
  }
}
