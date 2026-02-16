package com.cjrequena.sample.domain.model.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum AreaType {

  DISTRICT("DISTRICT"),
  NEIGHBORHOOD("NEIGHBORHOOD"),
  SECTOR("SECTOR"),
  PARISH("PARISH"),
  SUBURB("SUBURB"),
  QUARTER("QUARTER"),
  GENERIC("GENERIC");

  private final String value;

  AreaType(String value) {
    this.value = value;
  }

  /**
   * Default area type.
   */
  public static AreaType defaultType() {
    return GENERIC;
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
