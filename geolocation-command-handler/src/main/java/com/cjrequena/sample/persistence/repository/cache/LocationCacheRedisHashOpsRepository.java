package com.cjrequena.sample.persistence.repository.cache;

import com.cjrequena.sample.domain.exception.CacheException;
import com.cjrequena.sample.domain.model.aggregate.Location;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Comprehensive Redis cache repository for Location entities.
 *
 * @author cjrequena
 */
@Repository
@Qualifier("locationCacheRedisRepository")
@Slf4j
public class LocationCacheRedisHashOpsRepository implements CacheRepository<UUID, Location> {

  /* =========================================================
   * Redis Key Constants
   * ========================================================= */
  private static final String KEY_PREFIX = "locations:hash";


  /* =========================================================
   * Redis Operations
   * ========================================================= */
  private final RedisTemplate<String, Location> redisTemplate;
  private final HashOperations<String, String, Location> hashOps;

  @Autowired
  public LocationCacheRedisHashOpsRepository(RedisTemplate<String, Location> redisTemplate) {
    this.redisTemplate = redisTemplate;
    this.hashOps = redisTemplate.opsForHash();
  }

  /* =========================================================
   * HASH Operations - Primary Storage
   * ========================================================= */

  @Override
  public void load(List<Location> locations) {
    Objects.requireNonNull(locations, "Locations list cannot be null");

    try {
      // Clear existing hash
      redisTemplate.delete(KEY_PREFIX);

      if (locations.isEmpty()) {
        log.info("No locations to load into Redis cache");
        return;
      }
      saveAll(locations);

      log.info("Loaded locations into hash storage");
    } catch (Exception e) {
      log.error("Failed to load locations into hash", e);
      throw new CacheException("Failed to load locations", e);
    }
  }

  @Override
  public void save(Location location) {
    validateLocation(location);

    try {
      hashOps.put(KEY_PREFIX, location.getId().toString(), location);
      log.debug("Added location to hash: {}", location.getId());
    } catch (Exception e) {
      log.error("Failed to save location to hash: {}", location.getId(), e);
      throw new CacheException("Failed to save location", e);
    }
  }

  @Override
  public void saveAll(List<Location> locations) {
    Objects.requireNonNull(locations, "Locations list cannot be null");

    // Convert to map and bulk insert
    try {
      Map<String, Location> locationMap = locations.stream()
        .filter(Objects::nonNull)
        .filter(location -> location.getId() != null)
        .collect(Collectors.toMap(
          location -> location.getId().toString(),
          location -> location
        ));

      hashOps.putAll(KEY_PREFIX, locationMap);
      log.debug("Saved {} locations to hash", locationMap.size());
    } catch (Exception e) {
      log.error("Failed to save locations batch", e);
      throw new CacheException("Failed to save locations batch", e);
    }
  }

  @Override
  public List<Location> retrieve() {
    try {
      Map<String, Location> locationMap = hashOps.entries(KEY_PREFIX);

      if (locationMap == null || locationMap.isEmpty()) {
        return Collections.emptyList();
      }

      return new ArrayList<>(locationMap.values());
    } catch (Exception e) {
      log.error("Failed to retrieve locations from hash", e);
      throw new CacheException("Failed to retrieve locations", e); // Don't return empty list silently
    }
  }

  @Override
  public Optional<Location> retrieveById(UUID id) {
    Objects.requireNonNull(id, "Id cannot be null");

    try {
      return Optional.ofNullable(hashOps.get(KEY_PREFIX, id.toString()));
    } catch (Exception e) {
      log.error("Failed to retrieve location from hash: {}", id, e);
      return Optional.empty();
    }
  }

  @Override
  public void deleteById(UUID id) {
    Objects.requireNonNull(id, "Id cannot be null");

    try {
      hashOps.delete(KEY_PREFIX, id.toString());
      log.debug("Deleted location from hash: {}", id);
    } catch (Exception e) {
      log.error("Failed to delete location from hash: {}", id, e);
      throw new CacheException("Failed to delete location", e);
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
      log.debug("Deleted {} locations from hash", ids.size());
    } catch (Exception e) {
      log.error("Failed to delete locations batch", e);
      throw new CacheException("Failed to delete locations batch", e);
    }
  }

  @Override
  public boolean existsById(UUID id) {
    Objects.requireNonNull(id, "Id cannot be null");
    try {
      return hashOps.hasKey(KEY_PREFIX, id.toString());
    } catch (Exception e) {
      log.error("Failed to check existence of location: {}", id, e);
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
      log.info("Cleared all locations from cache");
    } catch (Exception e) {
      log.error("Failed to clear cache", e);
      throw new CacheException("Failed to clear cache", e);
    }
  }

  /* =========================================================
   * VALIDATION METHODS
   * ========================================================= */

  private void validateLocation(Location location) {
    Objects.requireNonNull(location, "Location cannot be null");
    Objects.requireNonNull(location.getId(), "Location Id cannot be null");
  }

}
