package com.cjrequena.sample.service;

import com.cjrequena.sample.domain.model.Country;
import com.cjrequena.sample.domain.model.Region;
import com.cjrequena.sample.domain.model.enums.RegionType;
import com.cjrequena.sample.domain.model.vo.AuditInfoVO;
import com.cjrequena.sample.domain.model.vo.IsoCodeVO;
import com.cjrequena.sample.domain.model.vo.MetadataVO;
import com.cjrequena.sample.persistence.repository.CountryRepository;
import com.cjrequena.sample.persistence.repository.RegionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link RegionService}.
 *
 * @author cjrequena
 */
@SpringBootTest
@ActiveProfiles("integrationTest")
@DisplayName("RegionService Integration Tests")
class RegionServiceIT {

  @Autowired
  private RegionService regionService;

  @Autowired
  private CountryService countryService;

  @Autowired
  private RegionRepository regionRepository;

  @Autowired
  private CountryRepository countryRepository;

  private UUID countryId;

  @BeforeEach
  void setUp() {
    regionRepository.deleteAll();
    countryRepository.deleteAll();

    // Create parent country
    Country country = new Country();
    country.setName("Spain");
    country.setIsoCode(IsoCodeVO.of("ES", "ESP", "724"));
    country.setActive(true);
    country.setAuditInfo(AuditInfoVO.of(OffsetDateTime.now(), OffsetDateTime.now()));
    Country createdCountry = countryService.create(country);
    countryId = createdCountry.getId();
  }

  @Test
  @DisplayName("Should create region successfully")
  void shouldCreateRegion() {
    Region region = createRegionDomain("Catalonia", countryId, RegionType.AUTONOMOUS_COMMUNITY, true);

    Region result = regionService.create(region);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isNotNull();
    assertThat(result.getName()).isEqualTo("Catalonia");
  }

  @Test
  @DisplayName("Should find region by ID")
  void shouldFindById() {
    Region created = regionService.create(createRegionDomain("Catalonia", countryId, RegionType.AUTONOMOUS_COMMUNITY, true));

    Optional<Region> result = regionService.findById(created.getId());

    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo("Catalonia");
  }

//  @Test
//  @DisplayName("Should find regions by country ID")
//  void shouldFindByCountryId() {
//    regionService.create(createRegionDomain("Catalonia", countryId, RegionType.AUTONOMOUS_COMMUNITY, true));
//    regionService.create(createRegionDomain("Andalusia", countryId, RegionType.AUTONOMOUS_COMMUNITY, true));
//
//    List<Region> result = regionService.findByCountryId(countryId);
//
//    assertThat(result).hasSize(2);
//  }

//  @Test
//  @DisplayName("Should find active regions by country ID")
//  void shouldFindActiveByCountryId() {
//    regionService.create(createRegionDomain("Catalonia", countryId, RegionType.AUTONOMOUS_COMMUNITY, true));
//    regionService.create(createRegionDomain("Andalusia", countryId, RegionType.AUTONOMOUS_COMMUNITY, false));
//
//    List<Region> result = regionService.findActiveByCountryId(countryId);
//
//    assertThat(result).hasSize(1);
//    assertThat(result.get(0).getName()).isEqualTo("Catalonia");
//  }

//  @Test
//  @DisplayName("Should find regions by region type")
//  void shouldFindByRegionType() {
//    regionService.create(createRegionDomain("Catalonia", countryId, RegionType.AUTONOMOUS_COMMUNITY, true));
//    regionService.create(createRegionDomain("Madrid", countryId, RegionType.PROVINCE, true));
//
//    List<Region> result = regionService.findByRegionType(RegionType.AUTONOMOUS_COMMUNITY.getValue());
//
//    assertThat(result).hasSize(1);
//    assertThat(result.get(0).getName()).isEqualTo("Catalonia");
//  }

//  @Test
//  @DisplayName("Should find region by country ID and name")
//  void shouldFindByCountryIdAndName() {
//    regionService.create(createRegionDomain("Catalonia", countryId, RegionType.AUTONOMOUS_COMMUNITY, true));
//
//    Optional<Region> result = regionService.findByCountryIdAndName(countryId, "Catalonia");
//
//    assertThat(result).isPresent();
//  }

//  @Test
//  @DisplayName("Should find regions by population greater than")
//  void shouldFindByPopulationGreaterThan() {
//    Region catalonia = createRegionDomain("Catalonia", countryId, RegionType.AUTONOMOUS_COMMUNITY, true);
//    catalonia.setPopulation(PopulationVO.of(7_500_000L));
//    regionService.create(catalonia);
//
//    Region madrid = createRegionDomain("Madrid", countryId, RegionType.AUTONOMOUS_COMMUNITY, true);
//    madrid.setPopulation(PopulationVO.of(6_500_000L));
//    regionService.create(madrid);
//
//    List<Region> result = regionService.findByCountryIdAndPopulationGreaterThan(countryId, 7_000_000L);
//
//    assertThat(result).hasSize(1);
//    assertThat(result.get(0).getName()).isEqualTo("Catalonia");
//  }

  @Test
  @DisplayName("Should update region successfully")
  void shouldUpdateRegion() {
    Region created = regionService.create(createRegionDomain("Catalonia", countryId, RegionType.AUTONOMOUS_COMMUNITY, true));

    created.setName("Cataluña");
    Region updated = regionService.update(created.getId(), created);

    assertThat(updated.getName()).isEqualTo("Cataluña");
  }

  @Test
  @DisplayName("Should throw exception when updating non-existent region")
  void shouldThrowExceptionWhenUpdatingNonExistent() {
    Region region = createRegionDomain("Catalonia", countryId, RegionType.AUTONOMOUS_COMMUNITY, true);

    assertThatThrownBy(() -> regionService.update(UUID.randomUUID(), region))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Region not found");
  }

  @Test
  @DisplayName("Should delete region by ID")
  void shouldDeleteById() {
    Region created = regionService.create(createRegionDomain("Catalonia", countryId, RegionType.AUTONOMOUS_COMMUNITY, true));

    regionService.deleteById(created.getId());

    assertThat(regionService.findById(created.getId())).isEmpty();
  }

//  @Test
//  @DisplayName("Should check if region exists by country ID and name")
//  void shouldCheckExistsByCountryIdAndName() {
//    regionService.create(createRegionDomain("Catalonia", countryId, RegionType.AUTONOMOUS_COMMUNITY, true));
//
//    boolean result = regionService.existsByCountryIdAndName(countryId, "Catalonia");
//
//    assertThat(result).isTrue();
//  }

  @Test
  @DisplayName("Should count all regions")
  void shouldCount() {
    regionService.create(createRegionDomain("Catalonia", countryId, RegionType.AUTONOMOUS_COMMUNITY, true));
    regionService.create(createRegionDomain("Andalusia", countryId, RegionType.AUTONOMOUS_COMMUNITY, true));

    long result = regionService.count();

    assertThat(result).isEqualTo(2L);
  }

  private Region createRegionDomain(String name, UUID countryId, RegionType type, boolean active) {
    Region region = new Region();
    region.setName(name);
    region.setCountryId(countryId);
    region.setType(type);
    region.setActive(active);
    region.setMetadata(MetadataVO.empty());
    region.setAuditInfo(AuditInfoVO.of(OffsetDateTime.now(), OffsetDateTime.now()));
    return region;
  }
}
