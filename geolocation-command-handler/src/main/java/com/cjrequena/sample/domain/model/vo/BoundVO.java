package com.cjrequena.sample.domain.model.vo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Bounds value object for bounding box.
 */
@Getter
@EqualsAndHashCode
public class BoundVO implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  private final CoordinateVO northEast;
  private final CoordinateVO southWest;

  private BoundVO(CoordinateVO northEast, CoordinateVO southWest) {
    if (northEast == null || southWest == null) {
      throw new IllegalArgumentException("Bounds coordinates cannot be null");
    }
    this.northEast = northEast;
    this.southWest = southWest;
  }

  public static BoundVO of(CoordinateVO northEast, CoordinateVO southWest) {
    return new BoundVO(northEast, southWest);
  }

  /**
   * Creates a BoundVO instance from a JsonNode.
   *
   * @param jsonNode the JsonNode containing bound data
   * @return a new BoundVO instance
   * @throws IllegalArgumentException if the JsonNode is invalid or missing required fields
   */
  public static BoundVO ofJsonNode(JsonNode jsonNode) {
    if (jsonNode == null || jsonNode.isNull()) {
      throw new IllegalArgumentException("JsonNode cannot be null");
    }

    JsonNode northEastNode = jsonNode.get("northEast");
    JsonNode southWestNode = jsonNode.get("southWest");

    if (northEastNode == null || northEastNode.isNull()) {
      throw new IllegalArgumentException("Missing required field: northEast");
    }
    if (southWestNode == null || southWestNode.isNull()) {
      throw new IllegalArgumentException("Missing required field: southWest");
    }

    CoordinateVO northEast = CoordinateVO.ofJsonNode(northEastNode);
    CoordinateVO southWest = CoordinateVO.ofJsonNode(southWestNode);

    return new BoundVO(northEast, southWest);
  }

  /**
   * Converts this BoundVO to a JsonNode.
   *
   * @param objectMapper the ObjectMapper to use for creating the JsonNode
   * @return a JsonNode representation of this BoundVO
   */
  public JsonNode toJsonNode(ObjectMapper objectMapper) {
    if (objectMapper == null) {
      throw new IllegalArgumentException("ObjectMapper cannot be null");
    }

    ObjectNode node = objectMapper.createObjectNode();
    node.set("northEast", CoordinateVO.toJsonNode(northEast, objectMapper));
    node.set("southWest", CoordinateVO.toJsonNode(southWest, objectMapper));

    return node;
  }

  /**
   * Converts this BoundVO to a JsonNode using a default ObjectMapper.
   *
   * @return a JsonNode representation of this BoundVO
   */
  public JsonNode toJsonNode() {
    return toJsonNode(new ObjectMapper());
  }


  public boolean contains(CoordinateVO point) {
    if (point == null) {
      return false;
    }
    return point.getLatitude().compareTo(southWest.getLatitude()) >= 0 &&
      point.getLatitude().compareTo(northEast.getLatitude()) <= 0 &&
      point.getLongitude().compareTo(southWest.getLongitude()) >= 0 &&
      point.getLongitude().compareTo(northEast.getLongitude()) <= 0;
  }

  @Override
  public String toString() {
    return String.format("Bounds[NE=%s, SW=%s]", northEast, southWest);
  }
}
