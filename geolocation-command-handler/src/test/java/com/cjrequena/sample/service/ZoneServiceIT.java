package com.cjrequena.sample.service;

import com.cjrequena.sample.domain.model.aggregate.Area;
import com.cjrequena.sample.domain.model.aggregate.Zone;
import com.cjrequena.sample.domain.model.enums.ZoneType;
import com.cjrequena.sample.domain.model.vo.AuditInfoVO;
import com.cjrequena.sample.persistence.repository.AreaRepository;
import com.cjrequena.sample.persistence.repository.ZoneRepository;
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
 * Integration tests for {@link ZoneService}.
 *
 * @author cjrequena
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ZoneService Integration Tests")
class ZoneServiceIT {

  @Autowired
  private ZoneService zoneService;

  @Autowired
  private AreaService areaService;

  @Autowired
  private ZoneRepository zoneRepository;

  @Autowired
  private AreaRepository areaRepository;

  private UUID areaId;

  @BeforeEach
  void setUp() {
    zoneRepository.deleteAll();
    areaRepository.deleteAll();

    // Create parent area
    Area area = new Area();
    area.setName("Test Area");
    area.setActive(true);
    area.setAuditInfo(AuditInfoVO.of(OffsetDateTime.now(), OffsetDateTime.now()));
    Area createdArea = areaService.create(area);
    areaId = createdArea.getId();
  }

  @Test
  @DisplayName("Should create zone successfully")
  void shouldCreateZone() {
    Zone zone = createZoneDomain("Downtown", areaId, ZoneType.RESIDENTIAL, true);

    Zone result = zoneService.create(zone);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isNotNull();
    assertThat(result.getName()).isEqualTo("Downtown");
  }

  @Test
  @DisplayName("Should find zone by ID")
  void shouldFindById() {
    Zone created = zoneService.create(createZoneDomain("Downtown", areaId, ZoneType.RESIDENTIAL, true));

    Optional<Zone> result = zoneService.findById(created.getId());

    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo("Downtown");
  }

  @Test
  @DisplayName("Should find zones by area ID")
  void shouldFindByAreaId() {
    zoneService.create(createZoneDomain("Downtown", areaId, ZoneType.RESIDENTIAL, true));
    zoneService.create(createZoneDomain("Uptown", areaId, ZoneType.COMMERCIAL, true));

    List<Zone> result = zoneService.findByAreaId(areaId);

    assertThat(result).hasSize(2);
  }

  @Test
  @DisplayName("Should find active zones by area ID")
  void shouldFindActiveByAreaId() {
    zoneService.create(createZoneDomain("Downtown", areaId, ZoneType.RESIDENTIAL, true));
    zoneService.create(createZoneDomain("Uptown", areaId, ZoneType.COMMERCIAL, false));

    List<Zone> result = zoneService.findActiveByAreaId(areaId);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getName()).isEqualTo("Downtown");
  }

  @Test
  @DisplayName("Should find zones by zone type")
  void shouldFindByZoneType() {
    zoneService.create(createZoneDomain("Downtown", areaId, ZoneType.RESIDENTIAL, true));
    zoneService.create(createZoneDomain("Business Park", areaId, ZoneType.INDUSTRIAL, true));

    List<Zone> result = zoneService.findByZoneType(ZoneType.RESIDENTIAL.getValue());

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getName()).isEqualTo("Downtown");
  }

  @Test
  @DisplayName("Should find zones by postal code")
  void shouldFindByPostalCode() {
    Zone zone = createZoneDomain("Downtown", areaId, ZoneType.RESIDENTIAL, true);
    zone.setPostalCode("28001");
    zoneService.create(zone);

    List<Zone> result = zoneService.findByPostalCode("28001");

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getName()).isEqualTo("Downtown");
  }

  @Test
  @DisplayName("Should find zone by area ID and name")
  void shouldFindByAreaIdAndName() {
    zoneService.create(createZoneDomain("Downtown", areaId, ZoneType.RESIDENTIAL, true));

    Optional<Zone> result = zoneService.findByAreaIdAndName(areaId, "Downtown");

    assertThat(result).isPresent();
  }

  @Test
  @DisplayName("Should update zone successfully")
  void shouldUpdateZone() {
    Zone created = zoneService.create(createZoneDomain("Downtown", areaId, ZoneType.RESIDENTIAL, true));

    created.setName("Downtown District");
    Zone updated = zoneService.update(created.getId(), created);

    assertThat(updated.getName()).isEqualTo("Downtown District");
  }

  @Test
  @DisplayName("Should throw exception when updating non-existent zone")
  void shouldThrowExceptionWhenUpdatingNonExistent() {
    Zone zone = createZoneDomain("Downtown", areaId, ZoneType.RESIDENTIAL, true);

    assertThatThrownBy(() -> zoneService.update(UUID.randomUUID(), zone))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Zone not found");
  }

  @Test
  @DisplayName("Should delete zone by ID")
  void shouldDeleteById() {
    Zone created = zoneService.create(createZoneDomain("Downtown", areaId, ZoneType.RESIDENTIAL, true));

    zoneService.deleteById(created.getId());

    assertThat(zoneService.findById(created.getId())).isEmpty();
  }

  @Test
  @DisplayName("Should check if zone exists by area ID and name")
  void shouldCheckExistsByAreaIdAndName() {
    zoneService.create(createZoneDomain("Downtown", areaId, ZoneType.RESIDENTIAL, true));

    boolean result = zoneService.existsByAreaIdAndName(areaId, "Downtown");

    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("Should count all zones")
  void shouldCount() {
    final Zone downtown = zoneService.create(createZoneDomain("Downtown", areaId, ZoneType.RESIDENTIAL, true));
    zoneService.create(createZoneDomain("Uptown", areaId, ZoneType.COMMERCIAL, true));

    long result = zoneService.count();

    assertThat(result).isEqualTo(2L);
  }

  private Zone createZoneDomain(String name, UUID areaId, ZoneType type, boolean active) {
    Zone zone = new Zone();
    zone.setName(name);
    zone.setAreaId(areaId);
    zone.setType(type);
    zone.setActive(active);
    zone.setAuditInfo(AuditInfoVO.of(OffsetDateTime.now(), OffsetDateTime.now()));
    return zone;
  }
}
