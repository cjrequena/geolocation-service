package com.cjrequena.sample.controller;

import com.cjrequena.sample.controller.dto.ZoneRequestDTO;
import com.cjrequena.sample.controller.dto.ZoneResponseDTO;
import com.cjrequena.sample.controller.exception.BadRequestException;
import com.cjrequena.sample.controller.exception.ConflictException;
import com.cjrequena.sample.controller.exception.NotFoundException;
import com.cjrequena.sample.domain.exception.*;
import com.cjrequena.sample.domain.mapper.ZoneMapper;
import com.cjrequena.sample.domain.model.Zone;
import com.cjrequena.sample.service.ZoneService;
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
 * REST controller for Zone operations.
 *
 * <p>Provides CRUD operations and query endpoints for zones (blocks, sectors, precincts).
 * Zones belong to areas and can contain locations.
 *
 * @author cjrequena
 */
@Log4j2
@RestController
@RequestMapping(value = ZoneController.ENDPOINT, headers = {ZoneController.ACCEPT_VERSION})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Tag(name = "Zones", description = "Zone management endpoints (blocks, sectors, precincts)")
public class ZoneController {

  public static final String ENDPOINT = "/geolocation-service/api/zones";
  public static final String ACCEPT_VERSION = "Accept-Version=" + VND_SAMPLE_SERVICE_V1;

  private final ZoneService zoneService;
  private final ZoneMapper zoneMapper;

