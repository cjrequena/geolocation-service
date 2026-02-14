# Quick Start: Adding Filtering to Your Service

This is a quick reference for adding RSQL filtering, sorting, and pagination to any service using the `BaseService` abstraction.

## 3-Step Process

### Step 1: Update Repository (30 seconds)

Add two interfaces to your repository:

```java
@Repository
public interface YourRepository extends 
    JpaRepository<YourEntity, UUID>,
    JpaSpecificationExecutor<YourEntity>,      // ← Add this
    QuerydslPredicateExecutor<YourEntity> {    // ← Add this
    // ... existing methods
}
```

### Step 2: Update Service (2 minutes)

Make your service extend `BaseService` and implement required methods:

```java
@Service
@RequiredArgsConstructor
public class YourService extends BaseService<YourEntity, YourDomain> {
    
    private final YourRepository repository;
    private final YourMapper mapper;
    
    // ============ Add these 4 methods ============
    
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
    
    // ============ Add this search method ============
    
    public List<YourDomain> search(String filters, Integer offset, Integer limit, String sort) {
        return super.findAllWithFiltersAndSort(filters, offset, limit, sort);
    }
    
    // ... keep all your existing methods unchanged
}
```

### Step 3: Update Controller (2 minutes)

Update your `getAll` endpoint to support query parameters:

```java
@GetMapping
public ResponseEntity<List<YourResponseDTO>> getAll(
    @RequestParam(value = "filters", required = false) String filters,
    @RequestParam(value = "offset", required = false) Integer offset,
    @RequestParam(value = "limit", required = false) Integer limit,
    @RequestParam(value = "sort", required = false) String sort
) {
    try {
        // No params? Use simple findAll (may use cache)
        if (filters == null && offset == null && limit == null && sort == null) {
            return ResponseEntity.ok(
                yourService.findAll().stream()
                    .map(mapper::domainToResponseDTO)
                    .collect(Collectors.toList())
            );
        }
        
        // Has params? Use search (always queries DB)
        return ResponseEntity.ok(
            yourService.search(filters, offset, limit, sort).stream()
                .map(mapper::domainToResponseDTO)
                .collect(Collectors.toList())
        );
    } catch (IllegalArgumentException e) {
        log.error("Invalid request: {}", e.getMessage());
        return ResponseEntity.badRequest().build();
    }
}
```

## Done! 🎉

Your service now supports:
- ✅ RSQL filtering: `?filters=active==true;name=like="test"`
- ✅ Sorting: `?sort=name,asc` or `?sort=name,asc;createdAt,desc`
- ✅ Pagination: `?offset=0&limit=10`
- ✅ Combined: `?filters=active==true&sort=name,asc&offset=0&limit=20`

## Test It

```bash
# No filters (uses your existing findAll)
curl 'http://localhost:8080/api/your-resource'

# With filter
curl 'http://localhost:8080/api/your-resource?filters=active==true'

# With sorting
curl 'http://localhost:8080/api/your-resource?sort=name,asc'

# With pagination
curl 'http://localhost:8080/api/your-resource?offset=0&limit=10'

# Combined
curl 'http://localhost:8080/api/your-resource?filters=active==true&sort=name,asc&offset=0&limit=10'
```

## RSQL Cheat Sheet

```
# Comparison
active==true              # equals
active!=false             # not equals
population=gt=1000000     # greater than
population=ge=1000000     # greater than or equal
population=lt=1000000     # less than
population=le=1000000     # less than or equal

# String matching
name=like="United"        # contains (case-insensitive)

# Lists
code=in=(US,GB,FR)        # in list
code=out=(XX,YY)          # not in list

# Combining
active==true;name=like="test"     # AND (semicolon)
code==US,code==GB                 # OR (comma)
```

## Sort Cheat Sheet

```
name,asc                  # single field ascending
name,desc                 # single field descending
name,asc;createdAt,desc   # multiple fields (semicolon)
```

## Need More Help?

- Full guide: `BASE-SERVICE-GUIDE.md`
- Examples: `LOCATION-FILTERING-EXAMPLES.md`
- Setup: `RSQL-QUERYDSL-SETUP.md`
- Summary: `IMPLEMENTATION-SUMMARY.md`
