package com.cjrequena.sample.domain.model.vo;

import com.cjrequena.sample.shared.common.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Bounds value object for bounding box.
 */
@Getter
@Builder
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

    JsonNode northEastNode = jsonNode.get("north_east");
    JsonNode southWestNode = jsonNode.get("south_west");

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
   * @return a JsonNode representation of this BoundVO
   */
  public JsonNode toJsonNode() {
    ObjectNode node = JsonUtil.getObjectMapper().createObjectNode();
    node.set("north_east", CoordinateVO.toJsonNode(northEast));
    node.set("south_west", CoordinateVO.toJsonNode(southWest));

    return node;
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
