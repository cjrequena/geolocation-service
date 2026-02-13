package com.cjrequena.sample.controller;

import com.cjrequena.sample.controller.dto.CreateLocationRequestDTO;
import com.cjrequena.sample.controller.dto.LocationResponseDTO;
import com.cjrequena.sample.controller.dto.UpdateLocationRequestDTO;
import com.cjrequena.sample.domain.mapper.LocationMapper;
import com.cjrequena.sample.domain.model.Location;
import com.cjrequena.sample.domain.model.enums.LocationType;
import com.cjrequena.sample.domain.model.vo.AltitudeVO;
import com.cjrequena.sample.domain.model.vo.GpsAccuracyVO;
import com.cjrequena.sample.domain.model.vo.MetadataVO;
import com.cjrequena.sample.domain.model.vo.PointVO;
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
    @ApiResponse(responseCode = "404", description = "Parent zone not found")
  })
  public ResponseEntity<LocationResponseDTO> createLocation(@Valid @RequestBody CreateLocationRequestDTO requestDTO) {

    log.info("Creating location: {} at ({}, {})",
      requestDTO.getName(), requestDTO.getLatitude(), requestDTO.getLongitude());

    // Convert DTO to domain model
    Location location = this.locationMapper.requestDTOtoDomain(requestDTO);

    // Create via service
    Location created = locationService.create(location);

    // Convert to response DTO
    LocationResponseDTO responseDTO = locationMapper.domainToResponseDTO(created);

    log.info("Location created with ID: {}", created.getId());

    return ResponseEntity
      .created(URI.create("/api/v1/locations/" + created.getId()))
      .body(responseDTO);
  }

  /**
   * Get a location by ID.
   *
   * @param id the location ID
   * @return the location if found, 404 otherwise
   */
  @GetMapping("/{id}")
  @Operation(summary = "Get location by ID", description = "Retrieves a location by its unique identifier")
  @ApiResponses(value = {
    @ApiResponse(
      responseCode = "200",
      description = "Location found",
      content = @Content(schema = @Schema(implementation = LocationResponseDTO.class))),
    @ApiResponse(responseCode = "404", description = "Location not found")
  })
  public ResponseEntity<LocationResponseDTO> getLocationById(
    @Parameter(description = "Location ID", required = true)
    @PathVariable UUID id
  ) {

    log.debug("Getting location by ID: {}", id);

    return locationService.findById(id)
      .map(locationMapper::domainToResponseDTO)
      .map(ResponseEntity::ok)
      .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Get all locations.
   *
   * @return list of all locations
   */
  @GetMapping
  @Operation(summary = "Get all locations", description = "Retrieves all locations")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Locations retrieved successfully")
  })
  public ResponseEntity<List<LocationResponseDTO>> getAllLocations() {
    log.debug("Getting all locations");

    List<LocationResponseDTO> locations = locationService
      .findAll()
      .stream()
      .map(locationMapper::domainToResponseDTO)
      .collect(Collectors.toList());

    return ResponseEntity.ok(locations);
  }

  /**
   * Get locations by zone.
   *
   * @param zoneId the zone ID
   * @return list of locations in the zone
   */
  @GetMapping("/zone/{zoneId}")
  @Operation(summary = "Get locations by zone", description = "Retrieves all locations within a specific zone")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Locations retrieved successfully")
  })
  public ResponseEntity<List<LocationResponseDTO>> getLocationsByZone(
    @Parameter(description = "Zone ID", required = true)
    @PathVariable UUID zoneId
  ) {

    log.debug("Getting locations by zone: {}", zoneId);

    List<LocationResponseDTO> locations = locationService.findByZoneId(zoneId).stream()
      .map(locationMapper::domainToResponseDTO)
      .collect(Collectors.toList());

    return ResponseEntity.ok(locations);
  }

  /**
   * Get locations with pagination.
   *
   * @param pageable pagination parameters
   * @return page of locations
   */
  @GetMapping("/page")
  @Operation(summary = "Get locations with pagination", description = "Retrieves locations with pagination support")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Locations retrieved successfully")
  })
  public ResponseEntity<Page<LocationResponseDTO>> getLocationsPage(@PageableDefault(size = 20) Pageable pageable) {

    log.debug("Getting locations page: {}", pageable);

    Page<LocationResponseDTO> page = locationService.findByActive(true, pageable)
      .map(locationMapper::domainToResponseDTO);

    return ResponseEntity.ok(page);
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
    @ApiResponse(responseCode = "400", description = "Invalid request data")
  })
  public ResponseEntity<LocationResponseDTO> updateLocation(
    @Parameter(description = "Location ID", required = true)
    @PathVariable UUID id,
    @Valid @RequestBody UpdateLocationRequestDTO requestDTO
  ) {

    log.info("Updating location with ID: {}", id);

    // Convert DTO to domain model
    Location location = Location.create(
      id,
      requestDTO.getZoneId() != null ? UUID.fromString(requestDTO.getZoneId()) : null,
      requestDTO.getName(),
      requestDTO.getLocationType() != null ? requestDTO.getLocationType() : LocationType.GENERIC,
      PointVO.of(requestDTO.getLatitude(), requestDTO.getLongitude()),
      requestDTO.getAltitudeMeters() != null ? AltitudeVO.of(requestDTO.getAltitudeMeters()) : null,
      requestDTO.getAccuracyMeters() != null ? GpsAccuracyVO.of(requestDTO.getAccuracyMeters()) : null,
      requestDTO.getAddress(),
      requestDTO.getPostalCode(),
      requestDTO.getActive() != null ? requestDTO.getActive() : Boolean.TRUE,
      requestDTO.getMetadata() != null ? MetadataVO.of(requestDTO.getMetadata()) : MetadataVO.empty()
    );

    // Update via service
    Location updated = locationService.update(id, location);

    // Convert to response DTO
    LocationResponseDTO responseDTO = locationMapper.domainToResponseDTO(updated);

    log.info("Location updated with ID: {}", id);

    return ResponseEntity.ok(responseDTO);
  }

  /**
   * Delete a location.
   *
   * @param id the location ID
   * @return 204 No Content on success
   */
  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a location", description = "Deletes a location by ID")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Location deleted successfully"),
    @ApiResponse(responseCode = "404", description = "Location not found")
  })
  public ResponseEntity<Void> deleteLocation(
    @Parameter(description = "Location ID", required = true)
    @PathVariable UUID id
  ) {

    log.info("Deleting location with ID: {}", id);

    locationService.deleteById(id);

    log.info("Location deleted with ID: {}", id);

    return ResponseEntity.noContent().build();
  }

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
  public ResponseEntity<List<LocationResponseDTO>> findLocationsNear(
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
  public ResponseEntity<List<LocationResponseDTO>> findLocationsWithin(
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

  /**
   * Get locations by postal code.
   *
   * @param postalCode the postal code
   * @return list of locations with the postal code
   */
  @GetMapping("/postal-code/{postalCode}")
  @Operation(summary = "Get locations by postal code", description = "Retrieves locations by postal code")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Locations retrieved successfully")
  })
  public ResponseEntity<List<LocationResponseDTO>> getLocationsByPostalCode(
    @Parameter(description = "Postal code", required = true)
    @PathVariable String postalCode) {

    log.debug("Getting locations by postal code: {}", postalCode);

    List<LocationResponseDTO> locations = locationService
      .findByPostalCode(postalCode)
      .stream()
      .map(locationMapper::domainToResponseDTO)
      .collect(Collectors.toList());

    return ResponseEntity.ok(locations);
  }

  /**
   * Search locations by address.
   *
   * @param address the address substring to search for
   * @return list of matching locations
   */
  @GetMapping("/search")
  @Operation(summary = "Search locations by address",
    description = "Searches locations by address (case-insensitive partial match)")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Search completed successfully")
  })
  public ResponseEntity<List<LocationResponseDTO>> searchLocationsByAddress(
    @Parameter(description = "Address substring to search for", required = true)
    @RequestParam String address) {

    log.debug("Searching locations by address: {}", address);

    List<LocationResponseDTO> locations = locationService
      .findByAddressContaining(address)
      .stream()
      .map(locationMapper::domainToResponseDTO)
      .collect(Collectors.toList());

    return ResponseEntity.ok(locations);
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
  public ResponseEntity<Void> checkLocationExists(
    @Parameter(description = "Location ID", required = true)
    @PathVariable UUID id
  ) {

    log.debug("Checking if location exists: {}", id);

    return locationService.existsById(id)
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
  public ResponseEntity<Long> getLocationCount() {
    log.debug("Getting location count");

    long count = locationService.count();

    return ResponseEntity.ok(count);
  }
}
