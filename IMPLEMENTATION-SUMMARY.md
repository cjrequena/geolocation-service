# RSQL Filtering, Sorting, and Pagination Implementation Summary

## What Was Implemented

Created a reusable `BaseService` abstraction that provides RSQL-based filtering, sorting, and pagination support to any service. Implemented in `LocationService` and `CountryService` as examples.

## Architecture

```
BaseService<EntityType, DomainType> (Abstract)
    ↑
    ├── LocationService extends BaseService<LocationEntity, Location>
    └── CountryService extends BaseService<CountryEntity, Country>
```

## Files Created/Modified

### 1. Created: `BaseService.java`
**Path:** `src/main/java/com/cjrequena/sample/service/base/BaseService.java`

Abstract base class providing reusable filtering, sorting, and pagination functionality.

**Features:**
- Generic implementation for any entity/domain pair
- RSQL filter parsing using `RSQLJPASupport.toSpecification()`
- Sort expression parsing using `SortUtils.parseSort()`
- Pagination with offset/limit
- Count operations with filters
- Comprehensive error handling and validation
- Logging for debugging

**Abstract Methods (must be implemented by subclasses):**
- `getRepository()` - Returns the JPA repository
- `getSpecificationExecutor()` - Returns the specification executor (usually same as repository)
- `getEntityToDomainMapper()` - Returns the entity-to-domain mapper function
- `getEntityClass()` - Returns the entity class

**Protected Methods (available to subclasses):**
- `findAllWithFiltersAndSort(filters, offset, limit, sort)` - Main query method
- `countWithFilters(filters)` - Count with optional filters

### 2. Created: `SortUtils.java`
**Path:** `src/main/java/com/cjrequena/sample/shared/common/util/SortUtils.java`

Utility class for parsing sort expressions into Spring Data Sort objects.

**Features:**
- Parses single field sort: `"name,asc"`
- Parses multiple field sort: `"name,asc;createdAt,desc"`
- Validates sort direction (asc/desc)
- Returns `Sort.unsorted()` for null/empty expressions
- Throws `IllegalArgumentException` for invalid formats

### 3. Modified: `LocationService.java`
**Path:** `src/main/java/com/cjrequena/sample/service/LocationService.java`

Extended to use `BaseService<LocationEntity, Location>`.

**Changes:**
- Now extends `BaseService<LocationEntity, Location>`
- Implements four required abstract methods
- Added `search(filters, offset, limit, sort)` method that delegates to base class
- Kept original `findAll()` method unchanged (uses cache)
- Maintains all existing CRUD and spatial query methods

### 4. Modified: `CountryService.java`
**Path:** `src/main/java/com/cjrequena/sample/service/CountryService.java`

Extended to use `BaseService<CountryEntity, Country>`.

**Changes:**
- Now extends `BaseService<CountryEntity, Country>`
- Implements four required abstract methods
- Added `search(filters, offset, limit, sort)` method that delegates to base class
- Kept original `findAll()` method unchanged (uses cache)
- Maintains all existing CRUD and query methods

### 5. Modified: `LocationController.java`
**Path:** `src/main/java/com/cjrequena/sample/controller/LocationController.java`

Updated `getAllLocations()` method to use both `findAll()` and `search()`.

**Changes:**
- If no query parameters provided → uses `findAll()` (may use cache)
- If any query parameters provided → uses `search()` (always queries database)
- Better error handling with `IllegalArgumentException` catch
- Maintains backward compatibility

### 6. Modified: `CountryRepository.java`
**Path:** `src/main/java/com/cjrequena/sample/persistence/repository/CountryRepository.java`

Added required interfaces for RSQL support.

**Changes:**
- Now extends `JpaSpecificationExecutor<CountryEntity>`
- Now extends `QuerydslPredicateExecutor<CountryEntity>`
- Maintains all existing query methods

### 7. Created: `BASE-SERVICE-GUIDE.md`
Comprehensive guide on how to use the `BaseService` abstraction in other services.

### 8. Created: `LOCATION-FILTERING-EXAMPLES.md`
Comprehensive documentation with curl examples for testing the new features.

### 9. Updated: `IMPLEMENTATION-SUMMARY.md`
This file - updated to reflect the reusable architecture.

---

## How to Add to Other Services

To add filtering/sorting/pagination to any service (e.g., `AreaService`, `CityService`, etc.):

### Step 1: Update Repository
```java
@Repository
public interface YourRepository extends 
    JpaRepository<YourEntity, UUID>, 
    JpaSpecificationExecutor<YourEntity>,
    QuerydslPredicateExecutor<YourEntity> {
    // existing methods
}
```

### Step 2: Extend BaseService
```java
@Service
@RequiredArgsConstructor
public class YourService extends BaseService<YourEntity, YourDomain> {
    
    private final YourRepository repository;
    private final YourMapper mapper;
    
    // Implement 4 required methods
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
    
    // Keep existing findAll()
    public List<YourDomain> findAll() {
        // your existing implementation
    }
    
    // Add search method
    public List<YourDomain> search(String filters, Integer offset, Integer limit, String sort) {
        return super.findAllWithFiltersAndSort(filters, offset, limit, sort);
    }
    
    // ... other methods
}
```

