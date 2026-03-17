# Package Structure & Naming Conventions

## Package Tree Structure

```
com.cjrequena.sample
├── configuration                          # Spring configuration beans
├── controller                             # REST controllers (API layer)
│   ├── dto                                # Data Transfer Objects (request/response)
│   └── exception                          # Controller-level exceptions & error handling
├── domain                                 # Domain/business layer
│   ├── exception                          # Domain-level exceptions
│   ├── mapper                             # MapStruct mappers (DTO ↔ Domain ↔ Entity)
│   └── model                              # Domain models
│       ├── enums                          # Enumerations
│       └── vo                             # Value Objects
├── persistence                            # Data access layer
│   ├── entity                             # JPA entities
│   └── repository                         # Spring Data JPA repositories
│       └── cache                          # Redis cache repositories
├── service                                # Service layer (business logic)
│   └── base                               # Base/abstract service classes
└── shared                                 # Cross-cutting concerns
    └── common
        └── util                           # Utility classes
```

### Test Package Structure

```
com.cjrequena.sample
├── configuration                          # Test configuration
├── domain
│   └── mapper                             # Mapper unit tests & integration tests
├── persistence
│   ├── entity                             # Entity unit tests
│   └── repository                         # Repository integration tests
└── service                                # Service unit tests & integration tests
```

---

## Class Naming Conventions

| Layer | Pattern | Examples |
|---|---|---|
| Configuration | `{Purpose}Configuration` / `{Purpose}ConfigurationProperties` | `RedisConfiguration`, `CacheConfigurationProperties` |
| Controller | `{Entity}Controller` | `CountryController`, `LocationController` |
| Request DTO | `{Entity}RequestDTO` | `CountryRequestDTO`, `LocationRequestDTO` |
| Response DTO | `{Entity}ResponseDTO` | `CountryResponseDTO`, `LocationResponseDTO` |
| Controller Exception | `{HttpStatus}Exception` | `NotFoundException`, `ConflictException`, `BadRequestException` |
| Domain Model | `{Entity}` | `Country`, `Location`, `GeoShape` |
| Domain Exception | `{Entity}{Reason}Exception` | `CountryNotFoundException`, `UniqueConstraintException` |
| Value Object | `{Concept}VO` | `CoordinateVO`, `AltitudeVO`, `MetadataVO` |
| Enum | `{Concept}Type` | `LocationType`, `GeometryType`, `ZoneType` |
| Mapper | `{Entity}Mapper` | `CountryMapper`, `LocationMapper` |
| JPA Entity | `{Entity}Entity` | `CountryEntity`, `LocationEntity` |
| Repository | `{Entity}Repository` | `CountryRepository`, `LocationRepository` |
| Cache Repository | `{Entity}CacheRedisHashOpsRepository` | `CountryCacheRedisHashOpsRepository` |
| Service | `{Entity}Service` | `CountryService`, `LocationService` |
| Base Service | `Base{Role}` | `BaseService` |
| Utility | `{Purpose}Util` / `{Purpose}Utils` | `JsonUtil`, `SortUtils`, `WKTParserUtil` |
| Unit Test | `{Class}Test` | `CountryServiceTest`, `CountryEntityTest` |
| Integration Test | `{Class}IT` | `CountryServiceIT`, `CountryRepositoryIT` |

---

## Layer Flow

The same entity name flows consistently through all layers:

```
CountryController → CountryService → CountryRepository → CountryEntity
       ↕                  ↕
 CountryRequestDTO    Country (domain)
 CountryResponseDTO       ↕
                     CountryMapper
```
