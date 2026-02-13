# LocationController - cURL Testing Examples

Complete guide to test LocationController endpoints with proper hierarchy setup.

## Prerequisites

Start the application:
```bash
cd geolocation-command-handler
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Base URL: `http://localhost:8080`

---

## Step 1: Create the Hierarchy

Locations require the full hierarchy: Country → Region → City → Area → Zone

### 1.1 Create Country

```bash
curl -X POST http://localhost:8080/api/v1/countries \
  -H "Content-Type: application/json" \
  -d '{
    "name": "United States",
    "isoCodeAlpha2": "US",
    "isoCodeAlpha3": "USA",
    "isoCodeNumeric": "840",
    "phoneCode": "+1",
    "currencyCode": "USD",
    "capital": "Washington D.C.",
    "population": 331002651,
    "active": true
  }'
```

**Save the returned `id` as `COUNTRY_ID`**

### 1.2 Create Region

```bash
curl -X POST http://localhost:8080/api/v1/regions \
  -H "Content-Type: application/json" \
  -d '{
    "countryId": "COUNTRY_ID",
    "name": "California",
    "code": "US-CA",
    "regionType": "STATE",
    "population": 39538223,
    "timeZone": "America/Los_Angeles",
    "active": true
  }'
```

**Save the returned `id` as `REGION_ID`**

### 1.3 Create City

```bash
curl -X POST http://localhost:8080/api/v1/cities \
  -H "Content-Type: application/json" \
  -d '{
    "regionId": "REGION_ID",
    "name": "San Francisco",
    "population": 873965,
    "timeZone": "America/Los_Angeles",
    "postalCode": "94102",
    "capital": false,
    "active": true
  }'
```

**Save the returned `id` as `CITY_ID`**

### 1.4 Create Area

```bash
curl -X POST http://localhost:8080/api/v1/areas \
  -H "Content-Type: application/json" \
  -d '{
    "cityId": "CITY_ID",
    "name": "Golden Gate Park",
    "areaType": "DISTRICT",
    "population": 50000,
    "postalCode": "94121",
    "active": true
  }'
```

**Save the returned `id` as `AREA_ID`**

### 1.5 Create Zone

```bash
curl -X POST http://localhost:8080/api/v1/zones \
  -H "Content-Type: application/json" \
  -d '{
    "areaId": "AREA_ID",
    "name": "North Zone",
    "zoneType": "SECTOR",
    "postalCode": "94121",
    "active": true
  }'
```

**Save the returned `id` as `ZONE_ID`**

---

## Step 2: Create Locations

### 2.1 Create Location with Full Details

```bash
curl -X POST http://localhost:8080/api/v1/locations \
  -H "Content-Type: application/json" \
  -d '{
    "zoneId": "ZONE_ID",
    "name": "Golden Gate Bridge Viewpoint",
    "locationType": "LANDMARK",
    "latitude": 37.8199,
    "longitude": -122.4783,
    "altitudeMeters": 67.5,
    "accuracyMeters": 3.2,
    "address": "Golden Gate Bridge, San Francisco, CA",
    "postalCode": "94129",
    "active": true,
    "metadata": {
      "type": "viewpoint",
      "accessibility": "public",
      "parking": true
    }
  }'
```

**Expected Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "latitude": 37.8199,
  "longitude": -122.4783,
  "altitudeMeters": 67.5,
  "accuracyMeters": 3.2,
  "address": "Golden Gate Bridge, San Francisco, CA",
  "postalCode": "94129",
  "zone": {
    "id": "...",
    "name": "North Zone"
  },
  "metadata": {
    "type": "viewpoint",
    "accessibility": "public",
    "parking": true
  },
  "active": true,
  "createdAt": "2024-02-12T10:30:00Z",
  "updatedAt": "2024-02-12T10:30:00Z"
}
```

**Save the returned `id` as `LOCATION_ID`**

### 2.2 Create More Locations for Testing

```bash
# Alcatraz Island
curl -X POST http://localhost:8080/api/v1/locations \
  -H "Content-Type: application/json" \
  -d '{
    "zoneId": "ZONE_ID",
    "name": "Alcatraz Island",
    "locationType": "LANDMARK",
    "latitude": 37.8267,
    "longitude": -122.4230,
    "address": "Alcatraz Island, San Francisco, CA",
    "postalCode": "94133",
    "active": true
  }'

