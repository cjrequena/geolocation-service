package com.cjrequena.sample.persistence.repository.cache;

import com.cjrequena.sample.domain.exception.CacheException;
import com.cjrequena.sample.domain.model.aggregate.Zone;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Comprehensive Redis cache repository for Zone entities.
 *
 * @author cjrequena
 */
@Repository
@Qualifier("zoneCacheRedisRepository")
@Slf4j
public class ZoneCacheRedisHashOpsRepository implements CacheRepository<UUID, Zone> {

  /* =========================================================
   * Redis Key Constants
   * ========================================================= */
  private static final String KEY_PREFIX = "zones:hash";


  /* =========================================================
   * Redis Operations
   * ========================================================= */
  private final RedisTemplate<String, Zone> redisTemplate;
  private final HashOperations<String, String, Zone> hashOps;

  @Autowired
  public ZoneCacheRedisHashOpsRepository(RedisTemplate<String, Zone> redisTemplate) {
    this.redisTemplate = redisTemplate;
    this.hashOps = redisTemplate.opsForHash();
  }

  /* =========================================================
   * HASH Operations - Primary Storage
   * ========================================================= */

  @Override
  public void load(List<Zone> zones) {
    Objects.requireNonNull(zones, "Zones list cannot be null");

    try {
      // Clear existing hash
      redisTemplate.delete(KEY_PREFIX);

      if (zones.isEmpty()) {
        log.info("No zones to load into Redis cache");
        return;
      }
      saveAll(zones);

      log.info("Loaded zones into hash storage");
    } catch (Exception e) {
      log.error("Failed to load zones into hash", e);
      throw new CacheException("Failed to load zones", e);
    }
  }

  @Override
  public void save(Zone zone) {
    validateZone(zone);

    try {
      hashOps.put(KEY_PREFIX, zone.getId().toString(), zone);
      log.debug("Added zone to hash: {}", zone.getId());
    } catch (Exception e) {
      log.error("Failed to save zone to hash: {}", zone.getId(), e);
      throw new CacheException("Failed to save zone", e);
    }
  }

  @Override
  public void saveAll(List<Zone> zones) {
    Objects.requireNonNull(zones, "Zones list cannot be null");

    // Convert to map and bulk insert
    try {
      Map<String, Zone> zoneMap = zones.stream()
        .filter(Objects::nonNull)
        .filter(zone -> zone.getId() != null)
        .collect(Collectors.toMap(
          zone -> zone.getId().toString(),
          zone -> zone
        ));

      hashOps.putAll(KEY_PREFIX, zoneMap);
      log.debug("Saved {} zones to hash", zoneMap.size());
    } catch (Exception e) {
      log.error("Failed to save zones batch", e);
      throw new CacheException("Failed to save zones batch", e);
    }
  }

  @Override
  public List<Zone> retrieve() {
    try {
      Map<String, Zone> zoneMap = hashOps.entries(KEY_PREFIX);

      if (zoneMap == null || zoneMap.isEmpty()) {
        return Collections.emptyList();
      }

      return new ArrayList<>(zoneMap.values());
    } catch (Exception e) {
      log.error("Failed to retrieve zones from hash", e);
      throw new CacheException("Failed to retrieve zones", e); // Don't return empty list silently
    }
  }

  @Override
  public Optional<Zone> retrieveById(UUID id) {
    Objects.requireNonNull(id, "Id cannot be null");

    try {
      return Optional.ofNullable(hashOps.get(KEY_PREFIX, id.toString()));
    } catch (Exception e) {
      log.error("Failed to retrieve zone from hash: {}", id, e);
      return Optional.empty();
    }
  }

  @Override
  public void deleteById(UUID id) {
    Objects.requireNonNull(id, "Id cannot be null");

    try {
      hashOps.delete(KEY_PREFIX, id.toString());
      log.debug("Deleted zone from hash: {}", id);
    } catch (Exception e) {
      log.error("Failed to delete zone from hash: {}", id, e);
      throw new CacheException("Failed to delete zone", e);
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
      log.debug("Deleted {} zones from hash", ids.size());
    } catch (Exception e) {
      log.error("Failed to delete zones batch", e);
      throw new CacheException("Failed to delete zones batch", e);
    }
  }

  @Override
  public boolean existsById(UUID id) {
    Objects.requireNonNull(id, "Id cannot be null");
    try {
      return hashOps.hasKey(KEY_PREFIX, id.toString());
    } catch (Exception e) {
      log.error("Failed to check existence of zone: {}", id, e);
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
      log.info("Cleared all zones from cache");
    } catch (Exception e) {
      log.error("Failed to clear cache", e);
      throw new CacheException("Failed to clear cache", e);
    }
  }

  /* =========================================================
   * VALIDATION METHODS
   * ========================================================= */

  private void validateZone(Zone zone) {
    Objects.requireNonNull(zone, "Zone cannot be null");
    Objects.requireNonNull(zone.getId(), "Zone Id cannot be null");
  }

}
