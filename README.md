# Geolocation Service

## Abstract

The Geolocation Service is a comprehensive Spring Boot 3.5.6 application designed to manage hierarchical geographic data 
with advanced spatial query capabilities. Built on PostgreSQL with PostGIS extension, it provides RESTful APIs for managing 
countries, regions, cities, areas, zones, locations, and geometric shapes. The service features RSQL-based dynamic filtering, 
flexible sorting, pagination, Redis caching, and powerful spatial operations including proximity searches and geometry-based queries.

---

## Overview

### Key Features

- **Hierarchical Geographic Data Model**: Country → Region → City → Area → Zone → Location
- **Spatial Query Support**: PostGIS-powered spatial operations (proximity, containment, intersection)
- **Dynamic Filtering**: RSQL (RESTful Service Query Language) for flexible, SQL-like filtering
- **Sorting & Pagination**: Multi-field sorting with offset/limit pagination
- **Caching Layer**: Redis-based caching for improved performance
- **RESTful API**: Comprehensive CRUD operations for all geographic entities
- **API Versioning**: Header-based versioning for backward compatibility
- **Code Quality**: Integrated SonarQube for code quality analysis
- **Test Coverage**: Unit and integration tests with JaCoCo coverage reporting

### Technology Stack

- **Framework**: Spring Boot 3.5.6 (Java 21)
- **Database**: PostgreSQL 15 with PostGIS 3.4 extension
- **Cache**: Redis (Alpine)
- **ORM**: Spring Data JPA with Hibernate Spatial
- **Query DSL**: QueryDSL 5.1.0 for type-safe queries
- **Filtering**: RSQL JPA Specification 6.0.33
- **Mapping**: MapStruct 1.6.3 for DTO conversions
- **Migration**: Flyway for database versioning
- **API Documentation**: SpringDoc OpenAPI 3
- **Build Tool**: Maven 3.9.9
- **Code Quality**: SonarQube Community Edition

---

## Architecture

### High-Level Design

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Layer                             │
│                    (REST API Consumers)                          │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Controller Layer                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │   Country    │  │   Location   │  │   GeoShape   │  ...     │
│  │  Controller  │  │  Controller  │  │  Controller  │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│         │                  │                  │                  │
│         │ Exception Handling & DTO Conversion │                 │
└─────────┼──────────────────┼──────────────────┼─────────────────┘
          │                  │                  │
          ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                       Service Layer                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │   Country    │  │   Location   │  │   GeoShape   │  ...     │
│  │   Service    │  │   Service    │  │   Service    │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│         │                  │                  │                  │
│         │    Business Logic & Domain Models   │                 │
│         │    extends BaseService<E, D>        │                 │
└─────────┼──────────────────┼──────────────────┼─────────────────┘
          │                  │                  │
          ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Persistence Layer                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │   Country    │  │   Location   │  │   GeoShape   │  ...     │
│  │  Repository  │  │  Repository  │  │  Repository  │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│         │                  │                  │                  │
│         │  JPA + QueryDSL + RSQL Filtering   │                 │
└─────────┼──────────────────┼──────────────────┼─────────────────┘
          │                  │                  │
          ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Data Layer                                  │
│  ┌──────────────────────┐      ┌──────────────────────┐        │
│  │   PostgreSQL 15      │      │      Redis           │        │
│  │   + PostGIS 3.4      │      │   (Cache Layer)      │        │
│  └──────────────────────┘      └──────────────────────┘        │
└─────────────────────────────────────────────────────────────────┘
```

### Domain Model Hierarchy

```
Country (root)
  └── Region
       └── City
            └── Area
                 └── Zone
                      └── Location (leaf)

GeoShape (independent geometric shapes)
```

### Key Components

1. **Controllers**: Handle HTTP requests, validate input, convert DTOs, manage exceptions
2. **Services**: Implement business logic, extend BaseService for filtering/sorting/pagination
3. **Repositories**: Data access layer with JPA, QueryDSL, and RSQL support
4. **Mappers**: MapStruct-based DTO ↔ Domain ↔ Entity conversions
5. **Domain Models**: Rich domain objects with value objects (VOs)
6. **Entities**: JPA entities with PostGIS spatial types
7. **Cache**: Redis-based caching for frequently accessed data

---

## How to Compile

### Prerequisites

- Java 21 or higher
- Maven 3.9.9 or higher
- Docker & Docker Compose (for running dependencies)

### Compilation Steps

1. **Clone the repository**:
```bash
git clone <repository-url>
cd geolocation-service
```

2. **Start infrastructure services** (PostgreSQL, Redis):
```bash
cd .docker
docker-compose up -d postgis-db-local redis
cd ..
```

3. **Clean and compile**:
```bash
mvn clean compile
```

This will:
- Clean previous builds
- Generate QueryDSL Q-classes
- Generate MapStruct mapper implementations
- Compile all source code

4. **Run tests** (optional):
```bash
# Unit tests only
mvn test

