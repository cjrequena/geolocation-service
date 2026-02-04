package com.cjrequena.sample;

import com.cjrequena.sample.domain.mapper.GeoShapeMapper;
import com.cjrequena.sample.domain.model.aggregate.GeoShape;
import com.cjrequena.sample.domain.model.enums.GeometryType;
import com.cjrequena.sample.domain.model.vo.GeometryVO;
import com.cjrequena.sample.domain.model.vo.MetadataVO;
import com.cjrequena.sample.domain.model.vo.PointVO;
import com.cjrequena.sample.persistence.entity.GeoShapeEntity;
import com.cjrequena.sample.persistence.repository.GeoShapeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SpringBootApplication(scanBasePackages = {
  "com.cjrequena.sample"
})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Log4j2
public class MainApplication implements CommandLineRunner {

  private final GeoShapeMapper geoShapeMapper;
  private final GeoShapeRepository geoShapeRepository;
  public static void main(String... args) {
    SpringApplication.run(MainApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    PointVO point = PointVO.of(37.12474775267529, -3.655513130688699);
    Map<String, Object> metadataMap = new HashMap<>();
    metadataMap.put("trace_id", UUID.randomUUID());
    metadataMap.put("created_by", "Carlos Requena");
    final MetadataVO metadataVO = MetadataVO.fromMap(metadataMap);
    GeoShape geoShape = GeoShape
      .builder()
      .geometryType(GeometryType.POINT)
      .geometry(GeometryVO.ofPoint(point))
      .metadata(metadataVO)
      .build();

    final GeoShapeEntity geoShapeEntity = this.geoShapeMapper.toEntity(geoShape);
    log.debug("{}", geoShapeEntity);
    this.geoShapeRepository.save(geoShapeEntity);
  }
}