# Fishermans Wharf
curl -X POST http://localhost:8080/api/v1/locations \
  -H "Content-Type: application/json" \
  -d '{
    "zoneId": "ZONE_ID",
    "name": "Fishermans Wharf",
    "locationType": "COMMERCIAL",
    "latitude": 37.8080,
    "longitude": -122.4177,
    "address": "Fishermans Wharf, San Francisco, CA",
    "postalCode": "94133",
    "active": true
  }'

# Coit Tower
curl -X POST http://localhost:8080/api/v1/locations \
  -H "Content-Type: application/json" \
  -d '{
    "zoneId": "ZONE_ID",
    "name": "Coit Tower",
    "locationType": "LANDMARK",
    "latitude": 37.8024,
    "longitude": -122.4058,
    "altitudeMeters": 85.0,
    "address": "1 Telegraph Hill Blvd, San Francisco, CA",
    "postalCode": "94133",
    "active": true
  }'
```

---

## Step 3: Query Locations

### 3.1 Get Location by ID

```bash
curl -X GET http://localhost:8080/api/v1/locations/LOCATION_ID
```

### 3.2 Get All Locations

```bash
curl -X GET http://localhost:8080/api/v1/locations
```

### 3.3 Get Locations by Zone

```bash
curl -X GET http://localhost:8080/api/v1/locations/zone/ZONE_ID
```

### 3.4 Get Locations with Pagination

```bash
curl -X GET "http://localhost:8080/api/v1/locations/page?page=0&size=10&sort=createdAt,desc"
```

---

## Step 4: Spatial Queries

### 4.1 Find Locations Near a Point

Find all locations within 2000 meters of Golden Gate Bridge:

```bash
curl -X GET "http://localhost:8080/api/v1/locations/near?latitude=37.8199&longitude=-122.4783&radiusMeters=2000"
```

**Parameters:**
- `latitude`: 37.8199 (Golden Gate Bridge)
- `longitude`: -122.4783
- `radiusMeters`: 2000 (2 km radius)

**Expected:** Returns Golden Gate Bridge, Alcatraz, Fisherman's Wharf, and Coit Tower

### 4.2 Find Locations Within a Smaller Radius

Find locations within 500 meters:

```bash
curl -X GET "http://localhost:8080/api/v1/locations/near?latitude=37.8199&longitude=-122.4783&radiusMeters=500"
```

**Expected:** Returns only Golden Gate Bridge viewpoint

### 4.3 Find Locations Within a Polygon

Find all locations within a bounding box around San Francisco Bay:

```bash
curl -X GET "http://localhost:8080/api/v1/locations/within?wkt=POLYGON((-122.52%2037.75,%20-122.35%2037.75,%20-122.35%2037.85,%20-122.52%2037.85,%20-122.52%2037.75))"
```

**Note:** URL-encoded WKT:
- Original: `POLYGON((-122.52 37.75, -122.35 37.75, -122.35 37.85, -122.52 37.85, -122.52 37.75))`
- Encoded: Spaces become `%20`, commas stay as-is

**Alternative (using --data-urlencode):**

```bash
curl -G http://localhost:8080/api/v1/locations/within \
  --data-urlencode "wkt=POLYGON((-122.52 37.75, -122.35 37.75, -122.35 37.85, -122.52 37.85, -122.52 37.75))"
```

### 4.4 Find Locations Within a Circle (Non-Standard WKT)

Find all locations within a circular area using the CIRCLE extension:

```bash
curl -X GET "http://localhost:8080/api/v1/locations/within?wkt=CIRCLE(-122.4783%2037.8199%202000)"
```

**CIRCLE WKT Format (Non-Standard Extension):**
```
CIRCLE(longitude latitude radiusMeters)
```

**Example:**
- `CIRCLE(-122.4783 37.8199 2000)` - Circle centered at Golden Gate Bridge with 2km radius
- Longitude: -122.4783 (must be between -180 and 180)
- Latitude: 37.8199 (must be between -90 and 90)
- Radius: 2000 meters (must be positive)

**URL-encoded:**
```
CIRCLE(-122.4783%2037.8199%202000)
```

**Alternative (using --data-urlencode):**

```bash
curl -G http://localhost:8080/api/v1/locations/within \
  --data-urlencode "wkt=CIRCLE(-122.4783 37.8199 2000)"