# Integration tests only
mvn verify -DskipUnitTests=true

# All tests with coverage
mvn clean verify -Pcoverage
```

5. **Package the application**:
```bash
mvn clean package
```

This creates:
- `target/geolocation-service.jar` - Standard JAR
- `target/geolocation-service-exec.jar` - Executable Spring Boot JAR

---

## How to Run

### Option 1: Using Maven (Development)

1. **Start infrastructure**:
```bash
cd .docker
docker-compose up -d postgis-db-local redis
cd ..
```

2. **Run the application**:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The service will start on `http://localhost:8080`

### Option 2: Using JAR (Production-like)

1. **Build the JAR**:
```bash
mvn clean package
```

2. **Start infrastructure**:
```bash
cd .docker
docker-compose up -d postgis-db-local redis
cd ..
```

3. **Run the JAR**:
```bash
java -jar target/geolocation-service-exec.jar --spring.profiles.active=local
```

### Option 3: Using Docker Compose (Full Stack)

```bash
cd .docker
docker-compose up -d
cd ..
```

This starts:
- PostgreSQL 15 + PostGIS 3.4 (port 5432)
- PostgreSQL for Integration Tests (port 6432)
- Redis (port 6379)
- SonarQube (port 9000)
- SonarQube Database

### Verify the Service

1. **Health check**:
```bash
curl http://localhost:8080/geolocation-service/management/healthcheck
```

2. **API Documentation**:
Open in browser: `http://localhost:8080/geolocation-service/swagger-ui.html`

3. **Actuator endpoints**:
```bash
curl http://localhost:8080/geolocation-service/management/info
```

### Environment Variables

Key environment variables (see `application.yml` and `application-local.yml`):

- `SERVER_PORT`: Server port (default: 8080)
- `SPRING_PROFILES_ACTIVE`: Active profile (local, dev, prod)
- Database configuration (auto-configured for local profile)
- Redis configuration (auto-configured for local profile)

---

## How to Check SonarQube

### 1. Start SonarQube

```bash
cd .docker
docker-compose up -d sonarqube sonarqube-db
cd ..
```

Wait for SonarQube to start (takes 1-2 minutes). Check status:
```bash
docker logs -f sonarqube-local
```

### 2. Login to SonarQube

1. Open browser: `http://localhost:9000`
2. Default credentials:
   - Username: `admin`
   - Password: `admin`
3. You'll be prompted to change the password on first login

### 3. Generate SonarQube Token

1. Click on your profile icon (top right) → **My Account**
2. Go to **Security** tab
3. Under **Generate Tokens**:
   - Name: `geolocation-service-token`
   - Type: `Global Analysis Token`
   - Expires: Choose expiration (or "No expiration" for local dev)
