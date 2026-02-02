package com.cjrequena.sample.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
  componentModel = "spring",
  unmappedTargetPolicy = ReportingPolicy.IGNORE,
  nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
  uses = {}
)
public interface RegionMapper {
  // ==========================================
  // Domain <-> Entity mappings
  // ==========================================

}
