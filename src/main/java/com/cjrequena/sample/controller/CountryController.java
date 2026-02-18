package com.cjrequena.sample.controller;

import com.cjrequena.sample.controller.dto.CountryRequestDTO;
import com.cjrequena.sample.controller.dto.CountryResponseDTO;
import com.cjrequena.sample.controller.exception.ConflictException;
import com.cjrequena.sample.controller.exception.NotFoundException;
import com.cjrequena.sample.domain.exception.CountryNotFoundException;
import com.cjrequena.sample.domain.exception.UniqueConstraintException;
import com.cjrequena.sample.domain.mapper.CountryMapper;
import com.cjrequena.sample.domain.model.Country;
import com.cjrequena.sample.service.CountryService;
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
 * REST controller for Country operations.
 *
 * <p>Provides CRUD operations and query endpoints for countries.
 * Uses CountryService for business logic and CountryMapper for DTO conversions.
 *
 * @author cjrequena
 */
@Log4j2
@RestController
@RequestMapping(value = CountryController.ENDPOINT, headers = {CountryController.ACCEPT_VERSION})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Tag(name = "Countries", description = "Country management endpoints")
public class CountryController {
  public static final String ENDPOINT = "/geolocation-service/api/countries";
  public static final String ACCEPT_VERSION = "Accept-Version=" + VND_SAMPLE_SERVICE_V1;

  private final CountryService countryService;
  private final CountryMapper countryMapper;

  // ================================================================
  // CRUD Standard Operations
  // ================================================================