```

**Note:** CIRCLE is a non-standard WKT extension. Standard WKT doesn't include circles. This format is specific to this API and some spatial databases like PostGIS.

---

## Step 5: Search and Filter

### 5.1 Search by Address

```bash
curl -X GET "http://localhost:8080/api/v1/locations/search?address=Golden%20Gate"
```

**Expected:** Returns locations with "Golden Gate" in the address

### 5.2 Get Locations by Postal Code

```bash
curl -X GET http://localhost:8080/api/v1/locations/postal-code/94133
```

**Expected:** Returns Alcatraz, Fisherman's Wharf, and Coit Tower

---

## Step 6: Update Location

### 6.1 Update Location Details

```bash
curl -X PUT http://localhost:8080/api/v1/locations/LOCATION_ID \
  -H "Content-Type: application/json" \
  -d '{
    "zoneId": "ZONE_ID",
    "name": "Golden Gate Bridge North Viewpoint",
    "locationType": "LANDMARK",
    "latitude": 37.8199,
    "longitude": -122.4783,
    "altitudeMeters": 70.0,
    "accuracyMeters": 2.5,
    "address": "Golden Gate Bridge North, San Francisco, CA",
    "postalCode": "94129",
    "active": true,
    "metadata": {
      "type": "viewpoint",
      "accessibility": "public",
      "parking": true,
      "updated": true
    }
  }'
```

---

## Step 7: Utility Endpoints

### 7.1 Check if Location Exists

```bash
curl -X GET http://localhost:8080/api/v1/locations/LOCATION_ID/exists
```

**Expected:** 200 OK if exists, 404 Not Found if doesn't exist

### 7.2 Get Location Count

```bash
curl -X GET http://localhost:8080/api/v1/locations/count
```

**Expected Response:**
```json
4
```

---

## Step 8: Delete Location

### 8.1 Delete a Location

```bash
curl -X DELETE http://localhost:8080/api/v1/locations/LOCATION_ID
```

**Expected:** 204 No Content

### 8.2 Verify Deletion

```bash
curl -X GET http://localhost:8080/api/v1/locations/LOCATION_ID
```

**Expected:** 404 Not Found

---

## Complete Test Script

Here's a complete bash script to test all endpoints:

```bash
#!/bin/bash

BASE_URL="http://localhost:8080"

echo "=== Creating Hierarchy ==="

# Create Country
COUNTRY_RESPONSE=$(curl -s -X POST $BASE_URL/api/v1/countries \
  -H "Content-Type: application/json" \
  -d '{"name":"United States","isoCodeAlpha2":"US","isoCodeAlpha3":"USA","isoCodeNumeric":"840","phoneCode":"+1","currencyCode":"USD","capital":"Washington D.C.","active":true}')
COUNTRY_ID=$(echo $COUNTRY_RESPONSE | jq -r '.id')
echo "Country ID: $COUNTRY_ID"

# Create Region
REGION_RESPONSE=$(curl -s -X POST $BASE_URL/api/v1/regions \
  -H "Content-Type: application/json" \
  -d "{\"countryId\":\"$COUNTRY_ID\",\"name\":\"California\",\"code\":\"US-CA\",\"regionType\":\"STATE\",\"timeZone\":\"America/Los_Angeles\",\"active\":true}")
REGION_ID=$(echo $REGION_RESPONSE | jq -r '.id')
echo "Region ID: $REGION_ID"

# Create City
CITY_RESPONSE=$(curl -s -X POST $BASE_URL/api/v1/cities \
  -H "Content-Type: application/json" \
  -d "{\"regionId\":\"$REGION_ID\",\"name\":\"San Francisco\",\"timeZone\":\"America/Los_Angeles\",\"postalCode\":\"94102\",\"capital\":false,\"active\":true}")
CITY_ID=$(echo $CITY_RESPONSE | jq -r '.id')
echo "City ID: $CITY_ID"

# Create Area
AREA_RESPONSE=$(curl -s -X POST $BASE_URL/api/v1/areas \
  -H "Content-Type: application/json" \
  -d "{\"cityId\":\"$CITY_ID\",\"name\":\"Golden Gate Park\",\"areaType\":\"DISTRICT\",\"postalCode\":\"94121\",\"active\":true}")
