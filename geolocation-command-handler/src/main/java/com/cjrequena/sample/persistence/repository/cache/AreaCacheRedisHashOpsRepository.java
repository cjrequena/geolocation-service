package com.cjrequena.sample.persistence.repository.cache;

import com.cjrequena.sample.domain.exception.CacheException;
import com.cjrequena.sample.domain.model.aggregate.Area;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Comprehensive Redis cache repository for Area entities.
 *
 * @author cjrequena
 */
@Repository
@Qualifier("areaCacheRedisRepository")
@Slf4j
public class AreaCacheRedisHashOpsRepository implements CacheRepository<UUID, Area> {

  /* =========================================================
   * Redis Key Constants
   * ========================================================= */
  private static final String KEY_PREFIX = "areas:hash";


  /* =========================================================
   * Redis Operations
   * ========================================================= */
  private final RedisTemplate<String, Object> redisTemplate;
  private final HashOperations<String, String, Area> hashOps;

  @Autowired
  public AreaCacheRedisHashOpsRepository(RedisTemplate<String, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
    this.hashOps = redisTemplate.opsForHash();
  }

  /* =========================================================
   * HASH Operations - Primary Storage
   * ========================================================= */

  @Override
  public void load(List<Area> areas) {
    Objects.requireNonNull(areas, "Areas list cannot be null");

    try {
      // Clear existing hash
      redisTemplate.delete(KEY_PREFIX);

      if (areas.isEmpty()) {
        log.info("No areas to load into Redis cache");
        return;
      }
      saveAll(areas);

      log.info("Loaded areas into hash storage");
    } catch (Exception e) {
      log.error("Failed to load areas into hash", e);
      throw new CacheException("Failed to load areas", e);
    }
  }

  @Override
  public void save(Area area) {
    validateArea(area);

    try {
      hashOps.put(KEY_PREFIX, area.getId().toString(), area);
      log.debug("Added area to hash: {}", area.getId());
    } catch (Exception e) {
      log.error("Failed to save area to hash: {}", area.getId(), e);
      throw new CacheException("Failed to save area", e);
    }
  }

  @Override
  public void saveAll(List<Area> areas) {
    Objects.requireNonNull(areas, "Areas list cannot be null");

    // Convert to map and bulk insert
    try {
      Map<String, Area> areaMap = areas.stream()
        .filter(Objects::nonNull)
        .filter(area -> area.getId() != null)
        .collect(Collectors.toMap(
          area -> area.getId().toString(),
          area -> area
        ));

      hashOps.putAll(KEY_PREFIX, areaMap);
      log.debug("Saved {} areas to hash", areaMap.size());
    } catch (Exception e) {
      log.error("Failed to save areas batch", e);
      throw new CacheException("Failed to save areas batch", e);
    }
  }

  @Override
  public List<Area> retrieve() {
    try {
      Map<String, Area> areaMap = hashOps.entries(KEY_PREFIX);

      if (areaMap == null || areaMap.isEmpty()) {
        return Collections.emptyList();
      }

      return new ArrayList<>(areaMap.values());
    } catch (Exception e) {
      log.error("Failed to retrieve areas from hash", e);
      throw new CacheException("Failed to retrieve areas", e); // Don't return empty list silently
    }
  }

  @Override
  public Optional<Area> retrieveById(UUID id) {
    Objects.requireNonNull(id, "Id cannot be null");

    try {
      return Optional.ofNullable(hashOps.get(KEY_PREFIX, id.toString()));
    } catch (Exception e) {
      log.error("Failed to retrieve area from hash: {}", id, e);
      return Optional.empty();
    }
  }

  @Override
  public void deleteById(UUID id) {
    Objects.requireNonNull(id, "Id cannot be null");

    try {
      hashOps.delete(KEY_PREFIX, id.toString());
      log.debug("Deleted area from hash: {}", id);
    } catch (Exception e) {
      log.error("Failed to delete area from hash: {}", id, e);
      throw new CacheException("Failed to delete area", e);
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
      log.debug("Deleted {} areas from hash", ids.size());
    } catch (Exception e) {
      log.error("Failed to delete areas batch", e);
      throw new CacheException("Failed to delete areas batch", e);
    }
  }

  @Override
  public boolean existsById(UUID id) {
    Objects.requireNonNull(id, "Id cannot be null");
    try {
      return hashOps.hasKey(KEY_PREFIX, id.toString());
    } catch (Exception e) {
      log.error("Failed to check existence of area: {}", id, e);
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
      log.info("Cleared all areas from cache");
    } catch (Exception e) {
      log.error("Failed to clear cache", e);
      throw new CacheException("Failed to clear cache", e);
    }
  }

  /* =========================================================
   * VALIDATION METHODS
   * ========================================================= */

  private void validateArea(Area area) {
    Objects.requireNonNull(area, "Area cannot be null");
    Objects.requireNonNull(area.getId(), "Area Id cannot be null");
  }

}