4. Click **Generate**
5. **Copy the token** (you won't see it again!)

Example token: `squ_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0`

### 4. Run Maven Sonar Analysis


**Option A: Using token in command line**:
```bash
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=geolocation-service \
  -Dsonar.projectName=geolocation-service \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=YOUR_TOKEN_HERE
```

**Option B: Update pom.xml** (already configured):
```xml
<properties>
    <sonar.host.url>http://localhost:9000</sonar.host.url>
    <sonar.token>YOUR_TOKEN_HERE</sonar.token>
    <sonar.projectKey>geolocation-service</sonar.projectKey>
</properties>
```

Then run:
```bash
mvn clean verify sonar:sonar -Pcoverage
```

### 5. View Results

1. Go to `http://localhost:9000`
2. Click on **Projects** → **geolocation-service**
3. View:
   - **Overview**: Quality gate status, coverage, code smells, bugs, vulnerabilities
   - **Issues**: Detailed list of code issues
   - **Measures**: Code metrics (lines, complexity, duplications)
   - **Code**: Browse code with inline issues
   - **Activity**: Historical analysis data

### SonarQube Configuration

The project is pre-configured with:
- **Coverage Plugin**: JaCoCo
- **Coverage Reports**: `target/site/jacoco/jacoco.xml` and `target/site/jacoco-it/jacoco.xml`
- **Minimum Coverage**: 10% (configurable in `pom.xml`)
- **Exclusions**: DTOs, entities, configuration classes, exceptions

---

## Quick Reference: RSQL Filtering

### RSQL Syntax

RSQL (RESTful Service Query Language) allows SQL-like filtering via query parameters.

### Operators

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

### Examples

**Simple filter**:
```
?filters=active==true
```

**Multiple conditions (AND)**:
```
?filters=active==true;population=gt=1000000
```

**Multiple conditions (OR)**:
```
?filters=isoCodeAlpha2==US,isoCodeAlpha2==GB
```

**Pattern matching**:
```
?filters=name=like="United"
```

**In list**:
```
?filters=isoCodeAlpha2=in=(US,GB,FR,DE)
```

**Complex query**:
```
?filters=active==true;(population=gt=1000000,capital=like="New")
```

### Field Names

Use entity field names (camelCase), not JSON field names (snake_case):
- ✅ `postalCode==94102`
- ❌ `postal_code==94102`

---

## Quick Reference: Sorting

### Sort Syntax

**Single field**:
```
?sort=property,direction
```

**Multiple fields** (semicolon-separated):
```
?sort=property1,direction1;property2,direction2
```

### Direction Values

- `asc` - Ascending order
- `desc` - Descending order

### Examples

**Sort by name ascending**:
```
?sort=name,asc
```

**Sort by population descending**:
```
?sort=population,desc
```

**Multiple sort fields**:
```
?sort=isoCodeAlpha2,asc;name,asc
```

**Sort with filters**:
```
?filters=active==true&sort=population,desc
```

---

## Quick Reference: Pagination

### Pagination Parameters

- `offset` - Starting position (0-based)
- `limit` - Maximum number of results

### Examples

**First page (10 items)**:
```
?offset=0&limit=10
```

**Second page**:
```
?offset=10&limit=10
```

**Third page**:
```
?offset=20&limit=10
```

**Custom page size**:
```
?offset=0&limit=50
```

### Combined with Filters and Sort

```
?filters=active==true&sort=name,asc&offset=0&limit=20
```

### Page Calculation

```
pageNumber = offset / limit
totalPages = ceil(totalCount / limit)
```

---

## API Endpoints

### Base URL

```
http://localhost:8080/geolocation-service/api
```

### Required Header

All requests require API version header:
```
Accept-Version: application/vnd.geolocation-service.v1
```

### Endpoints Overview

| Entity | Endpoint | Description |
|--------|----------|-------------|
| Country | `/countries` | Country management |
| Region | `/regions` | Region management |
| City | `/cities` | City management |
| Area | `/areas` | Area management |
| Zone | `/zones` | Zone management |
| Location | `/locations` | Location management + spatial queries |
| GeoShape | `/geoshapes` | Geometric shape management |

---

## Country Endpoints

### Create Country

```bash
curl --location 'http://localhost:8080/geolocation-service/api/countries' \
--header 'Accept-Version: application/vnd.geolocation-service.v1' \
--header 'Content-Type: application/json' \
--data '{
  "name": "United States",
  "iso_code_alpha2": "US",
  "iso_code_alpha3": "USA",
  "iso_code_numeric": "840",
  "phone_code": "+1",
  "currency_code": "USD",
  "capital": "Washington, D.C.",
  "population": 331900000,
  "active": true,
  "metadata": {
    "continent": "North America",
    "region": "Americas"
  }
}'
```

**Response** (201 Created):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "United States",
  "iso_code_alpha2": "US",
  "iso_code_alpha3": "USA",
  "iso_code_numeric": "840",
  "phone_code": "+1",
  "currency_code": "USD",
  "capital": "Washington, D.C.",
  "population": 331900000,
  "active": true,
  "metadata": {
    "continent": "North America",
    "region": "Americas"
  },
  "created_at": "2026-02-16T10:30:00Z",
  "updated_at": "2026-02-16T10:30:00Z",
  "version": 0
}
```

### Get Country by ID

```bash
curl --location 'http://localhost:8080/geolocation-service/api/countries/550e8400-e29b-41d4-a716-446655440000' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