AREA_ID=$(echo $AREA_RESPONSE | jq -r '.id')
echo "Area ID: $AREA_ID"

# Create Zone
ZONE_RESPONSE=$(curl -s -X POST $BASE_URL/api/v1/zones \
  -H "Content-Type: application/json" \
  -d "{\"areaId\":\"$AREA_ID\",\"name\":\"North Zone\",\"zoneType\":\"SECTOR\",\"postalCode\":\"94121\",\"active\":true}")
ZONE_ID=$(echo $ZONE_RESPONSE | jq -r '.id')
echo "Zone ID: $ZONE_ID"

echo ""
echo "=== Creating Locations ==="

# Create Location
LOCATION_RESPONSE=$(curl -s -X POST $BASE_URL/api/v1/locations \
  -H "Content-Type: application/json" \
  -d "{\"zoneId\":\"$ZONE_ID\",\"name\":\"Golden Gate Bridge\",\"locationType\":\"LANDMARK\",\"latitude\":37.8199,\"longitude\":-122.4783,\"altitudeMeters\":67.5,\"address\":\"Golden Gate Bridge, San Francisco, CA\",\"postalCode\":\"94129\",\"active\":true}")
LOCATION_ID=$(echo $LOCATION_RESPONSE | jq -r '.id')
echo "Location ID: $LOCATION_ID"
echo "$LOCATION_RESPONSE" | jq '.'

echo ""
echo "=== Testing Spatial Queries ==="

# Find nearby locations
echo "Finding locations within 2000m:"
curl -s -X GET "$BASE_URL/api/v1/locations/near?latitude=37.8199&longitude=-122.4783&radiusMeters=2000" | jq '.'

echo ""
echo "=== Getting Location by ID ==="
curl -s -X GET "$BASE_URL/api/v1/locations/$LOCATION_ID" | jq '.'

echo ""
echo "=== Location Count ==="
curl -s -X GET "$BASE_URL/api/v1/locations/count"

echo ""
echo "Test completed!"
```

**Save as `test-locations.sh` and run:**
```bash
chmod +x test-locations.sh
./test-locations.sh
```

---

## Expected HTTP Status Codes

- `201 Created` - Location created successfully
- `200 OK` - Location retrieved/updated successfully
- `204 No Content` - Location deleted successfully
- `404 Not Found` - Location not found
- `400 Bad Request` - Invalid request data or WKT format

---

## Tips

1. **Use jq for pretty JSON**: Install jq (`brew install jq` on Mac) to format responses
2. **Save IDs**: Save returned IDs in variables for subsequent requests
3. **Check logs**: Monitor application logs for any errors
4. **Spatial queries**: Ensure coordinates are in correct order (longitude, latitude for WKT)
5. **URL encoding**: Use `--data-urlencode` for complex WKT strings

---

## Troubleshooting

### Issue: 404 Not Found on spatial queries
**Solution**: Ensure locations are created first and coordinates are valid

### Issue: 400 Bad Request on polygon query
**Solution**: Check WKT format and URL encoding:
```bash
# Correct POLYGON format
POLYGON((-122.52 37.75, -122.35 37.75, -122.35 37.85, -122.52 37.85, -122.52 37.75))

# Correct CIRCLE format (non-standard extension)
CIRCLE(-122.4783 37.8199 2000)
```

### Issue: "Unsupported WKT type: CIRCLE"
**Solution**: Ensure you're using the correct CIRCLE format with 3 values:
```bash
# Correct: longitude latitude radiusMeters
CIRCLE(-122.4783 37.8199 2000)

# Wrong: missing radius
CIRCLE(-122.4783 37.8199)
```

### Issue: Foreign key constraint violation
**Solution**: Create the full hierarchy (Country → Region → City → Area → Zone) before creating locations

---

## Supported WKT Formats

### Standard WKT (OGC Specification)
- `POINT(longitude latitude)` - Single point
- `LINESTRING(lon1 lat1, lon2 lat2, ...)` - Line with multiple points
- `POLYGON((lon1 lat1, lon2 lat2, ...))` - Closed polygon

### Non-Standard Extensions
- `CIRCLE(longitude latitude radiusMeters)` - Circle with center and radius
  - Example: `CIRCLE(-122.4783 37.8199 2000)`
  - Longitude: -180 to 180
  - Latitude: -90 to 90
  - Radius: positive number in meters
