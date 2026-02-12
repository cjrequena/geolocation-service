package com.cjrequena.sample.domain.model.command;

import com.cjrequena.sample.controller.dto.CreateZoneRequestDTO;
import com.cjrequena.sample.domain.model.Zone;
import com.cjrequena.sample.domain.model.enums.ZoneType;
import com.cjrequena.sample.domain.model.vo.MetadataVO;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@ToString(callSuper = true)
public class CreateZoneCommand extends Command {

  private final Zone zone;

  @Builder
  public CreateZoneCommand(@NotNull CreateZoneRequestDTO dto) {
    super(UUID.randomUUID());
    this.zone = Zone.create(
      getId(),
      UUID.fromString(dto.getAreaId()),
      UUID.fromString(dto.getGeoShapeId()),
      dto.getName(),
      dto.getZoneType() != null ? dto.getZoneType() : ZoneType.GENERIC,
      dto.getPostalCode(),
      dto.getActive() != null ? dto.getActive() : Boolean.TRUE,
      dto.getMetadata() != null ? MetadataVO.of(dto.getMetadata()) : MetadataVO.empty()
    );
  }
}
