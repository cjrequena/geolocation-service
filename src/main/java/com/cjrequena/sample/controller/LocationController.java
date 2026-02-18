package com.cjrequena.sample.controller;

import com.cjrequena.sample.controller.dto.LocationRequestDTO;
import com.cjrequena.sample.controller.dto.LocationResponseDTO;
import com.cjrequena.sample.controller.exception.BadRequestException;
import com.cjrequena.sample.controller.exception.ConflictException;
import com.cjrequena.sample.controller.exception.NotFoundException;
import com.cjrequena.sample.domain.exception.LocationNotFoundException;
import com.cjrequena.sample.domain.exception.UniqueConstraintException;
import com.cjrequena.sample.domain.exception.ZoneNotFoundException;
import com.cjrequena.sample.domain.mapper.LocationMapper;
import com.cjrequena.sample.domain.model.Location;
import com.cjrequena.sample.service.LocationService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.cjrequena.sample.shared.common.util.Constant.VND_SAMPLE_SERVICE_V1;

/**
 * REST controller for Location operations.
 *
 * <p>Provides CRUD operations and spatial query endpoints for specific point locations.
 * Locations belong to zones and represent precise geographic coordinates.
 *
 * @author cjrequena
 */
@Log4j2
@RestController
@RequestMapping(value = LocationController.ENDPOINT, headers = {LocationController.ACCEPT_VERSION})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Tag(name = "Locations", description = "Location management and spatial query endpoints")
public class LocationController {
  public static final String ENDPOINT = "/geolocation-service/api/locations";
  public static final String ACCEPT_VERSION = "Accept-Version=" + VND_SAMPLE_SERVICE_V1;

  private final LocationService locationService;
  private final LocationMapper locationMapper;

  // ================================================================
  // CRUD Standard Operations
  // ================================================================

  /**
   * Create a new location.
   *
   * @param requestDTO the location creation request
   * @return the created location with 201 status
   */
  @PostMapping(
    consumes = "application/json",
    produces = "application/json"
  )
  @Operation(summary = "Create a new location", description = "Creates a new location with geographic coordinates")
  @ApiResponses(value = {
    @ApiResponse(
      responseCode = "201",
      description = "Location created successfully",
      content = @Content(schema = @Schema(implementation = LocationResponseDTO.class))
    ),
    @ApiResponse(responseCode = "400", description = "Invalid request data"),
    @ApiResponse(responseCode = "409", description = "Unique constraint violation")
  })
  public ResponseEntity<LocationResponseDTO> create(@Valid @RequestBody LocationRequestDTO requestDTO) {

    try {
      log.info("Creating location: {} at ({}, {})", requestDTO.getName(), requestDTO.getLatitude(), requestDTO.getLongitude());

      // Convert DTO to domain model
      Location location = this.locationMapper.requestDTOtoDomain(requestDTO);

      // Create via service
      Location created = locationService.create(location);

      // Convert to response DTO
      LocationResponseDTO responseDTO = locationMapper.domainToResponseDTO(created);

      log.info("Location created with ID: {}", created.getId());

      return ResponseEntity
        .created(URI.create(ENDPOINT + created.getId()))
        .header("Accept-Version", VND_SAMPLE_SERVICE_V1)
        .body(responseDTO);
    } catch (ZoneNotFoundException ex) {
      throw new BadRequestException(ex.getMessage());
    } catch (UniqueConstraintException ex) {
      throw new ConflictException(ex.getMessage());
    }
  }

