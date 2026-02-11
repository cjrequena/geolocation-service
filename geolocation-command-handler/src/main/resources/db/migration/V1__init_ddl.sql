----------------------------------------------------
-- IMPROVED VERSION - Current Structure Enhanced
----------------------------------------------------
DROP SCHEMA IF EXISTS geo_schema CASCADE;
CREATE SCHEMA geo_schema;

SET search_path = geo_schema, public;

----------------------------------------------------
-- EXTENSIONS
----------------------------------------------------
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_topology;

----------------------------------------------------
-- GeoShape (Improved)
----------------------------------------------------
CREATE TABLE geoshape (
    id                      UUID         PRIMARY KEY,
    name                    VARCHAR(255) NOT NULL,
    geometry_type           VARCHAR(20) NOT NULL CHECK (geometry_type IN ('POINT','CIRCLE','RECTANGLE','POLYGON','LINE')),
    geometry                geometry(Geometry, 4326) NOT NULL,

    -- Optional fields based on geometry_type
    center_latitude         DECIMAL(9,6),
    center_longitude        DECIMAL(9,6),
    radius_meters           DECIMAL(10,2),
    bounds                  JSON,

    active                  BOOLEAN NOT NULL DEFAULT TRUE,
    metadata                JSON NOT NULL DEFAULT '{}',
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Validation constraints
    CONSTRAINT chk_point_no_radius CHECK (
        geometry_type != 'POINT' OR radius_meters IS NULL
    ),
    CONSTRAINT chk_circle_has_radius CHECK (
        geometry_type != 'CIRCLE' OR (radius_meters IS NOT NULL AND center_latitude IS NOT NULL AND center_longitude IS NOT NULL)
    ),
    CONSTRAINT chk_polygon_no_radius CHECK (
        geometry_type != 'POLYGON' OR radius_meters IS NULL
    )
);

CREATE INDEX idx_geoshape_geometry_type ON geoshape(geometry_type);
CREATE INDEX idx_geoshape_geometry ON geoshape USING GIST(geometry);

COMMENT ON TABLE geoshape IS 'Stores geometric shapes for geographic entities';
COMMENT ON CONSTRAINT chk_circle_has_radius ON geoshape IS 'Circles must have radius and center coordinates';

----------------------------------------------------
-- Country (Improved)
----------------------------------------------------
CREATE TABLE country (
    id                UUID         PRIMARY KEY,
    name              VARCHAR(255) NOT NULL,
    iso_code_alpha2   CHAR(2) NOT NULL UNIQUE,
    iso_code_alpha3   CHAR(3) UNIQUE,
    iso_code_numeric  CHAR(3),
    phone_code        VARCHAR(10),
    currency_code     CHAR(3),
    capital           VARCHAR(255),
    population        BIGINT,
    active            BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_iso_alpha2_format CHECK (iso_code_alpha2 ~ '^[A-Z]{2}$'),
    CONSTRAINT chk_iso_alpha3_format CHECK (iso_code_alpha3 IS NULL OR iso_code_alpha3 ~ '^[A-Z]{3}$')
);

CREATE INDEX idx_country_name ON country(name);
CREATE INDEX idx_country_active ON country(active) WHERE active = TRUE;

COMMENT ON TABLE country IS 'Countries with ISO codes and basic metadata';

----------------------------------------------------
-- Region (Improved)
----------------------------------------------------
CREATE TABLE region (
    id                UUID         PRIMARY KEY,
    country_id        UUID NOT NULL,
    name              VARCHAR(255) NOT NULL,
    code              VARCHAR(50), -- ISO 3166-2 code, ex: US-CA for California
    region_type       VARCHAR(50), -- state, province, prefecture, etc.
    geoshape_id       UUID,
    population        BIGINT,
    timezone          VARCHAR(50),
    active            BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_region_country FOREIGN KEY (country_id) REFERENCES country(id) ON DELETE CASCADE,
    CONSTRAINT fk_region_geoshape FOREIGN KEY (geoshape_id) REFERENCES geoshape(id) ON DELETE SET NULL,
    CONSTRAINT uq_region_country_name UNIQUE(country_id, name)
);

CREATE INDEX idx_region_country ON region(country_id);
CREATE INDEX idx_region_geoshape ON region(geoshape_id);
CREATE INDEX idx_region_active ON region(active) WHERE active = TRUE;
CREATE INDEX idx_region_name ON region(name);

COMMENT ON TABLE region IS 'First-level administrative divisions (states, provinces, etc.)';

----------------------------------------------------
-- City (Improved)
----------------------------------------------------
CREATE TABLE city (
    id                UUID         PRIMARY KEY,
    region_id         UUID,
    name              VARCHAR(255) NOT NULL,
    geoshape_id       UUID,
    population        BIGINT,
    timezone          VARCHAR(50),
    postal_code       VARCHAR(20),
    capital        BOOLEAN DEFAULT FALSE,
    active         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_city_region FOREIGN KEY (region_id) REFERENCES region(id) ON DELETE CASCADE,
    CONSTRAINT fk_city_geoshape FOREIGN KEY (geoshape_id) REFERENCES geoshape(id) ON DELETE SET NULL,
    CONSTRAINT uq_city_region_name UNIQUE(region_id, name)
);

CREATE INDEX idx_city_region ON city(region_id);
CREATE INDEX idx_city_geoshape ON city(geoshape_id);
CREATE INDEX idx_city_active ON city(active) WHERE active = TRUE;
CREATE INDEX idx_city_name ON city(name);