  /**
   * Create a new zone.
   *
   * @param requestDTO the zone creation request
   * @return the created zone with 201 status
   */
  @PostMapping
  @Operation(summary = "Create a new zone", description = "Creates a new zone within an area")
  @ApiResponses(
    value = {
      @ApiResponse(responseCode = "201", description = "Zone created successfully",
        content = @Content(schema = @Schema(implementation = ZoneResponseDTO.class))
      ),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "404", description = "Parent area not found")
    }
  )
  public ResponseEntity<ZoneResponseDTO> create(@Valid @RequestBody ZoneRequestDTO requestDTO) {
    try {
      log.info("Creating zone: {} in area: {}", requestDTO.getName(), requestDTO.getAreaId());

      // Convert DTO to domain model
      Zone zone = this.zoneMapper.requestDTOtoDomain(requestDTO);

      // Create via service
      Zone created = zoneService.create(zone);

      // Convert to response DTO
      ZoneResponseDTO responseDTO = zoneMapper.domainToResponseDTO(created);

      log.info("Zone created with ID: {}", created.getId());

      return ResponseEntity
        .created(URI.create(ENDPOINT + created.getId()))
        .header("Accept-Version", ACCEPT_VERSION)
        .body(responseDTO);
    } catch (AreaNotFoundException ex) {
      throw new NotFoundException("Area with ID %s was not found".formatted(requestDTO.getAreaId()), ex);
    } catch (GeoShapeNotFoundException ex) {
      throw new NotFoundException("GeoShape with ID %s was not found".formatted(requestDTO.getGeoShapeId()), ex);
    } catch (AreaRequiredException ex) {
      throw new BadRequestException(ex.getMessage());
    } catch (UniqueConstraintException ex) {
      throw new ConflictException(ex.getMessage());
    }
  }

  /**
   * Get a zone by ID.
   *
   * @param id the zone ID
   * @return the zone if found, 404 otherwise
   */
  @GetMapping("/{id}")
  @Operation(summary = "Get zone by ID", description = "Retrieves a zone by its unique identifier")
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        description = "Zone found",
        content = @Content(schema = @Schema(implementation = ZoneResponseDTO.class))),
      @ApiResponse(responseCode = "404", description = "Zone not found")
    }
  )
  public ResponseEntity<ZoneResponseDTO> retrieveById(
    @Parameter(description = "Zone ID", required = true)
    @PathVariable UUID id) {
    try {
      log.debug("Getting zone by ID: {}", id);

      return zoneService
        .findById(id)
        .map(zoneMapper::domainToResponseDTO)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
    } catch (ZoneNotFoundException ex) {
      throw new NotFoundException("Zone with ID %s was not found".formatted(id), ex);
    }
  }

  /**
   * Get all zones.
   *
   * @return list of all zones
   */
  @GetMapping
  @Operation(summary = "Get all zones", description = "Retrieves all zones")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Zones retrieved successfully")
  })
  public ResponseEntity<List<ZoneResponseDTO>> retrieve() {
    log.debug("Getting all zones");

    List<ZoneResponseDTO> zones = zoneService
      .findAll()
      .stream()
      .map(zoneMapper::domainToResponseDTO)
      .collect(Collectors.toList());

    return ResponseEntity.ok(zones);
  }

  /**
   * Update a zone.
   *
   * @param id the zone ID
   * @param requestDTO the update request
   * @return the updated zone
   */
  @PutMapping("/{id}")
  @Operation(summary = "Update a zone", description = "Updates an existing zone")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Zone updated successfully",
      content = @Content(schema = @Schema(implementation = ZoneResponseDTO.class))),
    @ApiResponse(responseCode = "404", description = "Zone not found"),
    @ApiResponse(responseCode = "400", description = "Invalid request data")
  })
  public ResponseEntity<ZoneResponseDTO> update(
    @Parameter(description = "Zone ID", required = true)
    @PathVariable UUID id,
    @Valid @RequestBody ZoneRequestDTO requestDTO) {
    try {
      log.info("Updating zone with ID: {}", id);

      // Convert DTO to domain model
      Zone zone = this.zoneMapper.requestDTOtoDomain(requestDTO);

      // Update via service
      Zone updated = zoneService.update(id, zone);

      // Convert to response DTO
      ZoneResponseDTO responseDTO = zoneMapper.domainToResponseDTO(updated);

      log.info("Zone updated with ID: {}", id);

      return ResponseEntity.ok(responseDTO);
    } catch (ZoneNotFoundException ex) {
      throw new NotFoundException("Zone with ID %s was not found".formatted(id), ex);
    } catch (GeoShapeNotFoundException ex) {
      throw new NotFoundException("GeoShape with ID %s was not found".formatted(requestDTO.getGeoShapeId()), ex);
    } catch (UniqueConstraintException ex) {
      throw new ConflictException(ex.getMessage());
    }
  }

  /**
   * Delete a zone.
   *
   * @param id the zone ID
   * @return 204 No Content on success
   */
  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a zone", description = "Deletes a zone by ID")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Zone deleted successfully"),
    @ApiResponse(responseCode = "404", description = "Zone not found")
  })
  public ResponseEntity<Void> delete(
    @Parameter(description = "Zone ID", required = true)
    @PathVariable UUID id) {
    try {
      log.info("Deleting zone with ID: {}", id);

      zoneService.deleteById(id);

      log.info("Zone deleted with ID: {}", id);

      return ResponseEntity.noContent().build();
    } catch (ZoneNotFoundException ex) {
      throw new NotFoundException("Zone with ID %s was not found".formatted(id), ex);
    }
  }

  /**
   * Check if a zone exists by ID.
   *
   * @param id the zone ID
   * @return 200 if exists, 404 if not
   */
  @GetMapping("/{id}/exists")
  @Operation(summary = "Check if zone exists", description = "Checks if a zone exists by ID")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Zone exists"),
    @ApiResponse(responseCode = "404", description = "Zone does not exist")
  })
  public ResponseEntity<Void> checkExists(
    @Parameter(description = "Zone ID", required = true)
    @PathVariable UUID id) {

    log.debug("Checking if zone exists: {}", id);

    return zoneService
      .existsById(id)
      ? ResponseEntity.ok().build()
      : ResponseEntity.notFound().build();
  }

  /**
   * Get total count of zones.
   *
   * @return the count
   */
  @GetMapping("/count")
  @Operation(summary = "Get zone count", description = "Returns the total number of zones")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
  })
  public ResponseEntity<Long> count() {
    log.debug("Getting zone count");

    long count = zoneService.count();

    return ResponseEntity.ok(count);
  }
}
