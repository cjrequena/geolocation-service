package com.cjrequena.sample.domain.model.command;

import com.cjrequena.sample.controller.dto.CreateCountryRequestDTO;
import com.cjrequena.sample.domain.model.Country;
import com.cjrequena.sample.domain.model.vo.IsoCodeVO;
import com.cjrequena.sample.domain.model.vo.MetadataVO;
import com.cjrequena.sample.domain.model.vo.PopulationVO;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@ToString(callSuper = true)
public class CreateCountryCommand extends Command {

  private final Country country;

  @Builder
  public CreateCountryCommand(@NotNull CreateCountryRequestDTO dto) {
    super(UUID.randomUUID());
    //    this.country = Country
    //      .builder()
    //      .id(getId())
    //      .name(dto.getName())
    //      .isoCode(IsoCodeVO
    //        .builder()
    //        .alpha2(dto.getIsoCodeAlpha2())
    //        .alpha3(dto.getIsoCodeAlpha3())
    //        .numeric(dto.getIsoCodeNumeric())
    //        .build())
    //      .phoneCode(dto.getPhoneCode())
    //      .currencyCode(dto.getCurrencyCode())
    //      .capital(dto.getCapital())
    //      .population(
    //        PopulationVO
    //          .builder()
    //          .value(dto.getPopulation())
    //          .build())
    //      .auditInfo(AuditInfoVO.create())
    //      .build();

    this.country = Country.create(
      getId(),
      dto.getName(),
      IsoCodeVO.of(dto.getIsoCodeAlpha2(),dto.getIsoCodeAlpha3(),dto.getIsoCodeNumeric()),
      dto.getPhoneCode(),
      dto.getCurrencyCode(),
      dto.getCapital(),
      dto.getPopulation() != null ? PopulationVO.of(dto.getPopulation()) : null,
      dto.getActive() != null ? dto.getActive() : Boolean.TRUE,
      dto.getMetadata() != null ? MetadataVO.of(dto.getMetadata()) : MetadataVO.empty());
  }
}
