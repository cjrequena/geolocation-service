package com.cjrequena.sample.service;

import com.cjrequena.sample.configuration.CacheConfigurationProperties;
import com.cjrequena.sample.domain.exception.UniqueConstraintException;
import com.cjrequena.sample.domain.mapper.CountryMapper;
import com.cjrequena.sample.domain.model.Country;
import com.cjrequena.sample.persistence.entity.CountryEntity;
import com.cjrequena.sample.persistence.repository.CountryRepository;
import com.cjrequena.sample.persistence.repository.cache.CountryCacheRedisHashOpsRepository;
import com.cjrequena.sample.service.base.BaseService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
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
public class CountryService extends BaseService<CountryEntity, Country> {

  private final CountryRepository countryRepository;
  private final CountryCacheRedisHashOpsRepository countryCacheRedisHashOpsRepository;
  private final CacheConfigurationProperties cacheConfigurationProperties;
  private final CountryMapper countryMapper;

  // ================================================================
  // BaseService Implementation
  // ================================================================

  @Override
  protected JpaRepository<CountryEntity, ?> getRepository() {
    return countryRepository;
  }

  @Override
  protected JpaSpecificationExecutor<CountryEntity> getSpecificationExecutor() {
    return countryRepository;
  }

  @Override
  protected Function<CountryEntity, Country> getEntityToDomainMapper() {
    return countryMapper::toDomain;
  }

  @Override
  protected Class<CountryEntity> getEntityClass() {
    return CountryEntity.class;
  }

  // ================================================================
  // Cache Initialization
  // ================================================================

  @PostConstruct
  public void loadUpCache() {
    if (cacheConfigurationProperties.isFullLoadEnabled()) {
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

    try {
      CountryEntity entity = countryMapper.toEntity(country);
      CountryEntity savedEntity = countryRepository.saveAndFlush(entity);

      Country createdCountry = countryMapper.toDomain(savedEntity);

      // Cache update (best effort)
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

    } catch (DataIntegrityViolationException ex) {
      final String message = String.format("Unique constraint violation while creating country: %s", country.getName());
      log.warn(message);
      throw new UniqueConstraintException(message, ex);
    }
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
   * Finds all countries without any filtering or pagination.
   *
   * <p>This method tries cache first, then falls back to database if cache is disabled or empty.</p>
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
   * Finds all countries with optional RSQL filtering, sorting, and pagination.
   *
   * <p>This method does NOT use cache and always queries the database to ensure
   * accurate filtering and sorting results.</p>
   *
   * @param filters RSQL filter expression (e.g., "active==true;name=like='United'")
   * @param offset the offset for pagination (0-based)
   * @param limit the maximum number of results to return
   * @param sort the sort expression (e.g., "name,asc" or "name,desc;population,desc")
   * @return list of countries matching the criteria
   */
  public List<Country> findAll(String filters, Integer offset, Integer limit, String sort) {
    return super.findAllWithFiltersAndSort(filters, offset, limit, sort);
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

    try {
      CountryEntity updatedEntity = countryMapper.toEntity(country);
      updatedEntity.setId(existingEntity.getId());
      updatedEntity.setCreatedAt(existingEntity.getCreatedAt());

      CountryEntity savedEntity = countryRepository.saveAndFlush(updatedEntity);
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
    } catch (DataIntegrityViolationException ex) {
      final String message = String.format("Unique constraint violation while creating country: %s", country.getName());
      log.warn(message);
      throw new UniqueConstraintException(message, ex);
    }
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
