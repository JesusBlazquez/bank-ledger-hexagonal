package io.github.jesusblazquez.ledger;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * One PostgreSQL container for the whole test suite.
 *
 * <p>Declaring the container as a bean lets Spring reuse the same application context — and
 * therefore the same container — across every integration test that imports this configuration,
 * instead of paying the start-up cost in each test class.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer("postgres:18-alpine");
    }
}
