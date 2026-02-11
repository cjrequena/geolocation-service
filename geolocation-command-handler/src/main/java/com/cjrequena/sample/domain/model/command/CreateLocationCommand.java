package com.cjrequena.sample.domain.model.command;

import com.cjrequena.sample.controller.dto.CreateLocationRequestDTO;
import com.cjrequena.sample.domain.model.Location;
import com.cjrequena.sample.domain.model.vo.AltitudeVO;
import com.cjrequena.sample.domain.model.vo.GpsAccuracyVO;
import com.cjrequena.sample.domain.model.vo.MetadataVO;
import com.cjrequena.sample.domain.model.vo.PointVO;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@ToString(callSuper = true)
public class CreateLocationCommand extends Command {

  private final Location location;

  @Builder
  public CreateLocationCommand(@NotNull CreateLocationRequestDTO dto) {
    super(UUID.randomUUID());
    this.location = Location.create(
      getId(),
      UUID.fromString(dto.getZoneId()),
      dto.getName(),
      PointVO.of(dto.getLatitude(), dto.getLongitude()),
      AltitudeVO.of(dto.getAltitudeMeters()),
      GpsAccuracyVO.of(dto.getAccuracyMeters()),
      dto.getAddress(),
      dto.getPostalCode(),
      dto.getActive(),
      dto.getMetadata()!=null ? MetadataVO.of(dto.getMetadata()) : MetadataVO.empty()
    );
  }
}
