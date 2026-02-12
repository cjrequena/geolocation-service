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
      dto.getGeoShapeId() != null ? UUID.fromString(dto.getGeoShapeId()) : null,
      dto.getName(),
      dto.getAreaType() != null ? dto.getAreaType() : AreaType.GENERIC,
      dto.getPopulation() != null ? PopulationVO.of(dto.getPopulation()) : null,
      dto.getPostalCode(),
      dto.getActive(),
      dto.getMetadata() != null ? MetadataVO.of(dto.getMetadata()) : MetadataVO.empty()
    );
  }
}
