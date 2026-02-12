package com.cjrequena.sample.domain.model.enums;

import java.util.Arrays;

public enum RegionType {

  STATE("STATE"),
  PROVINCE("PROVINCE"),
  TERRITORY("TERRITORY"),
  DEPARTMENT("DEPARTMENT"),
  REGION("REGION"),
  AUTONOMOUS_COMMUNITY("AUTONOMOUS_COMMUNITY"),
  FEDERAL_DISTRICT("FEDERAL_DISTRICT"),
  SPECIAL_ADMINISTRATIVE_REGION("SPECIAL_ADMINISTRATIVE_REGION"),
  GENERIC("GENERIC");

  private final String value;

  RegionType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  /**
   * Default region type.
   */
  public static RegionType defaultType() {
    return GENERIC;
  }

  /**
   * Safe factory from String (case-insensitive).
   */
  public static RegionType from(String value) {
    if (value == null) {
      return defaultType();
    }
    return Arrays.stream(values())
      .filter(t -> t.value.equalsIgnoreCase(value))
      .findFirst()
      .orElseThrow(() ->
        new IllegalArgumentException("Unknown RegionType: " + value)
      );
  }
}
