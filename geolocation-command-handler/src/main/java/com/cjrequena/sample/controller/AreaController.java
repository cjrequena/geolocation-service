//package com.cjrequena.sample.controller;
//
//import com.cjrequena.sample.controller.dto.AreaResponseDTO;
//import com.cjrequena.sample.controller.dto.CreateAreaRequestDTO;
//import com.cjrequena.sample.controller.dto.UpdateAreaRequestDTO;
//import com.cjrequena.sample.domain.mapper.AreaMapper;
//import com.cjrequena.sample.domain.model.Area;
//import com.cjrequena.sample.domain.model.enums.AreaType;
//import com.cjrequena.sample.domain.model.vo.MetadataVO;
//import com.cjrequena.sample.domain.model.vo.PopulationVO;
//import com.cjrequena.sample.service.AreaService;
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
// * REST controller for Area operations.
// *
// * <p>Provides CRUD operations and query endpoints for areas (districts, boroughs, neighborhoods).
// * Areas belong to cities and can contain zones.
// *
// * @author cjrequena
// */
//@Log4j2
//@RestController
//@RequestMapping("/api/v1/areas")
//@RequiredArgsConstructor
//@Tag(name = "Areas", description = "Area management endpoints (districts, boroughs, neighborhoods)")
//public class AreaController {
//
//  private final AreaService areaService;
//  private final AreaMapper areaMapper;
//
//  /**
//   * Create a new area.
//   *
//   * @param requestDTO the area creation request
//   * @return the created area with 201 status
//   */
//  @PostMapping
//  @Operation(summary = "Create a new area", description = "Creates a new area within a city")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "201", description = "Area created successfully",
//      content = @Content(schema = @Schema(implementation = AreaResponseDTO.class))),
//    @ApiResponse(responseCode = "400", description = "Invalid request data"),
//    @ApiResponse(responseCode = "404", description = "Parent city not found")
//  })
//  public ResponseEntity<AreaResponseDTO> createArea(
//    @Valid @RequestBody CreateAreaRequestDTO requestDTO) {
//
//    log.info("Creating area: {} in city: {}", requestDTO.getName(), requestDTO.getCityId());
//
//    // Convert DTO to domain model
//    Area area = Area.create(
//      UUID.randomUUID(),
//      UUID.fromString(requestDTO.getCityId()),
//      requestDTO.getName(),
//      AreaType.valueOf(requestDTO.getAreaType()),
//      requestDTO.getGeoShapeId() != null ? UUID.fromString(requestDTO.getGeoShapeId()) : null,
//      requestDTO.getPopulation() != null ? PopulationVO.of(requestDTO.getPopulation()) : null,
//      requestDTO.getPostalCode(),
//      requestDTO.getActive() != null ? requestDTO.getActive() : Boolean.TRUE,
//      requestDTO.getMetadata() != null ? MetadataVO.of(requestDTO.getMetadata()) : MetadataVO.empty()
//    );
//
//    // Create via service
//    Area created = areaService.create(area);
//
//    // Convert to response DTO
//    AreaResponseDTO responseDTO = areaMapper.toResponseDTO(created);
//
//    log.info("Area created with ID: {}", created.getId());
//
//    return ResponseEntity
//      .created(URI.create("/api/v1/areas/" + created.getId()))
//      .body(responseDTO);
//  }
//
//  /**
//   * Get an area by ID.
//   *
//   * @param id the area ID
//   * @return the area if found, 404 otherwise
//   */
//  @GetMapping("/{id}")
//  @Operation(summary = "Get area by ID", description = "Retrieves an area by its unique identifier")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Area found",
//      content = @Content(schema = @Schema(implementation = AreaResponseDTO.class))),
//    @ApiResponse(responseCode = "404", description = "Area not found")
//  })
//  public ResponseEntity<AreaResponseDTO> getAreaById(
//    @Parameter(description = "Area ID", required = true)
//    @PathVariable UUID id) {
//
//    log.debug("Getting area by ID: {}", id);
//
//    return areaService.findById(id)
//      .map(areaMapper::toResponseDTO)
//      .map(ResponseEntity::ok)
//      .orElse(ResponseEntity.notFound().build());
//  }
//
//  /**
//   * Get all areas.
//   *
//   * @return list of all areas
//   */
//  @GetMapping
//  @Operation(summary = "Get all areas", description = "Retrieves all areas")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Areas retrieved successfully")
//  })
//  public ResponseEntity<List<AreaResponseDTO>> getAllAreas() {
//    log.debug("Getting all areas");
//
//    List<AreaResponseDTO> areas = areaService.findAll().stream()
//      .map(areaMapper::toResponseDTO)
//      .collect(Collectors.toList());
//
//    return ResponseEntity.ok(areas);
//  }
//
//  /**
//   * Get areas by city.
//   *
//   * @param cityId the city ID
//   * @return list of areas in the city
//   */
//  @GetMapping("/city/{cityId}")
//  @Operation(summary = "Get areas by city", description = "Retrieves all areas within a specific city")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Areas retrieved successfully")
//  })
//  public ResponseEntity<List<AreaResponseDTO>> getAreasByCity(
//    @Parameter(description = "City ID", required = true)
//    @PathVariable UUID cityId) {
//
//    log.debug("Getting areas by city: {}", cityId);
//
//    List<AreaResponseDTO> areas = areaService.findByCityId(cityId).stream()
//      .map(areaMapper::toResponseDTO)
//      .collect(Collectors.toList());
//
//    return ResponseEntity.ok(areas);
//  }
//
//  /**
//   * Get areas with pagination.
//   *
//   * @param pageable pagination parameters
//   * @return page of areas
//   */
//  @GetMapping("/page")
//  @Operation(summary = "Get areas with pagination", description = "Retrieves areas with pagination support")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Areas retrieved successfully")
//  })
//  public ResponseEntity<Page<AreaResponseDTO>> getAreasPage(
//    @PageableDefault(size = 20) Pageable pageable) {
//
//    log.debug("Getting areas page: {}", pageable);
//
//    Page<AreaResponseDTO> page = areaService.findByActive(true, pageable)
//      .map(areaMapper::toResponseDTO);
//
//    return ResponseEntity.ok(page);
//  }
//
//  /**
//   * Update an area.
//   *
//   * @param id the area ID
//   * @param requestDTO the update request
//   * @return the updated area
//   */
//  @PutMapping("/{id}")
//  @Operation(summary = "Update an area", description = "Updates an existing area")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Area updated successfully",
//      content = @Content(schema = @Schema(implementation = AreaResponseDTO.class))),
//    @ApiResponse(responseCode = "404", description = "Area not found"),
//    @ApiResponse(responseCode = "400", description = "Invalid request data")
//  })
//  public ResponseEntity<AreaResponseDTO> updateArea(
//    @Parameter(description = "Area ID", required = true)
//    @PathVariable UUID id,
//    @Valid @RequestBody UpdateAreaRequestDTO requestDTO) {
//
//    log.info("Updating area with ID: {}", id);
//
//    // Convert DTO to domain model
//    Area area = Area.create(
//      id,
//      UUID.fromString(requestDTO.getCityId()),
//      requestDTO.getName(),
//      AreaType.valueOf(requestDTO.getAreaType()),
//      requestDTO.getGeoShapeId() != null ? UUID.fromString(requestDTO.getGeoShapeId()) : null,
//      requestDTO.getPopulation() != null ? PopulationVO.of(requestDTO.getPopulation()) : null,
//      requestDTO.getPostalCode(),
//      requestDTO.getActive() != null ? requestDTO.getActive() : Boolean.TRUE,
//      requestDTO.getMetadata() != null ? MetadataVO.of(requestDTO.getMetadata()) : MetadataVO.empty()
//    );
//
//    // Update via service
//    Area updated = areaService.update(id, area);
//
//    // Convert to response DTO
//    AreaResponseDTO responseDTO = areaMapper.toResponseDTO(updated);
//
//    log.info("Area updated with ID: {}", id);
//
//    return ResponseEntity.ok(responseDTO);
//  }
//
//  /**
//   * Delete an area.
//   *
//   * @param id the area ID
//   * @return 204 No Content on success
//   */
//  @DeleteMapping("/{id}")
//  @Operation(summary = "Delete an area", description = "Deletes an area by ID")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "204", description = "Area deleted successfully"),
//    @ApiResponse(responseCode = "404", description = "Area not found")
//  })
//  public ResponseEntity<Void> deleteArea(
//    @Parameter(description = "Area ID", required = true)
//    @PathVariable UUID id) {
//
//    log.info("Deleting area with ID: {}", id);
//
//    areaService.deleteById(id);
//
//    log.info("Area deleted with ID: {}", id);
//
//    return ResponseEntity.noContent().build();
//  }
//
//  /**
//   * Search areas by name.
//   *
//   * @param name the name substring to search for
//   * @return list of matching areas
//   */
//  @GetMapping("/search")
//  @Operation(summary = "Search areas by name", description = "Searches areas by name (case-insensitive partial match)")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Search completed successfully")
//  })
//  public ResponseEntity<List<AreaResponseDTO>> searchAreasByName(
//    @Parameter(description = "Name substring to search for", required = true)
//    @RequestParam String name) {
//
//    log.debug("Searching areas by name: {}", name);
//
//    List<AreaResponseDTO> areas = areaService.findByNameContaining(name).stream()
//      .map(areaMapper::toResponseDTO)
//      .collect(Collectors.toList());
//
//    return ResponseEntity.ok(areas);
//  }
//
//  /**
//   * Get areas by postal code.
//   *
//   * @param postalCode the postal code
//   * @return list of areas with the postal code
//   */
//  @GetMapping("/postal-code/{postalCode}")
//  @Operation(summary = "Get areas by postal code", description = "Retrieves areas by postal code")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Areas retrieved successfully")
//  })
//  public ResponseEntity<List<AreaResponseDTO>> getAreasByPostalCode(
//    @Parameter(description = "Postal code", required = true)
//    @PathVariable String postalCode) {
//
//    log.debug("Getting areas by postal code: {}", postalCode);
//
//    List<AreaResponseDTO> areas = areaService.findByPostalCode(postalCode).stream()
//      .map(areaMapper::toResponseDTO)
//      .collect(Collectors.toList());
//
//    return ResponseEntity.ok(areas);
//  }
//
//  /**
//   * Check if an area exists by ID.
//   *
//   * @param id the area ID
//   * @return 200 if exists, 404 if not
//   */
//  @GetMapping("/{id}/exists")
//  @Operation(summary = "Check if area exists", description = "Checks if an area exists by ID")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Area exists"),
//    @ApiResponse(responseCode = "404", description = "Area does not exist")
//  })
//  public ResponseEntity<Void> checkAreaExists(
//    @Parameter(description = "Area ID", required = true)
//    @PathVariable UUID id) {
//
//    log.debug("Checking if area exists: {}", id);
//
//    return areaService.existsById(id)
//      ? ResponseEntity.ok().build()
//      : ResponseEntity.notFound().build();
//  }
//
//  /**
//   * Get total count of areas.
//   *
//   * @return the count
//   */
//  @GetMapping("/count")
//  @Operation(summary = "Get area count", description = "Returns the total number of areas")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
//  })
//  public ResponseEntity<Long> getAreaCount() {
//    log.debug("Getting area count");
//
//    long count = areaService.count();
//
//    return ResponseEntity.ok(count);
//  }
//}
