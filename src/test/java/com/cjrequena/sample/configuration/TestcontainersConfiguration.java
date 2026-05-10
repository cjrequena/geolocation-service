package com.cjrequena.sample.configuration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Shared Testcontainers configuration for integration tests.
 * Provides PostGIS and Redis containers that are reused across all IT classes.
 */
public abstract class TestcontainersConfiguration {

  static final PostgreSQLContainer<?> POSTGIS_CONTAINER =
    new PostgreSQLContainer<>(DockerImageName.parse("postgis/postgis:15-3.4").asCompatibleSubstituteFor("postgres"))
      .withDatabaseName("postgres")
      .withUsername("postgres")
      .withPassword("postgres");

  @SuppressWarnings("resource")
  static final GenericContainer<?> REDIS_CONTAINER =
    new GenericContainer<>(DockerImageName.parse("redis:alpine"))
      .withExposedPorts(6379);

  static {
    POSTGIS_CONTAINER.start();
    REDIS_CONTAINER.start();
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    // PostgreSQL / HikariCP datasource
    registry.add("spring.datasource.postgres.jdbcUrl", POSTGIS_CONTAINER::getJdbcUrl);
    registry.add("spring.datasource.postgres.username", POSTGIS_CONTAINER::getUsername);
    registry.add("spring.datasource.postgres.password", POSTGIS_CONTAINER::getPassword);

    // Redis
    registry.add("spring.redis.host", REDIS_CONTAINER::getHost);
    registry.add("spring.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379));
  }
}