  /**
   * Get a location by ID.
   *
   * @param id the location ID
   * @return the location if found, 404 otherwise
   */
  @GetMapping("/{id}")
  @Operation(
    summary = "Get location by ID",
    description = "Retrieves a location by its unique identifier"
  )
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        description = "Location found",
        content = @Content(schema = @Schema(implementation = LocationResponseDTO.class))
      ),
      @ApiResponse(responseCode = "404", description = "Location not found")
    }
  )
  public ResponseEntity<LocationResponseDTO> retrieveById(
    @Parameter(description = "Location ID", required = true)
    @PathVariable UUID id
  ) {
    try {
      log.debug("Getting location by ID: {}", id);
      Location location = locationService.findById(id);
      return ResponseEntity.ok(this.locationMapper.domainToResponseDTO(location));
    } catch (LocationNotFoundException ex) {
      throw new NotFoundException("Location with ID %s was not found".formatted(id), ex);
    }
  }

  /**
   * Get all locations with optional filtering, sorting, and pagination.
   *
   * @param filters RSQL filter expression (e.g., "active==true;postalCode==94102")
   * @param offset the offset for pagination (0-based)
   * @param limit the maximum number of results to return
   * @param sort the sort expression (e.g., "id,asc" or "name,desc;createdAt,asc")
   * @return list of locations matching the criteria
   */
  @GetMapping
  @Operation(
    summary = "Get all locations with filtering, sorting, and pagination",
    description = "Retrieves locations with optional RSQL filters, sorting, and pagination support"
  )
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Locations retrieved successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid filter or sort expression")
  })
  public ResponseEntity<List<LocationResponseDTO>> retrieve(
    @Parameter(
      name = "filters",
      description = "RSQL filter expression (e.g., 'active==true', 'postalCode==94102', 'name=like=\"Bridge\"')",
      example = "active==true;postalCode==94102"
    )
    @RequestParam(value = "filters", required = false) String filters,

    @Parameter(
      name = "offset",
      description = "Offset for pagination (0-based)",
      example = "0"
    )
    @RequestParam(value = "offset", required = false) Integer offset,

    @Parameter(
      name = "limit",
      description = "Maximum number of results to return",
      example = "20"
    )
    @RequestParam(value = "limit", required = false) Integer limit,

    @Parameter(
      name = "sort",
      description = "Sort expression (e.g., 'id,asc', 'name,desc', 'postalCode,asc;name,desc')",
      example = "name,asc"
    )
    @RequestParam(value = "sort", required = false) String sort
  ) {
    log.debug("Getting locations with filters: {}, offset: {}, limit: {}, sort: {}",
      filters, offset, limit, sort);

    try {
      // If no filters, offset, limit, or sort provided, use the cached findAll()
      if (filters == null && offset == null && limit == null && sort == null) {
        List<LocationResponseDTO> locations = locationService
          .findAll()
          .stream()
          .map(locationMapper::domainToResponseDTO)
          .collect(Collectors.toList());
        return ResponseEntity.ok(locations);
      }

      // Otherwise, use the search method with filters/sorting/pagination
      List<LocationResponseDTO> locations = locationService
        .findAll(filters, offset, limit, sort)
        .stream()
        .map(locationMapper::domainToResponseDTO)
        .collect(Collectors.toList());

      return ResponseEntity.ok(locations);
    } catch (IllegalArgumentException ex) {
      log.error("Invalid request parameters: {}", ex.getMessage());
      throw new BadRequestException();
    } catch (Exception ex) {
      log.error("Error retrieving locations: {}", ex.getMessage(), ex);
      throw new BadRequestException();
    }
  }

  /**
   * Update a location.
   *
   * @param id the location ID
   * @param requestDTO the update request
   * @return the updated location
   */
  @PutMapping("/{id}")
  @Operation(summary = "Update a location", description = "Updates an existing location")
  @ApiResponses(value = {
    @ApiResponse(
      responseCode = "200",
      description = "Location updated successfully",
      content = @Content(schema = @Schema(implementation = LocationResponseDTO.class))
    ),
    @ApiResponse(responseCode = "404", description = "Location not found"),
    @ApiResponse(responseCode = "400", description = "Invalid request data"),
    @ApiResponse(responseCode = "409", description = "Unique constraint violation")
  })
  public ResponseEntity<LocationResponseDTO> update(
    @Parameter(description = "Location ID", required = true)
    @PathVariable UUID id,
    @Valid @RequestBody LocationRequestDTO requestDTO
  ) {

    try {
      log.info("Updating location with ID: {}", id);

      // Convert DTO to domain model
      Location location = this.locationMapper.requestDTOtoDomain(requestDTO);

      // Update via service
      Location updated = null;
      try {
        updated = locationService.update(id, location);
      } catch (LocationNotFoundException ex) {
        throw new NotFoundException("Location with ID %s was not found".formatted(id), ex);
      }

      // Convert to response DTO
      LocationResponseDTO responseDTO = locationMapper.domainToResponseDTO(updated);

      log.info("Location updated with ID: {}", id);

      return ResponseEntity.ok(responseDTO);
    } catch (LocationNotFoundException ex) {
      throw new NotFoundException("Location with ID %s was not found".formatted(id), ex);
    } catch (ZoneNotFoundException ex) {
      throw new BadRequestException("Zone with ID %s was not found".formatted(requestDTO.getZoneId()), ex);
    } catch (UniqueConstraintException ex) {
      throw new ConflictException(ex.getMessage());
    }
  }

  /**
   * Delete a location.
   *
   * @param id the location ID
   * @return 204 No Content on success
   */
  @DeleteMapping("/{id}")
  @Operation(
    summary = "Delete a location",
    description = "Deletes a location by ID"
  )
  @ApiResponses(
    value = {
      @ApiResponse(responseCode = "204", description = "Location deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Location not found")
    }
  )
  public ResponseEntity<Void> delete(
    @Parameter(description = "Location ID", required = true)
    @PathVariable UUID id
  ) {

    try {
      log.info("Deleting location with ID: {}", id);

      locationService.deleteById(id);

      log.info("Location deleted with ID: {}", id);

      return ResponseEntity.noContent().build();
    } catch (LocationNotFoundException ex) {
      throw new NotFoundException("Location with ID %s was not found".formatted(id), ex);
    }
  }

  /**
   * Check if a location exists by ID.
   *
   * @param id the location ID
   * @return 200 if exists, 404 if not
   */
  @GetMapping("/{id}/exists")
  @Operation(summary = "Check if location exists", description = "Checks if a location exists by ID")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Location exists"),
    @ApiResponse(responseCode = "404", description = "Location does not exist")
  })
  public ResponseEntity<Void> checkExists(
    @Parameter(description = "Location ID", required = true)
    @PathVariable UUID id
  ) {

    log.debug("Checking if location exists: {}", id);

    return locationService
      .existsById(id)
      ? ResponseEntity.ok().build()
      : ResponseEntity.notFound().build();
  }

  /**
   * Get total count of locations.
   *
   * @return the count
   */
  @GetMapping("/count")
  @Operation(summary = "Get location count", description = "Returns the total number of locations")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
  })
  public ResponseEntity<Long> count() {
    log.debug("Getting location count");

    long count = locationService.count();

    return ResponseEntity.ok(count);
  }

  // ================================================================
  // Spatial Operations — Proximity
  // ================================================================

  /**
   * Find locations near a point.
   *
   * @param latitude the latitude
   * @param longitude the longitude
   * @param radiusMeters the search radius in meters
   * @return list of nearby locations
   */
  @GetMapping("/near")
  @Operation(summary = "Find locations near a point",
    description = "Finds all locations within specified radius from a point")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Query completed successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid coordinates or radius")
  })
  public ResponseEntity<List<LocationResponseDTO>> searchLocationsNear(
    @Parameter(description = "Latitude", required = true)
    @RequestParam double latitude,
    @Parameter(description = "Longitude", required = true)
    @RequestParam double longitude,
    @Parameter(description = "Search radius in meters", required = true)
    @RequestParam double radiusMeters
  ) {

    log.debug("Finding locations near ({}, {}) within {} meters", latitude, longitude, radiusMeters);

    try {
      String wkt = String.format("POINT(%f %f)", longitude, latitude);

      List<LocationResponseDTO> locations = locationService
        .findWithinRadius(wkt, radiusMeters)
        .stream()
        .map(locationMapper::domainToResponseDTO)
        .collect(Collectors.toList());

      return ResponseEntity.ok(locations);
    } catch (Exception e) {
      log.error("Error in spatial query: {}", e.getMessage(), e);
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Find locations within a geometry.
   *
   * @param wkt the geometry in WKT format
   * @return list of locations within the geometry
   */
  @GetMapping("/within")
  @Operation(summary = "Find locations within geometry",
    description = "Finds all locations within the specified geometry (polygon, circle, etc.)")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Query completed successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid WKT format")
  })
  public ResponseEntity<List<LocationResponseDTO>> searchLocationsWithin(
    @Parameter(description = "Geometry in WKT format", required = true)
    @RequestParam String wkt
  ) {

    log.debug("Finding locations within: {}", wkt);

    try {
      List<LocationResponseDTO> locations = locationService
        .findWithinPolygon(wkt)
        .stream()
        .map(locationMapper::domainToResponseDTO)
        .collect(Collectors.toList());

      return ResponseEntity.ok(locations);
    } catch (Exception e) {
      log.error("Invalid WKT format: {}", wkt, e);
      return ResponseEntity.badRequest().build();
    }
  }

}
