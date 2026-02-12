package com.cjrequena.sample.domain.model.command;

import com.cjrequena.sample.controller.dto.CreateCityRequestDTO;
import com.cjrequena.sample.domain.model.City;
import com.cjrequena.sample.domain.model.vo.MetadataVO;
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
      dto.getRegionId() != null ? UUID.fromString(dto.getRegionId()) : null,
      dto.getGeoShapeId() != null ? UUID.fromString(dto.getGeoShapeId()) : null,
      dto.getName(),
      dto.getPopulation() != null ? PopulationVO.of(dto.getPopulation()) : null,
      dto.getTimeZone() != null ?TimeZone.getTimeZone(dto.getTimeZone()): null,
      dto.getPostalCode(),
      dto.getCapital(),
      dto.getActive(),
      dto.getMetadata() != null ? MetadataVO.of(dto.getMetadata()) : MetadataVO.empty()
    );
  }
}
