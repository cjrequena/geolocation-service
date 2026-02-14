package com.cjrequena.sample.persistence.repository;

import com.cjrequena.sample.persistence.entity.CountryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link CountryEntity}.
 *
 * <p>Country is the root of the geographic hierarchy and has no parent entity.
 * This repository provides queries by ISO codes (alpha-2, alpha-3, numeric),
 * name, and active status.</p>
 */
@Repository
public interface CountryRepository extends JpaRepository<CountryEntity, UUID>, JpaSpecificationExecutor<CountryEntity>, QuerydslPredicateExecutor<CountryEntity> {

  // ================================================================
  // Active / Inactive filtering
  // ================================================================

  /**
   * Finds all active countries.
   */
  List<CountryEntity> findAllByActiveTrue();

  /**
   * Finds all inactive countries.
   */
  List<CountryEntity> findAllByActiveFalse();

  /**
   * Finds countries by active status with pagination.
   */
  Page<CountryEntity> findByActive(Boolean active, Pageable pageable);

  // ================================================================
  // ISO code queries
  // ================================================================

  /**
   * Finds a country by its ISO 3166-1 alpha-2 code (e.g., "US", "GB").
   *
   * @param alpha2 the two-letter country code
   * @return the country if found
   */
  Optional<CountryEntity> findByIsoCodeAlpha2(String alpha2);

  /**
   * Finds a country by its ISO 3166-1 alpha-3 code (e.g., "USA", "GBR").
   *
   * @param alpha3 the three-letter country code
   * @return the country if found
   */
  Optional<CountryEntity> findByIsoCodeAlpha3(String alpha3);

  /**
   * Finds a country by its ISO 3166-1 numeric code (e.g., "840" for USA).
   *
   * @param numeric the three-digit numeric code as a String
   * @return the country if found
   */
  Optional<CountryEntity> findByIsoCodeNumeric(String numeric);

  /**
   * Finds an active country by its alpha-2 code.
   */
  Optional<CountryEntity> findByIsoCodeAlpha2AndActiveTrue(String alpha2);

  /**
   * Finds an active country by its alpha-3 code.
   */
  Optional<CountryEntity> findByIsoCodeAlpha3AndActiveTrue(String alpha3);

  // ================================================================
  // Name-based queries
  // ================================================================

  /**
   * Finds a country by exact name (case-sensitive).
   */
  Optional<CountryEntity> findByName(String name);

  /**
   * Finds countries where the name contains the given substring (case-insensitive).
   */
  List<CountryEntity> findByNameContainingIgnoreCase(String namePart);

  /**
   * Finds active countries where the name contains the given substring.
   */
  List<CountryEntity> findByNameContainingIgnoreCaseAndActiveTrue(String namePart);

  // ================================================================
  // Phone / Currency code queries
  // ================================================================

  /**
   * Finds countries by phone code (e.g., "+1", "+44").
   */
  List<CountryEntity> findByPhoneCode(String phoneCode);

  /**
   * Finds countries by currency code (e.g., "USD", "EUR").
   */
  List<CountryEntity> findByCurrencyCode(String currencyCode);

  // ================================================================
  // Population queries
  // ================================================================

  /**
   * Finds countries with a population greater than the specified value.
   */
  @Query("SELECT c FROM CountryEntity c WHERE c.population > :minPopulation")
  List<CountryEntity> findByPopulationGreaterThan(@Param("minPopulation") Long minPopulation);

  /**
   * Finds the top N countries ordered by population descending.
   *
   * @param pageable use {@code PageRequest.of(0, n)} to fetch top N
   */
  Page<CountryEntity> findAllByOrderByPopulationDesc(Pageable pageable);

  // ================================================================
  // Audit / Temporal queries
  // ================================================================

  /**
   * Finds countries created within a given time range.
   */
  List<CountryEntity> findByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end);

  /**
   * Finds the 10 most recently updated countries.
   */
  List<CountryEntity> findTop10ByOrderByUpdatedAtDesc();

  // ================================================================
  // Existence checks
  // ================================================================

  /**
   * Checks if a country with the given alpha-2 code exists.
   */
  boolean existsByIsoCodeAlpha2(String alpha2);

  /**
   * Checks if a country with the given alpha-3 code exists.
   */
  boolean existsByIsoCodeAlpha3(String alpha3);

  /**
   * Checks if a country with the given name exists.
   */
  boolean existsByName(String name);

  /**
   * Checks if an active country with the given alpha-2 code exists.
   */
  boolean existsByIsoCodeAlpha2AndActiveTrue(String alpha2);
}
