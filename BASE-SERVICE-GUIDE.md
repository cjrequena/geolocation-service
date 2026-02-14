# BaseService Guide: Reusable RSQL Filtering, Sorting, and Pagination

This guide explains how to use the `BaseService` abstraction to add RSQL filtering, sorting, and pagination to any service in your application.

## Overview

`BaseService<E, D>` is an abstract base class that provides common functionality for:
- RSQL filtering (e.g., `"active==true;name=like='test'"`)
- Sorting (e.g., `"name,asc"` or `"name,asc;createdAt,desc"`)
- Pagination with offset/limit
- Count operations with filters

## Architecture

```
BaseService<EntityType, DomainType>
    ↑
    |
    └── YourService (LocationService, CountryService, etc.)
```

## Prerequisites

Your repository must extend both:
1. `JpaRepository<E, ID>`
2. `JpaSpecificationExecutor<E>`
3. `QuerydslPredicateExecutor<E>` (optional, for QueryDSL support)

Example:
```java
@Repository
public interface YourRepository extends 
    JpaRepository<YourEntity, UUID>, 
    JpaSpecificationExecutor<YourEntity>,
    QuerydslPredicateExecutor<YourEntity> {
    // Your custom query methods
}
```

---

## Step-by-Step Implementation

### Step 1: Extend BaseService

Make your service extend `BaseService<EntityType, DomainType>`:

```java
@Log4j2
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class YourService extends BaseService<YourEntity, YourDomain> {

    private final YourRepository repository;
    private final YourMapper mapper;
    
    // ... your existing code
}
```

### Step 2: Implement Required Abstract Methods

Implement the four required methods from `BaseService`:

```java
@Override
protected JpaRepository<YourEntity, ?> getRepository() {
    return repository;
}

@Override
protected JpaSpecificationExecutor<YourEntity> getSpecificationExecutor() {
    return repository;
}

@Override
protected Function<YourEntity, YourDomain> getEntityToDomainMapper() {
    return mapper::toDomain;
}

@Override
protected Class<YourEntity> getEntityClass() {
    return YourEntity.class;
}
```

### Step 3: Add Search Method

Add a public `search()` method that delegates to the base class:

```java
/**
 * Finds all entities with optional RSQL filtering, sorting, and pagination.
 *
 * @param filters RSQL filter expression (e.g., "active==true;name=like='test'")
 * @param offset the offset for pagination (0-based)
 * @param limit the maximum number of results to return
 * @param sort the sort expression (e.g., "name,asc" or "name,desc;createdAt,asc")
 * @return list of domain models matching the criteria
 */
public List<YourDomain> search(String filters, Integer offset, Integer limit, String sort) {
    return super.findAllWithFiltersAndSort(filters, offset, limit, sort);
}
```

### Step 4: Keep Your Existing findAll() Method

Keep your existing `findAll()` method unchanged for backward compatibility:

```java
/**
 * Finds all entities without any filtering or pagination.
 * 
 * @return list of all entities
 */
public List<YourDomain> findAll() {
    // Your existing implementation (with or without cache)
    return repository.findAll().stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
}
```

---

## Complete Example: LocationService

```java
@Log4j2
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationService extends BaseService<LocationEntity, Location> {

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    // ================================================================
    // BaseService Implementation
    // ================================================================

    @Override
    protected JpaRepository<LocationEntity, ?> getRepository() {
        return locationRepository;
    }

    @Override
    protected JpaSpecificationExecutor<LocationEntity> getSpecificationExecutor() {
        return locationRepository;
    }

    @Override
    protected Function<LocationEntity, Location> getEntityToDomainMapper() {
        return locationMapper::toDomain;
    }

    @Override
    protected Class<LocationEntity> getEntityClass() {
        return LocationEntity.class;
    }

    // ================================================================
    // Public API
    // ================================================================

    /**
     * Finds all locations without filtering or pagination.
     */
    public List<Location> findAll() {
        return locationRepository.findAll().stream()
            .map(locationMapper::toDomain)
            .collect(Collectors.toList());
    }

    /**
     * Finds locations with optional RSQL filtering, sorting, and pagination.
     */
    public List<Location> search(String filters, Integer offset, Integer limit, String sort) {
        return super.findAllWithFiltersAndSort(filters, offset, limit, sort);
    }

    // ... your other methods (create, update, delete, etc.)
}
```

---

## Controller Integration

Update your controller's `getAll` endpoint to use both methods:

```java
@GetMapping
public ResponseEntity<List<YourResponseDTO>> getAll(
    @RequestParam(value = "filters", required = false) String filters,
    @RequestParam(value = "offset", required = false) Integer offset,
    @RequestParam(value = "limit", required = false) Integer limit,
    @RequestParam(value = "sort", required = false) String sort
) {
    try {
        // If no query parameters, use the simple findAll() (may use cache)
        if (filters == null && offset == null && limit == null && sort == null) {
            List<YourResponseDTO> results = yourService
                .findAll()
                .stream()
                .map(mapper::domainToResponseDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(results);
        }

        // Otherwise, use the search method with filters/sorting/pagination
        List<YourResponseDTO> results = yourService
            .search(filters, offset, limit, sort)
            .stream()
            .map(mapper::domainToResponseDTO)
            .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    } catch (IllegalArgumentException e) {
        log.error("Invalid request parameters: {}", e.getMessage());
        return ResponseEntity.badRequest().build();
    }
}
```

