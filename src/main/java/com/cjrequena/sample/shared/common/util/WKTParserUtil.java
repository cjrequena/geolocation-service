package com.cjrequena.sample.shared.common.util;

import com.cjrequena.sample.domain.model.enums.GeometryType;
import lombok.experimental.UtilityClass;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class for parsing WKT (Well-Known Text) strings to JTS {@link Geometry} objects
 * and writing JTS {@link Geometry} objects back to WKT strings.
 *
 * <p>Supported geometry types:
 * <ul>
 *   <li>{@link GeometryType#POINT}     → {@link Point}</li>
 *   <li>{@link GeometryType#CIRCLE}    → {@link Polygon} (approximated as polygon via buffer)</li>
 *   <li>{@link GeometryType#RECTANGLE} → {@link Polygon} (4-vertex closed ring)</li>
 *   <li>{@link GeometryType#POLYGON}   → {@link Polygon}</li>
 *   <li>{@link GeometryType#LINE}      → {@link LineString}</li>
 * </ul>
 *
 * <p><b>Important note on CIRCLE:</b> WKT has no native circle type. This class represents
 * circles using the custom format {@code CIRCLE(longitude latitude radiusMeters)}, 
 * e.g. {@code CIRCLE(-73.985428 40.748817 500.0)}.
 * When parsing, a circle is approximated as a buffered {@link Point} with configurable precision.
 * When writing, a circle Geometry (which is stored as a Polygon in JTS) writes back as a
 * standard {@code POLYGON(...)} WKT — store the original CIRCLE WKT if you need to round-trip it.
 * 
 * <p>Legacy format {@code CIRCLE(cx cy, radius)} with comma separator is also supported for
 * backward compatibility.
 */
@UtilityClass
public class WKTParserUtil {

  private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);
  private static final int CIRCLE_SEGMENTS = 64; // number of segments to approximate a circle

  // Maps each GeometryType to its WKT → Geometry parsing function
  private static final Map<GeometryType, Function<String, Geometry>> PARSERS = new EnumMap<>(GeometryType.class);

  static {
    PARSERS.put(GeometryType.POINT, WKTParserUtil::parsePoint);
    PARSERS.put(GeometryType.CIRCLE, WKTParserUtil::parseCircle);
    PARSERS.put(GeometryType.RECTANGLE, WKTParserUtil::parseRectangle);
    PARSERS.put(GeometryType.POLYGON, WKTParserUtil::parsePolygon);
    PARSERS.put(GeometryType.LINE, WKTParserUtil::parseLine);
  }

  // ─────────────────────────────────────────────────────────────────
  // Public API
  // ─────────────────────────────────────────────────────────────────

  /**
   * Parses a WKT string into a JTS {@link Geometry} based on the expected {@link GeometryType}.
   *
   * @param wkt          the WKT string to parse
   * @param geometryType the expected geometry type
   * @return parsed JTS {@link Geometry}
   * @throws IllegalArgumentException if the WKT is null, blank, or cannot be parsed
   */
  public static Geometry fromWKT(String wkt, GeometryType geometryType) {
    if (wkt == null || wkt.isBlank()) {
      throw new IllegalArgumentException("WKT string must not be null or blank.");
    }
    Function<String, Geometry> parser = PARSERS.get(geometryType);
    if (parser == null) {
      throw new IllegalArgumentException("Unsupported GeometryType: " + geometryType);
    }
    return parser.apply(wkt.trim());
  }

  /**
   * Writes a JTS {@link Geometry} to its WKT string representation.
   *
   * @param geometry the geometry to serialize
   * @return WKT string
   * @throws IllegalArgumentException if geometry is null
   */
  public static String toWKT(Geometry geometry) {
    if (geometry == null) {
      throw new IllegalArgumentException("Geometry must not be null.");
    }
    return new WKTWriter().write(geometry);
  }

  /**
   * Resolves the {@link GeometryType} enum value from a JTS {@link Geometry} instance.
   *
   * @param geometry the JTS geometry
   * @return the corresponding {@link GeometryType}
   * @throws IllegalArgumentException if the geometry type is not supported
   */
  public static GeometryType resolveGeometryType(Geometry geometry) {
    return switch (geometry.getGeometryType()) {
      case "Point" -> GeometryType.POINT;
      case "LineString" -> GeometryType.LINE;
      case "Polygon" -> GeometryType.POLYGON; // Circles stored as Polygon are indistinguishable here
      default -> throw new IllegalArgumentException("Unsupported JTS geometry type: " + geometry.getGeometryType());
    };
  }

  // ─────────────────────────────────────────────────────────────────
  // Private parsers
  // ─────────────────────────────────────────────────────────────────

  /**
   * Parses a WKT POINT string.
   * Expected format: {@code POINT (x y)}
   */
  private static Point parsePoint(String wkt) {
    Geometry geometry = readWKT(wkt);
    if (!(geometry instanceof Point)) {
      throw new IllegalArgumentException("Expected POINT geometry but got: " + geometry.getGeometryType());
    }
    return (Point) geometry;
  }

  /**
   * Parses a custom CIRCLE WKT string and approximates it as a buffered polygon.
   * 
   * <p>Supports two formats:</p>
   * <ul>
   *   <li>Standard format: {@code CIRCLE(longitude latitude radiusMeters)} - space-separated</li>
   *   <li>Legacy format: {@code CIRCLE(cx cy, radius)} - comma-separated (deprecated)</li>
   * </ul>
   * 
   * <p>Examples:</p>
   * <ul>
   *   <li>{@code CIRCLE(-73.985428 40.748817 500.0)} - 500m radius circle</li>
   *   <li>{@code CIRCLE(10.0 20.0, 5.0)} - legacy format</li>
   * </ul>
   * 
   * @param wkt the CIRCLE WKT string
   * @return Polygon approximation of the circle
   * @throws IllegalArgumentException if format is invalid
   */
  private static Polygon parseCircle(String wkt) {
    String upper = wkt.toUpperCase();
    if (!upper.startsWith("CIRCLE")) {
      throw new IllegalArgumentException("Expected CIRCLE WKT format. Got: " + wkt);
    }
    
    // Extract content inside parentheses
    int start = wkt.indexOf('(');
    int end = wkt.lastIndexOf(')');
    if (start == -1 || end == -1 || end <= start) {
      throw new IllegalArgumentException("Malformed CIRCLE WKT - missing or invalid parentheses: " + wkt);
    }
    
    String inner = wkt.substring(start + 1, end).trim();
    
    double cx, cy, radius;
    
    // Try standard format first: CIRCLE(lon lat radius) - space-separated
    if (!inner.contains(",")) {
      String[] parts = inner.split("\\s+");
      if (parts.length != 3) {
        throw new IllegalArgumentException(
          String.format(
            "CIRCLE WKT must have format CIRCLE(longitude latitude radiusMeters). Expected 3 values, got %d: %s",
            parts.length, wkt
          )
        );
      }
      
      try {
        cx = Double.parseDouble(parts[0]);
        cy = Double.parseDouble(parts[1]);
        radius = Double.parseDouble(parts[2]);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Invalid numeric values in CIRCLE WKT: " + wkt, e);
      }
      
      // Validate coordinate ranges
      if (cy < -90 || cy > 90) {
        throw new IllegalArgumentException(
          String.format("Invalid latitude value: %f (must be between -90 and 90)", cy)
        );
      }
      if (cx < -180 || cx > 180) {
        throw new IllegalArgumentException(
          String.format("Invalid longitude value: %f (must be between -180 and 180)", cx)
        );
      }
    } 
    // Try legacy format: CIRCLE(cx cy, radius) - comma-separated
    else {
      String[] parts = inner.split(",");
      if (parts.length != 2) {
        throw new IllegalArgumentException(
          "CIRCLE WKT legacy format must be CIRCLE(cx cy, radius). Got: " + wkt
        );
      }
      
      String[] centerParts = parts[0].trim().split("\\s+");
      if (centerParts.length != 2) {
        throw new IllegalArgumentException(
          "CIRCLE center must be two space-separated coordinates. Got: " + parts[0]
        );
      }
      
      try {
        cx = Double.parseDouble(centerParts[0]);
        cy = Double.parseDouble(centerParts[1]);
        radius = Double.parseDouble(parts[1].trim());
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Invalid numeric values in CIRCLE WKT: " + wkt, e);
      }
    }
    
    // Validate radius
    if (radius <= 0) {
      throw new IllegalArgumentException(
        String.format("Circle radius must be positive. Got: %f", radius)
      );
    }

    Point center = GEOMETRY_FACTORY.createPoint(new Coordinate(cx, cy));
    
    // Convert radius from meters to degrees for JTS buffer operation
    // At the equator: 1 degree ≈ 111,320 meters
    // For latitude, this is constant
    // For longitude, it varies by cos(latitude)
    // We use an approximation at the circle's center latitude
    double radiusInDegrees = radius / 111320.0; // Approximate conversion
    
    // Buffer the center point to approximate a circle polygon
    return (Polygon) center.buffer(radiusInDegrees, CIRCLE_SEGMENTS);
  }

  /**
   * Parses a WKT POLYGON string expected to represent a rectangle (exactly 4 distinct corners + closing coord).
   * Expected format: {@code POLYGON ((x1 y1, x2 y2, x3 y3, x4 y4, x1 y1))}
   */
  private static Polygon parseRectangle(String wkt) {
    Geometry geometry = readWKT(wkt);
    if (!(geometry instanceof Polygon polygon)) {
      throw new IllegalArgumentException("Expected POLYGON (rectangle) geometry but got: " + geometry.getGeometryType());
    }
    // A rectangle ring has 5 coordinates: 4 corners + closing coordinate
    if (polygon.getExteriorRing().getNumPoints() != 5) {
      throw new IllegalArgumentException(
        "Expected a rectangular POLYGON with exactly 4 corners (5 coords incl. closing). Got "
          + (polygon.getExteriorRing().getNumPoints() - 1) + " corners.");
    }
    return polygon;
  }

  /**
   * Parses a WKT POLYGON string.
   * Expected format: {@code POLYGON ((x1 y1, x2 y2, ..., x1 y1))}
   */
  private static Polygon parsePolygon(String wkt) {
    Geometry geometry = readWKT(wkt);
    if (!(geometry instanceof Polygon)) {
      throw new IllegalArgumentException("Expected POLYGON geometry but got: " + geometry.getGeometryType());
    }
    return (Polygon) geometry;
  }

  /**
   * Parses a WKT LINESTRING string.
   * Expected format: {@code LINESTRING (x1 y1, x2 y2, ...)}
   */
  private static LineString parseLine(String wkt) {
    Geometry geometry = readWKT(wkt);
    if (!(geometry instanceof LineString)) {
      throw new IllegalArgumentException("Expected LINESTRING geometry but got: " + geometry.getGeometryType());
    }
    return (LineString) geometry;
  }

  // ─────────────────────────────────────────────────────────────────
  // Internal helpers
  // ─────────────────────────────────────────────────────────────────

  /**
   * Delegates WKT parsing to the JTS {@link WKTReader}.
   */
  private static Geometry readWKT(String wkt) {
    try {
      return new WKTReader(GEOMETRY_FACTORY).read(wkt);
    } catch (ParseException e) {
      throw new IllegalArgumentException("Failed to parse WKT string: " + wkt, e);
    }
  }
}