COMMENT ON TABLE city IS 'Cities and municipalities';

----------------------------------------------------
-- Area (Improved)
----------------------------------------------------
CREATE TABLE area (
    id                UUID         PRIMARY KEY,
    city_id           UUID NOT NULL,
    name              VARCHAR(255) NOT NULL,
    area_type         VARCHAR(50), -- district, borough, neighborhood, etc.
    geoshape_id       UUID,
    population        BIGINT,
    postal_code       VARCHAR(20),
    active         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_area_city FOREIGN KEY (city_id) REFERENCES city(id) ON DELETE CASCADE,
    CONSTRAINT fk_area_geoshape FOREIGN KEY (geoshape_id) REFERENCES geoshape(id) ON DELETE SET NULL,
    CONSTRAINT uq_area_city_name UNIQUE(city_id, name)
);

CREATE INDEX idx_area_city ON area(city_id);
CREATE INDEX idx_area_geoshape ON area(geoshape_id);
CREATE INDEX idx_area_active ON area(active) WHERE active = TRUE;
CREATE INDEX idx_area_name ON area(name);

COMMENT ON TABLE area IS 'Sub-city areas (districts, boroughs, neighborhoods)';

----------------------------------------------------
-- Zone (Improved)
----------------------------------------------------
CREATE TABLE zone (
    id                UUID         PRIMARY KEY,
    area_id           UUID NOT NULL,
    name              VARCHAR(255) NOT NULL,
    zone_type         VARCHAR(50), -- block, sector, precinct, etc.
    geoshape_id       UUID,
    postal_code       VARCHAR(20),
    active         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_zone_area FOREIGN KEY (area_id) REFERENCES area(id) ON DELETE CASCADE,
    CONSTRAINT fk_zone_geoshape FOREIGN KEY (geoshape_id) REFERENCES geoshape(id) ON DELETE SET NULL,
    CONSTRAINT uq_zone_area_name UNIQUE(area_id, name)
);

CREATE INDEX idx_zone_area ON zone(area_id);
CREATE INDEX idx_zone_geoshape ON zone(geoshape_id);
CREATE INDEX idx_zone_active ON zone(active) WHERE active = TRUE;
CREATE INDEX idx_zone_name ON zone(name);

COMMENT ON TABLE zone IS 'Fine-grained zones within areas';

----------------------------------------------------
-- Location (Improved)
----------------------------------------------------
CREATE TABLE location (
    id                UUID         PRIMARY KEY,
    zone_id           UUID, -- Made nullable to allow flexible assignment
    point         geometry(Point, 4326) NOT NULL,
    altitude_meters   DECIMAL(8,2),
    accuracy_meters   DECIMAL(8,2),
    address           TEXT,
    postal_code       VARCHAR(20),
    metadata          JSON DEFAULT '{}',
    active            BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_location_zone FOREIGN KEY (zone_id) REFERENCES zone(id) ON DELETE SET NULL
);

CREATE INDEX idx_location_zone ON location(zone_id);
CREATE INDEX idx_location_point ON location USING GIST(point);
CREATE INDEX idx_location_active ON location(active) WHERE active = TRUE;

COMMENT ON TABLE location IS 'Specific point locations with coordinates';
COMMENT ON COLUMN location.point IS 'PostGIS point geometry - single source of truth for coordinates';

----------------------------------------------------
-- Helper Functions
----------------------------------------------------

-- Function to extract latitude from point
CREATE OR REPLACE FUNCTION get_latitude(geom geometry)
RETURNS DECIMAL(10,7) AS $$
BEGIN
    RETURN ST_Y(geom);
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Function to extract longitude from point
CREATE OR REPLACE FUNCTION get_longitude(geom geometry)
RETURNS DECIMAL(10,7) AS $$
BEGIN
    RETURN ST_X(geom);
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply update triggers to all tables
CREATE TRIGGER update_geoshape_updated_at BEFORE UPDATE ON geoshape
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_country_updated_at BEFORE UPDATE ON country
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_region_updated_at BEFORE UPDATE ON region
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_city_updated_at BEFORE UPDATE ON city
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_area_updated_at BEFORE UPDATE ON area
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_zone_updated_at BEFORE UPDATE ON zone
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_location_updated_at BEFORE UPDATE ON location
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

----------------------------------------------------
-- Views for easier querying
----------------------------------------------------

-- Complete location hierarchy view
CREATE OR REPLACE VIEW v_location_hierarchy AS
SELECT
    l.id AS location_id,
    l.point,
    get_latitude(l.point) AS latitude,
    get_longitude(l.point) AS longitude,
    l.address,
    l.postal_code AS location_postal_code,
    z.id AS zone_id,
    z.name AS zone_name,
    a.id AS area_id,
    a.name AS area_name,
    c.id AS city_id,
    c.name AS city_name,
    r.id AS region_id,
    r.name AS region_name,
    co.id AS country_id,
    co.name AS country_name,
    co.iso_code_alpha2,
    r.timezone
FROM location l
LEFT JOIN zone z ON l.zone_id = z.id
LEFT JOIN area a ON z.area_id = a.id
LEFT JOIN city c ON a.city_id = c.id
LEFT JOIN region r ON c.region_id = r.id
LEFT JOIN country co ON r.country_id = co.id;

COMMENT ON VIEW v_location_hierarchy IS 'Complete location hierarchy from location to country';
