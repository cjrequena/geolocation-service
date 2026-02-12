package com.cjrequena.sample.domain.model.command;

import com.cjrequena.sample.controller.dto.UpdateCountryRequestDTO;
import com.cjrequena.sample.domain.model.Country;
import com.cjrequena.sample.domain.model.vo.IsoCodeVO;
import com.cjrequena.sample.domain.model.vo.MetadataVO;
import com.cjrequena.sample.domain.model.vo.PopulationVO;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@ToString(callSuper = true)
public class UpdateCountryCommand extends Command {

  private final Country country;

  @Builder
  public UpdateCountryCommand(@Nonnull UUID id, @NotNull UpdateCountryRequestDTO dto) {
    super(id);
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
