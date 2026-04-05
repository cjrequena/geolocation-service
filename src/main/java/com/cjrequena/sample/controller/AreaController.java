package com.cjrequena.sample.controller;

import com.cjrequena.sample.controller.dto.AreaRequestDTO;
import com.cjrequena.sample.controller.dto.AreaResponseDTO;
import com.cjrequena.sample.controller.exception.BadRequestException;
import com.cjrequena.sample.controller.exception.ConflictException;
import com.cjrequena.sample.controller.exception.NotFoundException;
import com.cjrequena.sample.domain.exception.*;
import com.cjrequena.sample.domain.mapper.AreaMapper;
import com.cjrequena.sample.domain.model.Area;
import com.cjrequena.sample.service.AreaService;
import io.github.perplexhub.rsql.UnknownPropertyException;
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
 * REST controller for Area operations.
 *
 * <p>Provides CRUD operations and query endpoints for areas (districts, boroughs, neighborhoods).
 * Areas belong to cities and can contain zones.
 *
 * @author cjrequena
 */
@Log4j2
@RestController
@RequestMapping(value = AreaController.ENDPOINT, headers = {AreaController.ACCEPT_VERSION})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Tag(name = "Areas", description = "Area management endpoints (districts, boroughs, neighborhoods)")
public class AreaController {

  public static final String ENDPOINT = "/geolocation-service/api/areas";
  public static final String ACCEPT_VERSION = "Accept-Version=" + VND_SAMPLE_SERVICE_V1;

  private final AreaService areaService;
  private final AreaMapper areaMapper;

  // ================================================================
  // CRUD Standard Operations
  // ================================================================