### Step 3: Update Controller
```java
@GetMapping
public ResponseEntity<List<YourResponseDTO>> getAll(
    @RequestParam(required = false) String filters,
    @RequestParam(required = false) Integer offset,
    @RequestParam(required = false) Integer limit,
    @RequestParam(required = false) String sort
) {
    try {
        if (filters == null && offset == null && limit == null && sort == null) {
            return ResponseEntity.ok(
                yourService.findAll().stream()
                    .map(mapper::domainToResponseDTO)
                    .collect(Collectors.toList())
            );
        }
        
        return ResponseEntity.ok(
            yourService.search(filters, offset, limit, sort).stream()
                .map(mapper::domainToResponseDTO)
                .collect(Collectors.toList())
        );
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().build();
    }
}
```

---

## API Usage

### Endpoint
```
GET /geolocation-service/api/{resource}
```

### Query Parameters

| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| `filters` | String | No | RSQL filter expression | `active==true;postalCode==94102` |
| `offset` | Integer | No | Pagination offset (0-based) | `0`, `10`, `20` |
| `limit` | Integer | No | Maximum results | `10`, `20`, `50` |
| `sort` | String | No | Sort expression | `name,asc` or `name,asc;createdAt,desc` |

### RSQL Operators

- `==` : Equal
- `!=` : Not equal
- `=lt=` : Less than
- `=le=` : Less than or equal
- `=gt=` : Greater than
- `=ge=` : Greater than or equal
- `=like=` : Like (case-insensitive)
- `=in=` : In list
- `=out=` : Not in list
- `;` : AND condition
- `,` : OR condition

### Sort Format

- Single field: `property,direction` (e.g., `name,asc`)
- Multiple fields: `prop1,dir1;prop2,dir2` (e.g., `name,asc;createdAt,desc`)
- Directions: `asc` or `desc`

---

## Example Requests

### No filters (uses cached findAll)
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

### Filter active locations
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations?filters=active==true' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

### Sort by name ascending
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations?sort=name,asc' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

### Paginate (first 10 results)
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations?offset=0&limit=10' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

### Combined: filter + sort + paginate
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations?filters=active==true&sort=name,asc&offset=0&limit=10' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

---

## Benefits of This Approach

1. **Reusability**: Write once, use everywhere - just extend `BaseService`
2. **Consistency**: All services have the same API for filtering, sorting, and pagination
3. **Maintainability**: Changes to base logic automatically apply to all services
4. **Type Safety**: Generic types ensure compile-time type checking
5. **Backward Compatibility**: Existing `findAll()` methods remain unchanged
6. **Performance**: Can optimize caching strategy per service (findAll vs search)
7. **Easy to Extend**: Adding to new services requires minimal code (4 methods + 1 search method)

---

## Testing

1. Start the application
2. Use the curl examples in `LOCATION-FILTERING-EXAMPLES.md`
3. Verify filtering, sorting, and pagination work correctly
4. Test error handling with invalid expressions
5. Test that `findAll()` without parameters still works (backward compatibility)

---

## Dependencies

The implementation uses:
- `rsql-jpa-spring-boot-starter` (already configured in pom.xml)
- `querydsl-jpa` and `querydsl-apt` (already configured in pom.xml)
- Spring Data JPA Specifications
- Spring Data Sort and Pageable

---

## Services Status

| Service | BaseService | Repository Updated | Search Method | Status |
|---------|-------------|-------------------|---------------|--------|
| LocationService | ✅ | ✅ | ✅ | Complete |
| CountryService | ✅ | ✅ | ✅ | Complete |
| AreaService | ❌ | ❌ | ❌ | Pending |
| CityService | ❌ | ❌ | ❌ | Pending |
| RegionService | ❌ | ❌ | ❌ | Pending |
| ZoneService | ❌ | ❌ | ❌ | Pending |
| GeoShapeService | ❌ | ❌ | ❌ | Pending |

---

## Notes

1. Field names in filters and sort must match entity field names (e.g., `postalCode`, not `postal_code`)
2. The response is always a JSON array (not a Page object)
3. All query parameters are optional
4. Invalid filter or sort expressions return 400 Bad Request
5. The `search()` method does not use cache (always queries database for accuracy)
6. The `findAll()` method can use cache (implementation-specific)

---

## Related Documentation

- `BASE-SERVICE-GUIDE.md` - Complete guide on using BaseService
- `RSQL-QUERYDSL-SETUP.md` - QueryDSL and RSQL setup guide
- `LOCATION-FILTERING-EXAMPLES.md` - Comprehensive curl examples
- [RSQL JPA Specification GitHub](https://github.com/perplexhub/rsql-jpa-specification)
