package com.cjrequena.sample.domain.model.command;

import com.cjrequena.sample.controller.dto.CreateCityRequestDTO;
import com.cjrequena.sample.domain.model.City;
import com.cjrequena.sample.domain.model.vo.PopulationVO;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.TimeZone;
import java.util.UUID;

@Getter
@ToString(callSuper = true)
public class CreateCityCommand extends Command {

  private final City city;

  @Builder
  public CreateCityCommand(@NotNull CreateCityRequestDTO dto) {
    super(UUID.randomUUID());
    this.city = City.create(
      getId(),
      UUID.fromString(dto.getRegionId()),
      UUID.fromString(dto.getGeoShapeId()),
      dto.getName(),
      PopulationVO.of(dto.getPopulation()),
      TimeZone.getTimeZone(dto.getTimeZone()),
      dto.getPostalCode(),
      dto.getIsCapital(),
      dto.getIsActive()
      );
  }
}