  /**
   * Create a new area.
   *
   * @param requestDTO the area creation request
   * @return the created area with 201 status
   */
  @PostMapping
  @Operation(
    summary = "Create a new area",
    description = "Creates a new area within a city"
  )
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "201", description = "Area created successfully",
        content = @Content(schema = @Schema(implementation = AreaResponseDTO.class))
      ),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "409", description = "Unique constraint violation")
    }
  )
  public ResponseEntity<AreaResponseDTO> create(@Valid @RequestBody AreaRequestDTO requestDTO) {
    try {
      log.info("Creating area: {} in city: {}", requestDTO.getName(), requestDTO.getCityId());

      // Convert DTO to domain model
      Area area = this.areaMapper.requestDTOtoDomain(requestDTO);

      // Create via service
      Area created = areaService.create(area);

      // Convert to response DTO
      AreaResponseDTO responseDTO = areaMapper.domainToResponseDTO(created);

      log.info("Area created with ID: {}", created.getId());

      return ResponseEntity
        .created(URI.create(ENDPOINT + created.getId()))
        .header("Accept-Version", ACCEPT_VERSION)
        .body(responseDTO);
    } catch (CityNotFoundException ex) {
      throw new BadRequestException("City with ID %s was not found".formatted(requestDTO.getCityId()), ex);
    } catch (GeoShapeNotFoundException ex) {
      throw new BadRequestException("GeoShape with ID %s was not found".formatted(requestDTO.getGeoShapeId()), ex);
    } catch (CityRequiredException ex) {
      throw new BadRequestException(ex.getMessage());
    } catch (UniqueConstraintException ex) {
      throw new ConflictException(ex.getMessage());
    }
  }

  /**
   * Get an area by ID.
   *
   * @param id the area ID
   * @return the area if found, 404 otherwise
   */
  @GetMapping("/{id}")
  @Operation(summary = "Get area by ID", description = "Retrieves an area by its unique identifier")
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        description = "Area found",
        content = @Content(schema = @Schema(implementation = AreaResponseDTO.class))
      ),
      @ApiResponse(responseCode = "404", description = "Area not found")
    }
  )
  public ResponseEntity<AreaResponseDTO> retrieveById(
    @Parameter(description = "Area ID", required = true)
    @PathVariable UUID id) {
    try {
      log.debug("Getting area by ID: {}", id);
      Area area = areaService.findById(id);
      return ResponseEntity.ok(this.areaMapper.domainToResponseDTO(area));
    } catch (AreaNotFoundException ex) {
      throw new NotFoundException("Area with ID %s was not found".formatted(id), ex);
    }
  }

  /**
   * Get all areas with optional filtering, sorting, and pagination.
   *
   * @param filters RSQL filter expression (e.g., "active==true;postalCode==94102")
   * @param offset the offset for pagination (0-based)
   * @param limit the maximum number of results to return
   * @param sort the sort expression (e.g., "id,asc" or "name,desc;createdAt,asc")
   * @return list of areas matching the criteria
   */
  @GetMapping
  @Operation(
    summary = "Get all areas with filtering, sorting, and pagination",
    description = "Retrieves areas with optional RSQL filters, sorting, and pagination support"
  )
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Areas retrieved successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid filter or sort expression")
  })
  public ResponseEntity<List<AreaResponseDTO>> retrieve(
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
    log.debug("Getting areas with filters: {}, offset: {}, limit: {}, sort: {}",
      filters, offset, limit, sort);

    try {
      // If no filters, offset, limit, or sort provided, use the cached findAll()
      if (filters == null && offset == null && limit == null && sort == null) {
        List<AreaResponseDTO> areas = areaService
          .findAll()
          .stream()
          .map(areaMapper::domainToResponseDTO)
          .collect(Collectors.toList());
        return ResponseEntity.ok(areas);
      }

      // Otherwise, use the search method with filters/sorting/pagination
      List<AreaResponseDTO> areas = areaService
        .findAll(filters, offset, limit, sort)
        .stream()
        .map(areaMapper::domainToResponseDTO)
        .collect(Collectors.toList());

      return ResponseEntity.ok(areas);
    } catch (IllegalArgumentException ex) {
      log.warn("Invalid request parameters: {}", ex.getMessage());
      throw new BadRequestException(ex.getMessage());
    } catch (UnknownPropertyException ex) {
      log.warn("Unknown property: {}", ex.getMessage(), ex);
      throw new BadRequestException(ex.getMessage());
    }
  }
  /**
   * Update an area.
   *
   * @param id the area ID
   * @param requestDTO the update request
   * @return the updated area
   */
  @PutMapping("/{id}")
  @Operation(
    summary = "Update an area",
    description = "Updates an existing area"
  )
  @ApiResponses(value = {
    @ApiResponse(
      responseCode = "200",
      description = "Area updated successfully",
      content = @Content(schema = @Schema(implementation = AreaResponseDTO.class))
    ),
    @ApiResponse(responseCode = "404", description = "Area not found"),
    @ApiResponse(responseCode = "400", description = "Invalid request data"),
    @ApiResponse(responseCode = "409", description = "Unique constraint violation")
  })
  public ResponseEntity<AreaResponseDTO> update(
    @Parameter(description = "Area ID", required = true)
    @PathVariable UUID id,
    @Valid @RequestBody AreaRequestDTO requestDTO
  ) {
    try {
      log.info("Updating area with ID: {}", id);

      // Convert DTO to domain model
      Area area = this.areaMapper.requestDTOtoDomain(requestDTO);

      // Update via service
      Area updated = areaService.update(id, area);

      // Convert to response DTO
      AreaResponseDTO responseDTO = areaMapper.domainToResponseDTO(updated);

      log.info("Area updated with ID: {}", id);

      return ResponseEntity.ok(responseDTO);
    } catch (AreaNotFoundException ex) {
      throw new NotFoundException("Area with ID %s was not found".formatted(id), ex);
    } catch (GeoShapeNotFoundException ex) {
      throw new BadRequestException("GeoShape with ID %s was not found".formatted(requestDTO.getGeoShapeId()), ex);
    } catch (UniqueConstraintException ex) {
      throw new ConflictException(ex.getMessage());
    }
  }

  /**
   * Delete an area.
   *
   * @param id the area ID
   * @return 204 No Content on success
   */
  @DeleteMapping("/{id}")
  @Operation(summary = "Delete an area", description = "Deletes an area by ID")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Area deleted successfully"),
    @ApiResponse(responseCode = "404", description = "Area not found")
  })
  public ResponseEntity<Void> delete(
    @Parameter(description = "Area ID", required = true)
    @PathVariable UUID id) {
    try {
      log.info("Deleting area with ID: {}", id);

      areaService.deleteById(id);

      log.info("Area deleted with ID: {}", id);

      return ResponseEntity.noContent().build();
    } catch (AreaNotFoundException ex) {
      throw new NotFoundException("Area with ID %s was not found".formatted(id), ex);
    }
  }

  // ================================================================
  // Other Operations
  // ================================================================

  /**
   * Check if an area exists by ID.
   *
   * @param id the area ID
   * @return 200 if exists, 404 if not
   */
  @GetMapping("/{id}/exists")
  @Operation(summary = "Check if area exists", description = "Checks if an area exists by ID")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Area exists"),
    @ApiResponse(responseCode = "404", description = "Area does not exist")
  })
  public ResponseEntity<Void> checkExists(
    @Parameter(description = "Area ID", required = true)
    @PathVariable UUID id) {

    log.debug("Checking if area exists: {}", id);

    return areaService.existsById(id)
      ? ResponseEntity.ok().build()
      : ResponseEntity.notFound().build();
  }

  /**
   * Get total count of areas.
   *
   * @return the count
   */
  @GetMapping("/count")
  @Operation(summary = "Get area count", description = "Returns the total number of areas")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
  })
  public ResponseEntity<Long> count() {
    log.debug("Getting area count");

    long count = areaService.count();

    return ResponseEntity.ok(count);
  }
}
