package com.cjrequena.sample.controller;

import com.cjrequena.sample.controller.dto.AreaRequestDTO;
import com.cjrequena.sample.controller.dto.AreaResponseDTO;
import com.cjrequena.sample.controller.exception.ConflictException;
import com.cjrequena.sample.controller.exception.NotFoundException;
import com.cjrequena.sample.domain.exception.AreaNotFoundException;
import com.cjrequena.sample.domain.exception.UniqueConstraintException;
import com.cjrequena.sample.domain.exception.ZoneNotFoundException;
import com.cjrequena.sample.domain.mapper.AreaMapper;
import com.cjrequena.sample.domain.model.Area;
import com.cjrequena.sample.service.AreaService;
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
      @ApiResponse(responseCode = "404", description = "Parent city not found")
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
        content = @Content(schema = @Schema(implementation = AreaResponseDTO.class))),
      @ApiResponse(responseCode = "404", description = "Area not found")
    }
  )
  public ResponseEntity<AreaResponseDTO> retrieveById(
    @Parameter(description = "Area ID", required = true)
    @PathVariable UUID id) {
    try {
      log.debug("Getting area by ID: {}", id);

      return areaService
        .findById(id)
        .map(areaMapper::domainToResponseDTO)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
    } catch (AreaNotFoundException ex) {
      throw new NotFoundException("Area with ID %s was not found".formatted(id), ex);
    }
  }

  /**
   * Get all areas.
   *
   * @return list of all areas
   */
  @GetMapping
  @Operation(summary = "Get all areas", description = "Retrieves all areas")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Areas retrieved successfully")
  })
  public ResponseEntity<List<AreaResponseDTO>> retrieve() {
    log.debug("Getting all areas");

    List<AreaResponseDTO> areas = areaService.findAll().stream()
      .map(areaMapper::domainToResponseDTO)
      .collect(Collectors.toList());

    return ResponseEntity.ok(areas);
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
    @ApiResponse(responseCode = "200", description = "Area updated successfully",
      content = @Content(schema = @Schema(implementation = AreaResponseDTO.class))
    ),
    @ApiResponse(responseCode = "404", description = "Area not found"),
    @ApiResponse(responseCode = "400", description = "Invalid request data")
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
    } catch (ZoneNotFoundException ex) {
      throw new NotFoundException("Area with ID %s was not found".formatted(id), ex);
    }
  }

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
