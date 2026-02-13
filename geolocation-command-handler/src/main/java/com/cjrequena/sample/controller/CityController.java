package com.cjrequena.sample.controller;

import com.cjrequena.sample.controller.dto.CityRequestDTO;
import com.cjrequena.sample.controller.dto.CityResponseDTO;
import com.cjrequena.sample.domain.mapper.CityMapper;
import com.cjrequena.sample.domain.model.City;
import com.cjrequena.sample.service.CityService;
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
 * REST controller for City operations.
 *
 * <p>Provides CRUD operations and query endpoints for cities.
 * Cities belong to regions and can contain areas.
 *
 * @author cjrequena
 */
@Log4j2
@RestController
@RequestMapping(value = CityController.ENDPOINT, headers = {CityController.ACCEPT_VERSION})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Tag(name = "Cities", description = "City management endpoints")
public class CityController {

  public static final String ENDPOINT = "/geolocation-service/api/cities";
  public static final String ACCEPT_VERSION = "Accept-Version=" + VND_SAMPLE_SERVICE_V1;

  private final CityService cityService;
  private final CityMapper cityMapper;

  /**
   * Create a new city.
   *
   * @param requestDTO the city creation request
   * @return the created city with 201 status
   */
  @PostMapping
  @Operation(summary = "Create a new city", description = "Creates a new city within a region")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "City created successfully",
      content = @Content(schema = @Schema(implementation = CityResponseDTO.class))),
    @ApiResponse(responseCode = "400", description = "Invalid request data"),
    @ApiResponse(responseCode = "404", description = "Parent region not found")
  })
  public ResponseEntity<CityResponseDTO> createCity(@Valid @RequestBody CityRequestDTO requestDTO) {

    log.info("Creating city: {} in region: {}", requestDTO.getName(), requestDTO.getRegionId());

    // Convert DTO to domain model
    City city = this.cityMapper.requestDTOtoDomain(requestDTO);

    // Create via service
    City created = cityService.create(city);

    // Convert to response DTO
    CityResponseDTO responseDTO = cityMapper.domainToResponseDTO(created);

    log.info("City created with ID: {}", created.getId());

    return ResponseEntity
      .created(URI.create("/api/v1/cities/" + created.getId()))
      .body(responseDTO);
  }

  /**
   * Get a city by ID.
   *
   * @param id the city ID
   * @return the city if found, 404 otherwise
   */
  @GetMapping("/{id}")
  @Operation(summary = "Get city by ID", description = "Retrieves a city by its unique identifier")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "City found",
      content = @Content(schema = @Schema(implementation = CityResponseDTO.class))),
    @ApiResponse(responseCode = "404", description = "City not found")
  })
  public ResponseEntity<CityResponseDTO> getCityById(
    @Parameter(description = "City ID", required = true)
    @PathVariable UUID id) {

    log.debug("Getting city by ID: {}", id);

    return cityService.findById(id)
      .map(cityMapper::domainToResponseDTO)
      .map(ResponseEntity::ok)
      .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Get all cities.
   *
   * @return list of all cities
   */
  @GetMapping
  @Operation(summary = "Get all cities", description = "Retrieves all cities")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Cities retrieved successfully")
  })
  public ResponseEntity<List<CityResponseDTO>> getAllCities() {
    log.debug("Getting all cities");

    List<CityResponseDTO> cities = cityService.findAll().stream()
      .map(cityMapper::domainToResponseDTO)
      .collect(Collectors.toList());

    return ResponseEntity.ok(cities);
  }

  /**
   * Get cities by region.
   *
   * @param regionId the region ID
   * @return list of cities in the region
   */
  @GetMapping("/region/{regionId}")
  @Operation(summary = "Get cities by region", description = "Retrieves all cities within a specific region")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Cities retrieved successfully")
  })
  public ResponseEntity<List<CityResponseDTO>> getCitiesByRegion(
    @Parameter(description = "Region ID", required = true)
    @PathVariable UUID regionId) {

    log.debug("Getting cities by region: {}", regionId);

    List<CityResponseDTO> cities = cityService.findByRegionId(regionId).stream()
      .map(cityMapper::domainToResponseDTO)
      .collect(Collectors.toList());

    return ResponseEntity.ok(cities);
  }

  /**
   * Get cities with pagination.
   *
   * @param pageable pagination parameters
   * @return page of cities
   */
  @GetMapping("/page")
  @Operation(summary = "Get cities with pagination", description = "Retrieves cities with pagination support")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Cities retrieved successfully")
  })
  public ResponseEntity<Page<CityResponseDTO>> getCitiesPage(
    @PageableDefault(size = 20) Pageable pageable) {

    log.debug("Getting cities page: {}", pageable);

    Page<CityResponseDTO> page = cityService.findByActive(true, pageable)
      .map(cityMapper::domainToResponseDTO);

    return ResponseEntity.ok(page);
  }

  /**
   * Update a city.
   *
   * @param id the city ID
   * @param requestDTO the update request
   * @return the updated city
   */
  @PutMapping("/{id}")
  @Operation(summary = "Update a city", description = "Updates an existing city")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "City updated successfully",
      content = @Content(schema = @Schema(implementation = CityResponseDTO.class))),
    @ApiResponse(responseCode = "404", description = "City not found"),
    @ApiResponse(responseCode = "400", description = "Invalid request data")
  })
  public ResponseEntity<CityResponseDTO> updateCity(
    @Parameter(description = "City ID", required = true)
    @PathVariable UUID id,
    @Valid @RequestBody CityRequestDTO requestDTO) {

    log.info("Updating city with ID: {}", id);

    // Convert DTO to domain model
    City city = this.cityMapper.requestDTOtoDomain(requestDTO);
    // Update via service
    City updated = cityService.update(id, city);

    // Convert to response DTO
    CityResponseDTO responseDTO = cityMapper.domainToResponseDTO(updated);

    log.info("City updated with ID: {}", id);

    return ResponseEntity.ok(responseDTO);
  }

  /**
   * Delete a city.
   *
   * @param id the city ID
   * @return 204 No Content on success
   */
  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a city", description = "Deletes a city by ID")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "City deleted successfully"),
    @ApiResponse(responseCode = "404", description = "City not found")
  })
  public ResponseEntity<Void> deleteCity(
    @Parameter(description = "City ID", required = true)
    @PathVariable UUID id) {

    log.info("Deleting city with ID: {}", id);

    cityService.deleteById(id);

    log.info("City deleted with ID: {}", id);

    return ResponseEntity.noContent().build();
  }

  /**
   * Search cities by name.
   *
   * @param name the name substring to search for
   * @return list of matching cities
   */
  @GetMapping("/search")
  @Operation(summary = "Search cities by name", description = "Searches cities by name (case-insensitive partial match)")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Search completed successfully")
  })
  public ResponseEntity<List<CityResponseDTO>> searchCitiesByName(
    @Parameter(description = "Name substring to search for", required = true)
    @RequestParam String name) {

    log.debug("Searching cities by name: {}", name);

    List<CityResponseDTO> cities = cityService.findByNameContaining(name).stream()
      .map(cityMapper::domainToResponseDTO)
      .collect(Collectors.toList());

    return ResponseEntity.ok(cities);
  }

