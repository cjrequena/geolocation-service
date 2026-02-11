package com.cjrequena.sample;

import com.cjrequena.sample.domain.mapper.GeoShapeMapper;
import com.cjrequena.sample.domain.model.GeoShape;
import com.cjrequena.sample.domain.model.enums.GeometryType;
import com.cjrequena.sample.domain.model.vo.CoordinateVO;
import com.cjrequena.sample.domain.model.vo.MetadataVO;
import com.cjrequena.sample.persistence.repository.GeoShapeRepository;
import com.cjrequena.sample.service.AreaService;
import com.cjrequena.sample.shared.common.util.WKTParserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.UUID;

@SpringBootApplication(scanBasePackages = {
  "com.cjrequena.sample"
})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Log4j2
public class MainApplication implements CommandLineRunner {

  private final GeoShapeMapper geoShapeMapper;
  private final GeoShapeRepository geoShapeRepository;
  private final AreaService areaService;

  public static void main(String... args) {
    SpringApplication.run(MainApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
//    PointVO point = PointVO.of(37.12474775267529, -3.655513130688699);
//    Map<String, Object> metadataMap = new HashMap<>();
//    metadataMap.put("trace_id", UUID.randomUUID());
//    metadataMap.put("created_by", "Carlos Requena");
//    final MetadataVO metadataVO = MetadataVO.fromMap(metadataMap);
//    GeoShape geoShape = GeoShape
//      .builder()
//      .geometryType(GeometryType.POINT)
//      .geometry(GeometryVO.ofPoint(point))
//      .metadata(metadataVO)
//      .build();
//
//    final GeoShapeEntity geoShapeEntity = this.geoShapeMapper.toEntity(geoShape);
//    log.debug("{}", geoShapeEntity);
//    final GeoShapeEntity save = this.geoShapeRepository.save(geoShapeEntity);
//
//    PointVO point2 = PointVO.of(37.061782675899174, -3.758469352132365);
//    Map<String, Object> metadataMap2 = new HashMap<>();
//    metadataMap2.put("trace_id", UUID.randomUUID());
//    metadataMap2.put("created_by", "Carlos Requena");
//    final MetadataVO metadataVO2 = MetadataVO.fromMap(metadataMap2);
//    GeoShape geoShape2 = GeoShape
//      .builder()
//      .geometryType(GeometryType.POINT)
//      .geometry(GeometryVO.ofPoint(point2))
//      .metadata(metadataVO2)
//      .build();
//    final GeoShapeEntity geoShapeEntity2 = this.geoShapeMapper.toEntity(geoShape2);
//    final GeoShapeEntity save2 = this.geoShapeRepository.save(geoShapeEntity2);

//     final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
//
//
//    Point point = geometryFactory.createPoint(
//      new Coordinate(
//        -3.655513130688699, // longitude (X)
//        37.12474775267529   // latitude  (Y)
//      )
//    );
//    point.setSRID(4326);
//    PointVO pointVO = PointVO.of(37.12474775267529, -3.655513130688699);
//    //final List<GeoShapeEntity> withinDistance = this.geoShapeRepository.findWithinDistance(pointVO.toWKT(), 10);
//    final List<GeoShapeEntity> withinDistance = this.geoShapeRepository.findWithinDistance(point.toText(), 1001);
//
//    log.debug("{}", withinDistance);
//
//    final List<Area> all = areaService.findAll();
//    log.debug("{}", all);

    // POINT
    String pointWKT = "POINT (10.5 20.3)";
    Geometry point = WKTParserUtil.fromWKT(pointWKT, GeometryType.POINT);
    System.out.println("POINT     → " + WKTParserUtil.toWKT(point));
    final CoordinateVO coordinateVO = CoordinateVO.of(point.getCoordinate().y, point.getCoordinate().x);

    final GeoShape point1 = GeoShape.createPoint(
      UUID.randomUUID(),
      "POINT",
      coordinateVO,
      MetadataVO.empty()
    );

    log.debug("{}", point1);

    // CIRCLE (custom format → buffered polygon)
    String circleWKT = "CIRCLE(10.0 20.0, 5.0)";
    Geometry circle = WKTParserUtil.fromWKT(circleWKT, GeometryType.CIRCLE);
    System.out.println("CIRCLE    → " + WKTParserUtil.toWKT(circle)); // outputs as POLYGON

    // RECTANGLE (4-corner polygon)
    String rectangleWKT = "POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0))";
    Geometry rectangle = WKTParserUtil.fromWKT(rectangleWKT, GeometryType.RECTANGLE);
    System.out.println("RECTANGLE → " + WKTParserUtil.toWKT(rectangle));

    // POLYGON
    String polygonWKT = "POLYGON ((0 0, 4 0, 4 4, 0 4, 2 6, 0 0))";
    Geometry polygon = WKTParserUtil.fromWKT(polygonWKT, GeometryType.POLYGON);
    System.out.println("POLYGON   → " + WKTParserUtil.toWKT(polygon));

    // LINE
    String lineWKT = "LINESTRING (0 0, 5 5, 10 0)";
    Geometry line = WKTParserUtil.fromWKT(lineWKT, GeometryType.LINE);
    System.out.println("LINE      → " + WKTParserUtil.toWKT(line));
  }
}
