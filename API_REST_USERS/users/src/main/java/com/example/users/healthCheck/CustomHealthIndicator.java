package com.example.users.healthCheck;

import io.nats.client.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public abstract class CustomHealthIndicator implements HealthIndicator {

    private static final String VERSION = "1.0.0";
    private final Instant startTime;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Connection natsConnection;

    public CustomHealthIndicator() {
        this.startTime = Instant.now();
    }

    @Override
    public Health health() {
        Health.Builder builder = getHealthBuilder();
        return builder.build();
    }


    private Health.Builder getHealthBuilder() {
        Duration uptime = Duration.between(startTime, Instant.now());

        Health.Builder builder;
        try {
            jdbcTemplate.execute("SELECT 1"); // Verificar la conexión a la base de datos
            natsConnection.request("health_check", null); // Verificar la conexión a NATS
            builder = Health.up()
                    .withDetail("version", VERSION)
                    .withDetail("uptime", uptime.toString());
        } catch (Exception e) {
            builder = Health.down()
                    .withDetail("version", VERSION)
                    .withDetail("uptime", uptime.toString())
                    .withException(e);
        }

        return builder;
    }
}
