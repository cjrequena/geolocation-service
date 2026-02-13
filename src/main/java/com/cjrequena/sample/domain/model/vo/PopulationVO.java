package com.cjrequena.sample.domain.model.vo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.io.Serial;
import java.io.Serializable;

/**
 * Population value object.
 */
@Builder
@Getter
@Jacksonized
@EqualsAndHashCode
public class PopulationVO implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  private final Long value;

  @Builder
  private PopulationVO(Long value) {
    if (value == null || value < 0) {
      throw new IllegalArgumentException("Population must be non-negative");
    }
    this.value = value;
  }

  public static PopulationVO of(Long value) {
    return PopulationVO
      .builder()
      .value(value)
      .build();
  }

  public boolean isGreaterThan(PopulationVO other) {
    return this.value > other.value;
  }

  public boolean isLessThan(PopulationVO other) {
    return this.value < other.value;
  }

  @Override
  public String toString() {
    return String.format("%,d", value);
  }
}
