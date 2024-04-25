package com.example.users.services;

import io.nats.client.Connection;
import io.nats.client.Nats;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NatsConfig {

    @Value("${nats.server.url}")
    private String natsServerUrl;

    @Bean
    public Connection natsConnection() throws Exception {
        System.out.println("Estableciendo conexion nats...");
        Connection connection = Nats.connect(natsServerUrl);
        System.out.println("Se establecio la conexion con nats");
        return connection;
    }
}