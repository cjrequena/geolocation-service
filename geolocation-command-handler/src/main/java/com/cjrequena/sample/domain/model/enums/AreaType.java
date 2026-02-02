package com.cjrequena.sample.domain.model.enums;

import java.util.Arrays;

public enum AreaType {

  DISTRICT("DISTRICT"),
  BOROUGH("BOROUGH"),
  NEIGHBORHOOD("NEIGHBORHOOD"),
  WARD("WARD"),
  ZONE("ZONE"),
  SECTOR("SECTOR"),
  PARISH("PARISH"),
  SUBURB("SUBURB"),
  QUARTER("QUARTER"),
  OTHER("OTHER");

  private final String value;

  AreaType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  /**
   * Default area type.
   */
  public static AreaType defaultType() {
    return OTHER;
  }

  /**
   * Safe factory from String (case-insensitive).
   * Useful at boundaries (API, persistence, messaging).
   */
  public static AreaType from(String value) {
    if (value == null) {
      return defaultType();
    }
    return Arrays.stream(values())
      .filter(t -> t.value.equalsIgnoreCase(value))
      .findFirst()
      .orElseThrow(() ->
        new IllegalArgumentException("Unknown AreaType: " + value)
      );
  }
}
