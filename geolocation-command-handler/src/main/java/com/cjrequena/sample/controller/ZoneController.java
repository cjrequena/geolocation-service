//package com.cjrequena.sample.controller;
//
//import com.cjrequena.sample.controller.dto.CreateZoneRequestDTO;
//import com.cjrequena.sample.controller.dto.UpdateZoneRequestDTO;
//import com.cjrequena.sample.controller.dto.ZoneResponseDTO;
//import com.cjrequena.sample.domain.mapper.ZoneMapper;
//import com.cjrequena.sample.domain.model.Zone;
//import com.cjrequena.sample.domain.model.enums.ZoneType;
//import com.cjrequena.sample.domain.model.vo.MetadataVO;
//import com.cjrequena.sample.service.ZoneService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.log4j.Log4j2;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.web.PageableDefault;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.net.URI;
//import java.util.List;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
///**
// * REST controller for Zone operations.
// *
// * <p>Provides CRUD operations and query endpoints for zones (blocks, sectors, precincts).
// * Zones belong to areas and can contain locations.
// *
// * @author cjrequena
// */
//@Log4j2
//@RestController
//@RequestMapping("/api/v1/zones")
//@RequiredArgsConstructor
//@Tag(name = "Zones", description = "Zone management endpoints (blocks, sectors, precincts)")
//public class ZoneController {
//
//  private final ZoneService zoneService;
//  private final ZoneMapper zoneMapper;
//
//  /**
//   * Create a new zone.
//   *
//   * @param requestDTO the zone creation request
//   * @return the created zone with 201 status
//   */
//  @PostMapping
//  @Operation(summary = "Create a new zone", description = "Creates a new zone within an area")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "201", description = "Zone created successfully",
//      content = @Content(schema = @Schema(implementation = ZoneResponseDTO.class))),
//    @ApiResponse(responseCode = "400", description = "Invalid request data"),
//    @ApiResponse(responseCode = "404", description = "Parent area not found")
//  })
//  public ResponseEntity<ZoneResponseDTO> createZone(
//    @Valid @RequestBody CreateZoneRequestDTO requestDTO) {
//
//    log.info("Creating zone: {} in area: {}", requestDTO.getName(), requestDTO.getAreaId());
//
//    // Convert DTO to domain model
//    Zone zone = Zone.create(
//      UUID.randomUUID(),
//      UUID.fromString(requestDTO.getAreaId()),
//      requestDTO.getName(),
//      ZoneType.valueOf(requestDTO.getZoneType()),
//      requestDTO.getGeoShapeId() != null ? UUID.fromString(requestDTO.getGeoShapeId()) : null,
//      requestDTO.getPostalCode(),
//      requestDTO.getActive() != null ? requestDTO.getActive() : Boolean.TRUE,
//      requestDTO.getMetadata() != null ? MetadataVO.of(requestDTO.getMetadata()) : MetadataVO.empty()
//    );
//
//    // Create via service
//    Zone created = zoneService.create(zone);
//
//    // Convert to response DTO
//    ZoneResponseDTO responseDTO = zoneMapper.toResponseDTO(created);
//
//    log.info("Zone created with ID: {}", created.getId());
//
//    return ResponseEntity
//      .created(URI.create("/api/v1/zones/" + created.getId()))
//      .body(responseDTO);
//  }
//
//  /**
//   * Get a zone by ID.
//   *
//   * @param id the zone ID
//   * @return the zone if found, 404 otherwise
//   */
//  @GetMapping("/{id}")
//  @Operation(summary = "Get zone by ID", description = "Retrieves a zone by its unique identifier")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Zone found",
//      content = @Content(schema = @Schema(implementation = ZoneResponseDTO.class))),
//    @ApiResponse(responseCode = "404", description = "Zone not found")
//  })
//  public ResponseEntity<ZoneResponseDTO> getZoneById(
//    @Parameter(description = "Zone ID", required = true)
//    @PathVariable UUID id) {
//
//    log.debug("Getting zone by ID: {}", id);
//
//    return zoneService.findById(id)
//      .map(zoneMapper::toResponseDTO)
//      .map(ResponseEntity::ok)
//      .orElse(ResponseEntity.notFound().build());
//  }
//
//  /**
//   * Get all zones.
//   *
//   * @return list of all zones
//   */
//  @GetMapping
//  @Operation(summary = "Get all zones", description = "Retrieves all zones")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Zones retrieved successfully")
//  })
//  public ResponseEntity<List<ZoneResponseDTO>> getAllZones() {
//    log.debug("Getting all zones");
//
//    List<ZoneResponseDTO> zones = zoneService.findAll().stream()
//      .map(zoneMapper::toResponseDTO)
//      .collect(Collectors.toList());
//
//    return ResponseEntity.ok(zones);
//  }
//
//  /**
//   * Get zones by area.
//   *
//   * @param areaId the area ID
//   * @return list of zones in the area
//   */
//  @GetMapping("/area/{areaId}")
//  @Operation(summary = "Get zones by area", description = "Retrieves all zones within a specific area")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Zones retrieved successfully")
//  })
//  public ResponseEntity<List<ZoneResponseDTO>> getZonesByArea(
//    @Parameter(description = "Area ID", required = true)
//    @PathVariable UUID areaId) {
//
//    log.debug("Getting zones by area: {}", areaId);
//
//    List<ZoneResponseDTO> zones = zoneService.findByAreaId(areaId).stream()
//      .map(zoneMapper::toResponseDTO)
//      .collect(Collectors.toList());
//
//    return ResponseEntity.ok(zones);
//  }
//
//  /**
//   * Get zones with pagination.
//   *
//   * @param pageable pagination parameters
//   * @return page of zones
//   */
//  @GetMapping("/page")
//  @Operation(summary = "Get zones with pagination", description = "Retrieves zones with pagination support")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Zones retrieved successfully")
//  })
//  public ResponseEntity<Page<ZoneResponseDTO>> getZonesPage(
//    @PageableDefault(size = 20) Pageable pageable) {
//
//    log.debug("Getting zones page: {}", pageable);
//
//    Page<ZoneResponseDTO> page = zoneService.findByActive(true, pageable)
//      .map(zoneMapper::toResponseDTO);
//
//    return ResponseEntity.ok(page);
//  }
//
//  /**
//   * Update a zone.
//   *
//   * @param id the zone ID
//   * @param requestDTO the update request
//   * @return the updated zone
//   */
//  @PutMapping("/{id}")
//  @Operation(summary = "Update a zone", description = "Updates an existing zone")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Zone updated successfully",
//      content = @Content(schema = @Schema(implementation = ZoneResponseDTO.class))),
//    @ApiResponse(responseCode = "404", description = "Zone not found"),
//    @ApiResponse(responseCode = "400", description = "Invalid request data")
//  })
//  public ResponseEntity<ZoneResponseDTO> updateZone(
//    @Parameter(description = "Zone ID", required = true)
//    @PathVariable UUID id,
//    @Valid @RequestBody UpdateZoneRequestDTO requestDTO) {
//
//    log.info("Updating zone with ID: {}", id);
//
//    // Convert DTO to domain model
//    Zone zone = Zone.create(
//      id,
//      UUID.fromString(requestDTO.getAreaId()),
//      requestDTO.getName(),
//      ZoneType.valueOf(requestDTO.getZoneType()),
//      requestDTO.getGeoShapeId() != null ? UUID.fromString(requestDTO.getGeoShapeId()) : null,
//      requestDTO.getPostalCode(),
//      requestDTO.getActive() != null ? requestDTO.getActive() : Boolean.TRUE,
//      requestDTO.getMetadata() != null ? MetadataVO.of(requestDTO.getMetadata()) : MetadataVO.empty()
//    );
//
//    // Update via service
//    Zone updated = zoneService.update(id, zone);
//
//    // Convert to response DTO
//    ZoneResponseDTO responseDTO = zoneMapper.toResponseDTO(updated);
//
//    log.info("Zone updated with ID: {}", id);
//
//    return ResponseEntity.ok(responseDTO);
//  }
//
//  /**
//   * Delete a zone.
//   *
//   * @param id the zone ID
//   * @return 204 No Content on success
//   */
//  @DeleteMapping("/{id}")
//  @Operation(summary = "Delete a zone", description = "Deletes a zone by ID")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "204", description = "Zone deleted successfully"),
//    @ApiResponse(responseCode = "404", description = "Zone not found")
//  })
//  public ResponseEntity<Void> deleteZone(
//    @Parameter(description = "Zone ID", required = true)
//    @PathVariable UUID id) {
//
//    log.info("Deleting zone with ID: {}", id);
//
//    zoneService.deleteById(id);
//
//    log.info("Zone deleted with ID: {}", id);
//
//    return ResponseEntity.noContent().build();
//  }
//
//  /**
//   * Search zones by name.
//   *
//   * @param name the name substring to search for
//   * @return list of matching zones
//   */
//  @GetMapping("/search")
//  @Operation(summary = "Search zones by name", description = "Searches zones by name (case-insensitive partial match)")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Search completed successfully")
//  })
//  public ResponseEntity<List<ZoneResponseDTO>> searchZonesByName(
//    @Parameter(description = "Name substring to search for", required = true)
//    @RequestParam String name) {
//
//    log.debug("Searching zones by name: {}", name);
//
//    List<ZoneResponseDTO> zones = zoneService.findByNameContaining(name).stream()
//      .map(zoneMapper::toResponseDTO)
//      .collect(Collectors.toList());
//
//    return ResponseEntity.ok(zones);
//  }
//
//  /**
//   * Get zones by postal code.
//   *
//   * @param postalCode the postal code
//   * @return list of zones with the postal code
//   */
//  @GetMapping("/postal-code/{postalCode}")
//  @Operation(summary = "Get zones by postal code", description = "Retrieves zones by postal code")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Zones retrieved successfully")
//  })
//  public ResponseEntity<List<ZoneResponseDTO>> getZonesByPostalCode(
//    @Parameter(description = "Postal code", required = true)
//    @PathVariable String postalCode) {
//
//    log.debug("Getting zones by postal code: {}", postalCode);
//
//    List<ZoneResponseDTO> zones = zoneService.findByPostalCode(postalCode).stream()
//      .map(zoneMapper::toResponseDTO)
//      .collect(Collectors.toList());
//
//    return ResponseEntity.ok(zones);
//  }
//
//  /**
//   * Check if a zone exists by ID.
//   *
//   * @param id the zone ID
//   * @return 200 if exists, 404 if not
//   */
//  @GetMapping("/{id}/exists")
//  @Operation(summary = "Check if zone exists", description = "Checks if a zone exists by ID")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Zone exists"),
//    @ApiResponse(responseCode = "404", description = "Zone does not exist")
//  })
//  public ResponseEntity<Void> checkZoneExists(
//    @Parameter(description = "Zone ID", required = true)
//    @PathVariable UUID id) {
//
//    log.debug("Checking if zone exists: {}", id);
//
//    return zoneService.existsById(id)
//      ? ResponseEntity.ok().build()
//      : ResponseEntity.notFound().build();
//  }
//
//  /**
//   * Get total count of zones.
//   *
//   * @return the count
//   */
//  @GetMapping("/count")
//  @Operation(summary = "Get zone count", description = "Returns the total number of zones")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
//  })
//  public ResponseEntity<Long> getZoneCount() {
//    log.debug("Getting zone count");
//
//    long count = zoneService.count();
//
//    return ResponseEntity.ok(count);
//  }
//}
