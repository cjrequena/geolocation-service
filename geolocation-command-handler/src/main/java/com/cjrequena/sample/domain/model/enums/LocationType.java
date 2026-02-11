package com.cjrequena.sample.domain.model.enums;

import java.util.Arrays;

public enum LocationType {

  HOTEL("HOTEL"),
  AIRPORT("AIRPORT"),
  BUS_STATION("BUS_STATION"),
  PORT("PORT"),
  PICKUP("PICKUP"),
  GENERIC("GENERIC"),
  OTHER("OTHER");

  private final String value;

  LocationType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  /**
   * Default zone type.
   */
  public static LocationType defaultType() {
    return OTHER;
  }

  /**
   * Safe factory from String (case-insensitive).
   * Useful at system boundaries.
   */
  public static LocationType from(String value) {
    if (value == null) {
      return defaultType();
    }
    return Arrays.stream(values())
      .filter(t -> t.value.equalsIgnoreCase(value))
      .findFirst()
      .orElseThrow(() ->
        new IllegalArgumentException("Unknown ZoneType: " + value)
      );
  }
}
