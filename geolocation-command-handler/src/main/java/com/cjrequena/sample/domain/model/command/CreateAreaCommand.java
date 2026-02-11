package com.cjrequena.sample.domain.model.command;

import com.cjrequena.sample.controller.dto.CreateAreaRequestDTO;
import com.cjrequena.sample.domain.model.Area;
import com.cjrequena.sample.domain.model.enums.AreaType;
import com.cjrequena.sample.domain.model.vo.MetadataVO;
import com.cjrequena.sample.domain.model.vo.PopulationVO;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@ToString(callSuper = true)
public class CreateAreaCommand extends Command {

  private final Area area;

  @Builder
  public CreateAreaCommand(@NotNull CreateAreaRequestDTO dto) {
    super(UUID.randomUUID());
    this.area = Area.create(
      getId(),
      UUID.fromString(dto.getCityId()),
      UUID.fromString(dto.getGeoShapeId()),
      dto.getName(),
      AreaType.from(dto.getAreaType()),
      PopulationVO.of(dto.getPopulation()),
      dto.getPostalCode(),
      dto.getActive(),
      dto.getMetadata()!=null ? MetadataVO.of(dto.getMetadata()) : MetadataVO.empty()

    );
  }
}
