package com.cjrequena.sample.domain.model.command;

import com.cjrequena.sample.controller.dto.CreateRegionRequestDTO;
import com.cjrequena.sample.domain.model.Region;
import com.cjrequena.sample.domain.model.enums.RegionType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@ToString(callSuper = true)
public class CreateRegionCommand extends Command {

  private final Region region;

  @Builder
  public CreateRegionCommand(@NotNull CreateRegionRequestDTO dto) {
    super(UUID.randomUUID());
    this.region = Region.create(
      getId(),
      UUID.fromString(dto.getCountryId()),
      dto.getName(),
      dto.getCode(),
      RegionType.from(dto.getRegionType()));
  }
}
