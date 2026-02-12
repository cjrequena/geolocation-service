//package com.cjrequena.sample.controller;
//
//import com.cjrequena.sample.controller.dto.CountryResponseDTO;
//import com.cjrequena.sample.controller.dto.CreateCountryRequestDTO;
//import com.cjrequena.sample.controller.dto.UpdateCountryRequestDTO;
//import com.cjrequena.sample.domain.mapper.CountryMapper;
//import com.cjrequena.sample.domain.model.Country;
//import com.cjrequena.sample.domain.model.vo.IsoCodeVO;
//import com.cjrequena.sample.domain.model.vo.MetadataVO;
//import com.cjrequena.sample.domain.model.vo.PopulationVO;
//import com.cjrequena.sample.service.CountryService;
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
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.web.PageableDefault;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.net.URI;
//import java.util.List;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
///**
// * REST controller for Country operations.
// *
// * <p>Provides CRUD operations and query endpoints for countries.
// * Uses CountryService for business logic and CountryMapper for DTO conversions.
// *
// * @author cjrequena
// */
//@Log4j2
//@RestController
//@RequestMapping("/api/v1/countries")
//@RequiredArgsConstructor
//@Tag(name = "Countries", description = "Country management endpoints")
//public class CountryController {
//
//  private final CountryService countryService;
//  private final CountryMapper countryMapper;
//
//  /**
//   * Create a new country.
//   *
//   * @param requestDTO the country creation request
//   * @return the created country with 201 status
//   */
//  @PostMapping
//  @Operation(summary = "Create a new country", description = "Creates a new country with the provided details")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "201", description = "Country created successfully",
//      content = @Content(schema = @Schema(implementation = CountryResponseDTO.class))),
//    @ApiResponse(responseCode = "400", description = "Invalid request data"),
//    @ApiResponse(responseCode = "409", description = "Country with same ISO code already exists")
//  })
//  public ResponseEntity<CountryResponseDTO> createCountry(
//    @Valid @RequestBody CreateCountryRequestDTO requestDTO) {
//
//    log.info("Creating country: {}", requestDTO.getName());
//
//    // Convert DTO to domain model
//    Country country = Country.create(
//      UUID.randomUUID(),
//      requestDTO.getName(),
//      IsoCodeVO.of(requestDTO.getIsoCodeAlpha2(), requestDTO.getIsoCodeAlpha3(), requestDTO.getIsoCodeNumeric()),
//      requestDTO.getPhoneCode(),
//      requestDTO.getCurrencyCode(),
//      requestDTO.getCapital(),
//      requestDTO.getPopulation() != null ? PopulationVO.of(requestDTO.getPopulation()) : null,
//      requestDTO.getActive() != null ? requestDTO.getActive() : Boolean.TRUE,
//      requestDTO.getMetadata() != null ? MetadataVO.of(requestDTO.getMetadata()) : MetadataVO.empty()
//    );
//
//    // Create via service
//    Country created = countryService.create(country);
//
//    // Convert to response DTO
//    CountryResponseDTO responseDTO = countryMapper.toResponseDTO(created);
//
//    log.info("Country created with ID: {}", created.getId());
//
//    return ResponseEntity
//      .created(URI.create("/api/v1/countries/" + created.getId()))
//      .body(responseDTO);
//  }
//
//  /**
//   * Get a country by ID.
//   *
//   * @param id the country ID
//   * @return the country if found, 404 otherwise
//   */
//  @GetMapping("/{id}")
//  @Operation(summary = "Get country by ID", description = "Retrieves a country by its unique identifier")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Country found",
//      content = @Content(schema = @Schema(implementation = CountryResponseDTO.class))),
//    @ApiResponse(responseCode = "404", description = "Country not found")
//  })
//  public ResponseEntity<CountryResponseDTO> getCountryById(
//    @Parameter(description = "Country ID", required = true)
//    @PathVariable UUID id) {
//
//    log.debug("Getting country by ID: {}", id);
//
//    return countryService.findById(id)
//      .map(countryMapper::toResponseDTO)
//      .map(ResponseEntity::ok)
//      .orElse(ResponseEntity.notFound().build());
//  }
//
//  /**
//   * Get all countries.
//   *
//   * @return list of all countries
//   */
//  @GetMapping
//  @Operation(summary = "Get all countries", description = "Retrieves all countries")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Countries retrieved successfully")
//  })
//  public ResponseEntity<List<CountryResponseDTO>> getAllCountries() {
//    log.debug("Getting all countries");
//
//    List<CountryResponseDTO> countries = countryService.findAll().stream()
//      .map(countryMapper::toResponseDTO)
//      .collect(Collectors.toList());
//
//    return ResponseEntity.ok(countries);
//  }
//
//  /**
//   * Get countries with pagination.
//   *
//   * @param pageable pagination parameters
//   * @return page of countries
//   */
//  @GetMapping("/page")
//  @Operation(summary = "Get countries with pagination", description = "Retrieves countries with pagination support")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Countries retrieved successfully")
//  })
//  public ResponseEntity<Page<CountryResponseDTO>> getCountriesPage(
//    @PageableDefault(size = 20) Pageable pageable) {
//
//    log.debug("Getting countries page: {}", pageable);
//
//    Page<CountryResponseDTO> page = countryService.findByActive(true, pageable)
//      .map(countryMapper::toResponseDTO);
//
//    return ResponseEntity.ok(page);
//  }
//
//  /**
//   * Update a country.
//   *
//   * @param id the country ID
//   * @param requestDTO the update request
//   * @return the updated country
//   */
//  @PutMapping("/{id}")
//  @Operation(summary = "Update a country", description = "Updates an existing country")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Country updated successfully",
//      content = @Content(schema = @Schema(implementation = CountryResponseDTO.class))),
//    @ApiResponse(responseCode = "404", description = "Country not found"),
//    @ApiResponse(responseCode = "400", description = "Invalid request data")
//  })
//  public ResponseEntity<CountryResponseDTO> updateCountry(
//    @Parameter(description = "Country ID", required = true)
//    @PathVariable UUID id,
//    @Valid @RequestBody UpdateCountryRequestDTO requestDTO) {
//
//    log.info("Updating country with ID: {}", id);
//
//    // Convert DTO to domain model
//    Country country = Country.create(
//      id,
//      requestDTO.getName(),
//      IsoCodeVO.of(requestDTO.getIsoCodeAlpha2(), requestDTO.getIsoCodeAlpha3(), requestDTO.getIsoCodeNumeric()),
//      requestDTO.getPhoneCode(),
//      requestDTO.getCurrencyCode(),
//      requestDTO.getCapital(),
//      requestDTO.getPopulation() != null ? PopulationVO.of(requestDTO.getPopulation()) : null,
//      requestDTO.getActive() != null ? requestDTO.getActive() : Boolean.TRUE,
//      requestDTO.getMetadata() != null ? MetadataVO.of(requestDTO.getMetadata()) : MetadataVO.empty()
//    );
//
//    // Update via service
//    Country updated = countryService.update(id, country);
//
//    // Convert to response DTO
//    CountryResponseDTO responseDTO = countryMapper.toResponseDTO(updated);
//
//    log.info("Country updated with ID: {}", id);
//
//    return ResponseEntity.ok(responseDTO);
//  }
//
//  /**
//   * Delete a country.
//   *
//   * @param id the country ID
//   * @return 204 No Content on success
//   */
//  @DeleteMapping("/{id}")
//  @Operation(summary = "Delete a country", description = "Deletes a country by ID")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "204", description = "Country deleted successfully"),
//    @ApiResponse(responseCode = "404", description = "Country not found")
//  })
//  public ResponseEntity<Void> deleteCountry(
//    @Parameter(description = "Country ID", required = true)
//    @PathVariable UUID id) {
//
//    log.info("Deleting country with ID: {}", id);
//
//    countryService.deleteById(id);
//
//    log.info("Country deleted with ID: {}", id);
//
//    return ResponseEntity.noContent().build();
//  }
//
//  /**
//   * Get country by ISO alpha-2 code.
//   *
//   * @param alpha2 the ISO alpha-2 code
//   * @return the country if found
//   */
//  @GetMapping("/iso-alpha2/{alpha2}")
//  @Operation(summary = "Get country by ISO alpha-2 code", description = "Retrieves a country by its ISO 3166-1 alpha-2 code")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Country found"),
//    @ApiResponse(responseCode = "404", description = "Country not found")
//  })
//  public ResponseEntity<CountryResponseDTO> getCountryByIsoAlpha2(
//    @Parameter(description = "ISO alpha-2 code (e.g., US, ES)", required = true)
//    @PathVariable String alpha2) {
//
//    log.debug("Getting country by ISO alpha-2: {}", alpha2);
//
//    return countryService.findByIsoAlpha2(alpha2.toUpperCase())
//      .map(countryMapper::toResponseDTO)
//      .map(ResponseEntity::ok)
//      .orElse(ResponseEntity.notFound().build());
//  }
//
//  /**
//   * Get country by ISO alpha-3 code.
//   *
//   * @param alpha3 the ISO alpha-3 code
//   * @return the country if found
//   */
//  @GetMapping("/iso-alpha3/{alpha3}")
//  @Operation(summary = "Get country by ISO alpha-3 code", description = "Retrieves a country by its ISO 3166-1 alpha-3 code")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Country found"),
//    @ApiResponse(responseCode = "404", description = "Country not found")
//  })
//  public ResponseEntity<CountryResponseDTO> getCountryByIsoAlpha3(
//    @Parameter(description = "ISO alpha-3 code (e.g., USA, ESP)", required = true)
//    @PathVariable String alpha3) {
//
//    log.debug("Getting country by ISO alpha-3: {}", alpha3);
//
//    return countryService.findByIsoAlpha3(alpha3.toUpperCase())
//      .map(countryMapper::toResponseDTO)
//      .map(ResponseEntity::ok)
//      .orElse(ResponseEntity.notFound().build());
//  }
//
//  /**
//   * Search countries by name.
//   *
//   * @param name the name substring to search for
//   * @return list of matching countries
//   */
//  @GetMapping("/search")
//  @Operation(summary = "Search countries by name", description = "Searches countries by name (case-insensitive partial match)")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Search completed successfully")
//  })
//  public ResponseEntity<List<CountryResponseDTO>> searchCountriesByName(
//    @Parameter(description = "Name substring to search for", required = true)
//    @RequestParam String name) {
//
//    log.debug("Searching countries by name: {}", name);
//
//    List<CountryResponseDTO> countries = countryService.findByNameContaining(name).stream()
//      .map(countryMapper::toResponseDTO)
//      .collect(Collectors.toList());
//
//    return ResponseEntity.ok(countries);
//  }
//
//  /**
//   * Get countries by currency code.
//   *
//   * @param currencyCode the currency code
//   * @return list of countries using the currency
//   */
//  @GetMapping("/currency/{currencyCode}")
//  @Operation(summary = "Get countries by currency", description = "Retrieves all countries using a specific currency")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Countries retrieved successfully")
//  })
//  public ResponseEntity<List<CountryResponseDTO>> getCountriesByCurrency(
//    @Parameter(description = "ISO 4217 currency code (e.g., USD, EUR)", required = true)
//    @PathVariable String currencyCode) {
//
//    log.debug("Getting countries by currency: {}", currencyCode);
//
//    List<CountryResponseDTO> countries = countryService.findByCurrencyCode(currencyCode.toUpperCase()).stream()
//      .map(countryMapper::toResponseDTO)
//      .collect(Collectors.toList());
//
//    return ResponseEntity.ok(countries);
//  }
//
//  /**
//   * Check if a country exists by ID.
//   *
//   * @param id the country ID
//   * @return 200 if exists, 404 if not
//   */
//  @GetMapping("/{id}/exists")
//  @Operation(summary = "Check if country exists", description = "Checks if a country exists by ID")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Country exists"),
//    @ApiResponse(responseCode = "404", description = "Country does not exist")
//  })
//  public ResponseEntity<Void> checkCountryExists(
//    @Parameter(description = "Country ID", required = true)
//    @PathVariable UUID id) {
//
//    log.debug("Checking if country exists: {}", id);
//
//    return countryService.existsById(id)
//      ? ResponseEntity.ok().build()
//      : ResponseEntity.notFound().build();
//  }
//
//  /**
//   * Get total count of countries.
//   *
//   * @return the count
//   */
//  @GetMapping("/count")
//  @Operation(summary = "Get country count", description = "Returns the total number of countries")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
//  })
//  public ResponseEntity<Long> getCountryCount() {
//    log.debug("Getting country count");
//
//    long count = countryService.count();
//
//    return ResponseEntity.ok(count);
//  }
//}
