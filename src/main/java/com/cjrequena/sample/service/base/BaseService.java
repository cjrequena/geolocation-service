package com.cjrequena.sample.service.base;

import com.cjrequena.sample.shared.common.util.SortUtils;
import io.github.perplexhub.rsql.RSQLJPASupport;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Base service class providing common RSQL filtering, sorting, and pagination functionality.
 *
 * <p>This abstract class can be extended by any service that needs to support
 * dynamic filtering, sorting, and pagination using RSQL syntax.</p>
 *
 * @param <E> the entity type
 * @param <D> the domain model type
 * @author cjrequena
 */
@Log4j2
public abstract class BaseService<E, D> {

  /**
   * Gets the JPA repository that supports specifications.
   *
   * @return the repository instance
   */
  protected abstract JpaRepository<E, ?> getRepository();

  /**
   * Gets the JPA specification executor (usually the same as repository).
   *
   * @return the specification executor
   */
  protected abstract JpaSpecificationExecutor<E> getSpecificationExecutor();

  /**
   * Gets the mapper function to convert entity to domain model.
   *
   * @return the entity-to-domain mapper function
   */
  protected abstract Function<E, D> getEntityToDomainMapper();

  /**
   * Gets the entity class for RSQL specification building.
   *
   * @return the entity class
   */
  protected abstract Class<E> getEntityClass();

  /**
   * Finds all entities with optional RSQL filtering, sorting, and pagination.
   *
   * <p>This method provides a flexible query interface supporting:
   * <ul>
   *   <li>RSQL filtering (e.g., "active==true;name=like='test'")</li>
   *   <li>Sorting (e.g., "name,asc" or "name,asc;createdAt,desc")</li>
   *   <li>Pagination with offset/limit</li>
   * </ul>
   *
   * @param filters RSQL filter expression (optional)
   * @param offset pagination offset, 0-based (optional)
   * @param limit maximum number of results (optional)
   * @param sort sort expression (optional)
   * @return list of domain models matching the criteria
   * @throws IllegalArgumentException if filter or sort expression is invalid
   */
  protected List<D> findAllWithFiltersAndSort(String filters, Integer offset, Integer limit, String sort) {
    log.debug("Finding {} with filters: {}, offset: {}, limit: {}, sort: {}",
      getEntityClass().getSimpleName(), filters, offset, limit, sort);

    // Parse RSQL filters into Specification
    Specification<E> specification = null;
    if (filters != null && !filters.trim().isEmpty()) {
      try {
        specification = RSQLJPASupport.toSpecification(filters);
      } catch (Exception e) {
        log.error("Invalid RSQL filter expression: {}", filters, e);
        throw new IllegalArgumentException("Invalid filter expression: " + filters, e);
      }
    }

    // Parse sort expression
    Sort sortObj;
    try {
      sortObj = SortUtils.parseSort(sort);
    } catch (Exception e) {
      log.error("Invalid sort expression: {}", sort, e);
      throw new IllegalArgumentException("Invalid sort expression: " + sort, e);
    }

    // Execute query with pagination or just sorting
    List<E> entities;
    if (offset != null && limit != null) {
      // Use pagination
      if (limit <= 0) {
        throw new IllegalArgumentException("Limit must be greater than 0");
      }
      if (offset < 0) {
        throw new IllegalArgumentException("Offset must be greater than or equal to 0");
      }

      int pageNumber = offset / limit;
      Pageable pageable = sortObj.isSorted()
        ? PageRequest.of(pageNumber, limit, sortObj)
        : PageRequest.of(pageNumber, limit);

      Page<E> page = specification != null
        ? getSpecificationExecutor().findAll(specification, pageable)
        : getRepository().findAll(pageable);

      entities = page.getContent();
      log.debug("Found {} of {} total {} with pagination",
        entities.size(), page.getTotalElements(), getEntityClass().getSimpleName());
    } else if (sortObj.isSorted()) {
      // Use sorting only
      entities = specification != null
        ? getSpecificationExecutor().findAll(specification, sortObj)
        : getRepository().findAll(sortObj);
      log.debug("Found {} {} with sorting", entities.size(), getEntityClass().getSimpleName());
    } else {
      // No sorting or pagination
      entities = specification != null
        ? getSpecificationExecutor().findAll(specification)
        : getRepository().findAll();
      log.debug("Found {} {} without sorting or pagination", entities.size(), getEntityClass().getSimpleName());
    }

    // Convert to domain models
    return entities.stream()
      .map(getEntityToDomainMapper())
      .collect(Collectors.toList());
  }

  /**
   * Counts entities matching the given RSQL filter.
   *
   * @param filters RSQL filter expression (optional)
   * @return count of matching entities
   */
  protected long countWithFilters(String filters) {
    if (filters == null || filters.trim().isEmpty()) {
      return getRepository().count();
    }

    try {
      Specification<E> specification = RSQLJPASupport.toSpecification(filters);
      return getSpecificationExecutor().count(specification);
    } catch (Exception e) {
      log.error("Invalid RSQL filter expression: {}", filters, e);
      throw new IllegalArgumentException("Invalid filter expression: " + filters, e);
    }
  }
}
