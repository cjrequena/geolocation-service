package com.cjrequena.sample.service;

import com.cjrequena.sample.domain.mapper.RegionMapper;
import com.cjrequena.sample.domain.model.aggregate.Region;
import com.cjrequena.sample.persistence.entity.RegionEntity;
import com.cjrequena.sample.persistence.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for Region aggregate operations.
 * 
 * <p>Handles business logic for regions (states, provinces, autonomous communities, etc.)
 * and orchestrates between domain model and persistence layer.</p>
 *
 * @author cjrequena
 */
@Log4j2
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionService {

  private final RegionRepository regionRepository;
  private final RegionMapper regionMapper;

  // ================================================================
  // Create Operations
  // ================================================================

  /**
   * Creates a new region.
   *
   * @param region the region domain aggregate to create
   * @return the created region with generated ID
   */
  @Transactional
  public Region create(Region region) {
    log.debug("Creating region: {}", region.getName());
    
    RegionEntity entity = regionMapper.toEntity(region);
    RegionEntity savedEntity = regionRepository.save(entity);
    
    log.info("Region created with ID: {}", savedEntity.getId());
    return regionMapper.toDomain(savedEntity);
  }

  // ================================================================
  // Read Operations
  // ================================================================

  /**
   * Finds a region by ID.
   *
   * @param id the region ID
   * @return Optional containing the region if found
   */
  public Optional<Region> findById(UUID id) {
    log.debug("Finding region by ID: {}", id);
    
    return regionRepository.findById(id)
      .map(regionMapper::toDomain);
  }

  /**
   * Finds all regions.
   *
   * @return list of all regions
   */
  public List<Region> findAll() {
    log.debug("Finding all regions");
    
    return regionRepository.findAll().stream()
      .map(regionMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds all active regions.
   *
   * @return list of active regions
   */
  public List<Region> findAllActive() {
    log.debug("Finding all active regions");
    
    return regionRepository.findAllByActiveTrue().stream()
      .map(regionMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds regions by active status with pagination.
   *
   * @param active the active status
   * @param pageable pagination information
   * @return page of regions
   */
  public Page<Region> findByActive(Boolean active, Pageable pageable) {
    log.debug("Finding regions by active status: {} with pagination", active);
    
    return regionRepository.findByActive(active, pageable)
      .map(regionMapper::toDomain);
  }

  /**
   * Finds all regions belonging to a specific country.
   *
   * @param countryId the country ID
   * @return list of regions in the country
   */
  public List<Region> findByCountryId(UUID countryId) {
    log.debug("Finding regions by country ID: {}", countryId);
    
    return regionRepository.findByCountryId(countryId).stream()
      .map(regionMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds active regions belonging to a specific country.
   *
   * @param countryId the country ID
   * @return list of active regions in the country
   */
  public List<Region> findActiveByCountryId(UUID countryId) {
    log.debug("Finding active regions by country ID: {}", countryId);
    
    return regionRepository.findByCountryIdAndActiveTrue(countryId).stream()
      .map(regionMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds regions in a country with pagination.
   *
   * @param countryId the country ID
   * @param pageable pagination information
   * @return page of regions
   */
  public Page<Region> findByCountryId(UUID countryId, Pageable pageable) {
    log.debug("Finding regions by country ID: {} with pagination", countryId);
    
    return regionRepository.findByCountryId(countryId, pageable)
      .map(regionMapper::toDomain);
  }

  /**
   * Finds a region by country ID and name.
   *
   * @param countryId the country ID
   * @param name the region name
   * @return Optional containing the region if found
   */
  public Optional<Region> findByCountryIdAndName(UUID countryId, String name) {
    log.debug("Finding region by country ID: {} and name: {}", countryId, name);
    
    return regionRepository.findByCountryIdAndName(countryId, name)
      .map(regionMapper::toDomain);
  }

  /**
   * Finds regions by region type.
   *
   * @param regionType the region type (e.g., "STATE", "PROVINCE")
   * @return list of regions
   */
  public List<Region> findByRegionType(String regionType) {
    log.debug("Finding regions by type: {}", regionType);
    
    return regionRepository.findByRegionType(regionType).stream()
      .map(regionMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds active regions by region type.
   *
   * @param regionType the region type
   * @return list of active regions
   */
  public List<Region> findActiveByRegionType(String regionType) {
    log.debug("Finding active regions by type: {}", regionType);
    
    return regionRepository.findByRegionTypeAndActiveTrue(regionType).stream()
      .map(regionMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds regions by name containing substring (case-insensitive).
   *
   * @param namePart the substring to search for
   * @return list of matching regions
   */
  public List<Region> findByNameContaining(String namePart) {
    log.debug("Finding regions by name containing: {}", namePart);
    
    return regionRepository.findByNameContainingIgnoreCase(namePart).stream()
      .map(regionMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds regions in a country with population greater than threshold.
   *
   * @param countryId the country ID
   * @param minPopulation the minimum population
   * @return list of regions
   */
  public List<Region> findByCountryIdAndPopulationGreaterThan(UUID countryId, Long minPopulation) {
    log.debug("Finding regions in country {} with population > {}", countryId, minPopulation);
    
    return regionRepository.findByCountryIdAndPopulationGreaterThan(countryId, minPopulation).stream()
      .map(regionMapper::toDomain)
      .collect(Collectors.toList());
  }

  /**
   * Finds top regions in a country ordered by population descending.
   *
   * @param countryId the country ID
   * @param pageable pagination information
   * @return page of regions ordered by population
   */
  public Page<Region> findByCountryIdOrderByPopulationDesc(UUID countryId, Pageable pageable) {
    log.debug("Finding top regions in country {} by population", countryId);
    
    return regionRepository.findByCountryIdOrderByPopulationDesc(countryId, pageable)
      .map(regionMapper::toDomain);
  }

  /**
   * Finds regions created within a time range.
   *
   * @param start start date/time
   * @param end end date/time
   * @return list of regions
   */
  public List<Region> findByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end) {
    log.debug("Finding regions created between {} and {}", start, end);
    
    return regionRepository.findByCreatedAtBetween(start, end).stream()
      .map(regionMapper::toDomain)
      .collect(Collectors.toList());
  }

  // ================================================================
  // Update Operations
  // ================================================================

  /**
   * Updates an existing region.
   *
   * @param id the region ID
   * @param region the updated region data
   * @return the updated region
   * @throws IllegalArgumentException if region not found
   */
  @Transactional
  public Region update(UUID id, Region region) {
    log.debug("Updating region with ID: {}", id);
    
    RegionEntity existingEntity = regionRepository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("Region not found with ID: " + id));
    
    RegionEntity updatedEntity = regionMapper.toEntity(region);
    updatedEntity.setId(existingEntity.getId());
    updatedEntity.setCreatedAt(existingEntity.getCreatedAt());
    
    RegionEntity savedEntity = regionRepository.save(updatedEntity);
    
    log.info("Region updated with ID: {}", savedEntity.getId());
    return regionMapper.toDomain(savedEntity);
  }

  // ================================================================
  // Delete Operations
  // ================================================================

  /**
   * Deletes a region by ID.
   *
   * @param id the region ID
   */
  @Transactional
  public void deleteById(UUID id) {
    log.debug("Deleting region with ID: {}", id);
    
    if (!regionRepository.existsById(id)) {
      throw new IllegalArgumentException("Region not found with ID: " + id);
    }
    
    regionRepository.deleteById(id);
    log.info("Region deleted with ID: {}", id);
  }

  // ================================================================
  // Existence Checks
  // ================================================================

  /**
   * Checks if a region exists by ID.
   *
   * @param id the region ID
   * @return true if exists, false otherwise
   */
  public boolean existsById(UUID id) {
    return regionRepository.existsById(id);
  }

  /**
   * Checks if a region exists by country ID and name.
   *
   * @param countryId the country ID
   * @param name the region name
   * @return true if exists, false otherwise
   */
  public boolean existsByCountryIdAndName(UUID countryId, String name) {
    return regionRepository.existsByCountryIdAndName(countryId, name);
  }

  /**
   * Checks if an active region exists by country ID and name.
   *
   * @param countryId the country ID
   * @param name the region name
   * @return true if exists, false otherwise
   */
  public boolean existsActiveByCountryIdAndName(UUID countryId, String name) {
    return regionRepository.existsByCountryIdAndNameAndActiveTrue(countryId, name);
  }

  // ================================================================
  // Count Operations
  // ================================================================

  /**
   * Counts all regions.
   *
   * @return total count of regions
   */
  public long count() {
    return regionRepository.count();
  }
}