### Get All Countries

```bash
curl --location 'http://localhost:8080/geolocation-service/api/countries' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

### Update Country

```bash
curl --location --request PUT 'http://localhost:8080/geolocation-service/api/countries/550e8400-e29b-41d4-a716-446655440000' \
--header 'Accept-Version: application/vnd.geolocation-service.v1' \
--header 'Content-Type: application/json' \
--data '{
  "name": "United States of America",
  "iso_code_alpha2": "US",
  "iso_code_alpha3": "USA",
  "iso_code_numeric": "840",
  "phone_code": "+1",
  "currency_code": "USD",
  "capital": "Washington, D.C.",
  "population": 332000000,
  "active": true
}'
```

### Delete Country

```bash
curl --location --request DELETE 'http://localhost:8080/geolocation-service/api/countries/550e8400-e29b-41d4-a716-446655440000' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

**Response**: 204 No Content

### Check if Country Exists

```bash
curl --location 'http://localhost:8080/geolocation-service/api/countries/550e8400-e29b-41d4-a716-446655440000/exists' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

**Response**: 200 OK (exists) or 404 Not Found

### Get Country Count

```bash
curl --location 'http://localhost:8080/geolocation-service/api/countries/count' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

**Response**:
```json
195
```

---

## Location Endpoints

### Create Location

```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations' \
--header 'Accept-Version: application/vnd.geolocation-service.v1' \
--header 'Content-Type: application/json' \
--data '{
  "name": "Golden Gate Bridge",
  "address": "Golden Gate Bridge, San Francisco, CA",
  "postal_code": "94129",
  "latitude": 37.8199,
  "longitude": -122.4783,
  "altitude_meters": 67.0,
  "gps_accuracy_meters": 5.0,
  "location_type": "LANDMARK",
  "zone_id": "zone-uuid-here",
  "active": true,
  "metadata": {
    "description": "Iconic suspension bridge",
    "year_built": "1937"
  }
}'
```

### Get Location by ID

```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations/location-uuid-here' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

### Get All Locations

```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

### Update Location

```bash
curl --location --request PUT 'http://localhost:8080/geolocation-service/api/locations/location-uuid-here' \
--header 'Accept-Version: application/vnd.geolocation-service.v1' \
--header 'Content-Type: application/json' \
--data '{
  "name": "Golden Gate Bridge - North Tower",
  "address": "Golden Gate Bridge, San Francisco, CA",
  "postal_code": "94129",
  "latitude": 37.8199,
  "longitude": -122.4783,
  "altitude_meters": 227.0,
  "location_type": "LANDMARK",
  "zone_id": "zone-uuid-here",
  "active": true
}'
```

### Delete Location

```bash
curl --location --request DELETE 'http://localhost:8080/geolocation-service/api/locations/location-uuid-here' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

---

## RSQL Filtering Examples

### Country Filtering

**Active countries only**:
```bash
curl --location 'http://localhost:8080/geolocation-service/api/countries?filters=active==true' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

**Countries by ISO code**:
```bash
curl --location 'http://localhost:8080/geolocation-service/api/countries?filters=isoCodeAlpha2==US' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

**Countries with population > 100 million**:
```bash
curl --location 'http://localhost:8080/geolocation-service/api/countries?filters=population=gt=100000000' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

**Countries by name pattern**:
```bash
curl --location 'http://localhost:8080/geolocation-service/api/countries?filters=name=like="United"' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

**Multiple ISO codes (OR)**:
```bash
curl --location 'http://localhost:8080/geolocation-service/api/countries?filters=isoCodeAlpha2=in=(US,GB,FR,DE)' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

**Active countries with population > 50M (AND)**:
```bash
curl --location 'http://localhost:8080/geolocation-service/api/countries?filters=active==true;population=gt=50000000' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

**Complex filter: Active countries in Europe with population > 10M**:
```bash
curl --location 'http://localhost:8080/geolocation-service/api/countries?filters=active==true;population=gt=10000000' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

### Location Filtering

**Active locations only**:
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations?filters=active==true' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

**Locations by postal code**:
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations?filters=postalCode==94102' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

