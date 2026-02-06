package com.cjrequena.sample.service;

import com.cjrequena.sample.domain.mapper.AreaMapper;
import com.cjrequena.sample.domain.model.aggregate.Area;
import com.cjrequena.sample.persistence.entity.AreaEntity;
import com.cjrequena.sample.persistence.repository.AreaRepository;
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
 * Service layer for Area aggregate operations.
 *
 * @author cjrequena
 */
@Log4j2
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AreaService {

  private final AreaRepository areaRepository;
  private final AreaMapper areaMapper;

  @Transactional
  public Area create(Area area) {
    log.debug("Creating area: {}", area.getName());
    AreaEntity entity = areaMapper.toEntity(area);
    AreaEntity savedEntity = areaRepository.save(entity);
    log.info("Area created with ID: {}", savedEntity.getId());
    return areaMapper.toDomain(savedEntity);
  }

  public Optional<Area> findById(UUID id) {
    log.debug("Finding area by ID: {}", id);
    return areaRepository.findById(id).map(areaMapper::toDomain);
  }

  public List<Area> findAll() {
    log.debug("Finding all areas");
    return areaRepository.findAll().stream()
      .map(areaMapper::toDomain)
      .collect(Collectors.toList());
  }

  public List<Area> findAllActive() {
    log.debug("Finding all active areas");
    return areaRepository.findAllByActiveTrue().stream()
      .map(areaMapper::toDomain)
      .collect(Collectors.toList());
  }

  public Page<Area> findByActive(Boolean active, Pageable pageable) {
    log.debug("Finding areas by active status: {}", active);
    return areaRepository.findByActive(active, pageable).map(areaMapper::toDomain);
  }

  public List<Area> findByCityId(UUID cityId) {
    log.debug("Finding areas by city ID: {}", cityId);
    return areaRepository.findByCityId(cityId).stream()
      .map(areaMapper::toDomain)
      .collect(Collectors.toList());
  }

  public List<Area> findActiveByCityId(UUID cityId) {
    log.debug("Finding active areas by city ID: {}", cityId);
    return areaRepository.findByCityIdAndActiveTrue(cityId).stream()
      .map(areaMapper::toDomain)
      .collect(Collectors.toList());
  }

  public Page<Area> findByCityId(UUID cityId, Pageable pageable) {
    log.debug("Finding areas by city ID: {} with pagination", cityId);
    return areaRepository.findByCityId(cityId, pageable).map(areaMapper::toDomain);
  }

  public List<Area> findByAreaType(String areaType) {
    log.debug("Finding areas by type: {}", areaType);
    return areaRepository.findByAreaType(areaType).stream()
      .map(areaMapper::toDomain)
      .collect(Collectors.toList());
  }

  public List<Area> findActiveByAreaType(String areaType) {
    log.debug("Finding active areas by type: {}", areaType);
    return areaRepository.findByAreaTypeAndActiveTrue(areaType).stream()
      .map(areaMapper::toDomain)
      .collect(Collectors.toList());
  }

  public List<Area> findByCityIdAndAreaType(UUID cityId, String areaType) {
    log.debug("Finding areas by city ID: {} and type: {}", cityId, areaType);
    return areaRepository.findByCityIdAndAreaType(cityId, areaType).stream()
      .map(areaMapper::toDomain)
      .collect(Collectors.toList());
  }

  public List<Area> findByPostalCode(String postalCode) {
    log.debug("Finding areas by postal code: {}", postalCode);
    return areaRepository.findByPostalCode(postalCode).stream()
      .map(areaMapper::toDomain)
      .collect(Collectors.toList());
  }

  public List<Area> findByCityIdAndPostalCode(UUID cityId, String postalCode) {
    log.debug("Finding areas by city ID: {} and postal code: {}", cityId, postalCode);
    return areaRepository.findByCityIdAndPostalCode(cityId, postalCode).stream()
      .map(areaMapper::toDomain)
      .collect(Collectors.toList());
  }

  public Optional<Area> findByCityIdAndName(UUID cityId, String name) {
    log.debug("Finding area by city ID: {} and name: {}", cityId, name);
    return areaRepository.findByCityIdAndName(cityId, name).map(areaMapper::toDomain);
  }

  public List<Area> findByNameContaining(String namePart) {
    log.debug("Finding areas by name containing: {}", namePart);
    return areaRepository.findByNameContainingIgnoreCase(namePart).stream()
      .map(areaMapper::toDomain)
      .collect(Collectors.toList());
  }

  public List<Area> findByCityIdAndPopulationGreaterThan(UUID cityId, Long minPopulation) {
    log.debug("Finding areas in city {} with population > {}", cityId, minPopulation);
    return areaRepository.findByCityIdAndPopulationGreaterThan(cityId, minPopulation).stream()
      .map(areaMapper::toDomain)
      .collect(Collectors.toList());
  }

  public Page<Area> findByCityIdOrderByPopulationDesc(UUID cityId, Pageable pageable) {
    log.debug("Finding top areas in city {} by population", cityId);
    return areaRepository.findByCityIdOrderByPopulationDesc(cityId, pageable)
      .map(areaMapper::toDomain);
  }

  public List<Area> findByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end) {
    log.debug("Finding areas created between {} and {}", start, end);
    return areaRepository.findByCreatedAtBetween(start, end).stream()
      .map(areaMapper::toDomain)
      .collect(Collectors.toList());
  }

  @Transactional
  public Area update(UUID id, Area area) {
    log.debug("Updating area with ID: {}", id);
    AreaEntity existingEntity = areaRepository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("Area not found with ID: " + id));
    
    AreaEntity updatedEntity = areaMapper.toEntity(area);
    updatedEntity.setId(existingEntity.getId());
    updatedEntity.setCreatedAt(existingEntity.getCreatedAt());
    
    AreaEntity savedEntity = areaRepository.save(updatedEntity);
    log.info("Area updated with ID: {}", savedEntity.getId());
    return areaMapper.toDomain(savedEntity);
  }

  @Transactional
  public void deleteById(UUID id) {
    log.debug("Deleting area with ID: {}", id);
    if (!areaRepository.existsById(id)) {
      throw new IllegalArgumentException("Area not found with ID: " + id);
    }
    areaRepository.deleteById(id);
    log.info("Area deleted with ID: {}", id);
  }

  public boolean existsById(UUID id) {
    return areaRepository.existsById(id);
  }

  public boolean existsByCityIdAndName(UUID cityId, String name) {
    return areaRepository.existsByCityIdAndName(cityId, name);
  }

  public boolean existsByPostalCode(String postalCode) {
    return areaRepository.existsByPostalCode(postalCode);
  }

  public long count() {
    return areaRepository.count();
  }
}
