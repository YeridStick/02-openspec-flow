package com.soluciones;

import com.soluciones.infrastructure.SolucionesApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(classes = SolucionesApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InfrastructureTest {
    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17.11");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired JdbcClient jdbc;
    @LocalServerPort int port;

    @Test
    void connectsToApprovedDatabaseAndAppliesMigration() {
        assertThat(jdbc.sql("SHOW server_version").query(String.class).single()).startsWith("17.11");
        assertThat(jdbc.sql("SELECT count(*) FROM soluciones.flyway_schema_history WHERE version = '1' AND success")
                .query(Long.class).single()).isEqualTo(1L);
        assertThat(jdbc.sql("SHOW TIME ZONE").query(String.class).single()).isEqualTo("UTC");
    }

    @Test
    void exposesHealthyHttpEndpoint() throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            var response = client.send(HttpRequest.newBuilder(URI.create(
                    "http://localhost:" + port + "/actuator/health")).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).contains("\"status\":\"UP\"");
        }
    }
}