---

## Usage Examples

### No filters (uses findAll)
```bash
GET /api/locations
```

### With RSQL filter
```bash
GET /api/locations?filters=active==true
GET /api/locations?filters=active==true;postalCode==94102
GET /api/locations?filters=name=like="Bridge"
```

### With sorting
```bash
GET /api/locations?sort=name,asc
GET /api/locations?sort=name,asc;createdAt,desc
```

### With pagination
```bash
GET /api/locations?offset=0&limit=10
GET /api/locations?offset=10&limit=10
```

### Combined
```bash
GET /api/locations?filters=active==true&sort=name,asc&offset=0&limit=20
```

---

## Benefits

1. **Reusability**: Write the filtering/sorting/pagination logic once, use it everywhere
2. **Consistency**: All services have the same API for filtering, sorting, and pagination
3. **Maintainability**: Changes to the base logic automatically apply to all services
4. **Backward Compatibility**: Existing `findAll()` methods remain unchanged
5. **Performance**: Can optimize caching strategy per service (findAll vs search)
6. **Type Safety**: Generic types ensure compile-time type checking

---

## Additional Features

### Count with Filters

The base class also provides a `countWithFilters()` method:

```java
public long countActiveLocations() {
    return super.countWithFilters("active==true");
}
```

### Error Handling

The base class handles common errors:
- Invalid RSQL filter expressions → `IllegalArgumentException`
- Invalid sort expressions → `IllegalArgumentException`
- Invalid offset/limit values → `IllegalArgumentException`

---

## Services Already Implemented

1. ✅ `LocationService` - extends BaseService
2. ✅ `CountryService` - extends BaseService

## Services To Implement

To add filtering/sorting/pagination to other services:

1. Update the repository to extend `JpaSpecificationExecutor` and `QuerydslPredicateExecutor`
2. Make the service extend `BaseService<EntityType, DomainType>`
3. Implement the four required abstract methods
4. Add a public `search()` method
5. Update the controller to use both `findAll()` and `search()`

Example repositories to update:
- `AreaRepository`
- `CityRepository`
- `RegionRepository`
- `ZoneRepository`
- `GeoShapeRepository`

---

## RSQL Operators Reference

| Operator | Description | Example |
|----------|-------------|---------|
| `==` | Equal | `active==true` |
| `!=` | Not equal | `active!=false` |
| `=lt=` | Less than | `population=lt=1000000` |
| `=le=` | Less than or equal | `population=le=1000000` |
| `=gt=` | Greater than | `population=gt=500000` |
| `=ge=` | Greater than or equal | `population=ge=500000` |
| `=like=` | Like (case-insensitive) | `name=like="United"` |
| `=in=` | In list | `isoCodeAlpha2=in=(US,GB,FR)` |
| `=out=` | Not in list | `isoCodeAlpha2=out=(XX,YY)` |
| `;` | AND condition | `active==true;population=gt=1000000` |
| `,` | OR condition | `isoCodeAlpha2==US,isoCodeAlpha2==GB` |

---

## Sort Format

- Single field: `property,direction`
  - Example: `name,asc` or `createdAt,desc`
- Multiple fields: `prop1,dir1;prop2,dir2`
  - Example: `name,asc;population,desc`
- Direction values: `asc` (ascending) or `desc` (descending)

---

## Pagination Format

- `offset`: 0-based starting position (e.g., 0, 10, 20)
- `limit`: maximum number of results to return (e.g., 10, 20, 50)
- Page calculation: `pageNumber = offset / limit`

---

## Best Practices

1. **Use findAll() for simple queries**: When no filtering/sorting is needed, use the simple `findAll()` method which can leverage caching
2. **Use search() for complex queries**: When filtering, sorting, or pagination is needed, use the `search()` method
3. **Validate input**: The base class validates offset/limit, but you may want additional validation in your controller
4. **Document your API**: Use Swagger annotations to document the filter syntax and available fields
5. **Field names**: Remember that RSQL uses entity field names, not DTO field names (e.g., `postalCode`, not `postal_code`)

---

## Troubleshooting

### "Did not find a query class QYourEntity"
- Make sure you've run `mvn clean compile` to generate QueryDSL Q-classes
- Check that `querydsl-apt` is configured in your `pom.xml`

### "Cannot convert YourRepository to JpaSpecificationExecutor"
- Make sure your repository extends `JpaSpecificationExecutor<YourEntity>`

### "Invalid filter expression"
- Check that field names match entity field names (case-sensitive)
- Ensure proper RSQL syntax (e.g., `==` not `=`)
- Use quotes for string values with spaces: `name=like="New York"`

### "Invalid sort expression"
- Format must be `property,direction` or `prop1,dir1;prop2,dir2`
- Direction must be `asc` or `desc` (lowercase)
- Property names must match entity field names

---

## Related Documentation

- `RSQL-QUERYDSL-SETUP.md` - QueryDSL and RSQL setup guide
- `LOCATION-FILTERING-EXAMPLES.md` - Comprehensive curl examples
- `IMPLEMENTATION-SUMMARY.md` - Implementation details
- [RSQL JPA Specification GitHub](https://github.com/perplexhub/rsql-jpa-specification)
