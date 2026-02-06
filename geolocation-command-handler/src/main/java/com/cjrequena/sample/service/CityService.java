package com.cjrequena.sample.service;

import com.cjrequena.sample.domain.mapper.CityMapper;
import com.cjrequena.sample.domain.model.aggregate.City;
import com.cjrequena.sample.persistence.entity.CityEntity;
import com.cjrequena.sample.persistence.repository.CityRepository;
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
 * Service layer for City aggregate operations.
 *
 * @author cjrequena
 */
@Log4j2
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CityService {

  private final CityRepository cityRepository;
  private final CityMapper cityMapper;

  @Transactional
  public City create(City city) {
    log.debug("Creating city: {}", city.getName());
    CityEntity entity = cityMapper.toEntity(city);
    CityEntity savedEntity = cityRepository.save(entity);
    log.info("City created with ID: {}", savedEntity.getId());
    return cityMapper.toDomain(savedEntity);
  }

  public Optional<City> findById(UUID id) {
    log.debug("Finding city by ID: {}", id);
    return cityRepository.findById(id).map(cityMapper::toDomain);
  }

  public List<City> findAll() {
    log.debug("Finding all cities");
    return cityRepository.findAll().stream()
      .map(cityMapper::toDomain)
      .collect(Collectors.toList());
  }

  public List<City> findAllActive() {
    log.debug("Finding all active cities");
    return cityRepository.findAllByActiveTrue().stream()
      .map(cityMapper::toDomain)
      .collect(Collectors.toList());
  }

  public Page<City> findByActive(Boolean active, Pageable pageable) {
    log.debug("Finding cities by active status: {}", active);
    return cityRepository.findByActive(active, pageable).map(cityMapper::toDomain);
  }

  public List<City> findByRegionId(UUID regionId) {
    log.debug("Finding cities by region ID: {}", regionId);
    return cityRepository.findByRegionId(regionId).stream()
      .map(cityMapper::toDomain)
      .collect(Collectors.toList());
  }

  public List<City> findActiveByRegionId(UUID regionId) {
    log.debug("Finding active cities by region ID: {}", regionId);
    return cityRepository.findByRegionIdAndActiveTrue(regionId).stream()
      .map(cityMapper::toDomain)
      .collect(Collectors.toList());
  }

  public Page<City> findByRegionId(UUID regionId, Pageable pageable) {
    log.debug("Finding cities by region ID: {} with pagination", regionId);
    return cityRepository.findByRegionId(regionId, pageable).map(cityMapper::toDomain);
  }

  public Optional<City> findCapitalByRegionId(UUID regionId) {
    log.debug("Finding capital city by region ID: {}", regionId);
    return cityRepository.findCapitalByRegionId(regionId).map(cityMapper::toDomain);
  }

  public List<City> findAllCapitals() {
    log.debug("Finding all capital cities");
    return cityRepository.findAllByCapitalTrue().stream()
      .map(cityMapper::toDomain)
      .collect(Collectors.toList());
  }

  public List<City> findByTimeZone(String timeZone) {
    log.debug("Finding cities by timezone: {}", timeZone);
    return cityRepository.findByTimeZone(timeZone).stream()
      .map(cityMapper::toDomain)
      .collect(Collectors.toList());
  }

  public Optional<City> findByRegionIdAndName(UUID regionId, String name) {
    log.debug("Finding city by region ID: {} and name: {}", regionId, name);
    return cityRepository.findByRegionIdAndName(regionId, name).map(cityMapper::toDomain);
  }

  public List<City> findByNameContaining(String namePart) {
    log.debug("Finding cities by name containing: {}", namePart);
    return cityRepository.findByNameContainingIgnoreCase(namePart).stream()
      .map(cityMapper::toDomain)
      .collect(Collectors.toList());
  }

  public List<City> findByRegionIdAndPopulationGreaterThan(UUID regionId, Long minPopulation) {
    log.debug("Finding cities in region {} with population > {}", regionId, minPopulation);
    return cityRepository.findByRegionIdAndPopulationGreaterThan(regionId, minPopulation).stream()
      .map(cityMapper::toDomain)
      .collect(Collectors.toList());
  }

  public Page<City> findByRegionIdOrderByPopulationDesc(UUID regionId, Pageable pageable) {
    log.debug("Finding top cities in region {} by population", regionId);
    return cityRepository.findByRegionIdOrderByPopulationDesc(regionId, pageable)
      .map(cityMapper::toDomain);
  }

  public Page<City> findAllOrderByPopulationDesc(Pageable pageable) {
    log.debug("Finding all cities ordered by population");
    return cityRepository.findAllByOrderByPopulationDesc(pageable).map(cityMapper::toDomain);
  }

  public List<City> findByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end) {
    log.debug("Finding cities created between {} and {}", start, end);
    return cityRepository.findByCreatedAtBetween(start, end).stream()
      .map(cityMapper::toDomain)
      .collect(Collectors.toList());
  }

  @Transactional
  public City update(UUID id, City city) {
    log.debug("Updating city with ID: {}", id);
    CityEntity existingEntity = cityRepository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("City not found with ID: " + id));
    
    CityEntity updatedEntity = cityMapper.toEntity(city);
    updatedEntity.setId(existingEntity.getId());
    updatedEntity.setCreatedAt(existingEntity.getCreatedAt());
    
    CityEntity savedEntity = cityRepository.save(updatedEntity);
    log.info("City updated with ID: {}", savedEntity.getId());
    return cityMapper.toDomain(savedEntity);
  }

  @Transactional
  public void deleteById(UUID id) {
    log.debug("Deleting city with ID: {}", id);
    if (!cityRepository.existsById(id)) {
      throw new IllegalArgumentException("City not found with ID: " + id);
    }
    cityRepository.deleteById(id);
    log.info("City deleted with ID: {}", id);
  }

  public boolean existsById(UUID id) {
    return cityRepository.existsById(id);
  }

  public boolean existsByRegionIdAndName(UUID regionId, String name) {
    return cityRepository.existsByRegionIdAndName(regionId, name);
  }

  public long count() {
    return cityRepository.count();
  }
}
