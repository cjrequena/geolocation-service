package com.cjrequena.sample.persistence.repository.cache;

import com.cjrequena.sample.domain.exception.CacheException;
import com.cjrequena.sample.domain.model.aggregate.Region;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Comprehensive Redis cache repository for Region entities.
 *
 * @author cjrequena
 */
@Repository
@Qualifier("regionCacheRedisRepository")
@Slf4j
public class RegionCacheRedisHashOpsRepository implements CacheRepository<UUID, Region> {

  /* =========================================================
   * Redis Key Constants
   * ========================================================= */
  private static final String KEY_PREFIX = "regions:hash";


  /* =========================================================
   * Redis Operations
   * ========================================================= */
  private final RedisTemplate<String, Object> redisTemplate;
  private final HashOperations<String, String, Region> hashOps;

  @Autowired
  public RegionCacheRedisHashOpsRepository(RedisTemplate<String, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
    this.hashOps = redisTemplate.opsForHash();
  }

  /* =========================================================
   * HASH Operations - Primary Storage
   * ========================================================= */

  @Override
  public void load(List<Region> regions) {
    Objects.requireNonNull(regions, "Regions list cannot be null");

    try {
      // Clear existing hash
      redisTemplate.delete(KEY_PREFIX);

      if (regions.isEmpty()) {
        log.info("No regions to load into Redis cache");
        return;
      }
      saveAll(regions);

      log.info("Loaded regions into hash storage");
    } catch (Exception e) {
      log.error("Failed to load regions into hash", e);
      throw new CacheException("Failed to load regions", e);
    }
  }

  @Override
  public void save(Region region) {
    validateRegion(region);

    try {
      hashOps.put(KEY_PREFIX, region.getId().toString(), region);
      log.debug("Added region to hash: {}", region.getId());
    } catch (Exception e) {
      log.error("Failed to save region to hash: {}", region.getId(), e);
      throw new CacheException("Failed to save region", e);
    }
  }

  @Override
  public void saveAll(List<Region> regions) {
    Objects.requireNonNull(regions, "Regions list cannot be null");

    // Convert to map and bulk insert
    try {
      Map<String, Region> regionMap = regions.stream()
        .filter(Objects::nonNull)
        .filter(region -> region.getId() != null)
        .collect(Collectors.toMap(
          region -> region.getId().toString(),
          region -> region
        ));

      hashOps.putAll(KEY_PREFIX, regionMap);
      log.debug("Saved {} regions to hash", regionMap.size());
    } catch (Exception e) {
      log.error("Failed to save regions batch", e);
      throw new CacheException("Failed to save regions batch", e);
    }
  }

  @Override
  public List<Region> retrieve() {
    try {
      Map<String, Region> regionMap = hashOps.entries(KEY_PREFIX);

      if (regionMap == null || regionMap.isEmpty()) {
        return Collections.emptyList();
      }

      return new ArrayList<>(regionMap.values());
    } catch (Exception e) {
      log.error("Failed to retrieve regions from hash", e);
      throw new CacheException("Failed to retrieve regions", e); // Don't return empty list silently
    }
  }

  @Override
  public Optional<Region> retrieveById(UUID id) {
    Objects.requireNonNull(id, "Id cannot be null");

    try {
      return Optional.ofNullable(hashOps.get(KEY_PREFIX, id.toString()));
    } catch (Exception e) {
      log.error("Failed to retrieve region from hash: {}", id, e);
      return Optional.empty();
    }
  }

  @Override
  public void deleteById(UUID id) {
    Objects.requireNonNull(id, "Id cannot be null");

    try {
      hashOps.delete(KEY_PREFIX, id.toString());
      log.debug("Deleted region from hash: {}", id);
    } catch (Exception e) {
      log.error("Failed to delete region from hash: {}", id, e);
      throw new CacheException("Failed to delete region", e);
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
      log.debug("Deleted {} regions from hash", ids.size());
    } catch (Exception e) {
      log.error("Failed to delete regions batch", e);
      throw new CacheException("Failed to delete regions batch", e);
    }
  }

  @Override
  public boolean existsById(UUID id) {
    Objects.requireNonNull(id, "Id cannot be null");
    try {
      return hashOps.hasKey(KEY_PREFIX, id.toString());
    } catch (Exception e) {
      log.error("Failed to check existence of region: {}", id, e);
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
      log.info("Cleared all regions from cache");
    } catch (Exception e) {
      log.error("Failed to clear cache", e);
      throw new CacheException("Failed to clear cache", e);
    }
  }

  /* =========================================================
   * VALIDATION METHODS
   * ========================================================= */

  private void validateRegion(Region region) {
    Objects.requireNonNull(region, "Region cannot be null");
    Objects.requireNonNull(region.getId(), "Region Id cannot be null");
  }

}
