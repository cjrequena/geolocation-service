package com.cjrequena.sample.domain.model.command;

import com.cjrequena.sample.controller.dto.UpdateGeoShapeRequestDTO;
import com.cjrequena.sample.domain.model.GeoShape;
import com.cjrequena.sample.domain.model.enums.GeometryType;
import com.cjrequena.sample.domain.model.vo.CoordinateVO;
import com.cjrequena.sample.domain.model.vo.GeometryVO;
import com.cjrequena.sample.domain.model.vo.MetadataVO;
import com.cjrequena.sample.domain.model.vo.RadiusVO;
import com.cjrequena.sample.shared.common.util.WKTParserUtil;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import java.util.UUID;

@Getter
@ToString(callSuper = true)
public class UpdateGeoShapeCommand extends Command {

  private final GeoShape geoShape;

  @Builder
  public UpdateGeoShapeCommand(@Nonnull UUID id, @NotNull UpdateGeoShapeRequestDTO dto) {
    super(id);
    switch (dto.getGeometryType()) {
      case POINT -> {
        final Geometry geometry = WKTParserUtil.fromWKT(dto.getGeometryWKT(), GeometryType.POINT);
        final CoordinateVO coordinateVO = CoordinateVO.of(geometry.getCoordinate().y, geometry.getCoordinate().x);
        this.geoShape = GeoShape.createPoint(
          getDomainId(),
          dto.getName(),
          coordinateVO,
          dto.getMetadata() != null ? MetadataVO.of(dto.getMetadata()) : MetadataVO.empty()
        );
      }
      case CIRCLE -> {
        final Geometry geometry = WKTParserUtil.fromWKT(dto.getGeometryWKT(), GeometryType.CIRCLE);
        final CoordinateVO coordinateVO = CoordinateVO.of(geometry.getCentroid().getY(), geometry.getCentroid().getX());
        final Point centroid = geometry.getCentroid();
        Coordinate boundaryPoint = geometry.getCoordinates()[0];
        double radius = centroid.getCoordinate().distance(boundaryPoint);
        this.geoShape = GeoShape.createCircle(
          getDomainId(),
          dto.getName(),
          coordinateVO,
          RadiusVO.of(radius),
          dto.getMetadata() != null ? MetadataVO.of(dto.getMetadata()) : MetadataVO.empty()
        );

      }
      case RECTANGLE -> {
        final Geometry geometry = WKTParserUtil.fromWKT(dto.getGeometryWKT(), GeometryType.RECTANGLE);
        final CoordinateVO coordinateVO = CoordinateVO.of(geometry.getCentroid().getY(), geometry.getCentroid().getX());
        final GeometryVO geometryVO = GeometryVO.ofCoordinates(coordinateVO);
        this.geoShape = GeoShape.createRectangle(
          getDomainId(),
          dto.getName(),
          geometryVO,
          geometryVO.getBoundingBox().toBounds(),
          dto.getMetadata() != null ? MetadataVO.of(dto.getMetadata()) : MetadataVO.empty()
        );
      }
      case POLYGON -> {
        final Geometry geometry = WKTParserUtil.fromWKT(dto.getGeometryWKT(), GeometryType.POLYGON);
        final CoordinateVO coordinateVO = CoordinateVO.of(geometry.getCentroid().getY(), geometry.getCentroid().getX());
        final GeometryVO geometryVO = GeometryVO.ofCoordinates(coordinateVO);
        this.geoShape = GeoShape.createPolygon(
          getDomainId(),
          dto.getName(),
          geometryVO,
          geometryVO.getBoundingBox().toBounds(),
          dto.getMetadata() != null ? MetadataVO.of(dto.getMetadata()) : MetadataVO.empty()
        );
      }
      case LINE -> {
        final Geometry geometry = WKTParserUtil.fromWKT(dto.getGeometryWKT(), GeometryType.LINE);
        final CoordinateVO coordinateVO = CoordinateVO.of(geometry.getCentroid().getY(), geometry.getCentroid().getX());
        final GeometryVO geometryVO = GeometryVO.ofCoordinates(coordinateVO);
        this.geoShape = GeoShape.createLine(
          getDomainId(),
          dto.getName(),
          geometryVO,
          dto.getMetadata() != null ? MetadataVO.of(dto.getMetadata()) : MetadataVO.empty()
        );
      }
      default -> {
        throw new IllegalArgumentException("Unsupported geometry type: " + dto.getGeometryType());
      }
    }
  }
}
