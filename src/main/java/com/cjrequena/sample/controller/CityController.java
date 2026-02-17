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
  @Operation(
    summary = "Create a new city",
    description = "Creates a new city within a region"
  )
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "City created successfully",
      content = @Content(schema = @Schema(implementation = CityResponseDTO.class))),
    @ApiResponse(responseCode = "400", description = "Invalid request data"),
    @ApiResponse(responseCode = "404", description = "Parent region not found")
  })
  public ResponseEntity<CityResponseDTO> create(@Valid @RequestBody CityRequestDTO requestDTO) {

    log.info("Creating city: {} in region: {}", requestDTO.getName(), requestDTO.getRegionId());

    // Convert DTO to domain model
    City city = this.cityMapper.requestDTOtoDomain(requestDTO);

    // Create via service
    City created = cityService.create(city);

    // Convert to response DTO
    CityResponseDTO responseDTO = cityMapper.domainToResponseDTO(created);

    log.info("City created with ID: {}", created.getId());

    return ResponseEntity
      .created(URI.create(ENDPOINT + created.getId()))
      .header("Accept-Version", VND_SAMPLE_SERVICE_V1)
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
  public ResponseEntity<CityResponseDTO> retrieveById(
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
  public ResponseEntity<List<CityResponseDTO>> retrieve() {
    log.debug("Getting all cities");

    List<CityResponseDTO> cities = cityService.findAll().stream()
      .map(cityMapper::domainToResponseDTO)
      .collect(Collectors.toList());

    return ResponseEntity.ok(cities);
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
  public ResponseEntity<CityResponseDTO> update(
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
  public ResponseEntity<Void> checkExists(
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
  public ResponseEntity<Long> count() {
    log.debug("Getting city count");

    long count = cityService.count();

    return ResponseEntity.ok(count);
  }
}
