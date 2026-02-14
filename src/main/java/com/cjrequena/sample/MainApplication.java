package com.cjrequena.sample;

import com.cjrequena.sample.domain.mapper.GeoShapeMapper;
import com.cjrequena.sample.persistence.repository.GeoShapeRepository;
import com.cjrequena.sample.persistence.repository.LocationRepository;
import com.cjrequena.sample.service.AreaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
  "com.cjrequena.sample"
})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Log4j2
public class MainApplication implements CommandLineRunner {

  private final GeoShapeMapper geoShapeMapper;
  private final GeoShapeRepository geoShapeRepository;
  private final AreaService areaService;
  private final LocationRepository locationRepository;

  public static void main(String... args) {
    SpringApplication.run(MainApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
  }
}
