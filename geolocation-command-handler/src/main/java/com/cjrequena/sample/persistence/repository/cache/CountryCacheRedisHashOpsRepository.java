package com.cjrequena.sample.persistence.repository.cache;

import com.cjrequena.sample.domain.exception.CacheException;
import com.cjrequena.sample.domain.model.Country;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Comprehensive Redis cache repository for Country entities.
 *
 * @author cjrequena
 */
@Repository
@Qualifier("countryCacheRedisRepository")
@Slf4j
public class CountryCacheRedisHashOpsRepository implements CacheRepository<UUID, Country> {

  /* =========================================================
   * Redis Key Constants
   * ========================================================= */
  private static final String KEY_PREFIX = "countries:hash";


  /* =========================================================
   * Redis Operations
   * ========================================================= */
  private final RedisTemplate<String, Country> redisTemplate;
  private final HashOperations<String, String, Country> hashOps;

  @Autowired
  public CountryCacheRedisHashOpsRepository(RedisTemplate<String, Country> redisTemplate) {
    this.redisTemplate = redisTemplate;
    this.hashOps = redisTemplate.opsForHash();
  }

  /* =========================================================
   * HASH Operations - Primary Storage
   * ========================================================= */

  @Override
  public void load(List<Country> countries) {
    Objects.requireNonNull(countries, "Countries list cannot be null");

    try {
      // Clear existing hash
      redisTemplate.delete(KEY_PREFIX);

      if (countries.isEmpty()) {
        log.info("No countries to load into Redis cache");
        return;
      }
      saveAll(countries);

      log.info("Loaded countries into hash storage");
    } catch (Exception e) {
      log.error("Failed to load countries into hash", e);
      throw new CacheException("Failed to load countries", e);
    }
  }

  @Override
  public void save(Country country) {
    validateCountry(country);

    try {
      hashOps.put(KEY_PREFIX, country.getId().toString(), country);
      log.debug("Added country to hash: {}", country.getId());
    } catch (Exception e) {
      log.error("Failed to save country to hash: {}", country.getId(), e);
      throw new CacheException("Failed to save country", e);
    }
  }

  @Override
  public void saveAll(List<Country> countries) {
    Objects.requireNonNull(countries, "Countries list cannot be null");

    // Convert to map and bulk insert
    try {
      Map<String, Country> countryMap = countries.stream()
        .filter(Objects::nonNull)
        .filter(country -> country.getId() != null)
        .collect(Collectors.toMap(
          country -> country.getId().toString(),
          country -> country
        ));

      hashOps.putAll(KEY_PREFIX, countryMap);
      log.debug("Saved {} countries to hash", countryMap.size());
    } catch (Exception e) {
      log.error("Failed to save countries batch", e);
      throw new CacheException("Failed to save countries batch", e);
    }
  }

  @Override
  public List<Country> retrieve() {
    try {
      Map<String, Country> countryMap = hashOps.entries(KEY_PREFIX);

      if (countryMap == null || countryMap.isEmpty()) {
        return Collections.emptyList();
      }

      return new ArrayList<>(countryMap.values());
    } catch (Exception e) {
      log.error("Failed to retrieve countries from hash", e);
      throw new CacheException("Failed to retrieve countries", e); // Don't return empty list silently
    }
  }

  @Override
  public Optional<Country> retrieveById(UUID id) {
    Objects.requireNonNull(id, "Id cannot be null");

    try {
      return Optional.ofNullable(hashOps.get(KEY_PREFIX, id.toString()));
    } catch (Exception e) {
      log.error("Failed to retrieve country from hash: {}", id, e);
      return Optional.empty();
    }
  }

  @Override
  public void deleteById(UUID id) {
    Objects.requireNonNull(id, "Id cannot be null");

    try {
      hashOps.delete(KEY_PREFIX, id.toString());
      log.debug("Deleted country from hash: {}", id);
    } catch (Exception e) {
      log.error("Failed to delete country from hash: {}", id, e);
      throw new CacheException("Failed to delete country", e);
    }
  }

  @Override
  public void deleteAll(Collection<UUID> ids) {
    Objects.requireNonNull(ids, "IDs collection cannot be null");

    try {
      String[] keys = ids.stream()
        .map(UUID::toString)
        .toArray(String[]::new);
      hashOps.delete(KEY_PREFIX, (Object[]) keys);
      log.debug("Deleted {} countries from hash", ids.size());
    } catch (Exception e) {
      log.error("Failed to delete countries batch", e);
      throw new CacheException("Failed to delete countries batch", e);
    }
  }

  @Override
  public boolean existsById(UUID id) {
    Objects.requireNonNull(id, "Id cannot be null");
    try {
      return hashOps.hasKey(KEY_PREFIX, id.toString());
    } catch (Exception e) {
      log.error("Failed to check existence of country: {}", id, e);
      return false;
    }
  }

  @Override
  public boolean isEmpty() {
    try {
      Long size = hashOps.size(KEY_PREFIX);
      return size == null || size == 0;
    } catch (Exception e) {
      log.error("Failed to check if hash is empty", e);
      return true;
    }
  }

  @Override
  public long size() {
    try {
      Long size = hashOps.size(KEY_PREFIX);
      return size != null ? size : 0L;
    } catch (Exception e) {
      log.error("Failed to get hash size", e);
      return 0L;
    }
  }

  @Override
  public void clear() {
    try {
      redisTemplate.delete(KEY_PREFIX);
      log.info("Cleared all countries from cache");
    } catch (Exception e) {
      log.error("Failed to clear cache", e);
      throw new CacheException("Failed to clear cache", e);
    }
  }

  /* =========================================================
   * VALIDATION METHODS
   * ========================================================= */

  private void validateCountry(Country country) {
    Objects.requireNonNull(country, "Country cannot be null");
    Objects.requireNonNull(country.getId(), "Country Id cannot be null");
  }

}
