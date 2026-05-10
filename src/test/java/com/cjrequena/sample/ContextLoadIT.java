package com.cjrequena.sample;

import com.cjrequena.sample.configuration.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("integrationTest")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ContextLoadIT extends TestcontainersConfiguration {

    @Test
    void contextLoad() {
        // Canonical test
        assertTrue(true);
    }
}
