package com.cjrequena.sample.persistence.repository.cache;

import com.cjrequena.sample.domain.exception.CacheException;
import com.cjrequena.sample.domain.model.GeoShape;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Comprehensive Redis cache repository for GeoShape entities.
 *
 * @author cjrequena
 */
@Repository
@Qualifier("geoShapeCacheRedisRepository")
@Slf4j
public class GeoShapeCacheRedisHashOpsRepository implements CacheRepository<UUID, GeoShape> {

  /* =========================================================
   * Redis Key Constants
   * ========================================================= */
  private static final String KEY_PREFIX = "geoShapes:hash";


  /* =========================================================
   * Redis Operations
   * ========================================================= */
  private final RedisTemplate<String, GeoShape> redisTemplate;
  private final HashOperations<String, String, GeoShape> hashOps;

  @Autowired
  public GeoShapeCacheRedisHashOpsRepository(RedisTemplate<String, GeoShape> redisTemplate) {
    this.redisTemplate = redisTemplate;
    this.hashOps = redisTemplate.opsForHash();
  }

  /* =========================================================
   * HASH Operations - Primary Storage
   * ========================================================= */

  @Override
  public void load(List<GeoShape> geoShapes) {
    Objects.requireNonNull(geoShapes, "GeoShapes list cannot be null");

    try {
      // Clear existing hash
      redisTemplate.delete(KEY_PREFIX);

      if (geoShapes.isEmpty()) {
        log.info("No geoShapes to load into Redis cache");
        return;
      }
      saveAll(geoShapes);

      log.info("Loaded geoShapes into hash storage");
    } catch (Exception e) {
      log.error("Failed to load geoShapes into hash", e);
      throw new CacheException("Failed to load geoShapes", e);
    }
  }

  @Override
  public void save(GeoShape geoShape) {
    validateGeoShape(geoShape);

    try {
      hashOps.put(KEY_PREFIX, geoShape.getId().toString(), geoShape);
      log.debug("Added geoShape to hash: {}", geoShape.getId());
    } catch (Exception e) {
      log.error("Failed to save geoShape to hash: {}", geoShape.getId(), e);
      throw new CacheException("Failed to save geoShape", e);
    }
  }

  @Override
  public void saveAll(List<GeoShape> geoShapes) {
    Objects.requireNonNull(geoShapes, "GeoShapes list cannot be null");

    // Convert to map and bulk insert
    try {
      Map<String, GeoShape> geoShapeMap = geoShapes.stream()
        .filter(Objects::nonNull)
        .filter(geoShape -> geoShape.getId() != null)
        .collect(Collectors.toMap(
          geoShape -> geoShape.getId().toString(),
          geoShape -> geoShape
        ));

      hashOps.putAll(KEY_PREFIX, geoShapeMap);
      log.debug("Saved {} geoShapes to hash", geoShapeMap.size());
    } catch (Exception e) {
      log.error("Failed to save geoShapes batch", e);
      throw new CacheException("Failed to save geoShapes batch", e);
    }
  }

  @Override
  public List<GeoShape> retrieve() {
    try {
      Map<String, GeoShape> geoShapeMap = hashOps.entries(KEY_PREFIX);

      if (geoShapeMap == null || geoShapeMap.isEmpty()) {
        return Collections.emptyList();
      }

      return new ArrayList<>(geoShapeMap.values());
    } catch (Exception e) {
      log.error("Failed to retrieve geoShapes from hash", e);
      throw new CacheException("Failed to retrieve geoShapes", e); // Don't return empty list silently
    }
  }

  @Override
  public Optional<GeoShape> retrieveById(UUID id) {
    Objects.requireNonNull(id, "Id cannot be null");

    try {
      return Optional.ofNullable(hashOps.get(KEY_PREFIX, id.toString()));
    } catch (Exception e) {
      log.warn("Failed to retrieve geoShape from hash: {}", id);
      return Optional.empty();
    }
  }

  @Override
  public void deleteById(UUID id) {
    Objects.requireNonNull(id, "Id cannot be null");

    try {
      hashOps.delete(KEY_PREFIX, id.toString());
      log.debug("Deleted geoShape from hash: {}", id);
    } catch (Exception e) {
      log.error("Failed to delete geoShape from hash: {}", id, e);
      throw new CacheException("Failed to delete geoShape", e);
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
      log.debug("Deleted {} geoShapes from hash", ids.size());
    } catch (Exception e) {
      log.error("Failed to delete geoShapes batch", e);
      throw new CacheException("Failed to delete geoShapes batch", e);
    }
  }

  @Override
  public boolean existsById(UUID id) {
    Objects.requireNonNull(id, "Id cannot be null");
    try {
      return hashOps.hasKey(KEY_PREFIX, id.toString());
    } catch (Exception e) {
      log.error("Failed to check existence of geoShape: {}", id, e);
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
      log.info("Cleared all geoShapes from cache");
    } catch (Exception e) {
      log.error("Failed to clear cache", e);
      throw new CacheException("Failed to clear cache", e);
    }
  }

  /* =========================================================
   * VALIDATION METHODS
   * ========================================================= */

  private void validateGeoShape(GeoShape geoShape) {
    Objects.requireNonNull(geoShape, "GeoShape cannot be null");
    Objects.requireNonNull(geoShape.getId(), "GeoShape Id cannot be null");
  }

}
