//package com.cjrequena.sample.controller;
//
//import com.cjrequena.sample.controller.dto.CreateGeoShapeRequestDTO;
//import com.cjrequena.sample.controller.dto.GeoShapeResponseDTO;
//import com.cjrequena.sample.controller.dto.UpdateGeoShapeRequestDTO;
//import com.cjrequena.sample.domain.mapper.GeoShapeMapper;
//import com.cjrequena.sample.domain.model.GeoShape;
//import com.cjrequena.sample.domain.model.enums.GeometryType;
//import com.cjrequena.sample.domain.model.vo.CoordinateVO;
//import com.cjrequena.sample.domain.model.vo.GeometryVO;
//import com.cjrequena.sample.domain.model.vo.MetadataVO;
//import com.cjrequena.sample.domain.model.vo.RadiusVO;
//import com.cjrequena.sample.service.GeoShapeService;
//import com.cjrequena.sample.shared.common.util.WKTParserUtil;
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
//import org.locationtech.jts.geom.Coordinate;
//import org.locationtech.jts.geom.Geometry;
//import org.locationtech.jts.geom.Point;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.net.URI;
//import java.util.List;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
///**
// * REST controller for GeoShape operations.
// *
// * <p>Provides CRUD operations and spatial query endpoints for geographic shapes.
// * Supports points, circles, rectangles, polygons, and lines with PostGIS spatial queries.
// *
// * @author cjrequena
// */
//@Log4j2
//@RestController
//@RequestMapping("/api/v1/geoshapes")
//@RequiredArgsConstructor
//@Tag(name = "GeoShapes", description = "Geographic shape management and spatial query endpoints")
//public class GeoShapeController {
//
//  private final GeoShapeService geoShapeService;
//  private final GeoShapeMapper geoShapeMapper;
//
//  /**
//   * Create a new geographic shape.
//   *
//   * @param requestDTO the shape creation request with WKT geometry
//   * @return the created shape with 201 status
//   */
//  @PostMapping
//  @Operation(summary = "Create a new geographic shape",
//    description = "Creates a new shape (point, circle, rectangle, polygon, or line) using WKT format")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "201", description = "Shape created successfully",
//      content = @Content(schema = @Schema(implementation = GeoShapeResponseDTO.class))),
//    @ApiResponse(responseCode = "400", description = "Invalid WKT geometry or request data")
//  })
//  public ResponseEntity<GeoShapeResponseDTO> createGeoShape(
//    @Valid @RequestBody CreateGeoShapeRequestDTO requestDTO) {
//
//    log.info("Creating GeoShape: {} of type {}", requestDTO.getName(), requestDTO.getGeometryType());
//
//    // Parse WKT and create appropriate GeoShape based on type
//    GeoShape geoShape = createGeoShapeFromDTO(requestDTO);
//
//    // Create via service
//    GeoShape created = geoShapeService.create(geoShape);
//
//    // Convert to response DTO
//    GeoShapeResponseDTO responseDTO = geoShapeMapper.toResponseDTO(created);
//
//    log.info("GeoShape created with ID: {}", created.getId());
//
//    return ResponseEntity
//      .created(URI.create("/api/v1/geoshapes/" + created.getId()))
//      .body(responseDTO);
//  }
//
//  /**
//   * Get a geographic shape by ID.
//   *
//   * @param id the shape ID
//   * @return the shape if found, 404 otherwise
//   */
//  @GetMapping("/{id}")
//  @Operation(summary = "Get shape by ID", description = "Retrieves a geographic shape by its unique identifier")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Shape found",
//      content = @Content(schema = @Schema(implementation = GeoShapeResponseDTO.class))),
//    @ApiResponse(responseCode = "404", description = "Shape not found")
//  })
//  public ResponseEntity<GeoShapeResponseDTO> getGeoShapeById(
//    @Parameter(description = "Shape ID", required = true)
//    @PathVariable UUID id) {
//
//    log.debug("Getting GeoShape by ID: {}", id);
//
//    return geoShapeService.findById(id)
//      .map(geoShapeMapper::toResponseDTO)
//      .map(ResponseEntity::ok)
//      .orElse(ResponseEntity.notFound().build());
//  }
//
//  /**
//   * Get all geographic shapes.
//   *
//   * @return list of all shapes
//   */
//  @GetMapping
//  @Operation(summary = "Get all shapes", description = "Retrieves all geographic shapes")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Shapes retrieved successfully")
//  })
//  public ResponseEntity<List<GeoShapeResponseDTO>> getAllGeoShapes() {
//    log.debug("Getting all GeoShapes");
//
//    List<GeoShapeResponseDTO> shapes = geoShapeService.findAll().stream()
//      .map(geoShapeMapper::toResponseDTO)
//      .collect(Collectors.toList());
//
//    return ResponseEntity.ok(shapes);
//  }
//
//  /**
//   * Update a geographic shape.
//   *
//   * @param id the shape ID
//   * @param requestDTO the update request
//   * @return the updated shape
//   */
//  @PutMapping("/{id}")
//  @Operation(summary = "Update a shape", description = "Updates an existing geographic shape")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Shape updated successfully",
//      content = @Content(schema = @Schema(implementation = GeoShapeResponseDTO.class))),
//    @ApiResponse(responseCode = "404", description = "Shape not found"),
//    @ApiResponse(responseCode = "400", description = "Invalid request data")
//  })
//  public ResponseEntity<GeoShapeResponseDTO> updateGeoShape(
//    @Parameter(description = "Shape ID", required = true)
//    @PathVariable UUID id,
//    @Valid @RequestBody UpdateGeoShapeRequestDTO requestDTO) {
//
//    log.info("Updating GeoShape with ID: {}", id);
//
//    // Parse WKT and create updated GeoShape
//    GeoShape geoShape = updateGeoShapeFromDTO(id, requestDTO);
//
//    // Update via service
//    GeoShape updated = geoShapeService.update(id, geoShape);
//
//    // Convert to response DTO
//    GeoShapeResponseDTO responseDTO = geoShapeMapper.toResponseDTO(updated);
//
//    log.info("GeoShape updated with ID: {}", id);
//
//    return ResponseEntity.ok(responseDTO);
//  }
//
//  /**
//   * Delete a geographic shape.
//   *
//   * @param id the shape ID
//   * @return 204 No Content on success
//   */
//  @DeleteMapping("/{id}")
//  @Operation(summary = "Delete a shape", description = "Deletes a geographic shape by ID")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "204", description = "Shape deleted successfully"),
//    @ApiResponse(responseCode = "404", description = "Shape not found")
//  })
//  public ResponseEntity<Void> deleteGeoShape(
//    @Parameter(description = "Shape ID", required = true)
//    @PathVariable UUID id) {
//
//    log.info("Deleting GeoShape with ID: {}", id);
//
//    geoShapeService.deleteById(id);
//
//    log.info("GeoShape deleted with ID: {}", id);
//
//    return ResponseEntity.noContent().build();
//  }
//
//  /**
//   * Find shapes containing a point.
//   *
//   * @param wkt the point in WKT format (e.g., "POINT(-3.7038 40.4168)")
//   * @return list of shapes containing the point
//   */
//  @GetMapping("/contains-point")
//  @Operation(summary = "Find shapes containing a point",
//    description = "Finds all shapes that contain the specified point using spatial query")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Query completed successfully"),
//    @ApiResponse(responseCode = "400", description = "Invalid WKT format")
//  })
//  public ResponseEntity<List<GeoShapeResponseDTO>> findShapesContainingPoint(
//    @Parameter(description = "Point in WKT format (e.g., 'POINT(-3.7038 40.4168)')", required = true)
//    @RequestParam String wkt) {
//
//    log.debug("Finding shapes containing point: {}", wkt);
//
//    try {
//      Geometry geometry = WKTParserUtil.fromWKT(wkt, GeometryType.POINT);
//      Point point = (Point) geometry;
//
//      List<GeoShapeResponseDTO> shapes = geoShapeService.findContainingPoint(point).stream()
//        .map(geoShapeMapper::toResponseDTO)
//        .collect(Collectors.toList());
//
//      return ResponseEntity.ok(shapes);
//    } catch (Exception e) {
//      log.error("Invalid WKT format: {}", wkt, e);
//      return ResponseEntity.badRequest().build();
//    }
//  }
//
//  /**
//   * Find shapes within distance from a point.
//   *
//   * @param wkt the center point in WKT format
//   * @param distanceMeters distance in meters
//   * @return list of shapes within the distance
//   */
//  @GetMapping("/within-distance")
//  @Operation(summary = "Find shapes within distance",
//    description = "Finds all shapes within specified distance from a point")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Query completed successfully"),
//    @ApiResponse(responseCode = "400", description = "Invalid WKT format or distance")
//  })
//  public ResponseEntity<List<GeoShapeResponseDTO>> findShapesWithinDistance(
//    @Parameter(description = "Center point in WKT format", required = true)
//    @RequestParam String wkt,
//    @Parameter(description = "Distance in meters", required = true)
//    @RequestParam double distanceMeters) {
//
//    log.debug("Finding shapes within {} meters of {}", distanceMeters, wkt);
//
//    try {
//      List<GeoShapeResponseDTO> shapes = geoShapeService.findWithinDistance(wkt, distanceMeters).stream()
//        .map(geoShapeMapper::toResponseDTO)
//        .collect(Collectors.toList());
//
//      return ResponseEntity.ok(shapes);
//    } catch (Exception e) {
//      log.error("Error in spatial query: {}", e.getMessage(), e);
//      return ResponseEntity.badRequest().build();
//    }
//  }
//
//  /**
//   * Find shapes intersecting with a geometry.
//   *
//   * @param wkt the geometry in WKT format
//   * @return list of intersecting shapes
//   */
//  @GetMapping("/intersects")
//  @Operation(summary = "Find intersecting shapes",
//    description = "Finds all shapes that intersect with the specified geometry")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Query completed successfully"),
//    @ApiResponse(responseCode = "400", description = "Invalid WKT format")
//  })
//  public ResponseEntity<List<GeoShapeResponseDTO>> findIntersectingShapes(
//    @Parameter(description = "Geometry in WKT format", required = true)
//    @RequestParam String wkt) {
//
//    log.debug("Finding shapes intersecting with: {}", wkt);
//
//    try {
//      Geometry geometry = WKTParserUtil.fromWKT(wkt, GeometryType.POLYGON);
//
//      List<GeoShapeResponseDTO> shapes = geoShapeService.findIntersecting(geometry).stream()
//        .map(geoShapeMapper::toResponseDTO)
//        .collect(Collectors.toList());
//
//      return ResponseEntity.ok(shapes);
//    } catch (Exception e) {
//      log.error("Invalid WKT format: {}", wkt, e);
//      return ResponseEntity.badRequest().build();
//    }
//  }
//
//  /**
//   * Search shapes by name.
//   *
//   * @param name the name substring to search for
//   * @return list of matching shapes
//   */
//  @GetMapping("/search")
//  @Operation(summary = "Search shapes by name",
//    description = "Searches shapes by name (case-insensitive partial match)")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Search completed successfully")
//  })
//  public ResponseEntity<List<GeoShapeResponseDTO>> searchShapesByName(
//    @Parameter(description = "Name substring to search for", required = true)
//    @RequestParam String name) {
//
//    log.debug("Searching shapes by name: {}", name);
//
//    List<GeoShapeResponseDTO> shapes = geoShapeService.findByNameContaining(name).stream()
//      .map(geoShapeMapper::toResponseDTO)
//      .collect(Collectors.toList());
//
//    return ResponseEntity.ok(shapes);
//  }
//
//  /**
//   * Get total count of shapes.
//   *
//   * @return the count
//   */
//  @GetMapping("/count")
//  @Operation(summary = "Get shape count", description = "Returns the total number of geographic shapes")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
//  })
//  public ResponseEntity<Long> getShapeCount() {
//    log.debug("Getting shape count");
//
//    long count = geoShapeService.count();
//
//    return ResponseEntity.ok(count);
//  }
//
//  // ================================================================
//  // Private Helper Methods
//  // ================================================================
//
//  private GeoShape createGeoShapeFromDTO(CreateGeoShapeRequestDTO dto) {
//    UUID id = UUID.randomUUID();
//    Geometry geometry = WKTParserUtil.fromWKT(dto.getGeometryWKT(), dto.getGeometryType());
//    MetadataVO metadata = dto.getMetadata() != null ? MetadataVO.of(dto.getMetadata()) : MetadataVO.empty();
//
//    return switch (dto.getGeometryType()) {
//      case POINT -> {
//        CoordinateVO coordinate = CoordinateVO.of(geometry.getCoordinate().y, geometry.getCoordinate().x);
//        yield GeoShape.createPoint(id, dto.getName(), coordinate, metadata);
//      }
//      case CIRCLE -> {
//        Point centroid = geometry.getCentroid();
//        CoordinateVO coordinate = CoordinateVO.of(centroid.getY(), centroid.getX());
//        Coordinate boundaryPoint = geometry.getCoordinates()[0];
//        double radius = centroid.getCoordinate().distance(boundaryPoint);
//        yield GeoShape.createCircle(id, dto.getName(), coordinate, RadiusVO.of(radius), metadata);
//      }
//      case RECTANGLE -> {
//        CoordinateVO coordinate = CoordinateVO.of(geometry.getCentroid().getY(), geometry.getCentroid().getX());
//        GeometryVO geometryVO = GeometryVO.ofCoordinates(coordinate);
//        yield GeoShape.createRectangle(id, dto.getName(), geometryVO, geometryVO.getBoundingBox().toBounds(), metadata);
//      }
//      case POLYGON -> {
//        CoordinateVO coordinate = CoordinateVO.of(geometry.getCentroid().getY(), geometry.getCentroid().getX());
//        GeometryVO geometryVO = GeometryVO.ofCoordinates(coordinate);
//        yield GeoShape.createPolygon(id, dto.getName(), geometryVO, geometryVO.getBoundingBox().toBounds(), metadata);
//      }
//      case LINE -> {
//        CoordinateVO coordinate = CoordinateVO.of(geometry.getCentroid().getY(), geometry.getCentroid().getX());
//        GeometryVO geometryVO = GeometryVO.ofCoordinates(coordinate);
//        yield GeoShape.createLine(id, dto.getName(), geometryVO, metadata);
//      }
//    };
//  }
//
//  private GeoShape updateGeoShapeFromDTO(UUID id, UpdateGeoShapeRequestDTO dto) {
//    Geometry geometry = WKTParserUtil.fromWKT(dto.getGeometryWKT(), dto.getGeometryType());
//    MetadataVO metadata = dto.getMetadata() != null ? MetadataVO.of(dto.getMetadata()) : MetadataVO.empty();
//
//    return switch (dto.getGeometryType()) {
//      case POINT -> {
//        CoordinateVO coordinate = CoordinateVO.of(geometry.getCoordinate().y, geometry.getCoordinate().x);
//        yield GeoShape.createPoint(id, dto.getName(), coordinate, metadata);
//      }
//      case CIRCLE -> {
//        Point centroid = geometry.getCentroid();
//        CoordinateVO coordinate = CoordinateVO.of(centroid.getY(), centroid.getX());
//        Coordinate boundaryPoint = geometry.getCoordinates()[0];
//        double radius = centroid.getCoordinate().distance(boundaryPoint);
//        yield GeoShape.createCircle(id, dto.getName(), coordinate, RadiusVO.of(radius), metadata);
//      }
//      case RECTANGLE -> {
//        CoordinateVO coordinate = CoordinateVO.of(geometry.getCentroid().getY(), geometry.getCentroid().getX());
//        GeometryVO geometryVO = GeometryVO.ofCoordinates(coordinate);
//        yield GeoShape.createRectangle(id, dto.getName(), geometryVO, geometryVO.getBoundingBox().toBounds(), metadata);
//      }
//      case POLYGON -> {
//        CoordinateVO coordinate = CoordinateVO.of(geometry.getCentroid().getY(), geometry.getCentroid().getX());
//        GeometryVO geometryVO = GeometryVO.ofCoordinates(coordinate);
//        yield GeoShape.createPolygon(id, dto.getName(), geometryVO, geometryVO.getBoundingBox().toBounds(), metadata);
//      }
//      case LINE -> {
//        CoordinateVO coordinate = CoordinateVO.of(geometry.getCentroid().getY(), geometry.getCentroid().getX());
//        GeometryVO geometryVO = GeometryVO.ofCoordinates(coordinate);
//        yield GeoShape.createLine(id, dto.getName(), geometryVO, metadata);
//      }
//    };
//  }
//}
