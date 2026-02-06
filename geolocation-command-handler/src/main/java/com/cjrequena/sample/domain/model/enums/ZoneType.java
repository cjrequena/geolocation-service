package com.cjrequena.sample.domain.model.enums;

import java.util.Arrays;

public enum ZoneType {

  BLOCK("BLOCK"),
  SECTOR("SECTOR"),
  PRECINCT("PRECINCT"),
  CELL("CELL"),
  PARCEL("PARCEL"),
  LOT("LOT"),
  SUBZONE("SUBZONE"),
  MICRODISTRICT("MICRODISTRICT"),
  RESIDENTIAL("RESIDENTIAL"),
  COMMERCIAL("COMMERCIAL"),
  INDUSTRIAL("INDUSTRIAL"),
  OTHER("OTHER");

  private final String value;

  ZoneType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  /**
   * Default zone type.
   */
  public static ZoneType defaultType() {
    return OTHER;
  }

  /**
   * Safe factory from String (case-insensitive).
   * Useful at system boundaries.
   */
  public static ZoneType from(String value) {
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
