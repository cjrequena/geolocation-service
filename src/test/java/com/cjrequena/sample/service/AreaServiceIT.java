package com.cjrequena.sample.service;

import com.cjrequena.sample.domain.model.Area;
import com.cjrequena.sample.domain.model.City;
import com.cjrequena.sample.domain.model.Country;
import com.cjrequena.sample.domain.model.Region;
import com.cjrequena.sample.domain.model.enums.AreaType;
import com.cjrequena.sample.domain.model.vo.AuditInfoVO;
import com.cjrequena.sample.domain.model.vo.IsoCodeVO;
import com.cjrequena.sample.domain.model.vo.MetadataVO;
import com.cjrequena.sample.persistence.repository.AreaRepository;
import com.cjrequena.sample.persistence.repository.CityRepository;
import com.cjrequena.sample.persistence.repository.CountryRepository;
import com.cjrequena.sample.persistence.repository.RegionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link AreaService}.
 * Tests the service layer with real database and mapper interactions.
 *
 * @author cjrequena
 */
@SpringBootTest
@ActiveProfiles("integrationTest")
@DisplayName("AreaService Integration Tests")
class AreaServiceIT {

  @Autowired
  private AreaService areaService;

  @Autowired
  private AreaRepository areaRepository;

  @Autowired
  private CityRepository cityRepository;

  @Autowired
  private RegionRepository regionRepository;

  @Autowired
  private CountryRepository countryRepository;

  @Autowired
  private CountryService countryService;

  @Autowired
  private RegionService regionService;

  @Autowired
  private CityService cityService;

  private UUID countryId;
  private UUID regionId;
  private UUID cityId;

  @BeforeEach
  void setUp() {
    // Clear repositories in correct order (children first)
    areaRepository.deleteAll();
    cityRepository.deleteAll();
    regionRepository.deleteAll();
    countryRepository.deleteAll();

    // Create hierarchy: Country -> Region -> City
    Country country = createCountryDomain("Spain", "ES", "ESP", true);
    Country createdCountry = countryService.create(country);
    countryId = createdCountry.getId();

    Region region = createRegionDomain("Catalonia", countryId, true);
    Region createdRegion = regionService.create(region);
    regionId = createdRegion.getId();

    City city = createCityDomain("Barcelona", regionId, true);
    City createdCity = cityService.create(city);
    cityId = createdCity.getId();
  }

  // ================================================================
  // Create Operations
  // ================================================================

  @Test
  @DisplayName("Should create area successfully")
  void shouldCreateArea() {
    Area area = createAreaDomain("Example", cityId, AreaType.DISTRICT, true);

    Area result = areaService.create(area);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isNotNull();
    assertThat(result.getName()).isEqualTo("Example");
    assertThat(result.getCityId()).isEqualTo(cityId);
  }

  // ================================================================
  // Read Operations
  // ================================================================

  @Test
  @DisplayName("Should find area by ID")
  void shouldFindById() {
    Area created = areaService.create(createAreaDomain("Example", cityId, AreaType.DISTRICT, true));

    Optional<Area> result = areaService.findById(created.getId());

    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo("Example");
  }

