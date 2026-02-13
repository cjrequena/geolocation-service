package com.cjrequena.sample.controller;

import com.cjrequena.sample.controller.dto.RegionRequestDTO;
import com.cjrequena.sample.controller.dto.RegionResponseDTO;
import com.cjrequena.sample.domain.mapper.RegionMapper;
import com.cjrequena.sample.domain.model.Region;
import com.cjrequena.sample.service.RegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.cjrequena.sample.shared.common.util.Constant.VND_SAMPLE_SERVICE_V1;

/**
 * REST controller for Region operations.
 *
 * <p>Provides CRUD operations and query endpoints for regions (states, provinces, etc.).
 * Regions belong to countries and can contain cities.
 *
 * @author cjrequena
 */
@Log4j2
@RestController
@RequestMapping(value = RegionController.ENDPOINT, headers = {RegionController.ACCEPT_VERSION})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Tag(name = "Regions", description = "Region management endpoints (states, provinces, etc.)")
public class RegionController {
  public static final String ENDPOINT = "/geolocation-service/api/regions";
  public static final String ACCEPT_VERSION = "Accept-Version=" + VND_SAMPLE_SERVICE_V1;

  private final RegionService regionService;
  private final RegionMapper regionMapper;

  /**
   * Create a new region.
   *
   * @param requestDTO the region creation request
   * @return the created region with 201 status
   */
  @PostMapping
  @Operation(summary = "Create a new region", description = "Creates a new region within a country")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Region created successfully",
      content = @Content(schema = @Schema(implementation = RegionResponseDTO.class))),
    @ApiResponse(responseCode = "400", description = "Invalid request data"),
    @ApiResponse(responseCode = "404", description = "Parent country not found")
  })
  public ResponseEntity<RegionResponseDTO> createRegion(
    @Valid @RequestBody RegionRequestDTO requestDTO) {

    log.info("Creating region: {} in country: {}", requestDTO.getName(), requestDTO.getCountryId());

    // Convert DTO to domain model
    Region region = this.regionMapper.requestDTOtoDomain(requestDTO);

    // Create via service
    Region created = regionService.create(region);

    // Convert to response DTO
    RegionResponseDTO responseDTO = regionMapper.domainToResponseDTO(created);

    log.info("Region created with ID: {}", created.getId());

    return ResponseEntity
      .created(URI.create("/api/v1/regions/" + created.getId()))
      .body(responseDTO);
  }

  /**
   * Get a region by ID.
   *
   * @param id the region ID
   * @return the region if found, 404 otherwise
   */
  @GetMapping("/{id}")
  @Operation(summary = "Get region by ID", description = "Retrieves a region by its unique identifier")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Region found",
      content = @Content(schema = @Schema(implementation = RegionResponseDTO.class))),
    @ApiResponse(responseCode = "404", description = "Region not found")
  })
  public ResponseEntity<RegionResponseDTO> getRegionById(
    @Parameter(description = "Region ID", required = true)
    @PathVariable UUID id) {

    log.debug("Getting region by ID: {}", id);

    return regionService.findById(id)
      .map(regionMapper::domainToResponseDTO)
      .map(ResponseEntity::ok)
      .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Get all regions.
   *
   * @return list of all regions
   */
  @GetMapping
  @Operation(
    summary = "Get all regions",
    description = "Retrieves all regions"
  )
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Regions retrieved successfully")
  })
  public ResponseEntity<List<RegionResponseDTO>> getAllRegions() {
    log.debug("Getting all regions");

    List<RegionResponseDTO> regions = regionService.findAll().stream()
      .map(regionMapper::domainToResponseDTO)
      .collect(Collectors.toList());

    return ResponseEntity.ok(regions);
  }

  /**
   * Get regions by country.
   *
   * @param countryId the country ID
   * @return list of regions in the country
   */
  @GetMapping("/country/{countryId}")
  @Operation(summary = "Get regions by country", description = "Retrieves all regions within a specific country")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Regions retrieved successfully")
  })
  public ResponseEntity<List<RegionResponseDTO>> getRegionsByCountry(
    @Parameter(description = "Country ID", required = true)
    @PathVariable UUID countryId) {

    log.debug("Getting regions by country: {}", countryId);

    List<RegionResponseDTO> regions = regionService.findByCountryId(countryId).stream()
      .map(regionMapper::domainToResponseDTO)
      .collect(Collectors.toList());

    return ResponseEntity.ok(regions);
  }

  /**
   * Get regions with pagination.
   *
   * @param pageable pagination parameters
   * @return page of regions
   */
  @GetMapping("/page")
  @Operation(summary = "Get regions with pagination", description = "Retrieves regions with pagination support")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Regions retrieved successfully")
  })
  public ResponseEntity<Page<RegionResponseDTO>> getRegionsPage(
    @PageableDefault(size = 20) Pageable pageable) {

    log.debug("Getting regions page: {}", pageable);

    Page<RegionResponseDTO> page = regionService.findByActive(true, pageable)
      .map(regionMapper::domainToResponseDTO);

    return ResponseEntity.ok(page);
  }

  /**
   * Update a region.
   *
   * @param id the region ID
   * @param requestDTO the update request
   * @return the updated region
   */
  @PutMapping("/{id}")
  @Operation(summary = "Update a region", description = "Updates an existing region")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Region updated successfully",
      content = @Content(schema = @Schema(implementation = RegionResponseDTO.class))),
    @ApiResponse(responseCode = "404", description = "Region not found"),
    @ApiResponse(responseCode = "400", description = "Invalid request data")
  })
  public ResponseEntity<RegionResponseDTO> updateRegion(
    @Parameter(description = "Region ID", required = true)
    @PathVariable UUID id,
    @Valid @RequestBody RegionRequestDTO requestDTO
  ) {

    log.info("Updating region with ID: {}", id);

    // Convert DTO to domain model
    Region region = this.regionMapper.requestDTOtoDomain(requestDTO);

    // Update via service
    Region updated = regionService.update(id, region);

    // Convert to response DTO
    RegionResponseDTO responseDTO = regionMapper.domainToResponseDTO(updated);

    log.info("Region updated with ID: {}", id);

    return ResponseEntity.ok(responseDTO);
  }

  /**
   * Delete a region.
   *
   * @param id the region ID
   * @return 204 No Content on success
   */
  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a region", description = "Deletes a region by ID")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Region deleted successfully"),
    @ApiResponse(responseCode = "404", description = "Region not found")
  })
  public ResponseEntity<Void> deleteRegion(
    @Parameter(description = "Region ID", required = true)
    @PathVariable UUID id) {

    log.info("Deleting region with ID: {}", id);

    regionService.deleteById(id);

    log.info("Region deleted with ID: {}", id);

    return ResponseEntity.noContent().build();
  }

  /**
   * Search regions by name.
   *
   * @param name the name substring to search for
   * @return list of matching regions
   */
  @GetMapping("/search")
  @Operation(summary = "Search regions by name", description = "Searches regions by name (case-insensitive partial match)")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Search completed successfully")
  })
  public ResponseEntity<List<RegionResponseDTO>> searchRegionsByName(
    @Parameter(description = "Name substring to search for", required = true)
    @RequestParam String name) {

    log.debug("Searching regions by name: {}", name);

    List<RegionResponseDTO> regions = regionService.findByNameContaining(name).stream()
      .map(regionMapper::domainToResponseDTO)
      .collect(Collectors.toList());

    return ResponseEntity.ok(regions);
  }


  /**
   * Check if a region exists by ID.
   *
   * @param id the region ID
   * @return 200 if exists, 404 if not
   */
  @GetMapping("/{id}/exists")
  @Operation(
    summary = "Check if region exists",
    description = "Checks if a region exists by ID"
  )
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Region exists"),
    @ApiResponse(responseCode = "404", description = "Region does not exist")
  })
  public ResponseEntity<Void> checkRegionExists(
    @Parameter(description = "Region ID", required = true)
    @PathVariable UUID id) {

    log.debug("Checking if region exists: {}", id);

    return regionService.existsById(id)
      ? ResponseEntity.ok().build()
      : ResponseEntity.notFound().build();
  }

  /**
   * Get total count of regions.
   *
   * @return the count
   */
  @GetMapping("/count")
  @Operation(summary = "Get region count", description = "Returns the total number of regions")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
  })
  public ResponseEntity<Long> getRegionCount() {
    log.debug("Getting region count");

    long count = regionService.count();

    return ResponseEntity.ok(count);
  }
}
