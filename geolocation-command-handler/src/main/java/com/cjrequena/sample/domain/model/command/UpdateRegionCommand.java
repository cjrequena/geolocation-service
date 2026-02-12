package com.cjrequena.sample.domain.model.command;

import com.cjrequena.sample.controller.dto.UpdateRegionRequestDTO;
import com.cjrequena.sample.domain.model.Region;
import com.cjrequena.sample.domain.model.enums.RegionType;
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
public class UpdateRegionCommand extends Command {

  private final Region region;

  @Builder
  public UpdateRegionCommand(@NotNull UUID id, @NotNull UpdateRegionRequestDTO dto) {
    super(id);
    this.region = Region.create(
      getId(),
      UUID.fromString(dto.getCountryId()),
      UUID.fromString(dto.getGeoShapeId()),
      dto.getName(),
      dto.getCode(),
      RegionType.from(dto.getRegionType()),
      dto.getPopulation() != null ? PopulationVO.of(dto.getPopulation()) : null,
      dto.getTimeZone() != null ? TimeZone.getTimeZone(dto.getTimeZone()) : null,
      dto.getActive() != null ? dto.getActive() : Boolean.TRUE,
      dto.getMetadata() != null ? MetadataVO.of(dto.getMetadata()) : MetadataVO.empty()
    );
  }
}
