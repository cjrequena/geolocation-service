# Location Filtering, Sorting, and Pagination Examples

This document provides curl examples for testing the RSQL filtering, sorting, and pagination features in the Location API.

## Base URL
```
http://localhost:8080/geolocation-service/api/locations
```

## Headers
All requests require:
```
Accept-Version: application/vnd.geolocation-service.v1
```

---

## 1. Basic Queries

### Get all locations (no filters)
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

---

## 2. RSQL Filtering Examples

### Filter by active status
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations?filters=active==true' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

### Filter by postal code
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations?filters=postalCode==94102' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

### Filter by name (partial match with LIKE)
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations?filters=name=like="Bridge"' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

### Multiple filters (AND condition using semicolon)
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations?filters=active==true;postalCode==94102' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

### Multiple filters (OR condition using comma)
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations?filters=postalCode==94102,postalCode==10001' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

### Filter by address containing text
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations?filters=address=like="Street"' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

### Filter by location type
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations?filters=locationType==LANDMARK' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

---

## 3. Sorting Examples

### Sort by name ascending
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations?sort=name,asc' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

### Sort by name descending
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations?sort=name,desc' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

### Sort by created date (newest first)
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations?sort=createdAt,desc' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

### Multiple sort fields (sort by postal code, then by name)
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations?sort=postalCode,asc;name,asc' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

---

## 4. Pagination Examples

### Get first 10 results
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations?offset=0&limit=10' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

### Get next 10 results (page 2)
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations?offset=10&limit=10' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

### Get 5 results starting from offset 20
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations?offset=20&limit=5' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

---

## 5. Combined Examples (Filtering + Sorting + Pagination)

### Active locations, sorted by name, first 10 results
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations?filters=active==true&sort=name,asc&offset=0&limit=10' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

### Locations in postal code 94102, sorted by created date (newest first), limit 5
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations?filters=postalCode==94102&sort=createdAt,desc&offset=0&limit=5' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

### Locations with "Bridge" in name, active only, sorted by name, paginated
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations?filters=name=like="Bridge";active==true&sort=name,asc&offset=0&limit=20' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

### Complex filter: active locations with altitude info, sorted by altitude descending
```bash
curl --location 'http://localhost:8080/geolocation-service/api/locations?filters=active==true;altitudeMeters!=null&sort=altitudeMeters,desc&offset=0&limit=10' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

---

## 6. RSQL Operators Reference

| Operator | Description | Example |
|----------|-------------|---------|
| `==` | Equal | `active==true` |
| `!=` | Not equal | `active!=false` |
| `=lt=` | Less than | `altitudeMeters=lt=1000` |
| `=le=` | Less than or equal | `altitudeMeters=le=1000` |
| `=gt=` | Greater than | `altitudeMeters=gt=500` |
| `=ge=` | Greater than or equal | `altitudeMeters=ge=500` |
| `=like=` | Like (case-insensitive) | `name=like="Bridge"` |
| `=in=` | In list | `postalCode=in=(94102,10001,90210)` |
| `=out=` | Not in list | `postalCode=out=(00000,99999)` |
| `;` | AND condition | `active==true;postalCode==94102` |
| `,` | OR condition | `postalCode==94102,postalCode==10001` |

---

## 7. Sort Format

- Single field: `property,direction`
  - Example: `name,asc` or `createdAt,desc`
- Multiple fields: `prop1,dir1;prop2,dir2`
  - Example: `postalCode,asc;name,asc`
- Direction values: `asc` (ascending) or `desc` (descending)

---

## 8. Pagination Format

- `offset`: 0-based starting position (e.g., 0, 10, 20)
- `limit`: maximum number of results to return (e.g., 10, 20, 50)

---

## 9. Error Handling

### Invalid filter expression
```bash
# This will return 400 Bad Request
curl --location 'http://localhost:8080/geolocation-service/api/locations?filters=invalid_field==value' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

### Invalid sort expression
```bash
# This will return 400 Bad Request (missing direction)
curl --location 'http://localhost:8080/geolocation-service/api/locations?sort=name' \
--header 'Accept-Version: application/vnd.geolocation-service.v1'
```

---

## Notes

1. All query parameters are optional
2. Filters use RSQL syntax (see RSQL-QUERYDSL-SETUP.md for more details)
3. Sort expressions are semicolon-separated for multiple fields
4. Pagination uses offset/limit (not page/size)
5. The response is always a JSON array of location objects (not a Page object)
6. Field names in filters and sort must match entity field names (e.g., `postalCode`, not `postal_code`)
