package com.example.users.healthCheck;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public abstract class CustomHealthIndicator implements HealthIndicator {

    private static final String VERSION = "1.0.0";
    private final Instant startTime;

    public CustomHealthIndicator() {
        this.startTime = Instant.now();
    }

    @Override
    public Health health() {

        Duration uptime = Duration.between(startTime, Instant.now());
        Health.Builder builder = Health.up()
                .withDetail("version", VERSION)
                .withDetail("uptime", uptime.toString());

        // Agregar más detalles específicos de tu aplicación aquí

        return builder.build();
    }
}
