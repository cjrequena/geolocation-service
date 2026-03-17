# Package Structure & Naming Conventions

## Project Root Structure

```
{project-name}/
├── .docker/                               # Docker infrastructure
│   ├── provision/                         # Provisioning scripts & seed data
│   ├── .env                               # Docker environment variables
│   └── docker-compose.yml                 # Docker Compose services definition
├── docs/                                  # Project documentation (markdown)
├── src/
│   ├── main/
│   │   ├── java/                          # Application source code
│   │   └── resources/
│   │       ├── db/
│   │       │   └── migration/             # Flyway database migration scripts
│   │       ├── application.yml            # Default application configuration
│   │       └── application-{profile}.yml  # Profile-specific configuration
│   └── test/
│       ├── java/                          # Test source code
│       └── resources/                     # Test resources
├── .gitignore                             # Git ignore rules
├── LICENSE                                # Project license
├── pom.xml                                # Maven project descriptor
└── README.md                              # Project documentation entry point
```

---

## Package Tree Structure

```
{groupId}.{project}
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
│       └── cache                          # Cache repositories (e.g. Redis)
├── service                                # Service layer (business logic)
│   └── base                               # Base/abstract service classes
└── shared                                 # Cross-cutting concerns
    └── common
        └── util                           # Utility classes
```

### Test Package Structure

```
{groupId}.{project}
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

In the table below, `{Entity}` represents any domain concept (e.g. `Order`, `Customer`, `Product`).

| Layer | Pattern | Examples |
|---|---|---|
| Configuration | `{Purpose}Configuration` / `{Purpose}ConfigurationProperties` | `SecurityConfiguration`, `CacheConfigurationProperties` |
| Controller | `{Entity}Controller` | `OrderController`, `CustomerController` |
| Request DTO | `{Entity}RequestDTO` | `OrderRequestDTO`, `CustomerRequestDTO` |
| Response DTO | `{Entity}ResponseDTO` | `OrderResponseDTO`, `CustomerResponseDTO` |
| Controller Exception | `{HttpStatus}Exception` | `NotFoundException`, `ConflictException`, `BadRequestException` |
| Domain Model | `{Entity}` | `Order`, `Customer`, `Product` |
| Domain Exception | `{Entity}{Reason}Exception` | `OrderNotFoundException`, `UniqueConstraintException` |
| Value Object | `{Concept}VO` | `AddressVO`, `MoneyVO`, `DateRangeVO` |
| Enum | `{Concept}Type` | `OrderType`, `StatusType`, `PriorityType` |
| Mapper | `{Entity}Mapper` | `OrderMapper`, `CustomerMapper` |
| JPA Entity | `{Entity}Entity` | `OrderEntity`, `CustomerEntity` |
| Repository | `{Entity}Repository` | `OrderRepository`, `CustomerRepository` |
| Cache Repository | `{Entity}Cache{Provider}Repository` | `OrderCacheRedisRepository` |
| Service | `{Entity}Service` | `OrderService`, `CustomerService` |
| Base Service | `Base{Role}` | `BaseService`, `BaseRepository` |
| Utility | `{Purpose}Util` / `{Purpose}Utils` | `JsonUtil`, `DateUtils`, `StringUtil` |
| Unit Test | `{Class}Test` | `OrderServiceTest`, `OrderEntityTest` |
| Integration Test | `{Class}IT` | `OrderServiceIT`, `OrderRepositoryIT` |

---

## Layer Flow

The same entity name flows consistently through all layers:

```
{Entity}Controller → {Entity}Service → {Entity}Repository → {Entity}Entity
       ↕                   ↕
 {Entity}RequestDTO    {Entity} (domain model)
 {Entity}ResponseDTO       ↕
                      {Entity}Mapper
```
