# RSQL and QueryDSL Setup

## Overview

Successfully configured RSQL (RESTful Service Query Language) with QueryDSL support for dynamic filtering and querying in the geolocation service.

## What was done

### 1. Added Dependencies

Added to `pom.xml`:

```xml
<properties>
    <querydsl.version>5.1.0</querydsl.version>
</properties>

<dependencies>
    <!-- RSQL JPA Spring Boot Starter -->
    <dependency>
        <groupId>io.github.perplexhub</groupId>
        <artifactId>rsql-jpa-spring-boot-starter</artifactId>
        <version>6.0.33</version>
    </dependency>
    
    <!-- RSQL QueryDSL -->
    <dependency>
        <groupId>io.github.perplexhub</groupId>
        <artifactId>rsql-querydsl</artifactId>
        <version>6.0.33</version>
    </dependency>
    
    <!-- QueryDSL JPA for Jakarta -->
    <dependency>
        <groupId>com.querydsl</groupId>
        <artifactId>querydsl-jpa</artifactId>
        <version>${querydsl.version}</version>
        <classifier>jakarta</classifier>
    </dependency>
    
    <!-- QueryDSL APT for code generation -->
    <dependency>
        <groupId>com.querydsl</groupId>
        <artifactId>querydsl-apt</artifactId>
        <version>${querydsl.version}</version>
        <classifier>jakarta</classifier>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### 2. Configured Annotation Processing

Updated `maven-compiler-plugin` to generate Q-classes:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <!-- Lombok must be first -->
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
            </path>
            <!-- MapStruct -->
            <path>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct-processor</artifactId>
                <version>${mapstruct.version}</version>
            </path>
            <!-- QueryDSL APT for Q-class generation -->
            <path>
                <groupId>com.querydsl</groupId>
                <artifactId>querydsl-apt</artifactId>
                <version>${querydsl.version}</version>
                <classifier>jakarta</classifier>
            </path>
            <!-- Jakarta Persistence API -->
            <path>
                <groupId>jakarta.persistence</groupId>
                <artifactId>jakarta.persistence-api</artifactId>
                <version>3.1.0</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

### 3. Repository Configuration

The `LocationRepository` already extends the required interfaces:

```java
public interface LocationRepository extends 
    JpaRepository<LocationEntity, UUID>,
    JpaSpecificationExecutor<LocationEntity>,
    QuerydslPredicateExecutor<LocationEntity> {
    // Custom methods
}
```

### 4. Generated Q-Classes

After running `mvn clean compile`, the following Q-classes were generated in `target/generated-sources/annotations/`:

- `QLocationEntity.java`
- `QZoneEntity.java`
- `QAreaEntity.java`
- `QCityEntity.java`
- `QRegionEntity.java`
- `QCountryEntity.java`
- `QGeoShapeEntity.java`

## Usage

### RSQL Query Examples

RSQL allows filtering via query parameters using a SQL-like syntax:

#### Basic Filtering

```bash
# Find active locations
GET /api/v1/locations?filter=active==true

# Find locations by postal code
GET /api/v1/locations?filter=postalCode==94102

# Find locations by name (contains)
GET /api/v1/locations?filter=name=like='Golden Gate'
```

#### Comparison Operators

```bash
# Equal
filter=active==true

# Not equal
filter=active!=true

# Greater than
filter=altitudeMeters>100

# Greater than or equal
filter=altitudeMeters>=100

# Less than
filter=altitudeMeters<500

# Less than or equal
filter=altitudeMeters<=500

# Like (contains)
filter=name=like='Bridge'

# In (multiple values)
filter=postalCode=in=(94102,94103,94104)

# Not in
filter=postalCode=out=(94102,94103)
```

#### Logical Operators

```bash
# AND
filter=active==true;postalCode==94102

# OR
filter=active==true,postalCode==94102

# Complex
filter=(active==true;altitudeMeters>50),(postalCode==94102)
```

#### Nested Properties

```bash
# Filter by zone name
filter=zone.name=='North Zone'

# Filter by zone active status
filter=zone.active==true

# Filter by area city name
filter=zone.area.city.name=='San Francisco'
```

### QueryDSL Programmatic Queries

You can also use QueryDSL programmatically in your service layer:

```java
@Service
public class LocationService {
    
    @Autowired
    private LocationRepository locationRepository;
    
    public List<Location> findActiveLocationsInZone(UUID zoneId) {
        QLocationEntity location = QLocationEntity.locationEntity;
        
        BooleanExpression predicate = location.active.isTrue()
            .and(location.zone.id.eq(zoneId));
        
        return locationRepository.findAll(predicate);
    }
    
