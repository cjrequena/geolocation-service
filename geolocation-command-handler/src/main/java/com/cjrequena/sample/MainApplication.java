package com.cjrequena.sample;

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

  public static void main(String... args) {
    SpringApplication.run(MainApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {

  }
}
