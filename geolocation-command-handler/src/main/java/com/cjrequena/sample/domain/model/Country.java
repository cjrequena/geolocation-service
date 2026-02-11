package com.cjrequena.sample.domain.model;

import com.cjrequena.sample.domain.model.vo.AuditInfoVO;
import com.cjrequena.sample.domain.model.vo.IsoCodeVO;
import com.cjrequena.sample.domain.model.vo.PopulationVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Country Domain Aggregate.
 *
 * Represents a country with ISO codes and geographic/demographic information.
 * This is the root aggregate for country-related operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Country {

  private UUID id;
  private String name;
  private IsoCodeVO isoCode;
  private String phoneCode;
  private String currencyCode;
  private String capital;
  private PopulationVO population;
  private Boolean active;
  private AuditInfoVO auditInfo;

  /**
   * Factory method to create a new country.
   */
  public static Country create(
    UUID id,
    String name,
    IsoCodeVO isoCode,
    String phoneCode,
    String currencyCode,
    String capital,
    PopulationVO population
  ) {

    validateCreation(id, name, isoCode);

    return Country.builder()
      .id(id)
      .name(name)
      .isoCode(isoCode)
      .phoneCode(phoneCode)
      .currencyCode(currencyCode)
      .active(Boolean.TRUE)
      .auditInfo(AuditInfoVO.create())
      .build();
  }

  /**
   * Update basic country information.
   */
  public void updateBasicInfo(String name, String phoneCode, String currencyCode) {
    if (name != null) {
      this.name = name;
    }
    if (phoneCode != null) {
      this.phoneCode = phoneCode;
    }
    if (currencyCode != null) {
      this.currencyCode = currencyCode;
    }
    this.auditInfo = this.auditInfo.update();
  }

  /**
   * Set the capital city.
   */
  public void setCapital(String capital) {
    this.capital = capital;
    this.auditInfo = this.auditInfo.update();
  }

  /**
   * Update population.
   */
  public void updatePopulation(PopulationVO population) {
    if (population == null || population.getValue() < 0) {
      throw new IllegalArgumentException("PopulationVO must be non-negative");
    }
    this.population = population;
    this.auditInfo = this.auditInfo.update();
  }

  /**
   * Activate the country.
   */
  public void activate() {
    this.active = Boolean.TRUE;
    this.auditInfo = this.auditInfo.update();
  }

  /**
   * Deactivate the country.
   */
  public void deactivate() {
    this.active = Boolean.FALSE;
    this.auditInfo = this.auditInfo.update();
  }

  /**
   * Check if country is active.
   */
  public boolean isActive() {
    return this.active != null && this.active.equals(Boolean.TRUE);
  }

  /**
   * Get ISO Alpha-2 code.
   */
  public String getIsoAlpha2() {
    return this.isoCode != null ? this.isoCode.getAlpha2() : null;
  }

  /**
   * Get ISO Alpha-3 code.
   */
  public String getIsoAlpha3() {
    return this.isoCode != null ? this.isoCode.getAlpha3() : null;
  }

  /**
   * Check if country has a capital defined.
   */
  public boolean hasCapital() {
    return this.capital != null;
  }

  /**
   * Check if country has population data.
   */
  public boolean hasPopulationData() {
    return this.population != null && this.population.getValue() > 0;
  }

  // Validation methods

  private static void validateCreation(UUID id, String name, IsoCodeVO isoCode) {
    if (id == null) {
      throw new IllegalArgumentException("Country ID cannot be null");
    }
    if (name == null) {
      throw new IllegalArgumentException("Country name cannot be null");
    }
    if (isoCode == null) {
      throw new IllegalArgumentException("ISO code cannot be null");
    }
  }

  @Override
  public String toString() {
    return String.format("Country{id=%s, name=%s, isoCode=%s}",
      id, name, isoCode != null ? isoCode.getAlpha2() : "N/A");
  }
}