    public List<Location> findLocationsWithHighAltitude(BigDecimal minAltitude) {
        QLocationEntity location = QLocationEntity.locationEntity;
        
        return locationRepository.findAll(
            location.altitudeMeters.goe(minAltitude)
        );
    }
}
```

### Controller Integration

Add RSQL filtering to your controller:

```java
@GetMapping
public ResponseEntity<List<LocationResponseDTO>> searchLocations(
    @RequestParam(required = false) String filter,
    Pageable pageable
) {
    Specification<LocationEntity> spec = RSQLSupport.toSpecification(filter);
    Page<LocationEntity> page = locationRepository.findAll(spec, pageable);
    
    List<LocationResponseDTO> dtos = page.getContent().stream()
        .map(locationMapper::toResponseDTO)
        .collect(Collectors.toList());
    
    return ResponseEntity.ok(dtos);
}
```

## Testing

### Test RSQL Queries

```bash
# Find active locations
curl "http://localhost:8080/geolocation-service/api/locations/?filter=active==true"

# Find locations by postal code
curl "http://localhost:8080/geolocation-service/api/locations/?filter=postalCode==94102"

# Find locations with altitude > 50m
curl "http://localhost:8080/geolocation-service/api/locations/?filter=altitudeMeters>50"

# Complex query: active locations in specific postal codes
curl "http://localhost:8080/geolocation-service/api/locations/?filter=active==true;postalCode=in=(94102,94103)"

# Search by name (contains)
curl "http://localhost:8080/geolocation-service/api/locations/?filter=name=like='Bridge'"

# Filter by zone
curl "http://localhost:8080/geolocation-service/api/locations/?filter=zone.name=='North Zone'"
```

### With Pagination and Sorting

```bash
# Page 0, size 10, sorted by name
curl "http://localhost:8080/geolocation-service/api/locations/?filter=active==true&page=0&size=10&sort=name,asc"

# Multiple sort fields
curl "http://localhost:8080/geolocation-service/api/locations/?filter=active==true&sort=postalCode,asc&sort=name,asc"
```

## RSQL Operators Reference

| Operator | Description | Example |
|----------|-------------|---------|
| `==` | Equal | `name=='Golden Gate'` |
| `!=` | Not equal | `active!=false` |
| `>` | Greater than | `altitudeMeters>100` |
| `>=` | Greater or equal | `altitudeMeters>=100` |
| `<` | Less than | `altitudeMeters<500` |
| `<=` | Less or equal | `altitudeMeters<=500` |
| `=in=` or `=in=(...)` | In list | `postalCode=in=(94102,94103)` |
| `=out=` or `=out=(...)` | Not in list | `postalCode=out=(94102)` |
| `=like=` or `=like='...'` | Like (contains) | `name=like='Bridge'` |
| `;` | AND | `active==true;postalCode==94102` |
| `,` | OR | `active==true,postalCode==94102` |
| `()` | Grouping | `(active==true;altitude>50),(postalCode==94102)` |

## Benefits

1. **Dynamic Filtering**: Clients can filter data without backend changes
2. **Type Safety**: QueryDSL provides compile-time type checking
3. **Reduced Boilerplate**: No need to write custom query methods for every filter combination
4. **Flexible Queries**: Support for complex AND/OR conditions
5. **Nested Properties**: Filter by related entity properties
6. **Pagination Support**: Works seamlessly with Spring Data pagination
7. **Sorting Support**: Combine filtering with sorting

## IDE Configuration

### IntelliJ IDEA

1. Right-click on `target/generated-sources/annotations`
2. Select "Mark Directory as" → "Generated Sources Root"
3. The Q-classes will now be recognized by the IDE

### Eclipse

1. Right-click on project → Properties
2. Java Build Path → Source
3. Add `target/generated-sources/annotations` as source folder

## Troubleshooting

### Q-classes not found

Run:
```bash
mvn clean compile
```

### IDE not recognizing Q-classes

Mark `target/generated-sources/annotations` as generated sources root in your IDE.

### Compilation errors after adding QueryDSL

Ensure annotation processor order is correct:
1. Lombok (must be first)
2. MapStruct
3. QueryDSL APT
4. Jakarta Persistence API

## References

- [RSQL JPA Specification](https://github.com/perplexhub/rsql-jpa-specification)
- [QueryDSL Documentation](http://querydsl.com/static/querydsl/latest/reference/html/)
- [RSQL Syntax](https://github.com/jirutka/rsql-parser)

## Next Steps

1. Add RSQL filtering to all controllers
2. Document available filter fields in API documentation
3. Add validation for filter expressions
4. Consider adding custom RSQL operators for spatial queries
5. Add integration tests for RSQL queries
