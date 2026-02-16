----------------------------------------------------
-- SPAIN COMPLETE HIERARCHY (Including Balearic Islands)
-- Run: psql -U postgres -d yourdb -f spain_complete.sql
----------------------------------------------------

SET search_path = geo_schema, public;

-- ========== 1. GEOSHAPES (12 total) ==========
INSERT INTO geoshape VALUES
-- Mainland Spain
('550e8400-e29b-41d4-a716-446655440001', 'Spain Boundary', 'POLYGON', ST_GeomFromText('POLYGON((-9.5 36.0, 3.5 36.0, 3.5 43.8, -9.5 43.8, -9.5 36.0))', 4326), NULL, NULL, NULL, NULL, true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440002', 'Madrid Region', 'POLYGON', ST_GeomFromText('POLYGON((-4.5 39.8, -3.0 39.8, -3.0 41.2, -4.5 41.2, -4.5 39.8))', 4326), NULL, NULL, NULL, NULL, true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440003', 'Madrid City', 'POLYGON', ST_GeomFromText('POLYGON((-3.88 40.31, -3.55 40.31, -3.55 40.56, -3.88 40.56, -3.88 40.31))', 4326), NULL, NULL, NULL, NULL, true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440004', 'Barcelona City', 'POLYGON', ST_GeomFromText('POLYGON((2.05 41.32, 2.23 41.32, 2.23 41.47, 2.05 41.47, 2.05 41.32))', 4326), NULL, NULL, NULL, NULL, true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440005', 'Centro District', 'POLYGON', ST_GeomFromText('POLYGON((-3.71 40.40, -3.68 40.40, -3.68 40.43, -3.71 40.43, -3.71 40.40))', 4326), NULL, NULL, NULL, NULL, true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440006', 'Sol Zone', 'POLYGON', ST_GeomFromText('POLYGON((-3.705 40.416, -3.701 40.416, -3.701 40.419, -3.705 40.419, -3.705 40.416))', 4326), NULL, NULL, NULL, NULL, true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440007', 'Airport Zone', 'CIRCLE', ST_Buffer(ST_GeomFromText('POINT(-3.5676 40.4719)', 4326)::geography, 5000)::geometry, 40.4719, -3.5676, 5000.00, NULL, true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440008', 'Historic Route', 'LINE', ST_GeomFromText('LINESTRING(-3.7038 40.4168, -3.7074 40.4155, -3.6958 40.4180)', 4326), NULL, NULL, NULL, NULL, true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Balearic Islands
('550e8400-e29b-41d4-a716-446655440020', 'Balearic Islands Boundary', 'POLYGON', ST_GeomFromText('POLYGON((1.2 38.6, 4.4 38.6, 4.4 40.1, 1.2 40.1, 1.2 38.6))', 4326), NULL, NULL, NULL, NULL, true, '{"description": "Balearic archipelago"}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440021', 'Palma City Boundary', 'POLYGON', ST_GeomFromText('POLYGON((2.58 39.52, 2.70 39.52, 2.70 39.63, 2.58 39.63, 2.58 39.52))', 4326), NULL, NULL, NULL, NULL, true, '{"description": "Palma de Mallorca"}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440022', 'Palma Historic Center', 'POLYGON', ST_GeomFromText('POLYGON((2.646 39.566, 2.654 39.566, 2.654 39.572, 2.646 39.572, 2.646 39.566))', 4326), NULL, NULL, NULL, NULL, true, '{"description": "Old town"}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440023', 'Ibiza Delivery Zone', 'CIRCLE', ST_Buffer(ST_GeomFromText('POINT(1.4323 38.9067)', 4326)::geography, 3000)::geometry, 38.9067, 1.4323, 3000.00, NULL, true, '{"radius_km": 3}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ========== 2. COUNTRY (1) ==========
INSERT INTO country VALUES 
('650e8400-e29b-41d4-a716-446655440001', 'Spain', 'ES', 'ESP', '724', '+34', 'EUR', 'Madrid', 47450795, true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ========== 3. REGIONS (5) ==========
INSERT INTO region VALUES
('750e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440001', 'Community of Madrid', 'ES-MD', 'AUTONOMOUS_COMMUNITY', '550e8400-e29b-41d4-a716-446655440002', 6751251, 'Europe/Madrid', true, '{"capital": "Madrid"}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('750e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440001', 'Catalonia', 'ES-CT', 'AUTONOMOUS_COMMUNITY', NULL, 7780479, 'Europe/Madrid', true, '{"capital": "Barcelona"}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('750e8400-e29b-41d4-a716-446655440003', '650e8400-e29b-41d4-a716-446655440001', 'Andalusia', 'ES-AN', 'AUTONOMOUS_COMMUNITY', NULL, 8464411, 'Europe/Madrid', true, '{"capital": "Seville"}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('750e8400-e29b-41d4-a716-446655440004', '650e8400-e29b-41d4-a716-446655440001', 'Valencian Community', 'ES-VC', 'AUTONOMOUS_COMMUNITY', NULL, 5058138, 'Europe/Madrid', true, '{"capital": "Valencia"}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('750e8400-e29b-41d4-a716-446655440005', '650e8400-e29b-41d4-a716-446655440001', 'Balearic Islands', 'ES-IB', 'AUTONOMOUS_COMMUNITY', '550e8400-e29b-41d4-a716-446655440020', 1173008, 'Europe/Madrid', true, '{"capital": "Palma", "islands": ["Mallorca", "Menorca", "Ibiza", "Formentera"]}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ========== 4. CITIES (8) ==========
INSERT INTO city VALUES
-- Mainland
('850e8400-e29b-41d4-a716-446655440001', '750e8400-e29b-41d4-a716-446655440001', 'Madrid', '550e8400-e29b-41d4-a716-446655440003', 3223334, 'Europe/Madrid', '28001', true, true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('850e8400-e29b-41d4-a716-446655440002', '750e8400-e29b-41d4-a716-446655440002', 'Barcelona', '550e8400-e29b-41d4-a716-446655440004', 1636762, 'Europe/Madrid', '08001', true, true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('850e8400-e29b-41d4-a716-446655440003', '750e8400-e29b-41d4-a716-446655440003', 'Seville', NULL, 688711, 'Europe/Madrid', '41001', true, true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('850e8400-e29b-41d4-a716-446655440004', '750e8400-e29b-41d4-a716-446655440004', 'Valencia', NULL, 791413, 'Europe/Madrid', '46001', true, true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Balearic Islands
('850e8400-e29b-41d4-a716-446655440005', '750e8400-e29b-41d4-a716-446655440005', 'Palma de Mallorca', '550e8400-e29b-41d4-a716-446655440021', 416065, 'Europe/Madrid', '07001', true, true, '{"island": "Mallorca"}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('850e8400-e29b-41d4-a716-446655440006', '750e8400-e29b-41d4-a716-446655440005', 'Calvià', NULL, 51774, 'Europe/Madrid', '07184', false, true, '{"island": "Mallorca"}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('850e8400-e29b-41d4-a716-446655440007', '750e8400-e29b-41d4-a716-446655440005', 'Mahón', NULL, 29125, 'Europe/Madrid', '07701', false, true, '{"island": "Menorca"}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('850e8400-e29b-41d4-a716-446655440008', '750e8400-e29b-41d4-a716-446655440005', 'Ibiza Town', NULL, 49783, 'Europe/Madrid', '07800', false, true, '{"island": "Ibiza"}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ========== 5. AREAS (20) ==========
INSERT INTO area VALUES
-- Madrid
('950e8400-e29b-41d4-a716-446655440001', '850e8400-e29b-41d4-a716-446655440001', 'Centro', 'DISTRICT', '550e8400-e29b-41d4-a716-446655440005', 131928, '28013', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('950e8400-e29b-41d4-a716-446655440002', '850e8400-e29b-41d4-a716-446655440001', 'Salamanca', 'DISTRICT', NULL, 145780, '28006', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('950e8400-e29b-41d4-a716-446655440003', '850e8400-e29b-41d4-a716-446655440001', 'Chamberí', 'DISTRICT', NULL, 139817, '28010', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('950e8400-e29b-41d4-a716-446655440004', '850e8400-e29b-41d4-a716-446655440001', 'Retiro', 'NEIGHBORHOOD', NULL, 118618, '28009', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Barcelona
('950e8400-e29b-41d4-a716-446655440010', '850e8400-e29b-41d4-a716-446655440002', 'Eixample', 'DISTRICT', NULL, 262000, '08008', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('950e8400-e29b-41d4-a716-446655440011', '850e8400-e29b-41d4-a716-446655440002', 'Ciutat Vella', 'QUARTER', NULL, 105000, '08002', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('950e8400-e29b-41d4-a716-446655440012', '850e8400-e29b-41d4-a716-446655440002', 'Gràcia', 'NEIGHBORHOOD', NULL, 120000, '08012', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('950e8400-e29b-41d4-a716-446655440013', '850e8400-e29b-41d4-a716-446655440002', 'Sants-Montjuïc', 'DISTRICT', NULL, 180824, '08004', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Seville
('950e8400-e29b-41d4-a716-446655440020', '850e8400-e29b-41d4-a716-446655440003', 'Casco Antiguo', 'QUARTER', NULL, 58000, '41004', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('950e8400-e29b-41d4-a716-446655440021', '850e8400-e29b-41d4-a716-446655440003', 'Triana', 'NEIGHBORHOOD', NULL, 46000, '41010', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Valencia
('950e8400-e29b-41d4-a716-446655440030', '850e8400-e29b-41d4-a716-446655440004', 'Ciutat Vella', 'QUARTER', NULL, 22000, '46001', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('950e8400-e29b-41d4-a716-446655440031', '850e8400-e29b-41d4-a716-446655440004', 'Russafa', 'NEIGHBORHOOD', NULL, 27000, '46006', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Palma
('950e8400-e29b-41d4-a716-446655440040', '850e8400-e29b-41d4-a716-446655440005', 'Casco Antiguo', 'QUARTER', '550e8400-e29b-41d4-a716-446655440022', 19500, '07001', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('950e8400-e29b-41d4-a716-446655440041', '850e8400-e29b-41d4-a716-446655440005', 'Portixol', 'NEIGHBORHOOD', NULL, 8200, '07006', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('950e8400-e29b-41d4-a716-446655440042', '850e8400-e29b-41d4-a716-446655440005', 'Santa Catalina', 'NEIGHBORHOOD', NULL, 12500, '07013', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('950e8400-e29b-41d4-a716-446655440043', '850e8400-e29b-41d4-a716-446655440005', 'Son Armadans', 'DISTRICT', NULL, 15800, '07014', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Calvià
('950e8400-e29b-41d4-a716-446655440050', '850e8400-e29b-41d4-a716-446655440006', 'Magaluf', 'NEIGHBORHOOD', NULL, 4500, '07181', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('950e8400-e29b-41d4-a716-446655440051', '850e8400-e29b-41d4-a716-446655440006', 'Santa Ponça', 'NEIGHBORHOOD', NULL, 12500, '07180', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Mahón
('950e8400-e29b-41d4-a716-446655440060', '850e8400-e29b-41d4-a716-446655440007', 'Centro Histórico', 'QUARTER', NULL, 8500, '07701', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Ibiza
('950e8400-e29b-41d4-a716-446655440070', '850e8400-e29b-41d4-a716-446655440008', 'Dalt Vila', 'QUARTER', NULL, 1200, '07800', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('950e8400-e29b-41d4-a716-446655440071', '850e8400-e29b-41d4-a716-446655440008', 'Marina District', 'DISTRICT', NULL, 5500, '07800', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ========== 6. ZONES (25) ==========
INSERT INTO zone VALUES
-- Madrid
('a50e8400-e29b-41d4-a716-446655440001', '950e8400-e29b-41d4-a716-446655440001', 'Puerta del Sol Area', 'BLOCK', '550e8400-e29b-41d4-a716-446655440006', '28013', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a50e8400-e29b-41d4-a716-446655440002', '950e8400-e29b-41d4-a716-446655440001', 'Plaza Mayor Zone', 'BLOCK', NULL, '28012', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a50e8400-e29b-41d4-a716-446655440003', '950e8400-e29b-41d4-a716-446655440001', 'Gran Vía Commercial', 'COMMERCIAL', NULL, '28013', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a50e8400-e29b-41d4-a716-446655440004', '950e8400-e29b-41d4-a716-446655440002', 'Serrano Shopping', 'COMMERCIAL', NULL, '28001', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a50e8400-e29b-41d4-a716-446655440005', '950e8400-e29b-41d4-a716-446655440002', 'Goya Residential', 'RESIDENTIAL', NULL, '28009', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a50e8400-e29b-41d4-a716-446655440006', '950e8400-e29b-41d4-a716-446655440004', 'Retiro Park Zone', 'SECTOR', NULL, '28009', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a50e8400-e29b-41d4-a716-446655440007', '950e8400-e29b-41d4-a716-446655440004', 'Jerónimos Block', 'BLOCK', NULL, '28014', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Barcelona
('a50e8400-e29b-41d4-a716-446655440010', '950e8400-e29b-41d4-a716-446655440010', 'Sagrada Família Zone', 'SECTOR', NULL, '08013', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a50e8400-e29b-41d4-a716-446655440011', '950e8400-e29b-41d4-a716-446655440011', 'Gothic Quarter', 'BLOCK', NULL, '08002', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a50e8400-e29b-41d4-a716-446655440012', '950e8400-e29b-41d4-a716-446655440011', 'La Rambla Zone', 'COMMERCIAL', NULL, '08002', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a50e8400-e29b-41d4-a716-446655440013', '950e8400-e29b-41d4-a716-446655440012', 'Vila de Gràcia', 'RESIDENTIAL', NULL, '08012', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a50e8400-e29b-41d4-a716-446655440014', '950e8400-e29b-41d4-a716-446655440013', 'Montjuïc Hill', 'SECTOR', NULL, '08038', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Seville
('a50e8400-e29b-41d4-a716-446655440020', '950e8400-e29b-41d4-a716-446655440020', 'Santa Cruz Block', 'BLOCK', NULL, '41004', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a50e8400-e29b-41d4-a716-446655440021', '950e8400-e29b-41d4-a716-446655440021', 'Triana Ceramics', 'INDUSTRIAL', NULL, '41010', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Valencia
('a50e8400-e29b-41d4-a716-446655440030', '950e8400-e29b-41d4-a716-446655440030', 'El Carmen Block', 'BLOCK', NULL, '46001', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a50e8400-e29b-41d4-a716-446655440031', '950e8400-e29b-41d4-a716-446655440031', 'Russafa Central', 'COMMERCIAL', NULL, '46006', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Palma
('a50e8400-e29b-41d4-a716-446655440040', '950e8400-e29b-41d4-a716-446655440040', 'Cathedral Quarter', 'BLOCK', NULL, '07001', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a50e8400-e29b-41d4-a716-446655440041', '950e8400-e29b-41d4-a716-446655440040', 'Born District', 'COMMERCIAL', NULL, '07012', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a50e8400-e29b-41d4-a716-446655440042', '950e8400-e29b-41d4-a716-446655440041', 'Portixol Marina', 'SECTOR', NULL, '07006', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a50e8400-e29b-41d4-a716-446655440043', '950e8400-e29b-41d4-a716-446655440042', 'Santa Catalina Market', 'COMMERCIAL', NULL, '07013', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a50e8400-e29b-41d4-a716-446655440044', '950e8400-e29b-41d4-a716-446655440043', 'Son Armadans Residential', 'RESIDENTIAL', NULL, '07014', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Calvià
('a50e8400-e29b-41d4-a716-446655440050', '950e8400-e29b-41d4-a716-446655440050', 'Magaluf Beach Zone', 'SECTOR', NULL, '07181', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a50e8400-e29b-41d4-a716-446655440051', '950e8400-e29b-41d4-a716-446655440051', 'Santa Ponça Golf', 'RESIDENTIAL', NULL, '07180', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Ibiza
('a50e8400-e29b-41d4-a716-446655440070', '950e8400-e29b-41d4-a716-446655440070', 'Dalt Vila Fortress', 'BLOCK', NULL, '07800', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a50e8400-e29b-41d4-a716-446655440071', '950e8400-e29b-41d4-a716-446655440071', 'Ibiza Marina', 'COMMERCIAL', NULL, '07800', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ========== 7. LOCATIONS (51) ==========
INSERT INTO location VALUES
-- MADRID
('b50e8400-e29b-41d4-a716-446655440001', 'a50e8400-e29b-41d4-a716-446655440001', 'Puerta del Sol', 'GENERIC', ST_GeomFromText('POINT(-3.7038 40.4168)', 4326), 667.0, 5.0, 'Plaza de la Puerta del Sol', '28013', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440002', 'a50e8400-e29b-41d4-a716-446655440002', 'Plaza Mayor', 'GENERIC', ST_GeomFromText('POINT(-3.7074 40.4155)', 4326), 667.0, 5.0, 'Plaza Mayor, 27', '28012', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440003', 'a50e8400-e29b-41d4-a716-446655440006', 'Retiro Park', 'GENERIC', ST_GeomFromText('POINT(-3.6824 40.4152)', 4326), 655.0, 3.0, 'Plaza de la Independencia, 7', '28001', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440004', 'a50e8400-e29b-41d4-a716-446655440007', 'Prado Museum', 'GENERIC', ST_GeomFromText('POINT(-3.6922 40.4138)', 4326), 655.0, 5.0, 'Calle de Ruiz de Alarcón, 23', '28014', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440010', 'a50e8400-e29b-41d4-a716-446655440002', 'Hotel Ritz Madrid', 'HOTEL', ST_GeomFromText('POINT(-3.6958 40.4180)', 4326), 667.0, 3.0, 'Plaza de la Lealtad, 5', '28014', true, '{"stars":5}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440011', 'a50e8400-e29b-41d4-a716-446655440003', 'Gran Meliá Palacio', 'HOTEL', ST_GeomFromText('POINT(-3.7112 40.4195)', 4326), 670.0, 3.0, 'Cuesta de Santo Domingo, 5', '28013', true, '{"stars":5}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440020', NULL, 'Madrid-Barajas Airport', 'AIRPORT', ST_GeomFromText('POINT(-3.5676 40.4719)', 4326), 610.0, 10.0, 'Av. de la Hispanidad', '28042', true, '{"iata":"MAD"}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440021', NULL, 'Atocha Station', 'BUS_STATION', ST_GeomFromText('POINT(-3.6907 40.4068)', 4326), 660.0, 10.0, 'Plaza del Emperador Carlos V', '28045', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440022', NULL, 'South Bus Station', 'BUS_STATION', ST_GeomFromText('POINT(-3.6859 40.3852)', 4326), 600.0, 10.0, 'Calle de Méndez Álvaro, 83', '28045', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440030', 'a50e8400-e29b-41d4-a716-446655440001', 'Sol Metro Pickup', 'PICKUP', ST_GeomFromText('POINT(-3.7032 40.4169)', 4326), 667.0, 2.0, 'Metro Sol Exit 1', '28013', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- BARCELONA
('b50e8400-e29b-41d4-a716-446655440040', 'a50e8400-e29b-41d4-a716-446655440010', 'Sagrada Família', 'GENERIC', ST_GeomFromText('POINT(2.1744 41.4036)', 4326), 12.0, 5.0, 'Carrer de Mallorca, 401', '08013', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440041', 'a50e8400-e29b-41d4-a716-446655440011', 'Barcelona Cathedral', 'GENERIC', ST_GeomFromText('POINT(2.1763 41.3841)', 4326), 12.0, 5.0, 'Pla de la Seu', '08002', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440042', 'a50e8400-e29b-41d4-a716-446655440012', 'La Rambla Start', 'GENERIC', ST_GeomFromText('POINT(2.1769 41.3784)', 4326), 5.0, 3.0, 'La Rambla, 1', '08002', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440043', 'a50e8400-e29b-41d4-a716-446655440010', 'Casa Batlló', 'GENERIC', ST_GeomFromText('POINT(2.1649 41.3916)', 4326), 15.0, 3.0, 'Passeig de Gràcia, 43', '08007', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440050', 'a50e8400-e29b-41d4-a716-446655440011', 'Hotel Arts Barcelona', 'HOTEL', ST_GeomFromText('POINT(2.1970 41.3901)', 4326), 5.0, 3.0, 'Carrer de la Marina, 19-21', '08005', true, '{"stars":5}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440060', NULL, 'Barcelona Airport', 'AIRPORT', ST_GeomFromText('POINT(2.0788 41.2974)', 4326), 4.0, 10.0, 'Carrer del Prat', '08820', true, '{"iata":"BCN"}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440061', NULL, 'Barcelona Nord Bus Station', 'BUS_STATION', ST_GeomFromText('POINT(2.1827 41.3910)', 4326), 12.0, 10.0, 'Carrer d''Alí Bei, 80', '08013', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440062', NULL, 'Port of Barcelona', 'PORT', ST_GeomFromText('POINT(2.1863 41.3612)', 4326), 0.0, 15.0, 'World Trade Center', '08039', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440070', 'a50e8400-e29b-41d4-a716-446655440011', 'Plaça Catalunya Pickup', 'PICKUP', ST_GeomFromText('POINT(2.1704 41.3874)', 4326), 12.0, 2.0, 'Plaça Catalunya', '08002', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- SEVILLE
('b50e8400-e29b-41d4-a716-446655440080', 'a50e8400-e29b-41d4-a716-446655440020', 'Seville Cathedral', 'GENERIC', ST_GeomFromText('POINT(-5.9931 37.3861)', 4326), 7.0, 5.0, 'Av. de la Constitución', '41004', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440081', 'a50e8400-e29b-41d4-a716-446655440020', 'Real Alcázar', 'GENERIC', ST_GeomFromText('POINT(-5.9925 37.3832)', 4326), 7.0, 5.0, 'Patio de Banderas', '41004', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440082', 'a50e8400-e29b-41d4-a716-446655440021', 'Triana Market', 'GENERIC', ST_GeomFromText('POINT(-6.0029 37.3850)', 4326), 5.0, 5.0, 'Plaza del Altozano', '41010', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440083', NULL, 'Seville Airport', 'AIRPORT', ST_GeomFromText('POINT(-5.8990 37.4180)', 4326), 34.0, 10.0, 'Carretera del Aeropuerto', '41020', true, '{"iata":"SVQ"}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- VALENCIA
('b50e8400-e29b-41d4-a716-446655440090', 'a50e8400-e29b-41d4-a716-446655440030', 'Valencia Cathedral', 'GENERIC', ST_GeomFromText('POINT(-0.3754 39.4755)', 4326), 15.0, 5.0, 'Plaça de l''Almoina, 1', '46003', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440091', 'a50e8400-e29b-41d4-a716-446655440031', 'Central Market', 'GENERIC', ST_GeomFromText('POINT(-0.3774 39.4740)', 4326), 12.0, 5.0, 'Plaça de la Ciutat de Bruges', '46001', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440092', NULL, 'City of Arts and Sciences', 'GENERIC', ST_GeomFromText('POINT(-0.3553 39.4545)', 4326), 10.0, 5.0, 'Av. del Professor López Piñero, 7', '46013', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440093', NULL, 'Valencia Airport', 'AIRPORT', ST_GeomFromText('POINT(-0.4816 39.4893)', 4326), 69.0, 10.0, 'Carretera del Aeropuerto', '46940', true, '{"iata":"VLC"}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- PALMA DE MALLORCA
('b50e8400-e29b-41d4-a716-446655440100', 'a50e8400-e29b-41d4-a716-446655440040', 'Palma Cathedral (La Seu)', 'GENERIC', ST_GeomFromText('POINT(2.6476 39.5668)', 4326), 13.0, 5.0, 'Plaça de la Seu, s/n', '07001', true, '{"unesco_candidate": true}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440101', 'a50e8400-e29b-41d4-a716-446655440040', 'Royal Palace of La Almudaina', 'GENERIC', ST_GeomFromText('POINT(2.6466 39.5677)', 4326), 13.0, 5.0, 'Carrer del Palau Reial', '07001', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440102', 'a50e8400-e29b-41d4-a716-446655440041', 'Paseo Marítimo', 'GENERIC', ST_GeomFromText('POINT(2.6496 39.5609)', 4326), 5.0, 3.0, 'Paseo Marítimo', '07014', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440103', 'a50e8400-e29b-41d4-a716-446655440043', 'Santa Catalina Market', 'GENERIC', ST_GeomFromText('POINT(2.6322 39.5755)', 4326), 10.0, 5.0, 'Plaça Navegació', '07013', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440110', 'a50e8400-e29b-41d4-a716-446655440040', 'Hotel Sant Francesc', 'HOTEL', ST_GeomFromText('POINT(2.6496 39.5707)', 4326), 15.0, 3.0, 'Plaça Sant Francesc, 5', '07001', true, '{"stars": 5}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440111', 'a50e8400-e29b-41d4-a716-446655440042', 'Hotel Portixol', 'HOTEL', ST_GeomFromText('POINT(2.6641 39.5604)', 4326), 5.0, 3.0, 'Carrer Sirena, 27', '07006', true, '{"stars": 4}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440120', NULL, 'Palma de Mallorca Airport', 'AIRPORT', ST_GeomFromText('POINT(2.7389 39.5517)', 4326), 8.0, 10.0, 'Carretera de l''Aeroport', '07611', true, '{"iata": "PMI"}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440121', NULL, 'Palma Port', 'PORT', ST_GeomFromText('POINT(2.6381 39.5642)', 4326), 0.0, 15.0, 'Estació Marítima', '07015', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440122', NULL, 'Palma Bus Station', 'BUS_STATION', ST_GeomFromText('POINT(2.6535 39.5743)', 4326), 10.0, 10.0, 'Plaça d''Espanya', '07002', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440130', 'a50e8400-e29b-41d4-a716-446655440041', 'Born Pickup Point', 'PICKUP', ST_GeomFromText('POINT(2.6509 39.5712)', 4326), 12.0, 2.0, 'Passeig del Born', '07012', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- MAGALUF
('b50e8400-e29b-41d4-a716-446655440140', 'a50e8400-e29b-41d4-a716-446655440050', 'Magaluf Beach', 'GENERIC', ST_GeomFromText('POINT(2.5344 39.5102)', 4326), 0.0, 5.0, 'Carrer Notari Alemany', '07181', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440141', 'a50e8400-e29b-41d4-a716-446655440050', 'Nikki Beach Mallorca', 'HOTEL', ST_GeomFromText('POINT(2.5339 39.5096)', 4326), 5.0, 3.0, 'Avinguda Notari Alemany', '07181', true, '{"stars": 5}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- MAHÓN
('b50e8400-e29b-41d4-a716-446655440150', NULL, 'Mahón Port', 'PORT', ST_GeomFromText('POINT(4.2657 39.8870)', 4326), 0.0, 10.0, 'Moll de Llevant', '07701', true, '{"second_deepest_harbor_in_world": true}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440151', NULL, 'Menorca Airport', 'AIRPORT', ST_GeomFromText('POINT(4.2187 39.8626)', 4326), 91.0, 10.0, 'Carretera de Sant Climent', '07712', true, '{"iata": "MAH"}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- IBIZA
('b50e8400-e29b-41d4-a716-446655440160', 'a50e8400-e29b-41d4-a716-446655440070', 'Dalt Vila Walls', 'GENERIC', ST_GeomFromText('POINT(1.4323 38.9067)', 4326), 50.0, 5.0, 'Portal de ses Taules', '07800', true, '{"unesco": true}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440161', 'a50e8400-e29b-41d4-a716-446655440070', 'Ibiza Cathedral', 'GENERIC', ST_GeomFromText('POINT(1.4317 38.9077)', 4326), 60.0, 5.0, 'Plaça de la Catedral', '07800', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440162', 'a50e8400-e29b-41d4-a716-446655440071', 'Ibiza Marina', 'PORT', ST_GeomFromText('POINT(1.4361 38.9059)', 4326), 0.0, 10.0, 'Marina Botafoch', '07800', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440163', 'a50e8400-e29b-41d4-a716-446655440071', 'Pacha Ibiza', 'GENERIC', ST_GeomFromText('POINT(1.4407 38.9141)', 4326), 5.0, 3.0, 'Avinguda 8 d''Agost', '07800', true, '{"iconic_nightclub": true}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440164', NULL, 'Ibiza Airport', 'AIRPORT', ST_GeomFromText('POINT(1.3731 38.8729)', 4326), 7.0, 10.0, 'Carretera de l''Aeroport', '07817', true, '{"iata": "IBZ"}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b50e8400-e29b-41d4-a716-446655440165', 'a50e8400-e29b-41d4-a716-446655440071', 'Ibiza Marina Pickup', 'PICKUP', ST_GeomFromText('POINT(1.4365 38.9063)', 4326), 2.0, 2.0, 'Marina Botafoch Entrance', '07800', true, '{}', 'system', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ========== VERIFICATION ==========
SELECT 
    'SPAIN COMPLETE' as dataset,
    (SELECT COUNT(*) FROM geoshape) as geoshapes,
    (SELECT COUNT(*) FROM country) as countries,
    (SELECT COUNT(*) FROM region) as regions,
    (SELECT COUNT(*) FROM city) as cities,
    (SELECT COUNT(*) FROM area) as areas,
    (SELECT COUNT(*) FROM zone) as zones,
    (SELECT COUNT(*) FROM location) as locations;

-- Sample hierarchy query
SELECT 
    co.name as country,
    r.name as region,
    c.name as city,
    a.name as area,
    z.name as zone,
    l.name as location,
    l.location_type,
    get_latitude(l.point) as lat,
    get_longitude(l.point) as lon
FROM location l
LEFT JOIN zone z ON l.zone_id = z.id
LEFT JOIN area a ON z.area_id = a.id
LEFT JOIN city c ON a.city_id = c.id
LEFT JOIN region r ON c.region_id = r.id
LEFT JOIN country co ON r.country_id = co.id
WHERE co.name = 'Spain'
ORDER BY r.name, c.name, l.name
LIMIT 30;