  @Test
  @DisplayName("Should return empty when area not found by ID")
  void shouldReturnEmptyWhenNotFoundById() {
    Optional<Area> result = areaService.findById(UUID.randomUUID());

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Should find all areas")
  void shouldFindAll() {
    areaService.create(createAreaDomain("Example", cityId, AreaType.DISTRICT, true));
    areaService.create(createAreaDomain("Gràcia", cityId, AreaType.DISTRICT, true));

    List<Area> result = areaService.findAll();

    assertThat(result).hasSizeGreaterThan(0);
  }

//  @Test
//  @DisplayName("Should find all active areas")
//  void shouldFindAllActive() {
//    areaService.create(createAreaDomain("Example", cityId, AreaType.DISTRICT, true));
//    areaService.create(createAreaDomain("Gràcia", cityId, AreaType.DISTRICT, false));
//
//    List<Area> result = areaService.findAllActive();
//
//    assertThat(result).hasSize(1);
//    assertThat(result.get(0).getName()).isEqualTo("Example");
//  }

//  @Test
//  @DisplayName("Should find areas by active status with pagination")
//  void shouldFindByActiveWithPagination() {
//    areaService.create(createAreaDomain("Example", cityId, AreaType.DISTRICT, true));
//    areaService.create(createAreaDomain("Gràcia", cityId, AreaType.DISTRICT, true));
//
//    Page<Area> result = areaService.findByActive(true, PageRequest.of(0, 10));
//
//    assertThat(result.getContent()).hasSize(2);
//  }

//  @Test
//  @DisplayName("Should find areas by city ID")
//  void shouldFindByCityId() {
//    areaService.create(createAreaDomain("Example", cityId, AreaType.DISTRICT, true));
//    areaService.create(createAreaDomain("Gràcia", cityId, AreaType.DISTRICT, true));
//
//    List<Area> result = areaService.findByCityId(cityId);
//
//    assertThat(result).hasSize(2);
//  }

//  @Test
//  @DisplayName("Should find active areas by city ID")
//  void shouldFindActiveByCityId() {
//    areaService.create(createAreaDomain("Example", cityId, AreaType.DISTRICT, true));
//    areaService.create(createAreaDomain("Gràcia", cityId, AreaType.DISTRICT, false));
//
//    List<Area> result = areaService.findActiveByCityId(cityId);
//
//    assertThat(result).hasSize(1);
//    assertThat(result.get(0).getName()).isEqualTo("Example");
//  }

//  @Test
//  @DisplayName("Should find areas by city ID with pagination")
//  void shouldFindByCityIdWithPagination() {
//    areaService.create(createAreaDomain("Example", cityId, AreaType.DISTRICT, true));
//    areaService.create(createAreaDomain("Gràcia", cityId, AreaType.DISTRICT, true));
//
//    Page<Area> result = areaService.findByCityId(cityId, PageRequest.of(0, 10));
//
//    assertThat(result.getContent()).hasSize(2);
//  }

//  @Test
//  @DisplayName("Should find areas by area type")
//  void shouldFindByAreaType() {
//    areaService.create(createAreaDomain("Example", cityId, AreaType.DISTRICT, true));
//    areaService.create(createAreaDomain("Gràcia", cityId, AreaType.NEIGHBORHOOD, true));
//
//    List<Area> result = areaService.findByAreaType(AreaType.DISTRICT.getValue());
//
//    assertThat(result).hasSize(1);
//    assertThat(result.get(0).getName()).isEqualTo("Example");
//  }

//  @Test
//  @DisplayName("Should find active areas by area type")
//  void shouldFindActiveByAreaType() {
//    areaService.create(createAreaDomain("Example", cityId, AreaType.DISTRICT, true));
//    areaService.create(createAreaDomain("Gràcia", cityId, AreaType.DISTRICT, false));
//
//    List<Area> result = areaService.findActiveByAreaType(AreaType.DISTRICT.getValue());
//
//    assertThat(result).hasSize(1);
//    assertThat(result.get(0).getName()).isEqualTo("Example");
//  }

//  @Test
//  @DisplayName("Should find areas by city ID and area type")
//  void shouldFindByCityIdAndAreaType() {
//    areaService.create(createAreaDomain("Example", cityId, AreaType.DISTRICT, true));
//    areaService.create(createAreaDomain("Gràcia", cityId, AreaType.NEIGHBORHOOD, true));
//
//    List<Area> result = areaService.findByCityIdAndAreaType(cityId, AreaType.DISTRICT.getValue());
//
//    assertThat(result).hasSize(1);
//    assertThat(result.get(0).getName()).isEqualTo("Example");
//  }

//  @Test
//  @DisplayName("Should find areas by postal code")
//  void shouldFindByPostalCode() {
//    Area area = createAreaDomain("Example", cityId, AreaType.DISTRICT, true);
//    area.setPostalCode("08001");
//    areaService.create(area);
//
//    List<Area> result = areaService.findByPostalCode("08001");
//
//    assertThat(result).hasSize(1);
//    assertThat(result.get(0).getName()).isEqualTo("Example");
//  }

//  @Test
//  @DisplayName("Should find areas by city ID and postal code")
//  void shouldFindByCityIdAndPostalCode() {
//    Area area = createAreaDomain("Example", cityId, AreaType.DISTRICT, true);
//    area.setPostalCode("08001");
//    areaService.create(area);
//
//    List<Area> result = areaService.findByCityIdAndPostalCode(cityId, "08001");
//
//    assertThat(result).hasSize(1);
//    assertThat(result.get(0).getName()).isEqualTo("Example");
//  }

//  @Test
//  @DisplayName("Should find area by city ID and name")
//  void shouldFindByCityIdAndName() {
//    areaService.create(createAreaDomain("Example", cityId, AreaType.DISTRICT, true));
//
//    Optional<Area> result = areaService.findByCityIdAndName(cityId, "Example");
//
//    assertThat(result).isPresent();
//    assertThat(result.get().getName()).isEqualTo("Example");
//  }

//  @Test
//  @DisplayName("Should find areas by name containing")
//  void shouldFindByNameContaining() {
//    areaService.create(createAreaDomain("Example Esquerra", cityId, AreaType.DISTRICT, true));
//    areaService.create(createAreaDomain("Example Dreta", cityId, AreaType.DISTRICT, true));
//
//    List<Area> result = areaService.findByNameContaining("Example");
//
//    assertThat(result).hasSize(2);
//  }

//  @Test
//  @DisplayName("Should find areas by city ID and population greater than")
//  void shouldFindByCityIdAndPopulationGreaterThan() {
//    Area smallArea = createAreaDomain("Small Area", cityId, AreaType.DISTRICT, true);
//    smallArea.setPopulation(PopulationVO.builder().value(50000L).build());
//    areaService.create(smallArea);
//
//    Area largeArea = createAreaDomain("Large Area", cityId, AreaType.DISTRICT, true);
//    largeArea.setPopulation(PopulationVO.builder().value(150000L).build());
//    areaService.create(largeArea);
//
//    List<Area> result = areaService.findByCityIdAndPopulationGreaterThan(cityId, 100000L);
//
//    assertThat(result).hasSize(1);
//    assertThat(result.get(0).getName()).isEqualTo("Large Area");
//  }

//  @Test
//  @DisplayName("Should find areas by city ID ordered by population desc")
//  void shouldFindByCityIdOrderByPopulationDesc() {
//    Area smallArea = createAreaDomain("Small Area", cityId, AreaType.DISTRICT, true);
//    smallArea.setPopulation(PopulationVO.builder().value(50000L).build());
//    areaService.create(smallArea);
//
//    Area largeArea = createAreaDomain("Large Area", cityId, AreaType.DISTRICT, true);
//    largeArea.setPopulation(PopulationVO.builder().value(150000L).build());
//    areaService.create(largeArea);
//
//    Page<Area> result = areaService.findByCityIdOrderByPopulationDesc(cityId, PageRequest.of(0, 10));
//
//    assertThat(result.getContent()).hasSize(2);
//    assertThat(result.getContent().get(0).getName()).isEqualTo("Large Area");
//  }

//  @Test
//  @DisplayName("Should find areas created between dates")
//  void shouldFindByCreatedAtBetween() {
//    OffsetDateTime start = OffsetDateTime.now().minusDays(1);
//    OffsetDateTime end = OffsetDateTime.now().plusDays(1);
//
//    areaService.create(createAreaDomain("Example", cityId, AreaType.DISTRICT, true));
//
//    List<Area> result = areaService.findByCreatedAtBetween(start, end);
//
//    assertThat(result).hasSize(1);
//  }

  // ================================================================
  // Update Operations
  // ================================================================

  @Test
  @DisplayName("Should update area successfully")
  void shouldUpdateArea() {
    Area created = areaService.create(createAreaDomain("Example", cityId, AreaType.DISTRICT, true));

    created.setName("Example Updated");
    Area result = areaService.update(created.getId(), created);

    assertThat(result.getName()).isEqualTo("Example Updated");
  }

  @Test
  @DisplayName("Should throw exception when updating non-existent area")
  void shouldThrowExceptionWhenUpdatingNonExistent() {
    Area area = createAreaDomain("Example", cityId, AreaType.DISTRICT, true);

    assertThatThrownBy(() -> areaService.update(UUID.randomUUID(), area))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Area not found");
  }

  // ================================================================
  // Delete Operations
  // ================================================================

  @Test
  @DisplayName("Should delete area by ID")
  void shouldDeleteById() {
    Area created = areaService.create(createAreaDomain("Example", cityId, AreaType.DISTRICT, true));

    areaService.deleteById(created.getId());

    Optional<Area> result = areaService.findById(created.getId());
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Should throw exception when deleting non-existent area")
  void shouldThrowExceptionWhenDeletingNonExistent() {
    assertThatThrownBy(() -> areaService.deleteById(UUID.randomUUID()))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Area not found");
  }

  // ================================================================
  // Existence Checks
  // ================================================================

  @Test
  @DisplayName("Should check if area exists by ID")
  void shouldCheckExistsById() {
    Area created = areaService.create(createAreaDomain("Example", cityId, AreaType.DISTRICT, true));

    boolean result = areaService.existsById(created.getId());

    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("Should return false when area does not exist by ID")
  void shouldReturnFalseWhenNotExistsById() {
    boolean result = areaService.existsById(UUID.randomUUID());

    assertThat(result).isFalse();
  }

//  @Test
//  @DisplayName("Should check if area exists by city ID and name")
//  void shouldCheckExistsByCityIdAndName() {
//    areaService.create(createAreaDomain("Example", cityId, AreaType.DISTRICT, true));
//
//    boolean result = areaService.existsByCityIdAndName(cityId, "Example");
//
//    assertThat(result).isTrue();
//  }

//  @Test
//  @DisplayName("Should check if area exists by postal code")
//  void shouldCheckExistsByPostalCode() {
//    Area area = createAreaDomain("Example", cityId, AreaType.DISTRICT, true);
//    area.setPostalCode("08001");
//    areaService.create(area);
//
//    boolean result = areaService.existsByPostalCode("08001");
//
//    assertThat(result).isTrue();
//  }

  // ================================================================
  // Count Operations
  // ================================================================

  @Test
  @DisplayName("Should count all areas")
  void shouldCount() {
    areaService.create(createAreaDomain("Example", cityId, AreaType.DISTRICT, true));
    areaService.create(createAreaDomain("Gràcia", cityId, AreaType.DISTRICT, true));

    long result = areaService.count();

    assertThat(result).isEqualTo(2L);
  }

  // ================================================================
  // Helper Methods
  // ================================================================

  private Country createCountryDomain(String name, String alpha2, String alpha3, boolean active) {
    final OffsetDateTime now = OffsetDateTime.now();
    Country country = new Country();
    country.setName(name);
    country.setIsoCode(IsoCodeVO
      .builder()
      .alpha2(alpha2)
      .alpha3(alpha3)
      .numeric("724")
      .build());
    country.setActive(active);
    country.setAuditInfo(
      AuditInfoVO
        .builder()
        .createdAt(now)
        .updatedAt(now)
        .build());
    return country;
  }

  private Region createRegionDomain(String name, UUID countryId, boolean active) {
    final OffsetDateTime now = OffsetDateTime.now();
    Region region = new Region();
    region.setName(name);
    region.setCountryId(countryId);
    region.setActive(active);
    region.setAuditInfo(AuditInfoVO
      .builder()
      .createdAt(now)
      .updatedAt(now)
      .build());
    return region;
  }

  private City createCityDomain(String name, UUID regionId, boolean active) {
    final OffsetDateTime now = OffsetDateTime.now();
    City city = new City();
    city.setName(name);
    city.setRegionId(regionId);
    city.setActive(active);
    city.setAuditInfo(AuditInfoVO
      .builder()
      .createdAt(now)
      .updatedAt(now)
      .build());
    return city;
  }

  private Area createAreaDomain(String name, UUID cityId, AreaType areaType, boolean active) {
    final OffsetDateTime now = OffsetDateTime.now();
    Area area = new Area();
    area.setName(name);
    area.setCityId(cityId);
    area.setType(areaType);
    area.setActive(active);
    area.setMetadata(MetadataVO.empty());
    area.setAuditInfo(AuditInfoVO
      .builder()
      .createdAt(now)
      .updatedAt(now)
      .build());
    return area;
  }
}
