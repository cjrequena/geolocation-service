package com.cjrequena.sample.persistence.repository.cache;

import com.cjrequena.sample.domain.exception.CacheException;
import com.cjrequena.sample.domain.model.City;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Comprehensive Redis cache repository for City entities.
 *
 * @author cjrequena
 */
@Repository
@Qualifier("cityCacheRedisRepository")
@Slf4j
public class CityCacheRedisHashOpsRepository implements CacheRepository<UUID, City> {

  /* =========================================================
   * Redis Key Constants
   * ========================================================= */
  private static final String KEY_PREFIX = "cities:hash";


  /* =========================================================
   * Redis Operations
   * ========================================================= */
  private final RedisTemplate<String, City> redisTemplate;
  private final HashOperations<String, String, City> hashOps;

  @Autowired
  public CityCacheRedisHashOpsRepository(RedisTemplate<String, City> redisTemplate) {
    this.redisTemplate = redisTemplate;
    this.hashOps = redisTemplate.opsForHash();
  }

  /* =========================================================
   * HASH Operations - Primary Storage
   * ========================================================= */

  @Override
  public void load(List<City> cities) {
    Objects.requireNonNull(cities, "Cities list cannot be null");

    try {
      // Clear existing hash
      redisTemplate.delete(KEY_PREFIX);

      if (cities.isEmpty()) {
        log.info("No cities to load into Redis cache");
        return;
      }
      saveAll(cities);

      log.info("Loaded cities into hash storage");
    } catch (Exception e) {
      log.error("Failed to load cities into hash", e);
      throw new CacheException("Failed to load cities", e);
    }
  }

  @Override
  public void save(City city) {
    validateCity(city);

    try {
      hashOps.put(KEY_PREFIX, city.getId().toString(), city);
      log.debug("Added city to hash: {}", city.getId());
    } catch (Exception e) {
      log.error("Failed to save city to hash: {}", city.getId(), e);
      throw new CacheException("Failed to save city", e);
    }
  }

  @Override
  public void saveAll(List<City> cities) {
    Objects.requireNonNull(cities, "Cities list cannot be null");

    // Convert to map and bulk insert
    try {
      Map<String, City> cityMap = cities.stream()
        .filter(Objects::nonNull)
        .filter(city -> city.getId() != null)
        .collect(Collectors.toMap(
          city -> city.getId().toString(),
          city -> city
        ));

      hashOps.putAll(KEY_PREFIX, cityMap);
      log.debug("Saved {} cities to hash", cityMap.size());
    } catch (Exception e) {
      log.error("Failed to save cities batch", e);
      throw new CacheException("Failed to save cities batch", e);
    }
  }

  @Override
  public List<City> retrieve() {
    try {
      Map<String, City> cityMap = hashOps.entries(KEY_PREFIX);

      if (cityMap == null || cityMap.isEmpty()) {
        return Collections.emptyList();
      }

      return new ArrayList<>(cityMap.values());
    } catch (Exception e) {
      log.error("Failed to retrieve cities from hash", e);
      throw new CacheException("Failed to retrieve cities", e); // Don't return empty list silently
    }
  }

  @Override
  public Optional<City> retrieveById(UUID id) {
    Objects.requireNonNull(id, "Id cannot be null");

    try {
      return Optional.ofNullable(hashOps.get(KEY_PREFIX, id.toString()));
    } catch (Exception e) {
      log.error("Failed to retrieve city from hash: {}", id, e);
      return Optional.empty();
    }
  }

  @Override
  public void deleteById(UUID id) {
    Objects.requireNonNull(id, "Id cannot be null");

    try {
      hashOps.delete(KEY_PREFIX, id.toString());
      log.debug("Deleted city from hash: {}", id);
    } catch (Exception e) {
      log.error("Failed to delete city from hash: {}", id, e);
      throw new CacheException("Failed to delete city", e);
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
      log.debug("Deleted {} cities from hash", ids.size());
    } catch (Exception e) {
      log.error("Failed to delete cities batch", e);
      throw new CacheException("Failed to delete cities batch", e);
    }
  }

  @Override
  public boolean existsById(UUID id) {
    Objects.requireNonNull(id, "Id cannot be null");
    try {
      return hashOps.hasKey(KEY_PREFIX, id.toString());
    } catch (Exception e) {
      log.error("Failed to check existence of city: {}", id, e);
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
      log.info("Cleared all cities from cache");
    } catch (Exception e) {
      log.error("Failed to clear cache", e);
      throw new CacheException("Failed to clear cache", e);
    }
  }

  /* =========================================================
   * VALIDATION METHODS
   * ========================================================= */

  private void validateCity(City city) {
    Objects.requireNonNull(city, "City cannot be null");
    Objects.requireNonNull(city.getId(), "City Id cannot be null");
  }

}