**Locations by name pattern**:
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations?filters=name=like="Bridge"' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

**Locations by type**:
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations?filters=locationType==LANDMARK' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

**Locations with altitude > 100m**:
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations?filters=altitudeMeters=gt=100' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

**Multiple postal codes (OR)**:
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations?filters=postalCode=in=(94102,94103,94104)' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

**Active locations in specific postal code (AND)**:
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations?filters=active==true;postalCode==94102' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

### Filtering with Sorting

**Active countries sorted by population (descending)**:
```bash
curl --location 'http://localhost:8080/geolocation-service/api/countries?filters=active==true&sort=population,desc' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

**Locations with "Bridge" in name, sorted by name**:
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations?filters=name=like="Bridge"&sort=name,asc' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

### Filtering with Pagination

**First 10 active countries**:
```bash
curl --location 'http://localhost:8080/geolocation-service/api/countries?filters=active==true&offset=0&limit=10' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

**Next 10 active countries**:
```bash
curl --location 'http://localhost:8080/geolocation-service/api/countries?filters=active==true&offset=10&limit=10' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

### Combined: Filter + Sort + Pagination

**Active countries with population > 50M, sorted by name, first 20 results**:
```bash
curl --location 'http://localhost:8080/geolocation-service/api/countries?filters=active==true;population=gt=50000000&sort=name,asc&offset=0&limit=20' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

**Active locations in postal code 94102, sorted by altitude descending, limit 5**:
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations?filters=active==true;postalCode==94102&sort=altitudeMeters,desc&offset=0&limit=5' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

---

## Spatial Query Examples

The Location API provides powerful spatial query capabilities using PostGIS.

### Find Locations Near a Point (Proximity Search)

**Find locations within 1000 meters of a point**:
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations/near?latitude=37.8199&longitude=-122.4783&radiusMeters=1000' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

**Explanation**:
- `latitude`: Latitude of the center point (37.8199 = Golden Gate Bridge)
- `longitude`: Longitude of the center point (-122.4783)
- `radiusMeters`: Search radius in meters (1000m = 1km)

**Find locations within 5km**:
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations/near?latitude=37.7749&longitude=-122.4194&radiusMeters=5000' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

**Find locations within 100 meters (very precise)**:
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations/near?latitude=37.8199&longitude=-122.4783&radiusMeters=100' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

### Find Locations Within a Geometry

The `/within` endpoint accepts WKT (Well-Known Text) format geometries.

#### Polygon (Rectangular Area)

**Find locations within a rectangular bounding box**:
```bash
curl --location --globoff 'http://localhost:8080/geolocation-service/api/locations/within?wkt=POLYGON((-122.52 37.70, -122.35 37.70, -122.35 37.85, -122.52 37.85, -122.52 37.70))' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

**Explanation**:
- `POLYGON((lon1 lat1, lon2 lat2, lon3 lat3, lon4 lat4, lon1 lat1))`
- Coordinates are in `longitude latitude` order (WKT standard)
- First and last points must be the same (closed polygon)
- This example covers San Francisco area

#### Polygon (Custom Shape)

**Find locations within a triangular area**:
```bash
curl --location --globoff 'http://localhost:8080/geolocation-service/api/locations/within?wkt=POLYGON((-122.50 37.75, -122.40 37.75, -122.45 37.82, -122.50 37.75))' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

#### Circle (Using POINT with Buffer)

**Find locations within a circular area** (alternative to `/near`):
```bash
curl --location --globoff 'http://localhost:8080/geolocation-service/api/locations/within?wkt=CIRCLE(-122.4783 37.8199 1000)' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

**Explanation**:
- `CIRCLE(longitude latitude radiusMeters)`
- Space-separated values
- Radius in meters

#### LineString (Along a Route)

**Find locations along a route** (with buffer):
```bash
curl --location --globoff 'http://localhost:8080/geolocation-service/api/locations/within?wkt=LINESTRING(-122.52 37.70, -122.45 37.75, -122.40 37.80)' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

**Explanation**:
- `LINESTRING(lon1 lat1, lon2 lat2, lon3 lat3, ...)`
- Represents a path or route
- Useful for finding locations along highways, trails, etc.

### Complex Spatial Queries

**Find active locations within 2km of Golden Gate Bridge**:
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations/near?latitude=37.8199&longitude=-122.4783&radiusMeters=2000' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