//  /**
//   * Get cities by postal code.
//   *
//   * @param postalCode the postal code
//   * @return list of cities with the postal code
//   */
//  @GetMapping("/postal-code/{postalCode}")
//  @Operation(summary = "Get cities by postal code", description = "Retrieves cities by postal code")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Cities retrieved successfully")
//  })
//  public ResponseEntity<List<CityResponseDTO>> getCitiesByPostalCode(
//    @Parameter(description = "Postal code", required = true)
//    @PathVariable String postalCode) {
//
//    log.debug("Getting cities by postal code: {}", postalCode);
//
//    List<CityResponseDTO> cities = cityService.findByPostalCode(postalCode).stream()
//      .map(cityMapper::domainToResponseDTO)
//      .collect(Collectors.toList());
//
//    return ResponseEntity.ok(cities);
//  }
//
//  /**
//   * Get capital cities.
//   *
//   * @return list of capital cities
//   */
//  @GetMapping("/capitals")
//  @Operation(summary = "Get capital cities", description = "Retrieves all cities marked as capitals")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Capital cities retrieved successfully")
//  })
//  public ResponseEntity<List<CityResponseDTO>> getCapitalCities() {
//    log.debug("Getting capital cities");
//
//    List<CityResponseDTO> cities = cityService.findCapitalCities().stream()
//      .map(cityMapper::domainToResponseDTO)
//      .collect(Collectors.toList());
//
//    return ResponseEntity.ok(cities);
//  }

  /**
   * Check if a city exists by ID.
   *
   * @param id the city ID
   * @return 200 if exists, 404 if not
   */
  @GetMapping("/{id}/exists")
  @Operation(summary = "Check if city exists", description = "Checks if a city exists by ID")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "City exists"),
    @ApiResponse(responseCode = "404", description = "City does not exist")
  })
  public ResponseEntity<Void> checkCityExists(
    @Parameter(description = "City ID", required = true)
    @PathVariable UUID id) {

    log.debug("Checking if city exists: {}", id);

    return cityService.existsById(id)
      ? ResponseEntity.ok().build()
      : ResponseEntity.notFound().build();
  }

  /**
   * Get total count of cities.
   *
   * @return the count
   */
  @GetMapping("/count")
  @Operation(summary = "Get city count", description = "Returns the total number of cities")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
  })
  public ResponseEntity<Long> getCityCount() {
    log.debug("Getting city count");

    long count = cityService.count();

    return ResponseEntity.ok(count);
  }
}
