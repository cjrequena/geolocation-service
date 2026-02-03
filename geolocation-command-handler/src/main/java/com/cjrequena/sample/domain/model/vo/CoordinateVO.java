package com.cjrequena.sample.domain.model.vo;

import com.cjrequena.sample.shared.common.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Coordinates value object representing latitude and longitude.
 */
@Getter
@SuperBuilder
@EqualsAndHashCode
@ToString
public class CoordinateVO implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private final BigDecimal latitude;
  private final BigDecimal longitude;

  private CoordinateVO(BigDecimal latitude, BigDecimal longitude) {
    validateLatitude(latitude);
    validateLongitude(longitude);
    this.latitude = latitude.setScale(6, RoundingMode.HALF_UP);
    this.longitude = longitude.setScale(6, RoundingMode.HALF_UP);
  }

  public static CoordinateVO of(BigDecimal latitude, BigDecimal longitude) {
    return new CoordinateVO(latitude, longitude);
  }

  public static CoordinateVO of(double latitude, double longitude) {
    return new CoordinateVO(BigDecimal.valueOf(latitude), BigDecimal.valueOf(longitude));
  }

  /**
   * Creates a CoordinateVO instance from a JsonNode.
   *
   * @param jsonNode the JsonNode containing coordinate data
   * @return a new CoordinateVO instance
   * @throws IllegalArgumentException if the JsonNode is invalid or missing required fields
   */
  public static CoordinateVO ofJsonNode(JsonNode jsonNode) {
    if (jsonNode == null || jsonNode.isNull()) {
      throw new IllegalArgumentException("Coordinate JsonNode cannot be null");
    }

    JsonNode latNode = jsonNode.get("latitude");
    JsonNode lonNode = jsonNode.get("longitude");

    if (latNode == null || latNode.isNull()) {
      throw new IllegalArgumentException("Missing required field: latitude");
    }
    if (lonNode == null || lonNode.isNull()) {
      throw new IllegalArgumentException("Missing required field: longitude");
    }

    BigDecimal latitude = new BigDecimal(latNode.asText());
    BigDecimal longitude = new BigDecimal(lonNode.asText());

    return CoordinateVO.of(latitude, longitude);
  }

  /**
   * Converts a CoordinateVO to a JsonNode.
   *
   * @param coordinate the CoordinateVO to convert
   * @return a JsonNode representation of the coordinate
   */
  public static ObjectNode toJsonNode(CoordinateVO coordinate) {
    if (coordinate == null) {
      throw new IllegalArgumentException("Coordinate cannot be null");
    }
    final ObjectMapper objectMapper = JsonUtil.getObjectMapper();
    ObjectNode node = objectMapper.createObjectNode();
    node.put("latitude", coordinate.getLatitude());
    node.put("longitude", coordinate.getLongitude());
    return node;
  }

  private void validateLatitude(BigDecimal latitude) {
    if (latitude == null) {
      throw new IllegalArgumentException("Latitude cannot be null");
    }
    if (latitude.compareTo(BigDecimal.valueOf(-90)) < 0 || latitude.compareTo(BigDecimal.valueOf(90)) > 0) {
      throw new IllegalArgumentException("Latitude must be between -90 and 90 degrees");
    }
  }

  private void validateLongitude(BigDecimal longitude) {
    if (longitude == null) {
      throw new IllegalArgumentException("Longitude cannot be null");
    }
    if (longitude.compareTo(BigDecimal.valueOf(-180)) < 0 || longitude.compareTo(BigDecimal.valueOf(180)) > 0) {
      throw new IllegalArgumentException("Longitude must be between -180 and 180 degrees");
    }
  }

  public double getLatitudeAsDouble() {
    return latitude.doubleValue();
  }

  public double getLongitudeAsDouble() {
    return longitude.doubleValue();
  }

}