Then filter results with RSQL:
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations?filters=active==true' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

**Find landmarks within downtown San Francisco**:
```bash
curl --location --globoff 'http://localhost:8080/geolocation-service/api/locations/within?wkt=POLYGON((-122.42 37.78, -122.38 37.78, -122.38 37.81, -122.42 37.81, -122.42 37.78))' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

Then filter by type:
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations?filters=locationType==LANDMARK' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

### WKT Format Reference

| Geometry Type | WKT Format | Example |
|---------------|------------|---------|
| Point | `POINT(lon lat)` | `POINT(-122.4783 37.8199)` |
| LineString | `LINESTRING(lon1 lat1, lon2 lat2, ...)` | `LINESTRING(-122.5 37.7, -122.4 37.8)` |
| Polygon | `POLYGON((lon1 lat1, lon2 lat2, ...))` | `POLYGON((-122.5 37.7, -122.4 37.7, -122.4 37.8, -122.5 37.8, -122.5 37.7))` |
| Circle | `CIRCLE(lon lat radius)` | `CIRCLE(-122.4783 37.8199 1000)` |
| MultiPoint | `MULTIPOINT((lon1 lat1), (lon2 lat2))` | `MULTIPOINT((-122.4 37.8), (-122.5 37.7))` |
| MultiLineString | `MULTILINESTRING((lon1 lat1, ...), (...))` | `MULTILINESTRING((-122.5 37.7, -122.4 37.8), (-122.3 37.9, -122.2 38.0))` |
| MultiPolygon | `MULTIPOLYGON(((lon1 lat1, ...)), (...))` | `MULTIPOLYGON(((-122.5 37.7, -122.4 37.7, -122.4 37.8, -122.5 37.7)))` |

### Important Notes

1. **Coordinate Order**: WKT uses `longitude latitude` order (not lat/lon)
2. **Closed Polygons**: First and last points must be identical
3. **URL Encoding**: Use `--globoff` with curl or URL-encode special characters
4. **Spatial Reference**: All coordinates use WGS84 (SRID 4326)
5. **Distance Units**: All distances are in meters

---

## Additional Resources

### Documentation Files
- [Architecture Overview](docs/architecture.md) - Detailed architecture diagrams and explanations
- [Security](docs/security.md) - Overview of security measures and best practices implemented in the service
- [Monitoring](docs/monitoring.md) - Instructions for monitoring the service in production
- [Data Model](docs/data-model.md) - Detailed data model diagrams and explanations
- [Deployment Guide](docs/deployment.md) - Instructions for deploying the service to production environments


- [API Documentation](docs/api.md) - Detailed API reference with examples
- [API Versioning](docs/versioning.md) - Explanation of API versioning strategy and guidelines for adding new versions
- [RSQL Guide](docs/rsql.md) - Comprehensive guide to RSQL filtering
- [Spatial Queries](docs/spatial.md) - In-depth documentation on spatial query capabilities

- [Testing Guide](docs/testing.md) - Instructions for running unit and integration tests
- [Error Handling](docs/error-handling.md) - Explanation of error handling strategy and common error responses
- [Caching Strategy](docs/caching.md) - Explanation of caching strategy and configuration
- [Logging](docs/logging.md) - Overview of logging configuration and best practices

### API Documentation

- Swagger UI: `http://localhost:8080/geolocation-service/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/geolocation-service/v3/api-docs`

### Monitoring & Management

- Health Check: `http://localhost:8080/geolocation-service/management/healthcheck`
- Info: `http://localhost:8080/geolocation-service/management/info`
- Metrics: `http://localhost:8080/geolocation-service/management/metrics`
- All Actuator Endpoints: `http://localhost:8080/geolocation-service/management`

### External Links

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/3.5.6/reference/html/)
- [PostGIS Documentation](https://postgis.net/documentation/)
- [RSQL Parser](https://github.com/jirutka/rsql-parser)
- [QueryDSL Documentation](http://querydsl.com/static/querydsl/latest/reference/html/)
- [MapStruct Documentation](https://mapstruct.org/documentation/stable/reference/html/)

---

## License

This project is licensed under the terms specified in the LICENSE file.

---

## Contact & Support

For questions, issues, or contributions, please refer to the project repository.