  /**
   * Create a new country.
   *
   * @param requestDTO the country creation request
   * @return the created country with 201 status
   */
  @PostMapping
  @Operation(
    summary = "Create a new country",
    description = "Creates a new country with the provided details"
  )
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "201",
        description = "Country created successfully",
        content = @Content(schema = @Schema(implementation = CountryResponseDTO.class))
      ),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "409", description = "Unique constraint violation")
    })
  public ResponseEntity<CountryResponseDTO> create(@Valid @RequestBody CountryRequestDTO requestDTO) {

    try {
      log.info("Creating country: {}", requestDTO.getName());

      // Convert DTO to domain model
      Country country = this.countryMapper.requestDTOtoDomain(requestDTO);

      // Create via service
      Country created = countryService.create(country);

      // Convert to response DTO
      CountryResponseDTO responseDTO = countryMapper.domainToResponseDTO(created);

      log.info("Country created with ID: {}", created.getId());

      return ResponseEntity
        .created(URI.create(ENDPOINT + created.getId()))
        .header("Accept-Version", VND_SAMPLE_SERVICE_V1)
        .body(responseDTO);
    } catch (UniqueConstraintException ex) {
      throw new ConflictException(ex.getMessage());
    }
  }

  /**
   * Get a country by ID.
   *
   * @param id the country ID
   * @return the country if found, 404 otherwise
   */
  @GetMapping("/{id}")
  @Operation(
    summary = "Get country by ID",
    description = "Retrieves a country by its unique identifier"
  )
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        description = "Country found",
        content = @Content(schema = @Schema(implementation = CountryResponseDTO.class))
      ),
      @ApiResponse(responseCode = "404", description = "Country not found")
    }
  )
  public ResponseEntity<CountryResponseDTO> retrieveById(
    @Parameter(description = "Country ID", required = true)
    @PathVariable UUID id) {
    try {
      log.debug("Getting country by ID: {}", id);
      Country country = countryService.findById(id);
      return ResponseEntity.ok(this.countryMapper.domainToResponseDTO(country));
    } catch (CountryNotFoundException ex) {
      throw new NotFoundException("Country with ID %s was not found".formatted(id), ex);
    }
  }

  /**
   * Get all countries with optional filtering, sorting, and pagination.
   *
   * @param filters RSQL filter expression (e.g., "active==true;postalCode==94102")
   * @param offset the offset for pagination (0-based)
   * @param limit the maximum number of results to return
   * @param sort the sort expression (e.g., "id,asc" or "name,desc;createdAt,asc")
   * @return list of countries matching the criteria
   */
  @GetMapping
  @Operation(
    summary = "Get all countries with filtering, sorting, and pagination",
    description = "Retrieves countries with optional RSQL filters, sorting, and pagination support"
  )
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Countries retrieved successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid filter or sort expression")
  })
  public ResponseEntity<List<CountryResponseDTO>> retrieve(
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
    log.debug("Getting countries with filters: {}, offset: {}, limit: {}, sort: {}",
      filters, offset, limit, sort);

    try {
      // If no filters, offset, limit, or sort provided, use the cached findAll()
      if (filters == null && offset == null && limit == null && sort == null) {
        List<CountryResponseDTO> countries = countryService
          .findAll()
          .stream()
          .map(countryMapper::domainToResponseDTO)
          .collect(Collectors.toList());
        return ResponseEntity.ok(countries);
      }

      // Otherwise, use the search method with filters/sorting/pagination
      List<CountryResponseDTO> countries = countryService
        .findAll(filters, offset, limit, sort)
        .stream()
        .map(countryMapper::domainToResponseDTO)
        .collect(Collectors.toList());

      return ResponseEntity.ok(countries);
    } catch (IllegalArgumentException e) {
      log.error("Invalid request parameters: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Error retrieving countries: {}", e.getMessage(), e);
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Update a country.
   *
   * @param id the country ID
   * @param requestDTO the update request
   * @return the updated country
   */
  @PutMapping("/{id}")
  @Operation(
    summary = "Update a country",
    description = "Updates an existing country"
  )
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        description = "Country updated successfully",
        content = @Content(schema = @Schema(implementation = CountryResponseDTO.class))
      ),
      @ApiResponse(responseCode = "404", description = "Country not found"),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "409", description = "Unique constraint violation")
    }
  )
  public ResponseEntity<CountryResponseDTO> update(
    @Parameter(description = "Country ID", required = true)
    @PathVariable UUID id,
    @Valid @RequestBody CountryRequestDTO requestDTO) {

    log.info("Updating country with ID: {}", id);

    try {
      // Convert DTO to domain model
      Country country = this.countryMapper.requestDTOtoDomain(requestDTO);

      // Update via service
      Country updated = countryService.update(id, country);

      // Convert to response DTO
      CountryResponseDTO responseDTO = countryMapper.domainToResponseDTO(updated);

      log.info("Country updated with ID: {}", id);

      return ResponseEntity.ok(responseDTO);
    } catch (CountryNotFoundException ex) {
      throw new NotFoundException("Country with ID %s was not found".formatted(id), ex);
    } catch (UniqueConstraintException ex) {
      throw new ConflictException(ex.getMessage());
    }
  }

  /**
   * Delete a country.
   *
   * @param id the country ID
   * @return 204 No Content on success
   */
  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a country", description = "Deletes a country by ID")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Country deleted successfully"),
    @ApiResponse(responseCode = "404", description = "Country not found")
  })
  public ResponseEntity<Void> delete(
    @Parameter(description = "Country ID", required = true)
    @PathVariable UUID id) {
    try {
      log.info("Deleting country with ID: {}", id);
      countryService.deleteById(id);
      log.info("Country deleted with ID: {}", id);
      return ResponseEntity.noContent().build();
    } catch (CountryNotFoundException ex) {
      throw new NotFoundException("Country with ID %s was not found".formatted(id), ex);
    }
  }

  /**
   * Check if a country exists by ID.
   *
   * @param id the country ID
   * @return 200 if exists, 404 if not
   */
  @GetMapping("/{id}/exists")
  @Operation(summary = "Check if country exists", description = "Checks if a country exists by ID")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Country exists"),
    @ApiResponse(responseCode = "404", description = "Country does not exist")
  })
  public ResponseEntity<Void> checkExists(
    @Parameter(description = "Country ID", required = true)
    @PathVariable UUID id) {

    log.debug("Checking if country exists: {}", id);

    return countryService
      .existsById(id)
      ? ResponseEntity.ok().build()
      : ResponseEntity.notFound().build();
  }

  /**
   * Get total count of countries.
   *
   * @return the count
   */
  @GetMapping("/count")
  @Operation(summary = "Get country count", description = "Returns the total number of countries")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
  })
  public ResponseEntity<Long> count() {
    log.debug("Getting country count");

    long count = countryService.count();

    return ResponseEntity.ok(count);
  }
}
