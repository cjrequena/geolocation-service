package com.cjrequena.sample.persistence.repository.cache;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CacheRepository<K, T> {

  void load(List<T> entities);

  void save(T entity);

  void saveAll(List<T> entities);

  List<T> retrieve();

  Optional<T> retrieveById(K id);

  void deleteById(K id);

  void deleteAll(Collection<K> ids);

  boolean existsById(K id);

  boolean isEmpty();

  long size();

  void clear();

}
